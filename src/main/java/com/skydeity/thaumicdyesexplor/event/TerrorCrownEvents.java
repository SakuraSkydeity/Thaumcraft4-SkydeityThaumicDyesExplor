package com.skydeity.thaumicdyesexplor.event;

import java.util.List;

import com.skydeity.thaumicdyesexplor.ThaumicDyesExplor;
import com.skydeity.thaumicdyesexplor.registry.ModRelicItems;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * 恐惧王冠（Forgotten Relics {@code ItemTerrorCrown}）的领域效果。
 *
 * <h2>源模组行为</h2>
 * <p>{@code onArmorTick} 每 tick 做两件事：</p>
 * <ol>
 *   <li>{@code SuperpositionHandler.cryHavoc(world, player, 24)} —— 也就是植物魔法
 *       「平成之梦」的效果：把半径 24 格内所有 {@code IMob} 两两配对，让它们互相攻击
 *       （源码注释自己写着 “Basically, does the same thing as Heisei Dream”）；</li>
 *   <li>视线拾取 32 格内第一个生物，给它上致盲 100t/2、凋零 40t/0、反胃 100t/1、
 *       缓慢 30t/1、虚弱 80t/2（{@code EntityUtils.getPointedEntity}）。</li>
 * </ol>
 *
 * <h2>1.21.1 的做法</h2>
 * <ul>
 *   <li>{@code IMob} 在 1.21 里没有对应接口：这里取 {@link Monster}（敌对生物），
 *       排除 {@link Enemy} 里的 Boss（凋灵/末影龙监守者），随机两两 {@code setTarget}，
 *       玩家因此会看到周围的怪互相打起来；20 tick 一次，避免每 tick 覆写 AI 目标；</li>
 *   <li>视线部分沿用 {@code AncientMaskEvents} 的服务端射线，10 tick 一次
 *       （药水是按持续时间刷新的，不必每 tick 发）。</li>
 * </ul>
 *
 * <p>揭示（{@code aura_revealing}）不在这里 —— 那是数据表的静态能力，见
 * {@code data/thaumcraft/data_maps/item/equipment_abilities.json}。</p>
 */
@EventBusSubscriber(modid = ThaumicDyesExplor.MOD_ID)
public final class TerrorCrownEvents {

    /** 挑拨半径（源模组 cryHavoc 的 RANGE）。 */
    private static final int HAVOC_RANGE = 24;
    /** 挑拨节拍（tick）。 */
    private static final int HAVOC_INTERVAL = 20;
    /** 视线长度（源模组 getPointedEntity 的 32 格）。 */
    private static final double GAZE_REACH = 32.0D;
    /** 凝视施加节拍（tick）。 */
    private static final int GAZE_INTERVAL = 10;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        if (helmet.getItem() != ModRelicItems.TERROR_CROWN.get()) {
            return;
        }
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }
        if (player.tickCount % HAVOC_INTERVAL == 0) {
            cryHavoc(level, player);
        }
        if (player.tickCount % GAZE_INTERVAL == 0) {
            terrorize(level, player);
        }
    }

    /**
     * 「平成之梦」：半径 24 格内的敌对生物两两结对，改成互相攻击。
     *
     * <p>源码的 {@code brainwashEntity} 也是随机挑一个同伴当目标；这里额外跳过
     * Boss（它们对 {@code setTarget} 不感兴趣，而且让凋灵打末影龙没有意义）。</p>
     */
    private static void cryHavoc(ServerLevel level, ServerPlayer player) {
        AABB box = player.getBoundingBox().inflate(HAVOC_RANGE);
        List<Monster> mobs = level.getEntitiesOfClass(Monster.class, box,
                mob -> mob.isAlive() && !(mob instanceof Enemy));
        if (mobs.size() < 2) {
            return;
        }
        for (Monster mob : mobs) {
            Monster other = pickOther(mobs, mob, player.getRandom().nextInt(mobs.size()));
            if (other != null) {
                mob.setTarget(other);
                mob.setLastHurtByMob(other);
            }
        }
    }

    /** 从列表里挑一个不是自己的目标（源模组也是随机挑，挑到自己就跳过）。 */
    private static Monster pickOther(List<Monster> mobs, Monster self, int start) {
        for (int i = 0; i < mobs.size(); i++) {
            Monster candidate = mobs.get((start + i) % mobs.size());
            if (candidate != self) {
                return candidate;
            }
        }
        return null;
    }

    /** 凝视降咒：致盲 / 凋零 / 反胃 / 缓慢 / 虚弱（数值与源模组一致）。 */
    private static void terrorize(ServerLevel level, ServerPlayer player) {
        LivingEntity target = rayTarget(player);
        if (target == null) {
            return;
        }
        target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 2, true, true));
        if (!target.hasEffect(MobEffects.WITHER)) {
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, 40, 0, false, true));
        }
        target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 1, true, true));
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 1, true, true));
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 2, true, true));
    }

    /** 沿视线找最近的生物（跳过玩家自己）。 */
    private static LivingEntity rayTarget(ServerPlayer player) {
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getLookAngle().scale(GAZE_REACH));
        AABB box = player.getBoundingBox().expandTowards(player.getLookAngle().scale(GAZE_REACH)).inflate(1.0D);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(player, eye, end, box,
                entity -> entity instanceof LivingEntity && entity != player && entity.isAlive(),
                GAZE_REACH * GAZE_REACH);
        if (hit == null) {
            return null;
        }
        Entity entity = hit.getEntity();
        return entity instanceof LivingEntity living ? living : null;
    }

    private TerrorCrownEvents() {
    }
}
