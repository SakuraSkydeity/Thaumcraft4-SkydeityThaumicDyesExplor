package com.skydeity.thaumicdyesexplor.item;

import com.skydeity.thaumicdyesexplor.registry.ModDataComponents;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.InteractionResult;

/**
 * Forgotten Relics 的武器与经验之书。
 *
 * <h2>PARADOXICALSTUFF 材质</h2>
 * <p>源模组 {@code RelicsMaterialHandler.materialParadoxicalStuff}：
 * 采集等级 4、耐久 3000、效率 16、附加伤害 -4（合成台面板总伤 0，实际伤害另有机关）。
 * 1.21.1 的 {@link Tier} 直接对齐这四项；{@code enchantability} 沿用源模组的 100。</p>
 *
 * <h2>悖论（The Paradox）</h2>
 * <p>源模组 {@code ItemParadox#onLeftClickEntity}：伤害在 0 ~ 上限间随机，
 * 玩家自己承受「上限 - 随机值」的反噬。这里上限取 20（源模组来自配置文件默认值）。</p>
 *
 * <h2>永恒智慧之书</h2>
 * <p>右键吸收自身全部经验（存进数据组件），潜行右键把存的经验全部返还。</p>
 */
public final class RelicWeapons {

    /** PARADOXICALSTUFF：4 级 / 3000 耐久 / 效率 16 / 附加伤害 -4（对照源模组）。 */
    public static final Tier PARADOX_TIER = new Tier() {
        @Override
        public int getUses() {
            return 3000;
        }

        @Override
        public float getSpeed() {
            return 16.0F;
        }

        @Override
        public float getAttackDamageBonus() {
            return -3.0F; // 剑的 3 点基础伤害加上后 = 0，对应源模组 4 + (-4)
        }

        @Override
        public TagKey<Block> getIncorrectBlocksForDrops() {
            return BlockTags.INCORRECT_FOR_NETHERITE_TOOL;
        }

        @Override
        public int getEnchantmentValue() {
            return 100;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.EMPTY;
        }
    };

    /** 遗物用法提示行（与 {@code item/RelicItem} 同一个键前缀）。 */
    static void appendUsage(List<Component> tooltip, String id) {
        tooltip.add(Component.translatable("tooltip.thaumicdyesexplor.usage." + id)
                .withStyle(net.minecraft.ChatFormatting.DARK_GRAY));
    }

    /** 悖论：左键命中时伤害随机（0~20），差额反噬自己。 */
    public static class ParadoxItem extends SwordItem {
        private static final float DAMAGE_CAP = 20.0F;

        public ParadoxItem(Properties properties) {
            super(RelicWeapons.PARADOX_TIER,
                    properties.attributes(SwordItem.createAttributes(RelicWeapons.PARADOX_TIER, 3, -2.4F)));
        }

        @Override
        public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
            RandomSource random = player.getRandom();
            float damage = random.nextFloat() * DAMAGE_CAP;
            if (entity.level() instanceof net.minecraft.server.level.ServerLevel level) {
                entity.hurt(player.damageSources().playerAttack(player), damage);
                player.hurt(player.damageSources().playerAttack(player), DAMAGE_CAP - damage);
                level.sendParticles(net.minecraft.core.particles.ParticleTypes.PORTAL,
                        entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ(),
                        12, 0.3, 0.4, 0.3, 0.05);
            }
            return true; // 取消原版近战结算
        }

        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
            super.appendHoverText(stack, context, tooltip, flag);
            RelicWeapons.appendUsage(tooltip, "reli_paradox");
        }
    }

    /** 荒芜铺展者：与悖论同材质；源模组的左键机关在源码里就是空实现（"???"），保持原样。 */
    public static class WastelayerItem extends SwordItem {
        public WastelayerItem(Properties properties) {
            super(RelicWeapons.PARADOX_TIER,
                    properties.attributes(SwordItem.createAttributes(RelicWeapons.PARADOX_TIER, 3, -2.4F)));
        }

        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
            super.appendHoverText(stack, context, tooltip, flag);
            RelicWeapons.appendUsage(tooltip, "reli_wastelayer");
        }
    }

    /** 永恒智慧之书：右键吸经验，潜行右键放经验。 */
    public static class XpTomeItem extends Item {
        public XpTomeItem(Properties properties) {
            super(properties);
        }

        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
            super.appendHoverText(stack, context, tooltip, flag);
            RelicWeapons.appendUsage(tooltip, "reli_xp_tome");
        }

        @Override
        public InteractionResultHolder<ItemStack> use(net.minecraft.world.level.Level level, Player player,
                                                      InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);
            if (level.isClientSide) {
                return InteractionResultHolder.success(stack);
            }
            int stored = stack.getOrDefault(ModDataComponents.XP_STORED, 0);
            if (player.isShiftKeyDown() && stored > 0) {
                player.giveExperiencePoints(stored);
                stack.set(ModDataComponents.XP_STORED, 0);
                player.displayClientMessage(Component.translatable(
                        "msg.thaumicdyesexplor.xp_tome.release", stored), true);
            } else if (!player.isShiftKeyDown()) {
                int points = totalPoints(player);
                if (points <= 0) {
                    return InteractionResultHolder.pass(stack);
                }
                stack.set(ModDataComponents.XP_STORED, stored + points);
                player.experienceLevel = 0;
                player.experienceProgress = 0.0F;
                player.totalExperience = 0;
                player.displayClientMessage(Component.translatable(
                        "msg.thaumicdyesexplor.xp_tome.absorb", points), true);
            }
            return InteractionResultHolder.success(stack);
        }

        /** 原版经验公式：升到下一级所需点数随等级段变化。 */
        private static int xpToNext(int level) {
            if (level >= 31) {
                return 9 * level - 158;
            }
            if (level >= 16) {
                return 5 * level - 38;
            }
            return 2 * level + 7;
        }

        private static int totalPoints(Player player) {
            int points = 0;
            for (int i = 0; i < player.experienceLevel; i++) {
                points += xpToNext(i);
            }
            points += (int) (player.experienceProgress * xpToNext(player.experienceLevel));
            return points;
        }
    }

    private RelicWeapons() {
    }
}
