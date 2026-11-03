package com.skydeity.thaumicdyesexplor.block;

import com.mojang.serialization.MapCodec;
import com.skydeity.thaumicdyesexplor.registry.ModBlocks;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 永满之瓮（源模组 {@code BlockEverfullUrn} + {@code TileEntityEverfullUrn}）。
 *
 * <p>源模组是一个假流体罐（{@code getFluidAmount = 9999}），因此能无限供水。
 * 这里做成「无限水源 + 自动供水」：</p>
 * <ul>
 *   <li><b>自动</b>（方块实体每 20 tick 一次，六个面都推）：给带流体能力的邻居灌 1000 mB 水、
 *       补满炼药锅、给可含水方块加水；开了「注水模式」时还会往相邻空气放水源；</li>
 *   <li><b>手动</b>：空桶 → 水桶、玻璃瓶 → 水瓶，右键还能立刻补满上方炼药锅、
 *       给 4 格内着火的生物灭火；</li>
 *   <li><b>潜行 + 右键</b>：切换注水模式（默认关，开了会淹地）。</li>
 * </ul>
 *
 * <p>模型是「底 14×7 / 颈 8×4 / 口沿 12×5」的三段陶瓮，见
 * {@code models/block/everfull_urn.json}。</p>
 */
public class EverfullUrnBlock extends BaseEntityBlock {

    public static final MapCodec<EverfullUrnBlock> CODEC = simpleCodec(EverfullUrnBlock::new);

    /** 瓮身是 2~14 的柱体，顶部略微外翻。 */
    private static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);

    public EverfullUrnBlock(Properties properties) {
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
        return new EverfullUrnBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                 BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlocks.EVERFULL_URN_BE.get(), EverfullUrnBlockEntity::tick);
    }

    // ============================================================================================
    // 取水
    // ============================================================================================
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, net.minecraft.world.InteractionHand hand,
                                              BlockHitResult hit) {
        if (stack.is(Items.BUCKET)) {
            if (!level.isClientSide) {
                ItemStack filled = new ItemStack(Items.WATER_BUCKET);
                player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, filled));
                level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        if (stack.is(Items.GLASS_BOTTLE)) {
            if (!level.isClientSide) {
                ItemStack filled = new ItemStack(Items.POTION);
                player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, filled));
                level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof EverfullUrnBlockEntity urn)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        // 潜行 + 右键：切换注水模式
        if (player.isShiftKeyDown()) {
            urn.toggleFloodMode();
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.displayClientMessage(Component.translatable(urn.floodMode()
                        ? "msg.thaumicdyesexplor.everfull_urn.flood_on"
                        : "msg.thaumicdyesexplor.everfull_urn.flood_off"), true);
            }
            return InteractionResult.SUCCESS;
        }

        boolean did = false;
        // 1) 立刻补满上方炼药锅（自动循环也会做，这里只是给个即时反馈）
        BlockPos above = pos.above();
        BlockState aboveState = level.getBlockState(above);
        if (aboveState.is(Blocks.WATER_CAULDRON)
                && aboveState.getValue(LayeredCauldronBlock.LEVEL) < LayeredCauldronBlock.MAX_FILL_LEVEL) {
            level.setBlock(above, aboveState.setValue(LayeredCauldronBlock.LEVEL,
                    LayeredCauldronBlock.MAX_FILL_LEVEL), Block.UPDATE_ALL);
            did = true;
        } else if (aboveState.is(Blocks.CAULDRON)) {
            level.setBlock(above, Blocks.WATER_CAULDRON.defaultBlockState()
                    .setValue(LayeredCauldronBlock.LEVEL, LayeredCauldronBlock.MAX_FILL_LEVEL), Block.UPDATE_ALL);
            did = true;
        }
        // 2) 给周围着火的生物灭火
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, new AABB(pos).inflate(4.0D))) {
            if (entity.isOnFire()) {
                entity.clearFire();
                did = true;
            }
        }
        if (did) {
            level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.6F, 1.2F);
        }
        return InteractionResult.SUCCESS;
    }
}
