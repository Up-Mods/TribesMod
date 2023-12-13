package io.github.lukegrahamlandry.tribes.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.lukegrahamlandry.tribes.api.tribe.Relation;
import io.github.lukegrahamlandry.tribes.commands.util.TribeArgumentType;
import io.github.lukegrahamlandry.tribes.tribe_data.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.entity.player.Player;

public class NeutralTribeCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("neutral")
                .then(Commands.argument("tribe", TribeArgumentType.tribe())
                        .executes(NeutralTribeCommand::handleJoin)
                ).executes(ctx -> {
                            ctx.getSource().sendSuccess(TribeError.ARG_TRIBE.getText(), false);
                            return 0;
                        }
                );

    }

    public static int handleJoin(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Player player = context.getSource().getPlayerOrException();
        Tribe otherTribe = TribeArgumentType.getTribe(context, "tribe");

        Tribe yourTribe = TribesManager.getTribeOf(player.getUUID());

        var result = yourTribe.setRelation(player.getUUID(), otherTribe, Relation.Type.NEUTRAL);
        if (!result.success()) {
            context.getSource().sendFailure(result.error().getText());
        }

        TribeHelper.broadcastMessage(yourTribe, TribeSuccessType.NEUTRAL_TRIBE, player, context.getSource().getServer(), otherTribe);

        return Command.SINGLE_SUCCESS;
    }
}
