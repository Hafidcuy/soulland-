package com.soulland.cultivation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Memastikan aritmetika mutasi spirit ring konsisten. */
class RingMutationTest {

    @Test
    void peluangMutasiSatuPersen() {
        assertEquals(1, RingMutation.CHANCE_PERCENT);
    }

    @Test
    void umurEfektifNaikSatuGrade() {
        assertEquals(1_000, RingMutation.mutatedAge(100));
        assertEquals(10_000, RingMutation.mutatedAge(1_000));
        assertEquals(100_000, RingMutation.mutatedAge(10_000));
        assertEquals(1_000_000, RingMutation.mutatedAge(100_000));
    }

    @Test
    void gradeTertinggiTidakBisaNaikLagi() {
        assertEquals(1_000_000, RingMutation.mutatedAge(1_000_000));
        assertTrue(RingMutation.isTopGrade(1_000_000));
        assertFalse(RingMutation.isTopGrade(100_000));
    }

    @Test
    void bonusStatNaikSeiringJumlahMutasi() {
        assertEquals(0f, RingMutation.healthBonus(0), 0.0001f);
        assertEquals(3f, RingMutation.healthBonus(3), 0.0001f);
        assertEquals(0f, RingMutation.damageBonus(0), 0.0001f);
        assertEquals(0.75f, RingMutation.damageBonus(3), 0.0001f);
    }

    @Test
    void jumlahMutasiNegatifDianggapNol() {
        assertEquals(0f, RingMutation.healthBonus(-5), 0.0001f);
        assertEquals(0f, RingMutation.damageBonus(-5), 0.0001f);
    }
}
