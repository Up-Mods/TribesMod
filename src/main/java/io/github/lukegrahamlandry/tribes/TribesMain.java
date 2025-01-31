package io.github.lukegrahamlandry.tribes;

import com.mojang.logging.LogUtils;
import io.github.lukegrahamlandry.tribes.commands.util.DeityArgumentType;
import io.github.lukegrahamlandry.tribes.commands.util.OfflinePlayerArgumentType;
import io.github.lukegrahamlandry.tribes.commands.util.TribeArgumentType;
import io.github.lukegrahamlandry.tribes.config.Config;
import io.github.lukegrahamlandry.tribes.init.*;
import net.minecraft.commands.synchronization.ArgumentTypes;
import net.minecraft.commands.synchronization.EmptyArgumentSerializer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(TribesMain.MOD_ID)
public class TribesMain {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final String MOD_ID = "tribes";

    public TribesMain() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // register configs
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.client_config, "tribes-client.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.server_config, "tribes-server.toml");

        // register
        ItemInit.ITEMS.register(eventBus);
        BlockInit.BLOCKS.register(eventBus);
        TileEntityInit.TILE_ENTITY_TYPES.register(eventBus);
        BannerInit.setup();

        ArgumentTypes.register("tribe", TribeArgumentType.class, new EmptyArgumentSerializer<>(TribeArgumentType::tribe));
        ArgumentTypes.register("deity", DeityArgumentType.class, new EmptyArgumentSerializer<>(DeityArgumentType::deity));
        ArgumentTypes.register("maybe_offline_player", OfflinePlayerArgumentType.class, new EmptyArgumentSerializer<>(OfflinePlayerArgumentType::offlinePlayerID));

        // event listeners
        eventBus.addListener(this::setup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        NetworkHandler.registerMessages();
    }
}
