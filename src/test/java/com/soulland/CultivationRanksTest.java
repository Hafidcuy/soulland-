package com.soulland;

import com.soulland.soul.Element;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Mem guaransi kurva XP dan aturan gerbang peringkat tetap konsisten. */
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
        assertEquals(99, CultivationRanks.MAX_LEVEL);
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
}
