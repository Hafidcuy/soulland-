package com.soulland.client.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.OverlayTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

/** Pembuat quad untuk render kustom. Semua tekstur dibuat putih supaya bisa diwarnai lewat vertex color. */
public final class Quads {

    private Quads() {}

    public static ResourceLocation texture(String path) {
        return new ResourceLocation("soulland", "textures/" + path + ".png");
    }

    /** Quad berdiri tegak menghadap kamera. */
    public static void billboard(VertexConsumer vc, Matrix4f matrix, float width, float height,
                                 float r, float g, float b, float a, int light) {
        float hw = width / 2f;
        float hh = height / 2f;
        vertex(vc, matrix, -hw, hh, 0f, r, g, b, a, 0f, 0f, light);
        vertex(vc, matrix, hw, hh, 0f, r, g, b, a, 1f, 0f, light);
        vertex(vc, matrix, hw, -hh, 0f, r, g, b, a, 1f, 1f, light);
        vertex(vc, matrix, -hw, -hh, 0f, r, g, b, a, 0f, 1f, light);

        vertex(vc, matrix, -hw, -hh, 0f, r, g, b, a, 0f, 1f, light);
        vertex(vc, matrix, hw, -hh, 0f, r, g, b, a, 1f, 1f, light);
        vertex(vc, matrix, hw, hh, 0f, r, g, b, a, 1f, 0f, light);
        vertex(vc, matrix, -hw, hh, 0f, r, g, b, a, 0f, 0f, light);
    }

    /** Quad datar di bidang XY (dipakai untuk cincin di tanah setelah diputar). */
    public static void flat(VertexConsumer vc, Matrix4f matrix, float width, float depth,
                             float r, float g, float b, float a, int light) {
        float hw = width / 2f;
        float hd = depth / 2f;
        vertex(vc, matrix, -hw, 0f, -hd, r, g, b, a, 0f, 1f, light);
        vertex(vc, matrix, hw, 0f, -hd, r, g, b, a, 1f, 1f, light);
        vertex(vc, matrix, hw, 0f, hd, r, g, b, a, 1f, 0f, light);
        vertex(vc, matrix, -hw, 0f, hd, r, g, b, a, 0f, 0f, light);

        vertex(vc, matrix, -hw, 0f, hd, r, g, b, a, 0f, 0f, light);
        vertex(vc, matrix, hw, 0f, hd, r, g, b, a, 1f, 0f, light);
        vertex(vc, matrix, hw, 0f, -hd, r, g, b, a, 1f, 1f, light);
        vertex(vc, matrix, -hw, 0f, -hd, r, g, b, a, 0f, 1f, light);
    }

    /** Pita energi dari titik asal ke ujung, melebar di sisi tegak lurus arah. */
    public static void ribbon(VertexConsumer vc, Matrix4f matrix,
                              float endX, float endY, float endZ,
                              float sideX, float sideY, float sideZ,
                              float r, float g, float b, float a, int light) {
        float ax = -sideX, ay = -sideY, az = -sideZ;
        float bx = sideX, by = sideY, bz = sideZ;
        float cx = endX + sideX, cy = endY + sideY, cz = endZ + sideZ;
        float dx = endX - sideX, dy = endY - sideY, dz = endZ - sideZ;

        vertex(vc, matrix, ax, ay, az, r, g, b, a, 0f, 0f, light);
        vertex(vc, matrix, bx, by, bz, r, g, b, a, 1f, 0f, light);
        vertex(vc, matrix, cx, cy, cz, r, g, b, a, 1f, 1f, light);
        vertex(vc, matrix, dx, dy, dz, r, g, b, a, 0f, 1f, light);

        vertex(vc, matrix, dx, dy, dz, r, g, b, a, 0f, 1f, light);
        vertex(vc, matrix, cx, cy, cz, r, g, b, a, 1f, 1f, light);
        vertex(vc, matrix, bx, by, bz, r, g, b, a, 1f, 0f, light);
        vertex(vc, matrix, ax, ay, az, r, g, b, a, 0f, 0f, light);
    }

    private static void vertex(VertexConsumer vc, Matrix4f matrix, float x, float y, float z,
                               float r, float g, float b, float a, float u, float v, int light) {
        vc.vertex(matrix, x, y, z)
                .color(r, g, b, a)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .light(light)
                .normal(0f, 1f, 0f)
                .endVertex();
    }
}
