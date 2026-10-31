package com.skydeity.thaumicdyesexplor.component;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.Codec;
import com.skydeity.thaumicdyesexplor.registry.ModDataComponents;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

/**
 * 符文护甲升级槽数据（对应 1.7.10 的 NBT 键 {@code upgrade} / {@code upgrade2} / {@code upgrade3}）。
 *
 * <p>源模组把三段升级写在物品 NBT 上：基础/强化护甲 1 个槽，符文护甲 III（Primal）3 个槽。
 * 1.21.1 用数据组件承载同样的信息，值域 0~10（0 = 空槽）：</p>
 *
 * <table border="1">
 *   <caption>升级表（见源模组 {@code tc.research_page.TD.RUNICARMORUPGRADES}）</caption>
 *   <tr><td>1</td><td>Berserker 狂战</td><td>破盾时获得力量爆发</td></tr>
 *   <tr><td>2</td><td>Shield Nexus 护盾节点</td><td>符文护盾上限 +50%</td></tr>
 *   <tr><td>3</td><td>Kinetic Feedback 动能反馈</td><td>破盾时爆炸</td></tr>
 *   <tr><td>4</td><td>Revitalizing Feedback 复苏反馈</td><td>破盾时获得生命恢复</td></tr>
 *   <tr><td>5</td><td>Hardened 硬化</td><td>破盾后额外护甲</td></tr>
 *   <tr><td>6</td><td>Emergency Shielding 应急护盾</td><td>破盾时立刻回充护盾</td></tr>
 *   <tr><td>7</td><td>Steadfast 坚毅</td><td>+20% 抗击退</td></tr>
 *   <tr><td>8</td><td>Vitality 强健</td><td>+10 最大生命</td></tr>
 *   <tr><td>9</td><td>Haste 迅捷</td><td>+3% 移动速度（仅靴子）</td></tr>
 *   <tr><td>10</td><td>Warlord 战争领主</td><td>+2 攻击伤害</td></tr>
 * </table>
 *
 * @param slots 各槽位的升级 id（0 = 空），长度不超过该护甲允许的槽数
 */
public record RunicUpgrades(List<Integer> slots) {

    public static final int MAX_UPGRADE = 10;

    public static final RunicUpgrades EMPTY = new RunicUpgrades(List.of());

    public static final Codec<RunicUpgrades> CODEC =
            Codec.INT.listOf().xmap(RunicUpgrades::new, RunicUpgrades::slots);

    public static final StreamCodec<ByteBuf, RunicUpgrades> STREAM_CODEC =
            ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list())
                    .map(RunicUpgrades::new, RunicUpgrades::slots);

    public RunicUpgrades {
        slots = List.copyOf(slots);
    }

    public static RunicUpgrades of(ItemStack stack) {
        RunicUpgrades value = stack.get(ModDataComponents.RUNIC_UPGRADES);
        return value == null ? EMPTY : value;
    }

    /** 指定槽位的升级 id，越界返回 0。 */
    public int at(int index) {
        return index >= 0 && index < this.slots.size() ? this.slots.get(index) : 0;
    }

    public int size() {
        return this.slots.size();
    }

    /** 是否至少有一个非空槽位。 */
    public boolean hasAny() {
        for (int value : this.slots) {
            if (value > 0) {
                return true;
            }
        }
        return false;
    }

    /** 是否含有某个升级（可能出现在多个槽位）。 */
    public boolean has(int upgrade) {
        return this.count(upgrade) > 0;
    }

    /** 某个升级出现的次数（多槽叠加是源模组的“魔法共鸣”设计）。 */
    public int count(int upgrade) {
        if (upgrade <= 0) {
            return 0;
        }
        int n = 0;
        for (int value : this.slots) {
            if (value == upgrade) {
                n++;
            }
        }
        return n;
    }

    /** 写入升级：先填空槽，槽满则覆盖第一个槽（源模组“注魔新的会覆盖第一个槽”的描述）。 */
    public RunicUpgrades with(int upgrade, int maxSlots) {
        List<Integer> list = new ArrayList<>(this.slots);
        while (list.size() < maxSlots) {
            list.add(0);
        }
        if (maxSlots <= 0) {
            return this;
        }
        boolean placed = false;
        for (int i = 0; i < maxSlots; i++) {
            if (list.get(i) == 0) {
                list.set(i, upgrade);
                placed = true;
                break;
            }
        }
        if (!placed) {
            list.set(0, upgrade);
        }
        return new RunicUpgrades(list.subList(0, maxSlots));
    }

    /** 清空（强化升级为 II/III 时源模组会抹掉旧升级）。 */
    public RunicUpgrades cleared(int maxSlots) {
        List<Integer> list = new ArrayList<>(maxSlots);
        for (int i = 0; i < maxSlots; i++) {
            list.add(0);
        }
        return new RunicUpgrades(list);
    }
}
