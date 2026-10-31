package com.skydeity.thaumicdyesexplor.item;

/**
 * 护甲几何类型 —— 对应 1.7.10 源模组 {@code client/models} 下的各个 Tabula 模型类。
 *
 * <p>仅客户端使用：由 {@code client/model/ArmorGeoModels} 按类型取“烘焙好的”人形模型。
 * 有几何的护甲走自定义模型渲染；{@link #NONE} 表示沿用原版人形护甲模型（贴图仍是 64×32 布局，
 * 例如符文护甲 I/II、水晶护甲、旅者靴）。</p>
 */
public enum ArmorGeoType {

    /** 原版人形护甲模型（64×32 贴图）。 */
    NONE,
    /** {@code ModelRobes} —— 染袍 / 神秘法袍 / 虚空法袍。 */
    ROBES,
    /** {@code ModelRobesSpecial} —— 符文护甲 III。 */
    ROBES_SPECIAL,
    /** {@code ModelKnight} —— 骑士甲 / 太阳骑士 / 寄居板甲。 */
    KNIGHT,
    /** {@code ModelPraetor} —— 领袖甲。 */
    PRAETOR,
    /** {@code ModelRanger} —— 游侠甲（已染色）。 */
    RANGER,
    /** {@code ModelRanger2} —— 游侠甲（未染色）。 */
    RANGER2,
    /** {@code ModelArcher} —— 弓箭手甲。 */
    ARCHER,
    /** {@code ModelFortress} —— 堡垒甲。 */
    FORTRESS,
    /** {@code ModelIronFortress} —— 铁堡垒甲。 */
    IRON_FORTRESS,
    /** {@code ModelPaladin} —— 圣骑士头盔。 */
    PALADIN
}
