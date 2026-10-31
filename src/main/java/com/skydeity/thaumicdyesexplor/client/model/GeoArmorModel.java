package com.skydeity.thaumicdyesexplor.client.model;

import java.util.Locale;
import java.util.Set;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.LivingEntity;

/**
 * 用 {@link ArmorGeo} 烘焙出来的护甲人形模型。
 *
 * <p>1.7.10 的护甲模型直接继承 {@code ModelBiped} 并追加自定义盒体；1.21.1 对应的做法是
 * 继承 {@link HumanoidModel}，把自定义盒体作为原版部件的子部件挂上去。</p>
 *
 * <p>源模组里有若干“条件显示”部件（护目镜、面具、卷轴、书本、饰板等），它们由 1.7.10 的
 * {@code ModelRenderer.isHidden} 在渲染时按玩家状态开关（是否戴着揭示之护目镜、
 * 堡垒甲的“套装等级”等）。本移植把这些状态依赖的部件默认隐藏，只保留基础外形，
 * 避免与主层叠面互相 z-fighting；隐藏是在烘焙时按名字关掉的（见
 * {@link ArmorGeo#bake(java.util.function.Predicate)}）。</p>
 */
public class GeoArmorModel extends HumanoidModel<LivingEntity> {

    /** 源模组里按状态条件显示的部件名前缀，移植后统一隐藏。 */
    private static final Set<String> HIDDEN_PREFIXES = Set.of(
            "goggles", "mask_1", "mask_2", "scroll", "book", "ornament", "gemornament");

    private final ArmorGeo geo;

    public GeoArmorModel(ArmorGeo geo) {
        super(geo.bake(GeoArmorModel::isConditional), RenderType::entityCutoutNoCull);
        this.geo = geo;
    }

    public ArmorGeo geo() {
        return this.geo;
    }

    private static boolean isConditional(String partName) {
        String name = partName.toLowerCase(Locale.ROOT);
        for (String prefix : HIDDEN_PREFIXES) {
            if (name.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}
