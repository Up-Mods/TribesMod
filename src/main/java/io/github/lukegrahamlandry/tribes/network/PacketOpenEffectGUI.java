package io.github.lukegrahamlandry.tribes.network;

import io.github.lukegrahamlandry.tribes.api.tribe.EffectsInfo;
import io.github.lukegrahamlandry.tribes.api.tribe.TribeEffect;
import io.github.lukegrahamlandry.tribes.client.gui.TribeEffectScreen;
import io.github.lukegrahamlandry.tribes.tribe_data.Tribe;
import io.github.lukegrahamlandry.tribes.tribe_data.TribesManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PacketOpenEffectGUI {
    private final int numGood;
    private final int numBad;
    private final EffectsInfo effects;

    public PacketOpenEffectGUI(ServerPlayer player) {
        this.numGood = TribesManager.getNumberOfGoodEffects(player);
        this.numBad = TribesManager.getNumberOfBadEffects(player);
        Tribe tribe = TribesManager.getTribeOf(player.getUUID());
        this.effects = tribe.getEffects();
    }

    public PacketOpenEffectGUI(int good, int bad, EffectsInfo effects) {
        this.numGood = good;
        this.numBad = bad;
        this.effects = effects;
    }

    public static PacketOpenEffectGUI decode(FriendlyByteBuf buf) {
        int goodNum = buf.readVarInt();
        int badNum = buf.readVarInt();

        var lastChanged = Instant.ofEpochSecond(buf.readLong());
        int size = buf.readVarInt();
        List<TribeEffect> effectList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            int level = buf.readVarInt();
            MobEffect effect = buf.readRegistryId();
            effectList.add(new TribeEffect(effect, level));
        }

        return new PacketOpenEffectGUI(goodNum, badNum, new EffectsInfo(lastChanged, effectList));
    }

    public static void encode(PacketOpenEffectGUI packet, FriendlyByteBuf buf) {
        buf.writeVarInt(packet.numGood);
        buf.writeVarInt(packet.numBad);

        buf.writeLong(packet.effects.getLastChanged().orElse(Instant.EPOCH).getEpochSecond());

        buf.writeVarInt(packet.effects.getEffects().size());
        packet.effects.getEffects().forEach((tribeEffect) -> {
            buf.writeVarInt(tribeEffect.level());
            buf.writeRegistryId(tribeEffect.effect());
        });
    }

    public static void handle(PacketOpenEffectGUI packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> doOpen(packet));
        ctx.get().setPacketHandled(true);
    }


    @OnlyIn(Dist.CLIENT)
    private static void doOpen(PacketOpenEffectGUI packet) {
        Minecraft.getInstance().setScreen(new TribeEffectScreen(packet.numGood, packet.numBad, packet.effects));
    }
}
