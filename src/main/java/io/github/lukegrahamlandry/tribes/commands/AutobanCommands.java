package io.github.lukegrahamlandry.tribes.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.lukegrahamlandry.tribes.api.tribe.Member;
import io.github.lukegrahamlandry.tribes.tribe_data.Tribe;
import io.github.lukegrahamlandry.tribes.tribe_data.TribeError;
import io.github.lukegrahamlandry.tribes.tribe_data.TribeSuccessType;
import io.github.lukegrahamlandry.tribes.tribe_data.TribesManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.entity.player.Player;

public class AutobanCommands {
    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("autoban")
                .then(Commands.literal("set")
                        .then(Commands.argument("numDeaths", IntegerArgumentType.integer(0, Integer.MAX_VALUE))
                                .then(Commands.argument("numDays", IntegerArgumentType.integer(0, Integer.MAX_VALUE))
                                        .executes(AutobanCommands::handleSet))))
                .then(Commands.literal("rank")
                        .then(Commands.argument("value", BoolArgumentType.bool())
                                .then(Commands.argument("rank", StringArgumentType.greedyString())
                                        .executes(AutobanCommands::handleRankSettings)))
                );
    }

    public static int handleSet(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Player player = context.getSource().getPlayerOrException();
        Tribe tribe = TribesManager.getTribeOf(player.getUUID());

        if (tribe == null) {
            context.getSource().sendFailure(TribeError.YOU_NOT_IN_TRIBE.getText());
            return 0;
        }

        if (tribe.getRankOf(player.getUUID()) != Member.Rank.LEADER) {
            context.getSource().sendFailure(TribeError.RANK_TOO_LOW.getText());
            return 0;
        }

        int numDeaths = IntegerArgumentType.getInteger(context, "numDeaths");
        int numDays = IntegerArgumentType.getInteger(context, "numDays");

        tribe.getAutobanInfo().setDeathsThreshold(numDeaths);
        tribe.getAutobanInfo().setDaysThreshold(numDays);

        context.getSource().sendSuccess(TribeSuccessType.AUTOBAN_NUMBERS.getText(numDeaths, numDays), true);

        return Command.SINGLE_SUCCESS;
    }

    public static int handleRankSettings(CommandContext<CommandSourceStack> source) throws CommandSyntaxException {
        Player player = source.getSource().getPlayerOrException();
        String rankName = StringArgumentType.getString(source, "rank");
        boolean value = BoolArgumentType.getBool(source, "value");

        Tribe tribe = TribesManager.getTribeOf(player.getUUID());
        if (tribe == null) {
            source.getSource().sendFailure(TribeError.YOU_NOT_IN_TRIBE.getText());
            return 0;
        }

        if (tribe.getRankOf(player.getUUID()) != Member.Rank.LEADER) {
            source.getSource().sendFailure(TribeError.RANK_TOO_LOW.getText());
            return 0;
        }

        Member.Rank rank = Member.Rank.fromString(rankName);
        if (rank == null) {
            source.getSource().sendFailure(TribeError.INVALID_RANK.getText());
            return 0;
        }

        if (value) {
            if (!tribe.getAutobanInfo().getRanks().contains(rank)) {
                tribe.getAutobanInfo().getRanks().add(rank);
            }

        } else {
            tribe.getAutobanInfo().getRanks().remove(rank);
        }

        if (value) {
            source.getSource().sendSuccess(TribeSuccessType.YES_AUTOBAN_RANK.getText(rankName), true);
        } else {
            source.getSource().sendSuccess(TribeSuccessType.NO_AUTOBAN_RANK.getText(rankName), true);
        }

        return Command.SINGLE_SUCCESS;
    }
}
