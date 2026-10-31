package com.skydeity.thaumicdyesexplor.event;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.skydeity.thaumicdyesexplor.ThaumicDyesExplor;
import com.skydeity.thaumicdyesexplor.registry.ModItems;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * 古代面具（TX Ancient Mask）的“凝视降雷”。
 *
 * <p>源模组在客户端 {@code LivingUpdateEvent} 里做视线拾取，盯住同一个生物越久就每 5 tick
 * 画一道 {@code FXLightningBolt}，同时发包给服务端扣血。1.21.1 的做法改成<b>服务端判定</b>：
 * 服务端射线拾取生物 → 累计“凝视 tick”→ 每 5 tick 造成一次闪电伤害，并用
 * {@code ELECTRIC_SPARK} 粒子在两点之间描出电弧（前置 API 未暴露 TC 的闪电特效类）。</p>
 */
@EventBusSubscriber(modid = ThaumicDyesExplor.MOD_ID)
public final class AncientMaskEvents {

    /** 射线长度（格）。 */
    private static final double REACH = 24.0D;
    /** 需要连续凝视多久才开始放电（tick）。 */
    private static final int CHARGE_TICKS = 10;
    /** 放电节拍（tick，源模组 {@code watchTicks % 5 == 0}）。 */
    private static final int ZAP_INTERVAL = 5;
    /** 每次闪电伤害。 */
    private static final float ZAP_DAMAGE = 2.0F;

    /** 玩家 -> [目标 entityId, 凝视 tick]。 */
    private static final Map<UUID, int[]> WATCH = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        if (helmet.getItem() != ModItems.ANCIENT_MASK.get()) {
            WATCH.remove(player.getUUID());
            return;
        }
        if (player.tickCount % ZAP_INTERVAL != 0) {
            return;
        }
        LivingEntity target = rayTarget(player);
        if (target == null) {
            WATCH.remove(player.getUUID());
            return;
        }
        int[] state = WATCH.computeIfAbsent(player.getUUID(), key -> new int[] { target.getId(), 0 });
        if (state[0] != target.getId()) {
            state[0] = target.getId();
            state[1] = 0;
        }
        state[1] += ZAP_INTERVAL;
        if (state[1] < CHARGE_TICKS) {
            return;
        }
        zap(player, target);
    }

    private static void zap(ServerPlayer player, LivingEntity target) {
        target.hurt(player.damageSources().indirectMagic(player, player), ZAP_DAMAGE);
        if (player.level() instanceof ServerLevel level) {
            Vec3 from = player.getEyePosition().add(0.0D, 0.75D, 0.0D);
            Vec3 to = new Vec3(target.getX(), target.getBoundingBox().maxY - 0.5D, target.getZ());
            Vec3 delta = to.subtract(from);
            int steps = Math.max(2, (int) (delta.length() * 4.0D));
            for (int i = 0; i <= steps; i++) {
                Vec3 point = from.add(delta.scale((double) i / steps));
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, point.x, point.y, point.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    /** 沿视线找最近的生物（跳过玩家自己）。 */
    private static LivingEntity rayTarget(ServerPlayer player) {
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getLookAngle().scale(REACH));
        AABB box = player.getBoundingBox().expandTowards(player.getLookAngle().scale(REACH)).inflate(1.0D);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(player, eye, end, box,
                entity -> entity instanceof LivingEntity && entity != player && entity.isAlive(), REACH * REACH);
        if (hit == null) {
            return null;
        }
        Entity entity = hit.getEntity();
        return entity instanceof LivingEntity living ? living : null;
    }

    private AncientMaskEvents() {
    }
}
