package io.github.lukegrahamlandry.tribes.api.tribe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lukegrahamlandry.tribes.util.CodecHelper;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EffectsInfo {
    @Nullable
    private Instant lastChanged;
    private List<TribeEffect> effects;

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public EffectsInfo(Optional<Instant> lastChanged, List<TribeEffect> effects) {
        this(lastChanged.orElse(null), effects);
    }

    public EffectsInfo(@Nullable Instant lastChanged, List<TribeEffect> effects) {
        this.lastChanged = lastChanged;
        this.effects = new ArrayList<>(effects);
    }

    public Optional<Instant> getLastChanged() {
        return Optional.ofNullable(lastChanged);
    }

    public void setLastChanged(@Nullable Instant lastChanged) {
        this.lastChanged = lastChanged;
    }

    public List<TribeEffect> getEffects() {
        return effects;
    }

    public void setEffects(List<TribeEffect> effects) {
        this.effects = effects;
        setLastChanged(Instant.now());
    }

    public static final Codec<EffectsInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            CodecHelper.INSTANT.optionalFieldOf("lastChanged").forGetter(EffectsInfo::getLastChanged),
            TribeEffect.CODEC.listOf().fieldOf("effects").forGetter(EffectsInfo::getEffects)
    ).apply(instance, EffectsInfo::new));

    public void clear() {
        this.effects.clear();
        setLastChanged(Instant.now());
    }
}
