package ru.iglo.hunt.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.tileentity.TileEntity;
import ru.iglo.hunt.items.ItemRegistry;
import ru.iglo.hunt.items.KeyItem;
import ru.iglo.hunt.tileentity.KeyTileEntity;

import javax.annotation.Nullable;

public class KeyBlock extends Block {
    // Хитбокс
    private static final VoxelShape SHAPE = VoxelShapes.box(0.1, 0.0, 0.1, 0.9, 0.1, 0.9);

    // Поддержка TileEntity для хранения NBT
    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new KeyTileEntity();
    }

    public KeyBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return VoxelShapes.empty();
    }

    @Override
    public BlockRenderType getRenderShape(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos,
                                PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (!world.isClientSide()) {
            TileEntity te = world.getBlockEntity(pos);

            if (te instanceof KeyTileEntity) {
                KeyTileEntity keyTE = (KeyTileEntity) te;

                // Получаем NBT данные из TileEntity
                CompoundNBT blockNBT = keyTE.getKeyNBT();

                // Создаем ключ с этими данными
                ItemStack keyStack;

                if (blockNBT != null && !blockNBT.isEmpty()) {
                    // Используем NTF из блока
                    keyStack = new ItemStack(ItemRegistry.KEY_ITEM.get());
                    keyStack.setTag(blockNBT.copy());
                } else {
                    // Дефолтный ключ
                    keyStack = KeyItem.createKey("default_key_" + pos.toShortString());
                }

                // Создаем выпавший предмет
                ItemEntity itemEntity = new ItemEntity(
                        world,
                        pos.getX() + 0.5,
                        pos.getY() + 0.5,
                        pos.getZ() + 0.5,
                        keyStack
                );

                // Эффект выпадения
                itemEntity.setDeltaMovement(
                        world.random.nextDouble() * 0.1 - 0.05,
                        0.2,
                        world.random.nextDouble() * 0.1 - 0.05
                );
                itemEntity.setDefaultPickUpDelay();

                world.addFreshEntity(itemEntity);
                world.setBlockAndUpdate(pos, net.minecraft.block.Blocks.AIR.defaultBlockState());

                System.out.println("[Hunt] Key dropped with NBT: " +
                        (blockNBT != null ? blockNBT.toString() : "none"));

                return ActionResultType.SUCCESS;
            }
        }
        return ActionResultType.SUCCESS;
    }

    // Метод для установки блока с NBT
    public static void placeKeyBlockWithNBT(World world, BlockPos pos, CompoundNBT nbt) {
        // Устанавливаем блок
        world.setBlockAndUpdate(pos, BlockRegistry.KEY_BLOCK.get().defaultBlockState());

        // Настраиваем TileEntity
        TileEntity te = world.getBlockEntity(pos);
        if (te instanceof KeyTileEntity) {
            ((KeyTileEntity) te).setKeyNBT(nbt);
            te.setChanged();
        }
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos) {
        return true;
    }
}