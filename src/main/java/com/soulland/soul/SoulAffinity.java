package com.soulland.soul;

/**
 * Aturan afinitas: hubungan antara elemen afinitas sebuah soul dan elemen lawannya.
 *
 * <p>Elemen pada {@link MartialSoul#element()} menjawab "apa yang soul ini adalah"
 * dan sudah dipakai sejak v1.1 untuk melawan soul beast. Afinitas menambah sumbu
 * kedua: <em>elemen apa yang soul ini resonansikan</em>. Karena itu afinitas punya
 * aturan sendiri yang tidak menimpa aturan elemen lama.
 *
 * <p>Tiga keadaan, semuanya bisa diukur:
 * <ul>
 *   <li><b>Resonan</b>: afinitasMengalahi elemen lawan. Damage dikalikan
 *       {@link #RESONANT}.</li>
 *   <li><b>Netral</b>: tidak ada hubungan menang-kalah. Damage tidak berubah.</li>
 *   <li><b>Disonan</b>: elemen lawan mengalahi afinitas. Damage dikalikan
 *       {@link #DISCORDANT}.</li>
 * </ul>
 *
 * <p>Semua perhitungan memakai pola counter {@link Element#beats(Element)} yang sudah
 * ada, jadi tidak ada tabel Relationships baru yang bisa bertentangan dengan tabel
 * elemen. Elemen tanpa lawan (misalnya {@link Element#NONE}) selalu netral.
 *
 * <p>Kelas ini murni Java tanpa kelas Minecraft, jadi seluruh matematikanya bisa
 * diuji tanpa bootup registry.
 */
public final class SoulAffinity {

    private SoulAffinity() {}

    /** Pengali damage saat afinitas soul mengalahi elemen lawan. */
    public static final double RESONANT = 1.20;

    /** Pengali damage saat elemen lawan mengalahi afinitas soul. */
    public static final double DISCORDANT = 0.85;

    /** Pengali damage saat tidak ada hubungan menang-kalah. */
    public static final double NEUTRAL = 1.0;

    /**
     * Pengali damage dari sudut pandang pemilik afinitas.
     *
     * @param affinity elemen afinitas pemilik soul
     * @param opposing elemen lawan: elemen soul beast, atau afinitas soul lawan
     * @return {@link #RESONANT}, {@link #DISCORDANT}, atau {@link #NEUTRAL}
     */
    public static double versus(Element affinity, Element opposing) {
        if (affinity == null || opposing == null) return NEUTRAL;
        if (affinity == Element.NONE || opposing == Element.NONE) return NEUTRAL;
        if (affinity == opposing) return NEUTRAL;
        if (affinity.beats(opposing)) return RESONANT;
        if (opposing.beats(affinity)) return DISCORDANT;
        return NEUTRAL;
    }

    /** True kalau afinitas soul mengalahi elemen lawan. */
    public static boolean isResonant(Element affinity, Element opposing) {
        return versus(affinity, opposing) > NEUTRAL;
    }

    /** True kalau elemen lawan mengalahi afinitas soul. */
    public static boolean isDiscordant(Element affinity, Element opposing) {
        return versus(affinity, opposing) < NEUTRAL;
    }

    /**
     * Daftar elemen yang dikalahkan sebuah afinitas, untuk ditampilkan di GUI.
     * Urutan mengikuti urutan enum {@link Element} supaya stabil.
     */
    public static java.util.List<Element> beatsOf(Element affinity) {
        java.util.List<Element> result = new java.util.ArrayList<>();
        if (affinity == null || affinity == Element.NONE) return result;
        for (Element element : Element.values()) {
            if (affinity.beats(element)) result.add(element);
        }
        return result;
    }

    /**
     * Daftar elemen yang mengalahkan sebuah afinitas, untuk ditampilkan di GUI.
     * Elemen yang tidak punya hubungan apa pun tidak ikut masuk.
     */
    public static java.util.List<Element> losesTo(Element affinity) {
        java.util.List<Element> result = new java.util.ArrayList<>();
        if (affinity == null || affinity == Element.NONE) return result;
        for (Element element : Element.values()) {
            if (element.beats(affinity)) result.add(element);
        }
        return result;
    }
}
