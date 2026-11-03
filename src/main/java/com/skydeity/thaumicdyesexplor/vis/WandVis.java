package com.skydeity.thaumicdyesexplor.vis;

import java.util.Map;

import dev.tc4port.thaumcraft.api.aspect.VisAction;
import dev.tc4port.thaumcraft.api.aspect.VisChannel;
import dev.tc4port.thaumcraft.api.aspect.VisCost;
import dev.tc4port.thaumcraft.api.wand.WandApi;
import dev.tc4port.thaumcraft.api.wand.WandCarrierItem;
import dev.tc4port.thaumcraft.api.wand.WandView;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

/**
 * 「遗物 / 焦点自己没有 vis，去玩家背包里找一把法杖扣」的公共工具。
 *
 * <p>第 19 轮之前遗物施法直接忽略 {@code consumeVis} 的返回值，法杖空了照样放技能，
 * 表现成「放出来了但没效果」；这里统一成<b>严格付费</b>：六个通道都够才一次性扣掉，
 * 不够就只提示、完全不动法杖。</p>
 *
 * <p>成本单位是 <b>centivis</b>（1 vis = 100），与 {@link WandView#visCentivis()} 一致。</p>
 */
public final class WandVis {

    private WandVis() {
    }

    /**
     * 严格付费：任一通道不足则不扣、不执行，并提示玩家。
     *
     * @param cost 每个通道需要的 centivis
     * @return 是否成功扣除
     */
    public static boolean pay(ServerPlayer player, Map<VisChannel, Integer> cost) {
        ItemStack wand = findWand(player);
        if (wand.isEmpty()) {
            player.displayClientMessage(
                    Component.translatable("msg.thaumicdyesexplor.relics.no_wand"), true);
            return false;
        }
        WandView view = WandApi.view(wand).orElse(null);
        if (view == null) {
            player.displayClientMessage(
                    Component.translatable("msg.thaumicdyesexplor.relics.no_wand"), true);
            return false;
        }
        for (var entry : cost.entrySet()) {
            if (view.visCentivis().getOrDefault(entry.getKey(), 0) < entry.getValue()) {
                player.displayClientMessage(
                        Component.translatable("msg.thaumicdyesexplor.relics.not_enough_vis"), true);
                return false;
            }
        }
        for (var entry : cost.entrySet()) {
            WandApi.extract(player, wand, VisCost.ofCentivis(entry.getKey(), entry.getValue()),
                    VisAction.EXECUTE);
        }
        return true;
    }

    /** 背包 / 副手 / 双手里第一把能读数据的法杖；没有返回 {@link ItemStack#EMPTY}。 */
    public static ItemStack findWand(ServerPlayer player) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack held = player.getItemInHand(hand);
            if (held.getItem() instanceof WandCarrierItem && WandApi.view(held).isPresent()) {
                return held;
            }
        }
        var inventory = player.getInventory();
        for (ItemStack stack : inventory.items) {
            if (stack.getItem() instanceof WandCarrierItem && WandApi.view(stack).isPresent()) {
                return stack;
            }
        }
        for (ItemStack stack : inventory.offhand) {
            if (stack.getItem() instanceof WandCarrierItem && WandApi.view(stack).isPresent()) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }
}
