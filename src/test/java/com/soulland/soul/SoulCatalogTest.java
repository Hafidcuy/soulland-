package com.soulland.soul;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integritas katalog 43 soul: jumlah, keunikan id, klasifikasi lineage,
 * kelengkapan afinitas, dan keragaman rarity.
 */
class SoulCatalogTest {

    /** Jumlah soul target katalog v1.4. */
    private static final int EXPECTED_SOULS = 43;

    /** Jumlah soul yang harus ada di tiap lineage agar filter GUI berimbang. */
    private static final Map<SoulLineage, Integer> EXPECTED_LINEAGE = new LinkedHashMap<>();

    static {
        EXPECTED_LINEAGE.put(SoulLineage.BEAST, 16);
        EXPECTED_LINEAGE.put(SoulLineage.TOOL, 11);
        EXPECTED_LINEAGE.put(SoulLineage.BODY, 8);
        EXPECTED_LINEAGE.put(SoulLineage.SPECIAL, 8);
    }

    @Test
    void registryBerisiTepatTigaPuluhTigaSoul() {
        assertEquals(EXPECTED_SOULS, MartialSouls.registeredCount());
        assertEquals(EXPECTED_SOULS, MartialSouls.all().size());
    }

    @Test
    void idSoulUnikDanTidakKosong() {
        Set<String> seen = new HashSet<>();
        for (MartialSoul soul : MartialSouls.all()) {
            assertNotNull(soul.id());
            assertFalse(soul.id().isEmpty(), "id soul kosong");
            assertTrue(seen.add(soul.id()), "id soul kembar: " + soul.id());
        }
    }

    @Test
    void setiapSoulPunyaLineageDanDistribusinyaSesuaiRencana() {
        Map<SoulLineage, Integer> counts = new LinkedHashMap<>();
        for (SoulLineage lineage : SoulLineage.values()) {
            counts.put(lineage, 0);
        }
        for (MartialSoul soul : MartialSouls.all()) {
            assertNotNull(soul.lineage(), "lineage kosong pada soul " + soul.id());
            counts.merge(soul.lineage(), 1, Integer::sum);
        }
        assertEquals(EXPECTED_LINEAGE, counts);
    }

    @Test
    void setiapSoulPunyaAfinitasYangTerisi() {
        for (MartialSoul soul : MartialSouls.all()) {
            assertNotNull(soul.affinity(), "afinitas kosong pada soul " + soul.id());
            assertNotEqualsNone(soul.affinity(), soul.id());
        }
    }

    @Test
    void afinitasSelaluPunyaArtiYangTerukur() {
        for (MartialSoul soul : MartialSouls.all()) {
            Element affinity = soul.affinity();
            boolean adaArti = !SoulAffinity.beatsOf(affinity).isEmpty()
                    || !SoulAffinity.losesTo(affinity).isEmpty();
            assertTrue(adaArti, "afinitas " + affinity + " pada " + soul.id() + " tidak punya hubungan menang-kalah");
        }
    }

    @Test
    void rarityTerdistribusiDanTidakSemuaTertinggi() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (MartialSoul soul : MartialSouls.all()) {
            counts.merge(soul.rarity(), 1, Integer::sum);
        }
        for (String rarity : new String[]{"Common", "Uncommon", "Rare", "Epic", "Legendary"}) {
            assertTrue(counts.getOrDefault(rarity, 0) > 0, "tidak ada soul berk rarerity " + rarity);
        }
        assertTrue(counts.getOrDefault("Common", 0) >= 3, "rarity Common terlalu jarang");
        assertTrue(counts.getOrDefault("Legendary", 0) <= 6, "rarity Legendary terlalu banyak");
    }

    @Test
    void setiapSoulPunyaEmpatSkillPadaSlotRingYangBenar() {
        for (MartialSoul soul : MartialSouls.all()) {
            assertEquals(4, soul.skills().size(), "jumlah skill soul " + soul.id() + " bukan 4");
            int[] expectedRings = {1, 3, 5, 7};
            for (int slot = 0; slot < 4; slot++) {
                SkillDef skill = soul.skill(slot);
                assertNotNull(skill, "slot skill kosong pada soul " + soul.id());
                assertEquals(expectedRings[slot], skill.ringsRequired(),
                        "syarat ring slot " + slot + " pada soul " + soul.id() + " salah");
            }
        }
    }

    @Test
    void setiapSkillPunyaNamaDanDeskripsi() {
        for (MartialSoul soul : MartialSouls.all()) {
            for (SkillDef skill : soul.skills()) {
                assertFalse(skill.id().isEmpty());
                assertFalse(skill.name().isEmpty(), "nama skill kosong: " + skill.id());
                assertFalse(skill.description().isEmpty(), "deskripsi skill kosong: " + skill.id());
                assertTrue(skill.energyCost() > 0f, "biaya energi nol: " + skill.id());
            }
        }
    }

    @Test
    void idSkillUnikSeluruhRegistry() {
        Set<String> seen = new HashSet<>();
        int total = 0;
        for (MartialSoul soul : MartialSouls.all()) {
            for (SkillDef skill : soul.skills()) {
                assertTrue(seen.add(skill.id()), "id skill kembar: " + skill.id());
                total++;
            }
        }
        assertEquals(172, total, "jumlah skill registry tidak sesuai kontrak 43 x 4");
    }

    @Test
    void filterLineageDanTipeMenghasilkanSoulYangBenar() {
        for (SoulLineage lineage : SoulLineage.values()) {
            for (MartialSoul soul : SoulCategories.soulsOfLineage(lineage)) {
                assertEquals(lineage, soul.lineage());
            }
        }
        int total = 0;
        for (SoulLineage lineage : SoulLineage.values()) {
            total += SoulCategories.soulsOfLineage(lineage).size();
        }
        assertEquals(EXPECTED_SOULS, total, "jumlah soul per lineage tidak menutup registry");
    }

    @Test
    void setiapTipePunyaSoulMinimalSatu() {
        for (String type : SoulCategories.types()) {
            assertFalse(SoulCategories.soulsOf(type).isEmpty(), "tipe tanpa soul: " + type);
        }
    }

    private static void assertNotEqualsNone(Element affinity, String soulId) {
        assertFalse(affinity == Element.NONE, "afinitas tanpa elemen pada soul " + soulId);
    }
}
