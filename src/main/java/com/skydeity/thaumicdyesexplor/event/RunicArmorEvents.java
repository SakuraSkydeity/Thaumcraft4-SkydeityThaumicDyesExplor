package com.skydeity.thaumicdyesexplor.event;

import com.skydeity.thaumicdyesexplor.ThaumicDyesExplor;
import com.skydeity.thaumicdyesexplor.compat.PlayerMagicAccess;
import com.skydeity.thaumicdyesexplor.component.RunicUpgrades;
import com.skydeity.thaumicdyesexplor.item.ArmorSets;
import com.skydeity.thaumicdyesexplor.item.RunicUpgradeHolder;
import com.skydeity.thaumicdyesexplor.item.TravellerBootsItem;

import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * 符文护甲的升级效果与自修复。
 *
 * <h2>升级表（源模组 NBT {@code upgrade}/{@code upgrade2}/{@code upgrade3} 的 0~10）</h2>
 * <table border="1">
 *   <caption>本类负责的升级</caption>
 *   <tr><td>2</td><td>Shield Nexus 护盾节点</td><td>符文充能 +50%/个（见 {@link RunicUpgradeHolder}）</td></tr>
 *   <tr><td>5</td><td>Hardened 硬化</td><td>护盾被击穿后，防具的普通护甲值加倍（强化甲 +1）</td></tr>
 *   <tr><td>7</td><td>Steadfast 坚毅</td><td>+20% 抗击退</td></tr>
 *   <tr><td>8</td><td>Vitality 强健</td><td>+10 最大生命</td></tr>
 *   <tr><td>9</td><td>Haste 迅捷</td><td>+3% 移速（仅靴子）</td></tr>
 *   <tr><td>10</td><td>Warlord 战争领主</td><td>+2 攻击伤害</td></tr>
 * </table>
 *
 * <p>升级 1/3/4/6（狂战/动能/复苏/应急）是<b>破盾瞬间</b>触发的，单独在
 * {@code event/RunicBreakEvents} 里做（每 tick 读一次护盾值、检测“有 → 无”的跌破沿，
 * 读值走 {@code compat/PlayerMagicAccess} 的反射）。本类只管持续性被动。</p>
 *
 * <h2>升级 5「硬化」为什么这么算</h2>
 * <p>源模组三个品阶各自重写 {@code ISpecialArmor#getProperties}：</p>
 * <ul>
 *   <li>基础甲：{@code ratio = damageReduceAmount * 2 / 25} —— 该件护甲值翻倍；</li>
 *   <li>强化甲：只多 1 点护甲；</li>
 *   <li>深渊甲：源码里整段被注释掉，等于无效果（源文本也说「substantially reduced」）；</li>
 *   <li>同一件上有多个「硬化」不叠加（源文本：additional slots with this upgrade will have no effect）。</li>
 * </ul>
 * <p>1.21.1 没有逐件的 {@code ISpecialArmor}，护甲减伤统一在 {@code LivingEntity#actuallyHurt}
 * 里按 {@code Attributes.ARMOR} 算。于是这里用「等值换算」：在伤害进入护甲计算<b>之前</b>，
 * 把伤害乘以 {@code 剩余比例(护甲+加成) / 剩余比例(护甲)}，让最终的护甲减伤结果与
 * 「这件护甲的护甲值翻倍」完全一致，而不去动任何属性修饰符（避免 HUD 抖动与并发问题）。</p>
 */
@EventBusSubscriber(modid = ThaumicDyesExplor.MOD_ID)
public final class RunicArmorEvents {

    private static final ResourceLocation VITALITY_ID = id("runic_vitality");
    private static final ResourceLocation STEADFAST_ID = id("runic_steadfast");
    private static final ResourceLocation HASTE_ID = id("runic_haste");
    private static final ResourceLocation WARLORD_ID = id("runic_warlord");

    /** 自修复基础间隔（tick）：源模组 1000ms。 */
    private static final int REPAIR_BASE_TICKS = 20;
    /** 每件「狂战」升级减少的间隔（tick）：源模组 150ms。 */
    private static final int REPAIR_BOOST_TICKS = 3;
    /** 自修复最快间隔（tick）：源模组 200ms。 */
    private static final int REPAIR_MIN_TICKS = 4;
    /** 虚空旅者靴的自修复间隔（tick，源模组每 20 tick 修 1 点）。 */
    private static final int VOID_REPAIR_INTERVAL = 20;

    /** 护甲槽（1.21.1 的 EquipmentSlot 没有 ARMOR 常量组）。 */
    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET };

    // ============================================================================================
    // 属性修正（升级 7 / 8 / 9 / 10）
    // ============================================================================================
    @SubscribeEvent
    public static void onItemAttributeModifiers(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        RunicUpgrades upgrades = RunicUpgrades.of(stack);
        if (!upgrades.hasAny()) {
            return;
        }
        EquipmentSlotGroup group = groupOf(stack);
        if (group == null) {
            return;
        }

        // 8 Vitality：+10 最大生命
        int vitality = upgrades.count(8);
        if (vitality > 0) {
            event.addModifier(Attributes.MAX_HEALTH,
                    new AttributeModifier(VITALITY_ID, 10.0D * vitality, AttributeModifier.Operation.ADD_VALUE), group);
        }

        // 7 Steadfast：+0.2 抗击退（每级）
        int steadfast = upgrades.count(7);
        if (steadfast > 0) {
            event.addModifier(Attributes.KNOCKBACK_RESISTANCE,
                    new AttributeModifier(STEADFAST_ID, 0.2D * steadfast, AttributeModifier.Operation.ADD_VALUE), group);
        }

        // 9 Haste：+3% 移速（只有靴子吃得到，源模组就是这样限定的）
        if (stack.getItem() instanceof ArmorItem armor && armor.getType() == ArmorItem.Type.BOOTS) {
            int haste = upgrades.count(9);
            if (haste > 0) {
                event.addModifier(Attributes.MOVEMENT_SPEED,
                        new AttributeModifier(HASTE_ID, 0.03D * haste,
                                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL), group);
            }
        }

        // 10 Warlord：+2 攻击伤害
        int warlord = upgrades.count(10);
        if (warlord > 0) {
            event.addModifier(Attributes.ATTACK_DAMAGE,
                    new AttributeModifier(WARLORD_ID, 2.0D * warlord, AttributeModifier.Operation.ADD_VALUE), group);
        }
    }

    // ============================================================================================
    // 升级 5：硬化（护盾被击穿之后才生效）
    // ============================================================================================

    /**
     * 破盾后的「硬化」护甲加成。
     *
     * <p>优先级用 {@link EventPriority#LOWEST}：前置的 {@code RunicShieldSystem} 在
     * {@code NORMAL} 里先扣护盾，所以到这里时护盾值已经是本次扣除后的结果。
     * 判定直接读 {@link PlayerMagicAccess#charge}（护盾还有就不加成，与源文本
     * “once its shielding has been breached” 一致）；反射不可用时退化成
     * “伤害没被护盾完全吸收”这个等价信号。</p>
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) {
            return;
        }
        float amount = event.getAmount();
        int charge = PlayerMagicAccess.charge(entity);
        if (charge > 0) {
            return; // 护盾仍在
        }
        if (charge < 0 && amount <= 0.0F) {
            return; // 读不到护盾值时的兜底：伤害被护盾完全吸收 = 护盾还在
        }
        double bonus = hardenedArmorBonus(entity);
        if (bonus <= 0.0D) {
            return;
        }
        double armor = entity.getAttributeValue(Attributes.ARMOR);
        double toughness = entity.getAttributeValue(Attributes.ARMOR_TOUGHNESS);
        double before = damageAfterArmor(amount, armor, toughness);
        double after = damageAfterArmor(amount, armor + bonus, toughness);
        if (before <= 0.0D || after >= before) {
            return;
        }
        event.setAmount((float) (amount * (after / before)));
    }

    /** 身上所有「硬化」符文甲提供的额外护甲值（每件最多算一次）。 */
    private static double hardenedArmorBonus(LivingEntity entity) {
        double bonus = 0.0D;
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack stack = entity.getItemBySlot(slot);
            if (!(stack.getItem() instanceof RunicUpgradeHolder holder)) {
                continue;
            }
            if (RunicUpgrades.of(stack).count(5) <= 0) {
                continue;
            }
            ArmorSets.RunicTier tier = ArmorSets.runicTier(stack.getItem());
            if (tier == null) {
                continue;
            }
            bonus += switch (tier) {
                case BASIC -> armorValueOf(stack); // 基础甲：该件护甲值翻倍
                case ENHANCED -> 1.0D;             // 强化甲：+1
                case PRIMAL -> 0.0D;               // 深渊甲：源模组无效果
            };
        }
        return bonus;
    }

    /** 该件护甲在属性面板上贡献的护甲值（读默认属性修饰符，含材质与 toughness 无关）。 */
    private static double armorValueOf(ItemStack stack) {
        ItemAttributeModifiers modifiers = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
        if (modifiers == null) {
            return 0.0D;
        }
        double armor = 0.0D;
        for (ItemAttributeModifiers.Entry entry : modifiers.modifiers()) {
            if (entry.attribute().value() == Attributes.ARMOR.value()
                    && entry.modifier().operation() == AttributeModifier.Operation.ADD_VALUE) {
                armor += entry.modifier().amount();
            }
        }
        return armor;
    }

    /**
     * 原版 {@code LivingEntity#getDamageAfterArmorAbsorb} 的等价实现：
     * {@code f = 2 + 韧性/4}，{@code g = clamp(护甲 - 伤害/f, 护甲*0.2, 20)}，
     * 返回扣掉护甲后的剩余伤害。
     */
    private static double damageAfterArmor(double damage, double armor, double toughness) {
        double f = 2.0D + toughness / 4.0D;
        // 与 MathHelper.clamp(护甲 - 伤害/f, 护甲*0.2, 20) 等价，但先各自夹到 20 以内，
        // 免得别的模组把护甲堆过 100 时 min > max 反而算出负伤害。
        double upper = Math.min(armor - damage / f, 20.0D);
        double lower = Math.min(armor * 0.2D, 20.0D);
        double g = Math.max(upper, lower);
        return damage * (1.0D - g / 25.0D);
    }

    // ============================================================================================
    // 自修复
    // ============================================================================================
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) {
            return;
        }
        int interval = repairInterval(player);
        boolean repairTick = player.tickCount % interval == 0;
        boolean voidTick = player.tickCount % VOID_REPAIR_INTERVAL == 0;
        if (!repairTick && !voidTick) {
            return;
        }
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.isEmpty() || !stack.isDamaged()) {
                continue;
            }
            boolean runic = stack.getItem() instanceof RunicUpgradeHolder;
            boolean voidBoots = stack.getItem() instanceof TravellerBootsItem boots
                    && boots.kind() == TravellerBootsItem.Kind.VOID;
            if ((runic && repairTick) || (voidBoots && voidTick)) {
                stack.setDamageValue(stack.getDamageValue() - 1);
            }
        }
    }

    /** 当前玩家的自修复间隔：基础 20 tick，每件带升级 1 的符文护甲 -3 tick，下限 4。 */
    private static int repairInterval(Player player) {
        int berserkers = 0;
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.getItem() instanceof RunicUpgradeHolder && RunicUpgrades.of(stack).count(1) > 0) {
                berserkers++;
            }
        }
        return Math.max(REPAIR_MIN_TICKS, REPAIR_BASE_TICKS - REPAIR_BOOST_TICKS * berserkers);
    }

    private static EquipmentSlotGroup groupOf(ItemStack stack) {
        if (stack.getItem() instanceof ArmorItem armor) {
            return EquipmentSlotGroup.bySlot(armor.getEquipmentSlot());
        }
        return null;
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ThaumicDyesExplor.MOD_ID, path);
    }

    private RunicArmorEvents() {
    }
}
