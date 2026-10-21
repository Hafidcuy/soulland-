package com.soulland.skill;

import com.soulland.soul.MartialSoul;
import com.soulland.soul.MartialSouls;
import com.soulland.soul.SkillArchetype;
import com.soulland.soul.SkillDef;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Cakupan skill: setiap id skill yang terdaftar harus benar-benar punya
 * penangan di {@code SkillHandler}, dan tidak boleh ada skill kosong.
 *
 * <p>Skill lama memakai blok {@code case} khusus, skill baru memakai jalur
 * arketipe. Kedua jalur dihitung di sini, jadi invariant "jumlah penangan
 * sama dengan jumlah skill terdaftar" terus dijaga.
 */
class SkillCoverageTest {

    /** Id skill yang ditangani jalur arketipe, bukan blok case. */
    private static final int EXPECTED_ARCHETYPE_SKILLS = 132;

    /** Id skill yang ditangani blok case khusus. */
    private static final int EXPECTED_CASE_SKILLS = 40;

    @Test
    void setiapSkillTerdaftarPunyaPenangan() {
        Set<String> handledByCase = switchCases();
        List<String> uncovered = new ArrayList<>();
        for (MartialSoul soul : MartialSouls.all()) {
            for (SkillDef skill : soul.skills()) {
                if (skill.usesArchetype()) continue;
                if (!handledByCase.contains(skill.id())) uncovered.add(skill.id());
            }
        }
        assertTrue(uncovered.isEmpty(), "skill tanpa penangan: " + uncovered);
    }

    @Test
    void tidakAdaBlokCaseYangMenggantung() {
        Set<String> registered = new HashSet<>();
        for (MartialSoul soul : MartialSouls.all()) {
            for (SkillDef skill : soul.skills()) {
                registered.add(skill.id());
            }
        }
        List<String> orphan = new ArrayList<>();
        for (String id : switchCases()) {
            if (!registered.contains(id)) orphan.add(id);
        }
        assertTrue(orphan.isEmpty(), "blok case tanpa skill terdaftar: " + orphan);
    }

    @Test
    void jumlahPenanganSamaDenganJumlahSkillTerdaftar() {
        Set<String> handledByCase = switchCases();
        int archetype = 0;
        for (MartialSoul soul : MartialSouls.all()) {
            for (SkillDef skill : soul.skills()) {
                if (skill.usesArchetype()) archetype++;
            }
        }
        assertEquals(EXPECTED_CASE_SKILLS, handledByCase.size(), "jumlah blok case skill lama berubah");
        assertEquals(EXPECTED_ARCHETYPE_SKILLS, archetype, "jumlah skill arketipe berubah");
        assertEquals(EXPECTED_CASE_SKILLS + archetype, handledByCase.size() + archetype);
    }

    @Test
    void tidakAdaSkillArketipeYangKosong() {
        for (MartialSoul soul : MartialSouls.all()) {
            for (SkillDef skill : soul.skills()) {
                if (!skill.usesArchetype()) continue;
                SkillArchetype archetype = skill.archetype();
                switch (archetype.kind()) {
                    case DAMAGE -> assertTrue(skill.damage() > 0f,
                            "skill damage tanpa angka: " + skill.id());
                    case CONTROL -> {
                        assertTrue(skill.radius() > 0, "skill kontrol tanpa radius: " + skill.id());
                        assertTrue(skill.effectTicks() > 0, "skill kontrol tanpa durasi: " + skill.id());
                    }
                    case SUPPORT -> assertTrue(skill.heal() > 0f || skill.effectTicks() > 0,
                            "skill penyokong tanpa efek: " + skill.id());
                    case MOBILITY -> assertTrue(skill.radius() > 0,
                            "skill pergerakan tanpa jangkauan: " + skill.id());
                }
            }
        }
    }

    @Test
    void semuaArketipePunyaArtiYangBerbeda() {
        Set<String> summaries = new LinkedHashSet<>();
        for (SkillArchetype archetype : SkillArchetype.values()) {
            assertNotNull(archetype.summary());
            assertFalse(archetype.summary().isEmpty());
            assertTrue(summaries.add(archetype.summary()),
                    "dua arketipe punya ringkasan yang sama: " + archetype);
        }
        assertEquals(20, SkillArchetype.values().length);
    }

    @Test
    void skillArketipePunyaTandaVisualYangBerbeda() {
        Set<Integer> colors = new HashSet<>();
        Set<String> shapes = new HashSet<>();
        for (MartialSoul soul : MartialSouls.all()) {
            for (SkillDef skill : soul.skills()) {
                if (!skill.usesArchetype()) continue;
                colors.add(skill.visual().color());
                shapes.add(skill.visual().shape().name());
            }
        }
        assertTrue(colors.size() >= 20, "warna tanda skill terlalu sedikit: " + colors.size());
        assertTrue(shapes.size() >= 5, "bentuk tanda skill terlalu sedikit: " + shapes.size());
    }

    /** Baca id skill yang ditangani blok {@code case} di sumber SkillHandler. */
    private static Set<String> switchCases() {
        Path source = findSource("src/main/java/com/soulland/skill/SkillHandler.java");
        assertNotNull(source, "sumber SkillHandler tidak ditemukan");
        String text;
        try {
            text = Files.readString(source, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("gagal membaca sumber SkillHandler", e);
        }
        Matcher matcher = Pattern.compile("case\\s+\"([a-z0-9_]+)\"\\s*->").matcher(text);
        Set<String> ids = new LinkedHashSet<>();
        while (matcher.find()) {
            ids.add(matcher.group(1));
        }
        return ids;
    }

    /** Cari berkas sumber dari direktori kerja sekarang ke atas. */
    private static Path findSource(String relative) {
        Path dir = Path.of(System.getProperty("user.dir")).toAbsolutePath();
        for (int depth = 0; depth < 5 && dir != null; depth++) {
            Path candidate = dir.resolve(relative);
            if (Files.exists(candidate)) return candidate;
            dir = dir.getParent();
        }
        return null;
    }
}
