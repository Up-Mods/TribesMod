package io.github.lukegrahamlandry.tribes.network;

import io.github.lukegrahamlandry.tribes.tribe_data.TribeSuccessType;
import io.github.lukegrahamlandry.tribes.tribe_data.TribesManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketCreateTribe {
    private final String tribeName;

    // Read tribe name from PacketBuffer
    public PacketCreateTribe(FriendlyByteBuf buf) {
        this.tribeName = buf.readUtf(32767);
    }

    // Write tribe name to PacketBuffer
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(this.tribeName);
    }

    public PacketCreateTribe(String tribeNameIn) {
        this.tribeName = tribeNameIn;
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        var player = ctx.get().getSender();
        ctx.get().enqueueWork(() -> {
            var result = TribesManager.createNewTribe(tribeName, player);
            if (result.success()) {
                player.displayClientMessage(TribeSuccessType.MADE_TRIBE.getText(result.value().getName()), false);
            } else {
                player.displayClientMessage(result.error().getText(), false);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
