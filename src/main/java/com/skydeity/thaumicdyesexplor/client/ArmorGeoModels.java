package com.skydeity.thaumicdyesexplor.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.skydeity.thaumicdyesexplor.client.model.ArmorGeo;
import com.skydeity.thaumicdyesexplor.client.model.ArmorWearerState;
import com.skydeity.thaumicdyesexplor.client.model.GeoArmorModel;
import com.skydeity.thaumicdyesexplor.client.model.geo.ModelArcherGeo;
import com.skydeity.thaumicdyesexplor.client.model.geo.ModelFortressGeo;
import com.skydeity.thaumicdyesexplor.client.model.geo.ModelIronFortressGeo;
import com.skydeity.thaumicdyesexplor.client.model.geo.ModelKnightGeo;
import com.skydeity.thaumicdyesexplor.client.model.geo.ModelPaladinGeo;
import com.skydeity.thaumicdyesexplor.client.model.geo.ModelPraetorGeo;
import com.skydeity.thaumicdyesexplor.client.model.geo.ModelRanger2Geo;
import com.skydeity.thaumicdyesexplor.client.model.geo.ModelRangerGeo;
import com.skydeity.thaumicdyesexplor.client.model.geo.ModelRobesGeo;
import com.skydeity.thaumicdyesexplor.client.model.geo.ModelRobesSpecialGeo;
import com.skydeity.thaumicdyesexplor.item.ArmorGeoType;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

/**
 * 护甲几何模型缓存。
 *
 * <p>源模组的 {@code getArmorModel(entity, stack, armorSlot)} 会按槽位选用两份模型：</p>
 * <pre>{@code
 * if (this.model1 == null) this.model1 = new ModelRobes(1.0F);
 * if (this.model2 == null) this.model2 = new ModelRobes(0.5F);
 * this.model = (type != 1 && type != 3) ? this.model2 : this.model1;
 * }</pre>
 * <p>也就是 <b>胸甲 / 靴子用 1.0 的膨胀量，头盔 / 护腿用 0.5</b>。这里照抄这个分支。</p>
 *
 * <p>带条件部件的几何（堡垒 / 铁堡垒 / 符文 III / 游侠系）还要按<b>佩戴者状态</b>
 * （套装件数 × 面具 × 护目镜）各缓存一份烘焙结果 —— 状态空间很小
 * （5 × 4 × 2），条件部件的显隐在烘焙时一次写死，渲染期不再改。</p>
 */
public final class ArmorGeoModels {

    /** 头盔 / 护腿用的膨胀量（源模组 {@code model2}）。 */
    private static final float GROW_SMALL = 0.5F;
    /** 胸甲 / 靴子用的膨胀量（源模组 {@code model1}）。 */
    private static final float GROW_LARGE = 1.0F;

    private record SlotKey(ArmorGeoType type, EquipmentSlot slot, float grow) {
    }

    /** (几何 × 槽位 × 状态) → 模型。状态空间有界（set 0~4、mask -1~2、goggles 二值），不会膨胀。 */
    private static final Map<SlotKey, Map<ArmorWearerState, HumanoidModel<LivingEntity>>> STATE_CACHE =
            new ConcurrentHashMap<>();

    /** 无状态几何：每个「外观 × 槽位」一份（槽位决定哪几个顶层部件可见，不能共用）。 */
    private static final Map<SlotKey, HumanoidModel<LivingEntity>> PLAIN = new ConcurrentHashMap<>();

    /** 是否有“按状态缓存”的条件几何。 */
    private static boolean hasStatefulParts(ArmorGeoType type) {
        return switch (type) {
            case FORTRESS, IRON_FORTRESS, ROBES_SPECIAL, RANGER, RANGER2, ARCHER -> true;
            default -> false;
        };
    }

    /**
     * 取某套外观在某个槽位要用的模型。
     *
     * @return 自定义模型；{@link ArmorGeoType#NONE} 时返回 {@code null}，调用方应保留原版模型
     */
    public static HumanoidModel<LivingEntity> get(ArmorGeoType type, EquipmentSlot slot) {
        return get(type, slot, ArmorWearerState.DEFAULT);
    }

    /** 同上，但按佩戴者状态取（条件几何不同状态是不同的烘焙结果）。 */
    public static HumanoidModel<LivingEntity> get(ArmorGeoType type, EquipmentSlot slot, ArmorWearerState state) {
        if (type == null || type == ArmorGeoType.NONE) {
            return null;
        }
        boolean large = slot == EquipmentSlot.CHEST || slot == EquipmentSlot.FEET;
        float grow = large ? GROW_LARGE : GROW_SMALL;
        SlotKey key = new SlotKey(type, slot, grow);
        if (!hasStatefulParts(type)) {
            return PLAIN.computeIfAbsent(key, k -> bake(type, slot, grow, null));
        }
        return STATE_CACHE
                .computeIfAbsent(key, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(state, st -> bake(type, slot, grow, st));
    }

    /** 烘焙一份模型，并按护甲槽裁剪顶层部件（源模组 {@code bipedXxx.showModel = armorSlot == N}）。 */
    private static HumanoidModel<LivingEntity> bake(ArmorGeoType type, EquipmentSlot slot, float grow,
                                                    ArmorWearerState state) {
        // 无状态几何也要把 type 传进模型 —— setupAnim 里的衣摆/披风摆动按 geoType 分派
        GeoArmorModel model = new GeoArmorModel(create(type, grow), type,
                state == null ? ArmorWearerState.DEFAULT : state);
        model.applySlotVisibility(slot);
        return model;
    }

    private static ArmorGeo create(ArmorGeoType type, float grow) {
        return switch (type) {
            case ROBES -> ModelRobesGeo.build(grow);
            case ROBES_SPECIAL -> ModelRobesSpecialGeo.build(grow);
            case KNIGHT -> ModelKnightGeo.build(grow);
            case PRAETOR -> ModelPraetorGeo.build(grow);
            case RANGER -> ModelRangerGeo.build(grow);
            case RANGER2 -> ModelRanger2Geo.build(grow);
            case ARCHER -> ModelArcherGeo.build(grow);
            case FORTRESS -> ModelFortressGeo.build(grow);
            case IRON_FORTRESS -> ModelIronFortressGeo.build(grow);
            case PALADIN -> ModelPaladinGeo.build(grow);
            case NONE -> throw new IllegalArgumentException("ArmorGeoType.NONE has no baked model");
        };
    }

    private ArmorGeoModels() {
    }
}
