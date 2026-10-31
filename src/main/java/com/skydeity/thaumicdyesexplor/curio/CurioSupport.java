package com.skydeity.thaumicdyesexplor.curio;

import java.util.function.Predicate;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

/**
 * 饰品栏查询的薄封装。
 *
 * <p>Curios 是 Thaumcraft 4R 的硬依赖，这里直接调用它的公开 API；所有方法都容忍
 * 「能力尚未挂载」（例如玩家刚进世界）的情况，统一返回 false。</p>
 */
public final class CurioSupport {

    private CurioSupport() {
    }

    /** 玩家身上是否装备了指定物品。 */
    public static boolean isEquipped(Player player, Item item) {
        return CuriosApi.getCuriosInventory(player)
                .map(handler -> handler.isEquipped(item))
                .orElse(false);
    }

    /** 玩家身上是否装备了满足条件的饰品。 */
    public static boolean isEquipped(Player player, Predicate<ItemStack> predicate) {
        return CuriosApi.getCuriosInventory(player)
                .map(handler -> handler.isEquipped(predicate))
                .orElse(false);
    }

    /** 玩家身上所有非空饰品。 */
    public static java.util.List<ItemStack> equipped(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .map(handler -> {
                    var curios = handler.getEquippedCurios();
                    java.util.List<ItemStack> out = new java.util.ArrayList<>(curios.getSlots());
                    for (int slot = 0; slot < curios.getSlots(); slot++) {
                        ItemStack stack = curios.getStackInSlot(slot);
                        if (!stack.isEmpty()) {
                            out.add(stack);
                        }
                    }
                    return java.util.List.copyOf(out);
                })
                .orElseGet(java.util.List::of);
    }
}
