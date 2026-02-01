package ru.iglo.hunt.tileentity;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

/**
 * TileEntity для табличек с поддержкой сохранения состояния
 */
public class TablichkaTileEntity extends TileEntity {
    private boolean lying = false;

    public TablichkaTileEntity() {
        super(TileEntityRegistry.TABLICHKA_TILE_ENTITY.get());
    }

    /**
     * Получить статус лежачести
     */
    public boolean isLying() {
        return lying;
    }

    /**
     * Установить статус лежачести
     */
    public void setLying(boolean lying) {
        this.lying = lying;
    }

    /**
     * Сохранение данных при разгрузке чанка
     */
    @Override
    public CompoundNBT save(CompoundNBT compound) {
        super.save(compound);
        compound.putBoolean("lying", lying);
        return compound;
    }

    /**
     * Загрузка данных при загрузке чанка
     */
    @Override
    public void load(BlockState state, CompoundNBT compound) {
        super.load(state, compound);
        lying = compound.getBoolean("lying");
    }

    /**
     * Обновление по сети (для синхронизации с клиентом)
     */
    @Override
    public CompoundNBT getUpdateTag() {
        CompoundNBT compound = super.getUpdateTag();
        compound.putBoolean("lying", lying);
        return compound;
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT tag) {
        super.handleUpdateTag(state, tag);
        lying = tag.getBoolean("lying");
    }
}
