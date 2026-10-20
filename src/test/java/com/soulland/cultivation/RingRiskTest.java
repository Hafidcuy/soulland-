package com.soulland.cultivation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Memastikan aritmetika risiko penyerapan spirit ring konsisten. */
class RingRiskTest {

    @Test
    void levelTepatDiBatasAmanTidakBerisiko() {
        assertTrue(RingRisk.isSafe(10, 10));
        assertTrue(RingRisk.isSafe(90, 90));
        assertEquals(RingRisk.Severity.SAFE, RingRisk.severity(50, 40));
        assertEquals(0, RingRisk.failureChancePercent(50, 40));
        assertEquals(0, RingRisk.backlashPercent(50, 40));
    }

    @Test
    void levelDiBatasAtasTidakPernahBerisiko() {
        assertTrue(RingRisk.isSafe(120, 10));
        assertEquals(0, RingRisk.riskPermille(120, 10));
    }

    @Test
    void zonaRisikoNaikSeiringSelisihLevel() {
        assertEquals(RingRisk.Severity.MILD, RingRisk.severity(35, 40));
        assertEquals(RingRisk.Severity.MODERATE, RingRisk.severity(25, 40));
        assertEquals(RingRisk.Severity.SEVERE, RingRisk.severity(10, 40));
    }

    @Test
    void peluangGagalNaikDiZonaYangLebihBerat() {
        int mild = RingRisk.failureChancePercent(35, 40);
        int moderate = RingRisk.failureChancePercent(25, 40);
        int severe = RingRisk.failureChancePercent(10, 40);
        assertTrue(mild < moderate && moderate < severe);
        assertEquals(0, RingRisk.failureChancePercent(40, 40));
    }

    @Test
    void backlashHanyaAdaDiZonaSedangDanBerat() {
        assertEquals(0, RingRisk.backlashPercent(35, 40));
        assertTrue(RingRisk.backlashPercent(25, 40) > 0);
        assertTrue(RingRisk.backlashPercent(10, 40) > RingRisk.backlashPercent(25, 40));
        assertTrue(RingRisk.spiritDrain(10, 40) > 0);
        assertEquals(0, RingRisk.spiritDrain(35, 40));
    }

    @Test
    void rasioSelaluDalamRentang() {
        assertEquals(0, RingRisk.riskPermille(5, 0));
        assertTrue(RingRisk.riskPermille(1, 90) <= 1000);
        assertFalse(RingRisk.riskPermille(200, 90) < 0);
    }
}
