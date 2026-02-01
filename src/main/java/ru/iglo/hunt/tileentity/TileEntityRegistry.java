package ru.iglo.hunt.tileentity;

import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import ru.iglo.hunt.Hunt;
import ru.iglo.hunt.blocks.BlockRegistry;

public class TileEntityRegistry {
    public static final DeferredRegister<TileEntityType<?>> TILE_ENTITIES =
            DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, Hunt.MODID);

    // Для обычного KeyBlock (если он у вас есть)
    public static final RegistryObject<TileEntityType<KeyTileEntity>> KEY_TE =
            TILE_ENTITIES.register("key_te",
                    () -> TileEntityType.Builder.of(
                            KeyTileEntity::new,
                            BlockRegistry.KEY_BLOCK.get() // Убедитесь что KEY_BLOCK существует
                    ).build(null)
            );

    public static final RegistryObject<TileEntityType<KeyCabinetTileEntity>> KEY_CABINET_TE =
            TILE_ENTITIES.register("key_cabinet_te",
                    () -> TileEntityType.Builder.of(
                            KeyCabinetTileEntity::new,
                            BlockRegistry.KEY_CABINET_BLOCK.get()
                    ).build(null)
            );

    // Для HospitalKeyBlock
    public static final RegistryObject<TileEntityType<HospitalKeyTileEntity>> HOSPITAL_KEY_TE =
            TILE_ENTITIES.register("hospital_key_te",
                    () -> TileEntityType.Builder.of(
                            HospitalKeyTileEntity::new,
                            BlockRegistry.HOSPITAL_KEY_BLOCK.get()
                    ).build(null)
            );

    // Для TablichkaBlock (табличка с двумя моделями)
    public static final RegistryObject<TileEntityType<TablichkaTileEntity>> TABLICHKA_TILE_ENTITY =
            TILE_ENTITIES.register("tablichka_te",
                    () -> TileEntityType.Builder.of(
                            TablichkaTileEntity::new,
                            BlockRegistry.TABLICHKA_BLOCK.get()
                    ).build(null)
            );
}