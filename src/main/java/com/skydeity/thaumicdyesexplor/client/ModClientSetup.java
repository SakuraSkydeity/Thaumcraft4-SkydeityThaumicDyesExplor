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

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.DyedItemColor;
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

    /**
     * 物品图标染色：封印认染料颜色，<b>可染色护甲的图标也跟着染料走</b>。
     *
     * <p>1.7.10 的护甲图标是两遍渲染（{@code iconChest} 打底 + {@code iconChestOver} 乘染料色），
     * 这里合并成一张图，所以必须自己把 {@code DYED_COLOR} 乘上去 —— 否则染完色的护甲在背包里
     * 还是灰白图标（第 24 轮用户报的「染色后贴图不对」就是这个）。</p>
     *
     * <p>没有 {@code DYED_COLOR} 时返回 {@code -1}（= 不上色），保持贴图原样：
     * 未染色的血腥游侠用的是已经画好红漆的图标，不能再乘一层默认色。</p>
     */
    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) -> SealColors.tint(stack), ModItems.BLANK_SEAL.get());
        for (var entry : ModItems.ITEMS.getEntries()) {
            if (entry.get() instanceof ArmorItem armor && hasClothLayer(armor)) {
                event.register((stack, tintIndex) ->
                        tintIndex == 0 ? 0xFF000000 | DyedItemColor.getOrDefault(stack, -1) : -1, armor);
            }
        }
    }

    /** 这套护甲是否有可染色的布料层（只有这类护甲的图标需要乘染料色）。 */
    private static boolean hasClothLayer(ArmorItem item) {
        for (ArmorMaterial.Layer layer : item.getMaterial().value().layers()) {
            if (layer.dyeable()) {
                return true;
            }
        }
        return false;
    }

    /** 遗物弹丸的 billboard 渲染器。 */
    @SubscribeEvent
    public static void registerRenderers(net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(
                com.skydeity.thaumicdyesexplor.registry.ModEntities.RELIC_PROJECTILE.get(),
                RelicProjectileRenderer::new);
    }

    private ModClientSetup() {
    }
}
