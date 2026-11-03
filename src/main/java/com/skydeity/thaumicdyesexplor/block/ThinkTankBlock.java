package com.skydeity.thaumicdyesexplor.block;

import com.mojang.serialization.MapCodec;
import com.skydeity.thaumicdyesexplor.registry.ModBlocks;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 思考罐（源模组 {@code BlockThinkTank}）：喂书换知识碎片。
 *
 * <p>交互：手持书 / 附魔书右键放入（一次 1 本，最多 10 本），空手右键取出知识碎片；
 * 空手右键且还没产出时会提示烧到百分之几。
 * 装置必须坐在书架上才会工作 —— 这条在方块实体里判断。</p>
 */
public class ThinkTankBlock extends BaseEntityBlock {

    public static final MapCodec<ThinkTankBlock> CODEC = simpleCodec(ThinkTankBlock::new);

    private static final VoxelShape SHAPE = box(3.0, 0.0, 3.0, 13.0, 14.0, 13.0);

    public ThinkTankBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ThinkTankBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                 BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlocks.THINK_TANK_BE.get(), ThinkTankBlockEntity::tick);
    }

    // ============================================================================================
    // 交互
    // ============================================================================================
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, net.minecraft.world.InteractionHand hand,
                                              BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof ThinkTankBlockEntity tank)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }
        // 空手要交给 useWithoutItem 去「取产物」；这里若返回 CONSUME，取产物的分支永远进不来
        if (stack.isEmpty()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        int inserted = tank.insertBook(stack);
        if (inserted == 0) {
            if (player instanceof ServerPlayer serverPlayer) {
                boolean book = stack.is(net.minecraft.world.item.Items.BOOK)
                        || stack.is(net.minecraft.world.item.Items.ENCHANTED_BOOK);
                String key = !book ? "msg.thaumicdyesexplor.think_tank.need_book"
                        : tank.isFull() ? "msg.thaumicdyesexplor.think_tank.full"
                        : "msg.thaumicdyesexplor.think_tank.need_book";
                serverPlayer.displayClientMessage(Component.translatable(key), true);
            }
            return ItemInteractionResult.CONSUME;
        }
        stack.shrink(inserted);
        if (player instanceof ServerPlayer serverPlayer) {
            if (!tank.hasBookshelfForMessage()) {
                serverPlayer.displayClientMessage(
                        Component.translatable("msg.thaumicdyesexplor.think_tank.no_shelf"), false);
            } else {
                serverPlayer.displayClientMessage(
                        Component.translatable("msg.thaumicdyesexplor.think_tank.inserted",
                                tank.storedCount(), ThinkTankBlockEntity.MAX_BOOKS), true);
            }
        }
        return ItemInteractionResult.CONSUME;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof ThinkTankBlockEntity tank)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!tank.hasOutput()) {
            if (player instanceof ServerPlayer serverPlayer) {
                if (tank.storedCount() > 0 && tank.hasBookshelfForMessage()) {
                    serverPlayer.displayClientMessage(
                            Component.translatable("msg.thaumicdyesexplor.think_tank.progress",
                                    tank.progressPercent(), tank.storedCount()), false);
                } else if (tank.storedCount() > 0) {
                    serverPlayer.displayClientMessage(
                            Component.translatable("msg.thaumicdyesexplor.think_tank.no_shelf"), false);
                } else {
                    serverPlayer.displayClientMessage(
                            Component.translatable("msg.thaumicdyesexplor.think_tank.empty"), true);
                }
            }
            return InteractionResult.CONSUME;
        }
        ItemStack out = tank.takeOutput();
        if (!player.getInventory().add(out)) {
            player.drop(out, false);
        }
        return InteractionResult.SUCCESS;
    }

    /** 破坏时把里面的书和产物都吐出来，免得吞物品。 */
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof ThinkTankBlockEntity tank) {
            ItemStack stored = tank.storedStack();
            if (!stored.isEmpty()) {
                popResource(level, pos, stored);
            }
            if (tank.hasOutput()) {
                popResource(level, pos, tank.takeOutput());
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
