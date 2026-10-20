package com.soulland.soul;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Kategori soul untuk GUI Upacara Kebangkitan. Kategori diambil otomatis dari
 * field {@code type()} tiap soul, jadi menambah soul baru otomatis menambah tabnya.
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
    }

    /** Semua tipe soul unik, urut kemunculan pertama di registry. */
    public static List<String> types() {
        List<String> types = new ArrayList<>();
        for (MartialSoul soul : MartialSouls.all()) {
            if (!types.contains(soul.type())) types.add(soul.type());
        }
        return types;
    }

    /** Semua soul dalam satu kategori, urut sesuai registry. */
    public static List<MartialSoul> soulsOf(String type) {
        List<MartialSoul> souls = new ArrayList<>();
        for (MartialSoul soul : MartialSouls.all()) {
            if (soul.type().equals(type)) souls.add(soul);
        }
        return souls;
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
