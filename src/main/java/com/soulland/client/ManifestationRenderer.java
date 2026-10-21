package com.soulland.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.soulland.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * Manifestasi martial soul: bentuk visual soul yang muncul saat skill aktif.
 *
 * <p>Manifestasi digambar memakai item model resmi {@code soulland:manifestation_*},
 * jadi JSON model dan texture yang terdaftar benar-benar yang tampil di dunia.
 */
public final class ManifestationRenderer {
    private ManifestationRenderer() {}

    /** Ketinggian manifestasi di atas kaki pemain (blok). */
    private static final float HEIGHT = 1.9f;
    /** Jarak manifestasi di belakang pemain (blok). */
    private static final float BEHIND = 1.2f;

    /** Pengali ukuran manifestasi selama True Body aktif. */
    private static final float TRUE_BODY_SCALE = 1.7f;

    /** Gambar manifestasi seorang pemain kalau sedang aktif. */
    public static void render(Player player, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        long gameTime = mc.level.getGameTime();
        boolean trueBody = ClientCombatData.trueBodyActive(player.getId(), gameTime);
        String soulId = "";
        float scale = 0f;

        ClientManifestations.Manifestation manifestation = ClientManifestations.get(player.getId());
        if (manifestation != null) {
            long elapsed = gameTime - manifestation.startTick();
            if (elapsed >= 0 && elapsed <= ClientManifestations.DURATION_TICKS) {
                soulId = manifestation.soulId();
                scale = scaleAt((float) elapsed / ClientManifestations.DURATION_TICKS);
            }
        }
        if (soulId.isEmpty()) {
            if (!trueBody) return;
            ClientCombatData.State state = ClientCombatData.stateOf(player.getId());
            if (state == null || state.soulId().isEmpty()) return;
            soulId = state.soulId();
            scale = TRUE_BODY_SCALE;
        }
        if (trueBody) scale *= TRUE_BODY_SCALE;

        ItemStack stack = ModItems.manifestationStack(soulId);
        if (stack.isEmpty()) return;

        Vec3 look = player.getLookAngle();
        double horizontal = Math.sqrt(look.x * look.x + look.z * look.z);
        float x = 0f;
        float z = 0f;
        if (horizontal > 1.0E-4) {
            x = (float) (-look.x / horizontal * BEHIND);
            z = (float) (-look.z / horizontal * BEHIND);
        }

        poseStack.pushPose();
        poseStack.translate(x, HEIGHT, z);
        faceCamera(poseStack, player, x, HEIGHT, z);
        poseStack.scale(scale, scale, scale);
        mc.getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, 0, 0, poseStack, buffer, mc.level, packedLight);
        poseStack.popPose();
    }

    /**
     * Skala manifestasi sepanjang animasi: materialize (0.15 ke 1.25), tahan,
     * lalu menyusut (1.25 ke 0.35) sampai habis.
     */
    private static float scaleAt(float progress) {
        if (progress < 0.35f) {
            float t = progress / 0.35f;
            float eased = 1f - (1f - t) * (1f - t);
            return 0.15f + (1.25f - 0.15f) * eased;
        }
        if (progress < 0.7f) return 1.25f;
        float t = (progress - 0.7f) / 0.3f;
        return 1.25f + (0.35f - 1.25f) * t;
    }

    /** Putar quad agar normalnya menunjuk ke kamera. */
    private static void faceCamera(PoseStack poseStack, Player player, float x, float y, float z) {
        Minecraft mc = Minecraft.getInstance();
        Vec3 camera = mc.gameRenderer.getMainCamera().getPosition();
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
}
