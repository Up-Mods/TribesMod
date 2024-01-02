package io.github.lukegrahamlandry.tribes.network;

import io.github.lukegrahamlandry.tribes.api.tribe.TribeEffect;
import io.github.lukegrahamlandry.tribes.config.TribesConfig;
import io.github.lukegrahamlandry.tribes.tribe_data.Tribe;
import io.github.lukegrahamlandry.tribes.tribe_data.TribeError;
import io.github.lukegrahamlandry.tribes.tribe_data.TribesManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.network.NetworkEvent;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Supplier;

public class SaveEffectsPacket {
    private final Map<MobEffect, Integer> good;
    private final Map<MobEffect, Integer> bad;

    // send this from the client (effect screen) to save the chosen effects on the server
    public SaveEffectsPacket(Map<MobEffect, Integer> good, Map<MobEffect, Integer> bad) {
        this.good = good;
        this.bad = bad;
    }

    public SaveEffectsPacket(FriendlyByteBuf buf) {
        good = new HashMap<>();
        bad = new HashMap<>();

        while (true) {
            int type = buf.readInt();
            if (type == 0) {
                MobEffect effect = MobEffect.byId(buf.readInt());
                int level = buf.readInt();
                good.put(effect, level);
            } else if (type == 1) {
                MobEffect effect = MobEffect.byId(buf.readInt());
                int level = buf.readInt();
                bad.put(effect, level);
            } else {
                break;
            }
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        good.forEach((effect, level) -> {
            buf.writeInt(0);  // good
            buf.writeInt(MobEffect.getId(effect));
            buf.writeInt(level);
        });
        bad.forEach((effect, level) -> {
            buf.writeInt(1);  // bad
            buf.writeInt(MobEffect.getId(effect));
            buf.writeInt(level);
        });
        buf.writeInt(2);  // done
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer player = ctx.get().getSender();
        ctx.get().enqueueWork(() -> {
            UUID playerID = player.getUUID();
            Tribe tribe = TribesManager.getTribeOf(playerID);
            if (tribe != null) {
                var now = Instant.now();
                var lastChanged = tribe.getEffects().getLastChanged().orElse(Instant.MIN);
                var target = lastChanged.plus(TribesConfig.daysBetweenEffectsChanges(), ChronoUnit.DAYS);
                if (now.isBefore(target)) {
                    long hours = now.until(target, ChronoUnit.HOURS);
                    player.displayClientMessage(TribeError.WAIT_HOURS.getTextWithArgs(hours), false);
                    return;
                }

                if (!tribe.getRankOf(playerID).isViceLeaderOrHigher()) {
                    player.displayClientMessage(TribeError.RANK_TOO_LOW.getText(), false);
                    return;
                }

                // TODO: validate numbers of effects so hacked clients cant lie

                List<TribeEffect> effects = new ArrayList<>();
                this.good.forEach((effect, level) -> effects.add(new TribeEffect(effect, level)));
                this.bad.forEach((effect, level) -> effects.add(new TribeEffect(effect, level)));
                tribe.getEffects().setEffects(effects);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
