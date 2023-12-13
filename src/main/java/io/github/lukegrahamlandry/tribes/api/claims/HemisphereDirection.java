package io.github.lukegrahamlandry.tribes.api.claims;

import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;

import java.util.function.ToIntFunction;

public enum HemisphereDirection implements StringRepresentable, ToIntFunction<BlockPos> {
    NORTH_SOUTH("north_south", BlockPos::getZ),
    EAST_WEST("east_west", BlockPos::getX);

    private final String name;
    private final ToIntFunction<BlockPos> posGetter;

    HemisphereDirection(String name, ToIntFunction<BlockPos> posGetter) {
        this.name = name;
        this.posGetter = posGetter;
    }

    @Override
    public int applyAsInt(BlockPos value) {
        return posGetter.applyAsInt(value);
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public static HemisphereDirection fromString(String name) {
        for (HemisphereDirection value : values()) {
            if (value.getSerializedName().equals(name)) return value;
        }
        return null;
    }
}
