package ru.iglo.hunt.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import ru.iglo.hunt.tileentity.TablichkaTileEntity;

import javax.annotation.Nullable;

/**
 * Блок табличка с двумя моделями (стоячая и лежачая)
 */
public class TablichkaBlock extends Block {
    // Свойство состояния - показывает, лежит ли табличка
    public static final BooleanProperty LYING = BooleanProperty.create("lying");
    // Свойство направления (NORTH, SOUTH, EAST, WEST)
    public static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST);
    
    // Один большой хитбокс для стоячей позиции
    private static final VoxelShape STANDING_SHAPE = VoxelShapes.box(0.1, 0.0, 0.1, 0.9, 1.0, 0.9);
    
    // Один большой хитбокс для лежачей позиции
    private static final VoxelShape LYING_SHAPE = VoxelShapes.box(0.0, 0.0, 0.0, 1.0, 0.5, 1.0);

    public TablichkaBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.getStateDefinition().any().setValue(LYING, false).setValue(FACING, Direction.NORTH));
    }

    /**
     * Возвращает вокселшейп (форму блока) в зависимости от состояния
     */
    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        boolean lying = state.getValue(LYING);
        return lying ? LYING_SHAPE : STANDING_SHAPE;
    }

    /**
     * Поддержка свойств состояния
     */
    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(LYING, FACING);
    }

    /**
     * Убрана коллизия - блок не имеет физического столкновения
     */
    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return VoxelShapes.empty();
    }

    /**
     * TileEntity для хранения данных
     */
    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new TablichkaTileEntity();
    }

    /**
     * &#x423;&#x441;&#x442;&#x430;&#x43D;&#x430;&#x432;&#x43B;&#x438;&#x432;&#x430;&#x435;&#x442; &#x43D;&#x430;&#x43F;&#x440;&#x430;&#x432;&#x43B;&#x435;&#x43D;&#x438;&#x435; &#x431;&#x43B;&#x43E;&#x43A;&#x430; &#x43F;&#x440;&#x438; &#x440;&#x430;&#x437;&#x43C;&#x435;&#x449;&#x435;&#x43D;&#x438;&#x438; &#x432; &#x437;&#x430;&#x432;&#x438;&#x441;&#x438;&#x43C;&#x43E;&#x441;&#x442;&#x438; &#x43E;&#x442; &#x432;&#x437;&#x433;&#x43B;&#x44F;&#x434;&#x430; &#x438;&#x433;&#x440;&#x43E;&#x43A;&#x430;
     */
    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        Direction direction = context.getHorizontalDirection().getOpposite();
        return this.defaultBlockState().setValue(FACING, direction).setValue(LYING, false);
    }

//    /**
//     * Обработка клика по блоку - переключение направления и состояния
//     */
//    @Override
//    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player,
//                                 Hand hand, BlockRayTraceResult hit) {
//        if (world.isClientSide) {
//            return ActionResultType.SUCCESS;
//        }
//
//        // Если сидит - переключаем направление (NORTH -> SOUTH -> EAST -> WEST -> NORTH)
//        Direction currentFacing = state.getValue(FACING);
//        Direction newFacing = Direction.NORTH;
//        if (currentFacing == Direction.NORTH) {
//            newFacing = Direction.SOUTH;
//        } else if (currentFacing == Direction.SOUTH) {
//            newFacing = Direction.EAST;
//        } else if (currentFacing == Direction.EAST) {
//            newFacing = Direction.WEST;
//        } else if (currentFacing == Direction.WEST) {
//            newFacing = Direction.NORTH;
//        }
//
//        BlockState newState = state.setValue(FACING, newFacing);
//        world.setBlock(pos, newState, 3); // 3 = отправить клиентам и обновить модель
//
//        // Обновляем TileEntity если нужна дополнительная логика
//        TileEntity tileEntity = world.getBlockEntity(pos);
//        if (tileEntity instanceof TablichkaTileEntity) {
//            ((TablichkaTileEntity) tileEntity).setLying(newState.getValue(LYING));
//            tileEntity.setChanged();
//        }
//
//        return ActionResultType.SUCCESS;
//    }

    /**
     * Вызывается когда сущность находится внутри/рядом с блоком
     * Переключает табличку во вторую модель и падает в сторону от игрока
     */
    @Override
    public void entityInside(BlockState state, World world, BlockPos pos, Entity entity) {
        if (!world.isClientSide && entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) entity;
            
            // Переключаем табличку на лежачую позицию (LYING = true) только один раз
            if (!state.getValue(LYING)) {
                // Определяем направление падения в сторону от игрока
                double playerX = player.getX();
                double playerZ = player.getZ();
                double blockX = pos.getX() + 0.5;
                double blockZ = pos.getZ() + 0.5;
                
                double dx = blockX - playerX;
                double dz = blockZ - playerZ;
                
                Direction fallDirection = Direction.NORTH;
                if (Math.abs(dx) > Math.abs(dz)) {
                    // Падение вправо-влево (EAST-WEST)
                    fallDirection = dx > 0 ? Direction.EAST : Direction.WEST;
                } else {
                    // Падение вперёд-назад (NORTH-SOUTH)
                    fallDirection = dz > 0 ? Direction.NORTH : Direction.SOUTH;
                }
                
                BlockState newState = state.setValue(LYING, true).setValue(FACING, fallDirection);
                world.setBlock(pos, newState, 3);
                
                // Обновляем TileEntity
                TileEntity tileEntity = world.getBlockEntity(pos);
                if (tileEntity instanceof TablichkaTileEntity) {
                    ((TablichkaTileEntity) tileEntity).setLying(true);
                    tileEntity.setChanged();
                }
                
                // Применяем эффект медлености только в момент падения (Slowness II на 2 секунды)
                player.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 40, 1));
            }
        }
    }

    /**
     * Блок не полный куб, поэтому отключаем оптимизацию
     */
    @Override
    public boolean skipRendering(BlockState state, BlockState adjacentBlockState, net.minecraft.util.Direction side) {
        return false;
    }
}
