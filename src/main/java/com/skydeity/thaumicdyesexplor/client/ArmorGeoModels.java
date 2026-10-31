package com.skydeity.thaumicdyesexplor.client;

import java.util.EnumMap;
import java.util.Map;

import com.skydeity.thaumicdyesexplor.client.model.ArmorGeo;
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
 * <p>也就是 <b>胸甲 / 靴子用 1.0 的膨胀量，头盔 / 护腿用 0.5</b>。这里照抄这个分支，
 * 每种几何缓存两份烘焙结果（{@link HumanoidModel} 是无状态的容器，可以安全复用，
 * 姿势与可见性由 {@code HumanoidArmorLayer} 每帧 copy 进来）。</p>
 */
public final class ArmorGeoModels {

    /** 头盔 / 护腿用的膨胀量（源模组 {@code model2}）。 */
    private static final float GROW_SMALL = 0.5F;
    /** 胸甲 / 靴子用的膨胀量（源模组 {@code model1}）。 */
    private static final float GROW_LARGE = 1.0F;

    private static final Map<ArmorGeoType, HumanoidModel<LivingEntity>> SMALL = new EnumMap<>(ArmorGeoType.class);
    private static final Map<ArmorGeoType, HumanoidModel<LivingEntity>> LARGE = new EnumMap<>(ArmorGeoType.class);

    /**
     * 取某套外观在某个槽位要用的模型。
     *
     * @return 自定义模型；{@link ArmorGeoType#NONE} 时返回 {@code null}，调用方应保留原版模型
     */
    public static HumanoidModel<LivingEntity> get(ArmorGeoType type, EquipmentSlot slot) {
        if (type == null || type == ArmorGeoType.NONE) {
            return null;
        }
        boolean large = slot == EquipmentSlot.CHEST || slot == EquipmentSlot.FEET;
        Map<ArmorGeoType, HumanoidModel<LivingEntity>> cache = large ? LARGE : SMALL;
        float grow = large ? GROW_LARGE : GROW_SMALL;
        return cache.computeIfAbsent(type, key -> new GeoArmorModel(create(key, grow)));
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
