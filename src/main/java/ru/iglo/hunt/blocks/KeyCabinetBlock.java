package ru.iglo.hunt.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import ru.iglo.hunt.items.HospitalKeyItem;
import ru.iglo.hunt.keys.KeyType;

public class KeyCabinetBlock extends Block {
    // Маленький хитбокс, как у ключа
    private static final VoxelShape SHAPE = VoxelShapes.box(
            0.2, 0.0, 0.2,
            0.8, 0.3, 0.8
    );

    // Какой ключ будет выпадать
    private final KeyType containedKeyType;

    // Конструктор с типом ключа
    public KeyCabinetBlock(KeyType keyType) {
        super(Block.Properties.of(Material.METAL)
                .strength(0.5f) // Легко ломается
                .noOcclusion()  // Прозрачный
        );
        this.containedKeyType = keyType;
    }

    // Конструктор по умолчанию (ключ охраны)
    public KeyCabinetBlock() {
        this(KeyType.SECURITY);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return VoxelShapes.empty(); // Нет коллизии
    }

    @Override
    public BlockRenderType getRenderShape(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos,
                                PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (!world.isClientSide()) {
            // Создаем ключ-предмет с уникальными NBT данными
            ItemStack keyStack = HospitalKeyItem.createKey(containedKeyType);

            // Добавляем информацию о том, откуда ключ был взят
            if (keyStack.hasTag()) {
                keyStack.getTag().putString("FromCabinet", pos.toShortString());
                keyStack.getTag().putLong("PickedUpTime", System.currentTimeMillis());
            }

            // Проверяем уникальность ключа перед выпадением
            boolean isUnique = HospitalKeyItem.verifyKeyUniqueness(keyStack);
            if (isUnique) {
                System.out.println("[Hunt] Ключ уникален! UUID: " + HospitalKeyItem.getUniqueId(keyStack));
            }

            // Создаем выпавшую сущность
            ItemEntity itemEntity = new ItemEntity(
                    world,
                    pos.getX() + 0.5,    // Центр блока по X
                    pos.getY() + 0.5,    // Центр блока по Y
                    pos.getZ() + 0.5,    // Центр блока по Z
                    keyStack
            );

            // Эффекты выпадения с небольшим разбросом
            itemEntity.setDeltaMovement(
                    (world.random.nextDouble() - 0.5) * 0.3, // X: -0.15 до 0.15
                    0.25,                                     // Y: подбрасываем вверх
                    (world.random.nextDouble() - 0.5) * 0.3   // Z: -0.15 до 0.15
            );

            itemEntity.setDefaultPickUpDelay(); // 10 тиков задержки перед подбором

            // Добавляем предмет в мир
            world.addFreshEntity(itemEntity);

            // Звук получения предмета
            world.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundCategory.BLOCKS, 0.5F, 1.0F);

            // Звук исчезновения блока (предварительно)
            world.playSound(null, pos, SoundEvents.CHEST_CLOSE, SoundCategory.BLOCKS, 0.8F, 1.0F);

            // Частицы взрыва/распада
            world.levelEvent(2001, pos, Block.getId(state));

            // УДАЛЯЕМ БЛОК (это главное!)
            boolean removed = world.setBlockAndUpdate(pos, net.minecraft.block.Blocks.AIR.defaultBlockState());
            
            if (removed) {
                System.out.println("[Hunt] Блок " + containedKeyType.getDisplayName() + 
                        " успешно удален на " + pos + ". Ключ выпал!");
            } else {
                System.out.println("[Hunt] ОШИБКА: Не удалось удалить блок на " + pos);
            }

            // Сообщение игроку с информацией о ключе
            String keyInfo = "§eПолучен ключ: §6" + containedKeyType.getDisplayName();
            if (isUnique) {
                keyInfo += " §a✓";
            }
            
            player.displayClientMessage(
                    new net.minecraft.util.text.StringTextComponent(keyInfo),
                    true
            );

            return ActionResultType.SUCCESS;
        }

        // На клиенте тоже подтверждаем действие
        return ActionResultType.SUCCESS;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos) {
        return true; // Пропускает свет
    }

    @Override
    public int getLightBlock(BlockState state, IBlockReader world, BlockPos pos) {
        return 0; // Не создает тень
    }

    // Метод для быстрой установки блока
    public static void placeSecurityKeyBlock(World world, BlockPos pos) {
        world.setBlockAndUpdate(pos, BlockRegistry.SECURITY_KEY_CABINET_BLOCK.get().defaultBlockState());
        System.out.println("[Hunt] Блок с ключом охраны установлен на " + pos);
    }

    // Универсальный метод установки кабинета с любым типом ключа
    public static void setupCabinet(World world, BlockPos pos, KeyType keyType) {
        Block blockToPlace;
        
        switch(keyType) {
            case CABINET_1:
                blockToPlace = BlockRegistry.CABINET1_KEY_BLOCK.get();
                break;
            case CABINET_2:
                blockToPlace = BlockRegistry.CABINET2_KEY_BLOCK.get();
                break;
            case CABINET_3:
                blockToPlace = BlockRegistry.CABINET3_KEY_BLOCK.get();
                break;
            case SECURITY:
                blockToPlace = BlockRegistry.SECURITY_KEY_CABINET_BLOCK.get();
                break;
            default:
                blockToPlace = BlockRegistry.KEY_CABINET_BLOCK.get();
                break;
        }
        
        world.setBlockAndUpdate(pos, blockToPlace.defaultBlockState());
        System.out.println("[Hunt] Кабинет с ключом " + keyType.getDisplayName() + " установлен на " + pos);
    }
}