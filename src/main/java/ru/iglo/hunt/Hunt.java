package ru.iglo.hunt;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.storage.FolderName;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mozilla.HuntEngine.Command.HuntCommand;
import org.mozilla.HuntEngine.HuntEngine;
import org.mozilla.javascript.*;
import ru.iglo.hunt.blocks.BlockRegistry;
import ru.iglo.hunt.items.ItemRegistry;
import ru.iglo.hunt.tileentity.TileEntityRegistry;
import software.bernie.geckolib3.GeckoLib;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Mod(Hunt.MODID)
public class Hunt {
    public static final String MODID = "hunt";
    public static final Logger LOGGER = LogManager.getLogger();

    public Hunt() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        GeckoLib.initialize();
        
        try {
            Context.enter().initStandardObjects();
            LOGGER.info("Rhino JavaScript engine initialized successfully");
        } finally {
            Context.exit();
        }

        BlockRegistry.BLOCKS.register(modEventBus);
        ItemRegistry.ITEMS.register(modEventBus);
        TileEntityRegistry.TILE_ENTITIES.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);
        LOGGER.info("Hunt mod initialized with hospital keys system");
    }

    @Mod.EventBusSubscriber
    public static class EventBreakServer {
        @SubscribeEvent
        public static void onServerStart(FMLServerStartingEvent event) {
            HuntCommand.register(event.getServer().getCommands().getDispatcher());
        }
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class EventBreakClient {
        @SubscribeEvent
        public static void event(PlayerEvent.PlayerLoggedInEvent event) {
            Path huntEngineDir = null;
            try {
                Path worldDir = event.getPlayer().level.getServer().getWorldPath(FolderName.ROOT);
                huntEngineDir = worldDir.resolve("HuntEngine");

                if (!Files.exists(huntEngineDir)) {
                    Files.createDirectories(huntEngineDir);
                    LOGGER.info("HuntEngine directory created at: " + huntEngineDir);
                }
            } catch (IOException e) {
                LOGGER.error("Error creating HuntEngine directory", e);
            }
            Path scriptPath = huntEngineDir.resolve("test.js");
            // HuntEngine engine = new HuntEngine((ServerPlayerEntity) event.getPlayer());
            // engine.executeScript(scriptPath);
        }
    }

}