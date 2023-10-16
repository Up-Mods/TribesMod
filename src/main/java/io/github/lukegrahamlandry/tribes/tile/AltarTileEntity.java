package io.github.lukegrahamlandry.tribes.tile;

import io.github.lukegrahamlandry.tribes.TribesMain;
import io.github.lukegrahamlandry.tribes.init.TileEntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class AltarTileEntity extends BlockEntity {
    public AltarTileEntity(BlockPos pos, BlockState state) {
        super(TileEntityInit.ALTAR.get(), pos, state);
    }

    String bannerKey;

    @Override
    public void setChanged() {
        super.setChanged();
        TribesMain.LOGGER.debug("display: " + this.bannerKey);
        this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
    }

    // use from block
    public void setBannerKey(String key) {
        this.bannerKey = key;
        this.setChanged();
    }

    // for render
    public String getBannerKey() {
        return this.bannerKey;
    }

    // saving data
    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.bannerKey = tag.contains("banner") ? tag.getString("banner") : null;
    }

    @Override
    protected void saveAdditional(CompoundTag p_187471_) {
        super.saveAdditional(p_187471_);
        if (this.bannerKey != null) p_187471_.putString("banner", this.bannerKey);
    }


    // block update
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        CompoundTag nbt = new CompoundTag();
        this.saveAdditional(nbt);

        //return new ClientboundBlockEntityDataPacket(this.worldPosition, 1, nbt);
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        this.load(pkt.getTag());
    }

    // chunk load
    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }
}
