package io.josemmo.bukkit.plugin.commands.arguments;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.josemmo.bukkit.plugin.utils.BlockFaceWithRotation;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import java.util.concurrent.CompletableFuture;

public class BlockFaceWithRotationArgument extends StringArgument {
    /**
     * Block face with rotation argument constructor
     * @param name Argument name
     */
    public BlockFaceWithRotationArgument(@NotNull String name) {
        super(name);
    }

    @Override
    public @NotNull CompletableFuture<Suggestions> suggest(@NotNull CommandSender sender, @NotNull SuggestionsBuilder builder) {
        for (BlockFaceWithRotation item : BlockFaceWithRotation.values()) {
            builder.suggest(item.name());
        }
        return builder.buildFuture();
    }

    @Override
    public @NotNull Object parse(@NotNull CommandSender sender, @NotNull Object rawValue) throws CommandSyntaxException {
        try {
            return BlockFaceWithRotation.valueOf((String) rawValue);
        } catch (IllegalArgumentException __) {
            throw newException("Unrecognized orientation \"" + rawValue + "\"");
        }
    }
}
