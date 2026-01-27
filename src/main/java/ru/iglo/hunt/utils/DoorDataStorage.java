// DoorDataStorage.java
package ru.iglo.hunt.utils;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.LongNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.storage.WorldSavedData;
import ru.iglo.hunt.keys.KeyType;

import java.util.HashMap;
import java.util.Map;

public class DoorDataStorage extends WorldSavedData {

    private final Map<Long, KeyType> doors = new HashMap<>(); // Long = BlockPos.asLong()

    public DoorDataStorage() {
        super("hunt_doors");
    }

    public DoorDataStorage(String name) {
        super(name);
    }

    public void addDoor(BlockPos pos, KeyType keyType) {
        doors.put(pos.asLong(), keyType);
        setDirty();
    }

    public KeyType getDoorKey(BlockPos pos) {
        return doors.get(pos.asLong());
    }

    public void removeDoor(BlockPos pos) {
        doors.remove(pos.asLong());
        setDirty();
    }

    public boolean hasDoor(BlockPos pos) {
        return doors.containsKey(pos.asLong());
    }

    @Override
    public void load(CompoundNBT nbt) {
        doors.clear();

        if (nbt.contains("doors")) {
            ListNBT doorList = nbt.getList("doors", 10); // TAG_Compound

            for (int i = 0; i < doorList.size(); i++) {
                CompoundNBT doorEntry = doorList.getCompound(i);
                long posLong = doorEntry.getLong("pos");
                String keyTypeId = doorEntry.getString("key");

                doors.put(posLong, KeyType.fromId(keyTypeId));
            }
        }
    }

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        ListNBT doorList = new ListNBT();

        for (Map.Entry<Long, KeyType> entry : doors.entrySet()) {
            CompoundNBT doorEntry = new CompoundNBT();
            doorEntry.putLong("pos", entry.getKey());
            doorEntry.putString("key", entry.getValue().getId());
            doorList.add(doorEntry);
        }

        nbt.put("doors", doorList);
        return nbt;
    }
}