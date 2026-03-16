package com.baisylia.modestmagic.block.entity.custom;

import com.baisylia.modestmagic.block.custom.PedestalBlock;
import com.baisylia.modestmagic.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;

public class PedestalBlockEntity extends BlockEntity {

    private ItemStack item = ItemStack.EMPTY;

    public PedestalBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.PEDESTAL_BLOCK_ENTITY.get(), pos, state);
    }

    protected PedestalBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public ItemStack getItem() {
        return item;
    }

    public void setItem(ItemStack stack) {
        this.item = stack;
        setChanged();

        if(level != null) {
            BlockState state = getBlockState();
            level.setBlockAndUpdate(worldPosition, state.setValue(PedestalBlock.HAS_ITEM, true));
        }
    }

    public void clear() {
        this.item = ItemStack.EMPTY;
        setChanged();

        if(level != null) {
            BlockState state = getBlockState();
            level.setBlockAndUpdate(worldPosition, state.setValue(PedestalBlock.HAS_ITEM, false));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        tag.put("item", item.save(new CompoundTag()));
    }

    @Override
    public void load(CompoundTag tag) {
        item = ItemStack.of(tag.getCompound("item"));
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition).inflate(2);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }
}