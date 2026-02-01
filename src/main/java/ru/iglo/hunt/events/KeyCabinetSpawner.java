package ru.iglo.hunt.events;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ru.iglo.hunt.Hunt;
import ru.iglo.hunt.blocks.KeyCabinetBlock;
import ru.iglo.hunt.utils.DoorUtils;
import ru.iglo.hunt.keys.KeyType;

@Mod.EventBusSubscriber(modid = Hunt.MODID)
public class KeyCabinetSpawner {

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        KeyCabinetBlock.setupCabinet((World) event.getWorld(), new BlockPos(-2,4,2), KeyType.MORGUE);
        KeyCabinetBlock.setupCabinet((World) event.getWorld(), new BlockPos( -13,4,-2), KeyType.CABINET_1);
        DoorUtils.createIronDoor(event.getPlayer().level, 0, 4, 0, KeyType.MORGUE);
        DoorUtils.createIronDoor(event.getPlayer().level, -14,4,0, KeyType.CABINET_1);
    }
}