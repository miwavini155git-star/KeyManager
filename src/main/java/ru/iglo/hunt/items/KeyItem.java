package ru.iglo.hunt.items;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemUseContext;

import javax.annotation.Nullable;
import java.util.List;

public class KeyItem extends Item {

    public KeyItem(Properties properties) {
        super(properties.stacksTo(1)); // Только 1 в стаке
    }

    // Создание ключа с NBT данными
    public static ItemStack createKey(String keyId, String textureName, int level, boolean special) {
        ItemStack stack = new ItemStack(ItemRegistry.KEY_ITEM.get());
        CompoundNBT tag = new CompoundNBT();

        tag.putString("KeyId", keyId);
        tag.putString("TextureName", textureName);
        tag.putInt("KeyLevel", level);
        tag.putBoolean("Special", special);
        tag.putString("DisplayName", "Ключ " + keyId);

        stack.setTag(tag);
        return stack;
    }

    public static ItemStack createKey(String keyId) {
        return createKey(keyId, "key_default.png", 1, false);
    }

    // Получение данных из NBT
    public static String getKeyId(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains("KeyId")) {
            return stack.getTag().getString("KeyId");
        }
        return "unknown";
    }

    public static int getKeyLevel(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains("KeyLevel")) {
            return stack.getTag().getInt("KeyLevel");
        }
        return 1;
    }

    public static String getTextureName(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains("TextureName")) {
            return stack.getTag().getString("TextureName");
        }
        return "key_default.png";
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!world.isClientSide()) {
            // Можно добавить эффект при использовании в воздухе
            player.displayClientMessage(
                    new StringTextComponent("Это ключ: " + getKeyId(stack)),
                    true
            );
        }

        return ActionResult.success(stack);
    }

    @Override
    public ActionResultType useOn(ItemUseContext context) {
        // Запрещаем размещение ключа как блока
        if (!context.getLevel().isClientSide()) {
            context.getPlayer().displayClientMessage(
                    new StringTextComponent("Ключ нельзя размещать!"),
                    true
            );
        }
        return ActionResultType.FAIL;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world,
                                List<ITextComponent> tooltip, ITooltipFlag flag) {
        super.appendHoverText(stack, world, tooltip, flag);

        if (stack.hasTag()) {
            CompoundNBT tag = stack.getTag();

            if (tag.contains("DisplayName")) {
                tooltip.add(new StringTextComponent("§e" + tag.getString("DisplayName")));
            }

            if (tag.contains("KeyId")) {
                tooltip.add(new StringTextComponent("§7ID: §f" + tag.getString("KeyId")));
            }

            if (tag.contains("KeyLevel")) {
                tooltip.add(new StringTextComponent("§7Уровень: §a" + tag.getInt("KeyLevel")));
            }

            if (tag.contains("Special") && tag.getBoolean("Special")) {
                tooltip.add(new StringTextComponent("§6★ Особый ключ"));
            }
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        // Мерцание если ключ особый
        return stack.hasTag() && stack.getTag().getBoolean("Special");
    }
}