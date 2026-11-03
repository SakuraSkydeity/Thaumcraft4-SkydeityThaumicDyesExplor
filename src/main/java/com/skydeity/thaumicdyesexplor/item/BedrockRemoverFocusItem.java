package com.skydeity.thaumicdyesexplor.item;

import java.util.List;
import java.util.Map;

import com.skydeity.thaumicdyesexplor.vis.WandVis;

import dev.tc4port.thaumcraft.api.aspect.VisChannel;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

/**
 * 法杖核心：基岩粉碎者（源模组 {@code ItemBedrockRemoverFocus}）。
 *
 * <p>源模组是「装在法杖上的焦点」，右键基岩时先 <b>扣 1000 气 + 1000 土 + 500 秩序 + 2000 混沌
 * （centivis，即 10/10/5/20 vis）</b>，成功才把基岩挖掉，且世界最底层那一层基岩永远挖不动
 * （{@code y != 0}）。</p>
 *
 * <p><b>移植做法</b>：1.21.1 的法杖焦点是一整套「profile + pipeline + Java handler」的体系，
 * 前置里每枚焦点都要各自注册行为模块；这里改成<b>直接手持使用</b>：手里拿着它对着基岩右键，
 * 效果与源模组一致（扣同一份 vis、同样跳过最底层），并且不依赖焦点可插拔系统。
 * 这样无论是拿在手上还是装在焦点袋里，行为都稳定可预期。</p>
 */
public class BedrockRemoverFocusItem extends Item {

    /** 与源模组完全一致的施法成本（centivis）。 */
    private static final Map<VisChannel, Integer> COST = Map.of(
            VisChannel.AER, 1000,
            VisChannel.TERRA, 1000,
            VisChannel.ORDO, 500,
            VisChannel.PERDITIO, 2000);

    public BedrockRemoverFocusItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (!level.getBlockState(pos).is(Blocks.BEDROCK)) {
            return InteractionResult.PASS;
        }
        if (!(context.getPlayer() instanceof ServerPlayer player)) {
            return InteractionResult.SUCCESS;
        }
        // 源模组判断 y != 0；1.21.1 的维度最低层可能是负数，这里按「最低可建造层」算
        if (pos.getY() <= level.getMinBuildHeight()) {
            player.displayClientMessage(
                    Component.translatable("msg.thaumicdyesexplor.bedrock_remover.too_low"), true);
            return InteractionResult.FAIL;
        }
        if (!WandVis.pay(player, COST)) {
            return InteractionResult.FAIL;
        }
        level.destroyBlock(pos, false);
        level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.PLAYERS, 1.0F, 0.6F);
        level.playSound(null, pos, SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 0.25F, 1.6F);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.thaumicdyesexplor.usage.me_focus_bedrock_remover")
                .withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("tooltip.thaumicdyesexplor.usage.me_focus_bedrock_remover.2")
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
