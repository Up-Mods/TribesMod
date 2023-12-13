package io.github.lukegrahamlandry.tribes.api.events;

import io.github.lukegrahamlandry.tribes.tribe_data.Tribe;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class TribeMemberRemovedEvent extends TribeEventWithSource {

    private final UUID affectedPlayer;

    public TribeMemberRemovedEvent(Tribe tribe, @Nullable UUID source, UUID affectedPlayer) {
        super(tribe, source);
        this.affectedPlayer = affectedPlayer;
    }

    public UUID getAffectedPlayer() {
        return affectedPlayer;
    }
}
