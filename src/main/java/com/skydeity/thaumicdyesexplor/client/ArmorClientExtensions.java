package com.skydeity.thaumicdyesexplor.client;

import com.skydeity.thaumicdyesexplor.item.ArmorGeoType;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

/**
 * 护甲的客户端扩展：自定义模型 + 布料层染色。
 *
 * <p>为什么只需要覆写 {@code getHumanoidArmorModel}：NeoForge 的
 * {@code HumanoidArmorLayer} 在拿到自定义模型后会调用
 * {@code ClientHooks.copyModelProperties(original, replacement)}，把姿势（头转角、四肢摆动、
 * 潜行弯腰）与七个顶层部件的可见性一起复制过去 —— 正好等价于 1.7.10 里
 * {@code model.isSneak = ...; model.bipedHead.showModel = ...} 那一段。</p>
 *
 * <p>贴图不在这里管：{@code ArmorMaterial.Layer} 已经把 {@code _layer_1/2.png} 定死了。
 * 这里只管“哪一层吃什么颜色”。</p>
 */
public final class ArmorClientExtensions implements IClientItemExtensions {

    /**
     * 源模组 {@code getColor(ItemStack)} 在 NBT 里没有 {@code display.color} 时返回的默认色
     * {@code 6961280}（0x6A3880，暗紫褐）—— 也就是“没染过的法袍”的颜色。
     */
    private static final int DEFAULT_CLOTH_COLOR = 0xFF6A3880;

    private final ArmorGeoType geoType;

    public ArmorClientExtensions(ArmorGeoType geoType) {
        this.geoType = geoType;
    }

    @Override
    public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity, ItemStack stack, EquipmentSlot slot,
                                                  HumanoidModel<?> original) {
        HumanoidModel<LivingEntity> custom = ArmorGeoModels.get(this.geoType, slot);
        return custom != null ? custom : original;
    }

    /**
     * 每层单独给颜色：布料层（{@code dyeable}）吃染料，饰板层恒定纯白乘算。
     *
     * <p>默认实现走 {@code ItemTags.DYEABLE} 判定，本模组的护甲没有进那个标签，
     * 会让布料层直接渲染成纯白，与源模组“默认暗紫褐法袍”的观感不符，所以这里显式接管。</p>
     */
    @Override
    public int getArmorLayerTintColor(ItemStack stack, LivingEntity entity, ArmorMaterial.Layer layer,
                                      int layerIdx, int fallbackColor) {
        if (!layer.dyeable()) {
            return 0xFFFFFFFF;
        }
        return DyedItemColor.getOrDefault(stack, DEFAULT_CLOTH_COLOR);
    }
}
