package com.soulland.soul;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Rarity martial soul. Warna dipakai sebagai penanda tambahan, selalu ditampilkan
 * bersama nama rarity agar tidak mengandalkan warna saja.
 */
public final class SoulRarity {
    private SoulRarity() {}

    public static final String COMMON = "Common";
    public static final String UNCOMMON = "Uncommon";
    public static final String RARE = "Rare";
    public static final String EPIC = "Epic";
    public static final String LEGENDARY = "Legendary";

    private static final Map<String, Integer> COLORS = new HashMap<>();

    static {
        COLORS.put(COMMON, 0xB7BFC7);
        COLORS.put(UNCOMMON, 0x6BD65B);
        COLORS.put(RARE, 0x4AA3E0);
        COLORS.put(EPIC, 0xB36BE0);
        COLORS.put(LEGENDARY, 0xF0C040);
    }

    /** Warna rarity sebagai ARGB. Rarity tak dikenal memakai warna teks netral. */
    public static int color(String rarity) {
        Integer color = COLORS.get(rarity);
        return color == null ? 0xB7BFC7 : color;
    }

    /** Kunci translatable untuk nama rarity (dipakai di GUI dan HUD). */
    public static String langKey(String rarity) {
        return "soul.soulland.rarity." + rarity.toLowerCase(Locale.ROOT);
    }
}
