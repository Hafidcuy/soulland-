package com.soulland.bone;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Memastikan lima mutu tulang spirit konsisten dengan grade spirit ring. */
class SpiritBoneTierTest {

    @Test
    void adaLimaMutuDenganIdUnik() {
        SpiritBoneTier[] tiers = SpiritBoneTier.values();
        assertEquals(5, tiers.length);
        Set<String> ids = new HashSet<>();
        for (SpiritBoneTier tier : tiers) {
            assertTrue(ids.add(tier.id()), "id tier kembar: " + tier.id());
        }
    }

    @Test
    void umurNaikSeiringMutuDanCocokDenganGradeRing() {
        SpiritBoneTier[] tiers = SpiritBoneTier.values();
        for (int index = 1; index < tiers.length; index++) {
            assertTrue(tiers[index].years() > tiers[index - 1].years(),
                    "umur tier " + tiers[index].id() + " harus lebih besar dari sebelumnya");
        }
        assertEquals(100, SpiritBoneTier.Y100.years());
        assertEquals(1_000, SpiritBoneTier.Y1000.years());
        assertEquals(10_000, SpiritBoneTier.Y10000.years());
        assertEquals(100_000, SpiritBoneTier.Y100000.years());
        assertEquals(1_000_000, SpiritBoneTier.Y1000000.years());
    }

    @Test
    void statDasarNaikGandaTiapTier() {
        SpiritBoneTier[] tiers = SpiritBoneTier.values();
        for (int index = 1; index < tiers.length; index++) {
            assertTrue(tiers[index].baseAttack() > tiers[index - 1].baseAttack());
            assertTrue(tiers[index].baseDefense() > tiers[index - 1].baseDefense());
            assertTrue(tiers[index].baseEnergy() > tiers[index - 1].baseEnergy());
            assertTrue(tiers[index].baseRegen() > tiers[index - 1].baseRegen());
        }
        assertEquals(16.0f, SpiritBoneTier.Y1000000.baseAttack(), 0.0001f);
    }

    @Test
    void statDasarTepatGandaTiapTier() {
        // Blueprint: setiap tier menggandakan angka tier sebelumnya, jadi
        // lonjakan kekuatan selalu terasa sama jelasnya.
        SpiritBoneTier[] tiers = SpiritBoneTier.values();
        for (int index = 1; index < tiers.length; index++) {
            assertEquals(tiers[index - 1].baseAttack() * 2f, tiers[index].baseAttack(), 0.0001f);
            assertEquals(tiers[index - 1].baseDefense() * 2f, tiers[index].baseDefense(), 0.0001f);
            assertEquals(tiers[index - 1].baseEnergy() * 2, tiers[index].baseEnergy());
            assertEquals(tiers[index - 1].baseRegen() * 2f, tiers[index].baseRegen(), 0.0001f);
        }
    }

    @Test
    void syaratSpiritRingNaikSesuaiMutu() {
        assertEquals(0, SpiritBoneTier.Y100.requiredRings());
        assertEquals(0, SpiritBoneTier.Y1000.requiredRings());
        assertEquals(1, SpiritBoneTier.Y10000.requiredRings());
        assertEquals(2, SpiritBoneTier.Y100000.requiredRings());
        assertEquals(3, SpiritBoneTier.Y1000000.requiredRings());
    }

    @Test
    void tierDicariLewatIdDanUmur() {
        assertEquals(SpiritBoneTier.Y1000, SpiritBoneTier.byId("y1000"));
        assertNull(SpiritBoneTier.byId("y1001"));
        assertNull(SpiritBoneTier.byId(null));
        assertEquals(SpiritBoneTier.Y100000, SpiritBoneTier.byYears(100_000));
        // Tier yang dipilih adalah umur tertinggi yang tidak melebihi umur itu.
        assertEquals(SpiritBoneTier.Y100000, SpiritBoneTier.byYears(999_999));
        assertEquals(SpiritBoneTier.Y1000000, SpiritBoneTier.byYears(1_000_000));
    }

    @Test
    void setiapTierPunyaBonusSetDanPuncak() {
        for (SpiritBoneTier tier : SpiritBoneTier.values()) {
            assertNotNull(tier.setBonus());
            assertNotNull(tier.peakBonus());
        }
        assertEquals(4, SpiritBoneTier.SET_THRESHOLD);
        assertEquals(6, SpiritBoneTier.PEAK_THRESHOLD);
        assertEquals(20f, SpiritBoneTier.Y1000000.setBonus().attackPercent(), 0.0001f);
    }
}
