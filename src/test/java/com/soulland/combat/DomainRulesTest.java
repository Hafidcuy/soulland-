package com.soulland.combat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Angka tetap Domain: jari-jari wilayah, pengurasan energi, dan matinya domain.
 *
 * <p>Domain harus benar-benar menguras energi dan menutup diri sendiri saat
 * energinya habis, jadi kedua hal itu diuji di sini.
 */
class DomainRulesTest {

    @Test
    void jariJariWilayahDuaBelasBlok() {
        assertEquals(12.0, DomainRules.RADIUS);
        assertEquals(144.0, DomainRules.RADIUS_SQUARED);
        assertTrue(DomainRules.inRadius(DomainRules.RADIUS_SQUARED));
        assertTrue(DomainRules.inRadius(0.0));
        assertFalse(DomainRules.inRadius(DomainRules.RADIUS_SQUARED + 1.0));
    }

    @Test
    void pengurasanDihitungPerDetikDanPerTick() {
        assertEquals(6f, DomainRules.drainPerSecond(6));
        assertEquals(0f, DomainRules.drainPerSecond(-4));
        assertEquals(0.3f, DomainRules.drainPerTick(6), 0.0001f);
        assertEquals(0f, DomainRules.drainPerTick(-2));
    }

    @Test
    void damageDomainDihitungPerDetikDanPerTick() {
        assertEquals(8f, DomainRules.damagePerSecond(8f));
        assertEquals(0f, DomainRules.damagePerSecond(-3f));
        assertEquals(0.4f, DomainRules.damagePerTick(8f), 0.0001f);
    }

    @Test
    void energiBerkurangSeiringDetikBerjalan() {
        assertEquals(6f, DomainRules.energyAfterSeconds(30f, 6, 4), 0.0001f);
        assertEquals(0f, DomainRules.energyAfterSeconds(30f, 6, 5), 0.0001f);
        assertEquals(0f, DomainRules.energyAfterSeconds(30f, 6, 9), 0.0001f);
    }

    @Test
    void domainMatiSendiriSaatEnergiHabis() {
        assertFalse(DomainRules.runsOut(30f, 6, 4));
        assertTrue(DomainRules.runsOut(30f, 6, 5));
        assertTrue(DomainRules.runsOut(30f, 6, 6));
        assertTrue(DomainRules.runsOut(0f, 6, 1));
        assertFalse(DomainRules.runsOut(30f, 6, 0));
    }
}
