package com.soulland.soul;

import com.soulland.lang.SoulLang;
import net.minecraft.network.chat.Component;

/**
 * Teks martial soul dan skill.
 *
 * <p>Tiap teks punya versi Bahasa Indonesia dan Inggris. Teks di-resolve
 * sesuai bahasa pemain: server memakai bahasa dari capability, client memakai
 * salinan lokal yang sudah disinkronkan. String di dalam data registry hanya
 * dipakai sebagai cadangan (fallback) bila kunci terjemahan tidak ada.
 */
public final class SoulText {
    private SoulText() {}

    /** Nama soul untuk bahasa yang sedang aktif di sisi client. */
    public static Component soulName(MartialSoul soul) {
        return soulName(soul, SoulLang.activeLanguage());
    }

    /** Nama soul untuk bahasa tertentu. */
    public static Component soulName(MartialSoul soul, String language) {
        return SoulLang.text(language, "soul.soulland." + soul.id() + ".name", soul.name());
    }

    /** Deskripsi soul untuk bahasa yang sedang aktif di sisi client. */
    public static Component soulDescription(MartialSoul soul) {
        return soulDescription(soul, SoulLang.activeLanguage());
    }

    /** Deskripsi soul untuk bahasa tertentu. */
    public static Component soulDescription(MartialSoul soul, String language) {
        return SoulLang.text(language, "soul.soulland." + soul.id() + ".description", soul.description());
    }

    /** Nama pemilik asli soul, dipakai di daftar dan pratinjau GUI. */
    public static Component soulOwner(MartialSoul soul) {
        return soulOwner(soul, SoulLang.activeLanguage());
    }

    /** Nama pemilik asli soul untuk bahasa tertentu. */
    public static Component soulOwner(MartialSoul soul, String language) {
        return SoulLang.text(language, "soul.soulland.owner." + soul.id(), soul.owner());
    }

    /** Tipe soul (Alat, Kontrol, Serangan Api, ...) yang tampil penuh di pratinjau. */
    public static Component soulType(MartialSoul soul) {
        return soulType(soul, SoulLang.activeLanguage());
    }

    /** Tipe soul untuk bahasa tertentu. */
    public static Component soulType(MartialSoul soul, String language) {
        return SoulLang.text(language, "soul.soulland.type." + SoulCategories.typeSlug(soul.type()), soul.type());
    }

    /** Label rarity soul, siap tampil dengan warna. */
    public static Component soulRarity(MartialSoul soul) {
        return soulRarity(soul, SoulLang.activeLanguage());
    }

    /** Label rarity soul untuk bahasa tertentu. */
    public static Component soulRarity(MartialSoul soul, String language) {
        return SoulLang.text(language, "soul.soulland.rarity." + soul.rarity().toLowerCase(java.util.Locale.ROOT),
                soul.rarity());
    }

    /** Lineage soul, yaitu bentuk manifestasinya. */
    public static Component soulLineage(MartialSoul soul, String language) {
        SoulLineage lineage = MartialSouls.lineageOf(soul);
        if (lineage == null) return Component.literal("");
        return SoulLang.text(language, lineage.langKey(), lineage.label());
    }

    /** Elemen afinitas soul untuk bahasa tertentu. */
    public static Component soulAffinity(MartialSoul soul, String language) {
        Element affinity = MartialSouls.affinityOf(soul);
        return SoulLang.text(language, affinityKey(affinity), affinity.label());
    }

    /** Kunci translatable untuk nama elemen, dipakai afinitas dan pratinjau. */
    public static String affinityKey(Element element) {
        return "soul.soulland.affinity." + element.name().toLowerCase(java.util.Locale.ROOT);
    }

    /** Nama skill untuk bahasa yang sedang aktif di sisi client. */
    public static Component skillName(SkillDef skill) {
        return skillName(skill, SoulLang.activeLanguage());
    }

    /** Nama skill untuk bahasa tertentu. */
    public static Component skillName(SkillDef skill, String language) {
        return SoulLang.text(language, "skill.soulland." + skill.id() + ".name", skill.name());
    }

    /** Deskripsi skill untuk bahasa yang sedang aktif di sisi client. */
    public static Component skillDescription(SkillDef skill) {
        return skillDescription(skill, SoulLang.activeLanguage());
    }

    /** Deskripsi skill untuk bahasa tertentu. */
    public static Component skillDescription(SkillDef skill, String language) {
        return SoulLang.text(language, "skill.soulland." + skill.id() + ".description", skill.description());
    }
}
