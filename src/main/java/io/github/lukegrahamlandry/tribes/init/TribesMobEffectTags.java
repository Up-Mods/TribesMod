package io.github.lukegrahamlandry.tribes.init;

import io.github.lukegrahamlandry.tribes.TribesMain;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;

import java.util.List;
import java.util.function.Predicate;

public class TribesMobEffectTags {

    public static final TagKey<MobEffect> SINGLE_LEVEL_EFFECTS = TagKey.create(Registry.MOB_EFFECT_REGISTRY, new ResourceLocation(TribesMain.MOD_ID, "single_level_effects"));
    public static final TagKey<MobEffect> EXCLUDED_EFFECTS = TagKey.create(Registry.MOB_EFFECT_REGISTRY, new ResourceLocation(TribesMain.MOD_ID, "excluded_effects"));

    @SuppressWarnings("deprecation")
    public static List<MobEffect> getGoodEffects() {
        var excluded = Registry.MOB_EFFECT.getOrCreateTag(EXCLUDED_EFFECTS);
        return Registry.MOB_EFFECT.holders()
                .filter(Predicate.not(excluded::contains)).map(Holder.Reference::value)
                .filter(MobEffect::isBeneficial)
                .filter(Predicate.not(MobEffect::isInstantenous))
                .toList();
    }

    @SuppressWarnings("deprecation")
    public static List<MobEffect> getBadEffects() {
        var excluded = Registry.MOB_EFFECT.getOrCreateTag(EXCLUDED_EFFECTS);
        return Registry.MOB_EFFECT.holders()
                .filter(Predicate.not(excluded::contains)).map(Holder.Reference::value)
                .filter(Predicate.not(MobEffect::isBeneficial))
                .filter(Predicate.not(MobEffect::isInstantenous))
                .toList();
    }
}
