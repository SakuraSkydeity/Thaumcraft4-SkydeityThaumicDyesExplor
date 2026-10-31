package com.skydeity.thaumicdyesexplor.curio;

import com.skydeity.thaumicdyesexplor.registry.ModItems;

import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import top.theillusivec4.curios.api.CuriosCapability;

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
    }
}
