package com.soulland.combat;

import com.soulland.soul.MartialSoul;
import com.soulland.soul.MartialSouls;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integritas katalog True Body: cakupan 43 soul, kelengkapan angka, dan
 * kecocokan klasifikasi dengan registry soul.
 */
class TrueBodyCatalogTest {

    /** Jumlah soul yang harus punya True Body. */
    private static final int EXPECTED_SOULS = 43;

    @Test
    void registryTrueBodyMenutupSemuaSoul() {
        assertEquals(EXPECTED_SOULS, TrueBodyCatalog.registeredCount());
        assertEquals(EXPECTED_SOULS, TrueBodyCatalog.all().size());
        for (MartialSoul soul : MartialSouls.all()) {
            assertNotNull(TrueBodyCatalog.get(soul.id()), "soul tanpa True Body: " + soul.id());
        }
    }

    @Test
    void setiapSoulPunyaTepatSatuTrueBody() {
        Set<String> seen = new HashSet<>();
        for (TrueBodyDef def : TrueBodyCatalog.all()) {
            assertTrue(seen.add(def.soulId()), "True Body kembar: " + def.soulId());
            assertEquals(1, TrueBodyCatalog.all().stream()
                    .filter(item -> item.soulId().equals(def.soulId())).count());
        }
    }

    @Test
    void setiapTrueBodyPunyaNamaDanAngkaYangBerarti() {
        for (TrueBodyDef def : TrueBodyCatalog.all()) {
            assertFalse(def.nameEn().isEmpty(), "nama Inggris kosong: " + def.soulId());
            assertFalse(def.nameId().isEmpty(), "nama Indonesia kosong: " + def.soulId());
            assertTrue(def.damageMultiplier() > 1f,
                    "pengali damage harus di atas 1: " + def.soulId());
            assertTrue(def.energyCost() > 0f, "biaya energi harus di atas 0: " + def.soulId());
            assertTrue(def.auraRadius() > 0.0, "radius aura harus di atas 0: " + def.soulId());
            assertTrue(def.selfBuff().present(), "buff diri sendiri kosong: " + def.soulId());
            assertTrue(def.auraBuff().present(), "buff sekutu kosong: " + def.soulId());
            assertNotNull(def.lineage(), "lineage kosong: " + def.soulId());
            assertNotNull(def.element(), "elemen kosong: " + def.soulId());
        }
    }

    @Test
    void klasifikasiTrueBodyCocokDenganRegistrySoul() {
        for (MartialSoul soul : MartialSouls.all()) {
            TrueBodyDef def = TrueBodyCatalog.get(soul.id());
            assertEquals(soul.lineage(), def.lineage(), "lineage beda pada " + soul.id());
            assertEquals(MartialSouls.elementOf(soul), def.element(), "elemen beda pada " + soul.id());
        }
    }

    @Test
    void profilPerLineagePunyaCiriKhasnya() {
        for (TrueBodyDef def : TrueBodyCatalog.all()) {
            switch (def.lineage()) {
                case BEAST -> assertTrue(def.damageMultiplier() >= 1.38f,
                        "Beast harus ditekan lewat damage: " + def.soulId());
                case BODY -> assertTrue(def.auraRadius() <= 7.0,
                        "Body harus punya aura kecil: " + def.soulId());
                case TOOL, SPECIAL -> assertTrue(def.auraRadius() >= 5.0,
                        "aura terlalu kecil: " + def.soulId());
            }
        }
    }

    @Test
    void setiapSoulPunyaTrueBodyDenganNamaUnik() {
        Set<String> names = new HashSet<>();
        for (TrueBodyDef def : TrueBodyCatalog.all()) {
            assertTrue(names.add(def.nameEn()), "nama True Body kembar: " + def.nameEn());
        }
    }
}
