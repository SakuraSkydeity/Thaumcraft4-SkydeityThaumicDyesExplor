package com.skydeity.thaumicdyesexplor.registry;

import java.util.List;

import com.skydeity.thaumicdyesexplor.ThaumicDyesExplor;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 创造模式物品栏标签：把两个源模组的全部装备与物品收进同一个标签页。
 *
 * <p>除了本模组自己注册的物品，这里还会<b>现场装配</b>若干把法杖 ——
 * 法杖不是独立物品，而是「{@code thaumcraft:wand} 这个载体 + 一组部件数据组件」，
 * 所以只能通过 {@code WandApi.createFullyCharged(rod, cap)} 造出成品再塞进标签页。</p>
 */
public final class ModTab {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ThaumicDyesExplor.MOD_ID);

    /**
     * 创造栏里陈列的成品法杖。
     *
     * <p>{@code rod} 是杖杆的材质 id，{@code cap} 是杖端的材质 id —— 都来自
     * {@code data_maps/item/wand_rods.json} / {@code wand_caps.json} 的 {@code material} 字段。
     * 杖杆是否算「大法杖」由 {@code #thaumcraft:staff_cores` 标签决定，此处不用额外声明。</p>
     *
     * @param rod      杖杆材质（如 {@code thaumicdyesexplor:amber_staff}）
     * @param cap      杖端材质（如 {@code thaumicdyesexplor:mechanist}）
     * @param nameKey  物品名语言键（法杖会自动拼名字，但那套模板把“杖端”放在最前面，
     *                 中文读起来是“机械杖端琥珀手杖”，所以这里直接给成品一个固定名字）
     * @param color    物品名颜色。成品法杖全都挂在同一个 {@code thaumcraft:wand} 物品上，
     *                 稀有度只有一个，不单独指定颜色的话八把杖全是同一个颜色，分不出谁是谁
     */
    private record WandPreset(String rod, String cap, String nameKey, int color) {
    }

    private static final List<WandPreset> WAND_PRESETS = List.of(
            new WandPreset("amber", "mechanist", "wand.amber_mechanist", 0xFFC24A),
            new WandPreset("amber_staff", "mechanist", "wand.amber_mechanist_staff", 0xFFD98A),
            new WandPreset("amber", "sojourner", "wand.amber_sojourner", 0xFFA62B),
            new WandPreset("transmutation", "mechanist", "wand.transmutation_mechanist", 0x7FB4FF),
            new WandPreset("transmutation", "sojourner", "wand.transmutation_sojourner", 0x9FD2FF),
            new WandPreset("transmutation_staff", "sojourner", "wand.transmutation_sojourner_staff", 0x6AA6F0),
            new WandPreset("bread", "sojourner", "wand.bread_sojourner", 0xE8C86A),
            new WandPreset("necro_staff", "mechanist", "wand.necro_mechanist_staff", 0xB98CE8));

    /** 创造栏图标：嬗变杖杆 + 旅者杖端的成品法杖。 */
    private static final WandPreset ICON_WAND = WAND_PRESETS.get(4);

    public static final java.util.function.Supplier<CreativeModeTab> MAIN = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.thaumicdyesexplor"))
                    .icon(ModTab::iconStack)
                    .displayItems((params, output) -> {
                        ModItems.ITEMS.getEntries().forEach(entry -> {
                            Item item = entry.get();
                            output.accept(item);
                        });
                        // 成品法杖放在最后，避免把「部件」和「成品」混在一起
                        for (WandPreset preset : WAND_PRESETS) {
                            ItemStack stack = assemble(preset);
                            if (stack != null) {
                                output.accept(stack);
                            }
                        }
                    })
                    .build());

    /**
     * 现场装配一把满充能的法杖。
     *
     * @return 装配失败的成品；部件都没注册（例如数据包没加载）时返回 {@code null}
     */
    private static ItemStack assemble(WandPreset preset) {
        try {
            ItemStack stack = dev.tc4port.thaumcraft.api.wand.WandApi.createFullyCharged(
                    dev.tc4port.thaumcraft.api.wand.WandMaterialId.of(id(preset.rod())),
                    dev.tc4port.thaumcraft.api.wand.WandMaterialId.of(id(preset.cap())));
            stack.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,
                    Component.translatable("item.thaumicdyesexplor." + preset.nameKey())
                            // 带 CUSTOM_NAME 的堆叠会被原版强制加上斜体（ItemStack#getTooltipLines），
                            // 在子组件上显式写 italic=false 才能盖过父级的 ITALIC 样式
                            .withStyle(style -> style.withItalic(false).withColor(preset.color())));
            return stack;
        } catch (RuntimeException exception) {
            ThaumicDyesExplor.LOGGER.warn("无法装配创造栏法杖 {} + {}: {}",
                    preset.rod(), preset.cap(), exception.toString());
            return null;
        }
    }

    private static ItemStack iconStack() {
        ItemStack stack = assemble(ICON_WAND);
        return stack != null ? stack : new ItemStack(ModItems.RUNIC_GOGGLES.get());
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ThaumicDyesExplor.MOD_ID, path);
    }

    private ModTab() {
    }
}
