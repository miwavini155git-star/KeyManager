package ru.iglo.hunt;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.iglo.hunt.blocks.BlockRegistry;
import ru.iglo.hunt.items.ItemRegistry;
import ru.iglo.hunt.tileentity.TileEntityRegistry;

@Mod(Hunt.MODID)
public class Hunt {
    public static final String MODID = "hunt";
    public static final Logger LOGGER = LogManager.getLogger();

    public Hunt() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Регистрируем все компоненты
        BlockRegistry.BLOCKS.register(modEventBus);
        ItemRegistry.ITEMS.register(modEventBus);
        TileEntityRegistry.TILE_ENTITIES.register(modEventBus); // Добавьте эту строку!

        MinecraftForge.EVENT_BUS.register(this);
        LOGGER.info("Hunt mod initialized with hospital keys system");
    }
}