package com.skydeity.thaumicdyesexplor.client.model;

import com.skydeity.thaumicdyesexplor.item.ArmorGeoType;

/**
 * 护甲条件部件的显隐规则 —— 逐条对照源模组模型类的 {@code render} 方法。
 *
 * <p>部件名先经 {@link GeoArmorModel#normalize} 规范化（小写、{@code #} 转 {@code _}），
 * 再按前缀 / 后缀匹配。规则来源：</p>
 * <ul>
 *   <li><b>ModelFortress</b>（堡垒甲）：{@code set} 为腿/胸/头三件里穿上的件数 ——
 *       {@code set>=2} 显示书本、肩甲上层、腰垂、侧裙第二片；{@code set>=3} 再显示卷轴、
 *       胸口宝石与纹章、四片肩甲、侧裙第三片；护目镜由头盔 NBT 开关、面具三选一。</li>
 *   <li><b>ModelIronFortress</b>（铁堡垒）：同上，但书本 / 卷轴在源模组里已被注释掉（恒隐）。</li>
 *   <li><b>ModelRobesSpecial</b>（符文护甲 III）：{@code set} 为四件里穿上的件数，
 *       <b>只有穿满 4 件（{@code set>=4}）才显示整套进阶部件</b>（兜帽、胸甲、披风、
 *       肩甲、臂甲等 58 件）。</li>
 *   <li><b>ModelRanger / ModelRanger2</b>（游侠）：{@code Mask#0 / Mask#1} 两片面具 ——
 *       已染色版按头盔的面具编号二选一；未染色版固定戴 1 号（源模组写死）。
 *       <b>ModelArcher</b>（弓箭手）没有面具部件，全部恒显。</li>
 * </ul>
 */
public final class ArmorPartRules {

    /** 符文 III 在穿满 4 件前隐藏的部件（源模组 {@code isHidden = set < 4} 的全部 58 件）。 */
    private static final java.util.Set<String> PRIMAL_LOCKED = java.util.Set.of(
            "backpanell1", "backpanell2", "backpanelr1", "backpanelr2",
            "backplate", "beltl", "beltr", "chestplate", "chestthing",
            "cloak1", "cloak2", "cloak3", "cloakatl", "cloakatr",
            "clothchestl", "clothchestr",
            "frontclothl1", "frontclothl2", "frontclothr1", "frontclothr2",
            "hood1", "hood2", "hood3", "hood4",
            "larm1", "larm2", "larm3",
            "legpanell4", "legpanell5", "legpanell6", "legpanelr4", "legpanelr5", "legpanelr6",
            "mbelt", "mbeltb", "mbeltl", "mbeltr",
            "rarm1", "rarm2", "rarm3",
            "shoulderl", "shoulderr",
            "shoulderplatel1", "shoulderplatel2", "shoulderplatel3",
            "shoulderplater1", "shoulderplater2", "shoulderplater3",
            "shoulderplatetopl", "shoulderplatetopr",
            "sideclothl1", "sideclothl2", "sideclothl3", "sideclothr1", "sideclothr2", "sideclothr3",
            "sidepanell1", "sidepanelr1");

    /**
     * 符文 III 里源模组写死 {@code isHidden = true} 的部件：
     * 后布片六件、背板第三层两件、卷轴 / 书本 / 法杖袋（ModelRobesSpecial render 里恒隐，
     * 与套装件数无关）。
     */
    private static final java.util.Set<String> PRIMAL_ALWAYS_HIDDEN = java.util.Set.of(
            "clothbackl1", "clothbackl2", "clothbackl3",
            "clothbackr1", "clothbackr2", "clothbackr3",
            "backpanell3", "backpanelr3",
            "scroll", "book", "focipouch");

    public static boolean visible(ArmorGeoType type, ArmorWearerState state, String partName) {
        String part = GeoArmorModel.normalize(partName);
        return switch (type) {
            case FORTRESS -> fortressVisible(part, state, true);
            case IRON_FORTRESS -> fortressVisible(part, state, false);
            case ROBES_SPECIAL -> primalVisible(part, state);
            case RANGER -> rangerMaskVisible(part, state, true);
            case RANGER2 -> rangerMaskVisible(part, state, false);
            case ARCHER -> true; // ModelArcher 没有面具部件（源模组 render 里也没有面具逻辑）
            default -> true;
        };
    }

    private static boolean fortressVisible(String part, ArmorWearerState state, boolean includeBookScroll) {
        if (part.equals("goggles")) {
            return state.goggles();
        }
        // 三片面具（Mask#0/1/2，源模组 ModelFortress 的 `Mask[i].isHidden = mask != i`）三选一，
        // 没有装面具（mask = -1）时三片全隐。
        if (part.startsWith("mask_")) {
            return state.mask() >= 0 && part.equals("mask_" + state.mask());
        }
        if (part.equals("book")) {
            return includeBookScroll && state.setLevel() >= 2;
        }
        if (part.equals("scroll")) {
            return includeBookScroll && state.setLevel() >= 3;
        }
        // 胸口宝石与纹章、四片饰环：set>=3
        if (part.startsWith("ornament") || part.equals("gem") || part.equals("gemornament")) {
            return state.setLevel() >= 3;
        }
        // 肩甲：top/1 是 set>=2 的第一层，2/3 是 set>=3 的进阶层
        if (part.startsWith("shoulderplate")) {
            if (part.endsWith("top") || part.endsWith("1")) {
                return state.setLevel() >= 2;
            }
            if (part.endsWith("2") || part.endsWith("3")) {
                return state.setLevel() >= 3;
            }
        }
        // 腰垂与侧裙
        if (part.equals("flapl") || part.equals("flapr")
                || part.equals("sidepanell2") || part.equals("sidepanelr2")) {
            return state.setLevel() >= 2;
        }
        if (part.equals("sidepanell3") || part.equals("sidepanelr3")) {
            return state.setLevel() >= 3;
        }
        return true;
    }

    private static boolean primalVisible(String part, ArmorWearerState state) {
        if (PRIMAL_ALWAYS_HIDDEN.contains(part)) {
            return false;
        }
        // 胸口纹章 OrnamentL：set >= 3（源模组 `OrnamentL.isHidden = set < 3`）
        if (part.equals("ornamentl")) {
            return state.setLevel() >= 3;
        }
        return state.setLevel() >= 4 || !PRIMAL_LOCKED.contains(part);
    }

    /**
     * 游侠系两片面罩的二选一。
     *
     * <p>已染色游侠（ModelRanger）：{@code Mask[i].isHidden = mask != i}，由头盔面具编号决定；
     * 未染色游侠（ModelRanger2）：源模组写死 {@code Mask[0].isHidden = true;
     * Mask[1].isHidden = false} —— 固定戴 1 号面罩（已画好红漆的那张），与 NBT 无关。</p>
     */
    private static boolean rangerMaskVisible(String part, ArmorWearerState state, boolean byNbt) {
        if (!part.startsWith("mask_")) {
            return true;
        }
        if (!byNbt) {
            return part.equals("mask_1");
        }
        if (part.equals("mask_0")) {
            return state.mask() == 0;
        }
        if (part.equals("mask_1")) {
            return state.mask() == 1;
        }
        return true;
    }

    private ArmorPartRules() {
    }
}
