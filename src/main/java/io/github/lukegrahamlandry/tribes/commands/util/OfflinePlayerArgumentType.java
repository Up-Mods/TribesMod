package io.github.lukegrahamlandry.tribes.commands.util;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.util.UUIDTypeAdapter;
import io.github.lukegrahamlandry.tribes.TribesMain;
import io.github.lukegrahamlandry.tribes.tribe_data.TribeError;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(modid = TribesMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class OfflinePlayerArgumentType implements ArgumentType<OfflinePlayerArgumentType.Data> {

    private static final SimpleCommandExceptionType INVALID_PLAYER = new SimpleCommandExceptionType(TribeError.PLAYER_NOT_FOUND.getText());

    @Nullable
    private static volatile MinecraftServer server;

    private OfflinePlayerArgumentType() {
    }

    @Override
    public OfflinePlayerArgumentType.Data parse(StringReader reader) throws CommandSyntaxException {
        String nameOrId = reader.readUnquotedString();
        try {
            return new Data(UUIDTypeAdapter.fromString(nameOrId));
        } catch (IllegalArgumentException e) {
            return Optional.ofNullable(server)
                    .flatMap(server -> server.getProfileCache().get(nameOrId))
                    .map(GameProfile::getId)
                    .map(Data::new)
                    .orElseThrow(INVALID_PLAYER::create);
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        StringReader stringreader = new StringReader(builder.getInput());
        stringreader.setCursor(builder.getStart());
        String s = stringreader.getRemaining();
        stringreader.setCursor(stringreader.getTotalLength());

        stringreader.skipWhitespace();

        if (server != null) {
            server.getPlayerList().getPlayers().forEach(player -> {
                var profile = player.getGameProfile();
                if (profile.getName().startsWith(s)) {
                    builder.suggest(profile.getName());
                }
                if (profile.getId().toString().startsWith(s)) {
                    builder.suggest(profile.getId().toString());
                }
            });

            //TODO maybe read offline UUIDs
        }

        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return List.of("Alex", "Steve", "ff0f9796-824a-4f34-8ca0-41d85de71a01");
    }

    public static <S> UUID getOfflinePlayer(CommandContext<S> context, String name) throws CommandSyntaxException {
        return context.getArgument(name, Data.class).id();
    }

    public static String getPlayerName(UUID uuid) {
        return Optional.ofNullable(server).flatMap(s -> s.getProfileCache().get(uuid)).map(GameProfile::getName).orElseGet(uuid::toString);
    }

    public static OfflinePlayerArgumentType offlinePlayerID() {
        return new OfflinePlayerArgumentType();
    }

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        server = event.getServer();
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppedEvent event) {
        server = null;
    }

    public record Data(UUID id) {
    }
}
