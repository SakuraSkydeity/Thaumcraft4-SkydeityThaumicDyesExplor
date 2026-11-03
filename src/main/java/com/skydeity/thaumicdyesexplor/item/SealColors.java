package com.skydeity.thaumicdyesexplor.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;

/**
 * 封印的 16 种颜色。
 *
 * <p>源模组里封印是“一个物品 + damage 0~15”的元数据变体，颜色由
 * {@code getColorFromItemStack} 返回 {@code EntitySheep.fleeceColorTable[15 - damage]}，
 * 贴图本身是纯灰白（{@code sealblank.png} 只有 #9D9D9D~#FDFDFD 几档灰度），
 * 所以不染色的时候看起来就是<b>一块白色方块</b>。</p>
 *
 * <p>1.21.1 的等价做法是 {@link DataComponents#DYED_COLOR} + 一个 {@code ItemColor}：
 * 灰度贴图 × 染料色 = 彩色封印。这里把“variant 0..15”映射回
 * {@link DyeColor#byId(int) 染料 id 15 - variant}，与原版的 {@code 15 - damage} 完全一致。</p>
 */
public final class SealColors {

    /** 颜色变体数量（对应 16 种染料）。 */
    public static final int VARIANTS = 16;

    /** 没染色时的默认色（白色羊毛）。 */
    public static final int DEFAULT_COLOR = 0xFFE9ECEC;

    /** 第 {@code variant} 个变体对应的 ARGB 颜色。 */
    public static int colorOf(int variant) {
        return DyeColor.byId(15 - Math.max(0, Math.min(15, variant))).getTextureDiffuseColor();
    }

    /** 造一个带颜色的封印。 */
    public static ItemStack stack(Item item, int variant) {
        ItemStack stack = new ItemStack(item);
        // getTextureDiffuseColor 本来就是 0xFFRRGGBB；别把 alpha 抹掉 ——
        // DYED_COLOR 存成 RGB（alpha=0）后，ItemColor 一乘就是全透明图标
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(colorOf(variant), false));
        return stack;
    }

    /** 取封印当前颜色（ARGB），未染色时给默认色。 */
    public static int tint(ItemStack stack) {
        // 旧存档里的 DYED_COLOR 存过无 alpha 的 RGB，这里统一把 alpha 提到不透明
        return 0xFF000000 | DyedItemColor.getOrDefault(stack, DEFAULT_COLOR);
    }

    private SealColors() {
    }
}
