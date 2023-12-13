package io.github.lukegrahamlandry.tribes.tribe_data;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class TribeHelper {
    public static void broadcastMessageNoCause(Tribe tribe, TribeSuccessType action, MinecraftServer server, Object... args) {
        Component text = action.getBlueText(args);

        for (var member : tribe.getMembers().values()) {
            Player player = server.getPlayerList().getPlayer(member.id());
            if (player != null) {
                player.displayClientMessage(text, false);
            }
        }
    }

    public static void broadcastMessage(Tribe tribe, TribeSuccessType action, @Nullable UUID causingPlayer, MinecraftServer server, Object... args) {
        Component text = action.getTextPrefixPlayer(causingPlayer, args);
        Component plainText = action.getText(args);

        for (var member : tribe.getMembers().values()) {
            Player player = server.getPlayerList().getPlayer(member.id());
            if (player != null) {
                boolean isCausingPlayer = member.id().equals(causingPlayer);
                player.displayClientMessage(isCausingPlayer ? plainText : text, false);
            }
        }
    }

    public static void broadcastMessage(Tribe tribe, TribeSuccessType action, Player causingPlayer, MinecraftServer server, Object... args) {
        broadcastMessage(tribe, action, causingPlayer.getUUID(), server, args);
    }
}
