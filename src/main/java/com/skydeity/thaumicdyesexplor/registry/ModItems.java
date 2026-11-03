package com.skydeity.thaumicdyesexplor.registry;

import com.skydeity.thaumicdyesexplor.ThaumicDyesExplor;
import com.skydeity.thaumicdyesexplor.curio.CurioItem;
import com.skydeity.thaumicdyesexplor.curio.StabilizerBeltHandler;
import com.skydeity.thaumicdyesexplor.curio.TalismanFoodHandler;
import com.skydeity.thaumicdyesexplor.item.ArmorGeoType;
import com.skydeity.thaumicdyesexplor.item.RunicArmorItem;
import com.skydeity.thaumicdyesexplor.item.RunicTravellerBootsItem;
import com.skydeity.thaumicdyesexplor.item.SimpleArmorItem;
import com.skydeity.thaumicdyesexplor.item.TravellerBootsItem;
import com.skydeity.thaumicdyesexplor.item.WandPartItem;

import java.util.List;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 物品注册表：整合 Thaumic Dyes（TD）与 Thaumic Exploration（TE）两个源模组的全部装备与物品。
 *
 * <p>命名规则：注册 id 沿用源模组的原始 id（小写下划线），避免与既有存档/配方冲突。</p>
 *
 * <h2>三件套的“三个维度”</h2>
 * <p>1.21.1 里一件护甲的外观由三样东西共同决定，注册时三者必须一一对应：</p>
 * <ol>
 *   <li><b>材质</b>（{@link ModArmorMaterials}）—— 决定防御值、附魔度、音效、以及<b>贴图</b>
 *       （{@code ArmorMaterial.Layer} 的 {@code *_layer_1/2.png}）；</li>
 *   <li><b>几何</b>（{@link ArmorGeoType}）—— 决定<b>模型</b>（源模组的 Tabula 模型，256×128 贴图
 *       配 128×64 UV）；{@link ArmorGeoType#NONE} 表示用原版人形模型（64×32 贴图）；</li>
 *   <li><b>行为</b>（{@link SimpleArmorItem} / {@link RunicArmorItem} / {@link TravellerBootsItem}）
 *       —— 符文升级、护盾充能、移动加成等。</li>
 * </ol>
 * <p>“材质”与“几何”必须成对出现：同一套外观不能既用 256×128 贴图又用原版模型，
 * 否则 UV 会整体错位（这正是移植初期贴图错乱的根因）。</p>
 */
public final class ModItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ThaumicDyesExplor.MOD_ID);

    // ============================================================================================
    // Thaumic Dyes —— 染袍（Cultist Robe Dyed）：ModelRobes
    // ============================================================================================
    public static final DeferredItem<SimpleArmorItem> CULTIST_ROBE_HELMET = armor("cultist_robe_helmet",
            ModArmorMaterials.CULTIST_ROBE, ArmorItem.Type.HELMET, Rarity.UNCOMMON, ArmorGeoType.ROBES);
    public static final DeferredItem<SimpleArmorItem> CULTIST_ROBE_CHESTPLATE = armor("cultist_robe_chestplate",
            ModArmorMaterials.CULTIST_ROBE, ArmorItem.Type.CHESTPLATE, Rarity.UNCOMMON, ArmorGeoType.ROBES);
    public static final DeferredItem<SimpleArmorItem> CULTIST_ROBE_LEGGINGS = armor("cultist_robe_leggings",
            ModArmorMaterials.CULTIST_ROBE, ArmorItem.Type.LEGGINGS, Rarity.UNCOMMON, ArmorGeoType.ROBES);

    // ============================================================================================
    // Thaumic Dyes —— 神秘法袍（Thaumium Robe Dyed）：ModelRobes
    // ============================================================================================
    public static final DeferredItem<SimpleArmorItem> THAUMIUM_ROBE_HELMET = armor("thaumium_robe_helmet",
            ModArmorMaterials.THAUMIUM_ROBE, ArmorItem.Type.HELMET, Rarity.RARE, ArmorGeoType.ROBES);
    public static final DeferredItem<SimpleArmorItem> THAUMIUM_ROBE_CHESTPLATE = armor("thaumium_robe_chestplate",
            ModArmorMaterials.THAUMIUM_ROBE, ArmorItem.Type.CHESTPLATE, Rarity.RARE, ArmorGeoType.ROBES);
    public static final DeferredItem<SimpleArmorItem> THAUMIUM_ROBE_LEGGINGS = armor("thaumium_robe_leggings",
            ModArmorMaterials.THAUMIUM_ROBE, ArmorItem.Type.LEGGINGS, Rarity.RARE, ArmorGeoType.ROBES);

    // ============================================================================================
    // Thaumic Dyes —— 虚空法袍（Void Robe Dyed）：ModelRobes
    // ============================================================================================
    public static final DeferredItem<SimpleArmorItem> VOID_ROBE_HELMET = armor("void_robe_helmet",
            ModArmorMaterials.VOID_ROBE, ArmorItem.Type.HELMET, Rarity.EPIC, ArmorGeoType.ROBES);
    public static final DeferredItem<SimpleArmorItem> VOID_ROBE_CHESTPLATE = armor("void_robe_chestplate",
            ModArmorMaterials.VOID_ROBE, ArmorItem.Type.CHESTPLATE, Rarity.EPIC, ArmorGeoType.ROBES);
    public static final DeferredItem<SimpleArmorItem> VOID_ROBE_LEGGINGS = armor("void_robe_leggings",
            ModArmorMaterials.VOID_ROBE, ArmorItem.Type.LEGGINGS, Rarity.EPIC, ArmorGeoType.ROBES);

    // ============================================================================================
    // Thaumic Dyes —— 堡垒甲（Thaumium Fortress Dyed）：ModelFortress
    // ============================================================================================
    public static final DeferredItem<SimpleArmorItem> FORTRESS_HELMET = armor("fortress_helmet",
            ModArmorMaterials.FORTRESS, ArmorItem.Type.HELMET, Rarity.RARE, ArmorGeoType.FORTRESS);
    public static final DeferredItem<SimpleArmorItem> FORTRESS_CHESTPLATE = armor("fortress_chestplate",
            ModArmorMaterials.FORTRESS, ArmorItem.Type.CHESTPLATE, Rarity.RARE, ArmorGeoType.FORTRESS);
    public static final DeferredItem<SimpleArmorItem> FORTRESS_LEGGINGS = armor("fortress_leggings",
            ModArmorMaterials.FORTRESS, ArmorItem.Type.LEGGINGS, Rarity.RARE, ArmorGeoType.FORTRESS);

    // ============================================================================================
    // Thaumic Dyes —— 领袖甲（Cultist Praetor Dyed）：ModelPraetor
    // ============================================================================================
    public static final DeferredItem<SimpleArmorItem> CULTIST_LEADER_HELMET = armor("cultist_leader_helmet",
            ModArmorMaterials.CULTIST_LEADER, ArmorItem.Type.HELMET, Rarity.RARE, ArmorGeoType.PRAETOR);
    public static final DeferredItem<SimpleArmorItem> CULTIST_LEADER_CHESTPLATE = armor("cultist_leader_chestplate",
            ModArmorMaterials.CULTIST_LEADER, ArmorItem.Type.CHESTPLATE, Rarity.RARE, ArmorGeoType.PRAETOR);
    public static final DeferredItem<SimpleArmorItem> CULTIST_LEADER_LEGGINGS = armor("cultist_leader_leggings",
            ModArmorMaterials.CULTIST_LEADER, ArmorItem.Type.LEGGINGS, Rarity.RARE, ArmorGeoType.PRAETOR);

    // ============================================================================================
    // Thaumic Dyes —— 骑士甲（Cultist Knight Dyed）：ModelKnight
    // ============================================================================================
    public static final DeferredItem<SimpleArmorItem> CULTIST_KNIGHT_HELMET = armor("cultist_knight_helmet",
            ModArmorMaterials.CULTIST_KNIGHT, ArmorItem.Type.HELMET, Rarity.UNCOMMON, ArmorGeoType.KNIGHT);
    public static final DeferredItem<SimpleArmorItem> CULTIST_KNIGHT_CHESTPLATE = armor("cultist_knight_chestplate",
            ModArmorMaterials.CULTIST_KNIGHT, ArmorItem.Type.CHESTPLATE, Rarity.UNCOMMON, ArmorGeoType.KNIGHT);
    public static final DeferredItem<SimpleArmorItem> CULTIST_KNIGHT_LEGGINGS = armor("cultist_knight_leggings",
            ModArmorMaterials.CULTIST_KNIGHT, ArmorItem.Type.LEGGINGS, Rarity.UNCOMMON, ArmorGeoType.KNIGHT);

    // 圣骑士头盔（ModelPaladin，只有头盔）
    public static final DeferredItem<SimpleArmorItem> CULTIST_PALADIN_HELMET = armor("cultist_paladin_helmet",
            ModArmorMaterials.CULTIST_PALADIN, ArmorItem.Type.HELMET, Rarity.RARE, ArmorGeoType.PALADIN);

    // ============================================================================================
    // Thaumic Dyes —— 寄居板甲（Inhabited Plate）：ModelKnight + 腐尸板甲贴图
    // ============================================================================================
    public static final DeferredItem<SimpleArmorItem> INHABITED_PLATE_HELMET = armor("inhabited_plate_helmet",
            ModArmorMaterials.INHABITED_PLATE, ArmorItem.Type.HELMET, Rarity.RARE, ArmorGeoType.KNIGHT);
    public static final DeferredItem<SimpleArmorItem> INHABITED_PLATE_CHESTPLATE = armor("inhabited_plate_chestplate",
            ModArmorMaterials.INHABITED_PLATE, ArmorItem.Type.CHESTPLATE, Rarity.RARE, ArmorGeoType.KNIGHT);
    public static final DeferredItem<SimpleArmorItem> INHABITED_PLATE_LEGGINGS = armor("inhabited_plate_leggings",
            ModArmorMaterials.INHABITED_PLATE, ArmorItem.Type.LEGGINGS, Rarity.RARE, ArmorGeoType.KNIGHT);

    // ============================================================================================
    // Thaumic Dyes —— 太阳骑士（Sun Knight）：ModelKnight + 太阳饰板贴图
    // ============================================================================================
    public static final DeferredItem<SimpleArmorItem> SUN_KNIGHT_HELMET = armor("sun_knight_helmet",
            ModArmorMaterials.SUN_KNIGHT, ArmorItem.Type.HELMET, Rarity.RARE, ArmorGeoType.KNIGHT);
    public static final DeferredItem<SimpleArmorItem> SUN_KNIGHT_CHESTPLATE = armor("sun_knight_chestplate",
            ModArmorMaterials.SUN_KNIGHT, ArmorItem.Type.CHESTPLATE, Rarity.RARE, ArmorGeoType.KNIGHT);

    // ============================================================================================
    // Thaumic Dyes —— 铁堡垒甲（Iron Fortress）：ModelIronFortress
    // ============================================================================================
    public static final DeferredItem<SimpleArmorItem> IRON_FORTRESS_HELMET = armor("iron_fortress_helmet",
            ModArmorMaterials.IRON_FORTRESS, ArmorItem.Type.HELMET, Rarity.UNCOMMON, ArmorGeoType.IRON_FORTRESS);
    public static final DeferredItem<SimpleArmorItem> IRON_FORTRESS_CHESTPLATE = armor("iron_fortress_chestplate",
            ModArmorMaterials.IRON_FORTRESS, ArmorItem.Type.CHESTPLATE, Rarity.UNCOMMON, ArmorGeoType.IRON_FORTRESS);
    public static final DeferredItem<SimpleArmorItem> IRON_FORTRESS_LEGGINGS = armor("iron_fortress_leggings",
            ModArmorMaterials.IRON_FORTRESS, ArmorItem.Type.LEGGINGS, Rarity.UNCOMMON, ArmorGeoType.IRON_FORTRESS);

    // ============================================================================================
    // Thaumic Dyes —— 游侠甲：未染色（CultistRanger → ModelRanger2）/ 已染色（→ ModelRanger）
    // ============================================================================================
    public static final DeferredItem<SimpleArmorItem> CULTIST_RANGER_HELMET = armor("cultist_ranger_helmet",
            ModArmorMaterials.CULTIST_RANGER_PLAIN, ArmorItem.Type.HELMET, Rarity.UNCOMMON, ArmorGeoType.RANGER2);
    public static final DeferredItem<SimpleArmorItem> CULTIST_RANGER_CHESTPLATE = armor("cultist_ranger_chestplate",
            ModArmorMaterials.CULTIST_RANGER_PLAIN, ArmorItem.Type.CHESTPLATE, Rarity.UNCOMMON, ArmorGeoType.RANGER2);
    public static final DeferredItem<SimpleArmorItem> CULTIST_RANGER_LEGGINGS = armor("cultist_ranger_leggings",
            ModArmorMaterials.CULTIST_RANGER_PLAIN, ArmorItem.Type.LEGGINGS, Rarity.UNCOMMON, ArmorGeoType.RANGER2);

    public static final DeferredItem<SimpleArmorItem> CULTIST_RANGER_DYED_HELMET = armor("cultist_ranger_dyed_helmet",
            ModArmorMaterials.CULTIST_RANGER, ArmorItem.Type.HELMET, Rarity.UNCOMMON, ArmorGeoType.RANGER);
    public static final DeferredItem<SimpleArmorItem> CULTIST_RANGER_DYED_CHESTPLATE = armor("cultist_ranger_dyed_chestplate",
            ModArmorMaterials.CULTIST_RANGER, ArmorItem.Type.CHESTPLATE, Rarity.UNCOMMON, ArmorGeoType.RANGER);
    public static final DeferredItem<SimpleArmorItem> CULTIST_RANGER_DYED_LEGGINGS = armor("cultist_ranger_dyed_leggings",
            ModArmorMaterials.CULTIST_RANGER, ArmorItem.Type.LEGGINGS, Rarity.UNCOMMON, ArmorGeoType.RANGER);

    // ============================================================================================
    // Thaumic Dyes —— 弓箭手甲：未染色 / 已染色（都用 ModelArcher）
    // ============================================================================================
    public static final DeferredItem<SimpleArmorItem> CULTIST_ARCHER_HELMET = armor("cultist_archer_helmet",
            ModArmorMaterials.CULTIST_ARCHER_PLAIN, ArmorItem.Type.HELMET, Rarity.UNCOMMON, ArmorGeoType.ARCHER);
    public static final DeferredItem<SimpleArmorItem> CULTIST_ARCHER_CHESTPLATE = armor("cultist_archer_chestplate",
            ModArmorMaterials.CULTIST_ARCHER_PLAIN, ArmorItem.Type.CHESTPLATE, Rarity.UNCOMMON, ArmorGeoType.ARCHER);
    public static final DeferredItem<SimpleArmorItem> CULTIST_ARCHER_LEGGINGS = armor("cultist_archer_leggings",
            ModArmorMaterials.CULTIST_ARCHER_PLAIN, ArmorItem.Type.LEGGINGS, Rarity.UNCOMMON, ArmorGeoType.ARCHER);

    public static final DeferredItem<SimpleArmorItem> CULTIST_ARCHER_DYED_HELMET = armor("cultist_archer_dyed_helmet",
            ModArmorMaterials.CULTIST_ARCHER, ArmorItem.Type.HELMET, Rarity.UNCOMMON, ArmorGeoType.ARCHER);
    public static final DeferredItem<SimpleArmorItem> CULTIST_ARCHER_DYED_CHESTPLATE = armor("cultist_archer_dyed_chestplate",
            ModArmorMaterials.CULTIST_ARCHER, ArmorItem.Type.CHESTPLATE, Rarity.UNCOMMON, ArmorGeoType.ARCHER);
    public static final DeferredItem<SimpleArmorItem> CULTIST_ARCHER_DYED_LEGGINGS = armor("cultist_archer_dyed_leggings",
            ModArmorMaterials.CULTIST_ARCHER, ArmorItem.Type.LEGGINGS, Rarity.UNCOMMON, ArmorGeoType.ARCHER);

    // ============================================================================================
    // Thaumic Dyes —— 织物材料（Fabric Resource）
    // ============================================================================================
    public static final DeferredItem<Item> FABRIC_RESOURCE = ITEMS.registerSimpleItem("fabric_resource",
            new Item.Properties().rarity(Rarity.UNCOMMON));

    // ============================================================================================
    // Thaumic Dyes —— 符文护甲 I（Runic）：原版人形模型 + 64×32 贴图
    // ============================================================================================
    public static final DeferredItem<RunicArmorItem> RUNIC_HELMET = runicArmor("runic_helmet",
            ModArmorMaterials.RUNIC, ArmorItem.Type.HELMET, ArmorGeoType.NONE, 1);
    public static final DeferredItem<RunicArmorItem> RUNIC_CHESTPLATE = runicArmor("runic_chestplate",
            ModArmorMaterials.RUNIC, ArmorItem.Type.CHESTPLATE, ArmorGeoType.NONE, 1);
    public static final DeferredItem<RunicArmorItem> RUNIC_LEGGINGS = runicArmor("runic_leggings",
            ModArmorMaterials.RUNIC, ArmorItem.Type.LEGGINGS, ArmorGeoType.NONE, 1);
    public static final DeferredItem<RunicArmorItem> RUNIC_BOOTS = runicArmor("runic_boots",
            ModArmorMaterials.RUNIC, ArmorItem.Type.BOOTS, ArmorGeoType.NONE, 1);

    // 符文护目镜（揭示之护目镜的符文版）
    public static final DeferredItem<RunicArmorItem> RUNIC_GOGGLES = runicArmor("runic_goggles",
            ModArmorMaterials.RUNIC_GOGGLES, ArmorItem.Type.HELMET, ArmorGeoType.NONE, 1);

    // ============================================================================================
    // Thaumic Dyes —— 符文护甲 II（Runic Enhanced）
    // ============================================================================================
    public static final DeferredItem<RunicArmorItem> RUNIC_ENHANCED_HELMET = runicArmor("runic_enhanced_helmet",
            ModArmorMaterials.RUNIC_ENHANCED, ArmorItem.Type.HELMET, ArmorGeoType.NONE, 1);
    public static final DeferredItem<RunicArmorItem> RUNIC_ENHANCED_CHESTPLATE = runicArmor("runic_enhanced_chestplate",
            ModArmorMaterials.RUNIC_ENHANCED, ArmorItem.Type.CHESTPLATE, ArmorGeoType.NONE, 1);
    public static final DeferredItem<RunicArmorItem> RUNIC_ENHANCED_LEGGINGS = runicArmor("runic_enhanced_leggings",
            ModArmorMaterials.RUNIC_ENHANCED, ArmorItem.Type.LEGGINGS, ArmorGeoType.NONE, 1);
    public static final DeferredItem<RunicArmorItem> RUNIC_ENHANCED_BOOTS = runicArmor("runic_enhanced_boots",
            ModArmorMaterials.RUNIC_ENHANCED, ArmorItem.Type.BOOTS, ArmorGeoType.NONE, 1);

    // ============================================================================================
    // Thaumic Dyes —— 符文护甲 III（Runic Primal）：ModelRobesSpecial + 三升级槽
    // ============================================================================================
    public static final DeferredItem<RunicArmorItem> RUNIC_PRIMAL_HELMET = runicArmor("runic_primal_helmet",
            ModArmorMaterials.RUNIC_PRIMAL, ArmorItem.Type.HELMET, ArmorGeoType.ROBES_SPECIAL, 3);
    public static final DeferredItem<RunicArmorItem> RUNIC_PRIMAL_CHESTPLATE = runicArmor("runic_primal_chestplate",
            ModArmorMaterials.RUNIC_PRIMAL, ArmorItem.Type.CHESTPLATE, ArmorGeoType.ROBES_SPECIAL, 3);
    public static final DeferredItem<RunicArmorItem> RUNIC_PRIMAL_LEGGINGS = runicArmor("runic_primal_leggings",
            ModArmorMaterials.RUNIC_PRIMAL, ArmorItem.Type.LEGGINGS, ArmorGeoType.ROBES_SPECIAL, 3);
    public static final DeferredItem<RunicArmorItem> RUNIC_PRIMAL_BOOTS = runicArmor("runic_primal_boots",
            ModArmorMaterials.RUNIC_PRIMAL, ArmorItem.Type.BOOTS, ArmorGeoType.ROBES_SPECIAL, 3);

    // ============================================================================================
    // Thaumic Dyes —— 古代面具（TX Ancient Mask）：头盔，非符文、不参与升级
    // ============================================================================================
    public static final DeferredItem<SimpleArmorItem> ANCIENT_MASK = armor("ancient_mask",
            ModArmorMaterials.ANCIENT_MASK, ArmorItem.Type.HELMET, Rarity.EPIC, ArmorGeoType.NONE, 5);

    // ============================================================================================
    // Thaumic Dyes —— 旅者靴（符文旅者靴带一个升级槽）
    // ============================================================================================
    public static final DeferredItem<TravellerBootsItem> THAUMIUM_BOOTS_TRAVELLER = boots("thaumium_boots_traveller",
            ModArmorMaterials.BOOTS_THAUMIUM_TRAVELLER, Rarity.RARE, ArmorGeoType.NONE, TravellerBootsItem.Kind.THAUMIUM);
    public static final DeferredItem<TravellerBootsItem> VOID_BOOTS_TRAVELLER = boots("void_boots_traveller",
            ModArmorMaterials.BOOTS_VOID_TRAVELLER, Rarity.EPIC, ArmorGeoType.NONE, TravellerBootsItem.Kind.VOID);
    /**
     * 符文旅者靴：源模组 {@code ItemRunicBootsTraveller} 继承符文甲，因此额外带
     * 1 个升级槽与符文充能（见 {@link RunicTravellerBootsItem}）。
     */
    public static final DeferredItem<RunicTravellerBootsItem> RUNIC_BOOTS_TRAVELLER =
            ITEMS.registerItem("runic_boots_traveller",
                    props -> new RunicTravellerBootsItem(ModArmorMaterials.BOOTS_RUNIC_TRAVELLER, props, ArmorGeoType.NONE),
                    new Item.Properties().rarity(Rarity.RARE).stacksTo(1));

    // ============================================================================================
    // Thaumic Exploration —— 流星靴 / 彗星靴
    // ============================================================================================
    public static final DeferredItem<TravellerBootsItem> BOOTS_METEOR = boots("boots_meteor",
            ModArmorMaterials.BOOTS_METEOR, Rarity.RARE, ArmorGeoType.NONE, TravellerBootsItem.Kind.METEOR);
    public static final DeferredItem<TravellerBootsItem> BOOTS_COMET = boots("boots_comet",
            ModArmorMaterials.BOOTS_COMET, Rarity.EPIC, ArmorGeoType.NONE, TravellerBootsItem.Kind.COMET);

    // ============================================================================================
    // Thaumic Exploration —— 水晶护甲（Crystal Armor）：原版人形模型 + 64×32 贴图
    // ============================================================================================
    // 魔力减免：源模组 ItemCrystalArmor#getVisDiscount = 头盔/胸甲/护腿 2%、靴子 1%（整套 7%）
    public static final DeferredItem<SimpleArmorItem> CRYSTAL_HELMET = armor("crystal_helmet",
            ModArmorMaterials.CRYSTAL, ArmorItem.Type.HELMET, Rarity.RARE, ArmorGeoType.NONE, 2);
    public static final DeferredItem<SimpleArmorItem> CRYSTAL_CHESTPLATE = armor("crystal_chestplate",
            ModArmorMaterials.CRYSTAL, ArmorItem.Type.CHESTPLATE, Rarity.RARE, ArmorGeoType.NONE, 2);
    public static final DeferredItem<SimpleArmorItem> CRYSTAL_LEGGINGS = armor("crystal_leggings",
            ModArmorMaterials.CRYSTAL, ArmorItem.Type.LEGGINGS, Rarity.RARE, ArmorGeoType.NONE, 2);
    public static final DeferredItem<SimpleArmorItem> CRYSTAL_BOOTS = armor("crystal_boots",
            ModArmorMaterials.CRYSTAL, ArmorItem.Type.BOOTS, Rarity.RARE, ArmorGeoType.NONE, 1);

    // ============================================================================================
    // Thaumic Exploration —— 饰品 / 材料 / 工具（护符、腰带、戒指、封印、杖芯等）
    // ============================================================================================
    public static final DeferredItem<Item> PURE_ZOMBIE_BRAIN = ITEMS.registerSimpleItem("pure_zombie_brain",
            new Item.Properties().rarity(Rarity.RARE));

    /**
     * 空白封印 —— 目前只保留这一件。
     *
     * <p>源模组还有「箱子封印 / 罐子封印 / 已绑定」四件，但它们的玩法依赖一整套
     * 世界级共享库存（方块 + 方块实体 + SavedData），本模组没有这套系统，
     * 剩下四件右键不会有任何反应，因此全部移除，只留这张白色的空白封印作为素材物品。</p>
     */
    public static final DeferredItem<Item> BLANK_SEAL = ITEMS.registerSimpleItem("blank_seal",
            new Item.Properties().rarity(Rarity.UNCOMMON));

    public static final DeferredItem<CurioItem> TALISMAN_FOOD = ITEMS.registerItem("talisman_food",
            props -> new CurioItem(props, TalismanFoodHandler::new),
            new Item.Properties().rarity(Rarity.RARE).stacksTo(1));

    public static final DeferredItem<CurioItem> STABILIZER_BELT = ITEMS.registerItem("stabilizer_belt",
            props -> new CurioItem(props, StabilizerBeltHandler::new),
            new Item.Properties().rarity(Rarity.RARE).stacksTo(1));

    public static final DeferredItem<Item> DISCOUNT_RING = ITEMS.registerSimpleItem("discount_ring",
            new Item.Properties().rarity(Rarity.RARE).stacksTo(1));

    public static final DeferredItem<Item> TENTACLE_RING = ITEMS.registerSimpleItem("tentacle_ring",
            new Item.Properties().rarity(Rarity.RARE).stacksTo(1));

    public static final DeferredItem<Item> CHARM_NO_TAINT = ITEMS.registerSimpleItem("charm_no_taint",
            new Item.Properties().rarity(Rarity.UNCOMMON));

    public static final DeferredItem<Item> CHARM_TAINT = ITEMS.registerSimpleItem("charm_taint",
            new Item.Properties().rarity(Rarity.RARE));

    public static final DeferredItem<Item> TAINT_BERRY = ITEMS.registerSimpleItem("taint_berry",
            new Item.Properties().rarity(Rarity.UNCOMMON));

    // ============================================================================================
    // 法杖部件（杖杆 / 杖端）
    //
    // 这些物品本身只是普通 Item —— 真正让它们「成为法杖部件」的是数据包：
    //   data/thaumicdyesexplor/data_maps/item/wand_rods.json   容量、工艺系数、充能方式、贴图
    //   data/thaumicdyesexplor/data_maps/item/wand_caps.json   消耗修正、工艺系数、贴图
    // 加上 data/thaumcraft/tags/item/{wand_rods,staff_cores,wand_caps} 的标签归属。
    // 装配成法杖由 TC4R 自带的 thaumcraft:wand_assembly / staff_assembly 配方完成，不需要我们写配方。
    // ============================================================================================
    /** 嬗变杖杆：容量 75，工艺系数 6（源模组 WAND_ROD_CRYSTAL）。 */
    public static final DeferredItem<Item> TRANSMUTATION_CORE = wandPart("transmutation_core", Rarity.RARE);

    /** 嬗变大杖杆：容量 175，工艺系数 14（源模组 STAFF_ROD_CRYSTAL）。 */
    public static final DeferredItem<Item> TRANSMUTATION_STAFF_CORE = wandPart("transmutation_staff_core", Rarity.RARE);

    /** 琥珀杖杆：容量 10，工艺系数 8。 */
    public static final DeferredItem<Item> AMBER_CORE = wandPart("amber_core", Rarity.RARE);

    /** 琥珀大杖杆：容量 25，工艺系数 18。 */
    public static final DeferredItem<Item> AMBER_STAFF_CORE = wandPart("amber_staff_core", Rarity.RARE);

    /** 死灵大杖杆：容量 200，工艺系数 15。 */
    public static final DeferredItem<Item> NECRO_STAFF_CORE = wandPart("necro_staff_core", Rarity.EPIC);

    /** 面包杖杆：容量 39，工艺系数 8。 */
    public static final DeferredItem<Item> BREAD_CORE = wandPart("bread_core", Rarity.UNCOMMON);

    /** 旅者杖端：消耗 ×0.95，工艺系数 6。 */
    public static final DeferredItem<Item> SOJOURNER_CAP = wandPart("sojourner_cap", Rarity.RARE);

    /** 旅者杖端（未充能）：合成材料。 */
    public static final DeferredItem<Item> SOJOURNER_CAP_UNCHARGED = wandPart("sojourner_cap_uncharged", Rarity.COMMON);

    /** 机械杖端：消耗 ×0.95，工艺系数 6。 */
    public static final DeferredItem<Item> MECHANIST_CAP = wandPart("mechanist_cap", Rarity.RARE);

    /** 机械杖端（未充能）：合成材料。 */
    public static final DeferredItem<Item> MECHANIST_CAP_UNCHARGED = wandPart("mechanist_cap_uncharged", Rarity.COMMON);

    // ============================================================================================
    // 辅助构造方法
    // ============================================================================================
    private static DeferredItem<SimpleArmorItem> armor(String id, Holder<ArmorMaterial> material,
                                                       ArmorItem.Type type, Rarity rarity, ArmorGeoType geo) {
        return armor(id, material, type, rarity, geo, 0);
    }

    /**
     * 带魔力减免的护甲。
     *
     * <p>{@code visDiscount} 只影响 tooltip 上的「魔力减免」一行，真正生效的数值在
     * {@code data/thaumcraft/data_maps/item/equipment_abilities.json} 的 {@code vis_discount}，
     * 两处必须一致。</p>
     */
    private static DeferredItem<SimpleArmorItem> armor(String id, Holder<ArmorMaterial> material,
                                                       ArmorItem.Type type, Rarity rarity, ArmorGeoType geo,
                                                       int visDiscount) {
        return ITEMS.registerItem(id,
                props -> new SimpleArmorItem(material, type, props, geo, visDiscount),
                new Item.Properties().rarity(rarity).stacksTo(1));
    }

    private static DeferredItem<RunicArmorItem> runicArmor(String id, Holder<ArmorMaterial> material,
                                                           ArmorItem.Type type, ArmorGeoType geo, int upgradeSlots) {
        return ITEMS.registerItem(id,
                props -> new RunicArmorItem(material, type, props, geo, upgradeSlots),
                new Item.Properties().rarity(Rarity.RARE).stacksTo(1));
    }

    private static DeferredItem<TravellerBootsItem> boots(String id, Holder<ArmorMaterial> material,
                                                          Rarity rarity, ArmorGeoType geo, TravellerBootsItem.Kind kind) {
        return ITEMS.registerItem(id,
                props -> new TravellerBootsItem(material, props, geo, kind),
                new Item.Properties().rarity(rarity).stacksTo(1));
    }

    /**
     * 法杖部件（杖杆 / 杖端）按可堆叠材料处理，一叠 64。
     *
     * <p>装配配方只读单个槽位里的物品，不关心堆叠数量，所以堆叠不会影响装配结果；
     * 杖杆 / 杖端本质上是“零件”，做成 1 个一叠反而更难用。</p>
     */
    private static DeferredItem<Item> wandPart(String id, Rarity rarity) {
        return ITEMS.registerItem(id, WandPartItem::new,
                new Item.Properties().rarity(rarity).stacksTo(64));
    }

    /**
     * 需要 Curios 物品能力的饰品。
     *
     * <p>只有「需要每 tick 做事」或「需要报属性修饰符」的饰品才在这里；
     * 纯被动判定的饰品（折扣之戒、触手之戒、捕梦网、腐化之戒）只要有
     * {@code data/curios/tags/item/*} 的标签就能装备，能力由事件查询槽位完成。</p>
     */
    public static final List<DeferredItem<CurioItem>> CURIO_ITEMS = List.of(TALISMAN_FOOD, STABILIZER_BELT);

    private ModItems() {
    }
}
