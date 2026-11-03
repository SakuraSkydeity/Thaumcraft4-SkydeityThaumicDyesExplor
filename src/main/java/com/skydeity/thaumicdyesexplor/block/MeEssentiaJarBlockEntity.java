package com.skydeity.thaumicdyesexplor.block;

import java.util.EnumMap;
import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skydeity.thaumicdyesexplor.registry.ModBlocks;

import dev.tc4port.thaumcraft.api.aspect.VisChannel;
import dev.tc4port.thaumcraft.api.wand.WandApi;
import dev.tc4port.thaumcraft.api.wand.WandCarrierItem;
import dev.tc4port.thaumcraft.api.wand.WandView;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 中洲精髓罐的方块实体：按六个元要素通道存 centivis。
 *
 * <p>容量 = 每通道 {@code capacityCentivis}，由方块设定（压缩罐 ×2、双重压缩 ×4、群星罐 ×8
 * 的“原版罐基准 8 vis”）。与法杖交互：普通右键把罐里的 vis 充进法杖，潜行右键把法杖的
 * vis 压进罐子 —— 与 TC4 的罐子 / 法杖交互方向一致。不实现小瓶与节点管线
 * （前置的 EssentiaApi 交互留给后续轮次）。</p>
 */
public class MeEssentiaJarBlockEntity extends BlockEntity {

    /** 原版奥秘罐的每要素容量：8 vis = 800 centivis。 */
    public static final int BASE_CENTIVIS = 800;

    private int capacityCentivis = -1;
    private final Map<VisChannel, Integer> vis = new EnumMap<>(VisChannel.class);

    public MeEssentiaJarBlockEntity(net.minecraft.core.BlockPos pos,
                                    net.minecraft.world.level.block.state.BlockState state) {
        super(ModBlocks.ME_ESSENTIA_JAR.get(), pos, state);
    }

    /** 每通道容量：按所在方块的倍率算（压缩 ×2、双重压缩 ×4、群星罐 ×8）。 */
    private int capacity() {
        if (this.capacityCentivis < 0) {
            this.capacityCentivis = capacityFor(this.getBlockState());
        }
        return this.capacityCentivis;
    }

    /** 方块上的容量倍率（写进 BlockEntity 类型工厂无法携带，直接从 BlockState 的方块类型判断）。 */
    public static int capacityFor(BlockState state) {
        if (state.getBlock() instanceof MeEssentiaJarBlock jar) {
            return BASE_CENTIVIS * jar.capacityMultiplier();
        }
        return BASE_CENTIVIS;
    }

    // ============================================================================================
    // 与法杖的交互（只在服务端调用）
    // ============================================================================================

    /** 法杖 → 罐（潜行右键）。 */
    public void pullFromWand(ServerPlayer player, ItemStack wand) {
        WandView view = WandApi.view(wand).orElse(null);
        if (view == null) {
            return;
        }
        for (VisChannel channel : VisChannel.values()) {
            int space = this.capacity() - this.vis.getOrDefault(channel, 0);
            int available = view.visCentivis().getOrDefault(channel, 0);
            int amount = Math.min(space, available);
            if (amount <= 0) {
                continue;
            }
            var result = WandApi.extract(player, wand,
                    dev.tc4port.thaumcraft.api.aspect.VisCost.ofCentivis(channel, amount),
                    dev.tc4port.thaumcraft.api.aspect.VisAction.EXECUTE);
            int moved = result.transferred(channel);
            if (moved > 0) {
                this.vis.merge(channel, moved, Integer::sum);
                this.setChanged();
            }
        }
    }

    /** 罐 → 法杖（普通右键）。 */
    public void pushToWand(ServerPlayer player, ItemStack wand) {
        WandView view = WandApi.view(wand).orElse(null);
        if (view == null) {
            return;
        }
        for (VisChannel channel : VisChannel.values()) {
            int stored = this.vis.getOrDefault(channel, 0);
            if (stored <= 0) {
                continue;
            }
            var result = WandApi.insert(player, wand,
                    dev.tc4port.thaumcraft.api.aspect.VisCost.ofCentivis(channel, stored),
                    dev.tc4port.thaumcraft.api.aspect.VisAction.EXECUTE);
            int moved = result.transferred(channel);
            if (moved > 0) {
                int left = stored - moved;
                if (left > 0) {
                    this.vis.put(channel, left);
                } else {
                    this.vis.remove(channel);
                }
                this.setChanged();
            }
        }
    }

    // ============================================================================================
    // NBT
    // ============================================================================================

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        CompoundTag visTag = new CompoundTag();
        for (Map.Entry<VisChannel, Integer> entry : this.vis.entrySet()) {
            visTag.putInt(String.valueOf(entry.getKey().index()), entry.getValue());
        }
        tag.put("Vis", visTag);
        tag.putInt("Capacity", this.capacity());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.vis.clear();
        CompoundTag visTag = tag.getCompound("Vis");
        for (String key : visTag.getAllKeys()) {
            VisChannel.fromIndex(Integer.parseInt(key))
                    .ifPresent(channel -> this.vis.put(channel, visTag.getInt(key)));
        }
    }
}
