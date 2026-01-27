package ru.iglo.hunt.events;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ru.iglo.hunt.Hunt;
import ru.iglo.hunt.utils.DoorUtils;
import ru.iglo.hunt.keys.KeyType;

@Mod.EventBusSubscriber(modid = Hunt.MODID)
public class KeyCabinetSpawner {

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getWorld() instanceof World)) return;
        World world = (World) event.getWorld();

        if (world.isClientSide()) return;

        // Создаем дверь морга на 0,4,0
        BlockPos doorPos = DoorUtils.createIronDoor(world, 0, 4, 0, KeyType.MORGUE);

        if (doorPos != null) {
            System.out.println("[KeyCabinetSpawner] Дверь морга создана на " + doorPos);

            // Создаем шкафчик с ключом от морга рядом
            BlockPos cabinetPos = new BlockPos(3, 4, 0);
            if (world.getBlockState(cabinetPos).isAir()) {
                ru.iglo.hunt.blocks.KeyCabinetBlock.setupCabinet(world, cabinetPos, KeyType.MORGUE);
                System.out.println("[KeyCabinetSpawner] Ключ от морга размещен рядом на " + cabinetPos);
            }

            // Показываем все сохраненные двери (для отладки)
            DoorUtils.printAllDoors();
        }
    }
}