package com.skydeity.thaumicdyesexplor.registry;

import com.mojang.serialization.Codec;
import com.skydeity.thaumicdyesexplor.ThaumicDyesExplor;
import com.skydeity.thaumicdyesexplor.component.RunicUpgrades;
import com.skydeity.thaumicdyesexplor.component.StoredFood;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 本模组自有的数据组件。
 *
 * <p>1.21.1 用数据组件取代了 1.7.10 的 NBT：符文护甲的 {@code upgrade}/{@code upgrade2}/
 * {@code upgrade3} 三段升级合并成一个 {@link RunicUpgrades} 列表。</p>
 *
 * <p>这里刻意“先建好组件对象、再用 {@code Supplier} 注册”，这样代码里拿到的就是
 * {@code DataComponentType<RunicUpgrades>} 本身，可以直接传给
 * {@code ItemStack#get/#set}，不必到处写 {@code .get()}。</p>
 */
public final class ModDataComponents {

    public static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ThaumicDyesExplor.MOD_ID);

    /** 符文护甲升级槽（0~10，0 = 空）。 */
    public static final DataComponentType<RunicUpgrades> RUNIC_UPGRADES =
            DataComponentType.<RunicUpgrades>builder()
                    .persistent(RunicUpgrades.CODEC)
                    .networkSynchronized(RunicUpgrades.STREAM_CODEC)
                    .build();

    /** 营养护符囤着的口粮。 */
    public static final DataComponentType<StoredFood> STORED_FOOD =
            DataComponentType.<StoredFood>builder()
                    .persistent(StoredFood.CODEC)
                    .networkSynchronized(StoredFood.STREAM_CODEC)
                    .build();

    /**
     * 堡垒/游侠头盔的“脸谱”编号（对应源模组 NBT {@code mask}，0~2；未设置 = 无脸谱）。
     * 决定面具部件显示与堡垒甲套装减伤的 +5%。
     */
    public static final DataComponentType<Integer> ARMOR_MASK =
            DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build();

    /** 堡垒/铁堡垒头盔的护目镜开关（对应源模组 NBT {@code goggles}）。 */
    public static final DataComponentType<Boolean> ARMOR_GOGGLES =
            DataComponentType.<Boolean>builder()
                    .persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL)
                    .build();

    /** 永恒智慧之书存的经验点数。 */
    public static final DataComponentType<Integer> XP_STORED =
            DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build();

    static {
        COMPONENTS.register("runic_upgrades", () -> RUNIC_UPGRADES);
        COMPONENTS.register("stored_food", () -> STORED_FOOD);
        COMPONENTS.register("armor_mask", () -> ARMOR_MASK);
        COMPONENTS.register("armor_goggles", () -> ARMOR_GOGGLES);
        COMPONENTS.register("xp_stored", () -> XP_STORED);
    }

    private ModDataComponents() {
    }
}
