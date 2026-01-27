package ru.iglo.hunt.events;

import com.mcwdoors.kikoz.init.BlockInit;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ru.iglo.hunt.Hunt;
import ru.iglo.hunt.keys.KeyType;
import ru.iglo.hunt.utils.DoorUtils;

@Mod.EventBusSubscriber(modid = Hunt.MODID)
public class DoorProtectionHandler {

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        World world = (World) event.getWorld();
        BlockPos pos = event.getPos();
        BlockState state = world.getBlockState(pos);
        PlayerEntity player = event.getPlayer();

        if (world.isClientSide()) return;
        if (!state.is(Blocks.IRON_DOOR.getBlock())) return;

        // Ищем нижний блок двери
        BlockPos lowerPos = pos;
        if (state.getValue(net.minecraft.block.DoorBlock.HALF) ==
                net.minecraft.state.properties.DoubleBlockHalf.UPPER) {
            lowerPos = pos.below();
        }

        // Если дверь защищена ключом
        if (DoorUtils.hasDoorKey(lowerPos)) {
            // Разрешаем ломать только в креативе
            if (player != null && !player.isCreative()) {
                event.setCanceled(true);

                KeyType keyType = DoorUtils.getDoorKeyType(lowerPos);
                if (keyType != null) {
                    player.displayClientMessage(
                            new StringTextComponent("§cЭта дверь защищена! Нужен ключ: " +
                                    keyType.getDisplayName()),
                            true
                    );
                } else {
                    player.displayClientMessage(
                            new StringTextComponent("§cЭта дверь не поддается!"),
                            true
                    );
                }

                // Эффект при попытке сломать
                world.levelEvent(2001, pos, net.minecraft.block.Block.getId(state));
            } else if (player != null && player.isCreative()) {
                // В креативе удаляем информацию о двери
                DoorUtils.removeDoor(lowerPos);
            }
        }
    }
}