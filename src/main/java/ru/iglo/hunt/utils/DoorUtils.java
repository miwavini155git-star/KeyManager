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
import ru.iglo.hunt.keys.KeyType;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class DoorUtils {

    // Простое хранилище в памяти (только для текущей сессии)
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

        // Сохраняем информацию о ключе
        if (keyType != null) {
            DOOR_KEYS.put(lowerPos.asLong(), keyType);
            System.out.println("[DoorUtils] Дверь создана на " + lowerPos + " с ключом " + keyType);
        } else {
            System.out.println("[DoorUtils] Дверь создана на " + lowerPos + " без ключа");
        }

        // Для долговременного хранения (если это серверный мир)
        if (!world.isClientSide()) {
            saveDoorPersistent(lowerPos, keyType);
        }

        return lowerPos;
    }

    /**
     * Сохраняет дверь для долговременного хранения
     */
    private static void saveDoorPersistent(BlockPos doorPos, KeyType keyType) {
        // Для постоянного хранения между сессиями можно использовать разные методы:
        // 1. Запись в файл
        // 2. Использование WorldSavedData (но нужен доступ к ServerWorld)
        // 3. NBT данных в самом мире

        System.out.println("[DoorUtils] Дверь сохранена для сессии: " + doorPos + " -> " + keyType);
    }

    /**
     * Проверяет, открывается ли дверь данным ключом
     * @param doorPos позиция нижнего блока двери
     * @param keyType тип ключа для проверки
     * @return true если ключ подходит
     */
    public static boolean checkDoorKey(BlockPos doorPos, KeyType keyType) {
        KeyType requiredKey = DOOR_KEYS.get(doorPos.asLong());

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
     * Получает тип ключа, необходимый для двери
     * @param doorPos позиция нижнего блока двери
     * @return тип ключа или null если дверь без ключа
     */
    @Nullable
    public static KeyType getDoorKeyType(BlockPos doorPos) {
        return DOOR_KEYS.get(doorPos.asLong());
    }

    /**
     * Удаляет информацию о двери
     * @param doorPos позиция двери
     */
    public static void removeDoor(BlockPos doorPos) {
        DOOR_KEYS.remove(doorPos.asLong());
        System.out.println("[DoorUtils] Информация о двери удалена: " + doorPos);
    }

    /**
     * Проверяет, есть ли у двери ключ
     * @param doorPos позиция двери
     * @return true если дверь защищена ключом
     */
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