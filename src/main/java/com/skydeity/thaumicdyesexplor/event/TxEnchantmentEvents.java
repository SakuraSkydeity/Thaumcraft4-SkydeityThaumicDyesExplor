package com.skydeity.thaumicdyesexplor.event;

import com.skydeity.thaumicdyesexplor.ThaumicDyesExplor;
import com.skydeity.thaumicdyesexplor.registry.ModEffects;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Thaumic Exploration 的三个附魔（源模组 {@code enchantment} 包）。
 *
 * <p>源模组里这三个类只有元数据（适用类型、最高等级、附魔台不可获得），真正的效果全部写在
 * {@code event/TXEventHandler} 里：{@code handleEnchantmentAttack} 管束缚与缴械，
 * {@code stopCreeperExplosions} 管夜视与苦力怕。这里按同样的分工实现。</p>
 *
 * <h2>效果表</h2>
 * <table border="1">
 *   <tr><th>附魔</th><th>等级</th><th>效果</th></tr>
 *   <tr><td>束缚 Binding</td><td>1</td><td>命中时目标缓慢 II，50 tick</td></tr>
 *   <tr><td></td><td>2</td><td>额外对末影人 / 苦力怕 / 玩家施加「束缚」50 tick：
 *       不能传送、苦力怕不能引爆</td></tr>
 *   <tr><td>缴械 Disarm</td><td>1~3</td><td>命中非玩家生物时按
 *       {@code 1/(10-2×等级)} 概率打掉目标主手（8 / 6 / 4 分之一）</td></tr>
 *   <tr><td>夜视 Night Vision</td><td>1</td><td>头盔带此附魔时持续获得夜视</td></tr>
 * </table>
 *
 * <p>三个附魔在 1.21.1 是数据驱动的（{@code data/thaumicdyesexplor/enchantment/*.json}），
 * 效果数值没有走 {@code effects} 组件 —— 那样要注册自定义 {@code EnchantmentEntityEffect}
 * 的 codec，而事件方式与源模组的分工更接近，也更好调。附魔书由注魔产出
 * （{@code infusion/enchant_*_book.json}），再用铁砧打上去。</p>
 */
@EventBusSubscriber(modid = ThaumicDyesExplor.MOD_ID)
public final class TxEnchantmentEvents {

    private static final ResourceKey<Enchantment> BINDING = key("binding");
    private static final ResourceKey<Enchantment> DISARM = key("disarm");
    private static final ResourceKey<Enchantment> NIGHT_VISION = key("night_vision");

    /** 束缚/缓慢的持续时间（源模组 50 tick）。 */
    private static final int BIND_TICKS = 50;
    /** 夜视剩余低于这个值就续（源模组 202）。 */
    private static final int NIGHT_VISION_REFRESH = 202;
    private static final int NIGHT_VISION_TICKS = 300;

    // ============================================================================================
    // 束缚 / 缴械：攻击命中时
    // ============================================================================================
    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide()) {
            return;
        }
        Entity attackerEntity = event.getSource().getEntity();
        if (!(attackerEntity instanceof LivingEntity attacker) || attacker == target) {
            return;
        }
        ItemStack weapon = attacker.getMainHandItem();
        if (weapon.isEmpty()) {
            return;
        }

        // --- 束缚：1 级缓慢；2 级再挂「束缚」标记 ---
        int binding = level(attacker, weapon, BINDING);
        if (binding > 0) {
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, BIND_TICKS, 1));
            if (binding > 1 && isBindable(target)) {
                target.addEffect(new MobEffectInstance(ModEffects.BINDING, BIND_TICKS, 0));
            }
        }

        // --- 缴械：只对非玩家生物，概率 1/(10-2×等级) ---
        int disarm = level(attacker, weapon, DISARM);
        if (disarm > 0 && !(target instanceof Player) && !target.getMainHandItem().isEmpty()) {
            int bound = Math.max(2, 10 - 2 * disarm);
            if (target.getRandom().nextInt(bound) == 0) {
                ItemStack dropped = target.getMainHandItem().copy();
                target.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                target.spawnAtLocation(dropped);
            }
        }
    }

    /** 会被「束缚」影响的生物：源模组只处理末影人、苦力怕、玩家。 */
    private static boolean isBindable(LivingEntity entity) {
        return entity instanceof Player || entity instanceof EnderMan || entity instanceof Creeper;
    }

    // ============================================================================================
    // 夜视：头盔附魔
    // ============================================================================================
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() || player.tickCount % 20 != 0) {
            return;
        }
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        if (helmet.isEmpty() || level(player, helmet, NIGHT_VISION) <= 0) {
            return;
        }
        MobEffectInstance current = player.getEffect(MobEffects.NIGHT_VISION);
        if (current == null || current.getDuration() < NIGHT_VISION_REFRESH) {
            // 环境效果：不冒粒子、不占屏幕图标，和源模组一样只是「看得见」
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, NIGHT_VISION_TICKS, 0,
                    true, false, false));
        }
    }

    // ============================================================================================
    // 「束缚」标记的三条禁令
    // ============================================================================================
    /** 末影人 / 潜影贝的随机传送。 */
    @SubscribeEvent
    public static void onEnderTeleport(EntityTeleportEvent.EnderEntity event) {
        if (event.getEntityLiving().hasEffect(ModEffects.BINDING)) {
            event.setCanceled(true);
        }
    }

    /** 玩家扔末影珍珠（源模组连玩家一起拦）。 */
    @SubscribeEvent
    public static void onEnderPearl(EntityTeleportEvent.EnderPearl event) {
        if (event.getPlayer().hasEffect(ModEffects.BINDING)) {
            event.setCanceled(true);
        }
    }

    /** 苦力怕引爆：有「束缚」就不炸。 */
    @SubscribeEvent
    public static void onExplosionStart(ExplosionEvent.Start event) {
        Entity source = event.getExplosion().getDirectSourceEntity();
        if (source instanceof LivingEntity living && living.hasEffect(ModEffects.BINDING)) {
            event.setCanceled(true);
        }
    }

    // ============================================================================================
    // 工具
    // ============================================================================================
    private static int level(LivingEntity entity, ItemStack stack, ResourceKey<Enchantment> key) {
        return entity.level().registryAccess().registryOrThrow(Registries.ENCHANTMENT)
                .getHolder(key)
                .map(holder -> EnchantmentHelper.getItemEnchantmentLevel(holder, stack))
                .orElse(0);
    }

    private static ResourceKey<Enchantment> key(String path) {
        return ResourceKey.create(Registries.ENCHANTMENT,
                ResourceLocation.fromNamespaceAndPath(ThaumicDyesExplor.MOD_ID, path));
    }

    private TxEnchantmentEvents() {
    }
}
