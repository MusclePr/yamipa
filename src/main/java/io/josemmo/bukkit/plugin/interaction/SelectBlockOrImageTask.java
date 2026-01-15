package io.josemmo.bukkit.plugin.interaction;

import com.comphenix.protocol.events.ListenerPriority;
import io.josemmo.bukkit.plugin.YamipaPlugin;
import io.josemmo.bukkit.plugin.renderer.FakeImage;
import io.josemmo.bukkit.plugin.utils.ActionBar;
import io.josemmo.bukkit.plugin.utils.Logger;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class SelectBlockOrImageTask {
    private static final Logger LOGGER = Logger.getLogger("SelectBlockOrImageTask");
    private static final Map<UUID, SelectBlockOrImageTask> INSTANCES = new HashMap<>();
    private static @Nullable SelectBlockListener BLOCK_LISTENER;
    private static @Nullable SelectFakeItemFrameListener ITEM_FRAME_LISTENER;
    private final @NotNull Player player;
    private @Nullable BiConsumer<@NotNull Location, @NotNull BlockFace> blockCallback;
    private @Nullable Runnable cancelCallback;
    private @Nullable Consumer<@NotNull FakeImage> imageCallback;
    private @Nullable ActionBar actionBar;

    /**
     * Class constructor
     * @param player Target player instance
     */
    public SelectBlockOrImageTask(@NotNull Player player) {
        this.player = player;
    }

    /**
     * Set on block callback
     * @param callback Block callback
     */
    public void onBlock(@Nullable BiConsumer<@NotNull Location, @NotNull BlockFace> callback) {
        this.blockCallback = callback;
    }

    /**
     * Set on fake image callback
     * @param callback Fake image callback
     */
    public void onImage(@Nullable Consumer<@NotNull FakeImage> callback) {
        this.imageCallback = callback;
    }

    /**
     * Set on cancel callback
     * @param callback Cancel callback
     */
    public void onCancel(@Nullable Runnable callback) {
        this.cancelCallback = callback;
    }

    /**
     * Run task
     * @param helpMessage Help message for the player
     */
    @SuppressWarnings("deprecation")
    public synchronized void run(@NotNull String helpMessage) {
        // Keep track of this instance
        UUID uuid = player.getUniqueId();
        if (INSTANCES.containsKey(uuid)) {
            player.sendMessage(ChatColor.RED + "You already have a pending action!");
            return;
        }
        INSTANCES.put(uuid, this);

        // Create block listener singleton (if needed)
        if (BLOCK_LISTENER == null) {
            BLOCK_LISTENER = new SelectBlockListener() {
                @Override
                protected boolean onLeftClick(@NotNull Player player, @NotNull Block block, @NotNull BlockFace face) {
                    SelectBlockOrImageTask task = getInstance(player);
                    if (task == null) {
                        return true;
                    }
                    task.stop();
                    if (task.cancelCallback != null) {
                        task.cancelCallback.run();
                    }
                    return false;
                }

                @Override
                protected boolean onRightClick(@NotNull Player player, @NotNull Block block, @NotNull BlockFace face) {
                    SelectBlockOrImageTask task = getInstance(player);
                    if (task == null) {
                        return true;
                    }
                    task.stop();
                    if (task.blockCallback != null) {
                        task.blockCallback.accept(block.getLocation(), face);
                    }
                    return false;
                }

                @Override
                protected void onPlayerQuit(@NotNull Player player) {
                    SelectBlockOrImageTask task = getInstance(player);
                    if (task == null) {
                        return;
                    }
                    task.stop();
                    if (task.cancelCallback != null) {
                        task.cancelCallback.run();
                    }
                }
            };
            BLOCK_LISTENER.register();
            LOGGER.fine("Created SelectBlockListener singleton");
        }

        // Create fake item frame listener singleton (if needed)
        if (ITEM_FRAME_LISTENER == null) {
            ITEM_FRAME_LISTENER = new SelectFakeItemFrameListener() {
                @Override
                protected boolean onLeftClick(@NotNull Player player, int entityId) {
                    SelectBlockOrImageTask task = getInstance(player);
                    if (task == null) {
                        return true;
                    }
                    task.stop();
                    if (task.cancelCallback != null) {
                        task.cancelCallback.run();
                    }
                    return false;
                }

                @Override
                protected boolean onRightClick(@NotNull Player player, int entityId) {
                    SelectBlockOrImageTask task = getInstance(player);
                    if (task == null) {
                        return true;
                    }
                    task.stop();
                    if (task.imageCallback != null) {
                        notifyImage(player, task.imageCallback);
                    }
                    return false;
                }

                @Override
                protected @NotNull ListenerPriority getPriority() {
                    return ListenerPriority.LOW;
                }
            };
            ITEM_FRAME_LISTENER.register();
            LOGGER.fine("Created SelectFakeItemFrameListener singleton");
        }

        // Start action bar
        actionBar = ActionBar.repeat(
            player,
            ChatColor.GREEN + helpMessage + ChatColor.RESET + " - " + ChatColor.RED + "Left click to cancel"
        );
    }

    /**
     * Stop task
     */
    public synchronized void stop() {
        // Stop action bar
        if (actionBar != null) {
            actionBar.clear();
            actionBar = null;
        }

        // Remove reference to this instance
        INSTANCES.remove(player.getUniqueId());
        if (!INSTANCES.isEmpty()) {
            return;
        }

        // Destroy block listener singleton
        if (BLOCK_LISTENER != null) {
            BLOCK_LISTENER.unregister();
            BLOCK_LISTENER = null;
            LOGGER.fine("Destroyed SelectBlockListener singleton");
        }

        // Destroy fake item frame listener singleton
        if (ITEM_FRAME_LISTENER != null) {
            ITEM_FRAME_LISTENER.unregister();
            ITEM_FRAME_LISTENER = null;
            LOGGER.fine("Destroyed SelectFakeItemFrameListener singleton");
        }
    }

    /**
     * Get task instance for player
     * @param  player Targeted player
     * @return        Task instance
     */
    private synchronized @Nullable SelectBlockOrImageTask getInstance(@NotNull Player player) {
        return INSTANCES.get(player.getUniqueId());
    }

    /**
     * Notify image
     * @param player   Player looking at the image
     * @param callback Image callback
     */
    private void notifyImage(@NotNull Player player, @NotNull Consumer<@NotNull FakeImage> callback) {
        YamipaPlugin.getInstance().getScheduler().runInGame(() -> {
            FakeImage image = SelectFakeItemFrameListener.getFakeImage(player);
            if (image == null) {
                LOGGER.warning("Failed to get image selected by Player#" + player.getName());
                return;
            }
            callback.accept(image);
        }, player.getLocation(), 0);
    }
}
