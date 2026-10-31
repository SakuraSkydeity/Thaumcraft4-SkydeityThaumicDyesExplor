package com.skydeity.thaumicdyesexplor.event;

import com.skydeity.thaumicdyesexplor.ThaumicDyesExplor;
import com.skydeity.thaumicdyesexplor.curio.CurioSupport;
import com.skydeity.thaumicdyesexplor.registry.ModEffects;
import com.skydeity.thaumicdyesexplor.registry.ModItems;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * 饰品的被动逻辑：腐化之戒、捕梦网、触手之戒。
 *
 * <p>营养护符与注魔稳定腰带靠 Curios 的 {@code curioTick} 与属性修饰符自行工作，
 * 不在这里；这些饰品需要「全局事件」（受伤、每 tick 判定群系），所以集中放这里。</p>
 */
@EventBusSubscriber(modid = ThaumicDyesExplor.MOD_ID)
public final class CurioEvents {

    /** TC4R 的腐化群系。 */
    private static final ResourceKey<Biome> TAINTED_LAND =
            ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("thaumcraft", "tainted_land"));

    /** TC4R 的腐化生物实体标签。 */
    private static final TagKey<EntityType<?>> TAINTED_ENTITIES = TagKey.create(
            Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath("thaumcraft", "tainted"));

    /** TC4R 的「腐化」负面效果（TC4 里的 Thaumarhia）。 */
    private static final ResourceLocation THAUMARHIA_ID =
            ResourceLocation.fromNamespaceAndPath("thaumcraft", "thaumarhia");

    private static final String KEY_WORN = "thaumicdyesexplor:taint_ring_worn";
    private static final String KEY_GRACE = "thaumicdyesexplor:taint_ring_grace";
    private static final String KEY_FOOD_BUFF = "thaumicdyesexplor:taint_ring_food";

    /** 离开腐化群系后多少 tick 开始戒断。 */
    private static final int WITHDRAWAL_THRESHOLD = 100;

    /** 腐化群系内补 1 点饥饿需要累积的 tick。 */
    private static final int TAINT_FEED_TICKS = 80;

    private CurioEvents() {
    }

    // ============================================================================================
    // 腐化之戒
    // ============================================================================================

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() || !player.isAlive()) {
            return;
        }
        boolean wearing = CurioSupport.isEquipped(player, ModItems.CHARM_TAINT.get());
        CompoundTag data = player.getPersistentData();
        if (!data.contains(KEY_WORN)) {
            // 第一次见到这名玩家：先把当前状态记下来，避免「刚戴上就暴毙」。
            data.putBoolean(KEY_WORN, wearing);
        }
        boolean wasWearing = data.getBoolean(KEY_WORN);
        if (wasWearing && !wearing) {
            data.putBoolean(KEY_WORN, false);
            data.remove(KEY_GRACE);
            data.remove(KEY_FOOD_BUFF);
            if (!player.isCreative() && !player.isSpectator()) {
                // 源模组语义：腐化之戒是摘不下来的诅咒物，强行取下就是死。
                player.hurt(player.damageSources().genericKill(), 999.0F);
            }
            return;
        }
        if (!wasWearing && wearing) {
            data.putBoolean(KEY_WORN, true);
            data.remove(KEY_GRACE);
            data.remove(KEY_FOOD_BUFF);
            player.level().playSound(null, player.blockPosition(),
                    net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_CHIME,
                    net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 0.6F);
        }
        if (!wearing) {
            return;
        }

        // 戴着戒指时免疫「腐化」效果本身。
        clearThaumarhia(player);

        boolean inTaint = player.level().getBiome(player.blockPosition()).is(TAINTED_LAND);
        if (inTaint) {
            // 回到腐化之地：戒断停止，饿到 4 点以下时慢慢「吃腐化」续命。
            data.remove(KEY_GRACE);
            if (player.getFoodData().getFoodLevel() < 4) {
                int buff = data.getInt(KEY_FOOD_BUFF) + 1;
                if (buff > TAINT_FEED_TICKS) {
                    player.getFoodData().setFoodLevel(player.getFoodData().getFoodLevel() + 1);
                    buff = 0;
                }
                data.putInt(KEY_FOOD_BUFF, buff);
            } else {
                data.putInt(KEY_FOOD_BUFF, 0);
            }
            return;
        }

        // 在腐化之地以外戴着：戒断倒计时。
        data.remove(KEY_FOOD_BUFF);
        int grace = data.getInt(KEY_GRACE) + 1;
        data.putInt(KEY_GRACE, grace);
        if (grace > WITHDRAWAL_THRESHOLD) {
            data.putInt(KEY_GRACE, 0);
            if (!player.hasEffect(ModEffects.TAINT_WITHDRAWAL)) {
                player.addEffect(new MobEffectInstance(ModEffects.TAINT_WITHDRAWAL, 100, 1));
            }
        }
    }

    private static void clearThaumarhia(Player player) {
        Holder<MobEffect> effect = BuiltInRegistries.MOB_EFFECT.getHolder(THAUMARHIA_ID).orElse(null);
        if (effect != null) {
            player.removeEffect(effect);
        }
    }

    // ============================================================================================
    // 捕梦网 + 触手之戒
    // ============================================================================================

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide()) {
            return;
        }
        // 捕梦网：3/4 概率挡下腐化生物与腐化/触手伤害。
        if (player.getRandom().nextInt(4) < 3
                && CurioSupport.isEquipped(player, ModItems.CHARM_NO_TAINT.get())
                && isTaintHarm(event)) {
            event.setCanceled(true);
            return;
        }
        // 触手之戒：站在地面上被攻击时，就近召唤一只触手帮手。
        if (player.onGround() && CurioSupport.isEquipped(player, ModItems.TENTACLE_RING.get())) {
            summonTaintacle(player);
        }
    }

    private static boolean isTaintHarm(LivingIncomingDamageEvent event) {
        if (event.getSource().is(dev.tc4port.thaumcraft.api.damage.ThaumcraftDamageTypes.FLUX_TAINT)
                || event.getSource().is(dev.tc4port.thaumcraft.api.damage.ThaumcraftDamageTypes.TENTACLE)) {
            return true;
        }
        Entity attacker = event.getSource().getEntity();
        return attacker instanceof LivingEntity living
                && living.getType().builtInRegistryHolder().is(TAINTED_ENTITIES);
    }

    private static void summonTaintacle(Player player) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE
                .get(ResourceLocation.fromNamespaceAndPath("thaumcraft", "taintacle"));
        if (type == null) {
            return;
        }
        Vec3 spot = findSafeSpot(player);
        if (spot == null) {
            return;
        }
        Entity spawned = type.create(level);
        if (spawned == null) {
            return;
        }
        spawned.moveTo(spot.x, spot.y, spot.z, player.getYRot(), 0.0F);
        level.addFreshEntity(spawned);
        level.playSound(null, player.blockPosition(),
                net.minecraft.sounds.SoundEvents.SLIME_BLOCK_PLACE,
                net.minecraft.sounds.SoundSource.PLAYERS, 0.8F, 0.6F);
    }

    /** 在玩家周围 ±2 格里找一个「脚下是实心方块」的位置。 */
    private static Vec3 findSafeSpot(Player player) {
        Level level = player.level();
        BlockPos origin = player.blockPosition();
        Vec3 best = null;
        int safeCount = 0;
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (dx == 0 && dz == 0) {
                    continue;
                }
                BlockPos ground = origin.offset(dx, -1, dz);
                if (!level.getBlockState(ground).isSolidRender(level, ground)) {
                    continue;
                }
                safeCount++;
                // 蓄水池抽样，避免为了随机选点而把所有候选存下来。
                if (player.getRandom().nextInt(safeCount) == 0) {
                    best = new Vec3(ground.getX() + 0.5D, ground.getY() + 1.0D, ground.getZ() + 0.5D);
                }
            }
        }
        return best;
    }
}
