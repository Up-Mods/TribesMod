package io.github.lukegrahamlandry.tribes.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.lukegrahamlandry.tribes.tribe_data.TribeError;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ConfirmCommand {
    private static final Map<UUID, IConfirmAction> CONFIRM_ACTIONS = new HashMap<>();

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("confirm").executes(ConfirmCommand::handleConfirm);
    }

    public static void add(Player player, IConfirmAction action) {
        CONFIRM_ACTIONS.put(player.getUUID(), action);
    }

    public static int handleConfirm(CommandContext<CommandSourceStack> source) throws CommandSyntaxException {
        Player player = source.getSource().getPlayerOrException();


        IConfirmAction action = CONFIRM_ACTIONS.get(player.getUUID());
        if (action == null) {
            source.getSource().sendFailure(TribeError.NO_CONFIRM.getText());
            return 0;
        }

        action.call();

        return Command.SINGLE_SUCCESS;
    }

    public interface IConfirmAction {
        void call();
    }
}
