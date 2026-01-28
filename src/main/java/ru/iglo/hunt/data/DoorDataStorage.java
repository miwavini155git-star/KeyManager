package ru.iglo.hunt.data;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import ru.iglo.hunt.Hunt;
import ru.iglo.hunt.keys.KeyType;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Класс для сохранения данных о дверях между сессиями
 * Использует систему WorldSavedData из Minecraft
 */
public class DoorDataStorage extends WorldSavedData {
    
    private static final String NAME = Hunt.MODID + "_door_data";
    private final Map<Long, KeyType> doorKeys = new HashMap<>();
    
    public DoorDataStorage() {
        super(NAME);
    }
    
    public DoorDataStorage(String name) {
        super(name);
    }
    
    /**
     * Получает экземпляр DoorDataStorage для мира
     */
    public static DoorDataStorage get(World world) {
        if (world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld) world;
            return serverWorld.getDataStorage().computeIfAbsent(
                    DoorDataStorage::new,
                    NAME
            );
        }
        return null;
    }
    
    /**
     * Сохраняет дверь с ключом
     */
    public void saveDoor(BlockPos doorPos, @Nullable KeyType keyType) {
        if (keyType == null) {
            doorKeys.remove(doorPos.asLong());
        } else {
            doorKeys.put(doorPos.asLong(), keyType);
        }
        this.setDirty();
    }
    
    /**
     * Получает тип ключа для двери
     */
    @Nullable
    public KeyType getDoorKey(BlockPos doorPos) {
        return doorKeys.get(doorPos.asLong());
    }
    
    /**
     * Проверяет, есть ли дверь в хранилище
     */
    public boolean hasDoor(BlockPos doorPos) {
        return doorKeys.containsKey(doorPos.asLong());
    }
    
    /**
     * Удаляет дверь из хранилища
     */
    public void removeDoor(BlockPos doorPos) {
        doorKeys.remove(doorPos.asLong());
        this.setDirty();
    }
    
    /**
     * Сохраняет данные в NBT
     */
    @Override
    public CompoundNBT save(CompoundNBT compound) {
        ListNBT doorsList = new ListNBT();
        
        for (Map.Entry<Long, KeyType> entry : doorKeys.entrySet()) {
            CompoundNBT doorNBT = new CompoundNBT();
            BlockPos pos = BlockPos.of(entry.getKey());
            
            doorNBT.putInt("x", pos.getX());
            doorNBT.putInt("y", pos.getY());
            doorNBT.putInt("z", pos.getZ());
            doorNBT.putString("key_id", entry.getValue().getId());
            
            doorsList.add(doorNBT);
        }
        
        compound.put("doors", doorsList);
        return compound;
    }
    
    /**
     * Загружает данные из NBT
     */
    public void load(CompoundNBT compound) {
        doorKeys.clear();
        
        ListNBT doorsList = compound.getList("doors", 10); // 10 = CompoundNBT
        
        for (int i = 0; i < doorsList.size(); i++) {
            CompoundNBT doorNBT = doorsList.getCompound(i);
            
            int x = doorNBT.getInt("x");
            int y = doorNBT.getInt("y");
            int z = doorNBT.getInt("z");
            String keyId = doorNBT.getString("key_id");
            
            BlockPos pos = new BlockPos(x, y, z);
            KeyType keyType = KeyType.fromId(keyId);
            
            if (keyType != null) {
                doorKeys.put(pos.asLong(), keyType);
            }
        }
    }
    
    /**
     * Получает всех дверей (для отладки)
     */
    public Map<Long, KeyType> getAllDoors() {
        return new HashMap<>(doorKeys);
    }
    
    /**
     * Очищает все данные
     */
    public void clear() {
        doorKeys.clear();
        this.setDirty();
    }
}
