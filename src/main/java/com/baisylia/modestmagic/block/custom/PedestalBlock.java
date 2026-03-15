package com.baisylia.modestmagic.block.custom;

import com.baisylia.modestmagic.block.entity.ModBlockEntities;
import com.baisylia.modestmagic.block.entity.custom.PedestalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class PedestalBlock extends BaseEntityBlock {

    public PedestalBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new PedestalBlockEntity(pPos, pState);
    }

    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        VoxelShape shape1 = Block.box(0, 0, 0, 16, 3, 16);
        VoxelShape shape2 = Block.box(3, 3, 3, 13, 13, 13);
        VoxelShape shape3 = Block.box(0, 13, 0, 16, 16, 16);

        return Shapes.or(shape1, shape2, shape3);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if(level.isClientSide) return InteractionResult.SUCCESS;

        PedestalBlockEntity pedestal = (PedestalBlockEntity) level.getBlockEntity(pos);
        ItemStack held = player.getItemInHand(hand);

        if(pedestal.getItem().isEmpty()) {
            if(!held.isEmpty()) {
                pedestal.setItem(held.split(1));
                return InteractionResult.CONSUME;
            }
        } else {
            ItemStack stack = pedestal.getItem();
            pedestal.clear();

            if(!player.addItem(stack)) {
                player.drop(stack, false);
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}
