package io.github.lukegrahamlandry.tribes.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.lukegrahamlandry.tribes.api.tribe.Member;
import io.github.lukegrahamlandry.tribes.tribe_data.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;

public class PromotePlayerCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("promote")
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(PromotePlayerCommand::handle)
                ).executes(ctx -> {
                            ctx.getSource().sendSuccess(TribeError.ARG_PLAYER.getText(), false);
                            return 0;
                        }
                );

    }

    public static int handle(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Player playerRunning = context.getSource().getPlayerOrException();
        Player playerTarget = EntityArgument.getPlayer(context, "player");
        MinecraftServer server = context.getSource().getServer();

        Tribe tribe = TribesManager.getTribeOf(playerRunning.getUUID());

        if (tribe == null) {
            context.getSource().sendFailure(TribeError.YOU_NOT_IN_TRIBE.getText());
            return 0;
        }

        // require confirm to demote yourself
        if (tribe.isViceLeaderOrHigher(playerTarget.getUUID())) {

            if (tribe.getRankOf(playerRunning.getUUID()) != Member.Rank.LEADER) {
                context.getSource().sendFailure(TribeError.RANK_TOO_LOW.getText());
                return 0;
            }

            context.getSource().sendSuccess(new TextComponent("make " + playerTarget.getName().getString() + " the leader of your tribe?"), true);

            ConfirmCommand.add(playerRunning, () -> {
                var result = tribe.promotePlayer(playerRunning.getUUID(), playerTarget.getUUID());

                if (!result.success()) {
                    context.getSource().sendFailure(result.error().getText());
                    return;
                }

                String rank = result.value().getSerializedName();
                TribeHelper.broadcastMessage(tribe, TribeSuccessType.PROMOTE, playerRunning, server, playerTarget, rank);
            });
            return Command.SINGLE_SUCCESS;
        }

        var result = tribe.promotePlayer(playerRunning.getUUID(), playerTarget.getUUID());

        if (!result.success()) {
            context.getSource().sendSuccess(result.error().getText(), true);
            return 0;
        }

        String rank = result.value().getSerializedName();
        TribeHelper.broadcastMessage(tribe, TribeSuccessType.PROMOTE, playerRunning, server, playerTarget, rank);

        return Command.SINGLE_SUCCESS;
    }
}
