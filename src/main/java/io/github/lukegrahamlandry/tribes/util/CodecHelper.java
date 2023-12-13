package io.github.lukegrahamlandry.tribes.util;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.ChunkPos;

import java.time.Instant;
import java.util.UUID;

public class CodecHelper {

    public static final Codec<Instant> INSTANT = Codec.LONG.xmap(Instant::ofEpochMilli, Instant::toEpochMilli);
    public static final Codec<Instant> ISO_INSTANT = Codec.STRING.xmap(Instant::parse, Instant::toString);

    public static final Codec<UUID> UUID_STRING = Codec.STRING.xmap(UUID::fromString, UUID::toString);
    public static final Codec<ChunkPos> CHUNK_POS = Codec.LONG.xmap(ChunkPos::new, ChunkPos::toLong);
}
