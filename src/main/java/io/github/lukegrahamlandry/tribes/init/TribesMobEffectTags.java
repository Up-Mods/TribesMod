package io.github.lukegrahamlandry.tribes.init;

import io.github.lukegrahamlandry.tribes.TribesMain;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;

public class TribesMobEffectTags {

    public static final TagKey<MobEffect> SINGLE_LEVEL_EFFECTS = TagKey.create(Registry.MOB_EFFECT_REGISTRY, new ResourceLocation(TribesMain.MOD_ID, "single_level_effects"));
}
