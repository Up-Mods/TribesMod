package io.github.lukegrahamlandry.tribes.commands.util;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.lukegrahamlandry.tribes.TribesMain;
import io.github.lukegrahamlandry.tribes.tribe_data.Tribe;
import io.github.lukegrahamlandry.tribes.tribe_data.TribeError;
import io.github.lukegrahamlandry.tribes.tribe_data.TribesManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

// big problem:
// since this uses a greedy string you can only have it as the last argument
// further thinking required on how to fix this

public class TribeArgumentType implements ArgumentType<Tribe> {

    private static final SimpleCommandExceptionType INVALID_TRIBE = new SimpleCommandExceptionType(TribeError.INVALID_TRIBE.getText());

    public static TribeArgumentType tribe() {
        return new TribeArgumentType();
    }

    public Tribe parse(StringReader reader) {
        String tribeName = reader.getRemaining();
        reader.setCursor(reader.getTotalLength());
        return TribesManager.findTribe(tribeName);
    }

    public static <S> Tribe getTribe(CommandContext<S> context, String name) throws CommandSyntaxException {
        try {
            return Objects.requireNonNull(context.getArgument(name, Tribe.class));
        } catch (Exception e) {
            throw INVALID_TRIBE.create();
        }
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        StringReader stringreader = new StringReader(builder.getInput());
        stringreader.setCursor(builder.getStart());
        String s = stringreader.getRemaining();
        stringreader.setCursor(stringreader.getTotalLength());

        stringreader.skipWhitespace();

        TribesMain.LOGGER.debug(s);

        for (Tribe tribe : TribesManager.getTribes()) {
            if (tribe.getName().startsWith(s)) builder.suggest(tribe.getName());
        }

        return builder.buildFuture();
    }

    public Collection<String> getExamples() {
        return new ArrayList<>();
    }
}
