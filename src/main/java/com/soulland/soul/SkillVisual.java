package com.soulland.soul;

/**
 * Deskriptor visual satu skill.
 *
 * <p>Setiap skill punya signature sendiri: bentuk, warna, ukuran, jumlah partikel, suara,
 * guncangan kamera, dan warna kilat layar. Inilah yang membuat 60 skill tidak terlihat sama.
 */
public record SkillVisual(
        FxShape shape,
        int color,
        int color2,
        float size,
        int count,
        String sound,
        float shake,
        int flash
) {
    /** Kilat layar dengan alpha 0 berarti tidak ada kilat. */
    public static final int NO_FLASH = 0;

    public SkillVisual {
        if (count <= 0) count = 24;
        if (size <= 0f) size = 4f;
        if (color2 == 0) color2 = 0xFFFFFF;
    }

    public boolean hasFlash() {
        return flash != NO_FLASH;
    }

    public int flashAlpha() {
        return (flash >>> 24) & 0xFF;
    }

    public int flashRgb() {
        return flash & 0xFFFFFF;
    }
}
