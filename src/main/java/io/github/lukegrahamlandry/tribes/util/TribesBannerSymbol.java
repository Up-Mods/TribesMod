package io.github.lukegrahamlandry.tribes.util;

import io.github.lukegrahamlandry.tribes.TribesMain;
import net.minecraft.resources.ResourceLocation;

import java.util.Locale;

public enum TribesBannerSymbol {
    AQUA("aqua"),
    BLAST("blast"),
    BOLT("bolt"),
    BOOK("book"),
    CHIP("chip"),
    COPY("copy"),
    CROSS("cross"),
    CROSSED_SWORDS("crossed_swords"),
    CUBE("cube"),
    DOCTOR("doctor"),
    DOLLAR("dollar"),
    EFBA("efba"),
    ESELIA("eselia"),
    FAENEN("faenen"),
    HEART("heart"),
    IKEA("ikea"),
    INVALID("invalid"),
    IZANAGI("izanagi"),
    JULIUS("julius"),
    MISSING("missing"),
    MOON("moon"),
    OMEGA("omega"),
    PENDEEN("pendeen"),
    PICKAXE("pickaxe"),
    POWER("power"),
    SCALES("scales"),
    SHIELD("shield"),
    SKRIBIT("skribit"),
    STARS("stars"),
    SUN("sun"),
    SWORD("sword"),
    TREE("tree"),
    TRIFORCE("triforce"),
    WHEAT("wheat"),
    WHEEL_CROSS("wheel_cross"),

    //unnamed
    N_11("n11"),
    N_12("n12"),
    N_13("n13"),
    N_15("n15"),
    N_21("n21"),
    N_24("n24"),
    N_36("n36"),
    N_36B("n36b"),
    N_37("n37");

    private final ResourceLocation id;

    TribesBannerSymbol(String name) {
        this.id = new ResourceLocation(TribesMain.MOD_ID, name);
    }

    public ResourceLocation getId() {
        return this.id;
    }

    public String getBannerEnumName() {
        return this.id.getNamespace() + "$" + this.id.getPath().toUpperCase(Locale.ROOT);
    }

    public String getBannerHashName() {
        return this.id.toString();
    }

    public String getFileLocationFragment() {
        return "tribes/" + this.id.getPath();
    }
}
