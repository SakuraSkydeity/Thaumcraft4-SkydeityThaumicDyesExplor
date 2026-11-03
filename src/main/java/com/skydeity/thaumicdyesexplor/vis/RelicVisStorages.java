package com.skydeity.thaumicdyesexplor.vis;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.skydeity.thaumicdyesexplor.ThaumicDyesExplor;
import com.skydeity.thaumicdyesexplor.registry.ModRelicItems;

import dev.tc4port.thaumcraft.api.aspect.VisAction;
import dev.tc4port.thaumcraft.api.aspect.VisChannel;
import dev.tc4port.thaumcraft.api.aspect.VisCost;
import dev.tc4port.thaumcraft.api.vis.OwnedVisStorageProvider;
import dev.tc4port.thaumcraft.api.vis.VisAmuletItem;
import dev.tc4port.thaumcraft.api.vis.VisStorageApi;
import dev.tc4port.thaumcraft.api.vis.VisStorageView;
import dev.tc4port.thaumcraft.api.wand.VisTransferResult;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * 把三件 vis 储存遗物注册进前置的「自有 vis 储存」系统
 * （{@link VisStorageApi#registerItem}）——注册之后前置的充能设施与背包扣费
 * （充能基座 / 节点巡游 / {@code OwnedVisStorageRegistry#consume}）才认得它们；
 * 不注册就只是个能看不能用的摆设。
 *
 * <p>同时提供右键交互：普通右键把手持法杖的 vis 充进遗物，
 * 潜行右键把遗物的 vis 排给法杖（见 {@code event/RelicItemEvents}）。</p>
 */
public final class RelicVisStorages {

    /**
     * 必须在 {@code FMLCommonSetupEvent} 里调用，不能在更早的地方调。
     *
     * <p>前置的 {@link VisStorageApi#registerItem} 要的是**已实例化的 Item**，
     * 而物品是在注册事件里才绑定到 {@code DeferredHolder} 上的。在模组构造函数
     * （注册事件之前）就 {@code holder.get()} 会抛
     * {@code NullPointerException: Trying to access unbound value ...}，
     * 直接让整个模组构造失败 → 启动闪退。</p>
     */
    public static void register() {
        Map<String, DeferredItem<? extends VisAmuletItem>> items = new LinkedHashMap<>();
        items.put("arcanum", ModRelicItems.ARCANUM);
        items.put("omega_core", ModRelicItems.OMEGA_CORE);
        items.put("oblivion_stone", ModRelicItems.OBLIVION_STONE);

        for (var entry : items.entrySet()) {
            DeferredItem<? extends VisAmuletItem> holder = entry.getValue();
            // 双保险：万一将来有人又把这里挪早、或某件物品注册失败，
            // 也只是少注册一件储存，不会炸整个启动。
            if (!holder.isBound()) {
                ThaumicDyesExplor.LOGGER.warn("[TDE] vis 储存遗物 {} 尚未绑定，跳过注册", entry.getKey());
                continue;
            }
            VisStorageApi.registerItem(holder.get(), new Provider());
            ThaumicDyesExplor.LOGGER.info("[TDE] vis 储存遗物已注册：{}", entry.getKey());
        }
    }

    /**
     * 前置只提供「正向 {@code add} + 原子 {@code consume}」，没有减量接口，
     * 所以早期用 {@code add(stack, channel, -n)} 的写法是**静默无效**的（负数直接被忽略），
     * 表现出来就是「遗物存了 vis 却永远抽不出来」。这里改用 {@code consume}。
     */
    private record Provider() implements OwnedVisStorageProvider {

        @Override
        public VisStorageView view(ItemStack stack) {
            VisAmuletItem item = (VisAmuletItem) stack.getItem();
            int capacity = item.maximumVis();
            Map<VisChannel, Integer> current = new EnumMap<>(VisChannel.class);
            Map<VisChannel, Integer> capacityMap = new EnumMap<>(VisChannel.class);
            for (VisChannel channel : VisChannel.values()) {
                // 夹一下：容量数据包变化后可能出现存量 > 容量，VisStorageView 会直接抛异常。
                current.put(channel, Math.min(item.vis(stack, channel), capacity));
                capacityMap.put(channel, capacity);
            }
            // 版本号 = 六个通道当前值的指纹。不能用 ItemStack#hashCode：
            // VisStorageView 要求 version >= 0，而 hashCode 可能为负 → IllegalArgumentException。
            long version = 0L;
            for (VisChannel channel : VisChannel.values()) {
                version = version * 31L + current.get(channel);
            }
            return new VisStorageView(current, capacityMap, version);
        }

        @Override
        public VisTransferResult extract(ServerPlayer player, ItemStack stack, VisCost cost, VisAction action) {
            VisAmuletItem item = (VisAmuletItem) stack.getItem();
            Map<VisChannel, Integer> requested = cost.amounts();
            // 契约：要么一次拿全，要么一点不动（前置不合并各部件的部分结果）。
            if (requested.isEmpty() || !item.contains(stack, requested)) {
                return VisTransferResult.none(cost);
            }
            if (action == VisAction.SIMULATE) {
                // 预演绝不许改动物品：前置会拿副本比对组件，变了就判定 FAILED。
                return new VisTransferResult(cost, requested, false);
            }
            item.consume(stack, requested);
            return new VisTransferResult(cost, requested, true);
        }
    }

    private RelicVisStorages() {
    }
}
