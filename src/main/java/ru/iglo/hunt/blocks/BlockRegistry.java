package ru.iglo.hunt.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import ru.iglo.hunt.keys.KeyType;

public class BlockRegistry {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, "hunt");

    // 1. Оригинальный блок Key (если нужен)
    public static final RegistryObject<Block> KEY_BLOCK = BLOCKS.register("key",
            () -> new KeyBlock(Block.Properties.of(Material.METAL)
                    .noOcclusion()
                    .strength(0.0f)
            )
    );

    // 2. Блок с ключом охраны
    public static final RegistryObject<Block> SECURITY_KEY_CABINET_BLOCK = BLOCKS.register("security_key_cabinet",
            () -> new KeyCabinetBlock(KeyType.SECURITY)
    );

    // 3. Блоки для других ключей
    public static final RegistryObject<Block> CABINET1_KEY_BLOCK = BLOCKS.register("cabinet1_key_block",
            () -> new KeyCabinetBlock(KeyType.CABINET_1)
    );

    public static final RegistryObject<Block> CABINET2_KEY_BLOCK = BLOCKS.register("cabinet2_key_block",
            () -> new KeyCabinetBlock(KeyType.CABINET_2)
    );

    public static final RegistryObject<Block> CABINET3_KEY_BLOCK = BLOCKS.register("cabinet3_key_block",
            () -> new KeyCabinetBlock(KeyType.CABINET_3)
    );

    // 4. Hospital Key Block (специальный блок с TileEntity для хранения NBT)
    public static final RegistryObject<Block> HOSPITAL_KEY_BLOCK = BLOCKS.register("hospital_key_block",
            () -> new HospitalKeyBlock(Block.Properties.of(Material.METAL)
                    .noOcclusion()
                    .strength(0.0f)
                    .lightLevel(state -> 5)
            )
    );

    // 5. Общий кабинет для ItemRegistry
    public static final RegistryObject<Block> KEY_CABINET_BLOCK = BLOCKS.register("key_cabinet",
            () -> new KeyCabinetBlock() // Дефолтный конструктор
    );
}