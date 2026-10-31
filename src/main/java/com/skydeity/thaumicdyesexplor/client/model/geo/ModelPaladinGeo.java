package com.skydeity.thaumicdyesexplor.client.model.geo;

import com.skydeity.thaumicdyesexplor.client.model.ArmorGeo;

/**
 * 圣骑士头盔护甲几何。
 *
 * <p>由 {@code tools/port_armor_models.py} 从 1.7.10 源模组同名 Model 类自动转换生成，
 * 请勿手工修改；要改请改脚本或源模组数据。</p>
 */
public final class ModelPaladinGeo {

    public static final int TEX_WIDTH = 128;
    public static final int TEX_HEIGHT = 64;

    private ModelPaladinGeo() {
    }

    public static ArmorGeo build(float grow) {
        ArmorGeo.Builder b = new ArmorGeo.Builder(TEX_WIDTH, TEX_HEIGHT, grow);
        b.cube("Helmet", "body", 41, 8, -4.5F, -9F, -4.5F, 9F, 9F, 9F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("CollarF", "body", 17, 31, -4.5F, -1.5F, -3F, 9F, 4F, 1F, 0F, 0.2269F, 0F, 0F, 0F, 0F, -2.5F, false);
        b.cube("CollarB", "body", 17, 26, -4.5F, -1.5F, 7F, 9F, 4F, 1F, 0F, 0.2269F, 0F, 0F, 0F, 0F, -2.5F, false);
        b.cube("CollarR", "body", 17, 11, -5.5F, -1.5F, -3F, 1F, 4F, 11F, 0F, 0.2269F, 0F, 0F, 0F, 0F, -2.5F, false);
        b.cube("CollarL", "body", 17, 11, 4.5F, -1.5F, -3F, 1F, 4F, 11F, 0F, 0.2269F, 0F, 0F, 0F, 0F, -2.5F, false);
        b.cube("BeltR", "body", 76, 44, -5F, 4F, -3F, 1F, 3F, 6F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("Mbelt", "body", 56, 55, -4F, 8F, -3F, 8F, 4F, 1F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("MbeltL", "body", 76, 44, 4F, 8F, -3F, 1F, 3F, 6F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("MbeltR", "body", 76, 44, -5F, 8F, -3F, 1F, 3F, 6F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("BeltL", "body", 76, 44, 4F, 4F, -3F, 1F, 3F, 6F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("CloakTL", "body", 0, 43, 2.5F, 1F, -1F, 2F, 1F, 3F, 0F, 0.1396F, 0F, 0F, 0F, 0F, 3F, false);
        b.cube("Cloak3", "body", 0, 59, -4.5F, 17F, -3.7F, 9F, 4F, 1F, 0F, 0.4466F, 0F, 0F, 0F, 0F, 3F, false);
        b.cube("CloakTR", "body", 0, 43, -4.5F, 1F, -1F, 2F, 1F, 3F, 0F, 0.1396F, 0F, 0F, 0F, 0F, 3F, false);
        b.cube("Cloak1", "body", 0, 47, -4.5F, 2F, 1F, 9F, 12F, 1F, 0F, 0.1396F, 0F, 0F, 0F, 0F, 3F, false);
        b.cube("Cloak2", "body", 0, 59, -4.5F, 14F, -1.3F, 9F, 4F, 1F, 0F, 0.3069F, 0F, 0F, 0F, 0F, 3F, false);
        b.cube("Chestplate", "body", 56, 45, -4F, 1F, -3.8F, 8F, 7F, 2F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("ChestOrnament", "body", 76, 53, -2.5F, 3F, -4.8F, 5F, 5F, 1F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("ChestClothL", "body", 20, 47, 1.5F, 1.2F, -4.5F, 3F, 9F, 1F, 0F, 0.0663F, 0F, 0F, 0F, 0F, 0F, true);
        b.cube("ChestClothR", "body", 20, 47, -4.5F, 1.2F, -4.5F, 3F, 9F, 1F, 0F, 0.0663F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("Backplate", "body", 36, 45, -4F, 1F, 2F, 8F, 11F, 2F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("GauntletR", "body", 100, 26, -3.5F, 3.5F, -2.5F, 2F, 6F, 5F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("GauntletL", "body", 114, 26, 1.5F, 3.5F, -2.5F, 2F, 6F, 5F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("GauntletstrapL1", "body", 84, 31, -1.5F, 3.5F, -2.5F, 3F, 1F, 5F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, true);
        b.cube("GauntletstrapL2", "body", 84, 31, -1.5F, 6.5F, -2.5F, 3F, 1F, 5F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, true);
        b.cube("GauntletstrapR1", "body", 84, 31, -1.5F, 3.5F, -2.5F, 3F, 1F, 5F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("GauntletstrapR2", "body", 84, 31, -1.5F, 6.5F, -2.5F, 3F, 1F, 5F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("GauntletR2", "body", 102, 37, -5F, 3.5F, -2F, 1F, 5F, 4F, 0F, 0F, 0F, -0.1676F, 0F, 0F, 0F, false);
        b.cube("GauntletL2", "body", 102, 37, 4F, 3.5F, -2F, 1F, 5F, 4F, 0F, 0F, 0F, 0.1676F, 0F, 0F, 0F, false);
        b.cube("ShoulderR", "body", 56, 35, -3.5F, -2.5F, -2.5F, 5F, 5F, 5F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("ShoulderR1", "body", 0, 0, -4.3F, -1.5F, -3F, 3F, 5F, 6F, 0F, 0F, 0F, 0.7854F, 0F, 0F, 0F, false);
        b.cube("ShoulderR2", "body", 0, 19, -3.3F, 3.5F, -2.5F, 1F, 1F, 5F, 0F, 0F, 0F, 0.7854F, 0F, 0F, 0F, false);
        b.cube("ShoulderR5", "body", 18, 4, -2.3F, -1.5F, 3F, 1F, 6F, 1F, 0F, 0F, 0F, 0.7854F, 0F, 0F, 0F, false);
        b.cube("ShoulderR3", "body", 0, 11, -2.3F, 3.5F, -3F, 1F, 2F, 6F, 0F, 0F, 0F, 0.7854F, 0F, 0F, 0F, false);
        b.cube("ShoulderR4", "body", 18, 4, -2.3F, -1.5F, -4F, 1F, 6F, 1F, 0F, 0F, 0F, 0.7854F, 0F, 0F, 0F, false);
        b.cube("ShoulderL", "body", 56, 35, -1.5F, -2.5F, -2.5F, 5F, 5F, 5F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, true);
        b.cube("ShoulderL1", "body", 0, 0, 1.3F, -1.5F, -3F, 3F, 5F, 6F, 0F, 0F, 0F, -0.7854F, 0F, 0F, 0F, false);
        b.cube("ShoulderL2", "body", 0, 19, 2.3F, 3.5F, -2.5F, 1F, 1F, 5F, 0F, 0F, 0F, -0.7854F, 0F, 0F, 0F, true);
        b.cube("ShoulderL3", "body", 0, 11, 1.3F, 3.5F, -3F, 1F, 2F, 6F, 0F, 0F, 0F, -0.7854F, 0F, 0F, 0F, false);
        b.cube("ShoulderL5", "body", 18, 4, 1.3F, -1.5F, 3F, 1F, 6F, 1F, 0F, 0F, 0F, -0.7854F, 0F, 0F, 0F, false);
        b.cube("ShoulderL4", "body", 18, 4, 1.3F, -1.5F, -4F, 1F, 6F, 1F, 0F, 0F, 0F, -0.7854F, 0F, 0F, 0F, false);
        b.cube("LegClothR", "body", 20, 55, 0F, 0F, 0F, 3F, 8F, 1F, 0F, -0.0349F, 0F, 0F, -4.5F, 10.4F, -3.9F, false);
        b.cube("LegClothL", "body", 20, 55, 0F, 0F, 0F, 3F, 8F, 1F, 0F, -0.0349F, 0F, 0F, 1.5F, 10.4F, -3.9F, true);
        b.cube("BackpanelR1", "body", 0, 25, -3F, -0.5F, 2.5F, 5F, 7F, 1F, 0F, 0.0698F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("BackpanelR2", "body", 96, 14, -3F, -0.5F, -2.5F, 5F, 3F, 5F, 0F, 0F, 0F, 0.1396F, 0F, 0F, 0F, false);
        b.cube("BackpanelR3", "body", 116, 13, -3F, 2.5F, -2.5F, 1F, 4F, 5F, 0F, 0F, 0F, 0.1396F, 0F, 0F, 0F, false);
        b.cube("BackpanelR4", "body", 0, 25, -3F, -0.5F, -3.5F, 5F, 7F, 1F, 0F, -0.0349F, 0F, 0F, 0F, 0F, 0F, true);
        b.cube("BackpanelL1", "body", 0, 25, -2F, -0.5F, 2.5F, 5F, 7F, 1F, 0F, 0.0698F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("BackpanelL4", "body", 0, 25, -2F, -0.5F, -3.5F, 5F, 7F, 1F, 0F, -0.0349F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("BackpanelL2", "body", 96, 14, -2F, -0.5F, -2.5F, 5F, 3F, 5F, 0F, 0F, 0F, -0.1396F, 0F, 0F, 0F, false);
        b.cube("BackpanelL3", "body", 116, 13, 2F, 2.5F, -2.5F, 1F, 4F, 5F, 0F, 0F, 0F, -0.1396F, 0F, 0F, 0F, false);
        b.cube("WingR1", "body", 41, 26, -5F, -7F, -0.5F, 1F, 4F, 4F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("WingR2", "body", 41, 34, -5F, -7F, 3.5F, 1F, 3F, 2F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("WingR3", "body", 47, 34, -5F, -8F, 5.5F, 1F, 3F, 1F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("WingR4", "body", 41, 39, -5F, -8F, 4.5F, 1F, 1F, 1F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("WingL1", "body", 41, 26, 4F, -7F, -0.5F, 1F, 4F, 4F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("WingL2", "body", 41, 34, 4F, -7F, 3.5F, 1F, 3F, 2F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("WingL3", "body", 47, 34, 4F, -8F, 5.5F, 1F, 3F, 1F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("WingL4", "body", 41, 39, 4F, -8F, 4.5F, 1F, 1F, 1F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        return b.build();
    }
}
