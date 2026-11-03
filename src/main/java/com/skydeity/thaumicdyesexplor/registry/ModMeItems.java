package com.skydeity.thaumicdyesexplor.registry;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * MiddleEarth-Thaumaturgy（1.7.10 TC4 附属）物品注册表，注册 id 统一加 {@code me_} 前缀。
 *
 * <p>源模组的杖柄 / 杖芯 / 杖端都是「一个物品 + metadata 变体」，1.21.1 没有 metadata，
 * 这里一律拆成独立 id：{@code me_wand_core_gondor} 等。24 个派系名来自源模组
 * {@code WandType.typesName}。</p>
 *
 * <p><b>24 枚召唤焦点已在第 22 轮整组删除</b>：源模组靠 LOTR 的
 * {@code LOTRLevelData#getAlignment} 判断能不能召唤，1.21.1 没有 LOTR，也没有可替代的结盟数据源，
 * 留着只是 24 件点不动的收藏品（用户要求「需要 LOTR 前置的核心全删」）。</p>
 *
 * <p>源模组 {@code ItemMaterial} 对全部 13 个材料子项开 {@code hasEffect}（附魔光效），
 * 对应这里的 {@link GlintItem}。杖柄 / 杖芯 / 杖端后续要接入前置的法杖部件
 * datamap（{@code wand_rods.json} 等），本轮先作为普通材料物品注册，见 roadmap。</p>
 */
public final class ModMeItems {

    private static final net.neoforged.neoforge.registries.DeferredRegister.Items ITEMS = ModItems.ITEMS;

    /** 24 个派系（WandType.typesName，全小写）。 */
    public static final String[] FACTIONS = {
            "gondor", "hobbit", "ranger_north", "blue_mountains", "high_elf", "gundabad",
            "angmar", "wood_elf", "dol_guldur", "dale", "dwarf", "galadhrim",
            "dunland", "uruk_hai", "fangorn", "rohan", "mordor", "dorwinion",
            "rhun", "near_harad", "moredain", "tauredain", "half_troll", "bree"};

    /** 10 种杖端金属（WandCap 名）。 */
    public static final String[] CAPS = {
            "mithril", "orcsteel", "dwarvensteel", "galvorn", "uruksteel",
            "morgulsteel", "bluedwarvensteel", "blackuruksteel", "elvensteel", "gildediron"};

    /** 10 种注入金属锭 / 粒（ItemMaterial 与 ItemMaterialNugget 的前 10 个子项）。 */
    public static final String[] MATERIALS = {
            "gulduril_black_uruk", "ithildin_blue_dwarven", "ithildin_dwarven", "edhelmir_elven",
            "daedelos_galvorn", "stared_mithril", "daedelos_morgul", "durnaur_orc",
            "gulduril_uruk", "brighten_gildediron"};

    // ============================================================================================
    // 派系法杖部件（后续接 wand datamap）与召唤焦点
    // ============================================================================================
    public static final List<DeferredItem<Item>> WAND_CORES = new ArrayList<>();
    public static final List<DeferredItem<Item>> STAFF_CORES = new ArrayList<>();

    public static final List<DeferredItem<Item>> WAND_CAPS = new ArrayList<>();
    public static final List<DeferredItem<Item>> INGOTS = new ArrayList<>();
    public static final List<DeferredItem<Item>> NUGGETS = new ArrayList<>();

    /**
     * 有法杖部件的派系：全部 24 个派系减去布理。
     *
     * <p>布理的杖柄与杖芯在第 20 轮按用户要求删除（源模组里布理本来也没有专属外观），
     * 所以 {@link #FACTIONS} 保留 24 项派系名（材料 / 存储块按全套走），
     * 只有法杖部件列表跳过布理。</p>
     */
    public static final java.util.List<String> WAND_FACTIONS = java.util.Arrays.stream(FACTIONS)
            .filter(faction -> !"bree".equals(faction))
            .toList();

    static {
        for (String faction : WAND_FACTIONS) {
            WAND_CORES.add(wandPart("me_wand_core_" + faction));
            STAFF_CORES.add(wandPart("me_staff_core_" + faction));
        }
        for (String cap : CAPS) {
            WAND_CAPS.add(wandPart("me_wand_cap_" + cap));
        }
        for (String mat : MATERIALS) {
            INGOTS.add(glint("me_material_" + mat));
            NUGGETS.add(glint("me_nugget_" + mat));
        }
    }

    // ============================================================================================
    // 罐子系统材料（源 ItemMaterial 的后 3 个子项）
    // ============================================================================================
    public static final DeferredItem<Item> JAR_BINDER = glint("me_jar_binder");
    public static final DeferredItem<Item> JAR_BINDER_ADVANCED = glint("me_jar_binder_advanced");
    public static final DeferredItem<Item> EXTREME_JAR_CAP = glint("me_extreme_jar_cap");

    /**
     * 法杖核心：基岩粉碎者（源 {@code ItemBedrockRemoverFocus}）。
     *
     * <p>手持对基岩右键，扣 10 气 / 10 土 / 5 秩序 / 20 混沌 vis 后挖掉基岩，
     * 世界最低层挖不动（见 {@code item/BedrockRemoverFocusItem}）。</p>
     */
    public static final DeferredItem<Item> FOCUS_BEDROCK_REMOVER = ITEMS.registerItem(
            "me_focus_bedrock_remover",
            props -> new com.skydeity.thaumicdyesexplor.item.BedrockRemoverFocusItem(props),
            new Item.Properties().stacksTo(1));

    /**
     * 扭曲修正之戒（源 {@code ItemRingWarpward}，戒指槽）：佩戴时持续挂上前置的「扭曲抗性」效果，
     * vis 消耗 -5%（数据表 {@code equipment_abilities}，见 {@code curio/RelicCurios}）。
     */
    public static final DeferredItem<Item> RING_WARPWARD = ITEMS.registerItem(
            "me_ring_warpward",
            props -> new com.skydeity.thaumicdyesexplor.item.RelicItem(props,
                    "tooltip.thaumicdyesexplor.usage.me_ring_warpward"),
            new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC).stacksTo(1));

    /** 模组徽记物品（源 iconMiddleEarthThaumaturgy，仅作收藏）。 */
    public static final DeferredItem<Item> ICON = plain("me_icon");

    private static DeferredItem<Item> plain(String id) {
        return ITEMS.registerItem(id, props -> new Item(props), new Item.Properties());
    }

    /**
     * 法杖部件（杖柄 / 杖芯 / 杖端）：容量、消耗修正与贴图全在
     * {@code data/thaumcraft/data_maps/item/wand_rods.json} / {@code wand_caps.json} 里，
     * 这里只提供 tooltip（通过公开 API {@code WandPartApi} 读取）。
     * 装配走前置自带的 {@code thaumcraft:wand_assembly} / {@code staff_assembly} 配方类型。
     */
    private static DeferredItem<Item> wandPart(String id) {
        return ITEMS.registerItem(id, com.skydeity.thaumicdyesexplor.item.WandPartItem::new,
                new Item.Properties().stacksTo(64));
    }

    /** 带附魔光效的物品（源模组 ItemMaterial#hasEffect 恒为 true）。 */
    private static DeferredItem<Item> glint(String id) {
        return ITEMS.registerItem(id, props -> new GlintItem(props), new Item.Properties());
    }

    /** 强制类初始化：把本类全部物品挂进物品注册总线（在模组构造器里调用）。 */
    public static void register() {
    }

    /** 恒定附魔光效的普通物品。 */
    public static class GlintItem extends Item {
        public GlintItem(Properties properties) {
            super(properties);
        }

        @Override
        public boolean isFoil(ItemStack stack) {
            return true;
        }
    }

    private ModMeItems() {
    }
}
