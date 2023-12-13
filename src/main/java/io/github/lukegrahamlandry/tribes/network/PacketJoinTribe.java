package io.github.lukegrahamlandry.tribes.network;

import io.github.lukegrahamlandry.tribes.tribe_data.TribeSuccessType;
import io.github.lukegrahamlandry.tribes.tribe_data.TribesManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketJoinTribe {
    private String tribeName;

    // Read tribe name from PacketBuffer
    public PacketJoinTribe(FriendlyByteBuf buf) {
        this.tribeName = buf.readUtf(32767);
    }

    // Write tribe name to PacketBuffer
    public void toBytes(FriendlyByteBuf buf){
        buf.writeUtf(this.tribeName);
    }

    public PacketJoinTribe(String tribeNameIn){
        this.tribeName = tribeNameIn;
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        var player = ctx.get().getSender();
        ctx.get().enqueueWork(() -> {
            var tribe = TribesManager.findTribe(tribeName);
            var result = TribesManager.joinTribe(tribe, player);
            if (!result.success()) {
                player.displayClientMessage(result.error().getText(), false);
                return;
            }

            ctx.get().getSender().displayClientMessage(TribeSuccessType.YOU_JOINED.getText(tribeName), false);
        });
        ctx.get().setPacketHandled(true);
    }
}
