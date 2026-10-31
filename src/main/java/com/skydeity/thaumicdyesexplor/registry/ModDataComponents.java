package com.skydeity.thaumicdyesexplor.registry;

import com.skydeity.thaumicdyesexplor.ThaumicDyesExplor;
import com.skydeity.thaumicdyesexplor.component.RunicUpgrades;
import com.skydeity.thaumicdyesexplor.component.StoredFood;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
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

    static {
        COMPONENTS.register("runic_upgrades", () -> RUNIC_UPGRADES);
        COMPONENTS.register("stored_food", () -> STORED_FOOD);
    }

    private ModDataComponents() {
    }
}
