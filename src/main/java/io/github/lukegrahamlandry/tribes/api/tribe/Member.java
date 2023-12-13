package io.github.lukegrahamlandry.tribes.api.tribe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lukegrahamlandry.tribes.tribe_data.Tribe;
import io.github.lukegrahamlandry.tribes.util.CodecHelper;
import net.minecraft.Util;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Member {

    private final UUID id;
    private Rank rank;

    public Member(UUID id, Rank rank) {
        this.id = id;
        this.rank = rank;
    }

    public static Codec<Member> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            CodecHelper.UUID_STRING.fieldOf("id").forGetter(Member::id),
            Rank.CODEC.fieldOf("rank").forGetter(Member::rank)
    ).apply(instance, Member::new));

    public static Codec<Map<UUID, Member>> MAP_CODEC = CODEC.listOf().xmap(
            list -> Util.make(new HashMap<>(), map -> list.forEach(it -> map.put(it.id(), it))),
            map -> List.copyOf(map.values())
    );

    public static Member of(UUID player) {
        return new Member(player, Rank.MEMBER);
    }

    public UUID id() {
        return id;
    }

    public Rank rank() {
        return rank;
    }

    /**
     * use {@link Tribe#setRank(UUID, Rank, UUID)} instead!
     */
    @ApiStatus.Internal
    public void setRank(Rank rank) {
        this.rank = rank;
    }

    public enum Rank implements StringRepresentable, Comparable<Rank> {
        BYPASS("__bypass__"), // special rank for fake players
        LEADER("leader"),
        VICE_LEADER("vice leader"),
        OFFICER("officer"),
        MEMBER("member"),
        NONE("none");

        private final String name;

        Rank(String name) {
            this.name = name;
        }

        public Rank promotesTo() {
            return switch (this) {
                case BYPASS, LEADER, NONE -> null;
                case VICE_LEADER -> LEADER;
                case OFFICER -> VICE_LEADER;
                case MEMBER -> OFFICER;
            };
        }

        public Rank demotesTo() {
            return switch (this) {
                case BYPASS, LEADER, MEMBER, NONE -> null;
                case VICE_LEADER -> OFFICER;
                case OFFICER -> MEMBER;
            };
        }

        public static final Codec<Rank> CODEC = StringRepresentable.fromEnum(Rank::values, Rank::fromString);

        public static Rank fromString(String s) {
            for (Rank value : values()) {
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

        public boolean isViceLeaderOrHigher() {
            return this.ordinal() <= VICE_LEADER.ordinal();
        }

        public boolean isOfficerOrHigher() {
            return this.ordinal() <= OFFICER.ordinal();
        }

        public boolean isLeader() {
            return this.ordinal() <= LEADER.ordinal();
        }
    }
}
