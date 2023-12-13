package io.github.lukegrahamlandry.tribes.api.tribe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lukegrahamlandry.tribes.tribe_data.Tribe;
import io.github.lukegrahamlandry.tribes.util.CodecHelper;

import java.time.Instant;

public class DeityInfo {
    private String name;
    private Instant lastChanged;

    public DeityInfo(String name, Instant lastChanged) {
        this.name = name;
        this.lastChanged = lastChanged;
    }

    public String getName() {
        return name;
    }

    public Instant getLastChanged() {
        return lastChanged;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLastChanged(Instant lastChanged) {
        this.lastChanged = lastChanged;
    }

    public static Codec<DeityInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(DeityInfo::getName),
            CodecHelper.ISO_INSTANT.fieldOf("lastChanged").forGetter(DeityInfo::getLastChanged)
    ).apply(instance, DeityInfo::new));
}
