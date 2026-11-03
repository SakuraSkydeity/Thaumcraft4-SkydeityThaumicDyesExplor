package com.skydeity.thaumicdyesexplor.curio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.skydeity.thaumicdyesexplor.registry.ModRelicItems;

import dev.tc4port.thaumcraft.api.aspect.VisAction;
import dev.tc4port.thaumcraft.api.aspect.VisChannel;
import dev.tc4port.thaumcraft.api.aspect.VisCost;
import dev.tc4port.thaumcraft.api.wand.WandApi;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

/**
 * Forgotten Relics 六件饰品 + 两件采矿护符的行为（对照源模组 onWornTick）：
 *
 * <ul>
 *   <li><b>七阳之戒</b>：身上着火时立刻灭火（源 ItemDarkSunRing）。</li>
 *   <li><b>叠加之戒</b>：每 600 tick 2.5% 概率与另一名同样佩戴叠加之戒的玩家交换位置
 *       （源 ItemSuperpositionRing，跨维度也换）。</li>
 *   <li><b>闪亮之石</b>：原地静止会积累「静电」，静止越久生命恢复越强
 *       （源 ItemShinyEnergy 治疗弹简化为原版再生 + 粒子）。</li>
 *   <li><b>神性护符</b>：清除负面药水效果、着火灭火、溺水瞬间用 vis 换一口气
 *       （源 ItemDeificAmulet）。</li>
 *   <li><b>遗忘护符</b>：约 0.04%/tick 随机附加一个负面药水（源模组的「诅咒」设定），
 *       扭曲 +4 走 equipped_warp 数据。</li>
 *   <li><b>采矿护符 / 灵虚采矿护符</b>：源模组靠 Botania 的额外触及距离；
 *       改用原版属性：交互距离 +2/+4、挖掘速度 +50%/+100%。</li>
 * </ul>
 */
public final class RelicCurios {

    private static final Map<UUID, BlockPos> LAST_POS = new HashMap<>();
    private static final Map<UUID, Integer> STILLNESS = new HashMap<>();

    public static void register(RegisterCapabilitiesEvent event) {
        register(event, ModRelicItems.DARK_SUN_RING.get(), new SimpleHandler() {
            @Override
            public void tick(SlotContext ctx) {
                LivingEntity entity = ctx.entity();
                if (!entity.level().isClientSide && entity.isOnFire()) {
                    entity.extinguishFire();
                }
            }
        });
        register(event, ModRelicItems.SUPERPOSITION_RING.get(), new SimpleHandler() {
            @Override
            public void tick(SlotContext ctx) {
                if (!(ctx.entity() instanceof ServerPlayer player) || player.level().isClientSide
                        || player.tickCount % 600 != 0 || player.getRandom().nextDouble() > 0.025) {
                    return;
                }
                List<ServerPlayer> others = new ArrayList<>();
                for (ServerPlayer other : player.server.getPlayerList().getPlayers()) {
                    if (other != player && CuriosApi.getCuriosHelper()
                            .findFirstCurio(other, ModRelicItems.SUPERPOSITION_RING.get()).isPresent()) {
                        others.add(other);
                    }
                }
                if (others.isEmpty()) {
                    return;
                }
                ServerPlayer victim = others.get(player.getRandom().nextInt(others.size()));
                double ax = player.getX(), ay = player.getY(), az = player.getZ();
                float ayaw = player.getYRot(), apitch = player.getXRot();
                player.teleportTo(victim.serverLevel(), victim.getX(), victim.getY(), victim.getZ(),
                        victim.getYRot(), victim.getXRot());
                victim.teleportTo(player.serverLevel(), ax, ay, az, ayaw, apitch);
            }
        });
        register(event, ModRelicItems.SHINY_STONE.get(), new SimpleHandler() {
            @Override
            public void tick(SlotContext ctx) {
                if (!(ctx.entity() instanceof ServerPlayer player) || player.level().isClientSide
                        || player.tickCount % 20 != 0) {
                    return;
                }
                BlockPos current = player.blockPosition();
                BlockPos last = LAST_POS.put(player.getUUID(), current);
                int still;
                if (current.equals(last)) {
                    still = STILLNESS.merge(player.getUUID(), 20, Integer::sum);
                } else {
                    STILLNESS.put(player.getUUID(), 0);
                    still = 0;
                }
                if (still < 40) {
                    return;
                }
                int amplifier = still >= 400 ? 2 : still >= 160 ? 1 : 0;
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, amplifier, true, true));
                if (player.level() instanceof ServerLevel level) {
                    level.sendParticles(ParticleTypes.END_ROD,
                            player.getX(), player.getY() + 1.0, player.getZ(), 3, 0.4, 0.6, 0.4, 0.02);
                }
            }
        });
        register(event, ModRelicItems.DEIFIC_AMULET.get(), new SimpleHandler() {
            @Override
            public void tick(SlotContext ctx) {
                LivingEntity entity = ctx.entity();
                if (entity.level().isClientSide) {
                    return;
                }
                if (entity.isOnFire()) {
                    entity.extinguishFire();
                }
                if (entity.tickCount % 20 == 0) {
                    List<MobEffectInstance> bad = new ArrayList<>();
                    for (MobEffectInstance effect : entity.getActiveEffects()) {
                        if (!effect.getEffect().value().isBeneficial()) {
                            bad.add(effect);
                        }
                    }
                    for (MobEffectInstance effect : bad) {
                        entity.removeEffect(effect.getEffect());
                    }
                }
                if (entity.getAirSupply() == 0 && entity instanceof ServerPlayer player) {
                    ItemStack wand = findWand(player);
                    if (wand != null) {
                        WandApi.extract(player, wand,
                                VisCost.ofCentivis(VisChannel.AER, 200), VisAction.EXECUTE);
                        WandApi.extract(player, wand,
                                VisCost.ofCentivis(VisChannel.AQUA, 200), VisAction.EXECUTE);
                        entity.setAirSupply(300);
                    }
                }
            }
        });
        register(event, ModRelicItems.OBLIVION_AMULET.get(), new SimpleHandler() {
            @Override
            public void tick(SlotContext ctx) {
                LivingEntity entity = ctx.entity();
                if (entity.level().isClientSide || entity.getRandom().nextDouble() > 0.0004) {
                    return;
                }
                MobEffectInstance[] curses = {
                        new MobEffectInstance(MobEffects.POISON, 100 + entity.getRandom().nextInt(2000), 0),
                        new MobEffectInstance(MobEffects.HUNGER, 100 + entity.getRandom().nextInt(2000), 0),
                        new MobEffectInstance(MobEffects.WEAKNESS, 100 + entity.getRandom().nextInt(2000), 0),
                        new MobEffectInstance(MobEffects.DARKNESS, 100 + entity.getRandom().nextInt(2000), 0)};
                entity.addEffect(curses[entity.getRandom().nextInt(curses.length)]);
            }
        });
        registerAttrs(event, ModRelicItems.MINING_CHARM.get(), 2.0, 0.5);
        registerAttrs(event, ModRelicItems.ADVANCED_MINING_CHARM.get(), 4.0, 1.0);
        // 第 19 轮补：远古神盾（源 ItemAncientAegis，BaubleType.BELT）此前根本没接能力，
        // 装到腰带位也不会回血、也不免击退。
        register(event, ModRelicItems.ANCIENT_AEGIS.get(), new AncientAegisHandler());
    }

    // ============================================================================================

    private interface SimpleHandler extends ICurio {
        default ItemStack getStack() {
            return ItemStack.EMPTY;
        }

        void tick(SlotContext ctx);

        @Override
        default void curioTick(SlotContext slotContext) {
            tick(slotContext);
        }
    }

    /**
     * 共享实例注册：这些行为都不依赖 per-stack 状态（tick 的参数带 entity），
     * 所以一份 handler 直接挂到物品上，Curios 会按槽位回调。
     */
    private static void register(RegisterCapabilitiesEvent event, Item item, SimpleHandler handler) {
        event.registerItem(CuriosCapability.ITEM,
                (stack, context) -> new SharedHandler(stack, handler), item);
    }

    private static void registerAttrs(RegisterCapabilitiesEvent event, Item item,
                                      double range, double speedBonus) {
        event.registerItem(CuriosCapability.ITEM,
                (stack, context) -> new SharedHandler(stack, new MiningHandler(range, speedBonus)), item);
    }

    /** 把公共 ItemStack 接到共享行为对象上。 */
    private record SharedHandler(ItemStack stack, SimpleHandler delegate) implements ICurio {

        @Override
        public ItemStack getStack() {
            return this.stack;
        }

        @Override
        public void curioTick(SlotContext slotContext) {
            this.delegate.curioTick(slotContext);
        }

        @Override
        public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(
                SlotContext slotContext, ResourceLocation identifier) {
            return this.delegate.getAttributeModifiers(slotContext, identifier);
        }
    }

    /** 采矿护符：交互距离 + 挖掘速度属性。 */
    private record MiningHandler(double range, double speedBonus) implements SimpleHandler {
        @Override
        public void tick(SlotContext ctx) {
        }

        @Override
        public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(
                SlotContext slotContext, ResourceLocation identifier) {
            Multimap<Holder<Attribute>, AttributeModifier> modifiers = HashMultimap.create();
            modifiers.put(Attributes.BLOCK_INTERACTION_RANGE, new AttributeModifier(
                    ResourceLocation.fromNamespaceAndPath("thaumicdyesexplor", "relic_charm_range"),
                    this.range, AttributeModifier.Operation.ADD_VALUE));
            modifiers.put(Attributes.ENTITY_INTERACTION_RANGE, new AttributeModifier(
                    ResourceLocation.fromNamespaceAndPath("thaumicdyesexplor", "relic_charm_entrange"),
                    this.range, AttributeModifier.Operation.ADD_VALUE));
            modifiers.put(Attributes.BLOCK_BREAK_SPEED, new AttributeModifier(
                    ResourceLocation.fromNamespaceAndPath("thaumicdyesexplor", "relic_charm_speed"),
                    this.speedBonus, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            return modifiers;
        }
    }

    /**
     * 远古神盾（源 {@code ItemAncientAegis}，腰带槽）：免疫击退 + 每秒回 1 点血。
     *
     * <p>源模组还给 32 格内的其他玩家分摊 40% 伤害，那套「替别人挨打」的结算在
     * 1.21.1 要拦 {@code LivingIncomingDamageEvent} 再按玩家列表分账，容易和护盾 /
     * 抗性 / 队友的减伤叠出错误结果，这里只保留两条稳定的：击退抗性拉满 + 持续回复。</p>
     */
    private record AncientAegisHandler() implements SimpleHandler {

        @Override
        public void tick(SlotContext ctx) {
            if (!(ctx.entity() instanceof LivingEntity living) || living.level().isClientSide
                    || living.tickCount % 20 != 0) {
                return;
            }
            if (living.getHealth() < living.getMaxHealth()) {
                living.heal(1.0F);
            }
        }

        @Override
        public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(
                SlotContext slotContext, ResourceLocation identifier) {
            Multimap<Holder<Attribute>, AttributeModifier> modifiers = HashMultimap.create();
            modifiers.put(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(
                    ResourceLocation.fromNamespaceAndPath("thaumicdyesexplor", "relic_aegis_knockback"),
                    1.0, AttributeModifier.Operation.ADD_VALUE));
            return modifiers;
        }
    }

    private static ItemStack findWand(ServerPlayer player) {
        var inventory = player.getInventory();
        for (ItemStack stack : inventory.items) {
            if (WandApi.view(stack).isPresent()) {
                return stack;
            }
        }
        for (ItemStack stack : inventory.offhand) {
            if (WandApi.view(stack).isPresent()) {
                return stack;
            }
        }
        return null;
    }

    private RelicCurios() {
    }
}
