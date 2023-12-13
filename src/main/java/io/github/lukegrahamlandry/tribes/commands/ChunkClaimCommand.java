package io.github.lukegrahamlandry.tribes.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.lukegrahamlandry.tribes.tribe_data.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;

public class ChunkClaimCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("chunk")
                .then(claim())
                .then(unclaim());
        // .then(who());  // done in ShowLandOwnerUI
    }

    private static ArgumentBuilder<CommandSourceStack, ?> claim() {
        return Commands.literal("claim")
                .executes(ChunkClaimCommand::handleClaim);
    }

    private static ArgumentBuilder<CommandSourceStack, ?> unclaim() {
        return Commands.literal("unclaim")
                .executes(ChunkClaimCommand::handleUnclaim);
    }

    private static ArgumentBuilder<CommandSourceStack, ?> who() {
        return Commands.literal("who")
                .executes(ChunkClaimCommand::handleWho);
    }

    public static int handleClaim(CommandContext<CommandSourceStack> source) throws CommandSyntaxException {
        Player player = source.getSource().getPlayerOrException();
        Tribe tribe = TribesManager.getTribeOf(player.getUUID());

        if (tribe == null) {
            source.getSource().sendSuccess(TribeError.YOU_NOT_IN_TRIBE.getText(), true);
            return Command.SINGLE_SUCCESS;
        }

        var result = tribe.claimChunk(getChunk(player), player.getUUID());
        if (!result.success()) {
            source.getSource().sendFailure(result.error().getText());
            return 0;
        } else {
            int x = result.value().x;
            int z = result.value().z;
            TribeHelper.broadcastMessage(tribe, TribeSuccessType.CLAIM_CHUNK, player, player.getServer(), x, z);
        }

        return Command.SINGLE_SUCCESS;
    }

    public static int handleUnclaim(CommandContext<CommandSourceStack> source) throws CommandSyntaxException {
        Player player = source.getSource().getPlayerOrException();
        Tribe tribe = TribesManager.getTribeOf(player.getUUID());

        if (tribe == null) {
            source.getSource().sendFailure(TribeError.YOU_NOT_IN_TRIBE.getText());
            return 0;
        }

        var result = tribe.unclaimChunk(getChunk(player), player.getUUID());
        if (!result.success()) {
            source.getSource().sendFailure(result.error().getText());
            return 0;
        }
        int x = result.value().x;
        int z = result.value().z;
        TribeHelper.broadcastMessage(tribe, TribeSuccessType.UNCLAIM_CHUNK, player, player.getServer(), x, z);

        return Command.SINGLE_SUCCESS;
    }

    public static int handleWho(CommandContext<CommandSourceStack> source) throws CommandSyntaxException {
        Player player = source.getSource().getPlayerOrException();

        Tribe owner = LandClaimHelper.getChunkOwner(getChunk(player));
        int x = getChunk(player).x;
        int z = getChunk(player).z;

        if (owner == null) {
            source.getSource().sendSuccess(new TextComponent("chunk (" + x + ", " + z + ") is unclaimed"), true);
        } else {
            source.getSource().sendSuccess(new TextComponent("chunk (" + x + ", " + z + ") is claimed by " + owner.getName() + " (" + owner.getInitials() + ")"), true);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static ChunkPos getChunk(Player player) {
        return player.getCommandSenderWorld().getChunkAt(player.blockPosition()).getPos();
    }
}
