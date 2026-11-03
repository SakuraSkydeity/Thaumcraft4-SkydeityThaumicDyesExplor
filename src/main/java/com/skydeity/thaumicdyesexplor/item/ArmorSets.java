package com.skydeity.thaumicdyesexplor.item;

import java.util.List;
import java.util.Set;

import com.skydeity.thaumicdyesexplor.registry.ModItems;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * 「套装」判定：哪些物品算作一套、哪些头盔支持面具 / 护目镜开关。
 *
 * <p>对应源模组：{@code ThaumiumFortressDyed} / {@code IronFortressArmor} 数身上穿的件数
 * 得到“套装等级”；{@code mask} / {@code goggles} NBT 由注魔写到头盔上。本移植里
 * 这两个开关改成 <b>手持头盔右键切换</b>（见 {@code event/ArmorToggles}）。</p>
 */
public final class ArmorSets {

    /** 堡垒甲（源 ThaumiumFortressDyed）：头/胸/腿三件，套装等级 0~3。 */
    public static final Set<Item> FORTRESS = Set.of(
            ModItems.FORTRESS_HELMET.get(), ModItems.FORTRESS_CHESTPLATE.get(), ModItems.FORTRESS_LEGGINGS.get());

    /** 铁堡垒甲（源 IronFortressArmor）：同样三件。 */
    public static final Set<Item> IRON_FORTRESS = Set.of(
            ModItems.IRON_FORTRESS_HELMET.get(), ModItems.IRON_FORTRESS_CHESTPLATE.get(),
            ModItems.IRON_FORTRESS_LEGGINGS.get());

    /** 符文护甲 III（源 ItemRunicArmorPrimal）：四件，套装等级 0~4。 */
    public static final Set<Item> RUNIC_PRIMAL = Set.of(
            ModItems.RUNIC_PRIMAL_HELMET.get(), ModItems.RUNIC_PRIMAL_CHESTPLATE.get(),
            ModItems.RUNIC_PRIMAL_LEGGINGS.get(), ModItems.RUNIC_PRIMAL_BOOTS.get());

    /** 符文护甲 II（源 ItemRunicArmorEnhanced）：四件。升级 5「硬化」只给 +1 护甲。 */
    public static final Set<Item> RUNIC_ENHANCED = Set.of(
            ModItems.RUNIC_ENHANCED_HELMET.get(), ModItems.RUNIC_ENHANCED_CHESTPLATE.get(),
            ModItems.RUNIC_ENHANCED_LEGGINGS.get(), ModItems.RUNIC_ENHANCED_BOOTS.get());

    /**
     * 符文护甲的档位，只用来区分升级 5「硬化」的三种档位效果。
     *
     * <p>源模组是三条各自重写 {@code getProperties} 的继承链，这里用一个枚举收敛：
     * 基础甲（I / 护目镜 / 旅者靴）护甲翻倍、强化甲（II）+1、深渊甲（III）无效。</p>
     */
    public enum RunicTier {
        /** 符文护甲 I、符文护目镜、符文旅者靴。 */
        BASIC,
        /** 符文护甲 II。 */
        ENHANCED,
        /** 符文护甲 III（源文本：对深渊甲几乎没有效果）。 */
        PRIMAL
    }

    /** 判定某件护甲的符文档位（非符文甲返回 {@code null}）。 */
    public static RunicTier runicTier(Item item) {
        if (RUNIC_PRIMAL.contains(item)) {
            return RunicTier.PRIMAL;
        }
        if (RUNIC_ENHANCED.contains(item)) {
            return RunicTier.ENHANCED;
        }
        if (item instanceof com.skydeity.thaumicdyesexplor.item.RunicUpgradeHolder) {
            return RunicTier.BASIC;
        }
        return null;
    }

    /** 堡垒家族（源里减伤公式同时数这两套，{@code instanceof ItemFortressArmor}）。 */
    public static final Set<Item> FORTRESS_FAMILY = Set.of(
            ModItems.FORTRESS_HELMET.get(), ModItems.FORTRESS_CHESTPLATE.get(), ModItems.FORTRESS_LEGGINGS.get(),
            ModItems.IRON_FORTRESS_HELMET.get(), ModItems.IRON_FORTRESS_CHESTPLATE.get(),
            ModItems.IRON_FORTRESS_LEGGINGS.get());

    /** 支持面具 0~2 的头盔（堡垒 / 铁堡垒）。 */
    public static final Set<Item> MASK_FULL = Set.of(
            ModItems.FORTRESS_HELMET.get(), ModItems.IRON_FORTRESS_HELMET.get());

    /** 支持面具 0~1 的头盔（游侠 / 弓箭手，染色与未染色都要算）。 */
    public static final Set<Item> MASK_RANGER = Set.of(
            ModItems.CULTIST_RANGER_HELMET.get(), ModItems.CULTIST_RANGER_DYED_HELMET.get(),
            ModItems.CULTIST_ARCHER_HELMET.get(), ModItems.CULTIST_ARCHER_DYED_HELMET.get());

    /** 四个护甲槽（1.21.1 的 EquipmentSlot 没有 ARMOR 常量组）。 */
    private static final List<EquipmentSlot> ARMOR_SLOTS =
            List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);

    /** 统计实体身上穿着的某套件数。 */
    public static int count(LivingEntity entity, Set<Item> set) {
        int count = 0;
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            if (set.contains(entity.getItemBySlot(slot).getItem())) {
                count++;
            }
        }
        return count;
    }

    /** 堡垒家族里是否有任意一件带了面具（源模组减伤公式的 +0.05）。 */
    public static boolean familyHasMask(LivingEntity entity) {
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack stack = entity.getItemBySlot(slot);
            if (FORTRESS_FAMILY.contains(stack.getItem())
                    && stack.get(com.skydeity.thaumicdyesexplor.registry.ModDataComponents.ARMOR_MASK) != null) {
                return true;
            }
        }
        return false;
    }

    private ArmorSets() {
    }
}
