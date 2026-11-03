package com.skydeity.thaumicdyesexplor.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * 束缚（Binding）。
 *
 * <p>对应源模组 {@code potionBinding}。它本身不造成任何数值变化，纯粹是个「标记」：
 * 带这个效果的末影人不能传送、苦力怕不能引爆、玩家不能扔末影珍珠 ——
 * 判定在 {@code event/TxEnchantmentEvents} 里。</p>
 *
 * <p>来源：主手武器带「束缚」附魔 2 级，攻击末影人 / 苦力怕 / 玩家时施加 50 tick。</p>
 */
public class BindingEffect extends MobEffect {

    public BindingEffect() {
        super(MobEffectCategory.HARMFUL, 0x3B1E5A);
    }

    /** 纯标记效果，不需要每 tick 执行。 */
    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return false;
    }
}
