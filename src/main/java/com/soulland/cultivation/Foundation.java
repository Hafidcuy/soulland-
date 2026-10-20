package com.soulland.cultivation;

/**
 * Aritmetika fondasi kultivasi. Kelas ini murni (tanpa kelas Minecraft)
 * supaya aturannya bisa diuji tanpa runtime game.
 *
 * <p>Fondasi bernilai 0 sampai 100 dan menyatakan kemajuan pemain menuju
 * ambang peringkat berikutnya. Meditasi memperbesar fondasi, dan peluang
 * sukses upacara terobosan mengikuti besar fondasi itu.
 */
public final class Foundation {
    private Foundation() {}

    /** Nilai maksimum fondasi. */
    public static final int MAX = 100;

    /** Fondasi minimal untuk boleh memulai upacara terobosan. */
    public static final int MIN_TO_ATTEMPT = 20;

    /** Berapa poin fondasi yang hilang ketika terobosan gagal. */
    public static final int BROKEN_ON_FAILURE = 25;

    /** Berapa tick meditasi yang dibutuhkan untuk menambah satu poin fondasi. */
    public static final int TICKS_PER_POINT = 8;

    /** Peluang terobosan terendah, ketika fondasi masih kosong. */
    public static final int MIN_CHANCE_PERCENT = 60;

    /** Menepati nilai fondasi ke rentang 0 sampai {@link #MAX}. */
    public static int clamp(int value) {
        return Math.max(0, Math.min(MAX, value));
    }

    /** Peluang sukses upacara terobosan dalam persen, dari nilai fondasi. */
    public static int successChancePercent(int foundation) {
        int safe = clamp(foundation);
        return MIN_CHANCE_PERCENT + (MAX - MIN_CHANCE_PERCENT) * safe / MAX;
    }

    /** True kalau fondasi cukup untuk mencoba upacara terobosan. */
    public static boolean canAttempt(int foundation) {
        return clamp(foundation) >= MIN_TO_ATTEMPT;
    }

    /** Fondasi setelah terobosan gagal: nilai turun, tapi tidak sampai nol dari nilai tinggi. */
    public static int afterFailure(int foundation) {
        return clamp(foundation - BROKEN_ON_FAILURE);
    }

    /** Menambah poin fondasi, berhenti di {@link #MAX}. */
    public static int gain(int foundation, int points) {
        if (points <= 0) return clamp(foundation);
        return clamp(foundation + points);
    }

    /** Kemajuan fondasi dalam rentang 0.0 sampai 1.0. */
    public static float ratio(int foundation) {
        return clamp(foundation) / (float) MAX;
    }
}
