package com.skydeity.thaumicdyesexplor.item;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorMaterial;

/**
 * 旅者靴 / 流星靴 / 彗星靴（对应源模组 {@code ThaumiumBoots}、{@code VoidBoots}、
 * {@code ItemRunicBootsTraveller} 与 Thaumic Exploration 的 {@code ItemTXArmorSpecial}）。
 *
 * <p>1.7.10 里这些靴子在 {@code onArmorTick} 里直接改 {@code player.stepHeight}、
 * {@code moveFlying(...)}、{@code fallDistance}、{@code jumpMovementFactor}。
 * 1.21.1 的等价实现见 {@code event/BootsEvents}：
 * 跨步高度走 {@code Attributes.STEP_HEIGHT}，加速走 {@code Attributes.MOVEMENT_SPEED}，
 * 跳跃加成走 {@code LivingJumpEvent}，悬浮燃料效率与坠落减免走本体的装备能力数据表
 * （{@code data_maps/item/equipment_abilities.json} 的 {@code hover_fuel_efficiency} /
 * {@code fall_distance_reduction}）。</p>
 */
public class TravellerBootsItem extends SimpleArmorItem {

    /** 靴子种类，决定移动参数。 */
    public enum Kind {
        /** 神秘旅者靴：跨步 1.0、加速 +0.085、跳跃 +0.275、坠落 -0.35。 */
        THAUMIUM(0.40F, 0.20D, 0.275D, false, false, 2),
        /** 虚空旅者靴：跨步 1.0、加速 +0.10、跳跃 +0.35、坠落 -0.5、缓慢自修复。 */
        VOID(0.40F, 0.30D, 0.35D, false, true, 5),
        /** 符文旅者靴：跨步 1.0、加速 +0.055、跳跃 +0.275、坠落 -0.25。 */
        RUNIC_TRAVELLER(0.40F, 0.15D, 0.275D, false, false, 2),
        /** 流星靴：跳跃 +0.275、潜行起跳冲刺、落地砸地爆炸。 */
        METEOR(0.40F, 0.15D, 0.275D, true, false, 2),
        /** 彗星靴：跳跃 +0.275、行走时冻结脚下水面。 */
        COMET(0.40F, 0.15D, 0.275D, false, false, 5);

        private final float stepBonus;
        private final double speedMultiplier;
        private final double jumpBonus;
        private final boolean slam;
        private final boolean selfRepair;
        /** 仅用于 tooltip 展示的魔力减免（百分比），真正生效的是数据表。 */
        private final int visDiscount;

        Kind(float stepBonus, double speedMultiplier, double jumpBonus, boolean slam, boolean selfRepair,
             int visDiscount) {
            this.stepBonus = stepBonus;
            this.speedMultiplier = speedMultiplier;
            this.jumpBonus = jumpBonus;
            this.slam = slam;
            this.selfRepair = selfRepair;
            this.visDiscount = visDiscount;
        }

        /** 魔力减免（百分比）：神秘 2 / 虚空 5 / 符文 2 / 流星 2 / 彗星 5。 */
        public int visDiscount() {
            return this.visDiscount;
        }

        /** 追加的跨步高度（原版基础 0.6，加到 1.0 需要 +0.4）。 */
        public float stepBonus() {
            return this.stepBonus;
        }

        /** 移动速度倍率（ADD_MULTIPLIED_TOTAL）。 */
        public double speedMultiplier() {
            return this.speedMultiplier;
        }

        /** 起跳时追加的垂直速度。 */
        public double jumpBonus() {
            return this.jumpBonus;
        }

        /** 是否具备“流星砸地”。 */
        public boolean slam() {
            return this.slam;
        }

        /** 是否缓慢自修复（虚空靴）。 */
        public boolean selfRepair() {
            return this.selfRepair;
        }
    }

    private final Kind kind;

    public TravellerBootsItem(Holder<ArmorMaterial> material, Properties properties, ArmorGeoType geoType, Kind kind) {
        super(material, Type.BOOTS, properties, geoType, kind.visDiscount());
        this.kind = kind;
    }

    public Kind kind() {
        return this.kind;
    }
}
