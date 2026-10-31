package com.skydeity.thaumicdyesexplor.client.model.geo;

import com.skydeity.thaumicdyesexplor.client.model.ArmorGeo;

/**
 * 弓箭手甲护甲几何。
 *
 * <p>由 {@code tools/port_armor_models.py} 从 1.7.10 源模组同名 Model 类自动转换生成，
 * 请勿手工修改；要改请改脚本或源模组数据。</p>
 */
public final class ModelArcherGeo {

    public static final int TEX_WIDTH = 128;
    public static final int TEX_HEIGHT = 64;

    private ModelArcherGeo() {
    }

    public static ArmorGeo build(float grow) {
        ArmorGeo.Builder b = new ArmorGeo.Builder(TEX_WIDTH, TEX_HEIGHT, grow);
        b.cube("BackpanelR3", "right_leg", 84, 3, 0F, 2.5F, -2.5F, 2F, 2F, 5F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("ShoulderL3", "left_arm", 0, 50, 2.3F, 3F, -3F, 1F, 2F, 6F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("Quiver", "body", 85, 18, 1F, 0F, 3F, 5F, 8F, 4F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("Chestplate", "body", 16, 25, -4F, 1F, -3F, 8F, 6F, 1F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("ShoulderL2", "left_arm", 0, 58, 3.3F, 3F, -2.5F, 1F, 1F, 5F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("BackpanelL1", "left_leg", 104, 9, 2F, 2.5F, -2.5F, 1F, 6F, 5F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("Helmet1", "head", 2, 1, -5.5F, -6F, -5.5F, 11F, 1F, 11F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("Mbelt", "body", 16, 55, -4F, 7F, -3F, 8F, 5F, 1F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("MbeltB", "body", 16, 55, -4F, 7F, -4F, 8F, 5F, 1F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("ShoulderR", "right_arm", 16, 45, -3.5F, -2.5F, -2.5F, 5F, 5F, 5F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("GauntletstrapR1", "right_arm", 84, 58, -1.5F, 3.5F, -2.5F, 3F, 1F, 5F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("GauntletL", "left_arm", 114, 53, 1.5F, 3.5F, -2.5F, 2F, 6F, 5F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("ShoulderL", "left_arm", 16, 45, -1.5F, -2.5F, -2.5F, 5F, 5F, 5F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, true);
        b.cube("LegpanelL5", "body", 76, 42, 1F, 2.5F, -2.5F, 2F, 3F, 1F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, true);
        b.cube("BackpanelL2", "left_leg", 84, 10, -2F, -0.5F, -2.5F, 5F, 3F, 5F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("GauntletstrapL2", "left_arm", 84, 58, -1.5F, 6.5F, -2.5F, 3F, 1F, 5F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("BackpanelR1", "right_leg", 84, 10, -3F, -0.5F, -2.5F, 5F, 3F, 5F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("ShoulderR5", "right_arm", 0, 38, -3.3F, -2F, 3F, 1F, 6F, 1F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("BackpanelL3", "left_leg", 84, 3, -2F, 2.5F, -2.5F, 2F, 2F, 5F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("Helmet", "head", 48, 0, -4.5F, -5F, -4.5F, 9F, 5F, 9F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("GauntletstrapL1", "left_arm", 84, 58, -1.5F, 3.5F, -2.5F, 3F, 1F, 5F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("clothchestL", "body", 108, 38, 0.1F, -11.5F, -3.5F, 2F, 8F, 1F, 0F, 0F, 0F, 0F, 2F, 12F, 0F, true);
        b.cube("ShoulderR1", "right_arm", 0, 39, -4.3F, -2F, -3F, 1F, 5F, 6F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("MbeltR", "body", 16, 36, -5F, 8F, -3F, 1F, 3F, 6F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("LegpanelR6", "body", 82, 38, -3F, 4.5F, -1.5F, 2F, 3F, 1F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("Chestthing", "body", 56, 50, -2.5F, 1F, -4F, 5F, 7F, 1F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("BackpanelR4", "right_leg", 102, 1, -2F, 2.5F, -2.5F, 2F, 3F, 5F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("ShoulderR4", "right_arm", 0, 38, -3.3F, -2F, -4F, 1F, 6F, 1F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("clothchestR", "body", 108, 38, -6.1F, -11.5F, -3.5F, 2F, 8F, 1F, 0F, 0F, 0F, 0F, 2F, 12F, 0F, false);
        b.cube("ShoulderR3", "right_arm", 0, 50, -3.3F, 3F, -3F, 1F, 2F, 6F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("LegpanelR4", "body", 76, 38, -3F, 0.5F, -3.5F, 2F, 3F, 1F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("ShoulderL1", "left_arm", 0, 39, 3.3F, -2F, -3F, 1F, 5F, 6F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("BeltR", "body", 16, 36, -5F, 4F, -3F, 1F, 3F, 6F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("GauntletstrapR2", "right_arm", 84, 58, -1.5F, 6.5F, -2.5F, 3F, 1F, 5F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("LegpanelL1", "body", 123, 9, 1F, 8.8F, 1.3F, 1F, 2F, 1F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, true);
        b.cube("Backplate", "body", 38, 46, -4F, 1F, 2F, 8F, 11F, 1F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("BackpanelL4", "left_leg", 102, 1, 0F, 2.5F, -2.5F, 2F, 3F, 5F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("LegpanelR5", "body", 76, 42, -3F, 2.5F, -2.5F, 2F, 3F, 1F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("BackpanelR2", "right_leg", 104, 9, -3F, 2.5F, -2.5F, 1F, 6F, 5F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("ShoulderL4", "left_arm", 0, 38, 2.3F, -2F, -4F, 1F, 6F, 1F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("LegpanelL6", "body", 82, 38, 1F, 4.5F, -1.5F, 2F, 3F, 1F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, true);
        b.cube("ShoulderR2", "right_arm", 0, 58, -4.3F, 3F, -2.5F, 1F, 1F, 5F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("ShoulderL5", "left_arm", 0, 38, 2.3F, -2F, 3F, 1F, 6F, 1F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("BeltL", "body", 16, 36, 4F, 4F, -3F, 1F, 3F, 6F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("Helmet2", "head", 0, 13, -4.5F, -9F, -4.5F, 9F, 3F, 9F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("MbeltL", "body", 16, 36, 4F, 8F, -3F, 1F, 3F, 6F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("LegpanelL4", "body", 76, 38, 1F, 0.5F, -3.5F, 2F, 3F, 1F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, true);
        b.cube("GauntletR", "right_arm", 100, 53, -3.5F, 3.5F, -2.5F, 2F, 6F, 5F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
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
        b.empty("bipedRightArm", "body", -1.5708F, 0F, 0F);
        b.empty("bipedLeftArm", "body", -1.5708F, 0F, 0F);
        return b.build();
    }
}
