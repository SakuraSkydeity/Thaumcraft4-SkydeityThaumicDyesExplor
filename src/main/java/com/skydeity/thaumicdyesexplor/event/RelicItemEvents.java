package com.skydeity.thaumicdyesexplor.event;

import java.util.List;
import java.util.Map;

import com.skydeity.thaumicdyesexplor.ThaumicDyesExplor;
import com.skydeity.thaumicdyesexplor.entity.RelicProjectileEntity;
import com.skydeity.thaumicdyesexplor.registry.ModRelicItems;
import com.skydeity.thaumicdyesexplor.vis.WandVis;

import dev.tc4port.thaumcraft.api.aspect.VisAction;
import dev.tc4port.thaumcraft.api.aspect.VisChannel;
import dev.tc4port.thaumcraft.api.aspect.VisCost;
import dev.tc4port.thaumcraft.api.vis.VisAmuletItem;
import dev.tc4port.thaumcraft.api.wand.WandApi;
import dev.tc4port.thaumcraft.api.wand.WandCarrierItem;
import dev.tc4port.thaumcraft.api.wand.WandView;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Forgotten Relics 全部「拿在手上右键 / 放在背包里被动」遗物的行为。
 *
 * <p>源模组每件遗物是一个独立 Item 类，这里统一成事件分发：右键一个 switch，
 * 被动放 {@link #onPlayerTick}、伤害改写放 {@link #onIncomingDamage}。
 * 需要装进 Curios 槽位才生效的（戒指 / 护符 / 腰带）在 {@code curio/RelicCurios}。</p>
 *
 * <p><b>vis 全部从玩家背包里第一把能用的法杖扣</b>；扣不起就什么都不做并提示，
 * 不会出现「放了技能但没扣 vis」的静默失败（第 19 轮修）。</p>
 */
@EventBusSubscriber(modid = ThaumicDyesExplor.MOD_ID)
public final class RelicItemEvents {

    /** 一次施法的默认 vis（centivis，六元要素均摊）。 */
    private static final int CAST_COST = 2000;

    private static final List<VisChannel> PRIMALS = List.of(
            VisChannel.AER, VisChannel.TERRA, VisChannel.IGNIS,
            VisChannel.AQUA, VisChannel.ORDO, VisChannel.PERDITIO);

    /** 混沌核心随机喂的药水（源模组从药水 id 1~21 里抽，这里挑同量级的原版效果）。 */
    private static final List<Holder<MobEffect>> CHAOS_EFFECTS = List.of(
            MobEffects.MOVEMENT_SPEED, MobEffects.MOVEMENT_SLOWDOWN, MobEffects.DIG_SPEED,
            MobEffects.DIG_SLOWDOWN, MobEffects.DAMAGE_BOOST, MobEffects.JUMP,
            MobEffects.CONFUSION, MobEffects.REGENERATION, MobEffects.DAMAGE_RESISTANCE,
            MobEffects.FIRE_RESISTANCE, MobEffects.WATER_BREATHING, MobEffects.INVISIBILITY,
            MobEffects.BLINDNESS, MobEffects.NIGHT_VISION, MobEffects.HUNGER,
            MobEffects.WEAKNESS, MobEffects.POISON, MobEffects.WITHER,
            MobEffects.HEALTH_BOOST, MobEffects.ABSORPTION, MobEffects.SATURATION);

    // ============================================================================================
    // 右键：施法 / 传动 / 放逐 / 唤醒
    // ============================================================================================

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickItem event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide()) {
            return;
        }
        ItemStack stack = event.getItemStack();
        String id = builtId(stack);
        if (id == null) {
            return;
        }
        ServerLevel level = player.serverLevel();
        InteractionHand hand = event.getHand();
        boolean sneak = player.isShiftKeyDown();
        boolean cast = switch (id) {
            case "reli_missile_tome" -> shoot(level, player, RelicProjectileEntity.Variant.RAGEOUS, 1.8F);
            case "reli_crimson_spell" -> shoot(level, player, RelicProjectileEntity.Variant.CRIMSON, 1.6F);
            case "reli_eldritch_spell" -> shoot(level, player, RelicProjectileEntity.Variant.CHAOS, 1.5F);
            case "reli_ghastly_skull" -> shoot(level, player, RelicProjectileEntity.Variant.SOUL, 1.2F);
            case "reli_lunar_flares" -> rainOfFlares(level, player);
            case "reli_chaos_tome" -> shoot(level, player, RelicProjectileEntity.Variant.CHAOS, 1.2F);
            case "reli_soul_tome" -> shoot(level, player, RelicProjectileEntity.Variant.SOUL, 1.8F);
            case "reli_void_grimoire" -> shoot(level, player, RelicProjectileEntity.Variant.DARK_MATTER, 1.1F);
            case "reli_thunderpeal" -> strikeLightning(level, player);
            case "reli_teleportation_tome" -> blink(level, player, true);
            case "reli_dimensional_mirror" -> returnToSpawn(level, player);
            case "reli_apotheosis" -> selfBlessing(player);
            case "reli_obelisk_drainer" -> drainNode(level, player);
            // 第 19 轮补齐：以前这 7 件右键完全没反应
            case "reli_dormant_arcanum" -> awakenArcanum(player, hand);
            case "reli_arcanum", "reli_omega_core", "reli_oblivion_stone" -> transferVis(player, stack, sneak);
            case "reli_weather_stone" -> clearWeather(level, player);
            case "reli_overthrower" -> banish(level, player);
            case "reli_telekinesis_tome" -> telekineticGrab(level, player);
            case "reli_discord_ring" -> discordBlink(level, player);
            default -> false;
        };
        if (cast) {
            player.getCooldowns().addCooldown(stack.getItem(), 20);
            event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
            event.setCanceled(true);
        }
    }

    // ============================================================================================
    // 被动：混沌核心（背包里随机喂药水）
    // ============================================================================================

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide()) {
            return;
        }
        if (player.tickCount % 20 != 0 || player.getRandom().nextDouble() > 0.004) {
            return;
        }
        if (!carries(player, ModRelicItems.CHAOS_CORE.get())) {
            return;
        }
        Holder<MobEffect> effect = CHAOS_EFFECTS.get(player.getRandom().nextInt(CHAOS_EFFECTS.size()));
        player.addEffect(new MobEffectInstance(effect,
                100 + player.getRandom().nextInt(2400), player.getRandom().nextInt(3), false, true));
    }

    // ============================================================================================
    // 被动：恐惧王冠（戴在头上时诅咒准星里的生物）
    // ============================================================================================

    /**
     * 恐惧王冠（源 {@code ItemTerrorCrown}）：戴在头盔位时，准星 32 格内的生物持续中
     * 失明 / 凋零 / 反胃 / 缓慢 / 虚弱（源模组 {@code onArmorTick}）。
     *
     * <p>源模组对任何 {@code EntityLivingBase} 生效，包括其他玩家；这里只作用于非玩家生物，
     * 避免多人服里变成「戴个帽子就能隔墙诅咒别人」的挂。灵气显示（{@code aura_revealing}）
     * 走 {@code equipment_abilities} 数据表，不需要在这里重复。</p>
     */
    @SubscribeEvent
    public static void onTerrorCrownTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || player.level().isClientSide() || player.tickCount % 20 != 0) {
            return;
        }
        if (!player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD)
                .is(ModRelicItems.TERROR_CROWN.get())) {
            return;
        }
        LivingEntity target = lookingAt(player, 32.0, 0.90);
        if (target == null || target instanceof Player) {
            return;
        }
        target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 2, true, true));
        target.addEffect(new MobEffectInstance(MobEffects.WITHER, 40, 0, true, true));
        target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 1, true, true));
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 1, true, true));
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 2, true, true));
    }

    // ============================================================================================
    // 被动：虚妄正义（双方伤害翻倍）、混沌核心（受伤 0~2 倍）
    // ============================================================================================

    @SubscribeEvent
    public static void onIncomingDamage(net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent event) {
        var source = event.getSource();
        if (isAbsolute(source)) {
            return;
        }
        float amount = event.getAmount();
        boolean changed = false;

        // 虚妄正义：持有者受到的伤害与造成的伤害都翻倍（源模组先取消再按真伤 ×2 结算）
        boolean attackerHasJustice = source.getEntity() instanceof Player attacker
                && attacker != event.getEntity() && carries(attacker, ModRelicItems.FALSE_JUSTICE.get());
        boolean victimHasJustice = event.getEntity() instanceof Player victim
                && carries(victim, ModRelicItems.FALSE_JUSTICE.get());
        if (victimHasJustice || attackerHasJustice) {
            amount *= 2.0F;
            changed = true;
        }

        // 混沌核心：持有者受到的伤害 × 0~2 的随机数
        if (event.getEntity() instanceof Player victim2 && carries(victim2, ModRelicItems.CHAOS_CORE.get())) {
            amount *= victim2.getRandom().nextFloat() * 2.0F;
            changed = true;
        }

        if (changed) {
            event.setAmount(amount);
        }
    }

    /**
     * 源模组的「绝对伤害」：虚空、/kill 这类跳过所有减伤与改写的伤害。
     * 遗物效果对它们一律不生效（否则可以被虚妄正义翻倍成秒杀）。
     */
    private static boolean isAbsolute(net.minecraft.world.damagesource.DamageSource source) {
        return source.is(net.minecraft.world.damagesource.DamageTypes.FELL_OUT_OF_WORLD)
                || source.is(net.minecraft.world.damagesource.DamageTypes.GENERIC_KILL);
    }

    // ============================================================================================
    // 各法书的实现
    // ============================================================================================

    private static boolean shoot(ServerLevel level, ServerPlayer player,
                                 RelicProjectileEntity.Variant variant, float speed) {
        if (!payVis(player, evenCost(CAST_COST))) {
            return false;
        }
        RelicProjectileEntity orb = new RelicProjectileEntity(level, player, variant);
        orb.setVariant(variant);
        orb.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, speed, 1.0F);
        level.addFreshEntity(orb);
        level.playSound(null, player.blockPosition(), SoundEvents.BLAZE_SHOOT,
                net.minecraft.sounds.SoundSource.PLAYERS, 0.6F, 1.6F);
        return true;
    }

    /** 月辉之书：朝视线落点周围降下 5 发月辉弹。 */
    private static boolean rainOfFlares(ServerLevel level, ServerPlayer player) {
        var hit = player.pick(48.0, 1.0F, false);
        BlockPos base = BlockPos.containing(hit.getLocation());
        if (!payVis(player, evenCost(CAST_COST * 2))) {
            return false;
        }
        for (int i = 0; i < 5; i++) {
            RelicProjectileEntity orb = new RelicProjectileEntity(level, player,
                    RelicProjectileEntity.Variant.LUNAR_FLARE);
            double dx = (level.random.nextDouble() - 0.5) * 6;
            double dz = (level.random.nextDouble() - 0.5) * 6;
            orb.setPos(base.getX() + dx, base.getY() + 12, base.getZ() + dz);
            orb.setDeltaMovement(0, -0.8, 0);
            level.addFreshEntity(orb);
        }
        return true;
    }

    private static boolean strikeLightning(ServerLevel level, ServerPlayer player) {
        var hit = player.pick(48.0, 1.0F, false);
        if (!payVis(player, evenCost(CAST_COST))) {
            return false;
        }
        var bolt = EntityType.LIGHTNING_BOLT.create(level);
        if (bolt != null) {
            bolt.moveTo(hit.getLocation().x, hit.getLocation().y, hit.getLocation().z, 0.0F, 0.0F);
            level.addFreshEntity(bolt);
        }
        return true;
    }

    /** 纷争之书：传送到视线 48 格内的落点。 */
    private static boolean blink(ServerLevel level, ServerPlayer player, boolean charge) {
        if (charge && !payVis(player, evenCost(CAST_COST))) {
            return false;
        }
        var hit = player.pick(48.0, 1.0F, false);
        BlockPos pos = BlockPos.containing(hit.getLocation());
        player.teleportTo(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
                player.getYRot(), player.getXRot());
        level.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT,
                net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
        return true;
    }

    /** 位面之镜：回到出生点 / 床。 */
    private static boolean returnToSpawn(ServerLevel level, ServerPlayer player) {
        if (!payVis(player, evenCost(CAST_COST * 2))) {
            return false;
        }
        var respawn = player.getRespawnPosition();
        ServerLevel targetLevel = respawn == null ? level
                : player.getServer().getLevel(player.getRespawnDimension());
        if (respawn == null) {
            BlockPos spawn = targetLevel.getSharedSpawnPos();
            player.teleportTo(targetLevel, spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5,
                    player.getYRot(), player.getXRot());
        } else {
            player.teleportTo(targetLevel, respawn.getX() + 0.5, respawn.getY(), respawn.getZ() + 0.5,
                    player.getYRot(), player.getXRot());
        }
        return true;
    }

    /** 封神之作：给自身 absorption + regeneration + resistance 短 buff。 */
    private static boolean selfBlessing(ServerPlayer player) {
        if (!payVis(player, evenCost(CAST_COST))) {
            return false;
        }
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 600, 2));
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 600, 0));
        return true;
    }

    /** 虚空吞噬者：对着灵气节点方块右键，把节点 vis 抽进背包里的法杖。 */
    private static boolean drainNode(ServerLevel level, ServerPlayer player) {
        ItemStack wand = findWand(player);
        if (wand.isEmpty()) {
            noWand(player);
            return false;
        }
        var hit = player.pick(16.0, 1.0F, false);
        BlockPos pos = BlockPos.containing(hit.getLocation());
        var source = dev.tc4port.thaumcraft.api.node.NodeApi.wandSource(level, pos);
        if (source != null && source.canTapIntoWand() && payVis(player, evenCost(500))) {
            boolean ok = source.tapIntoWand(player, wand);
            if (ok) {
                level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME,
                        net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 0.6F);
                return true;
            }
        }
        return false;
    }

    // ============================================================================================
    // 第 19 轮补齐的七件
    // ============================================================================================

    /**
     * 沉睡的星云核心 → 星云核心：右手拿着右键，付 20 vis（六元要素各 20）唤醒。
     * 源模组是「戴上后每 tick 吸 vis 攒够才醒」，这里压成一次性付费。
     */
    private static boolean awakenArcanum(ServerPlayer player, InteractionHand hand) {
        if (!payVis(player, evenCost(20 * 100 * 6))) {
            return false;
        }
        player.setItemInHand(hand, new ItemStack(ModRelicItems.ARCANUM.get()));
        player.level().playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME,
                net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 0.5F);
        player.displayClientMessage(Component.translatable("msg.thaumicdyesexplor.arcanum.awakened"), true);
        return true;
    }

    /**
     * 三件 vis 储具（星云核心 / 欧米茄核心 / 遗忘之基石）的充放：
     * 右键把法杖里的 vis 灌进遗物，潜行右键把遗物的 vis 倒回法杖。
     *
     * <p>前置的 {@link VisAmuletItem} 自己只有「戴在 Curios 里被动收放」，
     * 没有任何右键交互，所以手拿遗物右键是没反应的 —— 这就是玩家反馈「用不了」的原因。</p>
     */
    private static boolean transferVis(ServerPlayer player, ItemStack stack, boolean drain) {
        if (!(stack.getItem() instanceof VisAmuletItem amulet)) {
            return false;
        }
        ItemStack wand = findWand(player);
        if (wand.isEmpty()) {
            noWand(player);
            return false;
        }
        WandView view = WandApi.view(wand).orElse(null);
        if (view == null) {
            noWand(player);
            return false;
        }
        boolean moved = false;
        for (VisChannel channel : PRIMALS) {
            int stored = amulet.vis(stack, channel);
            if (drain) {
                int room = view.capacityCentivis().getOrDefault(channel, 0)
                        - view.visCentivis().getOrDefault(channel, 0);
                int amount = Math.min(stored, Math.max(0, room));
                if (amount > 0 && amulet.consume(stack, Map.of(channel, amount))) {
                    WandApi.insert(player, wand, VisCost.ofCentivis(channel, amount), VisAction.EXECUTE);
                    moved = true;
                }
            } else {
                int held = view.visCentivis().getOrDefault(channel, 0);
                int amount = Math.min(amulet.maximumVis() - stored, held);
                if (amount <= 0) {
                    continue;
                }
                var result = WandApi.extract(player, wand, VisCost.ofCentivis(channel, amount), VisAction.EXECUTE);
                int got = result.transferred(channel);
                if (got > 0) {
                    amulet.add(stack, channel, got);
                    moved = true;
                }
            }
        }
        if (moved) {
            player.level().playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME,
                    net.minecraft.sounds.SoundSource.PLAYERS, 0.8F, drain ? 0.7F : 1.4F);
        }
        return moved;
    }

    /**
     * 符文石：下雨时右键，付 25 风 / 25 土 / 25 水 vis 让雨停下来
     * （源模组要按住右键蓄力 3 秒，这里直接触发）。
     */
    private static boolean clearWeather(ServerLevel level, ServerPlayer player) {
        if (!level.isRaining()) {
            player.displayClientMessage(Component.translatable("msg.thaumicdyesexplor.weather_stone.no_rain"), true);
            return false;
        }
        if (!payVis(player, Map.of(VisChannel.AER, 2500, VisChannel.TERRA, 2500, VisChannel.AQUA, 2500))) {
            return false;
        }
        level.setWeatherParameters(24000 + player.getRandom().nextInt(24000), 0, false, false);
        level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME,
                net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 0.8F);
        return true;
    }

    /**
     * 永恒放逐咒令：对着生物右键，把它扔到地狱里随机 2000 格内
     * （源模组是 10000 格，1.21.1 里跨维度太远会生成一堆区块，缩到 2000）。
     */
    private static boolean banish(ServerLevel level, ServerPlayer player) {
        LivingEntity target = lookingAt(player, 32.0, 0.90);
        if (target == null) {
            player.displayClientMessage(Component.translatable("msg.thaumicdyesexplor.relics.no_target"), true);
            return false;
        }
        if (!payVis(player, Map.of(VisChannel.IGNIS, 800,
                VisChannel.ORDO, 500, VisChannel.PERDITIO, 500))) {
            return false;
        }
        ServerLevel nether = player.getServer().getLevel(Level.NETHER);
        if (nether == null) {
            return false;
        }
        int x = level.random.nextInt(4001) - 2000;
        int z = level.random.nextInt(4001) - 2000;
        int y = nether.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) + 1;
        target.teleportTo(nether, x + 0.5, (double) y, z + 0.5, java.util.Set.of(), target.getYRot(), target.getXRot());
        nether.playSound(null, target.blockPosition(), SoundEvents.ENDERMAN_TELEPORT,
                net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 0.6F);
        player.displayClientMessage(Component.translatable("msg.thaumicdyesexplor.overthrower.banished",
                target.getDisplayName()), true);
        return true;
    }

    /**
     * 预定天命之书：把视线 16 格内的生物拽到身前 2.5 格
     * （源模组是按住右键持续牵引 + 左键雷击，这里保留「抓取」这一条核心手感）。
     */
    private static boolean telekineticGrab(ServerLevel level, ServerPlayer player) {
        LivingEntity target = lookingAt(player, 16.0, 0.90);
        if (target == null) {
            player.displayClientMessage(Component.translatable("msg.thaumicdyesexplor.relics.no_target"), true);
            return false;
        }
        if (!payVis(player, Map.of(VisChannel.AER, 600, VisChannel.ORDO, 800))) {
            return false;
        }
        Vec3 dest = player.getEyePosition().add(player.getViewVector(1.0F).scale(2.5)).subtract(0.0, 1.0, 0.0);
        target.teleportTo(level, dest.x, dest.y, dest.z, java.util.Set.of(), target.getYRot(), target.getXRot());
        target.setDeltaMovement(Vec3.ZERO);
        target.fallDistance = 0.0F;
        level.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT,
                net.minecraft.sounds.SoundSource.PLAYERS, 0.7F, 1.4F);
        return true;
    }

    /** 纷争之戒：背包里有纷争之书时，拿在手上右键可以免费闪现一次。 */
    private static boolean discordBlink(ServerLevel level, ServerPlayer player) {
        if (!carries(player, ModRelicItems.TELEPORTATION_TOME.get())) {
            player.displayClientMessage(Component.translatable("msg.thaumicdyesexplor.discord_ring.no_tome"), true);
            return false;
        }
        return blink(level, player, false);
    }

    // ============================================================================================
    // 工具：vis 扣费、法杖查找、视线目标
    // ============================================================================================

    /** 六元要素均摊的 vis 成本（centivis）。 */
    private static Map<VisChannel, Integer> evenCost(int totalCentivis) {
        int perChannel = Math.max(1, totalCentivis / 6);
        return Map.of(VisChannel.AER, perChannel, VisChannel.TERRA, perChannel,
                VisChannel.IGNIS, perChannel, VisChannel.AQUA, perChannel,
                VisChannel.ORDO, perChannel, VisChannel.PERDITIO, perChannel);
    }

    /**
     * 严格付费：六个通道都够才扣，否则只提示（实现在 {@link WandVis}，
     * 基岩粉碎者等其它物品也用同一份逻辑）。
     */
    private static boolean payVis(ServerPlayer player, Map<VisChannel, Integer> cost) {
        return WandVis.pay(player, cost);
    }

    private static void noWand(ServerPlayer player) {
        player.displayClientMessage(Component.translatable("msg.thaumicdyesexplor.relics.no_wand"), true);
    }

    /** 背包里的第一把法杖（主副手优先）。 */
    private static ItemStack findWand(ServerPlayer player) {
        return WandVis.findWand(player);
    }

    /** 玩家身上（背包 + 副手 + Curios 槽）是否有这件物品。 */
    static boolean carries(Player player, Item item) {
        var inventory = player.getInventory();
        for (ItemStack stack : inventory.items) {
            if (stack.is(item)) {
                return true;
            }
        }
        for (ItemStack stack : inventory.offhand) {
            if (stack.is(item)) {
                return true;
            }
        }
        var curios = top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(player);
        if (curios.isPresent()) {
            var handler = curios.get().getEquippedCurios();
            for (int slot = 0; slot < handler.getSlots(); slot++) {
                if (handler.getStackInSlot(slot).is(item)) {
                    return true;
                }
            }
        }
        return false;
    }

    /** 视线方向（点积阈值）里最近的活体目标。 */
    private static LivingEntity lookingAt(ServerPlayer player, double range, double minDot) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0F).normalize();
        LivingEntity best = null;
        double bestDot = minDot;
        var box = player.getBoundingBox().inflate(range);
        for (LivingEntity candidate : player.level().getEntitiesOfClass(LivingEntity.class, box,
                entity -> entity != player && entity.isAlive())) {
            Vec3 to = candidate.getEyePosition().subtract(eye);
            double distance = to.length();
            if (distance < 0.2 || distance > range) {
                continue;
            }
            double dot = look.dot(to.normalize());
            if (dot > bestDot) {
                bestDot = dot;
                best = candidate;
            }
        }
        return best;
    }

    private static String builtId(ItemStack stack) {
        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        return path.startsWith("reli_") ? path : null;
    }

    private RelicItemEvents() {
    }

    /**
     * 命运之书：死亡瞬间消耗 6×100 vis 免死（源模组 RelicsEventHandler 的免死分支，
     * 冷却 5 分钟），回满血并随机附赠一档强 buff 或一小串 debuff。
     */
    @SubscribeEvent
    public static void onDeath(net.neoforged.neoforge.event.entity.living.LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide) {
            return;
        }
        ItemStack found = null;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(ModRelicItems.FATE_TOME.get())) {
                found = stack;
                break;
            }
        }
        if (found == null) {
            var curios = top.theillusivec4.curios.api.CuriosApi.getCuriosHelper()
                    .findFirstCurio(player, ModRelicItems.FATE_TOME.get());
            if (curios.isPresent()) {
                found = curios.get().stack();
            }
        }
        if (found == null || player.getCooldowns().isOnCooldown(found.getItem())) {
            return;
        }
        if (!payVis(player, Map.of(VisChannel.AER, 10_000, VisChannel.TERRA, 10_000,
                VisChannel.IGNIS, 10_000, VisChannel.AQUA, 10_000,
                VisChannel.ORDO, 10_000, VisChannel.PERDITIO, 10_000))) {
            return;
        }
        event.setCanceled(true);
        player.setHealth(player.getMaxHealth());
        player.getCooldowns().addCooldown(found.getItem(), 6000);
        if (player.getRandom().nextDouble() <= 0.75) {
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 200, 2));
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 500, 1));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 1000, 0));
        } else {
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 600, 2));
            player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 200, 0));
            player.addEffect(new MobEffectInstance(MobEffects.POISON, 300, 1));
        }
        player.displayClientMessage(net.minecraft.network.chat.Component.translatable(
                "msg.thaumicdyesexplor.fate_tome.saved"), true);
    }

    /**
     * 七阳之戒（对齐源模组 ItemDarkSunRing 的完整行为）：
     * 火焰 / 岩浆伤害转为治疗（+2）；单次伤害超过 12 点的攻击直接免疫。
     */
    @SubscribeEvent
    public static void onDarkSunDamage(net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide) {
            return;
        }
        if (top.theillusivec4.curios.api.CuriosApi.getCuriosHelper()
                .findFirstCurio(player, ModRelicItems.DARK_SUN_RING.get())
                .isEmpty()) {
            return;
        }
        var source = event.getSource();
        boolean fire = source.is(net.minecraft.world.damagesource.DamageTypes.IN_FIRE)
                || source.is(net.minecraft.world.damagesource.DamageTypes.ON_FIRE)
                || source.is(net.minecraft.world.damagesource.DamageTypes.LAVA)
                || source.is(net.minecraft.world.damagesource.DamageTypes.HOT_FLOOR);
        if (fire) {
            event.setCanceled(true);
            player.heal(2.0F);
            player.extinguishFire();
            player.clearFire();
        } else if (event.getAmount() > 12.0F) {
            event.setCanceled(true);
        }
    }
}
