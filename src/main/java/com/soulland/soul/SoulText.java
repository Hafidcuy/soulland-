package com.soulland.soul;

import net.minecraft.network.chat.Component;

/**
 * Teks martial soul dan skill. String di dalam data menjadi cadangan (fallback)
 * bila kunci translatable belum tersedia di bahasa aktif.
 */
public final class SoulText {
    private SoulText() {}

    public static Component soulName(MartialSoul soul) {
        return Component.translatableWithFallback("soul.soulland." + soul.id() + ".name", soul.name());
    }

    public static Component soulDescription(MartialSoul soul) {
        return Component.translatableWithFallback("soul.soulland." + soul.id() + ".description", soul.description());
    }

    /** Nama pemilik asli soul, dipakai di daftar dan pratinjau GUI. */
    public static Component soulOwner(MartialSoul soul) {
        return Component.translatableWithFallback("soul.soulland.owner." + soul.id(), soul.owner());
    }

    /** Tipe soul (Alat, Kontrol, Serangan Api, ...) yang tampil penuh di pratinjau. */
    public static Component soulType(MartialSoul soul) {
        return Component.translatableWithFallback("soul.soulland.type." + SoulCategories.typeSlug(soul.type()), soul.type());
    }

    public static Component skillName(SkillDef skill) {
        return Component.translatableWithFallback("skill.soulland." + skill.id() + ".name", skill.name());
    }

    public static Component skillDescription(SkillDef skill) {
        return Component.translatableWithFallback("skill.soulland." + skill.id() + ".description", skill.description());
    }
}
