package io.github.lukegrahamlandry.tribes.commands.util;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.lukegrahamlandry.tribes.tribe_data.DeitiesManager;
import io.github.lukegrahamlandry.tribes.tribe_data.TribeError;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

// FIXME big problem:
// since this uses a greedy string you can only have it as the last argument
// further thinking required on how to fix this

public class DeityArgumentType implements ArgumentType<DeitiesManager.DeityData> {

    private static final SimpleCommandExceptionType INVALID_DEITY = new SimpleCommandExceptionType(TribeError.INVALID_DEITY.getText());

    public static DeityArgumentType deity() {
        return new DeityArgumentType();
    }

    public DeitiesManager.DeityData parse(StringReader reader) {
        String argument = reader.getRemaining();
        reader.setCursor(reader.getTotalLength());

        for (String key : DeitiesManager.deities.keySet()) {
            String display = DeitiesManager.deities.get(key).displayName;
            if (argument.equals(key) || argument.equals(display)) return DeitiesManager.deities.get(key);
        }

        return null;
    }

    public static <S> DeitiesManager.DeityData getDeity(CommandContext<S> context, String name) throws CommandSyntaxException {
        try {
            return Objects.requireNonNull(context.getArgument(name, DeitiesManager.DeityData.class));
        } catch (Exception e) {
            throw INVALID_DEITY.create();
        }
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        StringReader stringreader = new StringReader(builder.getInput());
        stringreader.setCursor(builder.getStart());
        String s = stringreader.getRemaining();
        stringreader.setCursor(stringreader.getTotalLength());

        stringreader.skipWhitespace();

        for (String key : DeitiesManager.deities.keySet()) {
            String display = DeitiesManager.deities.get(key).displayName;
            if (key.startsWith(s) || display.startsWith(s)) builder.suggest(display);
        }

        return builder.buildFuture();
    }

    public Collection<String> getExamples() {
        return new ArrayList<>();
    }
}
