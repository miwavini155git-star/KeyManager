// MorgueIronDoor.java
package ru.iglo.hunt.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.properties.DoorHingeSide;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import ru.iglo.hunt.items.HospitalKeyItem;
import ru.iglo.hunt.keys.KeyType;

public class MorgueIronDoor extends DoorBlock {

    public MorgueIronDoor() {
        super(Properties.of(Material.METAL, MaterialColor.METAL)
                .strength(50.0F, 6000.0F) // Очень прочная, как обсидиан
                .requiresCorrectToolForDrops() // Требует инструмент для дропа
                .harvestTool(ToolType.PICKAXE) // Можно ломать только киркой
                .harvestLevel(3) // Требуется алмазная кирка или выше
                .noOcclusion() // Прозрачная для отображения
                .sound(net.minecraft.block.SoundType.METAL)
        );
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos,
                                PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (world.isClientSide()) {
            return ActionResultType.SUCCESS;
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
            state = state.cycle(OPEN);
            world.setBlock(pos, state, 10);

            // Обновляем вторую половину двери
            boolean isOpen = state.getValue(OPEN);
            BlockPos otherPos = state.getValue(HALF) == DoubleBlockHalf.LOWER ?
                    pos.above() : pos.below();
            BlockState otherState = world.getBlockState(otherPos);
            if (otherState.getBlock() instanceof DoorBlock) {
                world.setBlock(otherPos, otherState.setValue(OPEN, isOpen), 10);
            }

            // Звук открытия
            world.levelEvent(player, isOpen ? 1005 : 1011, pos, 0);

            // Сообщение игроку
            player.displayClientMessage(
                    new StringTextComponent("§aДверь морга " + (isOpen ? "открыта" : "закрыта") + " ключом"),
                    true
            );

            return ActionResultType.SUCCESS;
        } else {
            // Игрок не имеет ключа
            player.displayClientMessage(
                    new StringTextComponent("§cЭта дверь требует ключ от морга!"),
                    true
            );

            // Звук отказа
            world.levelEvent(1001, pos, 0);

            return ActionResultType.FAIL;
        }
    }

    @Override
    public boolean canHarvestBlock(BlockState state, net.minecraft.world.IBlockReader world,
                                   BlockPos pos, PlayerEntity player) {
        // Нельзя сломать даже с правильным инструментом в режиме выживания
        if (player != null && !player.isCreative()) {
            player.displayClientMessage(
                    new StringTextComponent("§cЭта дверь слишком прочная! Найдите ключ от морга."),
                    true
            );
            return false;
        }
        return super.canHarvestBlock(state, world, pos, player);
    }

    @Override
    public void playerWillDestroy(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        // В креативе можно ломать, в выживании - нет
        if (!player.isCreative()) {
            // Удаляем вторую половину двери
            DoubleBlockHalf half = state.getValue(HALF);
            BlockPos otherPos = half == DoubleBlockHalf.LOWER ? pos.above() : pos.below();
            BlockState otherState = world.getBlockState(otherPos);

            if (otherState.getBlock() == this) {
                world.setBlock(otherPos, net.minecraft.block.Blocks.AIR.defaultBlockState(), 35);
                world.levelEvent(player, 2001, otherPos, Block.getId(otherState));
            }
        }

        super.playerWillDestroy(world, pos, state, player);
    }
}