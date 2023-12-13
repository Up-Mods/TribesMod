package io.github.lukegrahamlandry.tribes.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.lukegrahamlandry.tribes.tribe_data.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.world.entity.player.Player;

public class BanPlayerCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("ban")
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(BanPlayerCommand::handleBan)
                ).executes(ctx -> {
                            ctx.getSource().sendSuccess(TribeError.ARG_PLAYER.getText(), false);
                            return 0;
                        }
                );

    }

    public static int handleBan(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Player playerBanning = context.getSource().getPlayerOrException();
        Player playerToBan = EntityArgument.getPlayer(context, "player");
        var server = context.getSource().getServer();

        Tribe tribe = TribesManager.getTribeOf(playerBanning.getUUID());
        if (tribe == null) {
            context.getSource().sendFailure(TribeError.YOU_NOT_IN_TRIBE.getText());
            return 0;
        }

        var result = tribe.banPlayer(playerBanning.getUUID(), playerToBan.getUUID());
        if (!result.success()) {
            context.getSource().sendFailure(result.error().getText());
            return 0;
        }

        TribeHelper.broadcastMessage(tribe, TribeSuccessType.BAN_PLAYER, playerBanning, server, playerToBan);

        return Command.SINGLE_SUCCESS;
    }
}
