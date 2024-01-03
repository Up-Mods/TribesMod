package io.github.lukegrahamlandry.tribes.tribe_data;

import io.github.lukegrahamlandry.tribes.TribesMain;
import io.github.lukegrahamlandry.tribes.api.claims.Hemisphere;
import io.github.lukegrahamlandry.tribes.api.claims.HemisphereDirection;
import io.github.lukegrahamlandry.tribes.config.TribesConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;

import java.util.*;

// can only be used on the server side
public class LandClaimHelper {
    private static final HashMap<ChunkPos, Tribe> claimedChunks = new HashMap<>();
    public static Map<Hemisphere, List<Tribe>> hemispheres = new EnumMap<>(Hemisphere.class);

    // important to call this whenever tribes load
    public static void setup() {

        for (Tribe tribe : TribesManager.getTribes()) {
            for (ChunkPos chunk : tribe.getClaimedChunks()) {
                claimedChunks.put(chunk, tribe);
            }

            var hemisphere = tribe.getHemisphereAccess();
            if (hemisphere != Hemisphere.NONE) {
                hemispheres.computeIfAbsent(hemisphere, h -> new ArrayList<>()).add(tribe);
            }
        }
    }

    public static Tribe getChunkOwner(ChunkPos chunk) {
        return claimedChunks.get(chunk);
    }

    public static void unclaimChunk(ChunkPos chunk) {
        //TODO event
        setChunkOwner(chunk, null);
    }

    // do not call directly, use the version in Tribe
    // important that anything that should update the map calls it
    public static void setChunkOwner(ChunkPos chunk, Tribe tribe) {
        if (tribe == null) claimedChunks.remove(chunk);
        else claimedChunks.put(chunk, tribe);

        //TODO cant claim chunk in hemi you cant access
    }

    public static String getOwnerDisplayFor(Player player) {
        var chunk = player.getCommandSenderWorld().getChunkAt(player.blockPosition()).getPos();
        Tribe chunkOwner = getChunkOwner(chunk);

        if (chunkOwner != null) {
            return chunkOwner.getName() + " claimed chunk";
        }

        Hemisphere currentHemisphere = getHemisphereAt(player.blockPosition());

        return switch (currentHemisphere) {
            case NEGATIVE ->
                    (TribesConfig.getHemisphereDirection() == HemisphereDirection.NORTH_SOUTH ? "Northern" : "Western") + " Hemisphere";
            case POSITIVE ->
                    (TribesConfig.getHemisphereDirection() == HemisphereDirection.NORTH_SOUTH ? "Southern" : "Eastern") + " Hemisphere";
            case NONE -> "Wilderness";
        };

    }


    // considers chunk claims, hemisphere, death punishments
    public static boolean canAccessLandAt(Player player, BlockPos position) {
        Tribe interactingTribe = TribesManager.getTribeOf(player.getUUID());  // could be null

        // claimed chunk
        var chunk = player.getCommandSenderWorld().getChunkAt(position).getPos();
        Tribe chunkOwner = getChunkOwner(chunk);
        if (chunkOwner != null && !chunkOwner.equals(interactingTribe)) {
            return chunkOwner.claimDisableTime <= 0;  // respect pvp death penalties
        }

        Hemisphere currentHemisphere = getHemisphereAt(position);

        // no mans land
        if (currentHemisphere == Hemisphere.NONE) return true;

        // hemisphere
        if (TribesConfig.getRequireHemisphereAccess()) {
            return interactingTribe != null && hemispheres.getOrDefault(currentHemisphere, List.of()).contains(interactingTribe);
        }

        return true;
    }

    private static Hemisphere getHemisphereAt(BlockPos pos) {
        int coord = TribesConfig.getHemisphereDirection().applyAsInt(pos);
        int limit = TribesConfig.getHalfNoMansLandWidth();

        if (Math.abs(coord) <= limit) return Hemisphere.NONE;

        return coord < 0 ? Hemisphere.NEGATIVE : Hemisphere.POSITIVE;
    }

    public static List<ChunkPos> getClaimedChunksOrdered(ChunkPos start) {
        List<ChunkPos> chunks = new ArrayList<>(claimedChunks.keySet());
        chunks.sort((a, b) -> {

            double distA = Math.sqrt(Math.pow(start.x - a.x, 2) + Math.pow(start.z - a.z, 2));
            double distB = Math.sqrt(Math.pow(start.x - b.x, 2) + Math.pow(start.z - b.z, 2));

            return (int) (distA - distB);
        });

        return chunks;
    }

    // important to call this whenever a tribe is deleted
    public static void forgetTribe(Tribe tribe) {
        tribe.claims.forEach((chunk) -> LandClaimHelper.setChunkOwner(chunk, null));
        LandClaimHelper.hemispheres.forEach((hemisphere, theTribes) -> theTribes.remove(tribe));

        TribesManager.getTribes().forEach((aTribe) -> aTribe.getRelations().remove(tribe.getId()));

        TribesMain.LOGGER.debug("deleted tribe: " + tribe.getName());
    }
}
