package com.skydeity.thaumicdyesexplor.curio;

import com.skydeity.thaumicdyesexplor.registry.ModDataComponents;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;

import com.skydeity.thaumicdyesexplor.component.StoredFood;

/**
 * 营养护符：自动把快捷栏里的食物「吃」进护符，饿的时候再吐出来。
 *
 * <p>对应源模组 {@code ItemFoodTalisman}：每 20 tick 扫一遍快捷栏，把没有药水效果的
 * 食物消耗 1 个，折算成食物点 / 饱和点存起来（各封顶 100）；饥饿值或饱和值不足时自动补充。
 * 源模组里真正补给玩家的两行被注释掉了（只能囤不能吃），这里补全成可用状态。</p>
 */
public final class TalismanFoodHandler extends CurioHandler {

    /** 扫描范围：快捷栏 9 格。源模组扫的是 0~9，1.21.1 快捷栏正好是 0~8。 */
    private static final int HOTBAR_SIZE = 9;

    public TalismanFoodHandler(ItemStack stack) {
        super(stack);
    }

    @Override
    public void curioTick(SlotContext slotContext) {
        if (!(slotContext.entity() instanceof Player player) || player.level().isClientSide()) {
            return;
        }
        if (player.tickCount % 20 != 0) {
            return;
        }
        StoredFood stored = this.getStack().getOrDefault(ModDataComponents.STORED_FOOD, StoredFood.EMPTY);
        stored = this.absorbHotbar(player, stored);
        stored = this.feedOwner(player, stored);
        this.getStack().set(ModDataComponents.STORED_FOOD, stored);
    }

    /** 把快捷栏里安全的食物转成口粮。 */
    private StoredFood absorbHotbar(Player player, StoredFood stored) {
        for (int slot = 0; slot < HOTBAR_SIZE; slot++) {
            ItemStack food = player.getInventory().getItem(slot);
            if (food.isEmpty() || stored.food() >= StoredFood.CAP) {
                continue;
            }
            FoodProperties properties = food.getFoodProperties(player);
            if (properties == null || !properties.effects().isEmpty()) {
                // 有药水效果的食物不收：吐出来会变成意外的 buff/debuff 来源。
                continue;
            }
            stored = stored.plus(properties.nutrition(), properties.saturation() * 2.0F);
            food.shrink(1);
            player.level().playSound(null, player.blockPosition(), SoundEvents.GENERIC_EAT,
                    SoundSource.PLAYERS, 0.5F, 0.9F + player.getRandom().nextFloat() * 0.2F);
        }
        return stored;
    }

    /** 饥饿值 / 饱和值不足时，从口粮里补回去。 */
    private StoredFood feedOwner(Player player, StoredFood stored) {
        FoodData stats = player.getFoodData();
        if (stored.food() > 0 && stats.getFoodLevel() < 20) {
            int take = Math.min(20 - stats.getFoodLevel(), stored.food());
            stats.setFoodLevel(stats.getFoodLevel() + take);
            stored = stored.minusFood(take);
        }
        if (stored.saturation() > 0.0F && stats.getSaturationLevel() < stats.getFoodLevel()) {
            float take = Math.min(stats.getFoodLevel() - stats.getSaturationLevel(), stored.saturation());
            stats.setSaturation(stats.getSaturationLevel() + take);
            stored = stored.minusSaturation(take);
        }
        return stored;
    }
}
