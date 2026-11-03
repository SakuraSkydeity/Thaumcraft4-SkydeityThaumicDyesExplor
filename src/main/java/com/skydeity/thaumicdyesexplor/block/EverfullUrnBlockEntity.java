package com.skydeity.thaumicdyesexplor.block;

import com.skydeity.thaumicdyesexplor.registry.ModBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

/**
 * 永满之瓮的方块实体：每 20 tick 朝<b>六个面</b>推一次水。
 *
 * <p>依次尝试四件事（每一面、每个周期）：</p>
 * <ol>
 *   <li>邻居带 {@code IFluidHandler} 能力（管道、储罐、别的机器的水箱）→ 灌 1000 mB 水；</li>
 *   <li>邻居是炼药锅（空或只有部分水）→ 补满；</li>
 *   <li>邻居是「可含水」方块（台阶 / 楼梯 / 栅栏 / 墙等）→ 设为含水；</li>
 *   <li>邻居是空气 <b>且</b>开到了「注水模式」→ 直接放一格水源。</li>
 * </ol>
 *
 * <p>注水模式默认关闭：一开就等于在基地里放洪水，只在你明确要铺水时再用
 * （潜行 + 右键切换）。</p>
 */
public class EverfullUrnBlockEntity extends BlockEntity {

    /** 推水周期（tick）。 */
    private static final int INTERVAL = 20;
    /** 每次灌给流体处理器的水量（mB）。 */
    private static final int FLUID_PER_PUSH = 1000;

    private boolean flood;

    public EverfullUrnBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.EVERFULL_URN_BE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, EverfullUrnBlockEntity urn) {
        if (level.isClientSide) {
            return;
        }
        if (level.getGameTime() % INTERVAL != 0) {
            return;
        }
        urn.pushWater();
    }

    private void pushWater() {
        if (this.level == null) {
            return;
        }
        BlockPos origin = this.worldPosition;
        boolean did = false;
        for (Direction direction : Direction.values()) {
            BlockPos target = origin.relative(direction);
            if (!this.level.isLoaded(target)) {
                continue;
            }

            // 1) 流体处理器：管道 / 储罐 / 机器水箱
            IFluidHandler handler = this.level.getCapability(Capabilities.FluidHandler.BLOCK,
                    target, direction.getOpposite());
            if (handler != null) {
                FluidStack water = new FluidStack(Fluids.WATER, FLUID_PER_PUSH);
                if (handler.fill(water, IFluidHandler.FluidAction.EXECUTE) > 0) {
                    did = true;
                }
                continue;
            }

            BlockState neighbor = this.level.getBlockState(target);
            // 2) 炼药锅补满
            if (neighbor.is(Blocks.CAULDRON)) {
                this.level.setBlock(target, Blocks.WATER_CAULDRON.defaultBlockState()
                        .setValue(LayeredCauldronBlock.LEVEL, LayeredCauldronBlock.MAX_FILL_LEVEL), 3);
                did = true;
                continue;
            }
            if (neighbor.is(Blocks.WATER_CAULDRON)
                    && neighbor.getValue(LayeredCauldronBlock.LEVEL) < LayeredCauldronBlock.MAX_FILL_LEVEL) {
                this.level.setBlock(target, neighbor.setValue(LayeredCauldronBlock.LEVEL,
                        LayeredCauldronBlock.MAX_FILL_LEVEL), 3);
                did = true;
                continue;
            }
            // 3) 可含水方块
            if (neighbor.hasProperty(BlockStateProperties.WATERLOGGED)
                    && !neighbor.getValue(BlockStateProperties.WATERLOGGED)) {
                this.level.setBlock(target, neighbor.setValue(BlockStateProperties.WATERLOGGED, true), 3);
                did = true;
                continue;
            }
            // 4) 注水模式：空气直接放水源
            if (this.flood && neighbor.isAir()) {
                this.level.setBlock(target, Blocks.WATER.defaultBlockState(), 3);
                did = true;
            }
        }
        if (did) {
            this.setChanged();
        }
    }

    public boolean floodMode() {
        return this.flood;
    }

    public void toggleFloodMode() {
        this.flood = !this.flood;
        this.setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("Flood", this.flood);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.flood = tag.getBoolean("Flood");
    }
}
