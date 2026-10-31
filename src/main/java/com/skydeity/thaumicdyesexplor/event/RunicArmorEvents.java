package com.skydeity.thaumicdyesexplor.event;

import com.skydeity.thaumicdyesexplor.ThaumicDyesExplor;
import com.skydeity.thaumicdyesexplor.component.RunicUpgrades;
import com.skydeity.thaumicdyesexplor.item.RunicArmorItem;
import com.skydeity.thaumicdyesexplor.item.TravellerBootsItem;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * 符文护甲的升级效果与自修复。
 *
 * <h2>升级表（源模组 NBT {@code upgrade}/{@code upgrade2}/{@code upgrade3} 的 0~10）</h2>
 * <table border="1">
 *   <caption>可被动实现的升级</caption>
 *   <tr><td>7</td><td>Steadfast 坚毅</td><td>+20% 抗击退</td></tr>
 *   <tr><td>8</td><td>Vitality 强健</td><td>+10 最大生命</td></tr>
 *   <tr><td>9</td><td>Haste 迅捷</td><td>+3% 移速（仅靴子）</td></tr>
 *   <tr><td>10</td><td>Warlord 战争领主</td><td>+2 攻击伤害</td></tr>
 * </table>
 *
 * <p>升级 1/3/4/6（狂战/动能/复苏/应急）都是<b>破盾触发</b>。1.21.1 的前置 API 只暴露
 * {@code MagicAttributes.RUNIC_SHIELD_CAPACITY}（上限倍率），没有“当前护盾值”或破盾事件，
 * 因此这四条暂时无法实现，已在 ROADMAP 中登记。</p>
 *
 * <h2>自修复</h2>
 * <p>源模组 {@code ItemRunicArmor#onUpdate} 的逻辑是：每 {@code 1000ms} 修 1 点耐久，
 * 身上每多一件带「狂战」升级的符文护甲就 {@code -150ms}，下限 {@code 200ms}。
 * 换算成 tick 就是 20 → 4 tick（每件 -3）。虚空旅者靴另有一套更慢的修复节拍。</p>
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
        if (stack.getItem() instanceof RunicArmorItem runic && runic.getType() == ArmorItem.Type.BOOTS) {
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
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR) {
                continue;
            }
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.isEmpty() || !stack.isDamaged()) {
                continue;
            }
            boolean runic = stack.getItem() instanceof RunicArmorItem;
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
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR) {
                continue;
            }
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.getItem() instanceof RunicArmorItem && RunicUpgrades.of(stack).count(1) > 0) {
                berserkers++;
            }
        }
        return Math.max(REPAIR_MIN_TICKS, REPAIR_BASE_TICKS - REPAIR_BOOST_TICKS * berserkers);
    }

    private static EquipmentSlotGroup groupOf(ItemStack stack) {
        if (stack.getItem() instanceof RunicArmorItem runic) {
            return EquipmentSlotGroup.bySlot(runic.getEquipmentSlot());
        }
        return null;
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ThaumicDyesExplor.MOD_ID, path);
    }

    private RunicArmorEvents() {
    }
}
