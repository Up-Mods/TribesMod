package io.github.lukegrahamlandry.tribes.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.lukegrahamlandry.tribes.api.tribe.Relation;
import io.github.lukegrahamlandry.tribes.commands.util.TribeArgumentType;
import io.github.lukegrahamlandry.tribes.tribe_data.Tribe;
import io.github.lukegrahamlandry.tribes.tribe_data.TribeHelper;
import io.github.lukegrahamlandry.tribes.tribe_data.TribeSuccessType;
import io.github.lukegrahamlandry.tribes.tribe_data.TribesManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.entity.player.Player;

public class AllyTribeCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("ally")
                .then(Commands.argument("tribe", TribeArgumentType.tribe())
                        .executes(AllyTribeCommand::handleAlly)
                );

    }

    public static int handleAlly(CommandContext<CommandSourceStack> source) throws CommandSyntaxException {
        Player player = source.getSource().getPlayerOrException();
        Tribe otherTribe = TribeArgumentType.getTribe(source, "tribe");
        var server = source.getSource().getServer();

        Tribe yourTribe = TribesManager.getTribeOf(player.getUUID());

        var result = yourTribe.setRelation(player.getUUID(), otherTribe, Relation.Type.ALLY);
        if (result.success()) {
            TribeHelper.broadcastMessage(yourTribe, TribeSuccessType.ALLY_TRIBE, player, server, otherTribe);
        } else {
            source.getSource().sendFailure(result.error().getText());
        }

        return Command.SINGLE_SUCCESS;
    }
}
