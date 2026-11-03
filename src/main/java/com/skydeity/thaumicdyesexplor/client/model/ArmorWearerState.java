package com.skydeity.thaumicdyesexplor.client.model;

import com.skydeity.thaumicdyesexplor.item.ArmorGeoType;
import com.skydeity.thaumicdyesexplor.item.ArmorSets;
import com.skydeity.thaumicdyesexplor.registry.ModDataComponents;

import net.minecraft.world.entity.LivingEntity;

/**
 * 护甲渲染时的“佩戴者状态”——决定哪些条件部件要显示。
 *
 * <p>对应源模组模型类里每 20 tick 刷新的 {@code hasSet} / {@code hasMask} / {@code hasGoggles}
 * 三张表：</p>
 * <ul>
 *   <li>{@code set}：身上穿着的该套装件数（堡垒 0~3、符文 III 0~4）；</li>
 *   <li>{@code mask}：头盔上的脸谱编号（-1 无 / 0~2）；</li>
 *   <li>{@code goggles}：头盔护目镜开关。</li>
 * </ul>
 *
 * @param setLevel 套装件数（无套装概念的几何填 0）
 * @param mask     头盔面具编号，-1 = 无
 * @param goggles  头盔护目镜开关
 */
public record ArmorWearerState(int setLevel, int mask, boolean goggles) {

    public static final ArmorWearerState DEFAULT = new ArmorWearerState(0, -1, false);

    public static ArmorWearerState of(LivingEntity entity, ArmorGeoType type) {
        int set = switch (type) {
            case FORTRESS -> ArmorSets.count(entity, ArmorSets.FORTRESS);
            case IRON_FORTRESS -> ArmorSets.count(entity, ArmorSets.IRON_FORTRESS);
            case ROBES_SPECIAL -> ArmorSets.count(entity, ArmorSets.RUNIC_PRIMAL);
            default -> 0;
        };
        var head = entity.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD);
        Integer mask = head.get(ModDataComponents.ARMOR_MASK);
        Boolean goggles = head.get(ModDataComponents.ARMOR_GOGGLES);
        return new ArmorWearerState(set, mask == null ? -1 : mask, Boolean.TRUE.equals(goggles));
    }
}
