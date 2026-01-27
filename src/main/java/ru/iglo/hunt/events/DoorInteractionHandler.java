package ru.iglo.hunt.events;

import com.mcwdoors.kikoz.init.BlockInit;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ru.iglo.hunt.Hunt;
import ru.iglo.hunt.items.HospitalKeyItem;
import ru.iglo.hunt.keys.KeyType;
import ru.iglo.hunt.utils.DoorUtils;

@Mod.EventBusSubscriber(modid = Hunt.MODID)
public class DoorInteractionHandler {

    @SubscribeEvent
    public static void onDoorInteract(PlayerInteractEvent.RightClickBlock event) {
        World world = event.getWorld();
        BlockPos pos = event.getPos();
        BlockState state = world.getBlockState(pos);
        PlayerEntity player = event.getPlayer();
        Hand hand = event.getHand();

        if (world.isClientSide()) return;
        if (!state.is(Blocks.IRON_DOOR.getBlock())) return;

        // Ищем нижний блок двери
        BlockPos lowerPos = pos;
        if (state.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER) {
            lowerPos = pos.below();
        }

        // Получаем состояние нижней части двери
        BlockState lowerState = world.getBlockState(lowerPos);

        // Проверяем, защищена ли эта дверь
        if (DoorUtils.hasDoorKey(lowerPos)) {
            // Дверь защищена, проверяем ключ у игрока
            ItemStack heldItem = player.getItemInHand(hand);
            KeyType playerKeyType = null;

            if (heldItem.getItem() instanceof HospitalKeyItem) {
                playerKeyType = HospitalKeyItem.getKeyType(heldItem);
            }

            if (playerKeyType != null && DoorUtils.checkDoorKey(lowerPos, playerKeyType)) {
                // Ключ подходит - ОТКРЫВАЕМ/ЗАКРЫВАЕМ ДВЕРЬ
                toggleDoor(world, lowerPos, lowerState);

                KeyType requiredKey = DoorUtils.getDoorKeyType(lowerPos);
                String doorName = DoorUtils.getDoorDisplayName(lowerPos);
                boolean isOpen = lowerState.getValue(DoorBlock.OPEN);

                player.displayClientMessage(
                        new StringTextComponent("§a" + doorName + " " +
                                (isOpen ? "закрыта" : "открыта") +
                                " ключом " + playerKeyType.getDisplayName()),
                        true
                );

                // Отменяем событие и возвращаем успех
                event.setCanceled(true);
                event.setCancellationResult(ActionResultType.SUCCESS);
                return;
            }

            // Ключ не подошел или его нет
            event.setCanceled(true);
            event.setCancellationResult(ActionResultType.FAIL);

            KeyType requiredKey = DoorUtils.getDoorKeyType(lowerPos);
            if (requiredKey != null) {
                player.displayClientMessage(
                        new StringTextComponent("§cЭта дверь требует ключ: " + requiredKey.getDisplayName()),
                        true
                );
            } else {
                player.displayClientMessage(
                        new StringTextComponent("§cЭта дверь требует специальный ключ!"),
                        true
                );
            }

            world.levelEvent(1001, pos, 0); // Звук отказа
        }
        // Если дверь не защищена, пропускаем стандартную обработку Minecraft
    }

    /**
     * Открывает/закрывает дверь
     */
    private static void toggleDoor(World world, BlockPos lowerPos, BlockState lowerState) {
        if (!lowerState.is(Blocks.IRON_DOOR.getBlock())) return;

        // Получаем верхнюю часть двери
        BlockPos upperPos = lowerPos.above();
        BlockState upperState = world.getBlockState(upperPos);

        // Текущее состояние открытости
        boolean isOpen = lowerState.getValue(DoorBlock.OPEN);

        // Меняем состояние на противоположное
        BlockState newLowerState = lowerState.setValue(DoorBlock.OPEN, !isOpen);
        BlockState newUpperState = upperState.setValue(DoorBlock.OPEN, !isOpen);

        // Обновляем оба блока
        world.setBlock(lowerPos, newLowerState, 10); // 10 = UPDATE_ALL
        world.setBlock(upperPos, newUpperState, 10);

        // Проигрываем звук
        world.levelEvent(null, isOpen ? 1011 : 1005, lowerPos, 0);

        System.out.println("[DoorInteraction] Дверь на " + lowerPos + " " +
                (isOpen ? "закрыта" : "открыта"));
    }
}