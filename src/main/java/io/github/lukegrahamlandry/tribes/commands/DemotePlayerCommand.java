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

public class DemotePlayerCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("demote")
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(DemotePlayerCommand::handle)
                ).executes(ctx -> {
                            ctx.getSource().sendSuccess(TribeError.ARG_PLAYER.getText(), false);
                            return 0;
                        }
                );

    }

    public static int handle(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Player playerRunning = context.getSource().getPlayerOrException();
        Player playerTarget = EntityArgument.getPlayer(context, "player");

        Tribe tribe = TribesManager.getTribeOf(playerRunning.getUUID());
        if (tribe == null) {
            context.getSource().sendFailure(TribeError.YOU_NOT_IN_TRIBE.getText());
            return 0;
        }

        var result = tribe.demotePlayer(playerRunning.getUUID(), playerTarget.getUUID());

        if (!result.success()) {
            context.getSource().sendFailure(result.error().getText());
            return 0;
        }

        String rank = tribe.getRankOf(playerTarget.getUUID()).asString();
        TribeHelper.broadcastMessage(tribe, TribeSuccessType.DEMOTE, playerRunning, context.getSource().getServer(), playerTarget, rank);

        return Command.SINGLE_SUCCESS;
    }
}
