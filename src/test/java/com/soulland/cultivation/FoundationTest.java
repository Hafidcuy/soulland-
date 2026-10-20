package com.soulland.cultivation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Memastikan aritmetika fondasi kultivasi konsisten. */
class FoundationTest {

    @Test
    void nilaiFondasiSelaluDalamRentang() {
        assertEquals(0, Foundation.clamp(-20));
        assertEquals(100, Foundation.clamp(180));
        assertEquals(42, Foundation.clamp(42));
    }

    @Test
    void peluangNaikSesuaiFondasi() {
        assertEquals(60, Foundation.successChancePercent(0));
        assertEquals(100, Foundation.successChancePercent(100));
        assertTrue(Foundation.successChancePercent(50) > Foundation.successChancePercent(10));
        assertTrue(Foundation.successChancePercent(90) < Foundation.successChancePercent(100));
    }

    @Test
    void fondasiPenuhSelaluBerhasil() {
        assertEquals(100, Foundation.successChancePercent(Foundation.MAX));
        assertEquals(100, Foundation.successChancePercent(200));
    }

    @Test
    void fondasiHarusCukupUntukMencoba() {
        assertFalse(Foundation.canAttempt(Foundation.MIN_TO_ATTEMPT - 1));
        assertTrue(Foundation.canAttempt(Foundation.MIN_TO_ATTEMPT));
        assertTrue(Foundation.canAttempt(Foundation.MAX));
    }

    @Test
    void kegagalanMengurangiFondasiTapiTidakMelebihiNol() {
        assertEquals(75, Foundation.afterFailure(100));
        assertEquals(0, Foundation.afterFailure(10));
        assertEquals(0, Foundation.afterFailure(0));
    }

    @Test
    void meditasiMenambahFondasiSampaiBatas() {
        assertEquals(1, Foundation.gain(0, 1));
        assertEquals(100, Foundation.gain(99, 5));
        assertEquals(50, Foundation.gain(50, 0));
        assertEquals(50, Foundation.gain(50, -5));
    }

    @Test
    void rasioFondasiSelaluAntaraNolDanSatu() {
        assertEquals(0f, Foundation.ratio(0), 0.0001f);
        assertEquals(1f, Foundation.ratio(100), 0.0001f);
        assertTrue(Foundation.ratio(50) > 0.4f && Foundation.ratio(50) < 0.6f);
    }
}
