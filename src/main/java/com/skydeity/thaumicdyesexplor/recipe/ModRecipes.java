package com.skydeity.thaumicdyesexplor.recipe;

import com.skydeity.thaumicdyesexplor.ThaumicDyesExplor;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 配方序列化器注册。
 *
 * <p>「染料 + 护甲」的染色配方用 {@link SimpleCraftingRecipeSerializer} 承载，
 * 数据包侧只需要一个 {@code data/thaumicdyesexplor/recipe/armor_dye.json}
 * （{@code {"type": "thaumicdyesexplor:armor_dye", "category": "misc"}}）。</p>
 */
public final class ModRecipes {

    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, ThaumicDyesExplor.MOD_ID);

    public static final DeferredHolder<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<ArmorDyeRecipe>> ARMOR_DYE =
            SERIALIZERS.register("armor_dye", () -> new SimpleCraftingRecipeSerializer<>(ArmorDyeRecipe::new));

    private ModRecipes() {
    }
}
