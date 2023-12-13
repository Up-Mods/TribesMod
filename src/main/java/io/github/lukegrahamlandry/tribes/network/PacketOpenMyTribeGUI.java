package io.github.lukegrahamlandry.tribes.network;

import io.github.lukegrahamlandry.tribes.api.tribe.Member;
import io.github.lukegrahamlandry.tribes.api.tribe.Relation;
import io.github.lukegrahamlandry.tribes.client.gui.MyTribeScreen;
import io.github.lukegrahamlandry.tribes.tribe_data.Tribe;
import io.github.lukegrahamlandry.tribes.tribe_data.TribesManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PacketOpenMyTribeGUI {
    private final String tribeName;
    private final Member.Rank rank;
    private final String leader;
    private final int members;
    private final int tier;
    private final List<String> goodTribes;
    private final List<String> badTribes;

    public PacketOpenMyTribeGUI(ServerPlayer player) {
        Tribe tribe = TribesManager.getTribeOf(player.getUUID());
        this.goodTribes = new ArrayList<>();
        this.badTribes = new ArrayList<>();
        if (tribe != null){
            this.tribeName = tribe.getName();
            this.rank = tribe.getRankOf(player.getUUID());
            this.leader = player.getLevel().getPlayerByUUID(tribe.getLeaderId()).getScoreboardName();
            this.members = tribe.getMemberCount();
            this.tier = tribe.getTribeTier();
            tribe.getRelations().forEach((id, relation) -> {
                if (relation.type() == Relation.Type.ALLY) this.goodTribes.add(TribesManager.getTribe(id).getName());
                if (relation.type() == Relation.Type.ENEMY) this.badTribes.add(TribesManager.getTribe(id).getName());
            });
        } else {
            this.tribeName = "NOT IN TRIBE";
            this.rank = Member.Rank.BYPASS;
            this.leader = "NONE";
            this.members = 0;
            this.tier = 0;
        }
    }

    public PacketOpenMyTribeGUI(String tribeName, Member.Rank rank, String leader, int members, int tier, List<String> goodTribes, List<String> badTribes) {
        this.tribeName = tribeName;
        this.rank = rank;
        this.leader = leader;
        this.members = members;
        this.tier = tier;
        this.goodTribes = goodTribes;
        this.badTribes = badTribes;
    }

    public static PacketOpenMyTribeGUI decode(FriendlyByteBuf buf) {
        return new PacketOpenMyTribeGUI(buf.readUtf(32767), buf.readEnum(Member.Rank.class), buf.readUtf(32767), buf.readInt(), buf.readInt(), PacketUtil.readStringList(buf), PacketUtil.readStringList(buf));
    }

    public static void encode(PacketOpenMyTribeGUI packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.tribeName);
        buf.writeEnum(packet.rank);
        buf.writeUtf(packet.leader);
        buf.writeInt(packet.members);
        buf.writeInt(packet.tier);
        PacketUtil.writeStringList(buf, packet.goodTribes);
        PacketUtil.writeStringList(buf, packet.badTribes);
    }

    public static void handle(PacketOpenMyTribeGUI packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> doOpen(packet));
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void doOpen(PacketOpenMyTribeGUI packet) {
        Screen gui = new MyTribeScreen(packet.tribeName, packet.rank, packet.leader, packet.members, packet.tier, packet.goodTribes, packet.badTribes);
        Minecraft.getInstance().setScreen(gui);
    }
}
