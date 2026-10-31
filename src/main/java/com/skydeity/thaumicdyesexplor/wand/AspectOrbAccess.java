package com.skydeity.thaumicdyesexplor.wand;

import java.lang.reflect.Method;

import javax.annotation.Nullable;

import dev.tc4port.thaumcraft.api.aspect.AspectId;

import net.minecraft.world.entity.Entity;

/**
 * 只读访问 TC4R 要素球（{@code thaumcraft:aspect_orb}）的「要素 + 数量」。
 *
 * <p>要素球实体的实现类（{@code dev.tc4port.thaumcraft.entity.AspectOrbEntity}）不在公开 API jar 里，
 * 前置也没有提供「吸收要素球」的 API。这里用反射读取它的三个公开方法
 * {@code aspect()}/{@code aspectValue()}/{@code setAspectValue(int)}：方法名在官方映射下是稳定的，
 * 万一哪天前置改名，{@link #isAvailable()} 会变 false，调用方直接跳过这一条杖芯特性即可，不会崩游戏。</p>
 */
public final class AspectOrbAccess {

    private static final String ORB_CLASS = "dev.tc4port.thaumcraft.entity.AspectOrbEntity";

    @Nullable
    private static final Method ASPECT = find("aspect");

    @Nullable
    private static final Method VALUE = find("aspectValue");

    @Nullable
    private static final Method SET_VALUE = findIntSetter("setAspectValue");

    private AspectOrbAccess() {
    }

    @Nullable
    private static Method find(String name) {
        try {
            Class<?> type = Class.forName(ORB_CLASS);
            return type.getMethod(name);
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return null;
        }
    }

    @Nullable
    private static Method findIntSetter(String name) {
        try {
            Class<?> type = Class.forName(ORB_CLASS);
            return type.getMethod(name, int.class);
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return null;
        }
    }

    /** 这条反射通路是否可用。 */
    public static boolean isAvailable() {
        return ASPECT != null && VALUE != null && SET_VALUE != null;
    }

    /** 这个实体是不是要素球。 */
    public static boolean isOrb(Entity entity) {
        if (!isAvailable()) {
            return false;
        }
        for (Class<?> type = entity.getClass(); type != null; type = type.getSuperclass()) {
            if (ORB_CLASS.equals(type.getName())) {
                return true;
            }
        }
        return false;
    }

    /** 要素球里的要素；读取失败返回 null。 */
    @Nullable
    public static AspectId aspectOf(Entity entity) {
        if (!isOrb(entity)) {
            return null;
        }
        try {
            return (AspectId) ASPECT.invoke(entity);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return null;
        }
    }

    /** 要素球里的要素量（vis 点数）；读取失败返回 0。 */
    public static int valueOf(Entity entity) {
        if (!isOrb(entity)) {
            return 0;
        }
        try {
            return (int) VALUE.invoke(entity);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return 0;
        }
    }

    /** 把要素球的存量改成指定的余量；失败则视为无效。 */
    public static boolean setValue(Entity entity, int value) {
        if (!isOrb(entity)) {
            return false;
        }
        try {
            SET_VALUE.invoke(entity, value);
            return true;
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return false;
        }
    }
}
