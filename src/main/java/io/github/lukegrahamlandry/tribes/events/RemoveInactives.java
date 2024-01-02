package io.github.lukegrahamlandry.tribes.events;

import com.google.gson.*;
import com.mojang.authlib.GameProfile;
import io.github.lukegrahamlandry.tribes.TribesMain;
import io.github.lukegrahamlandry.tribes.config.TribesConfig;
import io.github.lukegrahamlandry.tribes.tribe_data.Tribe;
import io.github.lukegrahamlandry.tribes.tribe_data.TribesManager;
import net.minecraft.server.MinecraftServer;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RemoveInactives {
    private static final Map<UUID, Instant> lastPlayTimes = new HashMap<>();

    public static JsonObject toJson() {
        JsonObject players = new JsonObject();
        lastPlayTimes.forEach((uuid, time) -> players.addProperty(uuid.toString(), time.toString()));

        return players;
    }

    public static void load(JsonObject players) {
        for (Map.Entry<String, JsonElement> e : players.entrySet()) {
            UUID player = UUID.fromString(e.getKey());
            Instant time = Instant.parse(e.getValue().getAsString());
            lastPlayTimes.put(player, time);
        }
    }

    public static void recordActive(UUID player) {
        lastPlayTimes.put(player, Instant.now());
    }

    public static void check(MinecraftServer server) {
        if (TribesConfig.removeInactiveAfterDays.get() == 0) return;

        var now = Instant.now();
        server.getPlayerList().getPlayers().forEach(player -> lastPlayTimes.put(player.getUUID(), now));

        var threshold = now.minus(TribesConfig.removeInactiveAfterDays.get(), ChronoUnit.DAYS);
        lastPlayTimes.forEach((uuid, loginTime) -> {
            Tribe tribe = TribesManager.getTribeOf(uuid);
            if (tribe != null) {
                if (loginTime.isBefore(threshold)) {
                    var username = server.getProfileCache().get(uuid).map(GameProfile::getName).orElseGet(uuid::toString);
                    TribesMain.LOGGER.debug("removing inactive member {} from tribe {} (last seen: {})", username, tribe.getName(), loginTime);
                    // todo: message to everyone that the player has been kicked
                    // todo: message to the player when they join again
                    tribe.removeMember(uuid);
                }
            }
        });
    }
}
