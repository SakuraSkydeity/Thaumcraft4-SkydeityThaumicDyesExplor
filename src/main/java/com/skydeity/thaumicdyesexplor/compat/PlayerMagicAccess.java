package com.skydeity.thaumicdyesexplor.compat;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.slf4j.Logger;

import com.skydeity.thaumicdyesexplor.ThaumicDyesExplor;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * 反射读取前置的“玩家符文护盾”状态（{@code thaumcraft:player_magic} 附件）。
 *
 * <p>前置把当前护盾值存在玩家 AttachmentType（注册 id {@code thaumcraft:player_magic}）里，
 * 公开 API 没有读口。附件类型本身在 NeoForge 注册表里可查，数据是前置的实现类
 * {@code PlayerMagicData}（record），所以走反射：注册表取类型 → record 访问器读值 →
 * record 构造器换值写回。与 {@code wand/AspectOrbAccess} 同一套防御式写法 ——
 * 前置改了结构就静默降级（返回 -1 / 写入失败），绝不抛异常带崩 tick。</p>
 */
public final class PlayerMagicAccess {

    private static final Logger LOG = ThaumicDyesExplor.LOGGER;

    private static boolean ready;
    private static boolean failed;
    private static AttachmentType<Object> attachmentType;
    private static Method mCharge;
    private static Method mMax;
    private static Constructor<?> ctor;

    private PlayerMagicAccess() {
    }

    private static synchronized boolean init() {
        if (ready || failed) {
            return ready;
        }
        try {
            attachmentType = (AttachmentType<Object>) NeoForgeRegistries.ATTACHMENT_TYPES
                    .get(ResourceLocation.fromNamespaceAndPath("thaumcraft", "player_magic"));
            Class<?> dataClass = Class.forName("dev.tc4port.thaumcraft.data.PlayerMagicData");
            mCharge = dataClass.getMethod("runicCharge");
            mMax = dataClass.getMethod("maximumRunicCharge");
            ctor = dataClass.getDeclaredConstructor(int.class, int.class, long.class, long.class,
                    long.class, long.class, long.class);
            ctor.setAccessible(true);
            ready = attachmentType != null;
            if (!ready) {
                failed = true;
                LOG.warn("[TDE] 未找到前置附件 thaumcraft:player_magic，破盾升级 1/3/4/6 将不生效");
            }
        } catch (Throwable t) {
            failed = true;
            LOG.warn("[TDE] 反射接入前置护盾状态失败（{}），破盾升级 1/3/4/6 将不生效", t.toString());
        }
        return ready;
    }

    /** 当前护盾值；不可用时返回 -1。 */
    public static int charge(Entity entity) {
        if (!init()) {
            return -1;
        }
        try {
            Object data = entity.getData(attachmentType);
            return data == null ? -1 : (Integer) mCharge.invoke(data);
        } catch (Throwable t) {
            return -1;
        }
    }

    /** 护盾上限；不可用时返回 -1。 */
    public static int maxCharge(Entity entity) {
        if (!init()) {
            return -1;
        }
        try {
            Object data = entity.getData(attachmentType);
            return data == null ? -1 : (Integer) mMax.invoke(data);
        } catch (Throwable t) {
            return -1;
        }
    }

    /**
     * 直接改写护盾值（应急护盾的“立刻回充”）。
     *
     * @return 是否写入成功
     */
    public static boolean setCharge(Entity entity, int newCharge) {
        if (!init() || entity.level().isClientSide) {
            return false;
        }
        try {
            Object data = entity.getData(attachmentType);
            if (data == null) {
                return false;
            }
            int max = (Integer) mMax.invoke(data);
            Object updated = ctor.newInstance(Math.max(0, Math.min(newCharge, max)), max,
                    record(data, 2), record(data, 3), record(data, 4), record(data, 5), record(data, 6));
            entity.setData(attachmentType, updated);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    /** record 的第 i 个 Long 分量（nextRunicRechargeTick 等，按声明顺序 2~6）。 */
    private static long record(Object data, int index) throws Exception {
        Method accessor = data.getClass().getRecordComponents()[index].getAccessor();
        return (Long) accessor.invoke(data);
    }
}
