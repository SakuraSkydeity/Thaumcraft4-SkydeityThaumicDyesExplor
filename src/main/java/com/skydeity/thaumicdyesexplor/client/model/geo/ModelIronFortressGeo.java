package com.skydeity.thaumicdyesexplor.client.model.geo;

import com.skydeity.thaumicdyesexplor.client.model.ArmorGeo;

/**
 * 铁堡垒甲护甲几何。
 *
 * <p>由 {@code tools/port_armor_models.py} 从 1.7.10 源模组同名 Model 类自动转换生成，
 * 请勿手工修改；要改请改脚本或源模组数据。</p>
 */
public final class ModelIronFortressGeo {

    public static final int TEX_WIDTH = 128;
    public static final int TEX_HEIGHT = 64;

    private ModelIronFortressGeo() {
    }

    public static ArmorGeo build(float grow) {
        ArmorGeo.Builder b = new ArmorGeo.Builder(TEX_WIDTH, TEX_HEIGHT, grow);
        b.cube("Goggles", "body", 100, 18, -4.5F, -5F, -4.25F, 9F, 5F, 1F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("OrnamentL", "body", 78, 8, 1.5F, -9F, -6.5F, 2F, 2F, 1F, 0F, -0.1396F, 0F, 0F, 0F, 0F, 0F, true);
        b.cube("OrnamentL2", "body", 78, 8, 3.5F, -10F, -6.5F, 1F, 2F, 1F, 0F, -0.1396F, 0F, 0F, 0F, 0F, 0F, true);
        b.cube("OrnamentR", "body", 78, 8, -3.5F, -9F, -6.5F, 2F, 2F, 1F, 0F, -0.1396F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("OrnamentR2", "body", 78, 8, -4.5F, -10F, -6.5F, 1F, 2F, 1F, 0F, -0.1396F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("Helmet", "body", 41, 8, -4.5F, -9F, -4.5F, 9F, 4F, 9F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("HelmetR", "body", 21, 13, -6.5F, -3F, -4.5F, 1F, 5F, 9F, 0F, 0F, 0F, 0.5236F, 0F, 0F, 0F, false);
        b.cube("HelmetL", "body", 21, 13, 5.5F, -3F, -4.5F, 1F, 5F, 9F, 0F, 0F, 0F, -0.5236F, 0F, 0F, 0F, true);
        b.cube("HelmetB", "body", 41, 21, -4.5F, -3F, 5.5F, 9F, 5F, 1F, 0F, 0.5236F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("capsthingy", "body", 21, 0, -4.5F, -6F, -6.5F, 9F, 1F, 2F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("flapR", "body", 59, 10, -10F, -2F, -1F, 3F, 3F, 1F, 0F, 0F, -0.5236F, 0.5236F, 0F, 0F, 0F, false);
        b.cube("flapL", "body", 59, 10, 7F, -2F, -1F, 3F, 3F, 1F, 0F, 0F, 0.5236F, -0.5236F, 0F, 0F, 0F, true);
        b.cube("Gemornament", "body", 68, 11, -1.5F, -9F, -7F, 3F, 3F, 2F, 0F, -0.1396F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("Gem", "body", 72, 8, -1F, -8.5F, -7.5F, 2F, 2F, 1F, 0F, -0.1396F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("BeltR", "body", 76, 44, -5F, 4F, -3F, 1F, 3F, 6F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("Mbelt", "body", 56, 55, -4F, 8F, -3F, 8F, 4F, 1F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("MbeltL", "body", 76, 44, 4F, 8F, -3F, 1F, 3F, 6F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("MbeltR", "body", 76, 44, -5F, 8F, -3F, 1F, 3F, 6F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("BeltL", "body", 76, 44, 4F, 4F, -3F, 1F, 3F, 6F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("Chestplate", "body", 56, 45, -4F, 1F, -4F, 8F, 7F, 2F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("Backplate", "body", 36, 45, -4F, 1F, 2F, 8F, 11F, 2F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("ShoulderR", "body", 56, 35, -3.5F, -2.5F, -2.5F, 5F, 5F, 5F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("GauntletR", "body", 100, 26, -3.5F, 3.5F, -2.5F, 2F, 6F, 5F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("GauntletstrapR1", "body", 84, 31, -1.5F, 3.5F, -2.5F, 3F, 1F, 5F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("GauntletstrapR2", "body", 84, 31, -1.5F, 6.5F, -2.5F, 3F, 1F, 5F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("ShoulderplateRtop", "body", 110, 37, -5.5F, -2.5F, -3.5F, 2F, 1F, 7F, 0F, 0F, 0F, 0.4363F, 0F, 0F, 0F, false);
        b.cube("ShoulderplateR1", "body", 110, 45, -4.5F, -1.5F, -3.5F, 1F, 4F, 7F, 0F, 0F, 0F, 0.4363F, 0F, 0F, 0F, false);
        b.cube("ShoulderplateR2", "body", 94, 45, -3.5F, 1.5F, -3.5F, 1F, 3F, 7F, 0F, 0F, 0F, 0.4363F, 0F, 0F, 0F, false);
        b.cube("ShoulderplateR3", "body", 94, 45, -2.5F, 3.5F, -3.5F, 1F, 3F, 7F, 0F, 0F, 0F, 0.4363F, 0F, 0F, 0F, false);
        b.cube("ShoulderL", "body", 56, 35, -1.5F, -2.5F, -2.5F, 5F, 5F, 5F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, true);
        b.cube("GauntletL", "body", 114, 26, 1.5F, 3.5F, -2.5F, 2F, 6F, 5F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("Gauntletstrapl1", "body", 84, 31, -1.5F, 3.5F, -2.5F, 3F, 1F, 5F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, true);
        b.cube("GauntletstrapL2", "body", 84, 31, -1.5F, 6.5F, -2.5F, 3F, 1F, 5F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, true);
        b.cube("ShoulderplateLtop", "body", 110, 37, 3.5F, -2.5F, -3.5F, 2F, 1F, 7F, 0F, 0F, 0F, -0.4363F, 0F, 0F, 0F, true);
        b.cube("ShoulderplateL1", "body", 110, 45, 3.5F, -1.5F, -3.5F, 1F, 4F, 7F, 0F, 0F, 0F, -0.4363F, 0F, 0F, 0F, true);
        b.cube("ShoulderplateL2", "body", 94, 45, 2.5F, 1.5F, -3.5F, 1F, 3F, 7F, 0F, 0F, 0F, -0.4363F, 0F, 0F, 0F, true);
        b.cube("ShoulderplateL3", "body", 94, 45, 1.5F, 3.5F, -3.5F, 1F, 3F, 7F, 0F, 0F, 0F, -0.4363F, 0F, 0F, 0F, true);
        b.cube("LegpanelR1", "body", 0, 51, -1F, 0.5F, -3.5F, 3F, 4F, 1F, 0F, -0.4363F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("LegpanelR2", "body", 8, 51, -1F, 3.5F, -2.5F, 3F, 4F, 1F, 0F, -0.4363F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("LegpanelR3", "body", 0, 56, -1F, 6.5F, -1.5F, 3F, 3F, 1F, 0F, -0.4363F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("LegpanelR4", "body", 0, 43, -3F, 0.5F, -3.5F, 2F, 3F, 1F, 0F, -0.4363F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("LegpanelR5", "body", 0, 47, -3F, 2.5F, -2.5F, 2F, 3F, 1F, 0F, -0.4363F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("LegpanelR6", "body", 6, 43, -3F, 4.5F, -1.5F, 2F, 3F, 1F, 0F, -0.4363F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("SidepanelR1", "body", 0, 22, -2.5F, 0.5F, -2.5F, 1F, 4F, 5F, 0F, 0F, 0F, 0.4363F, 0F, 0F, 0F, false);
        b.cube("SidepanelR2", "body", 0, 31, -1.5F, 3.5F, -2.5F, 1F, 3F, 5F, 0F, 0F, 0F, 0.4363F, 0F, 0F, 0F, false);
        b.cube("SidepanelR3", "body", 12, 31, -0.5F, 5.5F, -2.5F, 1F, 3F, 5F, 0F, 0F, 0F, 0.4363F, 0F, 0F, 0F, false);
        b.cube("BackpanelR1", "body", 0, 18, -3F, 0.5F, 2.5F, 5F, 3F, 1F, 0F, 0.4363F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("BackpanelR2", "body", 0, 18, -3F, 2.5F, 1.5F, 5F, 3F, 1F, 0F, 0.4363F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("BackpanelR3", "body", 0, 18, -3F, 4.5F, 0.5F, 5F, 3F, 1F, 0F, 0.4363F, 0F, 0F, 0F, 0F, 0F, false);
        b.cube("LegpanelL1", "body", 0, 51, -2F, 0.5F, -3.5F, 3F, 4F, 1F, 0F, -0.4363F, 0F, 0F, 0F, 0F, 0F, true);
        b.cube("LegpanelL2", "body", 8, 51, -2F, 3.5F, -2.5F, 3F, 4F, 1F, 0F, -0.4363F, 0F, 0F, 0F, 0F, 0F, true);
        b.cube("LegpanelL3", "body", 0, 56, -2F, 6.5F, -1.5F, 3F, 3F, 1F, 0F, -0.4363F, 0F, 0F, 0F, 0F, 0F, true);
        b.cube("LegpanelL4", "body", 0, 43, 1F, 0.5F, -3.5F, 2F, 3F, 1F, 0F, -0.4363F, 0F, 0F, 0F, 0F, 0F, true);
        b.cube("LegpanelL5", "body", 0, 47, 1F, 2.5F, -2.5F, 2F, 3F, 1F, 0F, -0.4363F, 0F, 0F, 0F, 0F, 0F, true);
        b.cube("LegpanelL6", "body", 6, 43, 1F, 4.5F, -1.5F, 2F, 3F, 1F, 0F, -0.4363F, 0F, 0F, 0F, 0F, 0F, true);
        b.cube("SidepanelL1", "body", 0, 22, 1.5F, 0.5F, -2.5F, 1F, 4F, 5F, 0F, 0F, 0F, -0.4363F, 0F, 0F, 0F, true);
        b.cube("SidepanelL2", "body", 0, 31, 0.5F, 3.5F, -2.5F, 1F, 3F, 5F, 0F, 0F, 0F, -0.4363F, 0F, 0F, 0F, true);
        b.cube("SidepanelL3", "body", 12, 31, -0.5F, 5.5F, -2.5F, 1F, 3F, 5F, 0F, 0F, 0F, -0.4363F, 0F, 0F, 0F, true);
        b.cube("BackpanelL1", "body", 0, 18, -2F, 0.5F, 2.5F, 5F, 3F, 1F, 0F, 0.4363F, 0F, 0F, 0F, 0F, 0F, true);
        b.cube("BackpanelL2", "body", 0, 18, -2F, 2.5F, 1.5F, 5F, 3F, 1F, 0F, 0.4363F, 0F, 0F, 0F, 0F, 0F, true);
        b.cube("BackpanelL3", "body", 0, 18, -2F, 4.5F, 0.5F, 5F, 3F, 1F, 0F, 0.4363F, 0F, 0F, 0F, 0F, 0F, true);
        return b.build();
    }
}
