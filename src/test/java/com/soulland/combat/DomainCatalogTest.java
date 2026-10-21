package com.soulland.combat;

import com.soulland.soul.MartialSoul;
import com.soulland.soul.MartialSouls;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integritas katalog Domain: cakupan 43 soul, angka wilayah yang seragam,
 * dan keberadaan dua domain canon yang wajib ada.
 */
class DomainCatalogTest {

    /** Jumlah soul yang harus punya Domain. */
    private static final int EXPECTED_SOULS = 43;

    /** Soul yang membawa Blue Silver Domain. */
    private static final String BLUE_SILVER_SOUL = "blue_silver_grass";

    /** Soul yang membawa Killing God Domain. */
    private static final String KILLING_GOD_SOUL = "seven_kill_sword";

    @Test
    void registryDomainMenutupSemuaSoul() {
        assertEquals(EXPECTED_SOULS, DomainCatalog.registeredCount());
        assertEquals(EXPECTED_SOULS, DomainCatalog.all().size());
        for (MartialSoul soul : MartialSouls.all()) {
            assertNotNull(DomainCatalog.get(soul.id()), "soul tanpa domain: " + soul.id());
        }
    }

    @Test
    void setiapSoulPunyaTepatSatuDomain() {
        Set<String> seen = new HashSet<>();
        for (DomainDef def : DomainCatalog.all()) {
            assertTrue(seen.add(def.soulId()), "domain kembar: " + def.soulId());
        }
    }

    @Test
    void semuaDomainPakaiJariJariYangSama() {
        for (DomainDef def : DomainCatalog.all()) {
            assertEquals(DomainRules.RADIUS, def.radius(), "radius domain beda: " + def.soulId());
            assertEquals(12.0, def.radius());
        }
    }

    @Test
    void setiapDomainPunyaAngkaYangBerarti() {
        for (DomainDef def : DomainCatalog.all()) {
            assertFalse(def.nameEn().isEmpty(), "nama Inggris kosong: " + def.soulId());
            assertFalse(def.nameId().isEmpty(), "nama Indonesia kosong: " + def.soulId());
            assertTrue(def.drainPerSecond() >= 1, "pengurasan harus di atas 0: " + def.soulId());
            assertTrue(def.damagePerSecond() >= 0f, "damage negatif: " + def.soulId());
            assertTrue(def.allyBuff().present(), "buff sekutu kosong: " + def.soulId());
            assertTrue(def.enemyDebuff().present(), "debuff musuh kosong: " + def.soulId());
        }
    }

    @Test
    void domainDrainCukupUntukBerdiriBeberapaDetikLaluMatiSendiri() {
        for (DomainDef def : DomainCatalog.all()) {
            float cap = 100f;
            assertFalse(DomainRules.runsOut(cap, def.drainPerSecond(), 1),
                    "domain harus masih hidup setelah 1 detik: " + def.soulId());
            assertTrue(DomainRules.runsOut(cap, def.drainPerSecond(), 60),
                    "domain harus habis dalam 60 detik: " + def.soulId());
        }
    }

    @Test
    void blueSilverDomainAdaPadaSoulBlueSilverGrass() {
        DomainDef def = DomainCatalog.get(BLUE_SILVER_SOUL);
        assertNotNull(def);
        assertEquals("Blue Silver Domain", def.nameEn());
        assertEquals("Domain Blue Silver", def.nameId());
    }

    @Test
    void killingGodDomainHanyaAdaPadaSatuSoul() {
        List<DomainDef> matches = new ArrayList<>();
        for (DomainDef def : DomainCatalog.all()) {
            if (def.nameEn().contains("Killing God") || def.nameId().contains("Pembunuh Dewa")) {
                matches.add(def);
            }
        }
        assertEquals(1, matches.size(), "Killing God Domain harus punya tepat satu pemilik");
        assertEquals(KILLING_GOD_SOUL, matches.get(0).soulId());
        assertEquals("Killing God Domain", matches.get(0).nameEn());
    }

    @Test
    void namaDomainUnikSeluruhKatalog() {
        Set<String> names = new HashSet<>();
        for (DomainDef def : DomainCatalog.all()) {
            assertTrue(names.add(def.nameEn()), "nama domain kembar: " + def.nameEn());
        }
    }
}
