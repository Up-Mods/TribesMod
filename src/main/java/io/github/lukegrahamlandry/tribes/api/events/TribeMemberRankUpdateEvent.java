package io.github.lukegrahamlandry.tribes.api.events;

import io.github.lukegrahamlandry.tribes.api.tribe.Member;
import io.github.lukegrahamlandry.tribes.tribe_data.Tribe;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class TribeMemberRankUpdateEvent extends TribeEventWithSource {

    private final UUID playerId;
    private final Member.Rank oldRank;
    private final Member.Rank newRank;

    public TribeMemberRankUpdateEvent(Tribe tribe, @Nullable UUID source, UUID playerId, Member.Rank oldRank, Member.Rank newRank) {
        super(tribe, source);
        this.playerId = playerId;
        this.oldRank = oldRank;
        this.newRank = newRank;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public Member.Rank getOldRank() {
        return oldRank;
    }

    public Member.Rank getNewRank() {
        return newRank;
    }
}
