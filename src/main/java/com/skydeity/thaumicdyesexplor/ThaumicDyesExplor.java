package com.skydeity.thaumicdyesexplor;

import com.mojang.logging.LogUtils;
import com.skydeity.thaumicdyesexplor.curio.ModCurios;
import com.skydeity.thaumicdyesexplor.recipe.ModRecipes;
import com.skydeity.thaumicdyesexplor.registry.ModArmorMaterials;
import com.skydeity.thaumicdyesexplor.registry.ModDataComponents;
import com.skydeity.thaumicdyesexplor.registry.ModEffects;
import com.skydeity.thaumicdyesexplor.registry.ModItems;
import com.skydeity.thaumicdyesexplor.registry.ModTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

/**
 * Thaumic Dyes Explor —— Thaumic Dyes 与 Thaumic Exploration 两个 1.7.10 TC4 附属模组
 * 向 Minecraft 1.21.1 NeoForge（Thaumcraft 4R）的迁移整合。
 *
 * <p>本模组只迁移「装备与物品」这一层：护甲、符文护甲、护目镜、旅者靴、探索装备与材料，
 * 以及它们的要素（Aspect）与装备能力（equipment abilities）数据。</p>
 */
@Mod(ThaumicDyesExplor.MOD_ID)
public class ThaumicDyesExplor {

    public static final String MOD_ID = "thaumicdyesexplor";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ThaumicDyesExplor(IEventBus modEventBus, ModContainer modContainer) {
        ModDataComponents.COMPONENTS.register(modEventBus);
        ModArmorMaterials.MATERIALS.register(modEventBus);
        ModEffects.EFFECTS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModRecipes.SERIALIZERS.register(modEventBus);
        ModTab.TABS.register(modEventBus);

        // 饰品的 Curios 物品能力必须在能力注册阶段挂上，晚于此阶段就没有 tick 了。
        modEventBus.addListener(ModCurios::registerCapabilities);
    }
}
