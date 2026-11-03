package com.skydeity.thaumicdyesexplor.registry;

import com.skydeity.thaumicdyesexplor.ThaumicDyesExplor;
import com.skydeity.thaumicdyesexplor.effect.BindingEffect;
import com.skydeity.thaumicdyesexplor.effect.TaintWithdrawalEffect;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 本模组自有的状态效果。
 *
 * <p>源模组有一个自定义药水 {@code potionTaintWithdrawl}：腐化之戒在腐化群系外戴久了
 * 会开始「戒断」。1.21.1 里状态效果要注册到 {@code Registries.MOB_EFFECT}。</p>
 */
public final class ModEffects {

    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, ThaumicDyesExplor.MOD_ID);

    /** 腐化戒断：持续掉血 + 加速饥饿。 */
    public static final Holder<MobEffect> TAINT_WITHDRAWAL =
            EFFECTS.register("taint_withdrawal", TaintWithdrawalEffect::new);

    /** 束缚：末影人 / 苦力怕 / 玩家不能传送或引爆（「束缚」附魔 2 级的标记）。 */
    public static final Holder<MobEffect> BINDING =
            EFFECTS.register("binding", BindingEffect::new);

    private ModEffects() {
    }
}
