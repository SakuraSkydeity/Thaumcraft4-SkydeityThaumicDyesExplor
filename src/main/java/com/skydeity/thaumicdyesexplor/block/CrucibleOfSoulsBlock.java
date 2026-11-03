package com.skydeity.thaumicdyesexplor.block;

import com.mojang.serialization.MapCodec;
import com.skydeity.thaumicdyesexplor.registry.ModBlocks;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 灵魂坩埚（源模组 {@code BlockCrucibleSouls}）：把附近生物抽血致死，
 * 把「死亡 / 灵魂」要素收进自己，可从任意面用管道抽走。
 *
 * <p>外观是 1.7.10 的坩埚贴图（暗紫带花纹），六面同图，直接 cube 模型。</p>
 */
public class CrucibleOfSoulsBlock extends BaseEntityBlock {

    public static final MapCodec<CrucibleOfSoulsBlock> CODEC = simpleCodec(CrucibleOfSoulsBlock::new);

    public CrucibleOfSoulsBlock(Properties properties) {
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
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CrucibleOfSoulsBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                 BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlocks.CRUCIBLE_OF_SOULS_BE.get(), CrucibleOfSoulsBlockEntity::tick);
    }
}
