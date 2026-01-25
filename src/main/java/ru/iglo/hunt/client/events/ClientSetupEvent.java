package ru.iglo.hunt.client.events;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import ru.iglo.hunt.Hunt;

/**
 * События инициализации клиента
 * Этот класс минимален, так как система текстур теперь использует CustomModelData
 * вместо Item Properties для совместимости с Minecraft 1.16.5
 */
@Mod.EventBusSubscriber(modid = Hunt.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetupEvent {
    
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // CustomModelData система работает автоматически через JSON predicates
        // Здесь может быть добавлена дополнительная логика клиента при необходимости
        
        System.out.println("[Hunt] Клиент инициализирован. Используется CustomModelData для текстур ключей.");
    }
}
