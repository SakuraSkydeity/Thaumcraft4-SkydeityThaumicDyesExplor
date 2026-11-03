package com.skydeity.thaumicdyesexplor.registry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.skydeity.thaumicdyesexplor.ThaumicDyesExplor;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 护甲材质注册表。
 *
 * <p>对应 1.7.10 源模组 {@code ItemHandler} 里通过 {@code EnumHelper.addArmorMaterial(...)}
 * 定义的各档材质，以及直接引用的 {@code ArmorMaterial.IRON} 与本体
 * {@code ThaumcraftApi.armorMatThaumium / armorMatVoid / armorMatSpecial}。</p>
 *
 * <h2>为什么“一套外观 = 一个材质”</h2>
 * <p>1.7.10 里护甲贴图由物品的 {@code getArmorTexture()} 决定，一个材质可以服务很多贴图；
 * 而 1.21.1 的 {@code HumanoidArmorLayer} 只认 {@link ArmorMaterial#layers()} ——
 * 贴图路径完全由材质的 {@link ArmorMaterial.Layer} 拼出：
 * {@code textures/models/armor/<path>_layer_1.png}（外层）与 {@code _layer_2.png}（内层）。
 * 所以这里按“外观套组”注册材质，每套把自己的图层资源名写死。</p>
 *
 * <h2>两遍渲染的正确顺序（关键）</h2>
 * <p>源模组的护甲是两遍渲染，{@code getArmorTexture} 的写法统一为</p>
 * <pre>{@code return type == null ? "..._overlay.png" : "..._base.png";}</pre>
 * <p>1.7.10 的 {@code LayerBipedArmor} 在 {@code type == null} 那一遍才会把
 * {@code getColor(stack)} 的颜色 {@code glColor} 下去。也就是说：</p>
 * <ul>
 *   <li><b>{@code *_overlay} = 可染色的布料</b>（贴图本身接近灰白，靠染色上色）；</li>
 *   <li><b>{@code *_base} = 已经画好颜色的饰板 / 金属件</b>（不吃染料）。</li>
 * </ul>
 * <p>因此这里的图层顺序是 <b>布料在前（{@code dyeable=true}）、饰板在后</b>，
 * 与源模组“先铺一层可染色布、再把饰板压上去”的观感一致。
 * 注意：{@code dyeable} 只是“这一层是否吃 {@code DYED_COLOR}”，不吃颜色的层默认按纯白
 * 乘算，所以饰板层必须保持原色（{@code dyeable=false}）。</p>
 */
public final class ModArmorMaterials {

    public static final DeferredRegister<ArmorMaterial> MATERIALS =
            DeferredRegister.create(Registries.ARMOR_MATERIAL, ThaumicDyesExplor.MOD_ID);

    // ============================================================================================
    // Thaumic Dyes —— 染袍 / 法袍（ROBE：防御 2/6/5/1，附魔 15）
    // ============================================================================================
    public static final Holder<ArmorMaterial> CULTIST_ROBE = set("cultist_robe", 2, 6, 5, 1, 15,
            SoundEvents.ARMOR_EQUIP_LEATHER, 0.0F, 0.0F,
            cloth("robe_armor_overlay"), plate("cultist_robe_base"));

    public static final Holder<ArmorMaterial> THAUMIUM_ROBE = set("thaumium_robe", 2, 6, 5, 1, 15,
            SoundEvents.ARMOR_EQUIP_LEATHER, 1.0F, 0.0F,
            cloth("thaumium_robe_overlay"), plate("thaumium_robe_base"));

    // 虚空法袍：源模组用 FORT 档（防御 3/7/6/1，附魔 30），布料沿用染袍的 overlay。
    public static final Holder<ArmorMaterial> VOID_ROBE = set("void_robe", 3, 7, 6, 1, 30,
            SoundEvents.ARMOR_EQUIP_IRON, 2.0F, 0.0F,
            cloth("robe_armor_overlay"), plate("void_robe_base"));

    // ============================================================================================
    // Thaumic Dyes —— 堡垒甲（FORT：3/7/6/1，附魔 30，韧性 2）
    // ============================================================================================
    public static final Holder<ArmorMaterial> FORTRESS = set("fortress", 3, 7, 6, 1, 30,
            SoundEvents.ARMOR_EQUIP_IRON, 2.0F, 0.0F,
            cloth("fortress_armor_overlay"), plate("fortress_armor_base"));

    // 领袖甲（Praetor）：同为 FORT 档，独立外观
    public static final Holder<ArmorMaterial> CULTIST_LEADER = set("cultist_leader", 3, 7, 6, 1, 30,
            SoundEvents.ARMOR_EQUIP_IRON, 2.0F, 0.0F,
            cloth("cultist_praetor_overlay"), plate("cultist_praetor_base"));

    // 圣骑士头盔：只有头盔，源模组单张贴图、不染色。
    public static final Holder<ArmorMaterial> CULTIST_PALADIN = set("cultist_paladin", 3, 7, 6, 1, 30,
            SoundEvents.ARMOR_EQUIP_IRON, 2.0F, 0.0F, plate("cultist_paladin"));

    // ============================================================================================
    // Thaumic Dyes —— 骑士甲（KNIGHT：2/6/5/1，附魔 12）
    // ============================================================================================
    public static final Holder<ArmorMaterial> CULTIST_KNIGHT = set("cultist_knight", 2, 6, 5, 1, 12,
            SoundEvents.ARMOR_EQUIP_IRON, 1.0F, 0.0F,
            cloth("cultist_knight_overlay"), plate("cultist_knight_base"));

    // 太阳骑士（Sun Knight）：KNIGHT 几何、太阳饰板贴图（单层）
    public static final Holder<ArmorMaterial> SUN_KNIGHT = set("sun_knight", 2, 6, 5, 2, 12,
            SoundEvents.ARMOR_EQUIP_IRON, 1.0F, 0.0F, plate("sun_knight"));

    // 寄居板甲（Inhabited Plate）：KNIGHT 几何、腐尸板甲贴图（单层）
    public static final Holder<ArmorMaterial> INHABITED_PLATE = set("inhabited_plate", 2, 6, 5, 2, 12,
            SoundEvents.ARMOR_EQUIP_IRON, 1.0F, 0.0F, plate("inhabited_plate"));

    // ============================================================================================
    // Thaumic Dyes —— 游侠甲 / 弓箭手甲（可染色 / 未染色两套）
    // ============================================================================================
    public static final Holder<ArmorMaterial> CULTIST_RANGER = set("cultist_ranger", 2, 6, 5, 1, 12,
            SoundEvents.ARMOR_EQUIP_LEATHER, 1.0F, 0.0F,
            cloth("cultist_ranger_overlay"), plate("cultist_ranger_base"));

    public static final Holder<ArmorMaterial> CULTIST_RANGER_PLAIN = set("cultist_ranger_plain", 2, 6, 5, 2, 12,
            SoundEvents.ARMOR_EQUIP_LEATHER, 1.0F, 0.0F, plate("cultist_ranger_plain"));

    public static final Holder<ArmorMaterial> CULTIST_ARCHER = set("cultist_archer", 2, 6, 5, 1, 12,
            SoundEvents.ARMOR_EQUIP_LEATHER, 1.0F, 0.0F,
            cloth("cultist_archer_overlay"), plate("cultist_archer_base"));

    public static final Holder<ArmorMaterial> CULTIST_ARCHER_PLAIN = set("cultist_archer_plain", 2, 6, 5, 2, 12,
            SoundEvents.ARMOR_EQUIP_LEATHER, 1.0F, 0.0F, plate("cultist_archer_plain"));

    // ============================================================================================
    // Thaumic Dyes —— 铁堡垒甲（IRONFORT：2/6/5/2，附魔 12）
    // ============================================================================================
    public static final Holder<ArmorMaterial> IRON_FORTRESS = set("iron_fortress", 2, 6, 5, 2, 12,
            SoundEvents.ARMOR_EQUIP_IRON, 1.0F, 0.0F, plate("iron_fortress"));

    // ============================================================================================
    // Thaumic Dyes —— 符文护甲 I / II / III（RUNIC / RUNICII / RUNICIII，附魔 25）
    // 三档沿用 64x32 的原版人形布局，因此不需要自定义几何（ArmorGeoType.NONE）。
    // ============================================================================================
    public static final Holder<ArmorMaterial> RUNIC = set("runic", 1, 3, 2, 1, 25,
            SoundEvents.ARMOR_EQUIP_GOLD, 0.0F, 0.0F, plate("runic"));

    public static final Holder<ArmorMaterial> RUNIC_ENHANCED = set("runic_enhanced", 2, 6, 5, 2, 25,
            SoundEvents.ARMOR_EQUIP_GOLD, 0.0F, 0.0F, plate("runic_enhanced"));

    // 符文护甲 III：源模组用 ModelRobesSpecial + guardian_robe_armor 系列贴图（256x128）。
    public static final Holder<ArmorMaterial> RUNIC_PRIMAL = set("runic_primal", 3, 7, 6, 3, 25,
            SoundEvents.ARMOR_EQUIP_GOLD, 0.0F, 0.0F, plate("runic_primal"));

    // 符文护目镜 / 古代面具：只有头盔
    public static final Holder<ArmorMaterial> RUNIC_GOGGLES = set("runic_goggles", 1, 0, 0, 0, 25,
            SoundEvents.ARMOR_EQUIP_GOLD, 0.0F, 0.0F, plate("runic_goggles"));

    public static final Holder<ArmorMaterial> ANCIENT_MASK = set("ancient_mask", 1, 0, 0, 0, 25,
            SoundEvents.ARMOR_EQUIP_GOLD, 0.0F, 0.0F, plate("ancient_mask"));

    // ============================================================================================
    // Thaumic Exploration —— 水晶护甲 / 旅者靴 / 流星靴 / 彗星靴
    // ============================================================================================
    public static final Holder<ArmorMaterial> CRYSTAL = set("crystal", 2, 3, 2, 1, 25,
            SoundEvents.ARMOR_EQUIP_GENERIC, 0.0F, 0.0F, plate("crystal"));

    public static final Holder<ArmorMaterial> BOOTS_THAUMIUM_TRAVELLER = set("boots_thaumium_traveller", 0, 0, 0, 1, 15,
            SoundEvents.ARMOR_EQUIP_LEATHER, 0.0F, 0.0F, plate("boots_thaumium_traveller"));

    public static final Holder<ArmorMaterial> BOOTS_VOID_TRAVELLER = set("boots_void_traveller", 0, 0, 0, 2, 30,
            SoundEvents.ARMOR_EQUIP_IRON, 0.0F, 0.0F, plate("boots_void_traveller"));

    public static final Holder<ArmorMaterial> BOOTS_RUNIC_TRAVELLER = set("boots_runic_traveller", 0, 0, 0, 1, 25,
            SoundEvents.ARMOR_EQUIP_GOLD, 0.0F, 0.0F, plate("boots_runic_traveller"));

    public static final Holder<ArmorMaterial> BOOTS_METEOR = set("boots_meteor", 0, 0, 0, 1, 25,
            SoundEvents.ARMOR_EQUIP_GOLD, 0.0F, 0.0F, plate("boots_meteor"));

    public static final Holder<ArmorMaterial> BOOTS_COMET = set("boots_comet", 0, 0, 0, 2, 25,
            SoundEvents.ARMOR_EQUIP_GOLD, 0.0F, 0.0F, plate("boots_comet"));

    // ============================================================================================
    // Forgotten Relics —— 恐惧王冠（NOBLEGOLD：3/9/6/3，耐久系数 40，只有头盔）
    // 源贴图 crown_prs.png 本身就是标准 64x32 人形布局，直接搬为 layer_1。
    // ============================================================================================
    public static final Holder<ArmorMaterial> TERROR_CROWN = set("terror_crown", 3, 9, 6, 3, 0,
            SoundEvents.ARMOR_EQUIP_GOLD, 0.0F, 0.0F, plate("terror_crown"));

    /**
     * 一个渲染层：资源名 + 是否吃染料颜色。
     *
     * @param asset   贴图资源名（不含 {@code textures/models/armor/} 前缀与 {@code _layer_N.png} 后缀）
     * @param dyeable 该层是否接受染料颜色
     */
    public record LayerSpec(String asset, boolean dyeable) {
    }

    /** 可染色布料层（源模组 {@code type == null} 那一遍）。 */
    private static LayerSpec cloth(String asset) {
        return new LayerSpec(asset, true);
    }

    /** 已上好色的饰板层（源模组 {@code type == "overlay"} 那一遍）。 */
    private static LayerSpec plate(String asset) {
        return new LayerSpec(asset, false);
    }

    private static Holder<ArmorMaterial> set(String id, int helmet, int chest, int legs, int boots, int enchantment,
                                             Holder<SoundEvent> sound, float toughness, float knockbackResistance,
                                             LayerSpec... layers) {
        Map<ArmorItem.Type, Integer> defense = Map.of(
                ArmorItem.Type.HELMET, helmet,
                ArmorItem.Type.CHESTPLATE, chest,
                ArmorItem.Type.LEGGINGS, legs,
                ArmorItem.Type.BOOTS, boots);
        List<ArmorMaterial.Layer> list = new ArrayList<>(layers.length);
        for (LayerSpec spec : layers) {
            list.add(new ArmorMaterial.Layer(
                    ResourceLocation.fromNamespaceAndPath(ThaumicDyesExplor.MOD_ID, spec.asset()), "", spec.dyeable()));
        }
        return MATERIALS.register(id, () -> new ArmorMaterial(
                defense,
                enchantment,
                sound,
                () -> Ingredient.of(Items.IRON_INGOT),
                List.copyOf(list),
                toughness,
                knockbackResistance));
    }

    private ModArmorMaterials() {
    }
}
