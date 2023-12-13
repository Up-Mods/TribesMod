package io.github.lukegrahamlandry.tribes.init;

import io.github.lukegrahamlandry.tribes.TribesMain;
import io.github.lukegrahamlandry.tribes.tile.AltarBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class TileEntityInit {
    public static final DeferredRegister<BlockEntityType<?>> TILE_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, TribesMain.MOD_ID);

    public static final RegistryObject<BlockEntityType<AltarBlockEntity>> ALTAR
            = TILE_ENTITY_TYPES.register("altar", () -> BlockEntityType.Builder.of(AltarBlockEntity::new, BlockInit.ALTER.get()).build(null));
}
