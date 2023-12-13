package io.github.lukegrahamlandry.tribes.api.tribe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lukegrahamlandry.tribes.util.CodecHelper;
import net.minecraft.Util;
import net.minecraft.util.StringRepresentable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record Relation(UUID tribe, Type type) {

    public static final Codec<Relation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            CodecHelper.UUID_STRING.fieldOf("tribe").forGetter(Relation::tribe),
            Type.CODEC.fieldOf("relation").forGetter(Relation::type)
    ).apply(instance, Relation::new));

    public static final Codec<Map<UUID, Relation>> MAP_CODEC = CODEC.listOf().xmap(
            list -> Util.make(new HashMap<>(), map -> list.forEach(it -> map.put(it.tribe(), it))),
            map -> List.copyOf(map.values())
    );

    public enum Type implements StringRepresentable {
        ALLY("ally"),
        NEUTRAL("neutral"),
        ENEMY("enemy");

        private final String name;

        Type(String name) {
            this.name = name;
        }

        public static final Codec<Type> CODEC = StringRepresentable.fromEnum(Type::values, Type::fromString);

        private static Type fromString(String s) {
            for (Type value : values()) {
                if (value.getSerializedName().equals(s)) return value;
            }
            return null;
        }

        public String asString() {
            return name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
