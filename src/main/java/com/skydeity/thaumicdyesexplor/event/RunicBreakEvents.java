package com.skydeity.thaumicdyesexplor.event;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.skydeity.thaumicdyesexplor.ThaumicDyesExplor;
import com.skydeity.thaumicdyesexplor.component.RunicUpgrades;
import com.skydeity.thaumicdyesexplor.compat.PlayerMagicAccess;
import com.skydeity.thaumicdyesexplor.item.RunicUpgradeHolder;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * 符文升级 1/3/4/6 —— 破盾触发（狂战 / 动能 / 复苏 / 应急）。
 *
 * <h2>前置没有“破盾事件”怎么办</h2>
 * <p>前置 20753 把当前护盾值存在附件 {@code thaumcraft:player_magic} 里（API 无读口），
 * 但它<b>已经内置</b>了动能/复苏/应急三档破盾效果 —— 走的是
 * {@code equipment_abilities.json} 的 {@code runic_trait} 静态特质。本模组的升级是
 * 玩家动态镶嵌的（{@link RunicUpgrades} 组件），静态特质表达不了，所以这里自己盯：
 * 每 tick 读一次护盾值，检测“上 tick &gt; 0、本 tick = 0”的跌破沿再触发效果。</p>
 *
 * <h2>效果与冷却（逐条对照源模组 EventHandlerRunicLegacy）</h2>
 * <table border="1">
 *   <tr><th>升级</th><th>冷却</th><th>效果</th></tr>
 *   <tr><td>1 狂战</td><td>400t</td><td>力量 I+n（100×n tick）；n≥4 再给抗性（200+100×(n-4) tick、(n-4) 级）</td></tr>
 *   <tr><td>3 动能</td><td>600t</td><td>以玩家为中心爆炸，半径 1.5 + 0.5×n，不毁方块不引燃</td></tr>
 *   <tr><td>4 复苏</td><td>600t</td><td>生命恢复 (1+n)/2 级、200 tick</td></tr>
 *   <tr><td>6 应急</td><td>2400t</td><td>护盾立刻回充 8×n 点（不超过上限）</td></tr>
 * </table>
 */
@EventBusSubscriber(modid = ThaumicDyesExplor.MOD_ID)
public final class RunicBreakEvents {

    private static final int CD_BERSERKER = 400;
    private static final int CD_KINETIC = 600;
    private static final int CD_HEALING = 600;
    private static final int CD_EMERGENCY = 2400;

    /** 上一次看到的护盾值（用于检测跌破沿）。 */
    private static final Map<UUID, Integer> LAST_CHARGE = new HashMap<>();
    /** 效果冷却：uuid:type → 剩余 tick。 */
    private static final Map<String, Integer> COOLDOWNS = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) {
            return;
        }
        if (!wearsRunic(player)) {
            LAST_CHARGE.remove(player.getUUID());
            return;
        }
        // 冷却倒计时（源模组是每 tick 递减）
        COOLDOWNS.values().removeIf(v -> v <= 1);
        COOLDOWNS.replaceAll((k, v) -> v - 1);

        int charge = PlayerMagicAccess.charge(player);
        if (charge < 0) {
            return; // 反射不可用或数据未初始化
        }
        Integer last = LAST_CHARGE.put(player.getUUID(), charge);
        if (last == null || last <= 0 || charge > 0) {
            return; // 只在“有盾 → 无盾”的跌破沿触发
        }
        int berserker = 0, kinetic = 0, healing = 0, emergency = 0;
        for (ItemStack stack : player.getArmorSlots()) {
            if (!(stack.getItem() instanceof RunicUpgradeHolder)) {
                continue;
            }
            RunicUpgrades upgrades = RunicUpgrades.of(stack);
            for (int i = 0; i < upgrades.size(); i++) {
                switch (upgrades.at(i)) {
                    case 1 -> berserker++;
                    case 3 -> kinetic++;
                    case 4 -> healing++;
                    case 6 -> emergency++;
                    default -> {
                    }
                }
            }
        }
        if (berserker > 0 && offCooldown(player, 1)) {
            // 源模组 PotionEffect 的 amplifier 直接 = 件数（0 基），这里保持一致
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 100 * berserker, berserker));
            if (berserker >= 4) {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,
                        200 + 100 * (berserker - 4), berserker - 4));
            }
            playShieldSound(player);
        }
        if (kinetic > 0 && offCooldown(player, 3) && player.level() instanceof ServerLevel level) {
            level.explode(player, player.getX(), player.getY() + player.getBbHeight() / 2.0,
                    player.getZ(), 1.5F + kinetic * 0.5F, net.minecraft.world.level.Level.ExplosionInteraction.NONE);
        }
        if (healing > 0 && offCooldown(player, 4)) {
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, healing));
            playShieldSound(player);
        }
        if (emergency > 0 && offCooldown(player, 6)) {
            PlayerMagicAccess.setCharge(player, 8 * emergency);
            playChargeSound(player);
        }
    }

    private static boolean wearsRunic(Player player) {
        for (ItemStack stack : player.getArmorSlots()) {
            if (stack.getItem() instanceof RunicUpgradeHolder) {
                return true;
            }
        }
        return false;
    }

    private static boolean offCooldown(Player player, int type) {
        String key = player.getUUID() + ":" + type;
        if (COOLDOWNS.containsKey(key)) {
            return false;
        }
        COOLDOWNS.put(key, switch (type) {
            case 1 -> CD_BERSERKER;
            case 3 -> CD_KINETIC;
            case 4 -> CD_HEALING;
            default -> CD_EMERGENCY;
        });
        return true;
    }

    private static void playShieldSound(Player player) {
        player.level().playSound(null, player.blockPosition(),
                net.minecraft.sounds.SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.6F, 1.4F);
    }

    private static void playChargeSound(Player player) {
        player.level().playSound(null, player.blockPosition(),
                net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.8F, 0.7F);
    }

    private RunicBreakEvents() {
    }
}
