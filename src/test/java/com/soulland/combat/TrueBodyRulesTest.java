package com.soulland.combat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Angka tetap True Body: syarat buka, lama aktif, dan jeda.
 *
 * <p>Angka ini kontrak gameplay, jadi dikunci di sini supaya tidak bisa
 * bergeser diam-diam.
 */
class TrueBodyRulesTest {

    @Test
    void syaratBukaTujuhSpiritRing() {
        assertEquals(7, TrueBodyRules.UNLOCK_RINGS);
        assertFalse(TrueBodyRules.isUnlocked(0));
        assertFalse(TrueBodyRules.isUnlocked(6));
        assertTrue(TrueBodyRules.isUnlocked(7));
        assertTrue(TrueBodyRules.isUnlocked(9));
    }

    @Test
    void durasiAktifDuaPuluhDetik() {
        assertEquals(400, TrueBodyRules.DURATION_TICKS);
        assertEquals(20, TrueBodyRules.DURATION_SECONDS);
    }

    @Test
    void jedaLimaMenit() {
        assertEquals(6000, TrueBodyRules.COOLDOWN_TICKS);
        assertEquals(300, TrueBodyRules.COOLDOWN_SECONDS);
    }

    @Test
    void titikWaktuAktifDanJedaDihitungDariSekarang() {
        long now = 12_345L;
        assertEquals(now + 400, TrueBodyRules.activeUntil(now));
        assertEquals(now + 6000, TrueBodyRules.cooldownUntil(now));
    }

    @Test
    void bentukAktifBerhentiTepatPadaTitikAkhir() {
        long now = 1000L;
        assertTrue(TrueBodyRules.isActive(now, now + 400));
        assertTrue(TrueBodyRules.isActive(now + 399, now + 400));
        assertFalse(TrueBodyRules.isActive(now + 400, now + 400));
        assertFalse(TrueBodyRules.isActive(now + 401, now + 400));
    }

    @Test
    void sisaWaktuDibulatkanKeAtasDanTidakPernahNegatif() {
        long now = 1000L;
        assertEquals(400, TrueBodyRules.remainingTicks(now, now + 400));
        assertEquals(20, TrueBodyRules.remainingSeconds(now, now + 400));
        assertEquals(1, TrueBodyRules.remainingSeconds(now, now + 1));
        assertEquals(0, TrueBodyRules.remainingTicks(now + 400, now + 400));
        assertEquals(0, TrueBodyRules.remainingSeconds(now + 400, now + 400));
    }

    @Test
    void jedaBerjalanLaluBerakhirTepatPadaTitikAkhir() {
        long now = 500L;
        assertTrue(TrueBodyRules.onCooldown(now, now + 6000));
        assertTrue(TrueBodyRules.onCooldown(now + 5999, now + 6000));
        assertFalse(TrueBodyRules.onCooldown(now + 6000, now + 6000));
        assertEquals(300, TrueBodyRules.cooldownSeconds(now, now + 6000));
        assertEquals(1, TrueBodyRules.cooldownSeconds(now + 5980, now + 6000));
        assertEquals(0, TrueBodyRules.cooldownSeconds(now + 6000, now + 6000));
    }
}
