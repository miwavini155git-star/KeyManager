package ru.iglo.hunt.tileentity;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import ru.iglo.hunt.Hunt;

import javax.annotation.Nullable;

public class KeyTileEntity extends TileEntity {

    private CompoundNBT keyNBT = new CompoundNBT();

    public KeyTileEntity() {
        super(TileEntityRegistry.KEY_TE.get());
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

    // Получение имени текстуры из NBT
    public ResourceLocation getTexture() {
        if (keyNBT.contains("TextureName")) {
            return new ResourceLocation(Hunt.MODID,
                    "textures/keys/" + keyNBT.getString("TextureName"));
        }
        return new ResourceLocation(Hunt.MODID, "textures/keys/key_default.png");
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        super.save(compound);
        compound.put("KeyData", keyNBT);
        return compound;
    }

    @Override
    public void load(BlockState state, CompoundNBT compound) {
        super.load(state, compound);
        if (compound.contains("KeyData")) {
            keyNBT = compound.getCompound("KeyData");
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