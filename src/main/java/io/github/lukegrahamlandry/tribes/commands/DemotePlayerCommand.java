package io.github.lukegrahamlandry.tribes.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.lukegrahamlandry.tribes.commands.util.OfflinePlayerArgumentType;
import io.github.lukegrahamlandry.tribes.tribe_data.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public class DemotePlayerCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("demote")
                .then(Commands.argument("player", OfflinePlayerArgumentType.offlinePlayerID())
                        .executes(DemotePlayerCommand::handle)
                );
    }

    public static int handle(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Player playerRunning = context.getSource().getPlayerOrException();
        UUID target = OfflinePlayerArgumentType.getOfflinePlayer(context, "player");

        Tribe tribe = TribesManager.getTribeOf(playerRunning.getUUID());
        if (tribe == null) {
            context.getSource().sendFailure(TribeError.YOU_NOT_IN_TRIBE.getText());
            return 0;
        }

        var result = tribe.demotePlayer(playerRunning.getUUID(), target);

        if (!result.success()) {
            context.getSource().sendFailure(result.error().getText());
            return 0;
        }

        String rank = tribe.getRankOf(target).asString();
        TribeHelper.broadcastMessage(tribe, TribeSuccessType.DEMOTE, playerRunning, context.getSource().getServer(), OfflinePlayerArgumentType.getPlayerName(target), rank);

        return Command.SINGLE_SUCCESS;
    }
}
