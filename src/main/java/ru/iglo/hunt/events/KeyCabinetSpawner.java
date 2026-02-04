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
    }
}