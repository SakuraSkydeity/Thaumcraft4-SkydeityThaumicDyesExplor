package com.skydeity.thaumicdyesexplor.recipe;

import java.util.List;

import com.skydeity.thaumicdyesexplor.item.SealColors;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * 「染料 + 护甲 → 上色护甲」的特殊合成（源模组 {@code CultistRobesDyes} 等 8 个配方的统一实现）。
 *
 * <h2>源模组是怎么做的</h2>
 * <p>每个染甲类各有一个 {@code IRecipe}，输入是「1 件该套护甲 + 1 份染料」，输出是把玩家
 * 选的 16 色写进 NBT 的 {@code display.color}。染料颜色不是取染料自身的 mapColor，而是
 * 直接取 {@code EntitySheep.fleeceColorTable} 那一档，所以是“16 色板”而不是自由混色。</p>
 *
 * <h2>移植做法</h2>
 * <p>1.21.1 里皮革染色的标准姿势就是 {@code DataComponents.DYED_COLOR}，护甲材质层用
 * {@code dyeable} 标记哪一层吃颜色。所以这里只写<b>一个</b>特殊配方：
 * 任意染料 + 任意“有可染色层”的护甲 → 覆盖式上色。这样既保留了“一套护甲 16 色”的效果，
 * 又不必为 8 套护甲 × 16 色写 128 个 JSON。</p>
 */
public class ArmorDyeRecipe extends CustomRecipe {

    public ArmorDyeRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        ItemStack armor = ItemStack.EMPTY;
        ItemStack dye = ItemStack.EMPTY;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.getItem() instanceof DyeItem) {
                if (!dye.isEmpty()) {
                    return false;
                }
                dye = stack;
            } else if (stack.getItem() instanceof ArmorItem armorItem && isDyeable(armorItem)) {
                if (!armor.isEmpty()) {
                    return false;
                }
                armor = stack;
            } else {
                return false;
            }
        }
        return !armor.isEmpty() && !dye.isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack armor = ItemStack.EMPTY;
        int color = SealColors.DEFAULT_COLOR;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.getItem() instanceof DyeItem dye) {
                color = dye.getDyeColor().getTextureDiffuseColor();
            } else if (!stack.isEmpty()) {
                armor = stack;
            }
        }
        if (armor.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack result = armor.copyWithCount(1);
        result.set(DataComponents.DYED_COLOR, new DyedItemColor(color & 0xFFFFFF, false));
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.ARMOR_DYE.get();
    }

    /** 这套护甲是否有可染色的布料层。 */
    private static boolean isDyeable(ArmorItem item) {
        ArmorMaterial material = item.getMaterial().value();
        List<ArmorMaterial.Layer> layers = material.layers();
        for (ArmorMaterial.Layer layer : layers) {
            if (layer.dyeable()) {
                return true;
            }
        }
        return false;
    }
}
