package com.skydeity.thaumicdyesexplor.event;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.skydeity.thaumicdyesexplor.ThaumicDyesExplor;
import com.skydeity.thaumicdyesexplor.item.TravellerBootsItem;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * 旅者靴 / 流星靴 / 彗星靴的移动行为。
 *
 * <p>源模组直接在 {@code onArmorTick} 里改 {@code player.stepHeight}、
 * {@code moveFlying(...)}、{@code fallDistance}、{@code jumpMovementFactor}。1.21.1 的等价映射：</p>
 * <table border="1">
 *   <caption>移动行为的移植对照</caption>
 *   <tr><td>{@code stepHeight = 1.0}</td><td>{@link Attributes#STEP_HEIGHT} 物品修饰符 +0.4</td></tr>
 *   <tr><td>{@code moveFlying(0, 1, bonus)}</td><td>{@link Attributes#MOVEMENT_SPEED} 乘算倍率</td></tr>
 *   <tr><td>{@code motionY += 0.275}</td><td>{@code LivingJumpEvent} 里给垂直速度补量</td></tr>
 *   <tr><td>{@code jumpMovementFactor}（悬浮分支）</td><td>{@link Attributes#JUMP_STRENGTH} 物品修饰符 +0.10（改为跳跃提升）</td></tr>
 *   <tr><td>{@code if (!isSneaking) stepHeight = 1.0}</td><td>属性做不到“潜行时失效”，改为潜行时 {@link Attributes#MOVEMENT_SPEED} +35%（改为速度加快）</td></tr>
 *   <tr><td>{@code fallDistance -= 0.35}</td><td>每 tick 直接削减 {@link net.minecraft.world.entity.Entity#fallDistance}</td></tr>
 *   <tr><td>流星砸地</td><td>落地瞬间按滞空 tick 数决定爆炸半径</td></tr>
 *   <tr><td>彗星结冰</td><td>移动时把脚下 11×11 的水换成融冰</td></tr>
 * </table>
 *
 * <p>源模组里“悬浮”（{@code Hover}）相关的 {@code jumpMovementFactor} 调整依赖
 * Thaumcraft 本体内部的悬浮状态，API 未暴露，故未移植。</p>
 */
@EventBusSubscriber(modid = ThaumicDyesExplor.MOD_ID)
public final class BootsEvents {

    private static final ResourceLocation STEP_ID = id("traveller_step");
    private static final ResourceLocation SPEED_ID = id("traveller_speed");
    private static final ResourceLocation JUMP_ID = id("traveller_jump");
    private static final ResourceLocation SNEAK_SPEED_ID = id("traveller_sneak_speed");

    /** 跳跃提升量（原版 {@code JUMP_STRENGTH} 基准 0.42，+0.10 ≈ 跳跃提升 I）。 */
    private static final double JUMP_STRENGTH_BONUS = 0.10D;

    /** 潜行时的移速加成（{@code ADD_MULTIPLIED_TOTAL}）。 */
    private static final double SNEAK_SPEED_BONUS = 0.35D;

    /** 虚空的缓降在源模组里比其他靴子更狠（-0.5）。 */
    private static final float FALL_REDUCTION_VOID = 0.5F;
    private static final float FALL_REDUCTION_DEFAULT = 0.35F;
    private static final float FALL_REDUCTION_RUNIC = 0.25F;

    /** 流星砸地的滞空 tick 阈值（源模组 smashTicks > 5 / 10 / 15）。 */
    private static final int SLAM_TIER_1 = 5;
    private static final int SLAM_TIER_2 = 10;
    private static final int SLAM_TIER_3 = 15;

    /** 彗星靴结冰半径（源模组 11×11，半径 5）。 */
    private static final int COMET_RADIUS = 5;
    /** 结冰节拍（每 4 tick 一次，避免每 tick 扫描）。 */
    private static final int COMET_INTERVAL = 4;

    private static final Map<java.util.UUID, Integer> SMASH_TICKS = new ConcurrentHashMap<>();

    // ============================================================================================
    // 跨步与移速
    // ============================================================================================
    @SubscribeEvent
    public static void onItemAttributeModifiers(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof TravellerBootsItem boots)) {
            return;
        }
        TravellerBootsItem.Kind kind = boots.kind();
        EquipmentSlotGroup group = EquipmentSlotGroup.FEET;
        event.addModifier(Attributes.STEP_HEIGHT,
                new AttributeModifier(STEP_ID, kind.stepBonus(), AttributeModifier.Operation.ADD_VALUE), group);
        // 悬浮分支的替代：跳跃提升
        event.addModifier(Attributes.JUMP_STRENGTH,
                new AttributeModifier(JUMP_ID, JUMP_STRENGTH_BONUS, AttributeModifier.Operation.ADD_VALUE), group);
        if (kind.speedMultiplier() > 0.0D) {
            event.addModifier(Attributes.MOVEMENT_SPEED,
                    new AttributeModifier(SPEED_ID, kind.speedMultiplier(),
                            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL), group);
        }
    }

    // ============================================================================================
    // 跳跃加成 + 流星冲刺
    // ============================================================================================
    @SubscribeEvent
    public static void onJump(LivingEvent.LivingJumpEvent event) {
        LivingEntity entity = event.getEntity();
        TravellerBootsItem boots = wornBoots(entity);
        if (boots == null) {
            return;
        }
        TravellerBootsItem.Kind kind = boots.kind();
        Vec3 motion = entity.getDeltaMovement();
        entity.setDeltaMovement(motion.x, motion.y + kind.jumpBonus(), motion.z);

        // 流星靴：潜行起跳 = 沿视线冲刺（源模组 motionX/Z += (jump+1)*vector*4）
        if (kind.slam() && entity.isShiftKeyDown()) {
            Vec3 look = entity.getLookAngle();
            entity.setDeltaMovement(
                    motion.x + look.x * 4.0D,
                    motion.y + look.y / 1.5D,
                    motion.z + look.z * 4.0D);
            entity.hurtMarked = true;
        }
    }

    // ============================================================================================
    // 坠落减免 / 流星砸地 / 彗星结冰
    // ============================================================================================
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        TravellerBootsItem boots = wornBoots(player);
        if (boots == null) {
            SMASH_TICKS.remove(player.getUUID());
            return;
        }
        TravellerBootsItem.Kind kind = boots.kind();

        // 坠落距离削减：源模组每 tick 直接减 fallDistance
        if (player.fallDistance > 0.0F) {
            player.fallDistance = Math.max(0.0F, player.fallDistance - fallReduction(kind));
        }

        // 跨步“潜行时失效”的替代：潜行时反而提速。
        // 属性修饰符没法随潜行开关，所以用“临时修饰符”：它会在本 tick 结束时被自动清除，
        // 因此只要潜行就每 tick 补一条，松开潜行自然失效。
        if (player.isShiftKeyDown()) {
            AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speed != null) {
                speed.addOrUpdateTransientModifier(new AttributeModifier(SNEAK_SPEED_ID, SNEAK_SPEED_BONUS,
                        AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            }
        }

        if (player.level().isClientSide()) {
            return;
        }

        if (kind.slam()) {
            tickSmash(player);
        }
        if (kind == TravellerBootsItem.Kind.COMET && player.tickCount % COMET_INTERVAL == 0) {
            freezeWater(player);
        }
    }

    private static float fallReduction(TravellerBootsItem.Kind kind) {
        return switch (kind) {
            case VOID -> FALL_REDUCTION_VOID;
            case RUNIC_TRAVELLER -> FALL_REDUCTION_RUNIC;
            default -> FALL_REDUCTION_DEFAULT;
        };
    }

    /** 流星靴砸地：滞空越久，落地爆炸越大。 */
    private static void tickSmash(Player player) {
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        if (player.onGround()) {
            Integer ticks = SMASH_TICKS.remove(player.getUUID());
            if (ticks == null || ticks <= SLAM_TIER_1) {
                return;
            }
            float radius = ticks > SLAM_TIER_3 ? 3.0F : (ticks > SLAM_TIER_2 ? 2.0F : 1.0F);
            Level level = player.level();
            level.explode(player, player.getX(), player.getY(), player.getZ(), radius,
                    Level.ExplosionInteraction.NONE);
            // 砸一次掉一点耐久（源模组也是这么消耗的）
            if (boots.isDamageableItem()) {
                boots.hurtAndBreak(1, player, EquipmentSlot.FEET);
            }
            return;
        }
        SMASH_TICKS.merge(player.getUUID(), 1, Integer::sum);
    }

    /** 彗星靴：把脚下的水面换成融冰（TC 的 melty ice，缺失时退化用原版霜冰）。 */
    private static void freezeWater(Player player) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }
        Block meltyIce = meltyIce();
        if (meltyIce == null) {
            return;
        }
        BlockPos center = player.blockPosition();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -COMET_RADIUS; dx <= COMET_RADIUS; dx++) {
            for (int dz = -COMET_RADIUS; dz <= COMET_RADIUS; dz++) {
                for (int dy = -1; dy <= 0; dy++) {
                    cursor.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                    BlockState state = level.getBlockState(cursor);
                    if (state.is(Blocks.WATER) && level.getBlockState(cursor.above()).isAir()) {
                        level.setBlockAndUpdate(cursor, meltyIce.defaultBlockState());
                    }
                }
            }
        }
    }

    private static Block cachedMeltyIce;
    private static boolean meltyIceResolved;

    /**
     * 找 TC 的「融冰」方块。
     *
     * <p><b>必须走注册表查询并吞掉异常</b>：TC4R 的 {@code ThaumcraftContent.block(...)}
     * 在方块不存在时会直接 {@code IllegalArgumentException}（它内部是
     * {@code optional.orElseThrow}），而这段代码跑在 {@code PlayerTickEvent} 里 ——
     * 一个异常就会顺着 {@code Player#tick} 冒上去，把整个服务器带崩。
     * 1.21.1 的 TC4R 并没有移植 {@code melty_ice}，所以这里查不到就退化成原版霜冰
     * （{@code minecraft:frosted_ice}，踩一会儿会自己化掉，观感最接近）。</p>
     */
    private static Block meltyIce() {
        if (!meltyIceResolved) {
            meltyIceResolved = true;
            cachedMeltyIce = resolveBlock("melty_ice");
            if (cachedMeltyIce == null) {
                cachedMeltyIce = Blocks.FROSTED_ICE;
            }
        }
        return cachedMeltyIce;
    }

    /** 安全地按 id 取方块；不存在或注册表未就绪时返回 {@code null}，绝不抛异常。 */
    private static Block resolveBlock(String path) {
        try {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath("thaumcraft", path);
            if (BuiltInRegistries.BLOCK.containsKey(id)) {
                return BuiltInRegistries.BLOCK.get(id);
            }
        } catch (RuntimeException ignored) {
            // 注册表还没准备好 / 前置版本不认这个 id：一律当作“没有”，走退化分支。
        }
        return null;
    }

    private static TravellerBootsItem wornBoots(LivingEntity entity) {
        ItemStack stack = entity.getItemBySlot(EquipmentSlot.FEET);
        return stack.getItem() instanceof TravellerBootsItem boots ? boots : null;
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ThaumicDyesExplor.MOD_ID, path);
    }

    private BootsEvents() {
    }
}
