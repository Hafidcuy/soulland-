package com.soulland.soul;

import java.util.Locale;

/**
 * Empat lineage martial soul, yaitu cara soul itu muncul di dunia.
 *
 * <p>Lineage tidak menimpa {@code type()} pada record soul. {@code type()} menjawab
 * "peran apa yang soul ini punya dalam pertarungan" (Alat, Kontrol, Support, dan
 * seterusnya), sedangkan lineage menjawab "bentuk apa yang soul ini ambil saat
 * manifestasi". Keduanya dipakai bersamaan: GUI memakai lineage sebagai pemutar
 * utama, lalu {@code type()} sebagai pemutar kedua di bawahnya.
 *
 * <p>Empat bentuk ini mengikuti cetakan Soul Land: soul yang bergerak sendiri
 * (Beast), soul yang berupa benda yang wield (Tool), soul yang menempel pada
 * tubuh pemiliknya (Body), dan sisanya (Special) yang tidak masuk ketiga
 * bentuk tersebut, misalnya susunan, cermin, atau lentera.
 */
public enum SoulLineage {

    /** Manifestasi sebagai makhluk hidup yang bertarung di pihak pemiliknya. */
    BEAST("Beast", "Hewan"),

    /** Manifestasi sebagai perkakas, senjata, atau alat kerja. */
    TOOL("Tool", "Alat"),

    /** Manifestasi melalui tubuh pemiliknya sendiri, bukan benda terpisah. */
    BODY("Body", "Tubuh"),

    /** Manifestasi sebagai susunan, cermin, lentera, atau kekuatan tanpa bentuk. */
    SPECIAL("Special", "Khusus");

    private final String label;
    private final String labelId;

    SoulLineage(String label, String labelId) {
        this.label = label;
        this.labelId = labelId;
    }

    /** Nama lineage dalam bahasa Inggris, dipakai sebagai label baku. */
    public String label() {
        return label;
    }

    /** Nama lineage dalam bahasa Indonesia, dipakai sebagai cadangan. */
    public String labelId() {
        return labelId;
    }

    /** Kunci translatable untuk label lineage di GUI dan buku panduan. */
    public String langKey() {
        return "soul.soulland.lineage." + name().toLowerCase(Locale.ROOT);
    }
}
