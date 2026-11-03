package com.skydeity.thaumicdyesexplor.entity;

import javax.annotation.Nullable;

import com.skydeity.thaumicdyesexplor.registry.ModEntities;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Forgotten Relics 的弹射物载体 —— 一种实体按 {@link Variant} 承载九种弹丸。
 *
 * <p>源模组每个弹丸是一个独立的 Entity 类（EntityChaoticOrb / EntityCrimsonOrb /
 * EntityDarkMatterOrb / EntityLunarFlare / EntityRageousMissile / EntityShinyEnergy /
 * EntitySoulEnergy / EntityThunderpealOrb / EntityBabylonWeaponSS），行为大同小异：
 * 直线飞行、命中造成伤害、部分带爆炸或药水效果。这里合并为一个实体 + 变体参数表，
 * 渲染共用一个 billboard 渲染器（按变体换颜色）。</p>
 */
public class RelicProjectileEntity extends ThrowableProjectile {

    public enum Variant {
        CHAOS(6.0F, 0, true, ParticleTypes.WITCH, 0x8A2BE2),
        CRIMSON(7.0F, 0, true, ParticleTypes.DAMAGE_INDICATOR, 0xDC143C),
        DARK_MATTER(8.0F, 2.5F, true, ParticleTypes.PORTAL, 0x2B0033),
        LUNAR_FLARE(9.0F, 1.5F, false, ParticleTypes.END_ROD, 0xE8E8FF),
        RAGEOUS(12.0F, 3.0F, true, ParticleTypes.FLAME, 0xFF4500),
        SHINY(4.0F, 0, false, ParticleTypes.HAPPY_VILLAGER, 0x7CFC00),
        SOUL(8.0F, 0, true, ParticleTypes.SOUL, 0x9FE2BF),
        THUNDER(10.0F, 0, true, ParticleTypes.ELECTRIC_SPARK, 0x7DF9FF),
        BABYLON(20.0F, 4.0F, false, ParticleTypes.GLOW, 0xFFD700);

        /** 直接命中伤害。 */
        public final float damage;
        /** 命中爆炸半径（0 = 不爆）。 */
        public final float explosion;
        /** 是否受重力。 */
        public final boolean gravity;
        public final net.minecraft.core.particles.SimpleParticleType trail;
        public final int color;

        Variant(float damage, float explosion, boolean gravity,
                net.minecraft.core.particles.SimpleParticleType trail, int color) {
            this.damage = damage;
            this.explosion = explosion;
            this.gravity = gravity;
            this.trail = trail;
            this.color = color;
        }
    }

    private static final EntityDataAccessor<String> VARIANT =
            SynchedEntityData.defineId(RelicProjectileEntity.class, EntityDataSerializers.STRING);

    public RelicProjectileEntity(EntityType<? extends RelicProjectileEntity> type, Level level) {
        super(type, level);
    }

    public RelicProjectileEntity(Level level, LivingEntity owner, Variant variant) {
        super(ModEntities.RELIC_PROJECTILE.get(), level);
        this.setOwner(owner);
        this.setPos(owner.getX(), owner.getEyeY() - 0.1, owner.getZ());
        this.entityData.set(VARIANT, variant.name());
    }

    public Variant variant() {
        try {
            return Variant.valueOf(this.entityData.get(VARIANT));
        } catch (IllegalArgumentException wrong) {
            return Variant.CHAOS;
        }
    }

    public void setVariant(Variant variant) {
        this.entityData.set(VARIANT, variant.name());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(VARIANT, Variant.CHAOS.name());
    }

    @Override
    protected double getDefaultGravity() {
        return this.variant().gravity ? 0.03 : 0.0;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level() instanceof ServerLevel server) {
            server.sendParticles(this.variant().trail, this.getX(), this.getY(), this.getZ(),
                    2, 0.05, 0.05, 0.05, 0.01);
        }
        if (this.tickCount > 200) {
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity target = result.getEntity();
        Variant variant = this.variant();
        DamageSource source = this.damageSources().magic();
        target.hurt(source, variant.damage);
        if (target instanceof LivingEntity living) {
            switch (variant) {
                case CRIMSON -> living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 1));
                case CHAOS -> living.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0));
                case SOUL -> living.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 0));
                case THUNDER -> {
                    if (this.level() instanceof ServerLevel server) {
                        var bolt = net.minecraft.world.entity.EntityType.LIGHTNING_BOLT
                                .create(server);
                        if (bolt != null) {
                            bolt.moveTo(target.getX(), target.getY(), target.getZ(), 0.0F, 0.0F);
                            server.addFreshEntity(bolt);
                        }
                    }
                }
                default -> {
                }
            }
        }
        this.discard();
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        Variant variant = this.variant();
        if (variant.explosion > 0 && this.level() instanceof ServerLevel server) {
            server.explode(this, this.getX(), this.getY(), this.getZ(),
                    variant.explosion, Level.ExplosionInteraction.NONE);
        }
        this.discard();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("Variant", this.entityData.get(VARIANT));
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Variant")) {
            this.entityData.set(VARIANT, tag.getString("Variant"));
        }
    }

}
