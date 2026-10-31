package com.skydeity.thaumicdyesexplor.client.model.geo;

import com.skydeity.thaumicdyesexplor.client.model.ArmorGeo;

/**
 * 骑士甲（Cultist Knight / Sun Knight / Inhabited Plate）护甲几何。
 *
 * <p>由 {@code tools/port_armor_models.py} 从 1.7.10 源模组同名 Model 类自动转换生成，
 * 请勿手工修改；要改请改脚本或源模组数据。</p>
 */
public final class ModelKnightGeo {

    public static final int TEX_WIDTH = 128;
    public static final int TEX_HEIGHT = 64;

    private ModelKnightGeo() {
    }

    public static ArmorGeo build(float grow) {
        ArmorGeo.Builder b = new ArmorGeo.Builder(TEX_WIDTH, TEX_HEIGHT, grow);
        b.cube("Helmet", "head", 41, 8, -4.5F, -9F, -4.5F, 9F, 9F, 9F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("BeltR", "body", 76, 44, -5F, 4F, -3F, 1F, 3F, 6F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("Mbelt", "body", 56, 55, -4F, 8F, -3F, 8F, 4F, 1F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("MbeltL", "body", 76, 44, 4F, 8F, -3F, 1F, 3F, 6F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("MbeltR", "body", 76, 44, -5F, 8F, -3F, 1F, 3F, 6F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("BeltL", "body", 76, 44, 4F, 4F, -3F, 1F, 3F, 6F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("Tabbard", "body", 114, 52, -3F, 1.2F, -3.5F, 6F, 10F, 1F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("CloakAtL", "body", 0, 43, 2.5F, 1F, 2F, 2F, 1F, 3F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("Backplate", "body", 36, 45, -4F, 1F, 2F, 8F, 11F, 2F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("Cloak1", "body", 0, 47, 0F, 0F, 0F, 9F, 12F, 1F, 0F, 0F, 0F, 0F, -4.5F, 1.3F, 4.2F, false);
        b.cube("Cloak2", "body", 0, 59, 0F, 11.7F, -2F, 9F, 4F, 1F, 0F, 0F, 0F, 0F, -4.5F, 1.3F, 4.2F, false);
        b.cube("Cloak3", "body", 0, 59, 0F, 15.2F, -4.2F, 9F, 4F, 1F, 0F, 0F, 0F, 0F, -4.5F, 1.3F, 4.2F, false);
        b.cube("CloakAtR", "body", 0, 43, -4.5F, 1F, 2F, 2F, 1F, 3F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("Chestplate", "body", 56, 45, -4F, 1F, -3F, 8F, 7F, 1F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("ShoulderR1", "right_arm", 0, 19, -3.3F, 3.5F, -2.5F, 1F, 1F, 5F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("GauntletR", "right_arm", 100, 26, -3.5F, 3.5F, -2.5F, 2F, 6F, 5F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, true);
        b.cube("GauntletstrapR1", "right_arm", 84, 31, -1.5F, 3.5F, -2.5F, 3F, 1F, 5F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, true);
        b.cube("GauntletstrapR2", "right_arm", 84, 31, -1.5F, 6.5F, -2.5F, 3F, 1F, 5F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, true);
        b.cube("ShoulderR", "right_arm", 56, 35, -3.5F, -2.5F, -2.5F, 5F, 5F, 5F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("ShoulderR0", "right_arm", 0, 0, -4.3F, -1.5F, -3F, 3F, 5F, 6F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, true);
        b.cube("ShoulderR2", "right_arm", 0, 11, -2.3F, 3.5F, -3F, 1F, 2F, 6F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, true);
        b.cube("ShoulderL1", "left_arm", 0, 19, 2.3F, 3.5F, -2.5F, 1F, 1F, 5F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, true);
        b.cube("GauntletL", "left_arm", 114, 26, 1.5F, 3.5F, -2.5F, 2F, 6F, 5F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("GauntletstrapL1", "left_arm", 84, 31, -1.5F, 3.5F, -2.5F, 3F, 1F, 5F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, true);
        b.cube("GauntletstrapL2", "left_arm", 84, 31, -1.5F, 6.5F, -2.5F, 3F, 1F, 5F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, true);
        b.cube("ShoulderL", "left_arm", 56, 35, -1.5F, -2.5F, -2.5F, 5F, 5F, 5F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, true);
        b.cube("ShoulderL0", "left_arm", 0, 0, 1.3F, -1.5F, -3F, 3F, 5F, 6F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("ShoulderL2", "left_arm", 0, 11, 1.3F, 3.5F, -3F, 1F, 2F, 6F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("SidepanelR3", "right_leg", 116, 13, -3F, 2.5F, -2.5F, 1F, 4F, 5F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("SidepanelR2", "right_leg", 114, 5, -2F, 2.5F, -2.5F, 2F, 3F, 5F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, true);
        b.cube("SidepanelL2", "left_leg", 114, 5, 0F, 2.5F, -2.5F, 2F, 3F, 5F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("SidepanelR0", "right_leg", 96, 14, -3F, -0.5F, -2.5F, 5F, 3F, 5F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("SidepanelL0", "left_leg", 96, 14, -2F, -0.5F, -2.5F, 5F, 3F, 5F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("SidepanelR1", "right_leg", 96, 7, 0F, 2.5F, -2.5F, 2F, 2F, 5F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, true);
        b.cube("SidepanelL3", "left_leg", 116, 13, 2F, 2.5F, -2.5F, 1F, 4F, 5F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("SidepanelL1", "left_leg", 96, 7, -2F, 2.5F, -2.5F, 2F, 2F, 5F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("Frontcloth1", "body", 120, 39, 0F, 0F, 0F, 6F, 8F, 1F, 0F, 0F, 0F, 0F, -3F, 11F, -3.5F, false);
        b.cube("Frontcloth2", "body", 100, 37, 0F, 7.5F, 1.8F, 6F, 3F, 1F, 0F, 0F, 0F, 0F, -3F, 11F, -3.5F, false);
        b.empty("bipedRightArm", "body", -1.5708F, 0F, 0F);
        b.empty("bipedLeftArm", "body", -1.5708F, 0F, 0F);
        return b.build();
    }
}
