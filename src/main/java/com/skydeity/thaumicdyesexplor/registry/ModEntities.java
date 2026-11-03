package com.skydeity.thaumicdyesexplor.registry;

import com.skydeity.thaumicdyesexplor.ThaumicDyesExplor;
import com.skydeity.thaumicdyesexplor.entity.RelicProjectileEntity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Forgotten Relics 的弹射物实体 —— 一种载体按变体承载九种弹丸
 * （混沌 / 绯红 / 暗物质 / 月辉 / 狂怒导弹 / 闪耀能量 / 灵魂能量 / 雷霆 / 巴比伦武装）。
 */
public final class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, ThaumicDyesExplor.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<RelicProjectileEntity>> RELIC_PROJECTILE =
            ENTITIES.register("relic_projectile", () -> EntityType.Builder
                    .<RelicProjectileEntity>of(RelicProjectileEntity::new, MobCategory.MISC)
                    .sized(0.4F, 0.4F)
                    .clientTrackingRange(6)
                    .updateInterval(2)
                    .build("relic_projectile"));

    private ModEntities() {
    }
}
