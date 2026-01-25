package ru.iglo.hunt.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import ru.iglo.hunt.items.ItemRegistry;

public class SpecialKeyBlock extends Block {
    // Такой же хитбокс как у обычного KeyBlock
    private static final VoxelShape SHAPE = VoxelShapes.box(
            0.1, 0.0, 0.1,
            0.9, 0.1, 0.9
    );

    public SpecialKeyBlock(Properties properties) {
        super(properties
                .noOcclusion()
                .strength(0.0f)
        );
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
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (!world.isClientSide()) {
            // Выпадает ТОТ ЖЕ предмет, что и у обычного ключа (или можно сделать другой)
            Item keyItem = ItemRegistry.KEY_ITEM.get();

            if (keyItem != null) {
                ItemStack itemStack = new ItemStack(keyItem, 1);
                ItemEntity itemEntity = new ItemEntity(
                        world,
                        pos.getX() + 0.5,
                        pos.getY() + 0.5,
                        pos.getZ() + 0.5,
                        itemStack
                );

                itemEntity.setDeltaMovement(
                        world.random.nextDouble() * 0.1 - 0.05,
                        0.2,
                        world.random.nextDouble() * 0.1 - 0.05
                );
                itemEntity.setDefaultPickUpDelay();

                world.addFreshEntity(itemEntity);
                world.setBlockAndUpdate(pos, net.minecraft.block.Blocks.AIR.defaultBlockState());

                System.out.println("[Hunt] SpecialKeyBlock clicked! Dropped KeyItem at " + pos);

                return ActionResultType.SUCCESS;
            }
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos) {
        return true;
    }

    @Override
    public int getLightBlock(BlockState state, IBlockReader world, BlockPos pos) {
        return 0;
    }
}