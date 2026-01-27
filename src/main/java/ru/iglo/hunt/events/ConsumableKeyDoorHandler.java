// ConsumableKeyDoorHandler.java
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
public class ConsumableKeyDoorHandler {

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

        // Проверяем, защищена ли эта дверь
        if (DoorUtils.hasDoorKey(lowerPos)) {
            // Дверь защищена, проверяем ключ у игрока
            ItemStack heldItem = player.getItemInHand(hand);
            KeyType playerKeyType = null;

            if (heldItem.getItem() instanceof HospitalKeyItem) {
                playerKeyType = HospitalKeyItem.getKeyType(heldItem);
            }

            if (playerKeyType != null && DoorUtils.checkDoorKey(lowerPos, playerKeyType)) {
                // Ключ подходит - открываем дверь
                toggleDoor(world, lowerPos);

                // УДАЛЯЕМ КЛЮЧ (если не в креативе)
                if (!player.isCreative()) {
                    // Получаем предмет в руке

                    // Уменьшаем количество на 1
                    heldItem.shrink(1);

                    // Если ключ закончился, удаляем стек полностью
                    if (heldItem.isEmpty()) {
                        player.setItemInHand(hand, ItemStack.EMPTY);
                    }

                    player.displayClientMessage(
                            new StringTextComponent("§6Ключ " + playerKeyType.getDisplayName() + " израсходован!"),
                            true
                    );
                }

                // Информационное сообщение
                KeyType requiredKey = DoorUtils.getDoorKeyType(lowerPos);
                boolean isOpen = world.getBlockState(lowerPos).getValue(DoorBlock.OPEN);

                player.displayClientMessage(
                        new StringTextComponent("§aДверь " + (isOpen ? "открыта" : "закрыта")),
                        true
                );

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
                        new StringTextComponent("§cТребуется ключ: " + requiredKey.getDisplayName()),
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
    private static void toggleDoor(World world, BlockPos lowerPos) {
        BlockState state = world.getBlockState(lowerPos);
        if (!state.is(Blocks.IRON_DOOR.getBlock())) return;

        BlockPos upperPos = lowerPos.above();
        BlockState upperState = world.getBlockState(upperPos);

        // Текущее состояние открытости
        boolean isOpen = state.getValue(DoorBlock.OPEN);

        // Меняем состояние на противоположное
        BlockState newState = state.setValue(DoorBlock.OPEN, !isOpen);
        BlockState newUpperState = upperState.setValue(DoorBlock.OPEN, !isOpen);

        // Обновляем оба блока
        world.setBlock(lowerPos, newState, 10);
        world.setBlock(upperPos, newUpperState, 10);

        // Проигрываем звук
        world.levelEvent(null, isOpen ? 1011 : 1005, lowerPos, 0);

        System.out.println("[ConsumableKeyDoor] Дверь на " + lowerPos + " " +
                (isOpen ? "закрыта" : "открыта") + " (ключ удален)");
    }
}