package io.github.lukegrahamlandry.tribes.tribe_data;

import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.Style;

public enum TribeError {
    SUCCESS,
    NAME_TAKEN,
    IN_OTHER_TRIBE,
    IN_TRIBE,
    NAME_TOO_LONG,
    CLIENT,
    INVALID_TRIBE,
    RANK_TOO_LOW,
    YOU_NOT_IN_TRIBE,
    BANNED,
    RANK_DOESNT_EXIST,
    CANNOT_PROMOTE_RANK,
    THEY_NOT_IN_TRIBE,
    SAME_TRIBE,
    CONFIG,
    ALREADY_CLAIMED,
    NOT_OWNED,
    ALREADY_HAVE_HEMISPHERE,
    INVALID_HEMISPHERE,
    WEAK_TRIBE,
    NO_CONFIRM,
    INVALID_DEITY,
    WAIT,
    NO_DEITY,
    HOLD_BOOK,
    HOLD_BANNER,
    ARG_TRIBE,
    ARG_PLAYER,
    ARG_DEITY,
    ARG_MISSING,
    INVALID_RANK,
    NOT_PRIVATE,
    IS_PRIVATE,
    EVENT_CANCELLED, CANNOT_DEMOTE_RANK, MAX_CLAIMS_REACHED;

    public TranslatableComponent getText(){
        String langEntry = "error.tribes." + this.name().toLowerCase();
        TranslatableComponent text = new TranslatableComponent(langEntry);
        Style style = text.getStyle().withColor(TextColor.fromRgb(0xFF0000));
        text.setStyle(style);
        return text;
    }

    public static TranslatableComponent getWaitText(long time){
        String langEntry = "error.tribes.wait";
        TranslatableComponent text = new TranslatableComponent(langEntry, time);
        Style style = text.getStyle().withColor(TextColor.fromRgb(0xFF0000));
        text.setStyle(style);
        return text;
    }

    @Override
    public String toString() {
        return "use TranslationTextComponent instead of TribeActionResult#toString";
    }
}
