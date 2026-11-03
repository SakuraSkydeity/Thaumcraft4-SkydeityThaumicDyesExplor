package com.skydeity.thaumicdyesexplor.block;

import com.mojang.serialization.MapCodec;
import com.skydeity.thaumicdyesexplor.registry.ModBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 湮灭罐（源模组 {@code BlockTrashJar} / Oblivion Jar）：吞掉一切进入它的要素。
 *
 * <p>源模组继承 {@code TileJarFillable}，每 5 tick 把存进来的要素扣掉 1 个 ——
 * 也就是「有吸力的要素垃圾桶」。这里用前置公开的 {@code EssentiaTransport} 能力实现：
 * {@code addEssentia} 直接返回传入的数量（等于全部收下并销毁），{@code takeEssentia} 恒为 0。</p>
 *
 * <p>吸力（{@code minimumSuction = 1}）刻意给得最低：源模组的研究文本说它「只收别处去不了的要素」，
 * 免得它把管道网络里的要素全抢走。</p>
 */
public class OblivionJarBlock extends BaseEntityBlock {

    public static final MapCodec<OblivionJarBlock> CODEC = simpleCodec(OblivionJarBlock::new);

    private static final VoxelShape SHAPE = box(3.0, 0.0, 3.0, 13.0, 14.0, 13.0);

    public OblivionJarBlock(Properties properties) {
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
        return new OblivionJarBlockEntity(pos, state);
    }
}
