package com.soulland;

import com.soulland.soul.Element;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Memastikan kurva XP dan aturan gerbang peringkat tetap konsisten. */
class CultivationRanksTest {

    @Test
    void xpSelaluNaikSeiringLevel() {
        for (int level = 1; level < CultivationRanks.MAX_LEVEL; level++) {
            assertTrue(CultivationRanks.xpForNextLevel(level + 1) > CultivationRanks.xpForNextLevel(level),
                    "kurva XP harus menaik");
        }
    }

    @Test
    void gerbangPeringkatTepatSepuluh() {
        assertTrue(CultivationRanks.isGateLevel(10));
        assertTrue(CultivationRanks.isGateLevel(90));
        assertFalse(CultivationRanks.isGateLevel(9));
        assertFalse(CultivationRanks.isGateLevel(99));
    }

    @Test
    void jumlahRingYangDibutuhkanSepadanDenganGerbang() {
        for (int level = 10; level <= 90; level += 10) {
            assertEquals(level / 10, CultivationRanks.ringsRequiredAtGate(level));
        }
    }

    @Test
    void peringkatSesuaiLevel() {
        assertEquals("Spirit Scholar", CultivationRanks.rankName(1));
        assertEquals("Spirit Master", CultivationRanks.rankName(15));
        assertEquals("Title Douluo", CultivationRanks.rankName(95));
        assertEquals("Spirit Douluo", CultivationRanks.rankName(85));
    }

    @Test
    void batasLevelDanEnergiDihormati() {
        assertEquals(120, CultivationRanks.MAX_LEVEL);
        assertEquals(9, CultivationRanks.MAX_RINGS);
        assertEquals(100f, CultivationRanks.MAX_ENERGY, 0.001f);
    }

    @Test
    void setiapElemenPunyaWarnaDanLabel() {
        for (Element element : Element.values()) {
            assertFalse(element.label().isEmpty());
            assertTrue(element.color() != 0);
        }
    }

    @Test
    void tanggaRankEmpatBelasSampaiDewa() {
        assertEquals(14, CultivationRanks.rankCount());
        assertEquals(120, CultivationRanks.MAX_LEVEL);
        assertEquals("Spirit Scholar", CultivationRanks.rankName(1));
        assertEquals("Title Douluo", CultivationRanks.rankName(91));
        assertEquals("Grand Title Douluo", CultivationRanks.rankName(101));
        assertEquals("Limit Douluo", CultivationRanks.rankName(107));
        assertEquals("Quasi-God", CultivationRanks.rankName(113));
        assertEquals("God", CultivationRanks.rankName(117));
        assertEquals("God", CultivationRanks.rankName(120));
    }

    @Test
    void setiapLevelPunyaSatuRankYangValid() {
        for (int level = 1; level <= CultivationRanks.MAX_LEVEL; level++) {
            int index = CultivationRanks.rankIndex(level);
            assertTrue(index >= 0 && index < CultivationRanks.rankCount(),
                    "rank level " + level + " di luar rentang");
            assertEquals(CultivationRanks.rankName(level), CultivationRanks.rankName(CultivationRanks.bandEnd(index)),
                    "nama rank harus sama di seluruh pita");
        }
    }

    @Test
    void gerbangBaruAdaDiSetiapBatasPitaKecualiPuncak() {
        int[] gate = {10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 106, 112, 116};
        for (int level : gate) {
            assertTrue(CultivationRanks.isGateLevel(level), "level " + level + " harus jadi gerbang");
        }
        assertFalse(CultivationRanks.isGateLevel(101));
        assertFalse(CultivationRanks.isGateLevel(107));
        assertFalse(CultivationRanks.isGateLevel(117));
        assertFalse(CultivationRanks.isGateLevel(CultivationRanks.MAX_LEVEL));
    }

    @Test
    void jumlahRingTidakMelebihiSembilanDiPeringkatAtas() {
        for (int level = 90; level < CultivationRanks.MAX_LEVEL; level += 10) {
            assertEquals(CultivationRanks.MAX_RINGS, CultivationRanks.ringsRequiredAtGate(level),
                    "pemain hanya bisa membawa 9 ring");
        }
        assertEquals(9, CultivationRanks.ringsRequiredAtGate(100));
        assertEquals(9, CultivationRanks.ringsRequiredAtGate(116));
    }

    @Test
    void ambangDewaTepatDiAwalPeringkatTerakhir() {
        assertEquals(117, CultivationRanks.GOD_THRESHOLD);
        assertEquals("God", CultivationRanks.rankName(CultivationRanks.GOD_THRESHOLD));
        assertFalse(CultivationRanks.rankName(CultivationRanks.GOD_THRESHOLD - 1).equals("God"));
    }
}
