package ru.iglo.hunt.managers;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import ru.iglo.hunt.blocks.BlockRegistry;
import ru.iglo.hunt.blocks.KeyBlock;
import ru.iglo.hunt.items.ItemRegistry;
import ru.iglo.hunt.items.KeyItem;

import java.util.Random;

public class KeyManager {
    private static final Random RANDOM = new Random();

    // Создание случайного ключа
    public static ItemStack createRandomKey() {
        String[] keyIds = {"bronze_key", "silver_key", "gold_key", "diamond_key", "emerald_key"};
        String[] textures = {"key_bronze.png", "key_silver.png", "key_gold.png", "key_diamond.png", "key_emerald.png"};

        int index = RANDOM.nextInt(keyIds.length);

        CompoundNBT nbt = new CompoundNBT();
        nbt.putString("KeyId", keyIds[index]);
        nbt.putString("TextureName", textures[index]);
        nbt.putInt("KeyLevel", index + 1);
        nbt.putBoolean("Special", RANDOM.nextDouble() < 0.1); // 10% шанс особого
        nbt.putString("DisplayName", getDisplayName(keyIds[index]));

        ItemStack stack = new ItemStack(ItemRegistry.KEY_ITEM.get());
        stack.setTag(nbt);
        return stack;
    }

    private static String getDisplayName(String keyId) {
        switch(keyId) {
            case "bronze_key": return "§6Бронзовый ключ";
            case "silver_key": return "§7Серебряный ключ";
            case "gold_key": return "§eЗолотой ключ";
            case "diamond_key": return "§bАлмазный ключ";
            case "emerald_key": return "§aИзумрудный ключ";
            default: return "§fКлюч";
        }
    }

    // Создание ключа по параметрам
    public static ItemStack createCustomKey(String keyId, int level, boolean special) {
        String texture = "key_" + keyId.split("_")[0] + ".png";

        CompoundNBT nbt = new CompoundNBT();
        nbt.putString("KeyId", keyId);
        nbt.putString("TextureName", texture);
        nbt.putInt("KeyLevel", level);
        nbt.putBoolean("Special", special);
        nbt.putString("DisplayName", "Ключ " + keyId);

        ItemStack stack = new ItemStack(ItemRegistry.KEY_ITEM.get());
        stack.setTag(nbt);
        return stack;
    }

    // Установка блока ключа в мир с NBT
    public static boolean placeKeyBlock(World world, BlockPos pos, CompoundNBT keyNBT) {
        if (world.isClientSide()) return false;

        // Устанавливаем блок
        world.setBlockAndUpdate(pos, BlockRegistry.KEY_BLOCK.get().defaultBlockState());

        // Настраиваем TileEntity
        if (world.getBlockEntity(pos) instanceof ru.iglo.hunt.tileentity.KeyTileEntity) {
            ru.iglo.hunt.tileentity.KeyTileEntity te =
                    (ru.iglo.hunt.tileentity.KeyTileEntity) world.getBlockEntity(pos);
            te.setKeyNBT(keyNBT);
            te.setChanged();

            System.out.println("[KeyManager] Key block placed at " + pos +
                    " with NBT: " + keyNBT.toString());
            return true;
        }

        return false;
    }

    // Быстрый метод для установки блока в 0 -60 0
    public static void placeKeyAtOrigin(String keyType) {
        World world = ServerLifecycleHooks.getCurrentServer().overworld();
        if (world == null) return;

        BlockPos pos = new BlockPos(0, -60, 0);

        CompoundNBT nbt = new CompoundNBT();
        nbt.putString("KeyId", keyType);
        nbt.putString("TextureName", keyType + ".png");
        nbt.putInt("KeyLevel", 1);

        placeKeyBlock(world, pos, nbt);
    }
}