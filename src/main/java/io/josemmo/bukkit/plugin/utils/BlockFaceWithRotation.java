package io.josemmo.bukkit.plugin.utils;

import org.bukkit.Location;
import org.bukkit.Rotation;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a block face with a rotation for the up and down faces.
 */
public enum BlockFaceWithRotation {
    NORTH(BlockFace.NORTH, Rotation.NONE),
    SOUTH(BlockFace.SOUTH, Rotation.NONE),
    EAST(BlockFace.EAST, Rotation.NONE),
    WEST(BlockFace.WEST, Rotation.NONE),
    UP_0DEG(BlockFace.UP, Rotation.NONE),
    UP_45DEG(BlockFace.UP, Rotation.CLOCKWISE_45),
    UP_90DEG(BlockFace.UP, Rotation.CLOCKWISE),
    UP_135DEG(BlockFace.UP, Rotation.CLOCKWISE_135),
    DOWN_0DEG(BlockFace.DOWN, Rotation.NONE),
    DOWN_45DEG(BlockFace.DOWN, Rotation.CLOCKWISE_45),
    DOWN_90DEG(BlockFace.DOWN, Rotation.CLOCKWISE),
    DOWN_135DEG(BlockFace.DOWN, Rotation.CLOCKWISE_135);

    private final @NotNull BlockFace blockFace;
    private final @NotNull Rotation rotation;

    /**
     * Get instance from player eyesight
     * @param  blockFace Targeted block face
     * @param  eyesight  Player eye location
     * @return           Block face with rotation instance
     */
    public static @NotNull BlockFaceWithRotation fromPlayerEyesight(
        @NotNull BlockFace blockFace,
        @NotNull Location eyesight
    ) {
        // Images placed on N/S/E/W faces never have rotation
        switch (blockFace) {
            case NORTH:
                return NORTH;
            case SOUTH:
                return SOUTH;
            case EAST:
                return EAST;
            case WEST:
                return WEST;
        }

        // Top and down images depend on where player is looking
        BlockFace eyeDirection = DirectionUtils.getCardinalDirection(eyesight.getYaw());
        switch (eyeDirection) {
            case EAST:
                return (blockFace == BlockFace.DOWN) ? DOWN_135DEG : UP_45DEG;
            case SOUTH:
                return (blockFace == BlockFace.DOWN) ? DOWN_90DEG : UP_90DEG;
            case WEST:
                return (blockFace == BlockFace.DOWN) ? DOWN_45DEG : UP_135DEG;
            default:
                return (blockFace == BlockFace.DOWN) ? DOWN_0DEG : UP_0DEG;
        }
    }

    /**
     * Class constructor
     * @param blockFace Block face
     * @param rotation  Rotation
     */
    BlockFaceWithRotation(@NotNull BlockFace blockFace, @NotNull Rotation rotation) {
        this.blockFace = blockFace;
        this.rotation = rotation;
    }

    /**
     * Get block face
     * @return Block face
     */
    public @NotNull BlockFace getBlockFace() {
        return blockFace;
    }

    /**
     * Get rotation
     * @return Rotation
     */
    public @NotNull Rotation getRotation() {
        return rotation;
    }
}
