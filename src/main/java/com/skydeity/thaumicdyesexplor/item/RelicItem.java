package com.skydeity.thaumicdyesexplor.item;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/**
 * Forgotten Relics 的通用物品，只多做一件事：在 tooltip 里写明「这件东西怎么用」。
 *
 * <p>遗物在源模组里靠 Baubles 槽位 + 按键，玩家经常拿到手不知道要装哪儿、要不要法杖；
 * 这里把用法压成 1~3 行灰字常驻在 tooltip 上（不需要按 Shift 才显示），
 * 具体数值与细节仍然写在魔导书「中土遗物学」里。</p>
 *
 * <p>用法文本的键由注册表 id 派生：{@code tooltip.thaumicdyesexplor.usage.<物品路径>}，
 * 多行时追加 {@code .2}、{@code .3}。键缺失会直接把键名显示出来，所以新增遗物时
 * 必须同时补 lang —— {@code tools/gen_book_relics.py} 会做一致性校验。</p>
 */
public class RelicItem extends Item {

    private final List<String> usageKeys;

    public RelicItem(Properties properties, String... usageKeys) {
        super(properties);
        this.usageKeys = List.of(usageKeys);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        for (String key : usageKeys) {
            tooltip.add(Component.translatable(key).withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
