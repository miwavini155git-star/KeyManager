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
import ru.iglo.hunt.items.HospitalKeyItem;
import ru.iglo.hunt.keys.KeyType;
import ru.iglo.hunt.tileentity.HospitalKeyTileEntity;

import javax.annotation.Nullable;

public class HospitalKeyBlock extends Block {
    private static final VoxelShape SHAPE = VoxelShapes.box(0.1, 0.0, 0.1, 0.9, 0.1, 0.9);

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new HospitalKeyTileEntity();
    }

    public HospitalKeyBlock(Properties properties) {
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

            if (te instanceof HospitalKeyTileEntity) {
                HospitalKeyTileEntity keyTE = (HospitalKeyTileEntity) te;

                // Получаем NBT из TileEntity
                CompoundNBT blockNBT = keyTE.getKeyNBT();

                // Создаем ключ-предмет
                ItemStack keyStack;

                if (blockNBT != null && !blockNBT.isEmpty()) {
                    // Используем NBT из блока
                    String keyTypeId = blockNBT.getString("KeyType");
                    KeyType keyType = KeyType.fromId(keyTypeId);

                    boolean masterCopy = blockNBT.contains("MasterCopy") &&
                            blockNBT.getBoolean("MasterCopy");

                    keyStack = HospitalKeyItem.createKey(keyType, masterCopy);

                    // Копируем все NBT из блока
                    keyStack.setTag(blockNBT.copy());
                } else {
                    // Дефолтный ключ
                    keyStack = HospitalKeyItem.createKey(KeyType.CABINET_1);
                }

                // Выпадающая сущность
                ItemEntity itemEntity = new ItemEntity(
                        world,
                        pos.getX() + 0.5,
                        pos.getY() + 0.5,
                        pos.getZ() + 0.5,
                        keyStack
                );

                // Эффекты
                itemEntity.setDeltaMovement(
                        world.random.nextDouble() * 0.1 - 0.05,
                        0.25,
                        world.random.nextDouble() * 0.1 - 0.05
                );
                itemEntity.setDefaultPickUpDelay();

                // Добавляем в мир
                world.addFreshEntity(itemEntity);

                // Удаляем блок
                world.setBlockAndUpdate(pos, net.minecraft.block.Blocks.AIR.defaultBlockState());

                // Логирование
                String keyName = keyStack.hasTag() ?
                        keyStack.getTag().getString("DisplayName") : "Unknown Key";
                System.out.println("[Hospital] Выпал ключ: " + keyName + " из блока на " + pos);

                return ActionResultType.SUCCESS;
            }
        }
        return ActionResultType.SUCCESS;
    }

    // Метод для установки блока с ключом определенного типа
    public static void placeHospitalKey(World world, BlockPos pos, KeyType keyType, boolean masterCopy) {
        world.setBlockAndUpdate(pos, BlockRegistry.HOSPITAL_KEY_BLOCK.get().defaultBlockState());

        TileEntity te = world.getBlockEntity(pos);
        if (te instanceof HospitalKeyTileEntity) {
            HospitalKeyTileEntity keyTE = (HospitalKeyTileEntity) te;

            // Создаем NBT для ключа
            ItemStack keyStack = HospitalKeyItem.createKey(keyType, masterCopy);
            keyTE.setKeyNBT(keyStack.getTag());
            keyTE.setChanged();
        }
    }

    public static void placeHospitalKey(World world, BlockPos pos, KeyType keyType) {
        placeHospitalKey(world, pos, keyType, false);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos) {
        return true;
    }
}