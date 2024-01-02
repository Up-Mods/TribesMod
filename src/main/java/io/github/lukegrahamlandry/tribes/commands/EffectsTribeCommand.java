package io.github.lukegrahamlandry.tribes.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.lukegrahamlandry.tribes.config.TribesConfig;
import io.github.lukegrahamlandry.tribes.init.NetworkHandler;
import io.github.lukegrahamlandry.tribes.network.PacketOpenEffectGUI;
import io.github.lukegrahamlandry.tribes.tribe_data.Tribe;
import io.github.lukegrahamlandry.tribes.tribe_data.TribeError;
import io.github.lukegrahamlandry.tribes.tribe_data.TribesManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class EffectsTribeCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("effects")
                .executes(EffectsTribeCommand::handleeffects);

    }

    public static int handleeffects(CommandContext<CommandSourceStack> source) throws CommandSyntaxException {
        ServerPlayer player = source.getSource().getPlayerOrException();

        Tribe tribe = TribesManager.getTribeOf(player.getUUID());
        if (tribe == null) {
            source.getSource().sendFailure(TribeError.YOU_NOT_IN_TRIBE.getText());
            return 0;
        }

        var now = Instant.now();
        var lastChanged = tribe.getEffects().getLastChanged().orElse(Instant.MIN);
        var target = lastChanged.plus(TribesConfig.daysBetweenEffectsChanges(), ChronoUnit.DAYS);
        if (now.isBefore(target)) {
            long hours = now.until(target, ChronoUnit.HOURS);
            source.getSource().sendFailure(TribeError.WAIT_HOURS.getTextWithArgs(hours));
            return 0;
        }

        if (!tribe.getRankOf(player.getUUID()).isViceLeaderOrHigher()) {
            source.getSource().sendFailure(TribeError.RANK_TOO_LOW.getText());
            return 0;
        }

        NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new PacketOpenEffectGUI(player));

        return Command.SINGLE_SUCCESS;
    }
}
