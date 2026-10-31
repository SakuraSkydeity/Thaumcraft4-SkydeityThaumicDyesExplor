package com.skydeity.thaumicdyesexplor.curio;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.skydeity.thaumicdyesexplor.ThaumicDyesExplor;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;

/**
 * 注魔稳定腰带：抗击退 +1.0。
 *
 * <p>对应源模组 {@code ItemStabilizerBelt} 里那条固定 UUID 的
 * {@code SharedMonsterAttributes.knockbackResistance} 修饰符。1.21.1 改由 Curios 在每次
 * 装备变化时重新汇总，所以只要把修饰符报出去即可，不需要自己 apply/remove。</p>
 */
public final class StabilizerBeltHandler extends CurioHandler {

    private static final ResourceLocation MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(ThaumicDyesExplor.MOD_ID, "stabilizer_belt_knockback");

    private static final double KNOCKBACK_RESISTANCE = 1.0D;

    public StabilizerBeltHandler(ItemStack stack) {
        super(stack);
    }

    @Override
    public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(SlotContext slotContext,
                                                                                ResourceLocation identifier) {
        Multimap<Holder<Attribute>, AttributeModifier> modifiers = HashMultimap.create();
        modifiers.put(Attributes.KNOCKBACK_RESISTANCE,
                new AttributeModifier(MODIFIER_ID, KNOCKBACK_RESISTANCE, AttributeModifier.Operation.ADD_VALUE));
        return modifiers;
    }
}
