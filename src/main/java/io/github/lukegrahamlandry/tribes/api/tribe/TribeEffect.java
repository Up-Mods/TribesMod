package io.github.lukegrahamlandry.tribes.api.tribe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lukegrahamlandry.tribes.tribe_data.Tribe;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.ForgeRegistries;

public record TribeEffect(MobEffect effect, int level) {

    public static final Codec<TribeEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ForgeRegistries.MOB_EFFECTS.getCodec().fieldOf("effect").forGetter(TribeEffect::effect),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("level").forGetter(TribeEffect::level)
    ).apply(instance, TribeEffect::new));
}
