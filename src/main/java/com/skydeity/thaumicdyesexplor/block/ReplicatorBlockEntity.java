package com.skydeity.thaumicdyesexplor.block;

import com.skydeity.thaumicdyesexplor.registry.ModBlocks;

import dev.tc4port.thaumcraft.api.aspect.AspectId;
import dev.tc4port.thaumcraft.api.essentia.EssentiaApi;
import dev.tc4port.thaumcraft.api.essentia.EssentiaSearch;
import dev.tc4port.thaumcraft.api.essentia.EssentiaTransferMode;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 神秘复制机的方块实体（源模组 {@code TileEntityReplicator}）。
 *
 * <p>源模组会读样本物品的要素构成、从周围 {@code IAspectSource} 一点一点抽，抽够了就
 * 「打印」一个复制品出来。1.21.1 的要素构成没有公开接口，这里统一按 4 点
 * {@code praecantatio} 计价，用前置公开的 {@link EssentiaApi} 搜索并抽取。</p>
 *
 * <p>抽取范围 6 格（{@link EssentiaSearch#nearby(int)}），每 40 tick 尝试一次；
 * 找不到源就停着等，不浪费。产出上限一叠，满了也停。</p>
 */
public class ReplicatorBlockEntity extends BlockEntity {

    private static final AspectId COST_ASPECT = AspectId.parse("praecantatio");
    /** 复制一件要 64 点魔法要素。 */
    public static final int COST_PER_ITEM = 64;
    private static final int ATTEMPT_INTERVAL = 40;
    private static final int SEARCH_RANGE = 6;
    /** 单次抽取的块大小：一次性要 64 点，很多罐子不够，按 4 点一块慢慢攒。 */
    private static final int CHUNK = 4;
    /** 每次尝试最多抽多少（16 / 4 = 4 个来源），约 5 秒攒满 64。 */
    private static final int BUDGET_PER_ATTEMPT = 16;

    private ItemStack sample = ItemStack.EMPTY;
    private ItemStack output = ItemStack.EMPTY;
    private int collected;

    public ReplicatorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.REPLICATOR_BE.get(), pos, state);
    }

    // ============================================================================================
    // 每 tick：攒要素 → 产出
    // ============================================================================================
    public static void tick(Level level, BlockPos pos, BlockState state, ReplicatorBlockEntity replicator) {
        if (level.isClientSide || level.getGameTime() % ATTEMPT_INTERVAL != 0) {
            return;
        }
        if (replicator.sample.isEmpty() || !replicator.canAcceptOutput()) {
            return;
        }
        int remaining = COST_PER_ITEM - replicator.collected;
        int drawn = replicator.drawEssentia(Math.min(remaining, BUDGET_PER_ATTEMPT));
        if (drawn > 0) {
            replicator.collected += drawn;
            replicator.setChanged();
        }
        if (replicator.collected < COST_PER_ITEM) {
            return;
        }
        replicator.collected = 0;
        replicator.produce();
        replicator.setChanged();
    }

    /**
     * 从周围精髓源抽要素，返回实际抽到的数量。
     *
     * <p>按 {@link #CHUNK} 点一块去要，能同时从多个罐子凑；一次要 64 点的话，
     * 单个罐子存量不足就直接失败。</p>
     */
    private int drawEssentia(int amount) {
        if (amount <= 0 || this.level == null) {
            return 0;
        }
        int total = 0;
        int budget = amount;
        while (budget > 0) {
            int chunk = Math.min(budget, CHUNK);
            var source = EssentiaApi.findSource(this.level, this.worldPosition, COST_ASPECT, chunk,
                    EssentiaSearch.nearby(SEARCH_RANGE));
            if (source.isEmpty()) {
                break;
            }
            int got = EssentiaApi.extract(this.level, source.get(), COST_ASPECT, chunk,
                    EssentiaTransferMode.EXECUTE);
            if (got <= 0) {
                break;
            }
            total += got;
            budget -= got;
        }
        return total;
    }

    private boolean canAcceptOutput() {
        if (this.output.isEmpty()) {
            return true;
        }
        return ItemStack.isSameItemSameComponents(this.output, singleSample())
                && this.output.getCount() < this.output.getMaxStackSize();
    }

    private ItemStack singleSample() {
        if (this.sample.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack copy = this.sample.copy();
        copy.setCount(1);
        return copy;
    }

    private void produce() {
        ItemStack one = singleSample();
        if (one.isEmpty()) {
            return;
        }
        if (this.output.isEmpty()) {
            this.output = one;
        } else {
            this.output.grow(1);
        }
    }

    // ============================================================================================
    // 交互
    // ============================================================================================
    public void setSample(ItemStack stack) {
        ItemStack copy = stack.copy();
        copy.setCount(1);
        this.sample = copy;
        this.collected = 0;
        this.setChanged();
    }

    /** 已经攒了多少点要素。 */
    public int collected() {
        return this.collected;
    }

    public boolean hasSample() {
        return !this.sample.isEmpty();
    }

    public boolean hasOutput() {
        return !this.output.isEmpty();
    }

    public ItemStack takeOutput() {
        ItemStack result = this.output;
        this.output = ItemStack.EMPTY;
        this.setChanged();
        return result;
    }

    // ============================================================================================
    // NBT
    // ============================================================================================
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!this.sample.isEmpty()) {
            tag.put("Sample", this.sample.save(registries));
        }
        if (!this.output.isEmpty()) {
            tag.put("Output", this.output.save(registries));
        }
        tag.putInt("Collected", this.collected);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.sample = tag.contains("Sample")
                ? ItemStack.parse(registries, tag.getCompound("Sample")).orElse(ItemStack.EMPTY)
                : ItemStack.EMPTY;
        this.output = tag.contains("Output")
                ? ItemStack.parse(registries, tag.getCompound("Output")).orElse(ItemStack.EMPTY)
                : ItemStack.EMPTY;
        this.collected = tag.getInt("Collected");
    }
}
