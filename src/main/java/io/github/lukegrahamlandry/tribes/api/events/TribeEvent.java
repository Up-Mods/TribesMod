package io.github.lukegrahamlandry.tribes.api.events;

import io.github.lukegrahamlandry.tribes.tribe_data.Tribe;
import net.minecraftforge.eventbus.api.Event;

public class TribeEvent extends Event {

    private final Tribe tribe;

    public TribeEvent(Tribe tribe) {
        this.tribe = tribe;
    }

    public Tribe getTribe() {
        return tribe;
    }
}
