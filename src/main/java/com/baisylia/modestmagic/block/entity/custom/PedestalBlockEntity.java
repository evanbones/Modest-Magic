package com.baisylia.modestmagic.block.entity.custom;

import com.baisylia.modestmagic.block.custom.AltarBlock;
import com.baisylia.modestmagic.block.custom.PedestalBlock;
import com.baisylia.modestmagic.block.entity.ModBlockEntities;
import com.baisylia.modestmagic.client.ModSounds;
import com.baisylia.modestmagic.config.ModestMagicConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.List;

public class PedestalBlockEntity extends BlockEntity implements WorldlyContainer {

    private final ItemStackHandler itemHandler = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();

            if (level != null && !level.isClientSide) {
                BlockState state = getBlockState();

                level.setBlock(worldPosition, state.setValue(PedestalBlock.HAS_ITEM, !getStackInSlot(0).isEmpty()), 3);

                level.sendBlockUpdated(worldPosition, state, state, 3);
            }
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        protected int getStackLimit(int slot, ItemStack stack) {
            return 1;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (stack.isEmpty()) return ItemStack.EMPTY;

            if (!getStackInSlot(slot).isEmpty()) {
                return stack;
            }
            ItemStack single = stack.copy();
            single.setCount(1);

            if (!simulate) {
                setStackInSlot(slot, single);
            }

            ItemStack remainder = stack.copy();
            remainder.shrink(1);
            return remainder;
        }
    };
    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    public PedestalBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.PEDESTAL_BLOCK_ENTITY.get(), pos, state);
    }

    protected PedestalBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, PedestalBlockEntity blockEntity) {
        if (level.isClientSide) return;
        if (!blockEntity.isEmpty()) return;

        if (state.hasProperty(PedestalBlock.AXIS) && state.getValue(PedestalBlock.AXIS) != Direction.Axis.Y) return;
        if (state.hasProperty(PedestalBlock.TOP) && !state.getValue(PedestalBlock.TOP)) return;

        if (ModestMagicConfig.THROW_ITEMS_ON_PEDESTALS.get()) {
            AABB pickupArea = new AABB(pos.getX(), pos.getY() + 1.0, pos.getZ(),
                    pos.getX() + 1.0, pos.getY() + 1.5, pos.getZ() + 1.0);

            List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, pickupArea);

            for (ItemEntity itemEntity : items) {
                ItemStack stack = itemEntity.getItem();
                if (!stack.isEmpty() && itemEntity.isAlive()) {
                    blockEntity.setItem(stack.split(1));

                    if (stack.isEmpty()) {
                        itemEntity.discard();
                    } else {
                        itemEntity.setItem(stack);
                    }

                    SoundEvent sound = state.getBlock() instanceof AltarBlock
                            ? ModSounds.ADD_ITEM_ALTAR.get()
                            : ModSounds.ADD_ITEM_PEDESTAL.get();
                    level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0f, 1.0f);

                    break;
                }
            }
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    public ItemStack getItem() {
        ItemStack stack = itemHandler.getStackInSlot(0);

        if (!stack.isEmpty() && stack.getCount() > 1) {
            ItemStack copy = stack.copy();
            copy.setCount(1);
            return copy;
        }

        return stack;
    }

    public void setItem(ItemStack stack) {
        if (!stack.isEmpty()) {
            stack = stack.copy();
            stack.setCount(1);
        }
        itemHandler.setStackInSlot(0, stack);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    public void clear() {
        itemHandler.setStackInSlot(0, ItemStack.EMPTY);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        tag.put("inventory", itemHandler.serializeNBT());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        itemHandler.deserializeNBT(tag.getCompound("inventory"));

        ItemStack stack = itemHandler.getStackInSlot(0);
        if (!stack.isEmpty() && stack.getCount() > 1) {
            stack.setCount(1);
            itemHandler.setStackInSlot(0, stack);
        }
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return new int[]{0};
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction direction) {
        return getItem().isEmpty();
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return true;
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return itemHandler.getStackInSlot(0).isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        ItemStack stack = itemHandler.getStackInSlot(slot);

        if (!stack.isEmpty() && stack.getCount() > 1) {
            ItemStack copy = stack.copy();
            copy.setCount(1);
            return copy;
        }

        return stack;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return itemHandler.extractItem(slot, 1, false);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return itemHandler.extractItem(slot, 1, false);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (!stack.isEmpty()) {
            stack = stack.copy();
            stack.setCount(1);
        }
        itemHandler.setStackInSlot(slot, stack);
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.level.getBlockEntity(this.worldPosition) != this) return false;
        return player.distanceToSqr(
                worldPosition.getX() + 0.5,
                worldPosition.getY() + 0.5,
                worldPosition.getZ() + 0.5
        ) <= 64.0D;
    }

    @Override
    public void clearContent() {
        itemHandler.setStackInSlot(0, ItemStack.EMPTY);
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