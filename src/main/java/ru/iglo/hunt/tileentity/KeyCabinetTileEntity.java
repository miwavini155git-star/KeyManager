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

public class KeyCabinetTileEntity extends TileEntity {

    private KeyType keyType = KeyType.CABINET_1;
    private boolean isEmpty = false;

    public KeyCabinetTileEntity() {
        super(TileEntityRegistry.KEY_CABINET_TE.get());
    }

    public KeyType getKeyType() {
        return keyType;
    }

    public void setKeyType(KeyType keyType) {
        this.keyType = keyType;
        this.isEmpty = false;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public boolean isEmpty() {
        return isEmpty;
    }

    public void setEmpty(boolean empty) {
        this.isEmpty = empty;
        setChanged();
    }

    // Получение текстуры в зависимости от типа ключа
    public ResourceLocation getTexture() {
        if (isEmpty) {
            return new ResourceLocation(Hunt.MODID, "textures/blocks/key_cabinet_empty.png");
        }

        String textureName;
        switch (keyType) {
            case CABINET_1: textureName = "key_cabinet_1"; break;
            case CABINET_2: textureName = "key_cabinet_2"; break;
            case CABINET_3: textureName = "key_cabinet_3"; break;
            case LIBRARY: textureName = "key_cabinet_library"; break;
            case SECURITY: textureName = "key_cabinet_security"; break;
            default: textureName = "key_cabinet_default";
        }

        return new ResourceLocation(Hunt.MODID, "textures/blocks/" + textureName + ".png");
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        super.save(compound);
        compound.putString("KeyType", keyType.getId());
        compound.putBoolean("IsEmpty", isEmpty);
        return compound;
    }

    @Override
    public void load(BlockState state, CompoundNBT compound) {
        super.load(state, compound);
        if (compound.contains("KeyType")) {
            keyType = KeyType.fromId(compound.getString("KeyType"));
        }
        if (compound.contains("IsEmpty")) {
            isEmpty = compound.getBoolean("IsEmpty");
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