package com.skydeity.thaumicdyesexplor.item;

import java.util.List;

import com.skydeity.thaumicdyesexplor.component.RunicUpgrades;
import com.skydeity.thaumicdyesexplor.registry.ModDataComponents;

import dev.tc4port.thaumcraft.api.item.RunicGear;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/**
 * 符文护甲基类（对应源模组 {@code ItemRunicArmor} / {@code ItemRunicArmorEnhanced} /
 * {@code ItemRunicArmorPrimal}）。
 *
 * <p>源模组用 {@code IRunicArmor#getRunicCharge} 把“这件护甲能给护盾贡献多少充能”告诉本体，
 * 并在 NBT 里存 {@code upgrade} 0~10 做升级。这里：</p>
 * <ul>
 *   <li>实现 {@link RunicGear} —— 1.21.1 里就是 {@code IRunicArmor} 的等价接口，
 *       本体的 {@code EquipmentAbilities.baseRunicCharge()} 会优先读它；</li>
 *   <li>升级数据放进 {@link RunicUpgrades} 组件，槽数按品阶区分（I/II 一槽、III 三槽）；</li>
 *   <li>属性修正、自修复与破盾效果在 {@code event/RunicArmorEvents} 里统一处理。</li>
 * </ul>
 */
public class RunicArmorItem extends SimpleArmorItem implements RunicGear {

    /** 各部位的符文护盾基础充能（与源模组的手写值一致）。 */
    private static final int CHARGE_HELMET = 8;
    private static final int CHARGE_CHEST = 24;
    private static final int CHARGE_LEGS = 16;
    private static final int CHARGE_BOOTS = 8;

    private final int upgradeSlots;

    public RunicArmorItem(Holder<ArmorMaterial> material, Type type, Properties properties, ArmorGeoType geoType,
                         int upgradeSlots) {
        super(material, type, properties, geoType);
        this.upgradeSlots = upgradeSlots;
    }

    /** 该件护甲允许的升级槽数（源模组：基础/强化 1 个，Primal 3 个）。 */
    public int upgradeSlots() {
        return this.upgradeSlots;
    }

    /** 部位基础充能（不含护盾节点加成）。 */
    public int baseCharge() {
        return switch (this.getType()) {
            case HELMET -> CHARGE_HELMET;
            case CHESTPLATE -> CHARGE_CHEST;
            case LEGGINGS -> CHARGE_LEGS;
            case BOOTS -> CHARGE_BOOTS;
            default -> 0;
        };
    }

    /**
     * 符文护盾充能：基础值 × (1 + 0.5 × 护盾节点数量)。
     *
     * <p>源模组升级 2「Shield Nexus」每多一个槽 +50%，Primal 三槽全叠时为 2.5 倍，
     * 与源码里“20/60/40/20（三节点 140）对基本 56”的比例一致。</p>
     */
    @Override
    public int thaumcraftRunicCharge(ItemStack stack) {
        RunicUpgrades upgrades = RunicUpgrades.of(stack);
        int nexus = upgrades.count(2);
        int base = this.baseCharge();
        return (int) Math.round(base * (1.0D + 0.5D * nexus));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        RunicUpgrades upgrades = RunicUpgrades.of(stack);
        if (upgrades.hasAny()) {
            for (int i = 0; i < upgrades.size(); i++) {
                int upgrade = upgrades.at(i);
                if (upgrade > 0) {
                    tooltip.add(Component.translatable("tooltip.thaumicdyesexplor.runic_upgrade."
                            + (this.upgradeSlots > 1 ? "slot" : "single"), i + 1,
                            Component.translatable("item.runic.upgrade." + upgrade))
                            .withStyle(ChatFormatting.DARK_AQUA));
                }
            }
        }
        if (this.upgradeSlots > 0) {
            tooltip.add(Component.translatable("tooltip.thaumicdyesexplor.runic_charge",
                    this.thaumcraftRunicCharge(stack)).withStyle(ChatFormatting.GOLD));
        }
        super.appendHoverText(stack, context, tooltip, flag);
    }

    /** 便捷方法：把升级写回物品。 */
    public ItemStack withUpgrade(ItemStack stack, int upgrade) {
        ItemStack copy = stack.copy();
        copy.set(ModDataComponents.RUNIC_UPGRADES, RunicUpgrades.of(copy).with(upgrade, this.upgradeSlots));
        return copy;
    }

    /** 判断这件护甲是否允许该升级（升级 9 只能上靴子）。 */
    public boolean acceptsUpgrade(int upgrade) {
        if (upgrade < 0 || upgrade > RunicUpgrades.MAX_UPGRADE || this.upgradeSlots <= 0) {
            return false;
        }
        return upgrade != 9 || this.getType() == ArmorItem.Type.BOOTS;
    }
}
