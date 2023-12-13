package io.github.lukegrahamlandry.tribes.api.claims;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum Hemisphere implements StringRepresentable {
    POSITIVE("positive"), // south or east
    NEGATIVE("negative"),  // north or west
    NONE("none");

    private final String name;

    Hemisphere(String name) {
        this.name = name;
    }

    public static final Codec<Hemisphere> CODEC = StringRepresentable.fromEnum(Hemisphere::values, Hemisphere::fromString);

    private static Hemisphere fromString(String name) {
        for (Hemisphere hemisphere : Hemisphere.values()) {
            if (hemisphere.getSerializedName().equals(name)) return hemisphere;
        }
        return null;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
