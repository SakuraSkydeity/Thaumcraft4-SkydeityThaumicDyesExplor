package com.skydeity.thaumicdyesexplor.wand;

import java.util.ArrayList;
import java.util.List;

import com.skydeity.thaumicdyesexplor.ThaumicDyesExplor;

import dev.tc4port.thaumcraft.api.aspect.AspectId;
import dev.tc4port.thaumcraft.api.aspect.VisAction;
import dev.tc4port.thaumcraft.api.aspect.VisChannel;
import dev.tc4port.thaumcraft.api.aspect.VisCost;
import dev.tc4port.thaumcraft.api.wand.WandApi;
import dev.tc4port.thaumcraft.api.wand.WandCarrierItem;
import dev.tc4port.thaumcraft.api.wand.WandMaterialId;
import dev.tc4port.thaumcraft.api.wand.WandView;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * 四种自制杖杆的被动行为：琥珀 / 嬗变 / 死灵 / 面包。
 *
 * <h2>为什么不用 {@code WandServerBehavior}</h2>
 * <p>前置的 {code WandBehaviorApi.register(...)} 只在<b>手持</b>法杖时、每 20 tick 回调一次
 * （{@code WandBehaviorRegistry#tick} 里写死了 {@code player.tickCount % 20 == 0} 且只扫双持）。
 * 而源模组的这四种杖芯是写在 {@code Item#onUpdate} 里的——法杖躺在背包里也会工作。
 * 所以这里改用 {@link PlayerTickEvent.Post} 自己遍历背包，节拍与源模组逐条对齐。</p>
 *
 * <h2>Vis 读写</h2>
 * <p>{@code WandApi.insert/extract} 的底盘（{@code WandVisChanges#base}）只是「按请求量改数值并夹到
 * [0, 容量]」，不涉及任何世界来源结算，正好等于 1.7.10 的 {@code addVis(stack, aspect, n, true)}。
 * 单位是 centivis，1 vis = 100。</p>
 */
@EventBusSubscriber(modid = ThaumicDyesExplor.MOD_ID)
public final class WandCores {

    private static final int CENTIVIS_PER_VIS = 100;

    private static final WandMaterialId AMBER = material("amber");
    private static final WandMaterialId AMBER_STAFF = material("amber_staff");
    private static final WandMaterialId TRANSMUTATION = material("transmutation");
    private static final WandMaterialId TRANSMUTATION_STAFF = material("transmutation_staff");
    private static final WandMaterialId BREAD = material("bread");
    private static final WandMaterialId NECRO_STAFF = material("necro_staff");

    /** 六个元要素，顺序与源模组 {@code Aspect.getPrimalAspects()} 一致。 */
    private static final List<VisChannel> PRIMALS = List.of(
            VisChannel.AER, VisChannel.TERRA, VisChannel.IGNIS,
            VisChannel.AQUA, VisChannel.ORDO, VisChannel.PERDITIO);

    /** 嬗变杖杆搬运要素的间隔：源模组每 tick 都跑，这里放慢到 4 tick，观感一致而不刷包。 */
    private static final int TRANSMUTE_PERIOD = 4;

    /** 死灵杖杆对要素球的加成倍率（源模组 4.3333333）。 */
    private static final float NECRO_ORB_MULTIPLIER = 4.3333333F;

    /** 要素球吸收半径（源模组 3 格）。 */
    private static final double ORB_RADIUS = 3.0D;

    private WandCores() {
    }

    private static WandMaterialId material(String path) {
        return WandMaterialId.parse(ThaumicDyesExplor.MOD_ID + ":" + path);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        for (ItemStack wand : carriedWands(player)) {
            WandView view = WandApi.view(wand).orElse(null);
            if (view == null) {
                continue;
            }
            WandMaterialId rod = view.rod();
            if (rod.equals(AMBER) || rod.equals(AMBER_STAFF)) {
                tickAmber(player, wand, view);
            } else if (rod.equals(TRANSMUTATION) || rod.equals(TRANSMUTATION_STAFF)) {
                tickTransmutation(player, wand, view);
            } else if (rod.equals(BREAD)) {
                tickBread(player, wand);
            } else if (rod.equals(NECRO_STAFF)) {
                tickNecromancer(player, wand);
            }
        }
    }

    /** 玩家身上的所有法杖：主背包 36 格 + 副手。 */
    private static List<ItemStack> carriedWands(ServerPlayer player) {
        List<ItemStack> out = new ArrayList<>(2);
        var inventory = player.getInventory();
        for (ItemStack stack : inventory.items) {
            // 先做一次 O(1) 的类型判断，避免对整背包都去解析法杖数据。
            if (stack.getItem() instanceof WandCarrierItem && WandApi.view(stack).isPresent()) {
                out.add(stack);
            }
        }
        for (ItemStack stack : inventory.offhand) {
            if (stack.getItem() instanceof WandCarrierItem && WandApi.view(stack).isPresent()) {
                out.add(stack);
            }
        }
        return out;
    }

    // ============================================================================================
    // 琥珀杖杆：每 5 秒给每个「没满」的元要素 +1 vis
    // ============================================================================================

    private static void tickAmber(ServerPlayer player, ItemStack wand, WandView view) {
        if (player.tickCount % 100 != 0) {
            return;
        }
        for (VisChannel channel : PRIMALS) {
            int capacity = view.capacityCentivis().getOrDefault(channel, 0);
            int current = view.visCentivis().getOrDefault(channel, 0);
            if (current >= capacity) {
                continue;
            }
            WandApi.insert(player, wand,
                    VisCost.ofCentivis(channel, CENTIVIS_PER_VIS), VisAction.EXECUTE);
        }
    }

    // ============================================================================================
    // 嬗变杖杆：把超过 90% 的要素挪给不足 90% 的要素
    // ============================================================================================

    private static void tickTransmutation(ServerPlayer player, ItemStack wand, WandView view) {
        if (player.tickCount % TRANSMUTE_PERIOD != 0) {
            return;
        }
        List<VisChannel> low = new ArrayList<>(PRIMALS.size());
        long surplusCentivis = 0;
        for (VisChannel channel : PRIMALS) {
            int capacity = view.capacityCentivis().getOrDefault(channel, 0);
            int current = view.visCentivis().getOrDefault(channel, 0);
            int threshold = capacity * 9 / 10;
            if (current > threshold) {
                surplusCentivis += current - threshold;
            } else {
                low.add(channel);
            }
        }
        if (low.isEmpty() || surplusCentivis < CENTIVIS_PER_VIS) {
            return;
        }
        // 先把超出 90% 的部分抽走。
        long actuallyExtracted = 0;
        for (VisChannel channel : PRIMALS) {
            int capacity = view.capacityCentivis().getOrDefault(channel, 0);
            int current = view.visCentivis().getOrDefault(channel, 0);
            int threshold = capacity * 9 / 10;
            int take = current - threshold;
            if (take <= 0) {
                continue;
            }
            var result = WandApi.extract(player, wand,
                    VisCost.ofCentivis(channel, take), VisAction.EXECUTE);
            actuallyExtracted += result.transferred(channel);
        }
        if (actuallyExtracted < CENTIVIS_PER_VIS) {
            return;
        }
        // 四分之三分给不足 90% 的要素，四分之一回到所有要素（源模组里的「损耗再均摊」）。
        long toLow = actuallyExtracted * 3 / 4;
        long each = toLow / low.size();
        for (VisChannel channel : low) {
            if (each < CENTIVIS_PER_VIS) {
                break;
            }
            WandApi.insert(player, wand,
                    VisCost.ofCentivis(channel, (int) Math.min(Integer.MAX_VALUE, each)), VisAction.EXECUTE);
        }
        long spill = actuallyExtracted - toLow;
        long spillEach = spill / PRIMALS.size();
        if (spillEach >= CENTIVIS_PER_VIS) {
            for (VisChannel channel : PRIMALS) {
                WandApi.insert(player, wand,
                        VisCost.ofCentivis(channel, (int) Math.min(Integer.MAX_VALUE, spillEach)), VisAction.EXECUTE);
            }
        }
    }

    // ============================================================================================
    // 死灵杖杆：把身边 3 格内的要素球吸进法杖，量 ×4.3333
    // ============================================================================================

    private static void tickNecromancer(ServerPlayer player, ItemStack wand) {
        if (!AspectOrbAccess.isAvailable() || player.tickCount % TRANSMUTE_PERIOD != 0) {
            return;
        }
        AABB box = player.getBoundingBox().inflate(ORB_RADIUS);
        List<Entity> orbs = player.level().getEntities(player, box, AspectOrbAccess::isOrb);
        for (Entity orb : orbs) {
            AspectId aspect = AspectOrbAccess.aspectOf(orb);
            int value = AspectOrbAccess.valueOf(orb);
            if (aspect == null || value <= 0) {
                continue;
            }
            VisChannel channel = VisChannel.fromAspectId(aspect).orElse(null);
            if (channel == null) {
                continue;
            }
            long centivis = (long) Math.ceil(value * NECRO_ORB_MULTIPLIER * CENTIVIS_PER_VIS);
            if (centivis <= 0) {
                continue;
            }
            WandApi.insert(player, wand,
                    VisCost.ofCentivis(channel, (int) Math.min(Integer.MAX_VALUE, centivis)), VisAction.EXECUTE);
            orb.discard();
        }
    }

    // ============================================================================================
    // 面包杖杆：极低概率说一句话
    // ============================================================================================

    private static void tickBread(ServerPlayer player, ItemStack wand) {
        if (player.getRandom().nextInt(1000) != 0) {
            return;
        }
        int line = player.getRandom().nextInt(5);
        player.sendSystemMessage(Component.translatable("thaumicdyesexplor.bread." + line));
    }
}
