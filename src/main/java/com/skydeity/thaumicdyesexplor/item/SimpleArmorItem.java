package com.skydeity.thaumicdyesexplor.item;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/**
 * 通用护甲物品：迁移源模组里没有独立行为逻辑的护甲（染袍、堡垒甲、骑士甲、铁堡垒甲等）。
 *
 * <p>渲染不在这里决定：1.21.1 的护甲贴图由 {@link ArmorMaterial#layers()} 决定（见
 * {@code ModArmorMaterials}），模型由 {@code IClientItemExtensions#getHumanoidArmorModel}
 * 按 {@link #geoType()} 提供（见 {@code client/ModClientSetup} 的
 * {@code RegisterClientExtensionsEvent}）。物品只负责“我是哪套外观”这一件事。</p>
 *
 * <p><b>魔力减免（vis discount）</b>：真正生效的数值由前置的数据表
 * {@code data/thaumcraft/data_maps/item/equipment_abilities.json} 的 {@code vis_discount}
 * 决定（前置 {@code EquipmentApi.visDiscount} 逐件累加）。但前置的 API 只在
 * 「有玩家上下文」时才给读数，{@code appendHoverText} 拿不到玩家，所以这里另存一份
 * <b>仅用于 tooltip 显示</b>的数值 —— 两份必须一起改，见 {@code ModItems} 里的传参。</p>
 */
public class SimpleArmorItem extends ArmorItem {

    private final ArmorGeoType geoType;
    /** 仅用于 tooltip 展示的魔力减免（百分比）；实际效果读数据表。 */
    private final int visDiscount;

    public SimpleArmorItem(Holder<ArmorMaterial> material, Type type, Properties properties, ArmorGeoType geoType) {
        this(material, type, properties, geoType, 0);
    }

    public SimpleArmorItem(Holder<ArmorMaterial> material, Type type, Properties properties, ArmorGeoType geoType,
                           int visDiscount) {
        super(material, type, properties);
        this.geoType = geoType;
        this.visDiscount = visDiscount;
    }

    /** 自定义护甲几何；{@link ArmorGeoType#NONE} 表示沿用原版人形模型。 */
    public ArmorGeoType geoType() {
        return this.geoType;
    }

    /** tooltip 展示用的魔力减免（百分比，0 = 不显示）。 */
    public int visDiscount() {
        return this.visDiscount;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (this.visDiscount > 0) {
            tooltip.add(Component.translatable("tc.visdiscount")
                    .append(": " + this.visDiscount + "%").withStyle(ChatFormatting.DARK_PURPLE));
        }
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
