package ru.iglo.hunt.events;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ru.iglo.hunt.Hunt;
import ru.iglo.hunt.blocks.KeyCabinetBlock;

import java.util.Random;

@Mod.EventBusSubscriber(modid = Hunt.MODID)
public class KeyCabinetSpawner { // Изменили на KeyCabinetSpawner
    private static final Random RANDOM = new Random();
    private static final BlockPos SPAWN_POS = new BlockPos(0, 4, 0);

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        // Используем правильный метод для получения мира
        World world = (World) event.getWorld(); // Исправляем getWorld()

        if (!world.isClientSide()) {
            BlockPos brokenPos = event.getPos();

            if (!brokenPos.equals(SPAWN_POS)) {
                if (world.getBlockState(SPAWN_POS).isAir(world, SPAWN_POS)) {
                    if (RANDOM.nextFloat() < 1.0f) {
                        // Используем правильный метод
                        KeyCabinetBlock.placeSecurityKeyBlock(world, SPAWN_POS);
                        System.out.println("[Hunt] Блок с ключом охраны спавн на " + SPAWN_POS);
                    }
                }
            }
        }
    }
}