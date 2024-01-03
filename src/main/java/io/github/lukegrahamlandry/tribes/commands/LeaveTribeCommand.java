package io.github.lukegrahamlandry.tribes.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.lukegrahamlandry.tribes.tribe_data.Tribe;
import io.github.lukegrahamlandry.tribes.tribe_data.TribeError;
import io.github.lukegrahamlandry.tribes.tribe_data.TribeSuccessType;
import io.github.lukegrahamlandry.tribes.tribe_data.TribesManager;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class LeaveTribeCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("leave")
                .executes(LeaveTribeCommand::handleLeave);

    }

    public static int handleLeave(CommandContext<CommandSourceStack> source) throws CommandSyntaxException {
        ServerPlayer player = source.getSource().getPlayerOrException();

        Tribe tribe = TribesManager.getTribeOf(player.getUUID());
        if (tribe != null) {
            player.sendMessage(TribeSuccessType.MUST_CONFIRM_LEAVE.getBlueText(), Util.NIL_UUID);
            ConfirmCommand.add(player, () -> {
                TribesManager.leaveTribe(player);
                source.getSource().sendSuccess(TribeSuccessType.YOU_LEFT.getText(), true);
            });
        } else {
            source.getSource().sendFailure(TribeError.YOU_NOT_IN_TRIBE.getText());
            return 0;
        }

        return Command.SINGLE_SUCCESS;
    }
}
