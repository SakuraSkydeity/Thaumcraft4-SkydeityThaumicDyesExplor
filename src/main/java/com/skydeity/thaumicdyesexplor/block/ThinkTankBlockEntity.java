package com.skydeity.thaumicdyesexplor.block;

import com.skydeity.thaumicdyesexplor.registry.ModBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 思考罐的方块实体（源模组 {@code TileEntityThinkTank}）。
 *
 * <p>源模组的研究文本：把书不断喂进奥秘罐，就能产出知识碎片。这里保留两个关键约束：</p>
 * <ul>
 *   <li><b>必须放在书架上</b>（下方是书架），否则不工作；</li>
 *   <li>一本书烧 200 tick（10 秒）产一次：普通书 10% 出 1 个知识碎片，
 *       附魔书 67% 出 1~2 个（与源模组的随机区间一致）。</li>
 * </ul>
 *
 * <p>另外两条是移植时加的（源模组没有）：</p>
 * <ul>
 *   <li><b>一次最多囤 10 本</b>，放满后要继续喂得先等它烧掉几本；</li>
 *   <li><b>保底</b>：连续 3 本普通书都没出碎片时，第 4 本必定出 1 个 ——
 *       纯 10% 的概率在游戏里太容易让人以为「机器坏了」。</li>
 * </ul>
 *
 * <p>知识碎片直接产出前置的 {@code thaumcraft:knowledge_fragment}，不另造物品。</p>
 */
public class ThinkTankBlockEntity extends BlockEntity {

    /** 前置的知识碎片物品 id。 */
    private static final ResourceLocation KNOWLEDGE_FRAGMENT =
            ResourceLocation.fromNamespaceAndPath("thaumcraft", "knowledge_fragment");

    private static final int COOK_TICKS = 200;
    /** 一次最多能囤的书本数。 */
    public static final int MAX_BOOKS = 10;
    /** 连续这么多本没出碎片就保底一本。 */
    private static final int DRY_LIMIT = 3;

    private ItemStack stored = ItemStack.EMPTY;
    private ItemStack output = ItemStack.EMPTY;
    private int progress;
    /** 连续几本没产出（保底用）。 */
    private int dryBooks;

    public ThinkTankBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.THINK_TANK_BE.get(), pos, state);
    }

    // ============================================================================================
    // 每 tick
    // ============================================================================================
    public static void tick(Level level, BlockPos pos, BlockState state, ThinkTankBlockEntity tank) {
        if (level.isClientSide) {
            return;
        }
        // 条件不满足就清零重来（和源模组一致：炉子灭了进度归零）
        if (!tank.hasBookshelf() || tank.stored.isEmpty() || !tank.canAcceptOutput()) {
            if (tank.progress != 0) {
                tank.progress = 0;
                tank.setChanged();
            }
            return;
        }
        if (++tank.progress < COOK_TICKS) {
            if (tank.progress % 20 == 0) {
                tank.setChanged();
            }
            return;
        }
        tank.progress = 0;
        int made = tank.consumeOneBook();
        if (made > 0) {
            // 出碎片时冒个粒子和一点声响，免得玩家不知道成了
            if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER,
                        pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D,
                        8, 0.25D, 0.25D, 0.25D, 0.02D);
            }
            level.playSound(null, pos, net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP,
                    net.minecraft.sounds.SoundSource.BLOCKS, 0.6F, 1.2F);
        }
        tank.setChanged();
    }

    /** 源模组要求装置坐在书架上（1.21 加了雕纹书架，一起认）。 */
    private boolean hasBookshelf() {
        if (this.level == null) {
            return false;
        }
        return this.level.getBlockState(this.worldPosition.below()).is(Blocks.BOOKSHELF)
                || this.level.getBlockState(this.worldPosition.below()).is(Blocks.CHISELED_BOOKSHELF);
    }

    private boolean canAcceptOutput() {
        if (this.output.isEmpty()) {
            return true;
        }
        ItemStack fragment = knowledgeFragment();
        return !fragment.isEmpty()
                && ItemStack.isSameItem(this.output, fragment)
                && this.output.getCount() < this.output.getMaxStackSize();
    }

    /** 烧掉一本，按源模组的概率产出知识碎片（带保底）。返回本次产出数量。 */
    private int consumeOneBook() {
        ItemStack book = this.stored.copy();
        this.stored.shrink(1);

        double roll = this.level == null ? 0.0D : this.level.random.nextDouble();
        int count = 0;
        if (book.is(Items.ENCHANTED_BOOK)) {
            if (roll > 0.33D) {
                count = roll > 0.66D ? 1 : 2;
            }
        } else if (book.is(Items.BOOK)) {
            if (roll <= 0.10D) {
                count = 1;
            }
            // 保底：连续 5 本没出，第 6 本必出
            if (count == 0 && ++this.dryBooks >= DRY_LIMIT) {
                count = 1;
            }
        }
        if (count > 0) {
            this.dryBooks = 0;
            ItemStack produced = knowledgeFragment();
            if (!produced.isEmpty()) {
                produced.setCount(count);
                if (this.output.isEmpty()) {
                    this.output = produced;
                } else {
                    this.output.grow(count);
                }
            } else {
                count = 0;
            }
        }
        if (this.stored.getCount() <= 0) {
            this.stored = ItemStack.EMPTY;
        }
        return count;
    }

    private static ItemStack knowledgeFragment() {
        var item = BuiltInRegistries.ITEM.get(KNOWLEDGE_FRAGMENT);
        return item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
    }

    // ============================================================================================
    // 交互
    // ============================================================================================
    /**
     * 放入书；返回实际放进去的本数（0 = 失败）。
     *
     * <p>一次右键只放 1 本，方便数着放；罐子满了（{@link #MAX_BOOKS}）就放不进去。</p>
     */
    public int insertBook(ItemStack stack) {
        if (stack.isEmpty() || !(stack.is(Items.BOOK) || stack.is(Items.ENCHANTED_BOOK))) {
            return 0;
        }
        if (!this.stored.isEmpty() && !ItemStack.isSameItem(this.stored, stack)) {
            return 0;
        }
        if (this.stored.getCount() >= MAX_BOOKS) {
            return 0;
        }
        if (this.stored.isEmpty()) {
            this.stored = new ItemStack(stack.getItem(), 1);
        } else {
            this.stored.grow(1);
        }
        this.setChanged();
        return 1;
    }

    /** 取出积累的知识碎片。 */
    public ItemStack takeOutput() {
        ItemStack result = this.output;
        this.output = ItemStack.EMPTY;
        this.setChanged();
        return result;
    }

    public boolean hasOutput() {
        return !this.output.isEmpty();
    }

    public boolean isRunning() {
        return !this.stored.isEmpty() && this.hasBookshelf() && this.canAcceptOutput();
    }

    /** 供方块交互提示用。 */
    public boolean hasBookshelfForMessage() {
        return this.hasBookshelf();
    }

    /** 里面囤着几本书。 */
    public int storedCount() {
        return this.stored.isEmpty() ? 0 : this.stored.getCount();
    }

    /** 当前这本烧到百分之几（0~100）。 */
    public int progressPercent() {
        return Math.min(100, this.progress * 100 / COOK_TICKS);
    }

    public boolean isFull() {
        return this.storedCount() >= MAX_BOOKS;
    }

    /** 破坏时返还剩余的书。 */
    public ItemStack storedStack() {
        return this.stored.copy();
    }

    // ============================================================================================
    // NBT
    // ============================================================================================
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!this.stored.isEmpty()) {
            tag.put("Stored", this.stored.save(registries));
        }
        if (!this.output.isEmpty()) {
            tag.put("Output", this.output.save(registries));
        }
        tag.putInt("Progress", this.progress);
        tag.putInt("DryBooks", this.dryBooks);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.stored = tag.contains("Stored")
                ? ItemStack.parse(registries, tag.getCompound("Stored")).orElse(ItemStack.EMPTY)
                : ItemStack.EMPTY;
        this.output = tag.contains("Output")
                ? ItemStack.parse(registries, tag.getCompound("Output")).orElse(ItemStack.EMPTY)
                : ItemStack.EMPTY;
        this.progress = tag.getInt("Progress");
        this.dryBooks = tag.getInt("DryBooks");
    }
}
