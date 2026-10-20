package com.soulland.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.soulland.entity.SoulVfxEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

/**
 * Menggambar bentuk energi yang muncul saat skill besar dilepas.
 *
 * <p>Semua tekstur dibuat putih supaya warna skill bisa dipakai lewat vertex color:
 * cincin melebar, berkas lurus, pilar vertikal, dan bola yang meledak.
 */
public class SoulVfxEntityRenderer extends EntityRenderer<SoulVfxEntity> {

    private static final ResourceLocation RING = Quads.texture("fx/ring");
    private static final ResourceLocation BEAM = Quads.texture("fx/beam");
    private static final ResourceLocation GLOW = Quads.texture("fx/glow");
    private static final ResourceLocation SPARK = Quads.texture("fx/spark");

    public SoulVfxEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(SoulVfxEntity entity, float yaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        float progress = entity.progress();
        float fade = 1f - progress;

        int color = entity.color();
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        switch (entity.mode()) {
            case WAVE -> renderWave(entity, poseStack, buffer, packedLight, r, g, b, fade, progress);
            case BEAM -> renderBeam(entity, poseStack, buffer, packedLight, r, g, b, fade);
            case COLUMN -> renderColumn(entity, poseStack, buffer, packedLight, r, g, b, fade);
            case ORB -> renderOrb(entity, poseStack, buffer, packedLight, r, g, b, fade, progress);
        }
    }

    private void renderWave(SoulVfxEntity entity, PoseStack poseStack, MultiBufferSource buffer, int light,
                            float r, float g, float b, float fade, float progress) {
        float size = entity.size() * (0.35f + progress * 1.35f);
        poseStack.pushPose();
        poseStack.translate(0.0, 0.08, 0.0);
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(-90f));
        VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(RING));
        Quads.flat(vc, poseStack.last().pose(), size, size, r, g, b, fade * 0.9f, light);
        poseStack.popPose();
    }

    private void renderBeam(SoulVfxEntity entity, PoseStack poseStack, MultiBufferSource buffer, int light,
                            float r, float g, float b, float fade) {
        Vec3 dir = new Vec3(entity.dirX(), entity.dirY(), entity.dirZ());
        if (dir.lengthSqr() < 1.0E-6) {
            dir = new Vec3(0, 0, 1);
        }
        dir = dir.normalize();

        Vec3 reference = Math.abs(dir.y) > 0.9 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
        Vec3 side = dir.cross(reference).normalize().scale(0.8f);
        float length = entity.size() * 1.6f;

        VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(BEAM));
        Quads.ribbon(vc, poseStack.last().pose(),
                (float) (dir.x * length), (float) (dir.y * length), (float) (dir.z * length),
                (float) side.x, (float) side.y, (float) side.z,
                r, g, b, fade * 0.85f, light);
    }

    private void renderColumn(SoulVfxEntity entity, PoseStack poseStack, MultiBufferSource buffer, int light,
                              float r, float g, float b, float fade) {
        float height = entity.size() * 1.4f;
        poseStack.pushPose();
        poseStack.translate(0.0, -height / 2f, 0.0);
        VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(GLOW));
        Quads.billboard(vc, poseStack.last().pose(), entity.size() * 0.8f, height,
                r, g, b, fade * 0.7f, light);
        poseStack.popPose();
    }

    private void renderOrb(SoulVfxEntity entity, PoseStack poseStack, MultiBufferSource buffer, int light,
                           float r, float g, float b, float fade, float progress) {
        float size = entity.size() * (0.9f + progress * 0.8f);
        VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(GLOW));
        Quads.billboard(vc, poseStack.last().pose(), size, size, r, g, b, fade * 0.95f, light);

        VertexConsumer spark = buffer.getBuffer(RenderType.entityTranslucent(SPARK));
        Quads.billboard(spark, poseStack.last().pose(), size * 1.7f, size * 1.7f,
                r, g, b, fade * 0.45f, light);
    }
}
