package io.github.lukegrahamlandry.tribes.network;

import io.github.lukegrahamlandry.tribes.commands.ConfirmCommand;
import io.github.lukegrahamlandry.tribes.tribe_data.Tribe;
import io.github.lukegrahamlandry.tribes.tribe_data.TribeError;
import io.github.lukegrahamlandry.tribes.tribe_data.TribeSuccessType;
import io.github.lukegrahamlandry.tribes.tribe_data.TribesManager;
import net.minecraft.Util;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

// client -> sever
public class PacketLeaveTribe {
    public PacketLeaveTribe(FriendlyByteBuf buf) {

    }

    public void encode(FriendlyByteBuf buf) {

    }

    public PacketLeaveTribe() {

    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        var player = ctx.get().getSender();
        ctx.get().enqueueWork(() -> {
            Tribe tribe = TribesManager.getTribeOf(player.getUUID());
            if (tribe != null) {
                player.sendMessage(TribeSuccessType.MUST_CONFIRM.getBlueText(), Util.NIL_UUID);
                ConfirmCommand.add(player, () -> {
                    TribesManager.leaveTribe(player);
                    player.sendMessage(TribeSuccessType.YOU_LEFT.getText(), Util.NIL_UUID);
                });
            } else {
                player.sendMessage(TribeError.YOU_NOT_IN_TRIBE.getText(), Util.NIL_UUID);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
