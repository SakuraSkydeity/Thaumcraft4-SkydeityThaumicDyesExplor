package com.skydeity.thaumicdyesexplor.curio;

import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

/**
 * 一件已装备饰品的能力实现。
 *
 * <p>Curios 9.x 把它做成了物品能力（{@code CuriosCapability.ITEM}）：能力对象按 {@link ItemStack}
 * 逐个创建，所以「当前饰品」就存在这个对象里，不需要再去查槽位。{@link ICurio} 的其余方法
 * 全部有默认实现，子类只覆盖自己需要的几个。</p>
 */
public abstract class CurioHandler implements ICurio {

    private final ItemStack stack;

    protected CurioHandler(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public ItemStack getStack() {
        return this.stack;
    }

    /**
     * 佩戴者每 tick 触发一次（仅服务端会真正跑逻辑）。
     */
    @Override
    public void curioTick(SlotContext slotContext) {
    }

    @Override
    public boolean canEquip(SlotContext slotContext) {
        return true;
    }

    @Override
    public boolean canUnequip(SlotContext slotContext) {
        return true;
    }
}
