package com.soulland.soul;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Kategori soul untuk GUI Upacara Kebangkitan.
 *
 * <p>Ada dua pemutar, keduanya diturunkan dari data registry sehingga menambah
 * soul baru otomatis menambah entri baru tanpa menyentuh kode GUI:
 * <ul>
 *   <li><b>Lineage</b> (v1.4): bentuk manifestasi, empat jenis.</li>
 *   <li><b>Type</b>: peran soul dalam pertarungan, diambil dari field
 *       {@code type()} tiap soul.</li>
 * </ul>
 *
 * <p>Urutan tab type mengikuti urutan kemunculan pertama di registry, jadi
 * penambahannya tidak mengubah urutan tab yang sudah ada.
 */
public final class SoulCategories {
    private SoulCategories() {}

    /** Pemetaan tipe ke kunci label pendek (label pendek supaya 7 tab muat di 300 px). */
    private static final Map<String, String> SHORT_LABEL_KEYS = new LinkedHashMap<>();

    static {
        SHORT_LABEL_KEYS.put("Alat", "gui.soulland.category.alat");
        SHORT_LABEL_KEYS.put("Kontrol", "gui.soulland.category.kontrol");
        SHORT_LABEL_KEYS.put("Agility", "gui.soulland.category.agility");
        SHORT_LABEL_KEYS.put("Support", "gui.soulland.category.support");
        SHORT_LABEL_KEYS.put("Serangan Api", "gui.soulland.category.serangan_api");
        SHORT_LABEL_KEYS.put("Power", "gui.soulland.category.power");
        SHORT_LABEL_KEYS.put("Racun", "gui.soulland.category.racun");
        SHORT_LABEL_KEYS.put("Serangan", "gui.soulland.category.serangan");
        SHORT_LABEL_KEYS.put("Pertahanan", "gui.soulland.category.pertahanan");
        SHORT_LABEL_KEYS.put("Penyembuhan", "gui.soulland.category.penyembuhan");
    }

    /** Semua tipe soul unik, urut kemunculan pertama di registry. */
    public static List<String> types() {
        return typesOf(null);
    }

    /**
     * Semua tipe soul unik milik satu lineage.
     *
     * @param lineage lineage yang difilter, null berarti semua lineage
     */
    public static List<String> typesOf(SoulLineage lineage) {
        List<String> types = new ArrayList<>();
        for (MartialSoul soul : MartialSouls.all()) {
            if (lineage != null && soul.lineage() != lineage) continue;
            if (!types.contains(soul.type())) types.add(soul.type());
        }
        return types;
    }

    /** Semua soul dalam satu kategori, urut sesuai registry. */
    public static List<MartialSoul> soulsOf(String type) {
        return soulsOf(null, type);
    }

    /**
     * Semua soul dalam satu kombinasi lineage dan kategori.
     *
     * @param lineage lineage yang difilter, null berarti semua lineage
     * @param type    kategori yang difilter, null berarti semua kategori
     */
    public static List<MartialSoul> soulsOf(SoulLineage lineage, String type) {
        List<MartialSoul> souls = new ArrayList<>();
        for (MartialSoul soul : MartialSouls.all()) {
            if (lineage != null && soul.lineage() != lineage) continue;
            if (type != null && !soul.type().equals(type)) continue;
            souls.add(soul);
        }
        return souls;
    }

    /** Semua soul dalam satu lineage, urut sesuai registry. */
    public static List<MartialSoul> soulsOfLineage(SoulLineage lineage) {
        return soulsOf(lineage, null);
    }

    /** Kunci translatable untuk label tab kategori. */
    public static String labelKey(String type) {
        String known = SHORT_LABEL_KEYS.get(type);
        if (known != null) return known;
        return "gui.soulland.category." + typeSlug(type);
    }

    /** Slug aman untuk dipakai pada kunci translatable. */
    public static String typeSlug(String type) {
        StringBuilder sb = new StringBuilder();
        for (char c : type.toLowerCase(java.util.Locale.ROOT).toCharArray()) {
            sb.append(Character.isLetterOrDigit(c) ? c : '_');
        }
        return sb.toString();
    }
}
