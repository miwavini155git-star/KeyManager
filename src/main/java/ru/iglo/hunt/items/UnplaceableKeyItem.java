package ru.iglo.hunt.items;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.item.ItemUseContext;
import net.minecraft.world.World;

public class UnplaceableKeyItem extends Item {

    public UnplaceableKeyItem(Properties properties) {
        super(properties
                .setNoRepair()           // Нельзя починить
                .defaultDurability(0)    // Нет прочности
        );
    }

    @Override
    public ActionResultType useOn(ItemUseContext context) {
        // Полностью запрещаем использование на блоках
        if (!context.getLevel().isClientSide) {
            // Можно добавить звук отказа
            // context.getLevel().playSound(null, context.getPlayer().blockPosition(),
            //     SoundEvents.ITEM_BREAK, SoundCategory.PLAYERS, 0.5F, 0.8F);
        }
        return ActionResultType.FAIL;
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!world.isClientSide) {
            // Можно добавить какое-то действие при использовании в воздухе
            // Например, показать сообщение
            // player.displayClientMessage(new TranslationTextComponent("message.hunt.key_cannot_place"), true);

            // Или сыграть звук
            // world.playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_BASS,
            //     SoundCategory.PLAYERS, 1.0F, 1.0F);
        }

        // Возвращаем success, но без изменения предмета
        return ActionResult.success(itemstack);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        // Добавить мерцание (как у зачарованных предметов)
        return true;
    }
}