package com.skydeity.thaumicdyesexplor.block;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;
import com.skydeity.thaumicdyesexplor.registry.ModBlocks;

import dev.tc4port.thaumcraft.api.wand.WandCarrierItem;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * 中洲精髓罐：普通右键把 vis 充进手里的法杖，潜行右键把法杖的 vis 压进罐子。
 * 容量倍率由子类 / 工厂参数决定（压缩 ×2、双重压缩 ×4、群星罐 ×8）。
 */
public class MeEssentiaJarBlock extends BaseEntityBlock {

    private final int capacityMultiplier;

    public static final com.mojang.serialization.MapCodec<MeEssentiaJarBlock> CODEC =
            com.mojang.serialization.codecs.RecordCodecBuilder.mapCodec(instance -> instance.group(
                    com.mojang.serialization.Codec.INT.fieldOf("capacity_multiplier")
                            .forGetter(MeEssentiaJarBlock::capacityMultiplier),
                    propertiesCodec()).apply(instance, MeEssentiaJarBlock::new));

    public MeEssentiaJarBlock(int capacityMultiplier, Properties properties) {
        super(properties);
        this.capacityMultiplier = capacityMultiplier;
    }

    public int capacityMultiplier() {
        return this.capacityMultiplier;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    /**
     * 罐子的碰撞 / 选中箱跟着模型走（模型是 3~13 的罐身 + 14 的罐盖），
     * 否则看起来是玻璃罐、实际按一整个方块算，站上去会"悬空"。
     */
    private static final net.minecraft.world.phys.shapes.VoxelShape SHAPE =
            net.minecraft.world.level.block.Block.box(3.0, 0.0, 3.0, 13.0, 14.0, 13.0);

    @Override
    protected net.minecraft.world.phys.shapes.VoxelShape getShape(BlockState state,
                                                                  net.minecraft.world.level.BlockGetter level,
                                                                  BlockPos pos,
                                                                  net.minecraft.world.phys.shapes.CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MeEssentiaJarBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        return null; // 无每 tick 逻辑
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(stack.getItem() instanceof WandCarrierItem) || !(player instanceof ServerPlayer serverPlayer)) {
            return super.useItemOn(stack, state, level, pos, player, hand, hit);
        }
        if (level.getBlockEntity(pos) instanceof MeEssentiaJarBlockEntity jar) {
            if (player.isShiftKeyDown()) {
                jar.pullFromWand(serverPlayer, stack);
            } else {
                jar.pushToWand(serverPlayer, stack);
            }
        }
        return ItemInteractionResult.CONSUME;
    }
}
