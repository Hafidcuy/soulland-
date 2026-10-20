package com.soulland.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.soulland.entity.MartialSoulEntity;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/**
 * Menggambar martial soul yang melayang di belakang pemiliknya.
 *
 * <p>Potret soul digambar sebagai billboard semi transparan, dikelilingi cincin energi
 * yang berputar. Warna came dari warna utama soul, jadi tiap soul punya kesan sendiri.
 */
public class MartialSoulEntityRenderer extends EntityRenderer<MartialSoulEntity> {

    private static final ResourceLocation GLOW = Quads.texture("fx/glow");
    private static final ResourceLocation RING = Quads.texture("fx/ring");

    public MartialSoulEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public net.minecraft.resources.ResourceLocation getTextureLocation(MartialSoulEntity entity) {
        return GLOW;
    }

    @Override
    public void render(MartialSoulEntity entity, float yaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        String soulId = entity.soulId();
        if (soulId == null || soulId.isEmpty()) {
            return;
        }

        int tint = entity.tint();
        float r = ((tint >> 16) & 0xFF) / 255f;
        float g = ((tint >> 8) & 0xFF) / 255f;
        float b = (tint & 0xFF) / 255f;

        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        float bob = (float) Math.sin((entity.tickCount + partialTick) * 0.08) * 0.08f;
        float echo = entity.echoProgress();

        poseStack.pushPose();
        poseStack.translate(0.0, bob, 0.0);
        poseStack.mulPose(camera.rotation());

        float size = 1.5f + echo * 0.35f;

        VertexConsumer glow = buffer.getBuffer(RenderType.entityTranslucent(GLOW));
        Quads.billboard(glow, poseStack.last().pose(), size * 1.9f, size * 1.9f,
                r, g, b, 0.22f, packedLight);

        ResourceLocation portrait = new ResourceLocation("soulland", "textures/soul/" + soulId + ".png");
        VertexConsumer pc = buffer.getBuffer(RenderType.entityTranslucent(portrait));
        Quads.billboard(pc, poseStack.last().pose(), size, size, 1f, 1f, 1f, 0.92f, packedLight);

        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.0, 0.05, 0.0);
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(-90f));
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees((entity.tickCount + partialTick) * 2.5f));
        VertexConsumer ring = buffer.getBuffer(RenderType.entityTranslucent(RING));
        Quads.flat(ring, poseStack.last().pose(), 1.6f, 1.6f, r, g, b, 0.45f, packedLight);
        poseStack.popPose();
    }
}
