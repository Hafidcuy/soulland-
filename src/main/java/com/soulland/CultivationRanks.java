package com.soulland;

/**
 * Peringkat kultivasi di dunia Soul Land (Douluo Dalu), level 1-120.
 *
 * <p>Tangga berisi 14 peringkat. Sembilan peringkat pertama memakai lebar
 * 10 level seperti versi sebelumnya, empat peringkat terakhir menyempit
 * (6, 6, 4, 4 level) supaya puncak terasa seperti puncak dan bukan grind
 * panjang. Nama peringkat lama tidak diganti.
 */
public final class CultivationRanks {
    private CultivationRanks() {}

    private static final String[] RANKS = {
            "Spirit Scholar",       // 1-10
            "Spirit Master",        // 11-20
            "Spirit Grandmaster",   // 21-30
            "Spirit Elder",         // 31-40
            "Spirit Ancestor",      // 41-50
            "Spirit King",          // 51-60
            "Spirit Emperor",       // 61-70
            "Spirit Sage",          // 71-80
            "Spirit Douluo",        // 81-90
            "Title Douluo",         // 91-100
            "Grand Title Douluo",   // 101-106
            "Limit Douluo",         // 107-112
            "Quasi-God",            // 113-116
            "God"                   // 117-120
    };

    /** Batas atas level tiap peringkat, sejajar dengan {@link #RANKS}. */
    private static final int[] BAND_END = {
            10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 106, 112, 116, 120
    };

    public static final int MAX_LEVEL = 120;
    public static final int MAX_RINGS = 9;
    public static final float MAX_ENERGY = 100f;
    /** Umur ring bawaan untuk data lama yang belum menyimpan umur tiap ring. */
    public static final int DEFAULT_RING_AGE = 100;

    /** Jumlah spirit ring yang dibutuhkan untuk membuka skill ke-3 dan ke-4. */
    public static final int RINGS_FOR_THIRD_SKILL = 5;
    public static final int RINGS_FOR_FOURTH_SKILL = 7;

    /** Level pertama peringkat tertinggi; mencapai angka ini memicu tanda tribulasi. */
    public static final int GOD_THRESHOLD = 117;

    /** Jumlah peringkat dalam tangga. */
    public static int rankCount() {
        return RANKS.length;
    }

    /** Indeks peringkat (0 = Spirit Scholar) untuk sebuah level. */
    public static int rankIndex(int level) {
        int safe = Math.max(1, Math.min(MAX_LEVEL, level));
        for (int i = 0; i < BAND_END.length; i++) {
            if (safe <= BAND_END[i]) return i;
        }
        return BAND_END.length - 1;
    }

    /** Batas atas level dari sebuah peringkat. */
    public static int bandEnd(int rankIndex) {
        int index = Math.max(0, Math.min(BAND_END.length - 1, rankIndex));
        return BAND_END[index];
    }

    public static String rankName(int level) {
        return RANKS[rankIndex(level)];
    }

    /**
     * Level gerbang: naik melewati batas bawah peringkat berikutnya wajib lewat
     * upacara terobosan, jadi butuh spirit ring dan fondasi yang cukup.
     */
    public static boolean isGateLevel(int level) {
        return level > 0 && level < MAX_LEVEL && level == bandEnd(rankIndex(level));
    }

    /**
     * Jumlah ring minimum agar boleh naik dari level gerbang tersebut.
     * Di atas peringkat kedelapan jumlah ring tidak menambah lagi karena
     * pemain hanya bisa membawa 9 ring.
     */
    public static int ringsRequiredAtGate(int level) {
        return Math.min(level / 10, MAX_RINGS);
    }

    /** XP yang dibutuhkan untuk naik dari level saat ini ke level berikutnya. */
    public static int xpForNextLevel(int level) {
        int safe = Math.max(1, Math.min(MAX_LEVEL, level));
        long base = 100L + safe * 25L;
        long multiplier = 100L + 15L * rankIndex(safe);
        return (int) (base * multiplier / 100L);
    }
}
