package io.github.lukegrahamlandry.tribes.api.events;

import io.github.lukegrahamlandry.tribes.api.tribe.Member;
import io.github.lukegrahamlandry.tribes.tribe_data.Tribe;
import net.minecraftforge.eventbus.api.Cancelable;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Cancelable
public class TribeMemberPromotionEvent extends TribeEventWithSource {

    public enum Type {
        PROMOTION,
        DEMOTION
    }

    private final UUID affectedPlayer;
    private final Type type;
    private Member.Rank newRank;

    public TribeMemberPromotionEvent(Tribe tribe, @Nullable UUID source, UUID affectedPlayer, Type type) {
        super(tribe, source);
        this.affectedPlayer = affectedPlayer;
        this.type = type;
    }

    public UUID getAffectedPlayer() {
        return affectedPlayer;
    }

    public Member.Rank getNewRank() {
        return newRank;
    }

    public Type getType() {
        return type;
    }

    public void setNewRank(Member.Rank newRank) {
        this.newRank = newRank;
    }
}
