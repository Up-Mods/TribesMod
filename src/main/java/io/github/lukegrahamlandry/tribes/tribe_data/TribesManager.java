package io.github.lukegrahamlandry.tribes.tribe_data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import io.github.lukegrahamlandry.tribes.api.tribe.Member;
import io.github.lukegrahamlandry.tribes.config.TribesConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;

public class TribesManager {

    private static final Logger logger = LogUtils.getLogger();

    private static final Set<UUID> FAKE_PLAYER_BYPASS_CHECKS = new HashSet<>();
    private static Map<UUID, Tribe> tribes = new HashMap<>();

    public static TribeResult<Tribe> createNewTribe(String name, Player player) {
        if (playerHasTribe(player.getUUID())) return TribeResult.error(TribeError.IN_TRIBE);
        if (name.length() > TribesConfig.getMaxTribeNameLength())
            return TribeResult.error(TribeError.NAME_TOO_LONG);  // should be caught by the create GUI
        if (getTribes().size() >= TribesConfig.getMaxNumberOfTribes()) return TribeResult.error(TribeError.CONFIG);

        //TODO pass along that boolean
        var tribe = Tribe.of(name, player.getUUID(), false);
        tribes.put(tribe.getId(), tribe);
        return TribeResult.success(tribe);
    }

    public static TribeResult<Member> joinTribe(Tribe tribe, Player player) {
        if (playerHasTribe(player.getUUID())) return TribeResult.error(TribeError.IN_TRIBE);

        if (tribe.isPrivate() && !tribe.getPendingInvites().contains(player.getUUID()))
            return TribeResult.error(TribeError.IS_PRIVATE);

        TribeHelper.broadcastMessageNoCause(tribe, TribeSuccessType.SOMEONE_JOINED, player.getServer(), player);

        return tribe.addMember(player.getUUID(), Member.Rank.MEMBER);
    }

    public static TribeResult<Void> deleteTribe(Tribe tribe, UUID playerID, MinecraftServer server) {
        if (tribe.getRankOf(playerID) != Member.Rank.LEADER) return TribeResult.error(TribeError.RANK_TOO_LOW);

        LandClaimHelper.forgetTribe(tribe);
        TribeHelper.broadcastMessage(tribe, TribeSuccessType.DELETE_TRIBE, playerID, server);
        tribes.remove(tribe.getId(), tribe);

        return TribeResult.empty_success();
    }

    public static void forceDeleteTribe(Tribe tribe) {
        LandClaimHelper.forgetTribe(tribe);
        tribes.remove(tribe.getId());
    }

    static public boolean isNameAvailable(String name) {
        return tribes.values().stream().noneMatch(tribe -> tribe.getName().equalsIgnoreCase(name));
    }

    static public List<Tribe> getTribes() {
        if (tribes.isEmpty()) {
            return List.of();
        }
        return new ArrayList<>(tribes.values());
    }

    public static Tribe getTribe(UUID id) {
        return tribes.get(id);
    }

    @Nullable
    public static Tribe findTribe(String name) {
        return tribes.values().stream().filter(tribe -> tribe.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public static boolean playerHasTribe(UUID playerID) {
        return getTribeOf(playerID) != null;
    }

    public static Tribe getTribeOf(UUID playerID) {
        return tribes.values().stream().filter(tribe -> tribe.isMember(playerID)).findFirst().orElse(null);
    }

    public static JsonElement toJson() {
        return Tribe.CODEC.listOf().encodeStart(JsonOps.INSTANCE, List.copyOf(tribes.values())).resultOrPartial(logger::error).orElseThrow();
    }

    public static void fromJson(JsonArray obj) {

        tribes.clear();

        Tribe.CODEC.listOf().parse(JsonOps.INSTANCE, obj).resultOrPartial(logger::error).orElseThrow().forEach(tribe -> tribes.put(tribe.getId(), tribe));
    }

    public static TribeResult<Void> leaveTribe(Player player) {
        if (!playerHasTribe(player.getUUID())) return TribeResult.error(TribeError.YOU_NOT_IN_TRIBE);
        Tribe tribe = getTribeOf(player.getUUID());
        tribe.removeMember(player.getUUID());
        return TribeResult.empty_success();
    }

    public static List<Tribe> getBans(UUID playerToCheck) {
        return tribes.values().stream().filter(tribe -> tribe.isBanned(playerToCheck)).toList();
    }

    public static int getNumberOfGoodEffects(Player player) {
        int tier = getTribeOf(player.getUUID()).getTribeTier();
        return TribesConfig.getTierPositiveEffects().get(tier - 1);
    }

    public static int getNumberOfBadEffects(Player player) {
        int tier = getTribeOf(player.getUUID()).getTribeTier();
        return TribesConfig.getTierNegativeEffects().get(tier - 1);
    }

    public static void renameTribe(Tribe tribe, String newName) {
        //TODO event
        tribe.setName(newName);
    }

    public static void registerFakePlayer(UUID id) {
        FAKE_PLAYER_BYPASS_CHECKS.add(id);
    }

    public static boolean doesPlayerBypassChecks(UUID id) {
        return FAKE_PLAYER_BYPASS_CHECKS.contains(id);
    }
}
