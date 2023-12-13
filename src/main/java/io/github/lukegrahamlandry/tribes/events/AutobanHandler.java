package io.github.lukegrahamlandry.tribes.events;

import com.mojang.authlib.GameProfile;
import io.github.lukegrahamlandry.tribes.TribesMain;
import io.github.lukegrahamlandry.tribes.api.TribesAPI;
import io.github.lukegrahamlandry.tribes.tribe_data.Tribe;
import io.github.lukegrahamlandry.tribes.tribe_data.TribeHelper;
import io.github.lukegrahamlandry.tribes.tribe_data.TribeSuccessType;
import io.github.lukegrahamlandry.tribes.tribe_data.TribesManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = TribesMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AutobanHandler {
    private static final String NBT_KEY = TribesMain.MOD_ID + ":deaths";
    public static final GameProfile FAKE_PLAYER_PROFILE = new GameProfile(UUID.fromString("5481464d-9c06-4826-856a-29c3cdd96fb6"), "[Tribes] Auto-Ban");

    static {
        TribesAPI.registerFakePlayer(FAKE_PLAYER_PROFILE.getId());
    }

    @SubscribeEvent
    public static void autobanOnDeath(LivingDeathEvent event) {
        if (!(event.getEntityLiving() instanceof ServerPlayer player))
            return;

        Tribe tribe = TribesManager.getTribeOf(player.getUUID());
        if (tribe == null || !tribe.getAutobanInfo().getRanks().contains(tribe.getRankOf(player.getUUID())))
            return;

        var now = Instant.now();
        var threshold = now.minus(tribe.getAutobanInfo().getDaysThreshold(), ChronoUnit.DAYS);

        CompoundTag nbt = event.getEntityLiving().getPersistentData();
        long[] pastDeaths = new long[0];
        if (nbt.contains(NBT_KEY)) pastDeaths = nbt.getLongArray(NBT_KEY);

        List<Instant> recentDeaths = new ArrayList<>();
        recentDeaths.add(now);

        for (long deathTime : pastDeaths) {
            var value = Instant.ofEpochMilli(deathTime);
            if (value.isAfter(threshold)) {
                recentDeaths.add(value);
            }
        }

        int numDeathsWithinThreshold = recentDeaths.size();
        if (numDeathsWithinThreshold >= tribe.getAutobanInfo().getDeathsThreshold()) {
            TribesMain.LOGGER.debug(player.getUUID() + " has died " + numDeathsWithinThreshold + " within their tribe's autoban threshold! banning...");
            // todo; specify that its because they died too often
            TribeHelper.broadcastMessageNoCause(tribe, TribeSuccessType.BAN_FOR_DEATHS, player.getServer(), player);
            var fakePlayer = FakePlayerFactory.get(player.getLevel(), FAKE_PLAYER_PROFILE);
            tribe.banPlayer(fakePlayer.getUUID(), event.getEntityLiving().getUUID());
            nbt.putLongArray(NBT_KEY, recentDeaths.stream().map(Instant::toEpochMilli).toList());
            // hopefully it was passed by reference and not copied
        }
    }
}
