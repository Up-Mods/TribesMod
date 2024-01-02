package io.github.lukegrahamlandry.tribes.tribe_data;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;

public enum TribeError {
    NAME_TAKEN("name_taken"),
    IN_OTHER_TRIBE("in_other_tribe"),
    IN_TRIBE("in_tribe"),
    NAME_TOO_LONG("name_too_long"),
    INVALID_TRIBE("invalid_tribe"),
    RANK_TOO_LOW("rank_too_low"),
    YOU_NOT_IN_TRIBE("you_not_in_tribe"),
    BANNED("banned"),
    RANK_NOT_FOUND("rank_not_found"),
    CANNOT_PROMOTE_RANK("cannot_promote_rank"),
    CANNOT_DEMOTE_RANK("cannot_demote_rank"),
    THEY_NOT_IN_TRIBE("they_not_in_tribe"),
    SAME_TRIBE("same_tribe"),
    CONFIG("config"),
    ALREADY_CLAIMED("already_claimed"),
    NOT_OWNED("not_owned"),
    MAX_CLAIMS_REACHED("max_claims_reached"),
    ALREADY_HAVE_HEMISPHERE("already_have_hemisphere"),
    INVALID_HEMISPHERE("invalid_hemisphere"),
    WEAK_TRIBE("weak_tribe"),
    NO_CONFIRM("no_confirm"),
    INVALID_DEITY("invalid_deity"),
    WAIT_HOURS("wait_hours"),
    NO_DEITY("no_deity"),
    HOLD_BOOK("hold_book"),
    HOLD_BANNER("hold_banner"),
    ARG_TRIBE("arg_tribe"),
    ARG_PLAYER("arg_player"),
    ARG_DEITY("arg_deity"),
    ARG_MISSING("arg_missing"),
    INVALID_RANK("invalid_rank"),
    NOT_PRIVATE("not_private"),
    IS_PRIVATE("is_private"),
    EVENT_CANCELLED("event_cancelled"),
    PLAYER_NOT_FOUND("player_not_found");

    private final String translationKey;

    TribeError(String translationKey) {
        this.translationKey = "error.tribes." + translationKey;
    }

    public MutableComponent getText() {
        return new TranslatableComponent(this.translationKey).withStyle(ChatFormatting.DARK_RED);
    }

    public MutableComponent getTextWithArgs(Object... args) {
        return new TranslatableComponent(this.translationKey, args).withStyle(ChatFormatting.DARK_RED);
    }

    @Override
    public String toString() {
        return "use getText() instead of toString()";
    }
}
