package com.baisylia.modestmagic.block.entity.custom;

import com.baisylia.modestmagic.block.entity.ModBlockEntities;
import com.baisylia.modestmagic.recipe.EnchantingRecipe;
import com.baisylia.modestmagic.recipe.ModRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class EnchantingBlockEntity extends PedestalBlockEntity {

    public EnchantingBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ENCHANTING_BLOCK_ENTITY.get(), pos, state);
    }

    private static final int PEDESTAL_RANGE = 3;

    public boolean tryCraft() {
        if (level == null || level.isClientSide)
            return false;

        List<PedestalBlockEntity> pedestals = new ArrayList<>();
        List<ItemStack> items = new ArrayList<>();

        // Scan Pedestals
        for (BlockPos pos : BlockPos.betweenClosed(worldPosition.offset(-PEDESTAL_RANGE, -PEDESTAL_RANGE, -PEDESTAL_RANGE),
                worldPosition.offset(PEDESTAL_RANGE, PEDESTAL_RANGE, PEDESTAL_RANGE))) {
            BlockEntity be = level.getBlockEntity(pos);

            if (be instanceof PedestalBlockEntity pedestal && be != this) {
                ItemStack stack = pedestal.getItem();
                if (!stack.isEmpty()) {
                    pedestals.add(pedestal);
                    items.add(stack);
                }
            }
        }

        if (items.isEmpty())
            return false;

        for (EnchantingRecipe recipe : level.getRecipeManager().getAllRecipesFor(ModRecipes.ENCHANTING_TYPE.get())) {
            if (recipe.matches(this.getItem(), items)) {

                // Do Thingy
                this.setItem(recipe.getResult());
                for (PedestalBlockEntity pedestal : pedestals)
                    pedestal.clear();
                setChanged();
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);

                if (level instanceof ServerLevel serverLevel) {

                    // Sound
                    serverLevel.playSound(
                            null,
                            worldPosition,
                            net.minecraft.sounds.SoundEvents.ENCHANTMENT_TABLE_USE,
                            net.minecraft.sounds.SoundSource.BLOCKS,
                            1.0f,
                            1.0f
                    );

                    // Particles
                    serverLevel.sendParticles(
                            ParticleTypes.ENCHANT,
                            worldPosition.getX() + 0.5,
                            worldPosition.getY() + 1,
                            worldPosition.getZ() + 0.5,
                            40,
                            0.5,0.5,0.5,
                            0.01
                    );
                    serverLevel.sendParticles(
                            ParticleTypes.SOUL_FIRE_FLAME,
                            worldPosition.getX() + 0.5,
                            worldPosition.getY() + 1.0,
                            worldPosition.getZ() + 0.5,
                            10,
                            0.1, 0.1, 0.1,
                            0.01
                    );

                    // Pedestal Particles
                    for (PedestalBlockEntity pedestal : pedestals) {
                        BlockPos pPos = pedestal.getBlockPos();
                        serverLevel.sendParticles(
                                ParticleTypes.ENCHANT,
                                pPos.getX() + 0.5,
                                pPos.getY() + 1,
                                pPos.getZ() + 0.5,
                                20,
                                0.3,0.3,0.3,
                                0.1
                        );
                        serverLevel.sendParticles(
                                ParticleTypes.SOUL_FIRE_FLAME,
                                pPos.getX() + 0.5,
                                pPos.getY() + 1.0,
                                pPos.getZ() + 0.5,
                                10,
                                0.1, 0.1, 0.1,
                                0.01
                        );
                    }
                }
                return true;
            }
        }
        return false;
    }
}
