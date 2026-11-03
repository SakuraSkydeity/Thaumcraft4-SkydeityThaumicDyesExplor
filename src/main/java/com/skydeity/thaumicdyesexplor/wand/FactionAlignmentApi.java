package com.skydeity.thaumicdyesexplor.wand;

/**
 * 派系结盟值查询接口 —— MiddleEarth 派系杖芯回 vis 用。
 *
 * <p><b>调研结论（2026-11）</b>：经典 LOTR 模组没有 1.21.1 版本（Legacy 停在 1.7.10、
 * Renewed 停在 1.16.5）；1.21.1 NeoForge 上出现了带派系/结盟系统的第三方中洲模组
 * （LOTR 2.0、Eldritch Realm Reborn），但均未确认公开结盟 API，modid 也未固定。
 * 因此这里做成可插拔接口：将来确认了某个模组的 API，写一个实现换进
 * {@link #PROVIDER} 即可；在那之前用 {@link FixedAlignmentProvider} 兜底 ——
 * 按 roadmap 预案视为结盟 +100（每 20 tick 回 5 vis 一档）。</p>
 */
public interface FactionAlignmentApi {

    /** 查询玩家对某派系的结盟值（源模组 LOTRPlayerData.getAlignment 的量纲，100 起 pledge）。 */
    float alignment(net.minecraft.world.entity.player.Player player, String faction);

    /** 当前生效的结盟数据源（未来接入真实 LOTR API 时替换）。 */
    FactionAlignmentApi PROVIDER = new FixedAlignmentProvider();

    /** 兜底实现：一律视为 +100 结盟（回 vis 5 的一档）。 */
    final class FixedAlignmentProvider implements FactionAlignmentApi {
        @Override
        public float alignment(net.minecraft.world.entity.player.Player player, String faction) {
            return 100.0F;
        }
    }
}
