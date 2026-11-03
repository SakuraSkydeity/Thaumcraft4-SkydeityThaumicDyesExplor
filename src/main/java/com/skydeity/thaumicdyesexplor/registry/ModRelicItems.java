package com.skydeity.thaumicdyesexplor.registry;

import java.util.List;

import com.skydeity.thaumicdyesexplor.item.ArmorGeoType;
import com.skydeity.thaumicdyesexplor.item.SimpleArmorItem;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * Forgotten Relics（1.7.10 TC4 附属）物品注册表，注册 id 统一加 {@code reli_} 前缀。
 *
 * <p>本轮只注册物品本体（图标 / 模型 / 名字），符文剑与王冠的特殊行为见
 * {@code .none/roadmap.md}。源模组里除采矿护符（uncommon）外全部是 epic 稀有度。</p>
 *
 * <p>贴图映射（源文件名 → 新 id）中值得注意的是三件“换皮”物品：
 * 星云核心（Arcanum）、沉睡的星云核心（Dormant Arcanum）、
 * 永恒放逐咒令（Overthrower，源贴图文件名是一句吐槽）。</p>
 */
public final class ModRelicItems {

    private static final net.neoforged.neoforge.registries.DeferredRegister.Items ITEMS = ModItems.ITEMS;

    // ============================================================================================
    // 普通遗物（图腾 / 法书 / 戒指 / 核心等，源模组全是 epic）
    // ============================================================================================
    // ----------------------------------------------------------------------------------------
    // 有独立行为的遗物（饰品行为接 Curios：curio/RelicCurios；剑与经验书见 item/RelicWeapons）
    // ----------------------------------------------------------------------------------------
    /** 命运之书：死亡时消耗 vis 免死（源模组 update handler 的免死分支）。 */
    public static final DeferredItem<Item> FATE_TOME = simple("reli_fate_tome");
    public static final DeferredItem<Item> TELEPORTATION_TOME = simple("reli_teleportation_tome");
    /** 虚妄正义：拿在背包里就把双方伤害翻倍（见 event/RelicItemEvents）。 */
    public static final DeferredItem<Item> FALSE_JUSTICE = simple("reli_false_justice");
    /** 远古神盾：腰带位，免疫击退 + 持续回血（见 curio/RelicCurios）。 */
    public static final DeferredItem<Item> ANCIENT_AEGIS = simple("reli_ancient_aegis");
    /** 混沌核心：被动随机改伤 + 随机药水（见 event/RelicItemEvents）。 */
    public static final DeferredItem<Item> CHAOS_CORE = simple("reli_chaos_core");
    public static final DeferredItem<Item> DARK_SUN_RING = simple("reli_dark_sun_ring");
    public static final DeferredItem<Item> SUPERPOSITION_RING = simple("reli_superposition_ring");
    public static final DeferredItem<Item> SHINY_STONE = simple("reli_shiny_stone");
    public static final DeferredItem<Item> DEIFIC_AMULET = simple("reli_deific_amulet");
    public static final DeferredItem<Item> OBLIVION_AMULET = simple("reli_oblivion_amulet");
    public static final DeferredItem<Item> MINING_CHARM = simple("reli_mining_charm", Rarity.UNCOMMON);
    public static final DeferredItem<Item> ADVANCED_MINING_CHARM = simple("reli_advanced_mining_charm");

    /** 悖论（剑）：源材质 PARADOXICALSTUFF，左键伤害随机并反噬差额。 */
    public static final DeferredItem<Item> PARADOX = ITEMS.registerItem("reli_paradox",
            props -> new com.skydeity.thaumicdyesexplor.item.RelicWeapons.ParadoxItem(props),
            new Item.Properties().rarity(Rarity.EPIC).stacksTo(1));

    /** 荒芜铺展者（剑）：同材质；源模组左键机关在源码中为空实现。 */
    public static final DeferredItem<Item> WASTELAYER = ITEMS.registerItem("reli_wastelayer",
            props -> new com.skydeity.thaumicdyesexplor.item.RelicWeapons.WastelayerItem(props),
            new Item.Properties().rarity(Rarity.EPIC).stacksTo(1));

    /** 永恒智慧之书：右键吸经验、潜行右键释放。 */
    public static final DeferredItem<Item> XP_TOME = ITEMS.registerItem("reli_xp_tome",
            props -> new com.skydeity.thaumicdyesexplor.item.RelicWeapons.XpTomeItem(props),
            new Item.Properties().rarity(Rarity.EPIC).stacksTo(1));

    // ----------------------------------------------------------------------------------------
    // 普通遗物（图腾 / 法书等，无独立行为，源模组全是 epic）
    // ----------------------------------------------------------------------------------------
    public static final List<DeferredItem<Item>> RELICS = List.of(
            simple("reli_dormant_arcanum"),
            simple("reli_telekinesis_tome"),
            simple("reli_missile_tome"),
            simple("reli_crimson_spell"),
            simple("reli_ghastly_skull"),
            simple("reli_obelisk_drainer"),
            simple("reli_eldritch_spell"),
            simple("reli_lunar_flares"),
            simple("reli_soul_tome"),
            simple("reli_dimensional_mirror"),
            simple("reli_weather_stone"),
            simple("reli_apotheosis"),
            simple("reli_chaos_tome"),
            simple("reli_thunderpeal"),
            simple("reli_overthrower"),
            simple("reli_discord_ring"),
            simple("reli_void_grimoire"));

    /** 恐惧王冠（头盔）：源材质 NOBLEGOLD（3/9/6/3），源贴图本身就是标准 64x32 人形布局。 */
    public static final DeferredItem<SimpleArmorItem> TERROR_CROWN = ITEMS.registerItem("reli_terror_crown",
            props -> new SimpleArmorItem(ModArmorMaterials.TERROR_CROWN, ArmorItem.Type.HELMET,
                    props, ArmorGeoType.NONE),
            new Item.Properties().rarity(Rarity.EPIC).stacksTo(1));

    // ============================================================================================
    // vis 储存类：星云核心 / 欧米茄核心 / 遗忘基石 —— 直接继承前置的 VisAmuletItem
    // （等于自带「从法杖充能 / 给法杖放 vis」的全部交互），容量按源模组定位分三档。
    // ============================================================================================
    public static final DeferredItem<dev.tc4port.thaumcraft.api.vis.VisAmuletItem> ARCANUM =
            ITEMS.registerItem("reli_arcanum",
                    props -> new VisAmulet(props, 25_000, "item.thaumicdyesexplor.reli_arcanum"),
                    new Item.Properties().rarity(Rarity.EPIC).stacksTo(1));
    public static final DeferredItem<dev.tc4port.thaumcraft.api.vis.VisAmuletItem> OMEGA_CORE =
            ITEMS.registerItem("reli_omega_core",
                    props -> new VisAmulet(props, 250_000, "item.thaumicdyesexplor.reli_omega_core"),
                    new Item.Properties().rarity(Rarity.EPIC).stacksTo(1));
    public static final DeferredItem<dev.tc4port.thaumcraft.api.vis.VisAmuletItem> OBLIVION_STONE =
            ITEMS.registerItem("reli_oblivion_stone",
                    props -> new VisAmulet(props, 2_500_000, "item.thaumicdyesexplor.reli_oblivion_stone"),
                    new Item.Properties().rarity(Rarity.EPIC).stacksTo(1));

    /**
     * 前置 VisAmuletItem 的薄封装，容量按物品档位决定。
     *
     * <p><b>单位是 centivis</b>（1 vis = 100 centivis）：星云核心 250 vis、
     * 欧米茄核心 2500 vis、遗忘基石 25000 vis。</p>
     *
     * <p><b>第三个参数是 descriptionId（名字翻译键），不是容量提示文本</b>——
     * 前置自己就传 {@code "item.ItemAmuletVis.0.name"}。早期这里传了
     * {@code "250"}，导致三件物品的游戏内名字直接显示成「250」，
     * 而 lang 里的 {@code item.thaumicdyesexplor.reli_*} 完全没被用上。</p>
     */
    private static final class VisAmulet extends dev.tc4port.thaumcraft.api.vis.VisAmuletItem {

        VisAmulet(Item.Properties properties, int centivis, String nameKey) {
            super(properties, centivis, nameKey);
        }
    }

    private static DeferredItem<Item> simple(String id) {
        return simple(id, Rarity.EPIC);
    }

    /**
     * 普通遗物：堆叠 1、固定稀有度，并在 tooltip 上常驻一行「怎么用」。
     *
     * <p>用法键由物品路径派生（{@code tooltip.thaumicdyesexplor.usage.<路径>}），
     * 写错只会看到键名本身，不会崩游戏。</p>
     */
    private static DeferredItem<Item> simple(String id, Rarity rarity) {
        return ITEMS.registerItem(id,
                props -> new com.skydeity.thaumicdyesexplor.item.RelicItem(props, usageKey(id)),
                new Item.Properties().rarity(rarity).stacksTo(1));
    }

    /** 遗物用法 tooltip 的翻译键。 */
    public static String usageKey(String id) {
        return "tooltip.thaumicdyesexplor.usage." + id;
    }

    /**
     * 剑类遗物。源材质数值与原版不一致（效率 16、耐久 3000、附加伤害 -4 但伤害随机），
     * 这里先用钻石剑面板占位，数值对齐放在后续轮次。
     */
    private static DeferredItem<Item> sword(String id) {
        return ITEMS.registerItem(id,
                props -> new SwordItem(Tiers.DIAMOND,
                        props.attributes(SwordItem.createAttributes(Tiers.DIAMOND, 3, -2.4F))),
                new Item.Properties().rarity(Rarity.EPIC).stacksTo(1));
    }

    /** 强制类初始化：把本类全部物品挂进物品注册总线（在模组构造器里调用）。 */
    public static void register() {
    }

    private ModRelicItems() {
    }
}
