package com.skydeity.thaumicdyesexplor.event;

import java.util.List;

import com.skydeity.thaumicdyesexplor.ThaumicDyesExplor;
import com.skydeity.thaumicdyesexplor.item.ArmorSets;
import com.skydeity.thaumicdyesexplor.registry.ModDataComponents;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * 头盔的面具 / 护目镜开关。
 *
 * <p>源模组里 {@code mask}（0~2 的脸谱编号）与 {@code goggles}（护目镜开关）是
 * <b>注魔配方</b>写到头盔 NBT 上的；本移植把注魔改成<b>手持头盔右键</b>直接切换，
 * 数值语义与源模组一致（模型显隐 + 堡垒减伤 +5% 都读同一个数据组件）：</p>
 * <ul>
 *   <li>右键：切换护目镜（堡垒 / 铁堡垒头盔）；</li>
 *   <li>潜行 + 右键：循环面具（无 → 0 → 1 → 2 → 无；游侠系只有 0/1）。</li>
 * </ul>
 */
@EventBusSubscriber(modid = ThaumicDyesExplor.MOD_ID)
public final class ArmorToggles {

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickItem event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) {
            return;
        }
        Player player = event.getEntity();
        if (player.level().isClientSide) {
            return; // 组件写入只在服务端做，客户端靠同步看到结果
        }
        boolean maskHelmet = ArmorSets.MASK_FULL.contains(stack.getItem())
                || ArmorSets.MASK_RANGER.contains(stack.getItem());
        boolean gogglesHelmet = ArmorSets.MASK_FULL.contains(stack.getItem());
        if (!maskHelmet) {
            return;
        }
        int maxMask = ArmorSets.MASK_FULL.contains(stack.getItem()) ? 2 : 1;
        if (player.isShiftKeyDown()) {
            Integer current = stack.get(ModDataComponents.ARMOR_MASK);
            int next = current == null ? 0 : current + 1;
            if (next > maxMask) {
                stack.remove(ModDataComponents.ARMOR_MASK);
                player.displayClientMessage(Component.translatable("msg.thaumicdyesexplor.mask.none"), true);
            } else {
                stack.set(ModDataComponents.ARMOR_MASK, next);
                player.displayClientMessage(
                        Component.translatable("msg.thaumicdyesexplor.mask." + next), true);
            }
        } else if (gogglesHelmet) {
            boolean now = Boolean.TRUE.equals(stack.get(ModDataComponents.ARMOR_GOGGLES));
            stack.set(ModDataComponents.ARMOR_GOGGLES, !now);
            player.displayClientMessage(Component.translatable(
                    "msg.thaumicdyesexplor.goggles." + (!now ? "on" : "off")), true);
        } else {
            return;
        }
        player.level().playSound(null, player.blockPosition(), SoundEvents.UI_BUTTON_CLICK.value(),
                SoundSource.PLAYERS, 0.4F, 1.2F);
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }

    private ArmorToggles() {
    }

    /** 供 tooltip 显示当前状态（避免每次右键都拼字符串）。 */
    public static List<Component> describe(ItemStack helmet) {
        Integer mask = helmet.get(ModDataComponents.ARMOR_MASK);
        Boolean goggles = helmet.get(ModDataComponents.ARMOR_GOGGLES);
        return List.of(
                Component.translatable("msg.thaumicdyesexplor.mask." + (mask == null ? "none" : mask)),
                Component.translatable("msg.thaumicdyesexplor.goggles."
                        + (Boolean.TRUE.equals(goggles) ? "on" : "off")));
    }
}
