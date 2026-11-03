package com.skydeity.thaumicdyesexplor.block;

import com.skydeity.thaumicdyesexplor.registry.ModBlocks;

import dev.tc4port.thaumcraft.api.aspect.AspectId;
import dev.tc4port.thaumcraft.api.essentia.EssentiaTransferMode;
import dev.tc4port.thaumcraft.api.essentia.EssentiaTransport;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 湮灭罐的方块实体：实现 {@link EssentiaTransport}，把收到的要素全部销毁。
 *
 * <p>源模组里它继承奥秘罐、把存进来的要素每 5 tick 扣 1；这里直接「入即消失」，
 * 对外表现一样（能接管道、能当溢出终点），但不需要维护库存。</p>
 */
public class OblivionJarBlockEntity extends BlockEntity implements EssentiaTransport {

    public OblivionJarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.OBLIVION_JAR_BE.get(), pos, state);
    }

    // ============================================================================================
    // EssentiaTransport
    // ============================================================================================
    @Override
    public boolean isConnectable(Direction direction) {
        return true;
    }

    @Override
    public boolean canInputFrom(Direction direction) {
        return true;
    }

    @Override
    public boolean canOutputTo(Direction direction) {
        return false;
    }

    @Override
    public AspectId suctionType(Direction direction) {
        return null; // 不挑种类
    }

    /**
     * 吸力。
     *
     * <p>前置的虚空罐是 {@code ALCHEMY_VOID_JAR_SUCTION = 32}；这里必须跟着给同样的值，
     * 否则管道算出的吸力永远压不过别处，要素根本灌不进来（第 34 轮踩过：写了 0，
     * 结果「管子接上了但罐子一直是空的」）。</p>
     */
    @Override
    public int suctionAmount(Direction direction) {
        return 32;
    }

    @Override
    public int takeEssentia(AspectId aspect, int amount, Direction direction, EssentiaTransferMode mode) {
        return 0; // 只能进不能出
    }

    @Override
    public int addEssentia(AspectId aspect, int amount, Direction direction, EssentiaTransferMode mode) {
        // 收下多少就销毁多少
        return amount;
    }

    @Override
    public AspectId essentiaType(Direction direction) {
        return null;
    }

    @Override
    public int essentiaAmount(Direction direction) {
        return 0;
    }

    @Override
    public int minimumSuction() {
        return 0;
    }

    @Override
    public boolean renderExtendedTube() {
        return false;
    }
}
