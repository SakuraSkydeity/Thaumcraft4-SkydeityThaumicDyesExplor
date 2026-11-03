package com.skydeity.thaumicdyesexplor.client.model.geo;

import com.skydeity.thaumicdyesexplor.client.model.ArmorGeo;

/**
 * 游侠甲（未染色）护甲几何。
 *
 * <p>由 {@code tools/port_armor_models.py} 从 1.7.10 源模组同名 Model 类自动转换生成，
 * 请勿手工修改；要改请改脚本或源模组数据。</p>
 */
public final class ModelRanger2Geo {

    public static final int TEX_WIDTH = 128;
    public static final int TEX_HEIGHT = 64;

    private ModelRanger2Geo() {
    }

    public static ArmorGeo build(float grow) {
        ArmorGeo.Builder b = new ArmorGeo.Builder(TEX_WIDTH, TEX_HEIGHT, grow);
        b.cube("Mask#0", "head", 74, 3, -4.5F, -5F, -4.6F, 9F, 5F, 1F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("Mask#1", "head", 96, 3, -4.5F, -5F, -4.6F, 9F, 5F, 1F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("Helmet", "head", 36, 6, -4.5F, -9F, -4.5F, 9F, 4F, 9F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("ShoulderplateL1", "left_arm", 56, 33, 3.5F, -1.5F, -3.5F, 1F, 4F, 7F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("clothchestR", "body", 108, 38, -6.1F, -11.5F, -3.5F, 2F, 8F, 1F, 0F, 0F, 0F, 0F, 2F, 12F, 0F, false);
        b.cube("clothchestL", "body", 108, 38, 0.1F, -11.5F, -3.5F, 2F, 8F, 1F, 0F, 0F, 0F, 0F, 2F, 12F, 0F, true);
        b.cube("Mbelt", "body", 16, 55, -4F, 7F, -3F, 8F, 5F, 1F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("HelmetL", "head", 16, 11, 5.5F, -3F, -4.5F, 1F, 5F, 9F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("ShoulderplateR1", "right_arm", 56, 33, -4.5F, -1.5F, -3.5F, 1F, 4F, 7F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("ShoulderplateR3", "right_arm", 40, 33, -2.5F, 3.5F, -3.5F, 1F, 3F, 7F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("HelmetB", "head", 36, 19, -4.5F, -3F, 5.5F, 9F, 5F, 1F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("Chestthing", "body", 56, 50, -2.5F, 1F, -4F, 5F, 7F, 1F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("ShoulderR", "right_arm", 16, 45, -3.5F, -2.5F, -2.5F, 5F, 5F, 5F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("capsthingy", "head", 21, 0, -4.5F, -6F, -6.5F, 9F, 1F, 2F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("ShoulderplateTopR", "right_arm", 56, 25, -5.5F, -2.5F, -3.5F, 2F, 1F, 7F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("bottleR1", "right_leg", 76, 10, -5.55F, 0.5F, -1.5F, 3F, 1F, 3F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("ShoulderplateR2", "right_arm", 40, 33, -3.5F, 1.5F, -3.5F, 1F, 3F, 7F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("bottleR3", "right_leg", 88, 10, -6.05F, 2.5F, -2F, 4F, 4F, 4F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("BeltL", "body", 16, 36, 4F, 4F, -3F, 1F, 3F, 6F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("bottleL3", "left_leg", 88, 10, 1.95F, 2.5F, -2F, 4F, 4F, 4F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, true);
        b.cube("Chestplate", "body", 16, 25, -4F, 1F, -3F, 8F, 6F, 1F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("HelmetR", "head", 16, 11, -6.5F, -3F, -4.5F, 1F, 5F, 9F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("ShoulderplateL3", "left_arm", 40, 33, 1.5F, 3.5F, -3.5F, 1F, 3F, 7F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("ShoulderplateLtop", "left_arm", 56, 25, 3.5F, -2.5F, -3.5F, 2F, 1F, 7F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("bottleL2", "left_leg", 88, 18, 2.95F, 1.5F, -1F, 2F, 1F, 2F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("BeltR", "body", 16, 36, -5F, 4F, -3F, 1F, 3F, 6F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("BackpackRoll", "body", 76, 32, -4.5F, 7F, 4F, 9F, 3F, 3F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("MbeltL", "body", 16, 36, 4F, 8F, -3F, 1F, 3F, 6F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("ShoulderL", "left_arm", 16, 45, -1.5F, -2.5F, -2.5F, 5F, 5F, 5F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, true);
        b.cube("MbeltR", "body", 16, 36, -5F, 8F, -3F, 1F, 3F, 6F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("ShoulderplateL2", "left_arm", 40, 33, 2.5F, 1.5F, -3.5F, 1F, 3F, 7F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("bottleR2", "right_leg", 88, 18, -5.05F, 1.5F, -1F, 2F, 1F, 2F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("bottleL1", "left_leg", 76, 10, 2.45F, 0.5F, -1.5F, 3F, 1F, 3F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("Backplate", "body", 36, 45, -4F, 1F, 2F, 8F, 11F, 2F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("Backpack", "body", 79, 45, -5.5F, 0F, 4F, 11F, 7F, 7F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("FrontclothR1", "body", 108, 38, 0F, 0F, 0F, 3F, 8F, 1F, 0F, -0.1047F, 0F, 0F, -3F, 11F, -2.9F, false);
        b.cube("FrontclothR2", "body", 108, 47, 0F, 7.5F, 1.7F, 3F, 3F, 1F, 0F, -0.3316F, 0F, 0F, -3F, 11F, -2.9F, false);
        b.cube("FrontclothL1", "body", 108, 38, 0F, 0F, 0F, 3F, 8F, 1F, 0F, -0.1047F, 0F, 0F, 0F, 11F, -2.9F, true);
        b.cube("FrontclothL2", "body", 108, 47, 0F, 7.5F, 1.7F, 3F, 3F, 1F, 0F, -0.3316F, 0F, 0F, 0F, 11F, -2.9F, true);
        b.cube("ClothBackR1", "body", 118, 16, 0F, 0F, 0F, 4F, 8F, 1F, 0F, 0.1047F, 0F, 0F, -4F, 11.5F, 2.9F, true);
        b.cube("ClothBackR2", "body", 123, 9, 0F, 7.8F, -0.9F, 1F, 2F, 1F, 0F, 0.2269F, 0F, 0F, -4F, 11.5F, 2.9F, false);
        b.cube("ClothBackR3", "body", 120, 12, 1F, 7.8F, -0.9F, 3F, 3F, 1F, 0F, 0.2269F, 0F, 0F, -4F, 11.5F, 2.9F, true);
        b.cube("ClothBackL1", "body", 118, 16, 0F, 0F, 0F, 4F, 8F, 1F, 0F, 0.1047F, 0F, 0F, 0F, 11.5F, 2.9F, false);
        b.cube("ClothBackL2", "body", 123, 9, 3F, 7.8F, -0.9F, 1F, 2F, 1F, 0F, 0.2269F, 0F, 0F, 0F, 11.5F, 2.9F, true);
        b.cube("ClothBackL3", "body", 120, 12, 0F, 7.8F, -0.9F, 3F, 3F, 1F, 0F, 0.2269F, 0F, 0F, 0F, 11.5F, 2.9F, false);
        b.cube("SideclothL2", "left_leg", 116, 34, 0.5F, 5.5F, -2.5F, 1F, 3F, 5F, 0F, 0F, 0F, -0.2967F, 0F, 0F, 0F, false);
        b.cube("SideclothR1", "right_leg", 116, 42, -2.5F, 0.5F, -2.5F, 1F, 5F, 5F, 0F, 0F, 0F, 0.1222F, 0F, 0F, 0F, false);
        b.cube("SideclothR2", "right_leg", 116, 34, -1.5F, 5.5F, -2.5F, 1F, 3F, 5F, 0F, 0F, 0F, 0.2967F, 0F, 0F, 0F, false);
        b.cube("SideclothR3", "body", 116, 1, 0.4F, 8.4F, -2.5F, 1F, 3F, 5F, 0F, 0F, 0F, 0.5236F, 0F, 0F, 0F, false);
        b.cube("SidepanelR1", "right_leg", 116, 25, -2.5F, 0.5F, -2.5F, 1F, 4F, 5F, 0F, 0F, 0F, 0.4363F, 0F, 0F, 0F, true);
        b.cube("LegpanelR6", "right_leg", 82, 38, -3F, 4.5F, -1.5F, 2F, 3F, 1F, 0F, -0.4363F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("LegpanelR5", "right_leg", 76, 42, -3F, 2.5F, -2.5F, 2F, 3F, 1F, 0F, -0.4363F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("LegpanelR4", "right_leg", 76, 38, -3F, 0.5F, -3.5F, 2F, 3F, 1F, 0F, -0.4363F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("SideclothL3", "body", 116, 1, -1.4F, 8.4F, -2.5F, 1F, 3F, 5F, 0F, 0F, 0F, -0.5236F, 0F, 0F, 0F, false);
        b.cube("SideclothL1", "left_leg", 116, 42, 1.5F, 0.5F, -2.5F, 1F, 5F, 5F, 0F, 0F, 0F, -0.1222F, 0F, 0F, 0F, false);
        b.cube("LegpanelL4", "left_leg", 76, 38, 1F, 0.5F, -3.5F, 2F, 3F, 1F, 0F, -0.4363F, 0F, 0F, 0F, 0F, 0F, true);
        b.cube("LegpanelL5", "left_leg", 76, 42, 1F, 2.5F, -2.5F, 2F, 3F, 1F, 0F, -0.4363F, 0F, 0F, 0F, 0F, 0F, true);
        b.cube("LegpanelL6", "left_leg", 82, 38, 1F, 4.5F, -1.5F, 2F, 3F, 1F, 0F, -0.4363F, 0F, 0F, 0F, 0F, 0F, true);
        b.cube("SidepanelL1", "left_leg", 116, 25, 1.5F, 0.5F, -2.5F, 1F, 4F, 5F, 0F, 0F, 0F, -0.4363F, 0F, 0F, 0F, false);
        return b.build();
    }
}
