package io.github.lukegrahamlandry.tribes.api.tribe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lukegrahamlandry.tribes.tribe_data.Tribe;

import java.util.ArrayList;
import java.util.List;

public class AutoBanInfo {
    private int deathsThreshold;
    private int daysThreshold;
    private final List<Member.Rank> ranks;

    public AutoBanInfo(int deathsThreshold, int daysThreshold, List<Member.Rank> ranks) {
        this.deathsThreshold = deathsThreshold;
        this.daysThreshold = daysThreshold;
        this.ranks = new ArrayList<>(ranks);
    }

    public int getDaysThreshold() {
        return daysThreshold;
    }

    public int getDeathsThreshold() {
        return deathsThreshold;
    }

    public List<Member.Rank> getRanks() {
        return ranks;
    }

    public void setDaysThreshold(int daysThreshold) {
        this.daysThreshold = daysThreshold;
    }

    public void setDeathsThreshold(int deathsThreshold) {
        this.deathsThreshold = deathsThreshold;
    }

    public static final Codec<AutoBanInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("deaths").forGetter(AutoBanInfo::getDeathsThreshold),
            Codec.INT.fieldOf("days").forGetter(AutoBanInfo::getDaysThreshold),
            Member.Rank.CODEC.listOf().fieldOf("ranks").forGetter(AutoBanInfo::getRanks)
    ).apply(instance, AutoBanInfo::new));
}
