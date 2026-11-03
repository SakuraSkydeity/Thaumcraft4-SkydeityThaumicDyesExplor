package com.skydeity.thaumicdyesexplor.block;

import com.skydeity.thaumicdyesexplor.registry.ModBlocks;

import dev.tc4port.thaumcraft.api.aspect.AspectId;
import dev.tc4port.thaumcraft.api.essentia.EssentiaTransferMode;
import dev.tc4port.thaumcraft.api.essentia.EssentiaTransport;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * 灵魂坩埚的方块实体（源模组 {@code TileEntityCrucibleSouls}）。
 *
 * <p>源模组：把 8 格内的生物「吸」过来，每 10 tick 抽 1 点血，生物死时按它的要素表把要素
 * 收进坩埚，装不下的部分溢出来变成咒波。</p>
 *
 * <p>1.21.1 没有逐生物的要素表（前置的 {@code entity_balance} 是内部数据），所以这里
 * 固定按生物最大生命折算两种「死亡系」要素：{@code mortuus} 与 {@code spiritus}。
 * 缓冲上限 32，对外通过 {@link EssentiaTransport} 输出，可接管道 / 罐子。</p>
 */
public class CrucibleOfSoulsBlockEntity extends BlockEntity implements EssentiaTransport {

    private static final AspectId MORTUUS = AspectId.parse("mortuus");
    private static final AspectId SPIRITUS = AspectId.parse("spiritus");

    private static final int CAPACITY = 32;
    /** 每个生物被抽血的间隔（tick）：源模组 10。 */
    private static final int DRAIN_INTERVAL = 10;
    /** 抽取半径（格）。 */
    private static final double RANGE = 6.0D;

    private int mortuus;
    private int spiritus;

    public CrucibleOfSoulsBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.CRUCIBLE_OF_SOULS_BE.get(), pos, state);
    }

    // ============================================================================================
    // 每 tick：抽血
    // ============================================================================================
    public static void tick(Level level, BlockPos pos, BlockState state, CrucibleOfSoulsBlockEntity crucible) {
        if (level.isClientSide) {
            return;
        }
        if (level.getGameTime() % DRAIN_INTERVAL != 0) {
            return;
        }
        AABB box = new AABB(pos).inflate(RANGE);
        // 每 10 tick 只抽一个目标，和源模组「一根管子慢慢吸」的手感一致
        LivingEntity target = level.getEntitiesOfClass(LivingEntity.class, box).stream()
                .filter(entity -> !(entity instanceof Player))
                .filter(LivingEntity::isAlive)
                .filter(entity -> !entity.isSpectator())
                .findFirst()
                .orElse(null);
        if (target == null) {
            return;
        }
        if (target.hurt(level.damageSources().magic(), 1.0F) && target.isDeadOrDying()) {
            int health = Math.max(1, (int) target.getMaxHealth());
            crucible.mortuus = Math.min(CAPACITY, crucible.mortuus + Math.max(1, health / 4));
            crucible.spiritus = Math.min(CAPACITY, crucible.spiritus + Math.max(1, health / 6));
            crucible.setChanged();
        }
    }

    // ============================================================================================
    // EssentiaTransport（输出）
    // ============================================================================================
    @Override
    public boolean isConnectable(Direction direction) {
        return true;
    }

    @Override
    public boolean canInputFrom(Direction direction) {
        return false;
    }

    @Override
    public boolean canOutputTo(Direction direction) {
        return true;
    }

    @Override
    public AspectId suctionType(Direction direction) {
        return this.mortuus > 0 ? MORTUUS : this.spiritus > 0 ? SPIRITUS : null;
    }

    /**
     * 吸力恒为 0。
     *
     * <p>坩埚是<b>源</b>（只往外给），一旦报出正吸力，管道会把它当成「想要要素的容器」，
     * 反而把整条线的吸力拉高、别处的要素不再往下游走。源方块统一报 0。</p>
     */
    @Override
    public int suctionAmount(Direction direction) {
        return 0;
    }

    /** 管道先问「这个面还能抽哪种」，两种要素同时有的话优先死亡要素。 */
    @Override
    public AspectId extractableAspect(Direction direction) {
        return this.suctionType(direction);
    }

    /** 按种类报可抽取量（默认实现会把两种要素的数量加起来，导致管道抽超）。 */
    @Override
    public int availableEssentia(AspectId aspect, Direction direction) {
        if (aspect.equals(MORTUUS)) {
            return this.mortuus;
        }
        if (aspect.equals(SPIRITUS)) {
            return this.spiritus;
        }
        return 0;
    }

    @Override
    public int takeEssentia(AspectId aspect, int amount, Direction direction, EssentiaTransferMode mode) {
        int taken = 0;
        if (aspect.equals(MORTUUS)) {
            taken = Math.min(amount, this.mortuus);
            if (mode.executes() && taken > 0) {
                this.mortuus -= taken;
                this.setChanged();
            }
        } else if (aspect.equals(SPIRITUS)) {
            taken = Math.min(amount, this.spiritus);
            if (mode.executes() && taken > 0) {
                this.spiritus -= taken;
                this.setChanged();
            }
        }
        return taken;
    }

    @Override
    public int addEssentia(AspectId aspect, int amount, Direction direction, EssentiaTransferMode mode) {
        return 0; // 只出不进
    }

    @Override
    public AspectId essentiaType(Direction direction) {
        return suctionType(direction);
    }

    @Override
    public int essentiaAmount(Direction direction) {
        AspectId type = this.suctionType(direction);
        if (type == null) {
            return 0;
        }
        return type.equals(MORTUUS) ? this.mortuus : this.spiritus;
    }

    @Override
    public int minimumSuction() {
        return 0;
    }

    @Override
    public boolean renderExtendedTube() {
        return false;
    }

    // ============================================================================================
    // NBT
    // ============================================================================================
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Mortuus", this.mortuus);
        tag.putInt("Spiritus", this.spiritus);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.mortuus = tag.getInt("Mortuus");
        this.spiritus = tag.getInt("Spiritus");
    }
}
