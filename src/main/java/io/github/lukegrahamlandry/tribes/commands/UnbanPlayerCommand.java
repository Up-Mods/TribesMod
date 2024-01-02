package io.github.lukegrahamlandry.tribes.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.lukegrahamlandry.tribes.commands.util.OfflinePlayerArgumentType;
import io.github.lukegrahamlandry.tribes.tribe_data.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public class UnbanPlayerCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("unban")
                .then(Commands.argument("player", OfflinePlayerArgumentType.offlinePlayerID())
                        .executes(UnbanPlayerCommand::handleBan)
                );
    }

    public static int handleBan(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Player playerBanning = context.getSource().getPlayerOrException();
        UUID playerToUnban = OfflinePlayerArgumentType.getOfflinePlayer(context, "player");

        Tribe tribe = TribesManager.getTribeOf(playerBanning.getUUID());
        if (tribe == null) {
            context.getSource().sendFailure(TribeError.YOU_NOT_IN_TRIBE.getText());
            return 0;
        }
        var result = tribe.unbanPlayer(playerBanning.getUUID(), playerToUnban);

        if (!result.success()) {
            context.getSource().sendFailure(result.error().getText());
            return 0;
        }

        TribeHelper.broadcastMessage(tribe, TribeSuccessType.UNBAN_PLAYER, playerBanning, context.getSource().getServer(), OfflinePlayerArgumentType.getPlayerName(playerToUnban));

        return Command.SINGLE_SUCCESS;
    }
}
