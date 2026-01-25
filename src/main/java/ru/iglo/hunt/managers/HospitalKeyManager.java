package ru.iglo.hunt.managers;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import ru.iglo.hunt.blocks.BlockRegistry;
import ru.iglo.hunt.blocks.HospitalKeyBlock;
import ru.iglo.hunt.items.HospitalKeyItem;
import ru.iglo.hunt.keys.KeyType;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class HospitalKeyManager {
    private static final Random RANDOM = new Random();
    private static final Map<String, KeyType> ROOM_KEYS = new HashMap<>();

    static {
        // Инициализация ключей
        ROOM_KEYS.put("cabinet_1", KeyType.CABINET_1);
        ROOM_KEYS.put("cabinet_2", KeyType.CABINET_2);
        ROOM_KEYS.put("cabinet_3", KeyType.CABINET_3);
        ROOM_KEYS.put("library", KeyType.LIBRARY);
        ROOM_KEYS.put("security", KeyType.SECURITY);
        ROOM_KEYS.put("server", KeyType.SERVER_ROOM);
        ROOM_KEYS.put("basement", KeyType.BASEMENT);
        ROOM_KEYS.put("morgue", KeyType.MORGUE);
        ROOM_KEYS.put("chief", KeyType.CHIEF_DOCTOR);
        ROOM_KEYS.put("electrical", KeyType.ELECTRICAL);
        ROOM_KEYS.put("master", KeyType.MASTER_KEY);
    }

    // Создание ключа по названию комнаты
    public static ItemStack createKeyForRoom(String roomId) {
        KeyType type = ROOM_KEYS.getOrDefault(roomId.toLowerCase(), KeyType.CABINET_1);
        return HospitalKeyItem.createKey(type);
    }

    public static ItemStack createKeyForRoom(String roomId, boolean masterCopy) {
        KeyType type = ROOM_KEYS.getOrDefault(roomId.toLowerCase(), KeyType.CABINET_1);
        return HospitalKeyItem.createKey(type, masterCopy);
    }

    // Создание случайного ключа
    public static ItemStack createRandomHospitalKey() {
        KeyType[] allKeys = KeyType.values();
        KeyType randomType = allKeys[RANDOM.nextInt(allKeys.length)];
        return HospitalKeyItem.createKey(randomType, RANDOM.nextDouble() < 0.05); // 5% шанс мастер-копии
    }

    // Создание полного набора ключей
    public static Map<KeyType, ItemStack> createFullKeySet() {
        Map<KeyType, ItemStack> keySet = new HashMap<>();
        for (KeyType type : KeyType.values()) {
            keySet.put(type, HospitalKeyItem.createKey(type));
        }
        return keySet;
    }

    // Создание ключа администратора
    public static ItemStack createAdminKeySet() {
        ItemStack masterKey = HospitalKeyItem.createKey(KeyType.MASTER_KEY, true);
        CompoundNBT tag = masterKey.getTag();

        // Добавляем особые свойства
        CompoundNBT meta = tag.getCompound("Metadata");
        meta.putBoolean("AdminAccess", true);
        meta.putBoolean("BypassAllLocks", true);
        meta.putInt("AccessLevel", 999);

        tag.putString("DisplayName", "§6КЛЮЧ СУПЕРАДМИНИСТРАТОРА");
        tag.putString("Description", "§7Полный доступ ко всем системам\n§cВЫСШАЯ СЕКРЕТНОСТЬ\n§6Привилегии: MAX");

        masterKey.setTag(tag);
        return masterKey;
    }

    // Установка блока с ключом
    public static boolean placeKeyBlock(World world, BlockPos pos, KeyType keyType, boolean masterCopy) {
        if (world.isClientSide()) return false;

        HospitalKeyBlock.placeHospitalKey(world, pos, keyType, masterCopy);

        System.out.println("[HospitalKeyManager] Размещен ключ: " +
                keyType.getDisplayName() + " на " + pos);
        return true;
    }

    // Автоматическая установка ключей по координатам
    public static void setupHospitalKeySystem() {
        World world = ServerLifecycleHooks.getCurrentServer().overworld();
        if (world == null) return;

        // Пример расположения ключей в больнице
        Map<BlockPos, KeyType> keyLocations = new HashMap<>();

        // Ключи на разных этажах
        keyLocations.put(new BlockPos(10, 65, 20), KeyType.CABINET_1);   // 1 этаж
        keyLocations.put(new BlockPos(25, 65, 15), KeyType.CABINET_2);   // 1 этаж
        keyLocations.put(new BlockPos(15, 70, 30), KeyType.CABINET_3);   // 2 этаж
        keyLocations.put(new BlockPos(40, 75, 25), KeyType.LIBRARY);     // 3 этаж
        keyLocations.put(new BlockPos(5, 65, 5), KeyType.SECURITY);      // Пост охраны
        keyLocations.put(new BlockPos(0, 55, 0), KeyType.SERVER_ROOM);   // Подвал
        keyLocations.put(new BlockPos(-10, 50, 10), KeyType.BASEMENT);   // Техподвал
        keyLocations.put(new BlockPos(-20, 45, 0), KeyType.MORGUE);      // Морг
        keyLocations.put(new BlockPos(50, 80, 50), KeyType.CHIEF_DOCTOR);// 5 этаж
        keyLocations.put(new BlockPos(0, 53, -10), KeyType.ELECTRICAL);  // Щитовая

        // Главный ключ в секретном месте
        keyLocations.put(new BlockPos(100, 60, 100), KeyType.MASTER_KEY);

        // Размещаем все ключи
        for (Map.Entry<BlockPos, KeyType> entry : keyLocations.entrySet()) {
            placeKeyBlock(world, entry.getKey(), entry.getValue(), false);
        }

        System.out.println("[HospitalKeyManager] Система ключей больницы инициализирована");
    }

    // Получение ключа по его ID
    public static KeyType getKeyTypeById(String id) {
        return ROOM_KEYS.getOrDefault(id.toLowerCase(), KeyType.CABINET_1);
    }

    // Проверка доступа
    public static boolean hasAccess(ItemStack key, KeyType requiredType) {
        KeyType keyType = HospitalKeyItem.getKeyType(key);
        int keyLevel = HospitalKeyItem.getAccessLevel(key);
        int requiredLevel = requiredType.getAccessLevel();

        // Мастер-ключ открывает все
        if (keyType == KeyType.MASTER_KEY) return true;

        // Проверка уровня доступа
        return keyLevel >= requiredLevel;
    }
}