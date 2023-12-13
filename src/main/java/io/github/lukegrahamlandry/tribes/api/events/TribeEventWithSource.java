package io.github.lukegrahamlandry.tribes.api.events;

import io.github.lukegrahamlandry.tribes.tribe_data.Tribe;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class TribeEventWithSource extends TribeEvent {

    @Nullable
    private final UUID source;

    public TribeEventWithSource(Tribe tribe, @Nullable UUID source) {
        super(tribe);
        this.source = source;
    }

    @Nullable
    public UUID getSource() {
        return source;
    }
}
