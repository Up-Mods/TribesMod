package io.github.lukegrahamlandry.tribes.api.events;

import io.github.lukegrahamlandry.tribes.tribe_data.Tribe;
import net.minecraftforge.eventbus.api.Cancelable;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Cancelable
public class TribeMemberBanEvent extends TribeEventWithSource {

    private final UUID affectedPlayer;

    public TribeMemberBanEvent(Tribe tribe, @Nullable UUID source, UUID affectedPlayer) {
        super(tribe, source);
        this.affectedPlayer = affectedPlayer;
    }

    public UUID getAffectedPlayer() {
        return affectedPlayer;
    }
}
