package io.josemmo.bukkit.plugin.interaction;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.wrappers.EnumWrappers;
import io.josemmo.bukkit.plugin.YamipaPlugin;
import io.josemmo.bukkit.plugin.renderer.FakeImage;
import io.josemmo.bukkit.plugin.renderer.FakeItemFrame;
import io.josemmo.bukkit.plugin.utils.Internals;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public abstract class SelectFakeItemFrameListener implements PacketListener {
    private static final int MAX_BLOCK_DISTANCE = 5;

    /**
     * Get fake image instance for player
     * @param  player Player pointing at the image
     * @return        Fake image instance or <code>null</code> if not found
     */
    public static @Nullable FakeImage getFakeImage(@NotNull Player player) {
        // Get target block in range
        List<Block> lastTwoTargetBlocks = player.getLastTwoTargetBlocks(null, MAX_BLOCK_DISTANCE);
        if (lastTwoTargetBlocks.size() != 2) {
            return null;
        }
        Block targetBlock = lastTwoTargetBlocks.get(1);
        if (!targetBlock.getType().isSolid()) {
            return null;
        }

        // Get target block face
        Block adjacentBlock = lastTwoTargetBlocks.get(0);
        BlockFace targetBlockFace = targetBlock.getFace(adjacentBlock);
        if (targetBlockFace == null) {
            return null;
        }

        // Get fake image instance
        Location targetBlockLocation = targetBlock.getLocation();
        return YamipaPlugin.getInstance().getRenderer().getImage(targetBlockLocation, targetBlockFace);
    }

    /**
     * On left click (attack)
     * @param  player   Initiating player
     * @param  entityId Entity ID
     * @return          Whether to allow original event or not
     */
    protected abstract boolean onLeftClick(@NotNull Player player, int entityId);

    /**
     * On right click (interact)
     * @param  player   Initiating player
     * @param  entityId Entity ID
     * @return          Whether to allow original event or not
     */
    protected abstract boolean onRightClick(@NotNull Player player, int entityId);

    /**
     * Get listener priority
     * @return Listener priority
     */
    protected abstract @NotNull ListenerPriority getPriority();

    /**
     * Register listener
     */
    public final void register() {
        ProtocolLibrary.getProtocolManager().addPacketListener(this);
    }

    /**
     * Unregister listener
     */
    public final void unregister() {
        ProtocolLibrary.getProtocolManager().removePacketListener(this);
    }

    @Override
    public final void onPacketReceiving(@NotNull PacketEvent event) {
        // Ignore events for "real" in-game entities
        int entityId = event.getPacket().getIntegers().read(0);
        if (entityId < FakeItemFrame.MIN_FRAME_ID) {
            return;
        }

        // Get action
        EnumWrappers.EntityUseAction action;
        if (Internals.MINECRAFT_VERSION < 1700) {
            action = event.getPacket().getEntityUseActions().read(0);
        } else {
            action = event.getPacket().getEnumEntityUseActions().read(0).getAction();
        }

        // Handle event
        boolean allowEvent = true;
        Player player =  event.getPlayer();
        if (action == EnumWrappers.EntityUseAction.ATTACK) {
            allowEvent = onLeftClick(player, entityId);
        } else if (action == EnumWrappers.EntityUseAction.INTERACT_AT) {
            allowEvent = onRightClick(player, entityId);
        }

        // Cancel event (if needed)
        if (!allowEvent) {
            event.setCancelled(true);
        }
    }

    @Override
    public final void onPacketSending(@NotNull PacketEvent event) {
        // Intentionally left blank
    }

    @Override
    public final @NotNull ListeningWhitelist getReceivingWhitelist() {
        return ListeningWhitelist.newBuilder()
            .priority(getPriority())
            .types(PacketType.Play.Client.USE_ENTITY)
            .build();
    }

    @Override
    public final @NotNull ListeningWhitelist getSendingWhitelist() {
        return ListeningWhitelist.EMPTY_WHITELIST;
    }

    @Override
    public final @NotNull Plugin getPlugin() {
        return YamipaPlugin.getInstance();
    }
}
