package io.github.lukegrahamlandry.tribes.tribe_data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import io.github.lukegrahamlandry.tribes.TribesMain;
import io.github.lukegrahamlandry.tribes.events.RemoveInactives;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;

@Mod.EventBusSubscriber(modid = TribesMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SaveHandler {

    private static final Logger logger = LogUtils.getLogger();
    private static final LevelResource TRIBES_DATA = new LevelResource("tribes");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static Path tribeDataLocation;  // folder that the tribe data was most recently loaded from

    @SubscribeEvent
    public static void doLoad(ServerStartingEvent event) {
        tribeDataLocation = event.getServer().getWorldPath(TRIBES_DATA);
        try {
            load(event.getServer());
        } catch (IOException e) {
            throw new RuntimeException("unable to load tribe data", e);
        }
    }

    @SubscribeEvent
    public static void doSave(ServerStoppingEvent event) {
        try {
            save(event.getServer());
        } catch (IOException e) {
            throw new RuntimeException("unable to save tribe data", e);
        }
    }

    public static void save(MinecraftServer server) throws IOException {
        Path worldDir = server.getWorldPath(TRIBES_DATA);
        Files.createDirectories(worldDir);
        Path dataFile = worldDir.resolve("tribes.json");
        try (var writer = Files.newBufferedWriter(dataFile)) {
            GSON.toJson(TribesManager.toJson(), writer);
        }

        Path loginTimesFile = worldDir.resolve("tribes-player-activity.json");
        try (var writer = Files.newBufferedWriter(loginTimesFile)) {
            GSON.toJson(RemoveInactives.toJson(), writer);
        }
    }

    public static void load(MinecraftServer server) throws IOException {
        Path worldDir = server.getWorldPath(TRIBES_DATA);
        Files.createDirectories(worldDir);
        // read tribes
        Path tribeDataFile = worldDir.resolve("tribes.json");
        if (!Files.exists(tribeDataFile)) {
            Files.writeString(tribeDataFile, "[]");
        }

        try (var reader = Files.newBufferedReader(tribeDataFile)) {
            TribesManager.fromJson(GsonHelper.parseArray(reader));
        }
        LandClaimHelper.setup();

        // read deities
        Path deitiesBooksLocation = worldDir.resolve("deities");
        Path deityDataFile = deitiesBooksLocation.resolve("deities.json");
        if (!Files.exists(deityDataFile)) {
            createDefaultDeityFiles(deitiesBooksLocation);
        }

        try (var reader = Files.newBufferedReader(deityDataFile)) {
            DeitiesManager.fromJson(GsonHelper.parseArray(reader));
        }

        // read deity books
        for (Map.Entry<String, DeitiesManager.DeityData> entry : DeitiesManager.deities.entrySet()) {
            String key = entry.getKey();
            DeitiesManager.DeityData deityData = entry.getValue();
            Path bookLocation = deitiesBooksLocation.resolve(key + ".txt");
            if (Files.exists(bookLocation)) {
                String rawBookContent = Files.readString(bookLocation);
                deityData.generateBook(rawBookContent);
            }
        }

        Path loginTimesFile = worldDir.resolve("tribes-player-activity.json");
        if (Files.exists(loginTimesFile)) {
            try (var reader = Files.newBufferedReader(loginTimesFile)) {
                RemoveInactives.load(GsonHelper.parse(reader));
            }
        }
    }

    private static void createDefaultDeityFiles(Path deityLocation) throws IOException {
        var srcPath = ModList.get().getModFileById(TribesMain.MOD_ID).getFile().findResource("/deities");
        Files.walkFileTree(srcPath, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                var relative = srcPath.relativize(file);
                var filePath = deityLocation.resolve(relative.toString());

                if(Files.exists(filePath)) {
                    logger.info("Skipping existing file: " + filePath);
                    return FileVisitResult.CONTINUE;
                }

                var parent = filePath.getParent();
                if (parent != null) {
                    Files.createDirectories(parent);
                }

                Files.copy(file, filePath, StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
