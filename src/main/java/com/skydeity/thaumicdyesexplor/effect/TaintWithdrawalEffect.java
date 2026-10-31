package com.skydeity.thaumicdyesexplor.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * 腐化戒断。
 *
 * <p>腐化之戒戴在腐化之地里是护身符；一旦带着它离开腐化群系超过 100 tick，
 * 身体就会开始「退瘾」——持续掉血并加速饥饿。回到腐化群系即停止累积。</p>
 *
 * <p>对应源模组 {@code ThaumicExploration.potionTaintWithdrawl}（原版药水，无代码效果），
 * 这里补上明确的负面效果。</p>
 */
public class TaintWithdrawalEffect extends MobEffect {

    public TaintWithdrawalEffect() {
        super(MobEffectCategory.HARMFUL, 0x6A3880);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        // 每 40 tick 扣 1 点魔法伤害，并把饥饿值往下拉一点。
        entity.hurt(entity.damageSources().magic(), 1.0F + amplifier);
        if (entity instanceof Player player) {
            player.causeFoodExhaustion(0.6F + 0.2F * amplifier);
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 40 == 0;
    }
}
