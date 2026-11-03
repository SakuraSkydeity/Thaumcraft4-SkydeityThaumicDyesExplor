package com.skydeity.thaumicdyesexplor.item;

import java.util.List;

import com.skydeity.thaumicdyesexplor.component.RunicUpgrades;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * 「能装符文升级的东西」的公共契约：符文护甲 I/II/III、符文护目镜、符文旅者靴。
 *
 * <p>源模组里这些物品全部继承 {@code ItemRunicArmor}（{@code ItemRunicBootsTraveller}
 * 也是它的子类），所以充能公式、升级槽、tooltip 是一份代码。1.21.1 里符文甲是
 * {@link RunicArmorItem}、旅者靴另有自己的移动逻辑（{@link TravellerBootsItem}），
 * 两条继承链没法合并，于是把「充能 / 槽位 / tooltip」抽到这个接口里共用。</p>
 *
 * <h2>充能公式（与 1.7.10 逐档对照）</h2>
 * <p>源模组三个品阶的 {@code getRunicCharge} 都是手写表，但表里的数字恰好等于
 * {@code 部位基础值 × (1 + 0.5 × 护盾节点数量)}：</p>
 * <table border="1">
 *   <caption>三档充能（头/胸/腿/靴）</caption>
 *   <tr><td>无节点</td><td>8 / 24 / 16 / 8（合计 56）</td></tr>
 *   <tr><td>1 个节点</td><td>12 / 36 / 24 / 12（合计 84）</td></tr>
 *   <tr><td>2 个节点（II 双槽 / III）</td><td>16 / 48 / 32 / 16（合计 112）</td></tr>
 *   <tr><td>3 个节点（III 满槽）</td><td>20 / 60 / 40 / 20（合计 140）</td></tr>
 * </table>
 * <p>所以升级 2「护盾节点」不需要任何静态表，按件数动态算即可。</p>
 */
public interface RunicUpgradeHolder {

    /** 该件允许的升级槽数（基础/强化/旅者靴 1 个，符文护甲 III 3 个）。 */
    int upgradeSlots();

    /** 该件的部位基础充能（不含护盾节点加成）。 */
    int runicBaseCharge();

    /**
     * 实际符文充能：{@code 基础值 × (1 + 0.5 × 升级 2 的个数)}，四舍五入。
     *
     * <p>这就是升级 2（Shield Nexus / 护盾节点）的全部效果所在 —— 前置的
     * {@code EquipmentAbilities.baseRunicCharge} 会优先读 {@code RunicGear} 接口，
     * 因此这里返回什么，玩家的护盾上限就是什么（数据表里的 {@code runic_charge}
     * 只对没实现接口的物品生效）。</p>
     */
    default int runicCharge(ItemStack stack) {
        int nexus = RunicUpgrades.of(stack).count(2);
        return (int) Math.round(this.runicBaseCharge() * (1.0D + 0.5D * nexus));
    }

    /** 把升级槽 + 各槽升级 + 充能写进 tooltip（与 1.7.10 的 addInformation 一致）。 */
    default void appendRunicTooltip(ItemStack stack, List<Component> tooltip) {
        RunicUpgrades upgrades = RunicUpgrades.of(stack);
        int slots = this.upgradeSlots();
        if (slots > 0) {
            int filled = 0;
            for (int i = 0; i < slots; i++) {
                if (upgrades.at(i) > 0) {
                    filled++;
                }
            }
            tooltip.add(Component.translatable("tooltip.thaumicdyesexplor.runic_slots",
                    filled, slots).withStyle(ChatFormatting.GRAY));
        }
        for (int i = 0; i < slots; i++) {
            int upgrade = upgrades.at(i);
            if (upgrade > 0) {
                tooltip.add(Component.translatable("tooltip.thaumicdyesexplor.runic_upgrade."
                        + (slots > 1 ? "slot" : "single"), i + 1,
                        Component.translatable("item.runic.upgrade." + upgrade))
                        .withStyle(ChatFormatting.DARK_AQUA));
            }
        }
        if (slots > 0) {
            tooltip.add(Component.translatable("tooltip.thaumicdyesexplor.runic_charge",
                    this.runicCharge(stack)).withStyle(ChatFormatting.GOLD));
        }
    }
}
