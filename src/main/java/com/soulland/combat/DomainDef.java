package com.soulland.combat;

import com.soulland.soul.Element;

/**
 * Definisi Domain milik satu martial soul.
 *
 * <p>Domain adalah wilayah yang berdiri di dalamnya. Saat aktif, domain
 * memberi buff kepada sekutu, memberi debuff kepada musuh, menahan damage
 * per detik, dan menguras spirit power pemilik setiap detik.
 *
 * <p>Jari-jari wilayah bukan field di sini karena nilainya sama untuk semua
 * domain dan ditetapkan di {@link DomainRules}.
 */
public record DomainDef(
        String soulId,
        String nameEn,
        String nameId,
        Element element,
        int color,
        BuffProfile allyBuff,
        BuffProfile enemyDebuff,
        int drainPerSecond,
        float damagePerSecond
) {

    /** Pengurasan spirit power per detik, minimal satu titik biar domain bisa hidup. */
    public int drainPerSecond() {
        return Math.max(1, drainPerSecond);
    }

    /** Jari-jari wilayah domain. Sama untuk semua domain, jadi dibaca dari aturan. */
    public double radius() {
        return DomainRules.RADIUS;
    }

    /** Damage domain ke musuh per detik. */
    public float damagePerSecond() {
        return Math.max(0f, damagePerSecond);
    }
}
