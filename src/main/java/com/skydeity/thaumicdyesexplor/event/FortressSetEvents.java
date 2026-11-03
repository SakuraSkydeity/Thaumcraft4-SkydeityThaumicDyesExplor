package com.skydeity.thaumicdyesexplor.event;

import com.skydeity.thaumicdyesexplor.ThaumicDyesExplor;
import com.skydeity.thaumicdyesexplor.item.ArmorSets;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/**
 * 堡垒家族（堡垒甲 + 铁堡垒）的套装减伤。
 *
 * <p>源模组 {@code ThaumiumFortressDyed / IronFortressArmor#getArmorProperties} 的公式是
 * “每件护甲的吸收比例 × (0.875 + 0.125 × 穿上件数 [+0.05 有面具])”，两套共用一个判定
 * （{@code instanceof ItemFortressArmor}）。1.21.1 没有逐件的 ISpecialArmor 接口，
 * 这里把该系数作用到<b>进入护甲计算前的伤害</b>上（除以系数）—— 穿满三件 + 面具时
 * 系数 1.25，等于多挡掉两成伤害，与源模组“套件越齐越硬”的方向一致。</p>
 */
@EventBusSubscriber(modid = ThaumicDyesExplor.MOD_ID)
public final class FortressSetEvents {

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) {
            return;
        }
        int count = ArmorSets.count(entity, ArmorSets.FORTRESS_FAMILY);
        if (count <= 0) {
            return;
        }
        double factor = 0.875D + 0.125D * count + (ArmorSets.familyHasMask(entity) ? 0.05D : 0.0D);
        event.setAmount((float) (event.getAmount() / factor));
    }

    private FortressSetEvents() {
    }
}
