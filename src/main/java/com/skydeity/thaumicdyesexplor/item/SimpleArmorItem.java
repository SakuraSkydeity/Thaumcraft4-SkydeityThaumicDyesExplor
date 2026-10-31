package com.skydeity.thaumicdyesexplor.item;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;

/**
 * 通用护甲物品：迁移源模组里没有独立行为逻辑的护甲（染袍、堡垒甲、骑士甲、铁堡垒甲等）。
 *
 * <p>渲染不在这里决定：1.21.1 的护甲贴图由 {@link ArmorMaterial#layers()} 决定（见
 * {@code ModArmorMaterials}），模型由 {@code IClientItemExtensions#getHumanoidArmorModel}
 * 按 {@link #geoType()} 提供（见 {@code client/ModClientSetup} 的
 * {@code RegisterClientExtensionsEvent}）。物品只负责“我是哪套外观”这一件事。</p>
 */
public class SimpleArmorItem extends ArmorItem {

    private final ArmorGeoType geoType;

    public SimpleArmorItem(Holder<ArmorMaterial> material, Type type, Properties properties, ArmorGeoType geoType) {
        super(material, type, properties);
        this.geoType = geoType;
    }

    /** 自定义护甲几何；{@link ArmorGeoType#NONE} 表示沿用原版人形模型。 */
    public ArmorGeoType geoType() {
        return this.geoType;
    }
}
