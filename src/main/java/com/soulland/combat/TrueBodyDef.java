package com.soulland.combat;

import com.soulland.soul.Element;
import com.soulland.soul.SoulLineage;

/**
 * Definisi True Body milik satu martial soul.
 *
 * <p>True Body adalah bentuk ascensi martial soul: saat aktif, pemilik
 * mendapatkan pengali damage, buff untuk dirinya sendiri, dan aura yang
 * memengaruhi sekutu serta musuh di sekitar.
 *
 * <p>Angka di sini adalah kontrak gameplay, bukan hasil hitungan runtime:
 * <ul>
 *   <li>{@code damageMultiplier} mengalikan damage keluar selama aktif.</li>
 *   <li>{@code selfBuff} berlaku untuk pemilik.</li>
 *   <li>{@code auraBuff} berlaku untuk sekutu dalam {@code auraRadius}.</li>
 *   <li>{@code auraDebuff} berlaku untuk musuh dalam {@code auraRadius}.</li>
 *   <li>{@code energyCost} adalah spirit power yang dibayar saat aktivasi.</li>
 * </ul>
 *
 * <p>Durasi aktif dan jeda cooldown tidak ada di sini karena keduanya sama
 * untuk semua soul dan ditetapkan di {@link TrueBodyRules}.
 */
public record TrueBodyDef(
        String soulId,
        String nameEn,
        String nameId,
        SoulLineage lineage,
        Element element,
        int color,
        float damageMultiplier,
        BuffProfile selfBuff,
        BuffProfile auraBuff,
        BuffProfile auraDebuff,
        double auraRadius,
        float energyCost
) {

    /** True kalau aura ini punya efek untuk musuh. */
    public boolean hasAuraDebuff() {
        return auraDebuff.present();
    }

    /** Pengali damage yang dipakai server saat menghitung hit. */
    public float damageMultiplier() {
        return Math.max(1f, damageMultiplier);
    }
}
