package com.skydeity.thaumicdyesexplor.curio;

import java.util.function.Function;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.type.capability.ICurio;

/**
 * 可装备到 Curios 饰品栏的物品。
 *
 * <p>物品本身只负责「能放进饰品槽」（由 {@code data/curios/tags/item/*} 的标签决定）；
 * 具体行为交给 {@link CurioHandler}，由 {@code RegisterCapabilitiesEvent} 按物品逐个提供。</p>
 */
public class CurioItem extends Item {

    private final Function<ItemStack, CurioHandler> factory;

    public CurioItem(Properties properties, Function<ItemStack, CurioHandler> factory) {
        super(properties);
        this.factory = factory;
    }

    /** 为这一份具体饰品创建能力对象。 */
    public ICurio createCurio(ItemStack stack) {
        return this.factory.apply(stack);
    }
}
