package com.soulland.cultivation;

/**
 * Aritmetika risiko menyerap spirit ring. Kelas ini murni (tanpa kelas
 * Minecraft) supaya aturannya bisa diuji tanpa runtime game.
 *
 * <p>Sebuah ring punya batas aman level. Menyerap di atas batas aman berarti
 * kapasitas pemain tidak cukup, dan hasilnya dihitung dari rasio selisih
 * terhadap batas aman itu. Di dalam batas aman tidak ada risiko sama sekali.
 */
public final class RingRisk {
    private RingRisk() {}

    /** Tingkat risiko penyerapan ring. */
    public enum Severity {
        /** Level pemain sudah cukup: penyerapan selalu aman. */
        SAFE,
        /** Sedikit di bawah batas aman: ring bisa hancur tanpa luka berarti. */
        MILD,
        /** Jauh di bawah batas aman: ring hancur dan vitalitas ikut jatuh. */
        MODERATE,
        /** Sangat jauh di bawah batas aman: ring hancur dan backlash berat. */
        SEVERE
    }

    /** Batas atas rasio (per seribu) untuk zona ringan. */
    public static final int MILD_MAX_PERMILLE = 340;
    /** Batas atas rasio (per seribu) untuk zona sedang. */
    public static final int MODERATE_MAX_PERMILLE = 690;

    /** Rasio (per seribu) selisih level pemain terhadap batas aman level. */
    public static int riskPermille(int playerLevel, int safeLevel) {
        if (safeLevel <= 0) return 0;
        int deficit = safeLevel - playerLevel;
        if (deficit <= 0) return 0;
        int permille = deficit * 1000 / safeLevel;
        return Math.max(0, Math.min(1000, permille));
    }

    /** Tingkat risiko untuk kombinasi level pemain dan batas aman level. */
    public static Severity severity(int playerLevel, int safeLevel) {
        int permille = riskPermille(playerLevel, safeLevel);
        if (permille <= 0) return Severity.SAFE;
        if (permille <= MILD_MAX_PERMILLE) return Severity.MILD;
        if (permille <= MODERATE_MAX_PERMILLE) return Severity.MODERATE;
        return Severity.SEVERE;
    }

    /** True kalau penyerapan ring ini aman untuk level pemain sekarang. */
    public static boolean isSafe(int playerLevel, int safeLevel) {
        return severity(playerLevel, safeLevel) == Severity.SAFE;
    }

    /** Peluang penyerapan gagal dalam persen. */
    public static int failureChancePercent(int playerLevel, int safeLevel) {
        return switch (severity(playerLevel, safeLevel)) {
            case SAFE -> 0;
            case MILD -> 15;
            case MODERATE -> 40;
            case SEVERE -> 70;
        };
    }

    /** Backlash sebagai persentase HP maksimum pemain. */
    public static int backlashPercent(int playerLevel, int safeLevel) {
        return switch (severity(playerLevel, safeLevel)) {
            case SAFE, MILD -> 0;
            case MODERATE -> 10;
            case SEVERE -> 25;
        };
    }

    /** Spirit power yang hilang saat backlash berat. */
    public static int spiritDrain(int playerLevel, int safeLevel) {
        return switch (severity(playerLevel, safeLevel)) {
            case SAFE, MILD -> 0;
            case MODERATE -> 15;
            case SEVERE -> 25;
        };
    }
}
