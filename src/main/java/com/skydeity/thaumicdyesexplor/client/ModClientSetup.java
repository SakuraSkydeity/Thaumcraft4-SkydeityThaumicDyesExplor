package com.skydeity.thaumicdyesexplor.client;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.skydeity.thaumicdyesexplor.ThaumicDyesExplor;
import com.skydeity.thaumicdyesexplor.item.ArmorGeoType;
import com.skydeity.thaumicdyesexplor.item.SealColors;
import com.skydeity.thaumicdyesexplor.item.SimpleArmorItem;
import com.skydeity.thaumicdyesexplor.registry.ModItems;

import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

/**
 * 客户端注册：护甲模型与物品染色。
 *
 * <p>两件事：</p>
 * <ol>
 *   <li><b>护甲模型</b> —— 把每件护甲按 {@link ArmorGeoType} 分组，注册一份
 *       {@link ArmorClientExtensions}；同一套几何的多件护甲共用一个扩展实例。</li>
 *   <li><b>封印染色</b> —— 源模组用 {@code getColorFromItemStack} 给灰白贴图染色
 *       （16 个 damage 变体），1.21.1 的等价钩子是 {@link RegisterColorHandlersEvent.Item}，
 *       颜色来源换成 {@code DataComponents.DYED_COLOR}。</li>
 * </ol>
 */
@EventBusSubscriber(modid = ThaumicDyesExplor.MOD_ID, value = Dist.CLIENT)
public final class ModClientSetup {

    /** 按几何类型注册护甲模型扩展。 */
    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        Map<ArmorGeoType, List<Item>> byGeo = new EnumMap<>(ArmorGeoType.class);
        for (var entry : ModItems.ITEMS.getEntries()) {
            Item item = entry.get();
            if (item instanceof SimpleArmorItem armor && armor.geoType() != ArmorGeoType.NONE) {
                byGeo.computeIfAbsent(armor.geoType(), k -> new ArrayList<>()).add(item);
            }
        }
        for (Map.Entry<ArmorGeoType, List<Item>> entry : byGeo.entrySet()) {
            IClientItemExtensions extensions = new ArmorClientExtensions(entry.getKey());
            event.registerItem(extensions, entry.getValue().toArray(new Item[0]));
        }
    }

    /** 封印认染料颜色。 */
    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) -> SealColors.tint(stack), ModItems.BLANK_SEAL.get());
    }

    private ModClientSetup() {
    }
}
