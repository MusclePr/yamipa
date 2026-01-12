package io.josemmo.bukkit.plugin.interaction;

import io.josemmo.bukkit.plugin.YamipaPlugin;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public abstract class SelectBlockListener implements Listener {
    /**
     * On left click (attack)
     * @param  player Initiating player
     * @param  block  Targeted block
     * @param  face   Targeted block face
     * @return        Whether to allow original event or not
     */
    protected abstract boolean onLeftClick(@NotNull Player player, @NotNull Block block, @NotNull BlockFace face);

    /**
     * On right click (interact)
     * @param  player Initiating player
     * @param  block  Targeted block
     * @param  face   Targeted block face
     * @return        Whether to allow original event or not
     */
    protected abstract boolean onRightClick(@NotNull Player player, @NotNull Block block, @NotNull BlockFace face);

    /**
     * On player quit
     * @param player Initiating player
     */
    protected abstract void onPlayerQuit(@NotNull Player player);

    /**
     * Register listener
     */
    public final void register() {
        YamipaPlugin plugin = YamipaPlugin.getInstance();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Unregister listener
     */
    public final void unregister() {
        HandlerList.unregisterAll(this);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public final void onBlockInteraction(@NotNull PlayerInteractEvent event) {
        Player player = event.getPlayer();

        // Get targeted block
        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }
        BlockFace face = event.getBlockFace();

        // Handle event
        boolean allowEvent = true;
        Action action = event.getAction();
        if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            allowEvent = onLeftClick(player, block, face);
        } else if (action == Action.RIGHT_CLICK_BLOCK) {
            allowEvent = onRightClick(player, block, face);
        }

        // Cancel event (if needed)
        if (!allowEvent) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public final void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        onPlayerQuit(event.getPlayer());
    }
}
