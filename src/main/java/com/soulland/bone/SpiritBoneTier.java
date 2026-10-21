package com.soulland.bone;

/**
 * Lima mutu tulang spirit, sepadan dengan grade spirit ring.
 *
 * <p>Setiap tier punya angka dasar untuk satu tulang. Angka turunan per slot
 * dihitung sebagai {@code tier.dasar x slot.pengali}, jadi menambah tier
 * selalu menaikkan semua bonus tanpa perlu tabel 35 baris.
 *
 * <p>Warna tier sengaja sama dengan warna cincin spirit ring pada umur yang
 * sama, jadi pemain membaca<<"tulang ini sepadan dengan ring ini" tanpa teks
 * tambahan.
 */
public enum SpiritBoneTier {
    Y100("y100", 100, 0xFFE4E8EA, 0xFFF7FAFB, 1.0f, 1.0f, 5, 0.25f, 0),
    Y1000("y1000", 1_000, 0xFFF0C040, 0xFFFFE9A8, 2.0f, 2.0f, 10, 0.50f, 0),
    Y10000("y10000", 10_000, 0xFFB36BE0, 0xFFE2C4FF, 4.0f, 4.0f, 20, 1.00f, 1),
    Y100000("y100000", 100_000, 0xFFD2453C, 0xFFFFA79E, 8.0f, 8.0f, 40, 2.00f, 2),
    Y1000000("y1000000", 1_000_000, 0xFFE8C46A, 0xFFFFF0C0, 16.0f, 16.0f, 80, 4.00f, 3);

    private final String id;
    private final int years;
    private final int color;
    private final int lightColor;
    private final float baseAttack;
    private final float baseDefense;
    private final int baseEnergy;
    private final float baseRegen;
    private final int requiredRings;

    SpiritBoneTier(String id, int years, int color, int lightColor, float baseAttack, float baseDefense,
                   int baseEnergy, float baseRegen, int requiredRings) {
        this.id = id;
        this.years = years;
        this.color = color;
        this.lightColor = lightColor;
        this.baseAttack = baseAttack;
        this.baseDefense = baseDefense;
        this.baseEnergy = baseEnergy;
        this.baseRegen = baseRegen;
        this.requiredRings = requiredRings;
    }

    /** Id pendek tier, dipakai untuk id item dan kunci terjemahan. */
    public String id() {
        return id;
    }

    /** Umur tulang dalam tahun, mengikuti grade spirit ring. */
    public int years() {
        return years;
    }

    /** Warna utama tier (ARGB). */
    public int color() {
        return color;
    }

    /** Warna terang tier (ARGB), dipakai untuk kilau tepi tulang. */
    public int lightColor() {
        return lightColor;
    }

    /** Bonus serangan dasar satu tulang tier ini. */
    public float baseAttack() {
        return baseAttack;
    }

    /** Bonus defense dasar satu tulang tier ini. */
    public float baseDefense() {
        return baseDefense;
    }

    /** Bonus spirit power maksimum dasar satu tulang tier ini. */
    public int baseEnergy() {
        return baseEnergy;
    }

    /** Regenerasi HP per detik dasar satu tulang tier ini. */
    public float baseRegen() {
        return baseRegen;
    }

    /** Jumlah spirit ring minimum supaya tulang tier ini boleh dipasang. */
    public int requiredRings() {
        return requiredRings;
    }

    /** Bonus set yang aktif saat empat tulang tier ini terpasang. */
    public SetBonus setBonus() {
        return switch (this) {
            case Y100 -> new SetBonus(0f, 5f, 0, 0.5f, 0f);
            case Y1000 -> new SetBonus(10f, 0f, 0, 0f, 25f);
            case Y10000 -> new SetBonus(0f, 15f, 25, 0f, 0f);
            case Y100000 -> new SetBonus(12f, 0f, 0, 0.5f, 0f);
            case Y1000000 -> new SetBonus(20f, 20f, 50, 1.0f, 0f);
        };
    }

    /** Bonus puncak yang aktif saat enam tulang tier ini terpasang. */
    public PeakBonus peakBonus() {
        return switch (this) {
            case Y100 -> new PeakBonus(5f, 0f, 10, 0f);
            case Y1000 -> new PeakBonus(0f, 5f, 15, 0f);
            case Y10000 -> new PeakBonus(5f, 0f, 0, 0.5f);
            case Y100000 -> new PeakBonus(0f, 5f, 25, 0f);
            case Y1000000 -> new PeakBonus(5f, 0f, 10, 0f);
        };
    }

    /** Jumlah slot yang harus terisi untuk set bonus tier ini aktif. */
    public static final int SET_THRESHOLD = 4;

    /** Jumlah slot yang harus terisi untuk bonus puncak tier ini aktif. */
    public static final int PEAK_THRESHOLD = 6;

    /** Tier dari id-nya, atau null kalau id tidak dikenal. */
    public static SpiritBoneTier byId(String id) {
        if (id == null) return null;
        for (SpiritBoneTier tier : values()) {
            if (tier.id.equals(id)) return tier;
        }
        return null;
    }

    /** Tier dari umurnya, dipakai saat membaca data lama. */
    public static SpiritBoneTier byYears(int years) {
        SpiritBoneTier best = Y100;
        for (SpiritBoneTier tier : values()) {
            if (tier.years <= years) best = tier;
        }
        return best;
    }

    /** Bonus set: persen dan nilai absolut yang aktif saat set terpenuhi. */
    public record SetBonus(float attackPercent, float defensePercent, int energy, float regen,
                            float spiritRegenPercent) {
    }

    /** Bonus puncak: lapisan kedua saat seluruh slot terisi. */
    public record PeakBonus(float attackPercent, float defensePercent, int energy, float regen) {
    }
}
