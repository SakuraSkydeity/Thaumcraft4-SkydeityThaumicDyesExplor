package com.skydeity.thaumicdyesexplor;

import com.mojang.logging.LogUtils;
import com.skydeity.thaumicdyesexplor.curio.ModCurios;
import com.skydeity.thaumicdyesexplor.recipe.ModRecipes;
import com.skydeity.thaumicdyesexplor.registry.ModArmorMaterials;
import com.skydeity.thaumicdyesexplor.registry.ModBlocks;
import com.skydeity.thaumicdyesexplor.registry.ModDataComponents;
import com.skydeity.thaumicdyesexplor.registry.ModEffects;
import com.skydeity.thaumicdyesexplor.registry.ModEntities;
import com.skydeity.thaumicdyesexplor.registry.ModItems;
import com.skydeity.thaumicdyesexplor.registry.ModMeItems;
import com.skydeity.thaumicdyesexplor.registry.ModRelicItems;
import com.skydeity.thaumicdyesexplor.registry.ModTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;

/**
 * Thaumic Dyes Explor —— 四个 1.7.10 TC4 附属模组向 Minecraft 1.21.1 NeoForge
 * （Thaumcraft 4R）的迁移整合：Thaumic Dyes、Thaumic Exploration、
 * Forgotten Relics（{@code reli_} 前缀）与 MiddleEarth-Thaumaturgy（{@code me_} 前缀）。
 *
 * <p>迁移节奏：先把物品本体（注册 / 图标 / 模型 / 名字）全部搬进来，
 * 再按 roadmap 逐步补行为、配方与研究。</p>
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
        // 两个新并入的源模组共用 ModItems.ITEMS 这条注册总线，
        // 这里调用 register() 只是为了触发类初始化（所有注册发生在静态初始化块里）。
        ModRelicItems.register();
        ModMeItems.register();
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlocks.BLOCK_ENTITIES.register(modEventBus);
        ModEntities.ENTITIES.register(modEventBus);
        ModRecipes.SERIALIZERS.register(modEventBus);
        ModTab.TABS.register(modEventBus);

        // 饰品的 Curios 物品能力必须在能力注册阶段挂上，晚于此阶段就没有 tick 了。
        modEventBus.addListener(ModCurios::registerCapabilities);
        // 湮灭罐 / 灵魂坩埚挂到前置的源质传输能力上（管道、蒸馏器、傀儡才认得它们）。
        modEventBus.addListener(com.skydeity.thaumicdyesexplor.compat.TxCapabilities::registerCapabilities);
        // 三件 vis 储存遗物注册进前置的自有 vis 储存系统（充能设施才认得它们）。
        // 注意：这一步要真正的 Item 实例，必须等物品注册事件跑完 ——
        // 在构造函数里直接 holder.get() 会抛
        // "Trying to access unbound value" 导致整个模组构造失败（启动闪退）。
        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(com.skydeity.thaumicdyesexplor.vis.RelicVisStorages::register);
        // 符文升级注魔的 input_patch 键必须在数据包配方解析之前注册好，
        // 否则 InfusionRecipe 解码时会因为「未注册的补丁」整条配方失败。
        event.enqueueWork(com.skydeity.thaumicdyesexplor.compat.RunicInfusionPatch::register);
    }
}
