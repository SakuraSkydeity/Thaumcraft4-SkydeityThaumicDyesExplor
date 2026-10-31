package com.skydeity.thaumicdyesexplor.client.model.geo;

import com.skydeity.thaumicdyesexplor.client.model.ArmorGeo;

/**
 * 符文护甲 III（Runic Primal）护甲几何，纹理 128x64 UV（贴图 256x128）。
 *
 * <p>由 {@code tools/port_armor_models.py} 从 1.7.10 源模组同名 Model 类自动转换生成，
 * 请勿手工修改；要改请改脚本或源模组数据。</p>
 */
public final class ModelRobesSpecialGeo {

    public static final int TEX_WIDTH = 128;
    public static final int TEX_HEIGHT = 64;

    private ModelRobesSpecialGeo() {
    }

    public static ArmorGeo build(float grow) {
        ArmorGeo.Builder b = new ArmorGeo.Builder(TEX_WIDTH, TEX_HEIGHT, grow);
        b.cube("Hood1", "head", 16, 7, -4.5F, -9F, -4.6F, 9F, 9F, 9F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("Hood2", "head", 52, 13, -4F, -9.7F, 2F, 8F, 9F, 3F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("Hood3", "head", 52, 14, -3.5F, -10F, 3.5F, 7F, 8F, 3F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("Hood4", "head", 53, 15, -3F, -10.7F, 3.5F, 6F, 7F, 3F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("Chestthing", "body", 56, 50, -2.5F, 1F, -4F, 5F, 7F, 1F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("Mbelt", "body", 16, 55, -4F, 7F, -3F, 8F, 5F, 1F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("MbeltB", "body", 16, 55, -4F, 7F, -4F, 8F, 5F, 1F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("ClothchestL", "body", 108, 38, 2.1F, 0.5F, -3.5F, 2F, 8F, 1F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, true);
        b.cube("ClothchestR", "body", 108, 38, -4.1F, 0.5F, -3.5F, 2F, 8F, 1F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("Book", "body", 81, 16, 1F, 0F, 4F, 5F, 7F, 2F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("Scroll", "body", 78, 25, -2F, 9.5F, 4F, 8F, 3F, 3F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("BeltR", "body", 16, 36, -5F, 4F, -3F, 1F, 3F, 6F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("Backplate", "body", 36, 45, -4F, 1F, 1.9F, 8F, 11F, 2F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("MbeltL", "body", 16, 36, 4F, 8F, -3F, 1F, 3F, 6F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("MbeltR", "body", 16, 36, -5F, 8F, -3F, 1F, 3F, 6F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("BeltL", "body", 16, 36, 4F, 4F, -3F, 1F, 3F, 6F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("Chestplate", "body", 16, 25, -4F, 1F, -3F, 8F, 6F, 1F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("ShoulderplateR1", "right_arm", 56, 33, -4.5F, -1.5F, -3.5F, 1F, 4F, 7F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("ShoulderplateR2", "right_arm", 40, 33, -3.5F, 1.5F, -3.5F, 1F, 3F, 7F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("ShoulderplateR3", "right_arm", 40, 33, -2.5F, 3.5F, -3.5F, 1F, 3F, 7F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("ShoulderplateTopR", "right_arm", 56, 25, -5.5F, -2.5F, -3.5F, 2F, 1F, 7F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("RArm1", "right_arm", 88, 39, -3.5F, 2.5F, -2.5F, 5F, 7F, 5F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("RArm2", "right_arm", 76, 32, -3F, 5.5F, 2.5F, 4F, 4F, 2F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("RArm3", "right_arm", 88, 32, -2.5F, 3.5F, 2.5F, 3F, 2F, 1F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("ShoulderplateL1", "left_arm", 56, 33, 3.5F, -1.5F, -3.5F, 1F, 4F, 7F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("ShoulderplateL2", "left_arm", 40, 33, 2.5F, 1.5F, -3.5F, 1F, 3F, 7F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("ShoulderplateL3", "left_arm", 40, 33, 1.5F, 3.5F, -3.5F, 1F, 3F, 7F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("ShoulderplateTopL", "left_arm", 56, 25, 3.5F, -2.5F, -3.5F, 2F, 1F, 7F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("ShoulderR", "right_arm", 16, 45, -3.5F, -2.5F, -2.5F, 5F, 5F, 5F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("LArm1", "left_arm", 88, 39, -1.5F, 2.5F, -2.5F, 5F, 7F, 5F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, true);
        b.cube("LArm2", "left_arm", 76, 32, -1F, 5.5F, 2.5F, 4F, 4F, 2F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("LArm3", "left_arm", 88, 32, -0.5F, 3.5F, 2.5F, 3F, 2F, 1F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("ShoulderL", "left_arm", 16, 45, -1.5F, -2.5F, -2.5F, 5F, 5F, 5F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, true);
        b.cube("FrontclothR1", "body", 108, 38, 0F, 0F, 0F, 3F, 8F, 1F, 0F, 0F, 0F, 0F, -3F, 11F, -2.9F, false);
        b.cube("FrontclothR2", "body", 108, 47, 0F, 7.5F, 1.7F, 3F, 3F, 1F, 0F, 0F, 0F, 0F, -3F, 11F, -2.9F, false);
        b.cube("FrontclothL1", "body", 108, 38, 0F, 0F, 0F, 3F, 8F, 1F, 0F, 0F, 0F, 0F, 0F, 11F, -2.9F, true);
        b.cube("FrontclothL2", "body", 108, 47, 0F, 7.5F, 1.7F, 3F, 3F, 1F, 0F, 0F, 0F, 0F, 0F, 11F, -2.9F, true);
        b.cube("ClothBackR1", "body", 118, 16, 0F, 0F, 0F, 4F, 8F, 1F, 0F, 0F, 0F, 0F, -4F, 11.5F, 2.9F, true);
        b.cube("ClothBackR2", "body", 123, 9, 0F, 7.8F, -0.9F, 1F, 2F, 1F, 0F, 0F, 0F, 0F, -4F, 11.5F, 2.9F, false);
        b.cube("ClothBackR3", "body", 120, 12, 1F, 7.8F, -0.9F, 3F, 3F, 1F, 0F, 0F, 0F, 0F, -4F, 11.5F, 2.9F, true);
        b.cube("ClothBackL1", "body", 118, 16, 0F, 0F, 0F, 4F, 8F, 1F, 0F, 0F, 0F, 0F, 0F, 11.5F, 2.9F, false);
        b.cube("ClothBackL2", "body", 123, 9, 3F, 7.8F, -0.9F, 1F, 2F, 1F, 0F, 0F, 0F, 0F, 0F, 11.5F, 2.9F, true);
        b.cube("ClothBackL3", "body", 120, 12, 0F, 7.8F, -0.9F, 3F, 3F, 1F, 0F, 0F, 0F, 0F, 0F, 11.5F, 2.9F, false);
        b.cube("SideclothL2", "left_leg", 116, 34, 0.5F, 5.5F, -2.5F, 1F, 3F, 5F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("SideclothR1", "right_leg", 116, 42, -2.5F, 0.5F, -2.5F, 1F, 5F, 5F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("SideclothR2", "right_leg", 116, 34, -1.5F, 5.5F, -2.5F, 1F, 3F, 5F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("SideclothR3", "right_leg", 116, 1, 0.4F, 8.4F, -2.5F, 1F, 3F, 5F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("SidepanelR1", "right_leg", 116, 25, -2.5F, 0.5F, -2.5F, 1F, 4F, 5F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, true);
        b.cube("LegpanelR6", "right_leg", 82, 38, -3F, 4.5F, -1.5F, 2F, 3F, 1F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("LegpanelR5", "right_leg", 76, 42, -3F, 2.5F, -2.5F, 2F, 3F, 1F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("LegpanelR4", "right_leg", 76, 38, -3F, 0.5F, -3.5F, 2F, 3F, 1F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("SideclothL3", "left_leg", 116, 1, -1.4F, 8.4F, -2.5F, 1F, 3F, 5F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("Focipouch", "left_leg", 100, 20, 3.5F, 0.5F, -2.5F, 3F, 6F, 5F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("SideclothL1", "left_leg", 116, 42, 1.5F, 0.5F, -2.5F, 1F, 5F, 5F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("LegpanelL4", "left_leg", 76, 38, 1F, 0.5F, -3.5F, 2F, 3F, 1F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, true);
        b.cube("LegpanelL5", "left_leg", 76, 42, 1F, 2.5F, -2.5F, 2F, 3F, 1F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, true);
        b.cube("LegpanelL6", "left_leg", 82, 38, 1F, 4.5F, -1.5F, 2F, 3F, 1F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, true);
        b.cube("SidepanelL1", "left_leg", 116, 25, 1.5F, 0.5F, -2.5F, 1F, 4F, 5F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("CloakAtL", "body", 78, 4, 2.5F, 1F, 2F, 2F, 1F, 3F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("Cloak1", "body", 78, 8, 0F, 0F, 0F, 9F, 12F, 1F, 0F, 0F, 0F, 0F, -4.5F, 1.3F, 4.2F, false);
        b.cube("CloakAtR", "body", 78, 4, -4.5F, 1F, 2F, 2F, 1F, 3F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("BackpanelL1", "left_leg", 0, 18, -2F, 0.5F, 2.5F, 5F, 3F, 1F, 0F, 0.4363F, 0F, 0F, 0F, 0F, 0F, true);
        b.cube("BackpanelL2", "left_leg", 0, 18, -2F, 2.5F, 1.5F, 5F, 3F, 1F, 0F, 0.4363F, 0F, 0F, 0F, 0F, 0F, true);
        b.cube("BackpanelL3", "left_leg", 0, 18, -2F, 4.5F, 0.5F, 5F, 3F, 1F, 0F, 0.4363F, 0F, 0F, 0F, 0F, 0F, true);
        b.cube("BackpanelR1", "right_leg", 0, 18, -3F, 0.5F, 2.5F, 5F, 3F, 1F, 0F, 0.4363F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("BackpanelR2", "right_leg", 0, 18, -3F, 2.5F, 1.5F, 5F, 3F, 1F, 0F, 0.4363F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("BackpanelR3", "right_leg", 0, 18, -3F, 4.5F, 0.5F, 5F, 3F, 1F, 0F, 0.4363F, 0F, 0F, 0F, 0F, 0F, false);
        b.empty("bipedRightArm", "body", -1.5708F, 0F, 0F);
        b.empty("bipedLeftArm", "body", -1.5708F, 0F, 0F);
        b.cube("Cloak2", "Cloak1", 78, 20, 4.5F, 10.7F, -1.6F, 9F, 4F, 1F, 0F, 0F, 0F, 0F, -4.5F, 1.3F, 0F, false);
        b.cube("Cloak3", "Cloak2", 78, 20, 9F, 12.8F, -4.4F, 9F, 4F, 1F, 0F, 0F, 0F, 0F, -4.5F, 1.3F, 0F, false);
        return b.build();
    }
}
