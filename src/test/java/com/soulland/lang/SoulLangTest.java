package com.soulland.lang;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Memastikan resolusi bahasa per-pemain membaca kedua file lang dengan benar. */
class SoulLangTest {

    @Test
    void kodeBahasaDnormalisasi() {
        assertEquals("id", SoulLang.normalize("id"));
        assertEquals("id", SoulLang.normalize("id_id"));
        assertEquals("id", SoulLang.normalize("ID_ID"));
        assertEquals("en", SoulLang.normalize("en"));
        assertEquals("en", SoulLang.normalize("en_us"));
        assertEquals("", SoulLang.normalize(""));
        assertEquals("", SoulLang.normalize(null));
        assertEquals("", SoulLang.normalize("de_de"));
    }

    @Test
    void localeMinecraftDipetakanKeBahasaMod() {
        assertEquals("id", SoulLang.fromLocale("id_id"));
        assertEquals("en", SoulLang.fromLocale("en_us"));
        assertEquals("en", SoulLang.fromLocale("de_de"));
        assertEquals("en", SoulLang.fromLocale(""));
        assertEquals("en", SoulLang.fromLocale(null));
    }

    @Test
    void terjemahanDibacaDariFileBahasa() {
        assertEquals("Bahasa Indonesia", SoulLang.translate("id", "gui.soulland.language.id", "cadangan"));
        assertEquals("English", SoulLang.translate("en", "gui.soulland.language.en", "cadangan"));
    }

    @Test
    void kunciHilangMemakaiCadangan() {
        assertEquals("cadangan", SoulLang.translate("id", "kunci.yang.tidak.ada", "cadangan"));
        assertEquals("cadangan", SoulLang.translate("", "kunci.yang.tidak.ada", "cadangan"));
    }

    @Test
    void namaPeringkatMengikutiBahasa() {
        assertEquals("Dewa", SoulLang.rankName(120, "id").getString());
        assertEquals("God", SoulLang.rankName(120, "en").getString());
    }

    @Test
    void bahasaDefaultModAdalahIndonesia() {
        assertEquals("id", SoulLang.DEFAULT);
        assertTrue(SoulLang.translate(SoulLang.DEFAULT, "gui.soulland.language.id", "").contains("Indonesia"));
        assertFalse(SoulLang.translate(SoulLang.DEFAULT, "gui.soulland.language.id", "").isEmpty());
    }
}
