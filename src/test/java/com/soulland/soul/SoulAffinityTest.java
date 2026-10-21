package com.soulland.soul;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Matematika afinitas: afinitas yang menang memberi pengali lebih besar,
 * yang kalah memberi pengali lebih kecil, dan yang tidak berhubungan netral.
 */
class SoulAffinityTest {

    @Test
    void afinitasYangMenangMemberiPengaliResonan() {
        assertTrue(SoulAffinity.isResonant(Element.FIRE, Element.EARTH));
        assertEquals(SoulAffinity.RESONANT, SoulAffinity.versus(Element.FIRE, Element.EARTH), 0.0001);
        assertEquals(1.20, SoulAffinity.versus(Element.FIRE, Element.EARTH), 0.0001);
    }

    @Test
    void afinitasYangKalahMemberiPengaliDisonan() {
        assertTrue(SoulAffinity.isDiscordant(Element.FIRE, Element.WATER));
        assertEquals(SoulAffinity.DISCORDANT, SoulAffinity.versus(Element.FIRE, Element.WATER), 0.0001);
        assertEquals(0.85, SoulAffinity.versus(Element.FIRE, Element.WATER), 0.0001);
    }

    @Test
    void elemenTanpaHubunganMemberiPengaliNetral() {
        assertEquals(SoulAffinity.NEUTRAL, SoulAffinity.versus(Element.WATER, Element.WATER), 0.0001);
        assertEquals(1.0, SoulAffinity.versus(Element.WATER, Element.LIGHT), 0.0001);
        assertFalse(SoulAffinity.isResonant(Element.WATER, Element.WATER));
        assertFalse(SoulAffinity.isDiscordant(Element.WATER, Element.WATER));
    }

    @Test
    void elemenTanpaElemenSelaluNetral() {
        assertEquals(1.0, SoulAffinity.versus(Element.NONE, Element.FIRE), 0.0001);
        assertEquals(1.0, SoulAffinity.versus(Element.FIRE, Element.NONE), 0.0001);
        assertEquals(1.0, SoulAffinity.versus(Element.NONE, Element.NONE), 0.0001);
    }

    @Test
    void nullSelaluNetral() {
        assertEquals(1.0, SoulAffinity.versus(null, Element.FIRE), 0.0001);
        assertEquals(1.0, SoulAffinity.versus(Element.FIRE, null), 0.0001);
    }

    @Test
    void duaAfinitasTidakSalingMembatalkan() {
        for (Element affinity : Element.values()) {
            for (Element opposing : Element.values()) {
                boolean menang = SoulAffinity.versus(affinity, opposing) > 1.0;
                boolean kalah = SoulAffinity.versus(affinity, opposing) < 1.0;
                assertFalse(menang && kalah,
                        affinity + " dan " + opposing + " tidak boleh menang dan kalah sekaligus");
            }
        }
    }

    @Test
    void aturanAfinitasKonsistenDenganCounterElemen() {
        for (Element affinity : Element.values()) {
            for (Element opposing : Element.values()) {
                double value = SoulAffinity.versus(affinity, opposing);
                if (affinity == Element.NONE || opposing == Element.NONE) {
                    assertEquals(1.0, value, 0.0001);
                } else if (affinity.beats(opposing)) {
                    assertEquals(1.20, value, 0.0001);
                } else if (opposing.beats(affinity)) {
                    assertEquals(0.85, value, 0.0001);
                } else {
                    assertEquals(1.0, value, 0.0001);
                }
            }
        }
    }

    @Test
    void daftarElemenYangDikalahkanSesuaiCounterElemen() {
        assertTrue(SoulAffinity.beatsOf(Element.FIRE).contains(Element.EARTH));
        assertTrue(SoulAffinity.beatsOf(Element.FIRE).contains(Element.NATURE));
        assertFalse(SoulAffinity.beatsOf(Element.FIRE).contains(Element.WATER));
        assertTrue(SoulAffinity.losesTo(Element.FIRE).contains(Element.WATER));
        assertTrue(SoulAffinity.losesTo(Element.FIRE).contains(Element.WIND));
        assertFalse(SoulAffinity.losesTo(Element.FIRE).contains(Element.ICE));
        assertTrue(SoulAffinity.beatsOf(Element.NONE).isEmpty());
        assertTrue(SoulAffinity.losesTo(Element.NONE).isEmpty());
    }

    @Test
    void setiapAfinitasSoulTerdaftarPunyaArti() {
        for (MartialSoul soul : MartialSouls.all()) {
            Element affinity = soul.affinity();
            assertTrue(!SoulAffinity.beatsOf(affinity).isEmpty() || !SoulAffinity.losesTo(affinity).isEmpty(),
                    "afinitas soul " + soul.id() + " tidak punya hubungan menang-kalah");
        }
    }
}
