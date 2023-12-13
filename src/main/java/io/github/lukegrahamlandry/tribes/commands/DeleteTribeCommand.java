package io.github.lukegrahamlandry.tribes.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.lukegrahamlandry.tribes.tribe_data.TribesManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;

public class DeleteTribeCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("delete")
                .executes(DeleteTribeCommand::handleDelete);

    }

    public static int handleDelete(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Player player = context.getSource().getPlayerOrException();

        var result = TribesManager.deleteTribe(TribesManager.getTribeOf(player.getUUID()), player.getUUID(), context.getSource().getServer());
        if (!result.success()){
            context.getSource().sendFailure(result.error().getText());
        }
        else {
            //TODO translatable
            context.getSource().sendSuccess(new TextComponent("Successfully deleted tribe!"), true);
        }

        return Command.SINGLE_SUCCESS;
    }
}
