package io.github.lukegrahamlandry.tribes.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.lukegrahamlandry.tribes.tribe_data.Tribe;
import io.github.lukegrahamlandry.tribes.tribe_data.TribeHelper;
import io.github.lukegrahamlandry.tribes.tribe_data.TribeSuccessType;
import io.github.lukegrahamlandry.tribes.tribe_data.TribesManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;

public class SetInitialsCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("initials")
                .then(Commands.argument("name", StringArgumentType.word())
                        .executes(SetInitialsCommand::handleCreate)
                );
    }

    public static int handleCreate(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Player player = context.getSource().getPlayerOrException();
        String str = StringArgumentType.getString(context, "name");
        MinecraftServer server = context.getSource().getServer();

        Tribe tribe = TribesManager.getTribeOf(player.getUUID());
        var result = tribe.trySetInitials(str, player.getUUID(), server);
        if (!result.success()) {
            context.getSource().sendFailure(result.error().getText());
            return 0;
        }

        TribeHelper.broadcastMessage(tribe, TribeSuccessType.SET_INITIALS, player, server, str);

        return Command.SINGLE_SUCCESS;
    }
}
