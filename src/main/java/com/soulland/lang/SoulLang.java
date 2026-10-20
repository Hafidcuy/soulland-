package com.soulland.lang;

import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Resolusi bahasa per-pemain untuk seluruh teks mod Soul Land.
 *
 * <p>Bahasa vanilla Minecraft tidak pernah disentuh. Mod ini punya dua bahasa
 * sendiri (Bahasa Indonesia dan Inggris) yang dimuat dari file lang yang sama
 * dengan milik vanilla, lalu teks di-resolve berdasarkan bahasa yang dipilih
 * pemain di dalam capability {@code ICultivation}.
 *
 * <p>Urutan fallback:
 * <ol>
 *   <li>bahasa yang dipilih pemain ("id" atau "en"),</li>
 *   <li>bahasa client Minecraft (dipakai server lewat rantai ClientBridge),
 *       atau Bahasa Indonesia sebagai default mod.</li>
 * </ol>
 *
 * <p>Pembacaan file memakai {@link Language#loadFromJson} dari Minecraft
 * supaya tidak ada parser JSON tambahan di sisi mod.
 */
public final class SoulLang {
    private SoulLang() {}

    /** Kode bahasa Indonesia. */
    public static final String INDONESIAN = "id";

    /** Kode bahasa Inggris. */
    public static final String ENGLISH = "en";

    /** Pemain belum memilih bahasa. */
    public static final String UNSET = "";

    /** Bahasa default mod: Bahasa Indonesia. */
    public static final String DEFAULT = INDONESIAN;

    private static final Map<String, Map<String, String>> TABLES = new HashMap<>();

    static {
        load(INDONESIAN, "/assets/soulland/lang/id_id.json");
        load(ENGLISH, "/assets/soulland/lang/en_us.json");
    }

    private static void load(String language, String resource) {
        Map<String, String> table = new HashMap<>();
        try (InputStream stream = SoulLang.class.getResourceAsStream(resource)) {
            if (stream != null) {
                Language.loadFromJson(stream, table::put);
            }
        } catch (Exception ignored) {
            // Berkas hilang atau rusak: tabel tetap kosong, teks cadangan yang dipakai.
        }
        TABLES.put(language, Collections.unmodifiableMap(table));
    }

    /** Normalisasi kode bahasa: "id_id" dan "id" sama-sama jadi "id". */
    public static String normalize(String raw) {
        if (raw == null) return UNSET;
        String value = raw.trim().toLowerCase(java.util.Locale.ROOT);
        if (value.isEmpty()) return UNSET;
        if (value.startsWith(INDONESIAN)) return INDONESIAN;
        if (value.startsWith(ENGLISH)) return ENGLISH;
        return UNSET;
    }

    /** Memetakan kode locale Minecraft ke bahasa mod, default-nya Inggris. */
    public static String fromLocale(String locale) {
        if (locale == null) return ENGLISH;
        String value = locale.trim().toLowerCase(java.util.Locale.ROOT);
        if (value.isEmpty()) return ENGLISH;
        if (value.startsWith(INDONESIAN)) return INDONESIAN;
        return ENGLISH;
    }

    /** Bahasa yang sedang aktif di sisi client (bisa kosong di server khusus). */
    public static String activeLanguage() {
        return normalize(com.soulland.net.ClientBridge.language());
    }

    /** Terjemahan teks untuk sebuah bahasa, dengan teks cadangan bila kuncinya hilang. */
    public static String translate(String language, String key, String fallback) {
        String code = normalize(language);
        if (code.isEmpty()) code = DEFAULT;
        Map<String, String> table = TABLES.get(code);
        if (table != null) {
            String value = table.get(key);
            if (value != null && !value.isEmpty()) return value;
        }
        return fallback == null ? "" : fallback;
    }

    /**
     * Terjemahan teks dengan placeholder {@code %s}, seperti format Minecraft.
     * Setiap placeholder diisi berurutan dengan argumen yang diberikan.
     */
    public static String translate(String language, String key, String fallback, Object... args) {
        return format(translate(language, key, fallback), args);
    }

    /** Terjemahan teks sebagai {@link Component} siap tampil. */
    public static MutableComponent text(String language, String key, String fallback) {
        return Component.literal(translate(language, key, fallback));
    }

    /** Terjemahan teks dengan placeholder {@code %s}, seperti format Minecraft. */
    public static MutableComponent text(String language, String key, String fallback, Object... args) {
        return Component.literal(translate(language, key, fallback, args));
    }

    /** Mengganti setiap {@code %s} pada pola dengan argumen yang diberikan. */
    private static String format(String pattern, Object... args) {
        if (args == null || args.length == 0) return pattern;
        StringBuilder builder = new StringBuilder();
        int argIndex = 0;
        int cursor = 0;
        while (argIndex < args.length) {
            int placeholder = pattern.indexOf("%s", cursor);
            if (placeholder < 0) break;
            builder.append(pattern, cursor, placeholder);
            builder.append(args[argIndex++]);
            cursor = placeholder + 2;
        }
        builder.append(pattern.substring(cursor));
        return builder.toString();
    }

    /** Nama peringkat kultivasi untuk bahasa tertentu. */
    public static MutableComponent rankName(int level, String language) {
        int index = com.soulland.CultivationRanks.rankIndex(level);
        return text(language, "rank.soulland." + index, com.soulland.CultivationRanks.rankName(level));
    }
}
