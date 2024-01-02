package io.github.lukegrahamlandry.tribes.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.lukegrahamlandry.tribes.commands.util.OfflinePlayerArgumentType;
import io.github.lukegrahamlandry.tribes.tribe_data.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public class BanPlayerCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("ban")
                .then(Commands.argument("player", OfflinePlayerArgumentType.offlinePlayerID())
                        .executes(ctx -> {
                            var uuid = OfflinePlayerArgumentType.getOfflinePlayer(ctx, "offlinePlayer");
                            return handleBan(ctx, uuid);
                        })
                );
    }

    public static int handleBan(CommandContext<CommandSourceStack> context, UUID target) throws CommandSyntaxException {
        Player playerBanning = context.getSource().getPlayerOrException();
        var server = context.getSource().getServer();

        Tribe tribe = TribesManager.getTribeOf(playerBanning.getUUID());
        if (tribe == null) {
            context.getSource().sendFailure(TribeError.YOU_NOT_IN_TRIBE.getText());
            return 0;
        }

        var result = tribe.banPlayer(playerBanning.getUUID(), target);
        if (!result.success()) {
            context.getSource().sendFailure(result.error().getText());
            return 0;
        }

        TribeHelper.broadcastMessage(tribe, TribeSuccessType.BAN_PLAYER, playerBanning, server, OfflinePlayerArgumentType.getPlayerName(target));

        return Command.SINGLE_SUCCESS;
    }
}
