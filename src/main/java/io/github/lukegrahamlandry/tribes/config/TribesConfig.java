package io.github.lukegrahamlandry.tribes.config;

import com.electronwill.nightconfig.core.EnumGetMethod;
import io.github.lukegrahamlandry.tribes.api.claims.HemisphereDirection;
import io.github.lukegrahamlandry.tribes.api.tribe.Member;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TribesConfig {
    //Declaration of config variables
    private static ForgeConfigSpec.IntValue numTribes;
    private static ForgeConfigSpec.BooleanValue tribeRequired;
    private static ForgeConfigSpec.ConfigValue<List<? extends Integer>> tierThresholds;
    private static ForgeConfigSpec.ConfigValue<List<? extends Integer>> tierNegEffects;
    private static ForgeConfigSpec.ConfigValue<List<? extends Integer>> tierPosEffects;

    private static ForgeConfigSpec.BooleanValue friendlyFire;

    // land claiming
    private static ForgeConfigSpec.BooleanValue requireHemisphereAccess;
    private static ForgeConfigSpec.IntValue tierForSelectHemisphere;
    private static ForgeConfigSpec.ConfigValue<List<? extends Integer>> maxChunksClaimed;
    private static ForgeConfigSpec.EnumValue<HemisphereDirection> hemisphereDirection;
    private static ForgeConfigSpec.IntValue halfNoMansLandWidth;
    private static ForgeConfigSpec.EnumValue<Member.Rank> rankToChooseHemisphere;

    private static ForgeConfigSpec.ConfigValue<List<? extends Integer>> nonpvpDeathPunishTimes;
    private static ForgeConfigSpec.ConfigValue<List<? extends Integer>> pvpDeathPunishTimes;

    private static ForgeConfigSpec.IntValue daysBetweenDeityChange;
    private static ForgeConfigSpec.IntValue daysBetweenEffectsChange;

    private static ForgeConfigSpec.ConfigValue<List<? extends String>> ignoredEffects;
    public static ForgeConfigSpec.IntValue removeInactiveAfterDays;

    private static ForgeConfigSpec.ConfigValue<String> landOwnerDisplayPosition;

    public static void init(ForgeConfigSpec.Builder server, ForgeConfigSpec.Builder client) {
        server.push("server");
        numTribes = server
                .comment("Maximum Number of Tribes: ")
                .defineInRange("numberOfTribes", 10, 1, Integer.MAX_VALUE);
        tribeRequired = server
                .comment("Tribe Required: ")
                .define("tribesRequired", true);
        tierThresholds = server
                .comment("I:Tier Thresholds: ")
                .defineList("tier_thresholds", Arrays.asList(4, 12, 30, 100), i -> (int) i >= 0);
        tierNegEffects = server
                .comment("I:Number of negative effects by tribe tier: ")
                .defineList("tier_negative_effects", Arrays.asList(2, 2, 1, 1, 0), i -> (int) i >= 0);
        tierPosEffects = server
                .comment("I:Number of positive effects by tribe tier: ")
                .defineList("tier_positive_effects", Arrays.asList(1, 2, 2, 3, 3), i -> (int) i >= 0);
        friendlyFire = server
                .comment("Whether players should be able to harm other members of their tribe: ")
                .define("friendlyFire", false);
        tierForSelectHemisphere = server
                .comment("Minimum tribe tier to access a hemisphere: ")
                .defineInRange("tierForSelectHemisphere", 2, 0, 10);
        requireHemisphereAccess = server
                .comment("Whether player's tribe must select a hemisphere to access it: ")
                .define("requireHemisphereAccess", true);
        maxChunksClaimed = server
                .comment("I:Maximum number of chunks able to be claimed at each tribe rank: ")
                .defineList("max_claimed_chunks", Arrays.asList(1, 4, 10, 20, 30), i -> (int) i >= 0);
        hemisphereDirection = server
                .comment(
                        "The direction of the hemispheres: ",
                        "Allowed values: " + Arrays.stream(HemisphereDirection.values()).map(HemisphereDirection::name).map(s -> String.format("'%s'", s.toLowerCase(Locale.ROOT))).collect(Collectors.joining(", "))
                )
                .defineEnum("hemisphereDirection", HemisphereDirection.NORTH_SOUTH, EnumGetMethod.NAME_IGNORECASE);
        halfNoMansLandWidth = server
                .comment("The distance from zero to the edge of a hemisphere, half the width of no mans land : ")
                .defineInRange("halfNoMansLandWidth", 500, 0, Integer.MAX_VALUE);
        nonpvpDeathPunishTimes = server
                .comment("I:How long your chunk claims will be disabled by how many times people have died (out of PVP) in the interval: ")
                .defineList("nonpvpDeathPunishTimes", Arrays.asList(10, 60, 360), i -> (int) i >= 0);
        pvpDeathPunishTimes = server
                .comment("I:How long your chunk claims will be disabled by how many times people have died (by PVP) in the interval: ")
                .defineList("pvpDeathPunishTimes", Arrays.asList(30, 120, 1440), i -> (int) i >= 0);
        rankToChooseHemisphere = server
                .comment("A member must have equal or greater than this rank to select a hemisphere for thier tribe [member, officer, vice_leader, leader]: ")
                .defineEnum("rankToChooseHemisphere", Member.Rank.VICE_LEADER);
        daysBetweenDeityChange = server
                .comment("The number of days you must wait between changing your tribe's deity : ")
                .defineInRange("daysBetweenDeityChange", 30, 0, Integer.MAX_VALUE);
        daysBetweenEffectsChange = server
                .comment("The number of days you must wait between changing your tribe's effects : ")
                .defineInRange("daysBetweenEffectsChange", 10, 0, Integer.MAX_VALUE);
        ignoredEffects = server
                .comment("S: effects that cannot be chosen as a persistent tribe effect : ")
                .defineList("ignoredEffects", Arrays.asList("minecraft:bad_omen", "minecraft:conduit_power", "minecraft:health_boost", "minecraft:luck", "minecraft:unluck", "minecraft:hero_of_the_village", "minecraft:absorption"), i -> ((String) i).contains(":"));
        removeInactiveAfterDays = server
                .comment("Players who haven't logged on in this many days will automatically be removed from the tribe they're in. Setting this value to 0 will disable this feature: ")
                .defineInRange("removeInactiveAfterDays", 10, 0, Integer.MAX_VALUE);

        server.pop();

        client.push("client");

        landOwnerDisplayPosition = client
                .comment("position of the land owner ui. options: top_left, top_right, top_middle, bottom_left, bottom_right, bottom_middle, none ")
                .define("landOwnerDisplayPosition", "top_left");

        client.pop();
    }

    public static int getMaxNumberOfTribes() {
        return numTribes.get();
    }

    public static boolean isTribeRequired() {
        return tribeRequired.get();
    }

    public static List<Integer> getTierThresholds() {
        return (List<Integer>) tierThresholds.get();
    }

    public static List<Integer> getTierNegativeEffects() {
        return (List<Integer>) tierNegEffects.get();
    }

    public static List<Integer> getTierPositiveEffects() {
        return (List<Integer>) tierPosEffects.get();
    }

    public static boolean getFriendlyFireEnabled() {
        return friendlyFire.get();
    }

    public static int getMaxTribeNameLength() {
        return 24;
    }

    public static boolean getRequireHemisphereAccess() {
        return requireHemisphereAccess.get();
    }

    public static int getMinTierToSelectHemi() {
        return tierForSelectHemisphere.get();
    }

    public static int getMaxChunksForTier(int tier) {
        var list = maxChunksClaimed.get();
        if (tier > list.size()) {
            return list.get(list.size() - 1);
        } else {
            return list.get(tier - 1);
        }
    }

    public static HemisphereDirection getHemisphereDirection() {
        return hemisphereDirection.get();
    }

    public static int getHalfNoMansLandWidth() {
        return halfNoMansLandWidth.get();
    }

    public static Member.Rank rankToChooseHemisphere() {
        return rankToChooseHemisphere.get();
    }

    public static int getDeathClaimDisableTime(int index, boolean deathWasPVP) {
        var punishments = deathWasPVP ? pvpDeathPunishTimes.get() : nonpvpDeathPunishTimes.get();
        index = Math.min(index, punishments.size() - 1);
        return punishments.get(index);
    }

    public static List<MobEffect> getGoodEffects() {
        var ignored = ignoredEffects.get();

        return ForgeRegistries.MOB_EFFECTS.getEntries().stream().filter(it -> ignored.contains(it.getKey().location().toString())).map(Map.Entry::getValue)
                .filter(MobEffect::isBeneficial).filter(Predicate.not(MobEffect::isInstantenous)).toList();
    }

    public static List<MobEffect> getBadEffects() {
        var ignored = ignoredEffects.get();

        return ForgeRegistries.MOB_EFFECTS.getEntries().stream().filter(it -> ignored.contains(it.getKey().location().toString())).map(Map.Entry::getValue)
                .filter(Predicate.not(MobEffect::isBeneficial)).filter(Predicate.not(MobEffect::isInstantenous)).toList();
    }

    public static int daysBetweenDeityChanges() {
        return daysBetweenDeityChange.get();
    }

    public static int daysBetweenEffectsChanges() {
        return daysBetweenEffectsChange.get();
    }

    // CLIENT

    public static String getLandOwnerDisplayPosition() {
        return landOwnerDisplayPosition.get();
    }

}
