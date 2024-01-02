package io.github.lukegrahamlandry.tribes.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.lukegrahamlandry.tribes.commands.util.TribeArgumentType;
import io.github.lukegrahamlandry.tribes.init.NetworkHandler;
import io.github.lukegrahamlandry.tribes.network.PacketOpenJoinGUI;
import io.github.lukegrahamlandry.tribes.tribe_data.Tribe;
import io.github.lukegrahamlandry.tribes.tribe_data.TribeError;
import io.github.lukegrahamlandry.tribes.tribe_data.TribeSuccessType;
import io.github.lukegrahamlandry.tribes.tribe_data.TribesManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.PacketDistributor;

import static io.github.lukegrahamlandry.tribes.tribe_data.TribesManager.playerHasTribe;

public class JoinTribeCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("join")
                .then(Commands.argument("tribe", TribeArgumentType.tribe())
                        .executes(JoinTribeCommand::handleJoin)
                ).executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new PacketOpenJoinGUI(player));
                    return Command.SINGLE_SUCCESS;
                });
    }

    public static int handleJoin(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Player player = context.getSource().getPlayerOrException();
        Tribe tribe = TribeArgumentType.getTribe(context, "tribe");

        if (playerHasTribe(player.getUUID())) {
            context.getSource().sendFailure(TribeError.IN_TRIBE.getText());
            return 0;
        }

        var result = TribesManager.joinTribe(tribe, player);
        if (!result.success()) {
            context.getSource().sendFailure(result.error().getText());
        }

        context.getSource().sendSuccess(TribeSuccessType.YOU_JOINED.getText(tribe), true);

        return Command.SINGLE_SUCCESS;
    }
}
