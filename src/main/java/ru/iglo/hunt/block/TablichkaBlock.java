package ru.iglo.hunt.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
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
    
    // Хитбокс для стоячей позиции
    private static final VoxelShape STANDING_SHAPE = VoxelShapes.box(0.2, 0.125, 0.35, 0.8, 0.875, 0.65);
    
    // Хитбокс для лежачей позиции
    private static final VoxelShape LYING_SHAPE = VoxelShapes.box(0.35, 0.125, 0.2, 0.65, 0.875, 0.8);

    public TablichkaBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.getStateDefinition().any().setValue(LYING, false));
    }

    /**
     * Возвращает вокселшейп (форму блока) в зависимости от состояния
     */
    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return VoxelShapes.empty();
    }

    /**
     * Поддержка свойств состояния
     */
    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(LYING);
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
     * Test Обработка клика по блоку
     */
    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player,
                                 Hand hand, BlockRayTraceResult hit) {
        if (world.isClientSide) {
            return ActionResultType.SUCCESS;
        }

        // Переключаем состояние LYING (лежит/стоит)
        BlockState newState = state.cycle(LYING);
        world.setBlock(pos, newState, 3); // 3 = отправить клиентам и обновить модель

        // Обновляем TileEntity если нужна дополнительная логика
        TileEntity tileEntity = world.getBlockEntity(pos);
        if (tileEntity instanceof TablichkaTileEntity) {
            ((TablichkaTileEntity) tileEntity).setLying(newState.getValue(LYING));
            tileEntity.setChanged();
        }

        return ActionResultType.SUCCESS;
    }

    /**
     * Вызывается когда сущность находится внутри/рядом с блоком
     * Переключает табличку во вторую модель и применяет медленость игроку в момент падения
     */
    @Override
    public void entityInside(BlockState state, World world, BlockPos pos, Entity entity) {
        if (!world.isClientSide && entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) entity;
            
            // Переключаем табличку на лежачую позицию (LYING = true) только один раз
            if (!state.getValue(LYING)) {
                BlockState newState = state.setValue(LYING, true);
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
