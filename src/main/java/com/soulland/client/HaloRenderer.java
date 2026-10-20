package com.soulland.client;

import com.soulland.SoulLandMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * Halo spirit ring: cincin-cincin yang mengelilingi pinggang pemain.
 *
 * <p>Setiap spirit ring digambar sebagai satu quad datar menghadap kamera,
 * memakai sprite putih yang sama dan diwarnai menurut umur ring. Warna umur
 * sengaja mengikuti warna item spirit ring supaya keduanya konsisten.
 */
public final class HaloRenderer {
    private HaloRenderer() {}

    private static final ResourceLocation RING_TEXTURE = new ResourceLocation(SoulLandMod.MODID, "entity/spirit_ring");

    /** Ketinggian pusat halo di atas kaki pemain (blok). */
    private static final float WAIST_HEIGHT = 1.0f;
    /** Jarak cincin dari sumbu pinggang pemain (blok). */
    private static final float ORBIT_RADIUS = 0.95f;
    /** Radius luar sprite (64px dengan radius 30px) dibanding setengah ukuran quad. */
    private static final float SPRITE_RADIUS_RATIO = 30.0f / 32.0f;
    /** Amplitudo naik-turun tiap cincin (blok). */
    private static final float BOB_AMPLITUDE = 0.06f;
    /** Lama satu putaran tiap cincin (detik), tiap cincin punya kecepatan berbeda. */
    private static final float[] SPIN_SECONDS = {40f, 46f, 52f, 58f, 64f, 70f, 76f, 82f, 88f};
    /** Kecepatan seluruh susunan cincin berputar mengelilingi pinggau (radian per tick). */
    private static final double ORBIT_SPEED = 0.008;

    /** Gambar halo milik seorang pemain. Aman dipanggil untuk pemain tanpa ring. */
    public static void render(Player player, PoseStack poseStack, MultiBufferSource buffer, int packedLight, float partialTick) {
        if (player.isInvisible() || player.isSpectator()) return;
        int[] ages = ringAges(player);
        if (ages.length == 0) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        int count = Math.min(ages.length, SPIN_SECONDS.length);
        double time = mc.level.getGameTime() + partialTick;
        float yaw = player.getYRot();

        for (int i = 0; i < count; i++) {
            int age = ages[i];
            double angle = Math.toRadians(yaw) + i * (2.0 * Math.PI / count) + time * ORBIT_SPEED;
            float x = (float) (Math.cos(angle) * ORBIT_RADIUS);
            float z = (float) (Math.sin(angle) * ORBIT_RADIUS);
            float bob = (float) Math.sin(time * 0.06 + i * 1.1) * BOB_AMPLITUDE;
            float spin = (float) ((time * 18.0 / SPIN_SECONDS[i] + i * 37.0) % 360.0);

            poseStack.pushPose();
            poseStack.translate(x, WAIST_HEIGHT + bob, z);
            faceCamera(poseStack, player, x, WAIST_HEIGHT + bob, z);
            poseStack.mulPose(Axis.ZP.rotationDegrees(spin));
            drawRing(poseStack, buffer, visualRadius(age) / SPRITE_RADIUS_RATIO, colorFor(age), packedLight);
            poseStack.popPose();
        }
    }

    /** Umur ring pemain: data sendiri untuk pemain lokal, data sinkron untuk pemain lain. */
    private static int[] ringAges(Player player) {
        Minecraft mc = Minecraft.getInstance();
        if (player == mc.player) return ClientCultivationData.ringAges;
        int[] ages = ClientHaloData.get(player.getId());
        return ages == null ? new int[0] : ages;
    }

    /** Radius visual cincin (blok), makin tua makin besar. */
    private static float visualRadius(int age) {
        if (age >= 100_000) return 0.46f;
        if (age >= 10_000) return 0.42f;
        if (age >= 1_000) return 0.38f;
        return 0.34f;
    }

    /** Warna cincin mengikuti warna item spirit ring dengan umur yang sama. */
    private static int colorFor(int age) {
        if (age >= 100_000) return 0xC8D2453C;
        if (age >= 10_000) return 0xC8B36BE0;
        if (age >= 1_000) return 0xC8F0C040;
        return 0xC8E4E8EA;
    }

    /** Putar quad agar normalnya menunjuk ke kamera. */
    private static void faceCamera(PoseStack poseStack, Player player, float x, float y, float z) {
        Minecraft mc = Minecraft.getInstance();
        net.minecraft.world.phys.Vec3 camera = mc.gameRenderer.getMainCamera().getPosition();
        double dx = camera.x - (player.getX() + x);
        double dy = camera.y - (player.getY() + y);
        double dz = camera.z - (player.getZ() + z);
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length < 1.0E-4) return;
        float pitch = (float) Math.toDegrees(Math.asin(-dy / length));
        float yaw = (float) Math.toDegrees(Math.atan2(dx, dz));
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
    }

    /** Gambar satu quad bertekstur dengan warna dan cahaya tertentu. */
    private static void drawRing(PoseStack poseStack, MultiBufferSource buffer, float halfSize, int color, int packedLight) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(RING_TEXTURE));
        Matrix4f pose = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();
        int alpha = (color >>> 24) & 0xFF;
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;

        consumer.vertex(pose, -halfSize, -halfSize, 0).color(red, green, blue, alpha)
                .uv(0f, 1f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
                .normal(normal, 0f, 0f, 1f).endVertex();
        consumer.vertex(pose, halfSize, -halfSize, 0).color(red, green, blue, alpha)
                .uv(1f, 1f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
                .normal(normal, 0f, 0f, 1f).endVertex();
        consumer.vertex(pose, halfSize, halfSize, 0).color(red, green, blue, alpha)
                .uv(1f, 0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
                .normal(normal, 0f, 0f, 1f).endVertex();
        consumer.vertex(pose, -halfSize, halfSize, 0).color(red, green, blue, alpha)
                .uv(0f, 0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
                .normal(normal, 0f, 0f, 1f).endVertex();
    }
}
