package ru.iglo.hunt.utils;

import com.mcwdoors.kikoz.init.BlockInit;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import ru.iglo.hunt.data.DoorDataStorage;
import ru.iglo.hunt.keys.KeyType;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class DoorUtils {

    // Для обратной совместимости (может быть удалено позже)
    private static final Map<Long, KeyType> DOOR_KEYS = new HashMap<>(); // Long = BlockPos.asLong()

    /**
     * Создает железную дверь с привязкой к ключу
     * @param world мир
     * @param x координата X
     * @param y координата Y (нижний блок двери)
     * @param z координата Z
     * @param keyType тип ключа для открытия (null = открывается без ключа)
     * @return BlockPos нижнего блока двери
     */
    @Nullable
    public static BlockPos createIronDoor(World world, int x, int y, int z, KeyType keyType) {
        BlockPos lowerPos = new BlockPos(x, y, z);
        BlockPos upperPos = lowerPos.above();

        // Проверяем, можно ли поставить дверь
        if (!world.getBlockState(lowerPos).isAir() || !world.getBlockState(upperPos).isAir()) {
            System.out.println("[DoorUtils] Не могу установить дверь - место занято!");
            return null;
        }

        // Создаем состояния для нижней и верхней частей
        BlockState lowerState = Blocks.IRON_DOOR.defaultBlockState()
                .setValue(DoorBlock.FACING, Direction.NORTH)
                .setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER)
                .setValue(DoorBlock.OPEN, false)
                .setValue(DoorBlock.POWERED, false);

        BlockState upperState = Blocks.IRON_DOOR.defaultBlockState()
                .setValue(DoorBlock.FACING, Direction.NORTH)
                .setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER)
                .setValue(DoorBlock.OPEN, false)
                .setValue(DoorBlock.POWERED, false);

        // Устанавливаем блоки
        world.setBlock(lowerPos, lowerState, 3);
        world.setBlock(upperPos, upperState, 3);

        // Сохраняем информацию о ключе в оба хранилища
        if (keyType != null) {
            // Локальное хранилище (для сессии)
            DOOR_KEYS.put(lowerPos.asLong(), keyType);
            
            // Долговременное хранилище (между сессиями)
            saveDoorPersistent(world, lowerPos, keyType);
            
            System.out.println("[DoorUtils] Дверь создана на " + lowerPos + " с ключом " + keyType);
        } else {
            System.out.println("[DoorUtils] Дверь создана на " + lowerPos + " без ключа");
        }

        return lowerPos;
    }

    /**
     * Сохраняет дверь для долговременного хранения между сессиями
     */
    private static void saveDoorPersistent(World world, BlockPos doorPos, KeyType keyType) {
        if (world.isClientSide()) {
            return;
        }
        
        DoorDataStorage storage = DoorDataStorage.get(world);
        if (storage != null) {
            storage.saveDoor(doorPos, keyType);
            System.out.println("[DoorUtils] Дверь сохранена в WorldSavedData: " + doorPos + " -> " + keyType);
        }
    }

    /**
     * Проверяет, открывается ли дверь данным ключом
     * @param doorPos позиция нижнего блока двери
     * @param keyType тип ключа для проверки
     * @param world мир (для получения сохраненных данных)
     * @return true если ключ подходит
     */
    public static boolean checkDoorKey(BlockPos doorPos, KeyType keyType, World world) {
        KeyType requiredKey = null;
        
        // Сначала проверяем долговременное хранилище
        if (!world.isClientSide()) {
            DoorDataStorage storage = DoorDataStorage.get(world);
            if (storage != null) {
                requiredKey = storage.getDoorKey(doorPos);
            }
        }
        
        // Если не найдено, проверяем локальное хранилище
        if (requiredKey == null) {
            requiredKey = DOOR_KEYS.get(doorPos.asLong());
        }

        if (requiredKey == null) {
            return true; // Дверь без ключа открывается всегда
        }

        // Мастер-ключ открывает все
        if (keyType == KeyType.MASTER_KEY) {
            return true;
        }

        return keyType == requiredKey;
    }
    
    /**
     * Старая версия без мира (не рекомендуется использовать)
     * @deprecated Используйте checkDoorKey(BlockPos, KeyType, World) вместо этого
     */
    @Deprecated
    public static boolean checkDoorKey(BlockPos doorPos, KeyType keyType) {
        KeyType requiredKey = DOOR_KEYS.get(doorPos.asLong());

        if (requiredKey == null) {
            return true;
        }

        if (keyType == KeyType.MASTER_KEY) {
            return true;
        }

        return keyType == requiredKey;
    }

    /**
     * Получает тип ключа, необходимый для двери
     * @param doorPos позиция нижнего блока двери
     * @param world мир (для получения сохраненных данных)
     * @return тип ключа или null если дверь без ключа
     */
    @Nullable
    public static KeyType getDoorKeyType(BlockPos doorPos, World world) {
        KeyType keyType = null;
        
        // Сначала проверяем долговременное хранилище
        if (!world.isClientSide()) {
            DoorDataStorage storage = DoorDataStorage.get(world);
            if (storage != null) {
                keyType = storage.getDoorKey(doorPos);
            }
        }
        
        // Если не найдено, проверяем локальное хранилище
        if (keyType == null) {
            keyType = DOOR_KEYS.get(doorPos.asLong());
        }
        
        return keyType;
    }
    
    /**
     * Старая версия без мира (не рекомендуется использовать)
     * @deprecated Используйте getDoorKeyType(BlockPos, World) вместо этого
     */
    @Deprecated
    @Nullable
    public static KeyType getDoorKeyType(BlockPos doorPos) {
        return DOOR_KEYS.get(doorPos.asLong());
    }

    /**
     * Удаляет информацию о двери
     * @param doorPos позиция двери
     * @param world мир
     */
    public static void removeDoor(BlockPos doorPos, World world) {
        DOOR_KEYS.remove(doorPos.asLong());
        
        if (!world.isClientSide()) {
            DoorDataStorage storage = DoorDataStorage.get(world);
            if (storage != null) {
                storage.removeDoor(doorPos);
            }
        }
        
        System.out.println("[DoorUtils] Информация о двери удалена: " + doorPos);
    }
    
    /**
     * Старая версия без мира (не рекомендуется использовать)
     * @deprecated Используйте removeDoor(BlockPos, World) вместо этого
     */
    @Deprecated
    public static void removeDoor(BlockPos doorPos) {
        DOOR_KEYS.remove(doorPos.asLong());
        System.out.println("[DoorUtils] Информация о двери удалена: " + doorPos);
    }

    /**
     * Проверяет, есть ли у двери ключ
     * @param doorPos позиция двери
     * @param world мир
     * @return true если дверь защищена ключом
     */
    public static boolean hasDoorKey(BlockPos doorPos, World world) {
        // Проверяем долговременное хранилище
        if (!world.isClientSide()) {
            DoorDataStorage storage = DoorDataStorage.get(world);
            if (storage != null && storage.hasDoor(doorPos)) {
                return true;
            }
        }
        
        // Проверяем локальное хранилище
        return DOOR_KEYS.containsKey(doorPos.asLong());
    }
    
    /**
     * Старая версия без мира (не рекомендуется использовать)
     * @deprecated Используйте hasDoorKey(BlockPos, World) вместо этого
     */
    @Deprecated
    public static boolean hasDoorKey(BlockPos doorPos) {
        return DOOR_KEYS.containsKey(doorPos.asLong());
    }

    /**
     * Получает все двери с их ключами (для отладки)
     */
    public static void printAllDoors() {
        System.out.println("=== Все сохраненные двери ===");
        for (Map.Entry<Long, KeyType> entry : DOOR_KEYS.entrySet()) {
            BlockPos pos = BlockPos.of(entry.getKey());
            System.out.println(pos + " -> " + entry.getValue().getDisplayName());
        }
        System.out.println("============================");
    }

    /**
     * Получает имя двери для отображения
     */
    public static void debugDoorInfo(BlockPos doorPos) {
        System.out.println("[DoorUtils Debug] Проверка двери на " + doorPos);
        System.out.println("  - hasDoorKey: " + hasDoorKey(doorPos));
        System.out.println("  - getDoorKeyType: " + getDoorKeyType(doorPos));

        if (hasDoorKey(doorPos)) {
            KeyType key = getDoorKeyType(doorPos);
            System.out.println("  - Key ID: " + (key != null ? key.getId() : "null"));
            System.out.println("  - Key Name: " + (key != null ? key.getDisplayName() : "null"));
        }
    }
    public static String getDoorDisplayName(BlockPos doorPos) {
        KeyType keyType = getDoorKeyType(doorPos);
        if (keyType == null) {
            return "Железная дверь";
        }
        return "Дверь (" + keyType.getDisplayName() + ")";
    }
}