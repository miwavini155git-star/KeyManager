package ru.iglo.hunt.tileentity;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import ru.iglo.hunt.Hunt;
import ru.iglo.hunt.keys.KeyType;

import javax.annotation.Nullable;

public class HospitalKeyTileEntity extends TileEntity {

    private CompoundNBT keyNBT = new CompoundNBT();

    public HospitalKeyTileEntity() {
        // Используем тип из регистрации
        super(TileEntityRegistry.HOSPITAL_KEY_TE.get());
    }

    public CompoundNBT getKeyNBT() {
        return keyNBT;
    }

    public void setKeyNBT(CompoundNBT nbt) {
        this.keyNBT = nbt;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    // Получение типа ключа
    public KeyType getKeyType() {
        if (keyNBT.contains("KeyType")) {
            return KeyType.fromId(keyNBT.getString("KeyType"));
        }
        return KeyType.CABINET_1;
    }

    // Получение текстуры
    public ResourceLocation getTexture() {
        KeyType type = getKeyType();
        return new ResourceLocation(Hunt.MODID,
                "textures/keys/hospital/" + type.getTextureName());
    }

    // Получение цвета свечения
    public int getGlowColor() {
        return getKeyType().getColor();
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        super.save(compound);
        compound.put("HospitalKeyData", keyNBT);
        return compound;
    }

    @Override
    public void load(BlockState state, CompoundNBT compound) {
        super.load(state, compound);
        if (compound.contains("HospitalKeyData")) {
            keyNBT = compound.getCompound("HospitalKeyData");
        }
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(worldPosition, 0, getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return save(new CompoundNBT());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        load(getBlockState(), pkt.getTag());
    }
}