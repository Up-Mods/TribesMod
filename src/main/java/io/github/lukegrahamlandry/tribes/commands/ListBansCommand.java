package io.github.lukegrahamlandry.tribes.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.lukegrahamlandry.tribes.commands.util.OfflinePlayerArgumentType;
import io.github.lukegrahamlandry.tribes.tribe_data.Tribe;
import io.github.lukegrahamlandry.tribes.tribe_data.TribeError;
import io.github.lukegrahamlandry.tribes.tribe_data.TribeSuccessType;
import io.github.lukegrahamlandry.tribes.tribe_data.TribesManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;

import java.util.UUID;
import java.util.stream.Collectors;

public class ListBansCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("bans")
                .then(Commands.argument("player", OfflinePlayerArgumentType.offlinePlayerID())
                        .executes(ListBansCommand::handleListBans)
                )
                .executes(ListBansCommand::handleListBansTribe);
    }

    private static int handleListBansTribe(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var player = context.getSource().getPlayerOrException();
        var tribe = TribesManager.getTribeOf(player.getUUID());
        var server = context.getSource().getServer();

        if (tribe == null) {
            context.getSource().sendFailure(TribeError.YOU_NOT_IN_TRIBE.getText());
            return 0;
        }

        if(!tribe.getRankOf(player.getUUID()).isOfficerOrHigher()) {
            context.getSource().sendFailure(TribeError.RANK_TOO_LOW.getText());
            return 0;
        }

        //TODO translation, formatting, etc
        var banned = tribe.getBans();
        if (banned.isEmpty()) {
            context.getSource().sendSuccess(new TextComponent("No active bans."), false);
            return Command.SINGLE_SUCCESS;
        } else {
            var joined = banned.stream().map(id -> server.getProfileCache().get(id).map(GameProfile::getName).orElseGet(id::toString)).sorted().collect(Collectors.joining(", "));
            context.getSource().sendSuccess(new TextComponent(joined), false);
            return banned.size();
        }
    }

    ;

    public static int handleListBans(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        UUID playerToCheck = OfflinePlayerArgumentType.getOfflinePlayer(context, "player");

        var bannedIn = TribesManager.getBans(playerToCheck);
        if (bannedIn.isEmpty()) {
            context.getSource().sendSuccess(TribeSuccessType.NO_BANS.getBlueText(OfflinePlayerArgumentType.getPlayerName(playerToCheck)), true);
        } else {
            context.getSource().sendSuccess(TribeSuccessType.LIST_BANS.getBlueText(OfflinePlayerArgumentType.getPlayerName(playerToCheck), bannedIn.stream().map(Tribe::getName).collect(Collectors.joining(", "))), true);
        }

        return Command.SINGLE_SUCCESS;
    }
}
