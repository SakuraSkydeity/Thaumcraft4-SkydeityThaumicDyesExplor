package com.skydeity.thaumicdyesexplor.compat;

import com.skydeity.thaumicdyesexplor.registry.ModBlocks;

import dev.tc4port.thaumcraft.api.essentia.EssentiaCapabilities;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/**
 * 把本模组的方块挂到前置的精髓能力上。
 *
 * <p>前置 20753 公开了 {@link EssentiaCapabilities#TRANSPORT}（管道、蒸馏器、傀儡都用这个
 * 找目标），所以湮灭罐与灵魂坩埚不需要自己实现管道协议，注册能力即可被网络识别。</p>
 *
 * <p>由 {@code ThaumicDyesExplor} 构造函数显式挂到 mod 事件总线上（与
 * {@code ModCurios::registerCapabilities} 一致），不依赖注解自动分总线。</p>
 */
public final class TxCapabilities {

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(EssentiaCapabilities.TRANSPORT, ModBlocks.OBLIVION_JAR_BE.get(),
                (blockEntity, side) -> blockEntity);
        event.registerBlockEntity(EssentiaCapabilities.TRANSPORT, ModBlocks.CRUCIBLE_OF_SOULS_BE.get(),
                (blockEntity, side) -> blockEntity);
    }

    private TxCapabilities() {
    }
}
