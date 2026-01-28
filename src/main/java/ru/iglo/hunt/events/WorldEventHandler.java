package ru.iglo.hunt.events;

import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ru.iglo.hunt.Hunt;
import ru.iglo.hunt.data.DoorDataStorage;

/**
 * Обработчик событий мира для загрузки данных дверей
 */
@Mod.EventBusSubscriber(modid = Hunt.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class WorldEventHandler {
    
    /**
     * Загружает данные дверей при загрузке мира (для сервера)
     */
    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event) {
        World world = (World) event.getWorld();
        
        if (!world.isClientSide() && world instanceof ServerWorld) {
            DoorDataStorage storage = DoorDataStorage.get(world);
            if (storage != null) {
                System.out.println("[Hunt] Загружены данные дверей для мира: " + world.dimension());
                System.out.println("[Hunt] Всего дверей: " + storage.getAllDoors().size());
            }
        }
    }
    
    /**
     * Сохраняет данные дверей при сохранении мира
     */
    @SubscribeEvent
    public static void onWorldSave(WorldEvent.Save event) {
        World world = (World) event.getWorld();
        
        if (!world.isClientSide() && world instanceof ServerWorld) {
            DoorDataStorage storage = DoorDataStorage.get(world);
            if (storage != null) {
                storage.setDirty();
                System.out.println("[Hunt] Данные дверей сохранены");
            }
        }
    }
}
