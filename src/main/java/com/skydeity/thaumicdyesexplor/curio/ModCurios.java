package com.skydeity.thaumicdyesexplor.curio;

import com.skydeity.thaumicdyesexplor.registry.ModItems;
import com.skydeity.thaumicdyesexplor.registry.ModMeItems;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

/**
 * 把本模组的饰品接到 Curios 的物品能力上。
 *
 * <p>「能放进饰品槽」由 {@code data/curios/tags/item/*.json} 的标签决定，和这里无关；
 * 这里提供的是真正做事的能力对象——没有它，饰品只能摆着，{@code curioTick} 不会触发。</p>
 */
public final class ModCurios {

    private ModCurios() {
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        ModItems.CURIO_ITEMS.forEach(holder -> {
            CurioItem item = holder.get();
            event.registerItem(CuriosCapability.ITEM, (stack, context) -> item.createCurio(stack), item);
        });
        RelicCurios.register(event);
        // 扭曲修正之戒（MiddleEarth）：每 20 tick 刷新前置的 warp_ward 药水效果，
        // 源模组在 onUnequipped 时移除 —— Curios 的效果会随时长自然到期，这里不额外摘。
        event.registerItem(CuriosCapability.ITEM, (stack, context) -> new ICurio() {
            @Override
            public ItemStack getStack() {
                return stack;
            }

            @Override
            public void curioTick(SlotContext slotContext) {
                if (!(slotContext.entity() instanceof LivingEntity living)
                        || living.level().isClientSide || living.tickCount % 20 != 0) {
                    return;
                }
                var warpWard = BuiltInRegistries.MOB_EFFECT
                        .getHolder(ResourceLocation.fromNamespaceAndPath("thaumcraft", "warp_ward"));
                warpWard.ifPresent(holder -> living.addEffect(new MobEffectInstance(holder, 240, 0)));
            }
        }, ModMeItems.RING_WARPWARD.get());
    }
}
