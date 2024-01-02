package io.github.lukegrahamlandry.tribes.init;

import io.github.lukegrahamlandry.tribes.util.TribesBannerSymbol;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BannerPattern;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class BannerInit {
    private static final Map<ResourceLocation, BannerPattern> patterns = new HashMap<>();

    public static void setup() {
        for (TribesBannerSymbol symbol : TribesBannerSymbol.values()) {
            var pattern = BannerPattern.create(symbol.getBannerEnumName(), symbol.getFileLocationFragment(), symbol.getBannerHashName(), false);
            patterns.put(symbol.getId(), pattern);
        }
    }

    @Nullable
    public static BannerPattern get(ResourceLocation key) {
        return patterns.get(key);
    }
}
