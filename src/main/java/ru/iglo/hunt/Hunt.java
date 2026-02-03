package ru.iglo.hunt;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.iglo.hunt.blocks.BlockRegistry;
import ru.iglo.hunt.items.ItemRegistry;
import ru.iglo.hunt.tileentity.TileEntityRegistry;
import software.bernie.geckolib3.GeckoLib;

@Mod(Hunt.MODID)
public class Hunt {
    public static final String MODID = "hunt";
    public static final Logger LOGGER = LogManager.getLogger();

    public Hunt() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        GeckoLib.initialize();
        BlockRegistry.BLOCKS.register(modEventBus);
        ItemRegistry.ITEMS.register(modEventBus);
        TileEntityRegistry.TILE_ENTITIES.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);
        LOGGER.info("Hunt mod initialized with hospital keys system");
    }
}