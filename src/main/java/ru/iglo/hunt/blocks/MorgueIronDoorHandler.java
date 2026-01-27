// MorgueIronDoorHandler.java
package ru.iglo.hunt.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import ru.iglo.hunt.items.HospitalKeyItem;
import ru.iglo.hunt.keys.KeyType;

public class MorgueIronDoorHandler {

    // Метод для обработки взаимодействия с железной дверью
    public static ActionResultType handleIronDoorInteraction(BlockState state, World world, BlockPos pos,
                                                             PlayerEntity player, Hand hand, BlockRayTraceResult hit) {

        if (world.isClientSide()) {
            return ActionResultType.SUCCESS;
        }

        // Проверяем, является ли блок железной дверью
        if (!(state.getBlock() instanceof DoorBlock) ||
                state.getMaterial() != Material.METAL) {
            return ActionResultType.PASS; // Не железная дверь, пропускаем
        }

        // Проверяем координаты двери (0,4,0 и 0,5,0)
        boolean isMorgueDoor = isMorgueDoorLocation(pos);

        if (!isMorgueDoor) {
            return ActionResultType.PASS; // Не дверь морга, пропускаем
        }

        // Проверяем, есть ли у игрока ключ от морга
        ItemStack heldItem = player.getItemInHand(hand);
        boolean hasMorgueKey = false;

        if (heldItem.getItem() instanceof HospitalKeyItem) {
            KeyType keyType = HospitalKeyItem.getKeyType(heldItem);
            if (keyType == KeyType.MORGUE) {
                hasMorgueKey = true;
                System.out.println("[MorgueDoor] Игрок имеет ключ от морга: " +
                        HospitalKeyItem.getSerialNumber(heldItem));
            }
        }

        if (hasMorgueKey) {
            // Открываем/закрываем дверь как обычно
            DoorBlock door = (DoorBlock) state.getBlock();
            return door.use(state, world, pos, player, hand, hit);
        } else {
            // Игрок не имеет ключа
            player.displayClientMessage(
                    new StringTextComponent("§cЭта железная дверь требует ключ от морга!"),
                    true
            );

            // Звук отказа
            world.levelEvent(1001, pos, 0);

            return ActionResultType.FAIL;
        }
    }

    // Метод для проверки, можно ли сломать железную дверь
    public static boolean canBreakIronDoor(BlockState state, World world, BlockPos pos, PlayerEntity player) {
        // Проверяем, является ли блок железной дверью на позиции морга
        if (!(state.getBlock() instanceof DoorBlock) ||
                state.getMaterial() != Material.METAL) {
            return true; // Не железная дверь, можно ломать
        }

        if (isMorgueDoorLocation(pos)) {
            // Нельзя сломать железную дверь морга в выживании
            if (!player.isCreative()) {
                player.displayClientMessage(
                        new StringTextComponent("§cЭта железная дверь слишком прочная! Найдите ключ от морга."),
                        true
                );
                return false;
            }
        }

        return true;
    }

    // Проверяем, находится ли позиция в области двери морга
    private static boolean isMorgueDoorLocation(BlockPos pos) {
        // Дверь занимает две позиции: (0,4,0) и (0,5,0)
        return (pos.getX() == 0 && (pos.getY() == 4 || pos.getY() == 5) && pos.getZ() == 0);
    }

    // Метод для установки железной двери
    public static void placeMorgueIronDoor(World world, BlockPos pos) {
        // Используем стандартную железную дверь Minecraft
        net.minecraft.block.Blocks.IRON_DOOR.defaultBlockState();

        // Устанавливаем нижнюю часть двери
        BlockState lowerDoorState = net.minecraft.block.Blocks.IRON_DOOR.defaultBlockState()
                .setValue(DoorBlock.FACING, net.minecraft.util.Direction.NORTH)
                .setValue(DoorBlock.OPEN, false)
                .setValue(DoorBlock.HINGE, net.minecraft.state.properties.DoorHingeSide.LEFT)
                .setValue(DoorBlock.POWERED, false)
                .setValue(DoorBlock.HALF, net.minecraft.state.properties.DoubleBlockHalf.LOWER);

        // Устанавливаем верхнюю часть двери
        BlockState upperDoorState = net.minecraft.block.Blocks.IRON_DOOR.defaultBlockState()
                .setValue(DoorBlock.FACING, net.minecraft.util.Direction.NORTH)
                .setValue(DoorBlock.OPEN, false)
                .setValue(DoorBlock.HINGE, net.minecraft.state.properties.DoorHingeSide.LEFT)
                .setValue(DoorBlock.POWERED, false)
                .setValue(DoorBlock.HALF, net.minecraft.state.properties.DoubleBlockHalf.UPPER);

        // Размещаем обе части двери
        world.setBlock(pos, lowerDoorState, 3);
        world.setBlock(pos.above(), upperDoorState, 3);

        System.out.println("[MorgueDoor] Стандартная железная дверь Minecraft установлена на " + pos);
    }
}