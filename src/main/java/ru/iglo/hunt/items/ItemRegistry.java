package ru.iglo.hunt.items;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Rarity;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import ru.iglo.hunt.blocks.BlockRegistry;

public class ItemRegistry {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, "hunt");

    // Ключ-предмет с NBT
    public static final RegistryObject<Item> KEY_ITEM = ITEMS.register("key_item",
            () -> new KeyItem(new Item.Properties()
                    .tab(ItemGroup.TAB_MISC)
                    .stacksTo(1)
            )
    );


    public static final RegistryObject<Item> HOSPITAL_KEY_ITEM = ITEMS.register("hospital_key",
            () -> new HospitalKeyItem(new Item.Properties()
                    .tab(ItemGroup.TAB_MISC)
                    .stacksTo(1)
                    .rarity(Rarity.UNCOMMON)
            )
    );

    public static final RegistryObject<Item> KEY_CABINET_ITEM = ITEMS.register("key_cabinet",
            () -> new BlockItem(BlockRegistry.KEY_CABINET_BLOCK.get(), new Item.Properties()
                    .tab(ItemGroup.TAB_DECORATIONS)
            )
    );
    // Блок ключа (если нужен)
    public static final RegistryObject<Item> KEY_BLOCK_ITEM = ITEMS.register("key_block",
            () -> new BlockItem(BlockRegistry.KEY_BLOCK.get(), new Item.Properties()
                    .tab(ItemGroup.TAB_DECORATIONS)
            )
    );

    // Шкафчики с ключами
    public static final RegistryObject<Item> CABINET1_KEY_BLOCK_ITEM = ITEMS.register("cabinet1_key_block",
            () -> new BlockItem(BlockRegistry.CABINET1_KEY_BLOCK.get(), new Item.Properties()
                    .tab(ItemGroup.TAB_DECORATIONS)
            )
    );

    public static final RegistryObject<Item> CABINET2_KEY_BLOCK_ITEM = ITEMS.register("cabinet2_key_block",
            () -> new BlockItem(BlockRegistry.CABINET2_KEY_BLOCK.get(), new Item.Properties()
                    .tab(ItemGroup.TAB_DECORATIONS)
            )
    );

    public static final RegistryObject<Item> CABINET3_KEY_BLOCK_ITEM = ITEMS.register("cabinet3_key_block",
            () -> new BlockItem(BlockRegistry.CABINET3_KEY_BLOCK.get(), new Item.Properties()
                    .tab(ItemGroup.TAB_DECORATIONS)
            )
    );

    // Блок больничного ключа
    public static final RegistryObject<Item> HOSPITAL_KEY_BLOCK_ITEM = ITEMS.register("hospital_key_block",
            () -> new BlockItem(BlockRegistry.HOSPITAL_KEY_BLOCK.get(), new Item.Properties()
                    .tab(ItemGroup.TAB_DECORATIONS)
            )
    );

    // Шкаф охраны
    public static final RegistryObject<Item> SECURITY_KEY_CABINET_ITEM = ITEMS.register("security_key_cabinet",
            () -> new BlockItem(BlockRegistry.SECURITY_KEY_CABINET_BLOCK.get(), new Item.Properties()
                    .tab(ItemGroup.TAB_DECORATIONS)
            )
    );

    // Табличка с двумя моделями
    public static final RegistryObject<Item> TABLICHKA_ITEM = ITEMS.register("tablichka",
            () -> new BlockItem(BlockRegistry.TABLICHKA_BLOCK.get(), new Item.Properties()
                    .tab(ItemGroup.TAB_DECORATIONS)
            )
    );

    // Скамейка из железа
    public static final RegistryObject<Item> BENCH_IRON_ITEM = ITEMS.register("bench_iron",
            () -> new BlockItem(BlockRegistry.BENCH_IRON_BLOCK.get(), new Item.Properties()
                    .tab(ItemGroup.TAB_DECORATIONS)
            )
    );
}