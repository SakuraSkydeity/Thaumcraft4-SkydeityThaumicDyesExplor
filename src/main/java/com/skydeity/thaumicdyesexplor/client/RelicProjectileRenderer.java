package com.skydeity.thaumicdyesexplor.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.skydeity.thaumicdyesexplor.entity.RelicProjectileEntity;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import org.joml.Matrix4f;

/**
 * 遗物弹丸渲染：一个始终面向玩家的彩色 billboard 方片。
 *
 * <p>九种弹丸共用这一张白噪点贴图（8×8，程序生成），颜色按变体在顶点色里给出 ——
 * 与源模组“每个弹丸一张粒子贴图”观感等价，省掉九个渲染器。</p>
 */
public class RelicProjectileRenderer extends EntityRenderer<RelicProjectileEntity> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("thaumicdyesexplor", "textures/entity/relic_orb.png");

    public RelicProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public Vec3 getRenderOffset(RelicProjectileEntity entity, float partialTick) {
        return Vec3.ZERO;
    }

    @Override
    public void render(RelicProjectileEntity entity, float yaw, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int light) {
        pose.pushPose();
        // 面向相机
        pose.mulPose(this.entityRenderDispatcher.cameraOrientation());
        pose.mulPose(new org.joml.Quaternionf().rotationY((float) Math.PI));
        float size = entity.variant() == RelicProjectileEntity.Variant.BABYLON ? 0.8F : 0.4F;
        int color = entity.variant().color;
        float r = (color >> 16 & 0xFF) / 255.0F;
        float g = (color >> 8 & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;

        Matrix4f matrix = pose.last().pose();
        VertexConsumer buffer = buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        vertex(buffer, matrix, -size, -size, 0, 1, r, g, b, light);
        vertex(buffer, matrix, size, -size, 1, 1, r, g, b, light);
        vertex(buffer, matrix, size, size, 1, 0, r, g, b, light);
        vertex(buffer, matrix, -size, size, 0, 0, r, g, b, light);
        pose.popPose();
        super.render(entity, yaw, partialTick, pose, buffers, light);
    }

    private static void vertex(VertexConsumer buffer, Matrix4f matrix,
                               float x, float y, int u, int v, float r, float g, float b, int light) {
        buffer.addVertex(matrix, x, y, 0)
                .setColor(r, g, b, 1.0F)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(0.0F, 0.0F, 1.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(RelicProjectileEntity entity) {
        return TEXTURE;
    }
}
