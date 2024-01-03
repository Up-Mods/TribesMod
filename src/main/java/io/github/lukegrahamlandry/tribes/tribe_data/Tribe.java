package io.github.lukegrahamlandry.tribes.tribe_data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lukegrahamlandry.tribes.api.TribesAPI;
import io.github.lukegrahamlandry.tribes.api.claims.Hemisphere;
import io.github.lukegrahamlandry.tribes.api.claims.HemisphereDirection;
import io.github.lukegrahamlandry.tribes.api.events.*;
import io.github.lukegrahamlandry.tribes.api.tribe.*;
import io.github.lukegrahamlandry.tribes.config.TribesConfig;
import io.github.lukegrahamlandry.tribes.util.CodecHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Tribe {
    private final UUID id;
    private String name;
    private String initials;
    private UUID leaderId;
    private boolean isPrivate;
    private Hemisphere hemisphereAccess;
    @Nullable
    private DeityInfo deityInfo;
    private final EffectsInfo effects;
    private final HashMap<UUID, Member> members;
    private final Map<UUID, Relation> relations;
    private final List<UUID> bans;
    private final AutoBanInfo autobans;
    //TODO move elsewhere
    private final List<UUID> pendingInvites;

    //FIXME move to LandClaimHelper
    List<ChunkPos> claims;

    //FIXME not serialized
    //FIXME move elsewhere
    public int claimDisableTime = 0;
    public int deathIndex = 0;
    public boolean deathWasPVP = false;

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public Tribe(UUID id, String name, String initials, UUID leaderId, boolean isPrivate, Hemisphere hemisphere, Optional<DeityInfo> deityInfo, EffectsInfo effects, Map<UUID, Member> members, Map<UUID, Relation> relations, List<UUID> bans, AutoBanInfo autoBanInfo, List<UUID> pendingInvites, List<ChunkPos> claims) {
        this.id = id;
        this.name = name;
        this.initials = initials;
        this.leaderId = leaderId;
        this.isPrivate = isPrivate;
        this.hemisphereAccess = hemisphere;
        this.deityInfo = deityInfo.orElse(null);
        this.effects = effects;
        this.members = new HashMap<>(members);
        this.relations = new HashMap<>(relations);
        this.bans = new ArrayList<>(bans);
        this.autobans = autoBanInfo;
        this.pendingInvites = new ArrayList<>(pendingInvites);
        this.claims = new ArrayList<>(claims);
    }

    public static Tribe of(String name, UUID leader, boolean isPrivate) {
        var tribe = new Tribe(UUID.randomUUID(), name, Character.toString(name.charAt(0)), leader, isPrivate, Hemisphere.NONE, Optional.empty(), new EffectsInfo(Optional.empty(), new ArrayList<>()), new HashMap<>(), new HashMap<>(), new ArrayList<>(), new AutoBanInfo(3, 2, new ArrayList<>()), new ArrayList<>(), new ArrayList<>());
        tribe.addMember(leader, Member.Rank.LEADER);
        tribe.autobans.getRanks().add(Member.Rank.MEMBER);
        tribe.autobans.getRanks().add(Member.Rank.OFFICER);
        return tribe;
    }

    public TribeResult<Member> addMember(UUID playerID, Member.Rank rank) {
        if (isBanned(playerID)) return TribeResult.error(TribeError.BANNED);
        if (TribesManager.playerHasTribe(playerID))
            return TribeResult.error(TribeError.IN_TRIBE);

        var info = new Member(playerID, rank);
        this.members.put(playerID, info);
        MinecraftForge.EVENT_BUS.post(new TribeMemberAddedEvent(this, playerID));
        return TribeResult.success(info);
    }

    public TribeResult<Void> banPlayer(UUID playerRunningCommand, UUID playerToBan) {
        if (this.getRankOf(playerRunningCommand).compareTo(this.getRankOf(playerToBan)) <= 0)
            return TribeResult.error(TribeError.RANK_TOO_LOW);

        var event = new TribeMemberBanEvent(this, playerRunningCommand, playerToBan);
        if (MinecraftForge.EVENT_BUS.post(event)) return TribeResult.error(TribeError.EVENT_CANCELLED);

        if (isMember(playerToBan)) {
            this.removeMember(playerToBan);
        }

        this.bans.add(playerToBan);

        return TribeResult.empty_success();
    }

    public TribeResult<Void> unbanPlayer(UUID playerRunningCommand, UUID playerToUnban) {
        if (!this.isOfficerOrHigher(playerRunningCommand)) return TribeResult.error(TribeError.RANK_TOO_LOW);

        var event = new TribeMemberUnbanEvent(this, playerRunningCommand, playerToUnban);
        if (MinecraftForge.EVENT_BUS.post(event)) return TribeResult.error(TribeError.EVENT_CANCELLED);

        this.bans.remove(playerToUnban);

        return TribeResult.empty_success();
    }

    public TribeResult<Member.Rank> promotePlayer(UUID playerRunningCommand, UUID playerToPromote) {
        if (!isMember(playerToPromote)) return TribeResult.error(TribeError.THEY_NOT_IN_TRIBE);

        var runRank = this.getRankOf(playerRunningCommand);
        var targetRank = this.getRankOf(playerToPromote);

        var newRank = targetRank.promotesTo();
        if (newRank == null) return TribeResult.error(TribeError.CANNOT_PROMOTE_RANK);
        if (runRank != Member.Rank.LEADER && runRank.compareTo(newRank) <= 0)
            return TribeResult.error(TribeError.RANK_TOO_LOW);

        var event = new TribeMemberPromotionEvent(this, playerRunningCommand, playerToPromote, TribeMemberPromotionEvent.Type.PROMOTION);
        if (MinecraftForge.EVENT_BUS.post(event)) return TribeResult.error(TribeError.EVENT_CANCELLED);

        this.setRank(playerToPromote, event.getNewRank(), playerRunningCommand);

        if (event.getNewRank() == Member.Rank.LEADER) {
            this.setRank(playerRunningCommand, Member.Rank.VICE_LEADER, playerRunningCommand);
        }

        return TribeResult.success(event.getNewRank());
    }

    public TribeResult<Member.Rank> demotePlayer(UUID playerRunningCommand, UUID playerToDemote) {
        if (!isMember(playerToDemote)) return TribeResult.error(TribeError.THEY_NOT_IN_TRIBE);

        var runRank = this.getRankOf(playerRunningCommand);
        var targetRank = this.getRankOf(playerToDemote);

        if (runRank.compareTo(targetRank) <= 0) return TribeResult.error(TribeError.RANK_TOO_LOW);

        var newRank = targetRank.demotesTo();
        if (newRank == null) return TribeResult.error(TribeError.CANNOT_DEMOTE_RANK);

        var event = new TribeMemberPromotionEvent(this, playerRunningCommand, playerToDemote, TribeMemberPromotionEvent.Type.DEMOTION);
        if (MinecraftForge.EVENT_BUS.post(event)) return TribeResult.error(TribeError.EVENT_CANCELLED);

        this.setRank(playerToDemote, event.getNewRank(), playerRunningCommand);

        return TribeResult.success(event.getNewRank());
    }

    public TribeResult<Relation.Type> setRelation(UUID player, Tribe otherTribe, Relation.Type type) {
        if (!this.isViceLeaderOrHigher(player)) return TribeResult.error(TribeError.RANK_TOO_LOW);
        if (this.getId().equals(otherTribe.getId())) return TribeResult.error(TribeError.SAME_TRIBE);

        //TODO event

        if (type == Relation.Type.NEUTRAL) {
            this.relations.remove(otherTribe.getId());
        } else {
            this.relations.put(otherTribe.getId(), new Relation(otherTribe.getId(), type));
        }

        return TribeResult.success(type);
    }

    public TribeResult<String> trySetInitials(String initials, UUID player, MinecraftServer server) {
        if (initials.length() > 4) return TribeResult.error(TribeError.NAME_TOO_LONG);
        if (!this.isViceLeaderOrHigher(player)) return TribeResult.error(TribeError.RANK_TOO_LOW);

        this.initials = initials;

        for (var member : this.members.values()) {
            Player toUpdate = server.getPlayerList().getPlayer(member.id());
            if (toUpdate != null) toUpdate.refreshDisplayName();
        }

        return TribeResult.success(initials);
    }

    public String getInitials() {
        return this.initials;
    }

    public void setRank(UUID player, Member.Rank rank, @Nullable UUID source) {
        if (!isMember(player)) {
            this.members.put(player, Member.of(player));
        }
        var info = this.members.get(player);
        var oldRank = info.rank();
        info.setRank(rank);

        // TODO event?
        if (rank == Member.Rank.LEADER) {
            this.leaderId = player;
        }

        MinecraftForge.EVENT_BUS.post(new TribeMemberRankUpdateEvent(this, source, player, oldRank, rank));
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<UUID, Member> getMembers() {
        return this.members;
    }

    public List<Member> byRank(Member.Rank rank) {
        return members.values().stream().filter((info) -> info.rank() == rank).toList();
    }

    public int getMemberCount() {
        return getMembers().size();
    }

    public Member.Rank getRankOf(UUID playerID) {
        if (TribesAPI.checkFakePlayerBypass(playerID)) return Member.Rank.BYPASS;
        return this.members.get(playerID).rank();
    }

    public int getTribeTier() {
        List<Integer> membersRequired = TribesConfig.getTierThresholds();
        for (int i = membersRequired.size() - 1; i >= 0; i--) {
            if (getMemberCount() >= membersRequired.get(i)) return i + 2;
        }

        return 1;
    }

    public static final Codec<Tribe> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            CodecHelper.UUID_STRING.fieldOf("id").forGetter(Tribe::getId),
            Codec.STRING.fieldOf("name").forGetter(Tribe::getName),
            Codec.STRING.fieldOf("initials").forGetter(Tribe::getInitials),
            CodecHelper.UUID_STRING.fieldOf("leader").forGetter(Tribe::getLeaderId),
            Codec.BOOL.fieldOf("private").forGetter(Tribe::isPrivate),
            Hemisphere.CODEC.optionalFieldOf("hemisphere", Hemisphere.NONE).forGetter(Tribe::getHemisphereAccess),
            DeityInfo.CODEC.optionalFieldOf("deity").forGetter(Tribe::getDeityInfo),
            EffectsInfo.CODEC.fieldOf("effects").forGetter(Tribe::getEffects),
            Member.MAP_CODEC.fieldOf("members").forGetter(Tribe::getMembers),
            Relation.MAP_CODEC.fieldOf("relations").forGetter(Tribe::getRelations),
            CodecHelper.UUID_STRING.listOf().fieldOf("bans").forGetter(Tribe::getBans),
            AutoBanInfo.CODEC.fieldOf("autoban_ranks").forGetter(Tribe::getAutobanInfo),
            CodecHelper.UUID_STRING.listOf().fieldOf("pending_invites").forGetter(Tribe::getPendingInvites),
            CodecHelper.CHUNK_POS.listOf().fieldOf("claims").forGetter(Tribe::getClaimedChunks)
    ).apply(instance, Tribe::new));

    public AutoBanInfo getAutobanInfo() {
        return autobans;
    }

    public List<UUID> getPendingInvites() {
        return pendingInvites;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public EffectsInfo getEffects() {
        return this.effects;
    }

    public Optional<DeityInfo> getDeityInfo() {
        return Optional.ofNullable(deityInfo);
    }

    public UUID getId() {
        return id;
    }

    public Map<UUID, Relation> getRelations() {
        return relations;
    }

    public List<UUID> getBans() {
        return bans;
    }

    public boolean isMember(UUID playerID) {
        return this.members.containsKey(playerID);
    }

    public Hemisphere getHemisphereAccess() {
        return hemisphereAccess;
    }

    public boolean isViceLeaderOrHigher(UUID player) {
        return this.getRankOf(player).isViceLeaderOrHigher();
    }

    public boolean isOfficerOrHigher(UUID player) {
        return this.getRankOf(player).isOfficerOrHigher();
    }

    public void removeMember(UUID playerID) {
        removeMember(playerID, null);
    }

    public void removeMember(UUID playerID, @Nullable UUID source) {
        int oldTier = getTribeTier();

        this.members.remove(playerID);
        MinecraftForge.EVENT_BUS.post(new TribeMemberRemovedEvent(this, source, playerID));

        if (this.members.isEmpty()) {
            TribesManager.forceDeleteTribe(this);
            return;
        }

        if (getRankOf(playerID) == Member.Rank.LEADER) {
            UUID toPromote = null;

            // if there's a vice leader pick them, otherwise officer
            // should use a list instead of set to select the person who joined the tribe first
            for (var testPlayer : this.members.values()) {
                UUID id = testPlayer.id();
                if (isViceLeaderOrHigher(id)) {
                    toPromote = id;
                    break;
                } else if (toPromote == null && isOfficerOrHigher(id)) {
                    toPromote = id;
                }
            }

            // no vice leaders or officer (can't be nobody in tribe cause that would be caught earlier and just delete the tribe)
            if (toPromote == null) {
                toPromote = this.members.keySet().iterator().next();
            }

            this.setRank(toPromote, Member.Rank.LEADER, source);
            // TODO broadcast message but needs server
        }

        // reset effects when you go down a tier
        if (oldTier < getTribeTier()) {
            this.effects.clear();
        }

        // TODO broadcast message but needs server
//        Player left = TribeServer.getPlayerByUuid(playerID);
//        if (left != null) this.broadcastMessageNoCause(TribeSuccessType.SOMEONE_LEFT, left);
    }

    public boolean isBanned(UUID player) {
        return this.bans.contains(player);
    }

    //TODO move to LandClaimHelper
    public TribeResult<ChunkPos> claimChunk(ChunkPos chunk, UUID player) {
        if (!this.isOfficerOrHigher(player)) return TribeResult.error(TribeError.RANK_TOO_LOW);
        if (LandClaimHelper.getChunkOwner(chunk) != null) return TribeResult.error(TribeError.ALREADY_CLAIMED);

        if (this.getClaimedChunks().size() >= TribesConfig.getMaxChunksForTier(this.getTribeTier()))
            return TribeResult.error(TribeError.MAX_CLAIMS_REACHED);

        this.claims.add(chunk);
        LandClaimHelper.setChunkOwner(chunk, this);

        return TribeResult.success(chunk);
    }

    //TODO move to LandClaimHelper
    public TribeResult<ChunkPos> unclaimChunk(ChunkPos chunk, UUID player) {
        if (!this.isOfficerOrHigher(player)) return TribeResult.error(TribeError.RANK_TOO_LOW);
        if (LandClaimHelper.getChunkOwner(chunk) != this) return TribeResult.error(TribeError.NOT_OWNED);

        this.claims.remove(chunk);
        LandClaimHelper.unclaimChunk(chunk);

        return TribeResult.success(chunk);
    }

    public TribeResult<Hemisphere> validateSelectHemi(Player player, String side) {
        var runRank = this.getRankOf(player.getUUID());
        if (runRank.compareTo(TribesConfig.rankToChooseHemisphere()) <= 0) return TribeResult.error(TribeError.RANK_TOO_LOW);
        if (this.getTribeTier() < TribesConfig.getMinTierToSelectHemi()) return TribeResult.error(TribeError.WEAK_TRIBE);
        if (this.hemisphereAccess != Hemisphere.NONE) return TribeResult.error(TribeError.ALREADY_HAVE_HEMISPHERE);
        if (TribesConfig.getHemisphereDirection() == HemisphereDirection.NORTH_SOUTH) {
            if (!side.equals("north") && !side.equals("south")) return TribeResult.error(TribeError.INVALID_HEMISPHERE);
        } else {
            if (!side.equals("east") && !side.equals("west")) return TribeResult.error(TribeError.INVALID_HEMISPHERE);
        }
        if (side.equals("east") || side.equals("south")) return TribeResult.success(Hemisphere.POSITIVE);
        else return TribeResult.success(Hemisphere.NEGATIVE);
    }

    public TribeResult<Hemisphere> selectHemisphere(Player player, Hemisphere hemisphere) {
        var runRank = this.getRankOf(player.getUUID());
        if (runRank.compareTo(TribesConfig.rankToChooseHemisphere()) <= 0) return TribeResult.error(TribeError.RANK_TOO_LOW);
        if (this.getTribeTier() < TribesConfig.getMinTierToSelectHemi()) return TribeResult.error(TribeError.WEAK_TRIBE);
        if (this.hemisphereAccess != Hemisphere.NONE) return TribeResult.error(TribeError.ALREADY_HAVE_HEMISPHERE);
        this.hemisphereAccess = hemisphere;
        LandClaimHelper.hemispheres.get(hemisphere).add(this);

        return TribeResult.success(this.hemisphereAccess);
    }

    public UUID getLeaderId() {
        return this.leaderId;
    }

    public List<ChunkPos> getClaimedChunks() {
        return this.claims;
    }

    public void setDeityInfo(@Nullable DeityInfo deityInfo) {
        this.deityInfo = deityInfo;
    }

    public void setPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }
}
