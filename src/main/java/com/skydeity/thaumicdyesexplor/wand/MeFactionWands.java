package com.skydeity.thaumicdyesexplor.wand;

import java.util.List;

import com.skydeity.thaumicdyesexplor.ThaumicDyesExplor;

import dev.tc4port.thaumcraft.api.aspect.VisAction;
import dev.tc4port.thaumcraft.api.aspect.VisChannel;
import dev.tc4port.thaumcraft.api.aspect.VisCost;
import dev.tc4port.thaumcraft.api.wand.WandApi;
import dev.tc4port.thaumcraft.api.wand.WandCarrierItem;
import dev.tc4port.thaumcraft.api.wand.WandView;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * 中洲派系杖柄 / 杖芯的回 vis 行为（源模组 {@code FactionWandUpdate implements IWandRodOnUpdate}）。
 *
 * <p>源模组：每 20 tick，按玩家对杖杆派系的结盟值（≥10000 → 50、≥1000 → 10、≥100 → 5）
 * 给六个元要素里每个「不足 90% 容量」的元素补 vis。结盟值来自 LOTR 模组的玩家数据。</p>
 *
 * <p><b>结盟来源降级</b>：1.21.1 没有可用的 LOTR 结盟 API（见
 * {@link FactionAlignmentApi} 的调研结论），当前 {@link FactionAlignmentApi.FixedAlignmentProvider}
 * 一律按 +100 结盟（每 20 tick 回 5 vis）处理；将来接入真实结盟数据后自动分档。</p>
 *
 * <p>杖杆的派系从材质 id 解析：{@code thaumicdyesexplor:me_gondor}（杖芯
 * {@code me_gondor_staff}）→ 派系 {@code gondor}。</p>
 */
@EventBusSubscriber(modid = ThaumicDyesExplor.MOD_ID)
public final class MeFactionWands {

    private static final int CENTIVIS_PER_VIS = 100;

    private static final List<VisChannel> PRIMALS = List.of(
            VisChannel.AER, VisChannel.TERRA, VisChannel.IGNIS,
            VisChannel.AQUA, VisChannel.ORDO, VisChannel.PERDITIO);

    private MeFactionWands() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 20 != 0) {
            return;
        }
        var inventory = player.getInventory();
        for (ItemStack stack : inventory.items) {
            tickWand(player, stack);
        }
        for (ItemStack stack : inventory.offhand) {
            tickWand(player, stack);
        }
    }

    private static void tickWand(ServerPlayer player, ItemStack stack) {
        if (!(stack.getItem() instanceof WandCarrierItem)) {
            return;
        }
        WandView view = WandApi.view(stack).orElse(null);
        if (view == null) {
            return;
        }
        String faction = factionOf(view.rod().serialized());
        if (faction == null) {
            return;
        }
        float alignment = FactionAlignmentApi.PROVIDER.alignment(player, faction);
        int recoveryVis = alignment >= 10000.0F ? 50 : alignment >= 1000.0F ? 10 : alignment >= 100.0F ? 5 : 0;
        if (recoveryVis <= 0) {
            return;
        }
        for (VisChannel channel : PRIMALS) {
            int capacity = view.capacityCentivis().getOrDefault(channel, 0);
            int current = view.visCentivis().getOrDefault(channel, 0);
            if (current >= capacity * 9 / 10) {
                continue;
            }
            WandApi.insert(player, stack,
                    VisCost.ofCentivis(channel, recoveryVis * CENTIVIS_PER_VIS), VisAction.EXECUTE);
        }
    }

    /** 从材质 id（{@code thaumicdyesexplor:me_<派系>[_staff]}）解析派系名；非 ME 杖返回 null。 */
    static String factionOf(String materialPath) {
        if (!materialPath.startsWith(ThaumicDyesExplor.MOD_ID + ":me_")) {
            return null;
        }
        String path = materialPath.substring(materialPath.indexOf(':') + 3);
        if (path.endsWith("_staff")) {
            path = path.substring(0, path.length() - "_staff".length());
        }
        return path;
    }
}
