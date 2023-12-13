package io.github.lukegrahamlandry.tribes.api.events;

import io.github.lukegrahamlandry.tribes.tribe_data.Tribe;

import java.util.UUID;

public class TribeMemberAddedEvent extends TribeEvent {

    private final UUID affectedPlayer;

    public TribeMemberAddedEvent(Tribe tribe, UUID affectedPlayer) {
        super(tribe);
        this.affectedPlayer = affectedPlayer;
    }

    public UUID getAffectedPlayer() {
        return affectedPlayer;
    }
}
