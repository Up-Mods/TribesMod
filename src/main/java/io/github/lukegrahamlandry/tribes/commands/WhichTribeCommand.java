package io.github.lukegrahamlandry.tribes.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.lukegrahamlandry.tribes.commands.util.OfflinePlayerArgumentType;
import io.github.lukegrahamlandry.tribes.tribe_data.Tribe;
import io.github.lukegrahamlandry.tribes.tribe_data.TribeSuccessType;
import io.github.lukegrahamlandry.tribes.tribe_data.TribesManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import java.util.UUID;

public class WhichTribeCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("who")
                .then(Commands.argument("player", OfflinePlayerArgumentType.offlinePlayerID())
                        .executes(WhichTribeCommand::handleCheck)
                );
    }

    public static int handleCheck(CommandContext<CommandSourceStack> source) throws CommandSyntaxException {
        UUID playerToCheck = OfflinePlayerArgumentType.getOfflinePlayer(source, "player");

        Tribe tribe = TribesManager.getTribeOf(playerToCheck);

        if (tribe == null) {
            source.getSource().sendSuccess(TribeSuccessType.WHICH_NO_TRIBE.getBlueText(OfflinePlayerArgumentType.getPlayerName(playerToCheck)), true);
        } else {
            source.getSource().sendSuccess(TribeSuccessType.WHICH_TRIBE.getBlueText(OfflinePlayerArgumentType.getPlayerName(playerToCheck), tribe), true);
        }

        return Command.SINGLE_SUCCESS;
    }
}
