package com.skydeity.thaumicdyesexplor.client;

import com.skydeity.thaumicdyesexplor.item.ArmorGeoType;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
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
        // 条件几何（堡垒/铁堡垒/符文III/游侠系）按佩戴者状态取不同的烘焙结果；
        // 状态空间有界且缓存，不会每帧重建模型。
        HumanoidModel<LivingEntity> custom = ArmorGeoModels.get(this.geoType, slot,
                com.skydeity.thaumicdyesexplor.client.model.ArmorWearerState.of(entity, this.geoType));
        return custom != null ? custom : original;
    }

    /**
     * 让自定义模型跟着实体动起来（走路摆臂、潜行弯腰、游泳、挥手）。
     *
     * <p>原版 {@code HumanoidArmorLayer} 只对<b>它自己的</b> inner/outer 模型调用
     * {@code copyPropertiesTo} + {@code setupAnim}，我们换上去的模型收不到，四肢会僵在站立姿势。
     * 这里补上同一套状态复制 + {@code setupAnim}。</p>
     */
    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void setupModelAnimations(LivingEntity entity, ItemStack stack, EquipmentSlot slot, Model model,
                                     float limbSwing, float limbSwingAmount, float partialTick,
                                     float ageInTicks, float netHeadYaw, float headPitch) {
        if (!(model instanceof HumanoidModel humanoid)) {
            return;
        }
        humanoid.young = entity.isBaby();
        humanoid.crouching = entity.isCrouching();
        humanoid.riding = entity.isPassenger();
        humanoid.swimAmount = entity.getSwimAmount(partialTick);
        humanoid.attackTime = entity.getAttackAnim(partialTick);
        humanoid.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
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
        // DYED_COLOR 组件只存 RGB（旧存档里的值更是没有 alpha），
        // 而 renderToBuffer 要的是 ARGB —— 不补 0xFF 的话染色层整层透明，
        // 表现为「染过色的护甲穿上后布料部分消失不见」
        return 0xFF000000 | DyedItemColor.getOrDefault(stack, DEFAULT_CLOTH_COLOR);
    }
}
