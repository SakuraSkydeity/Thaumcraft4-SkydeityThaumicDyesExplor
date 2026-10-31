package com.skydeity.thaumicdyesexplor.item;

import java.util.List;

import dev.tc4port.thaumcraft.api.wand.WandCapSpec;
import dev.tc4port.thaumcraft.api.wand.WandPartApi;
import dev.tc4port.thaumcraft.api.wand.WandRodSpec;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/**
 * 杖杆 / 杖端的通用物品。
 *
 * <p>物品本身不带任何数据——容量、消耗修正、贴图全在数据包（{@code data_maps/item/wand_rods.json}
 * 与 {@code wand_caps.json}）里。这里只是通过公开 API {@link WandPartApi} 把那些数值显示到
 * tooltip 上，让玩家在装配前就知道这根杆子装得下多少 Vis。</p>
 */
public class WandPartItem extends Item {

    public WandPartItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        WandRodSpec rod = WandPartApi.rod(stack).orElse(null);
        if (rod != null) {
            tooltip.add(Component.translatable("tooltip.thaumicdyesexplor.wand_rod.capacity", rod.capacity())
                    .withStyle(net.minecraft.ChatFormatting.GRAY));
            if (!rod.staff()) {
                tooltip.add(Component.translatable("tooltip.thaumicdyesexplor.wand_rod.hint")
                        .withStyle(net.minecraft.ChatFormatting.DARK_GRAY));
            }
            return;
        }
        WandCapSpec cap = WandPartApi.cap(stack).orElse(null);
        if (cap != null) {
            tooltip.add(Component.translatable("tooltip.thaumicdyesexplor.wand_cap.cost", cap.baseCostModifier())
                    .withStyle(net.minecraft.ChatFormatting.GRAY));
        }
    }
}
