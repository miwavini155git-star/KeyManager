package ru.iglo.hunt.items;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemUseContext;
import ru.iglo.hunt.keys.KeyType;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class HospitalKeyItem extends Item {

    public HospitalKeyItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    // Создание ключа определенного типа
    public static ItemStack createKey(KeyType keyType) {
        return createKey(keyType, false);
    }

    public static ItemStack createKey(KeyType keyType, boolean masterCopy) {
        ItemStack stack = new ItemStack(ItemRegistry.HOSPITAL_KEY_ITEM.get());
        CompoundNBT tag = new CompoundNBT();

        // Основные данные
        tag.putString("KeyType", keyType.getId());
        tag.putString("DisplayName", keyType.getDisplayName());
        tag.putInt("AccessLevel", keyType.getAccessLevel());
        tag.putString("Description", keyType.getDescription());
        tag.putBoolean("MasterCopy", masterCopy);
        tag.putLong("IssueDate", System.currentTimeMillis());
        
        // УНИКАЛЬНЫЕ ДАННЫЕ - Каждый ключ имеет свой UUID
        UUID uniqueId = UUID.randomUUID();
        tag.putString("UniqueId", uniqueId.toString());
        tag.putString("SerialNumber", generateSerialNumber(keyType, uniqueId));

        // Дополнительные NBT данные с уникальностью
        CompoundNBT metaData = new CompoundNBT();
        metaData.putString("Location", getLocationForType(keyType));
        metaData.putBoolean("ElectronicLock", hasElectronicLock(keyType));
        metaData.putInt("UsesRemaining", keyType == KeyType.MASTER_KEY ? -1 : 100);
        metaData.putLong("CreatedAt", System.currentTimeMillis());
        metaData.putString("CreatedBy", "Hunt-System");
        
        // Вычисляем контрольную сумму для дополнительной проверки уникальности
        metaData.putLong("CheckSum", calculateChecksum(keyType, uniqueId));

        tag.put("Metadata", metaData);
        
        // ВАЖНО: Устанавливаем CustomModelData для выбора правильной модели
        // Это работает с JSON predicates в hospital_key.json
        int customModelData = getCustomModelDataForKeyType(keyType);
        tag.putInt("CustomModelData", customModelData);

        stack.setTag(tag);
        return stack;
    }
    
    /**
     * Преобразует тип ключа в CustomModelData для JSON predicate
     * Значения от 1 до 11 (0 зарезервирован для дефолтной модели)
     */
    private static int getCustomModelDataForKeyType(KeyType type) {
        switch(type) {
            case CABINET_1: return 1;
            case CABINET_2: return 2;
            case CABINET_3: return 3;
            case LIBRARY: return 4;
            case SECURITY: return 5;
            case SERVER_ROOM: return 6;
            case BASEMENT: return 7;
            case MORGUE: return 8;
            case CHIEF_DOCTOR: return 9;
            case ELECTRICAL: return 10;
            case MASTER_KEY: return 11;
            default: return 0;
        }
    }
    
    private static String generateSerialNumber(KeyType type, UUID uniqueId) {
        // Используем первые 8 символов UUID для более уникального номера
        String uuidPart = uniqueId.toString().substring(0, 8).toUpperCase();
        return String.format("HOSP-%s-%s",
                type.getId().toUpperCase().replace("KEY_", ""),
                uuidPart
        );
    }
    
    private static long calculateChecksum(KeyType type, UUID uniqueId) {
        // Вычисляем контрольную сумму для проверки подлинности
        long checksum = type.hashCode() * 31;
        checksum += uniqueId.getMostSignificantBits();
        checksum += uniqueId.getLeastSignificantBits();
        return Math.abs(checksum);
    }

    private static String getLocationForType(KeyType type) {
        switch(type) {
            case CABINET_1: return "1 этаж, коридор А";
            case CABINET_2: return "1 этаж, коридор Б";
            case CABINET_3: return "2 этаж, крыло В";
            case LIBRARY: return "3 этаж, западное крыло";
            case SECURITY: return "Центральный пост";
            case SERVER_ROOM: return "Подвал, секция Б";
            case BASEMENT: return "Технический подвал";
            case MORGUE: return "-1 этаж, северное крыло";
            case CHIEF_DOCTOR: return "5 этаж, администрация";
            case ELECTRICAL: return "Подвал, секция А";
            case MASTER_KEY: return "Все помещения";
            default: return "Не указано";
        }
    }

    private static boolean hasElectronicLock(KeyType type) {
        return type == KeyType.SECURITY ||
                type == KeyType.SERVER_ROOM ||
                type == KeyType.CHIEF_DOCTOR ||
                type == KeyType.MASTER_KEY;
    }

    // Получение типа ключа из ItemStack
    public static KeyType getKeyType(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains("KeyType")) {
            return KeyType.fromId(stack.getTag().getString("KeyType"));
        }
        return KeyType.CABINET_1;
    }

    public static int getAccessLevel(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains("AccessLevel")) {
            return stack.getTag().getInt("AccessLevel");
        }
        return 1;
    }

    public static String getSerialNumber(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains("SerialNumber")) {
            return stack.getTag().getString("SerialNumber");
        }
        return "UNKNOWN";
    }

    public static boolean isMasterCopy(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getBoolean("MasterCopy");
    }

    // Получение уникального ID ключа
    public static String getUniqueId(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains("UniqueId")) {
            return stack.getTag().getString("UniqueId");
        }
        return "UNKNOWN";
    }

    // Проверка уникальности ключа по UUID
    public static boolean verifyKeyUniqueness(ItemStack stack) {
        if (!stack.hasTag()) return false;
        CompoundNBT tag = stack.getTag();
        
        // Проверяем наличие уникальных идентификаторов
        if (!tag.contains("UniqueId") || !tag.contains("SerialNumber")) {
            return false;
        }
        
        // Проверяем контрольную сумму
        if (tag.contains("Metadata")) {
            CompoundNBT meta = tag.getCompound("Metadata");
            if (meta.contains("CheckSum")) {
                try {
                    UUID uniqueId = UUID.fromString(tag.getString("UniqueId"));
                    KeyType keyType = getKeyType(stack);
                    long expectedChecksum = calculateChecksum(keyType, uniqueId);
                    long actualChecksum = meta.getLong("CheckSum");
                    return expectedChecksum == actualChecksum;
                } catch (IllegalArgumentException e) {
                    return false;
                }
            }
        }
        
        return true;
    }

    // Получение информации о создании ключа
    public static String getKeyInfo(ItemStack stack) {
        if (!stack.hasTag()) return "Нет информации";
        CompoundNBT tag = stack.getTag();
        
        StringBuilder info = new StringBuilder();
        info.append("Уникальный ID: ").append(getUniqueId(stack)).append("\n");
        info.append("Серийный номер: ").append(getSerialNumber(stack)).append("\n");
        
        if (tag.contains("Metadata")) {
            CompoundNBT meta = tag.getCompound("Metadata");
            if (meta.contains("CreatedAt")) {
                long createdAt = meta.getLong("CreatedAt");
                info.append("Создан: ").append(new java.util.Date(createdAt)).append("\n");
            }
            if (meta.contains("CreatedBy")) {
                info.append("Выдан: ").append(meta.getString("CreatedBy")).append("\n");
            }
        }
        
        return info.toString();
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!world.isClientSide()) {
            KeyType keyType = getKeyType(stack);

            // Проверяем уникальность
            boolean isUnique = verifyKeyUniqueness(stack);
            
            // Сообщение при использовании с уникальной информацией
            IFormattableTextComponent message = new StringTextComponent("")
                    .append(new StringTextComponent("[" + keyType.getDisplayName() + "] ")
                            .withStyle(TextFormatting.GOLD));

            if (!isUnique) {
            } else {
                message.append(new StringTextComponent(" ✓")
                        .withStyle(TextFormatting.GREEN, TextFormatting.BOLD));
            }

            player.displayClientMessage(message, true);

            // Звук ключа (можно добавить позже)
            // world.playSound(null, player.blockPosition(), SoundEvents.ITEM_BREAK,
            //     SoundCategory.PLAYERS, 0.5F, 1.0F);
        }

        return ActionResult.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world,
                                List<ITextComponent> tooltip, ITooltipFlag flag) {
        super.appendHoverText(stack, world, tooltip, flag);

        if (stack.hasTag()) {
            CompoundNBT tag = stack.getTag();

            // Название ключа
            if (tag.contains("DisplayName")) {
                String displayName = tag.getString("DisplayName");
                int accessLevel = tag.getInt("AccessLevel");

                // Цвет в зависимости от уровня доступа
                TextFormatting color = getColorForLevel(accessLevel);
                tooltip.add(new StringTextComponent(color + "✦ " + displayName)
                        .withStyle(TextFormatting.BOLD));
            }

            // Серийный номер
            if (tag.contains("SerialNumber")) {
                tooltip.add(new StringTextComponent("§8Серия: §7" + tag.getString("SerialNumber")));
            }

            // Уровень доступа
            if (tag.contains("AccessLevel")) {
                int level = tag.getInt("AccessLevel");
                String levelText = getAccessLevelText(level);
                tooltip.add(new StringTextComponent("§7Уровень доступа: " + levelText));
            }

            // Описание
            if (tag.contains("Description")) {
                String[] lines = tag.getString("Description").split("\n");
                for (String line : lines) {
                    tooltip.add(new StringTextComponent(line));
                }
            }

            // Метаданные
            if (tag.contains("Metadata")) {
                CompoundNBT meta = tag.getCompound("Metadata");

                if (meta.contains("Location")) {
                    tooltip.add(new StringTextComponent("§7Локация: §f" + meta.getString("Location")));
                }

                if (meta.contains("ElectronicLock")) {
                    if (meta.getBoolean("ElectronicLock")) {
                        tooltip.add(new StringTextComponent("§9[Электронный замок]"));
                    }
                }

                if (meta.contains("UsesRemaining")) {
                    int uses = meta.getInt("UsesRemaining");
                    if (uses > 0) {
                        tooltip.add(new StringTextComponent("§7Осталось использований: §e" + uses));
                    } else if (uses == -1) {
                        tooltip.add(new StringTextComponent("§6[Неограниченное использование]"));
                    }
                }
            }

            // Мастер-копия
            if (tag.getBoolean("MasterCopy")) {
                tooltip.add(new StringTextComponent("§c⚠ МАСТЕР-КОПИЯ ⚠")
                        .withStyle(TextFormatting.BOLD));
            }
        }
    }

    private TextFormatting getColorForLevel(int level) {
        switch(level) {
            case 1: return TextFormatting.GRAY;
            case 2: return TextFormatting.GREEN;
            case 3: return TextFormatting.BLUE;
            case 4: return TextFormatting.DARK_PURPLE;
            case 5: return TextFormatting.GOLD;
            default: return TextFormatting.RED;
        }
    }

    private String getAccessLevelText(int level) {
        String[] levels = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
        if (level >= 1 && level <= levels.length) {
            return "§l" + levels[level - 1];
        }
        return "MAX";
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        // Мерцание для мастер-копий и высокого уровня доступа
        if (stack.hasTag()) {
            int level = stack.getTag().getInt("AccessLevel");
            return level >= 4 || stack.getTag().getBoolean("MasterCopy");
        }
        return false;
    }

    @Override
    public ITextComponent getName(ItemStack stack) {
        // Динамическое имя в зависимости от NBT
        if (stack.hasTag() && stack.getTag().contains("DisplayName")) {
            return new StringTextComponent(stack.getTag().getString("DisplayName"));
        }
        return super.getName(stack);
    }
}