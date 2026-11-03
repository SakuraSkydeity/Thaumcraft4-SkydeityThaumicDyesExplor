package com.skydeity.thaumicdyesexplor.item;

import java.util.List;

import dev.tc4port.thaumcraft.api.item.RunicGear;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/**
 * 符文旅者靴（源模组 {@code ItemRunicBootsTraveller}）。
 *
 * <p>源模组里它 <b>继承</b> {@code ItemRunicArmor}，所以除了旅者靴的移动加成，
 * 它同时具备：1 个符文升级槽、8 点符文充能（护盾节点可按比例抬高）、
 * 以及全套升级效果。此前的移植只把它当成普通旅者靴（只靠数据表给了静态充能），
 * 结果「升级 2 / 5 装上去没反应」—— 这里补上 {@link RunicGear} 与
 * {@link RunicUpgradeHolder} 两件事：</p>
 * <ul>
 *   <li>实现 {@code RunicGear} 后，前置读的是本类动态充能，数据表里的
 *       {@code runic_charge} 不再生效；</li>
 *   <li>实现 {@link RunicUpgradeHolder} 后，tooltip 会显示「升级槽 x/y」与充能值，
 *       并且 {@code event/RunicArmorEvents} 的升级/硬化逻辑也会认它。</li>
 * </ul>
 */
public class RunicTravellerBootsItem extends TravellerBootsItem implements RunicGear, RunicUpgradeHolder {

    /** 源模组：靴子位 8 点基础充能，与符文靴一致。 */
    private static final int CHARGE_BOOTS = 8;

    public RunicTravellerBootsItem(Holder<ArmorMaterial> material, Properties properties, ArmorGeoType geoType) {
        super(material, properties, geoType, Kind.RUNIC_TRAVELLER);
    }

    @Override
    public int upgradeSlots() {
        return 1;
    }

    @Override
    public int runicBaseCharge() {
        return CHARGE_BOOTS;
    }

    @Override
    public int thaumcraftRunicCharge(ItemStack stack) {
        return this.runicCharge(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        this.appendRunicTooltip(stack, tooltip);
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
