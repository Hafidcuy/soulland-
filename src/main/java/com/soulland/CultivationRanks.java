package com.soulland;

/** Peringkat kultivasi di dunia Soul Land (Douluo Dalu), level 1-99. */
public final class CultivationRanks {
    private CultivationRanks() {}

    private static final String[] RANKS = {
            "Spirit Scholar",      // 1-10
            "Spirit Master",       // 11-20
            "Spirit Grandmaster",  // 21-30
            "Spirit Elder",        // 31-40
            "Spirit Ancestor",     // 41-50
            "Spirit King",         // 51-60
            "Spirit Emperor",      // 61-70
            "Spirit Sage",         // 71-80
            "Spirit Douluo",       // 81-90
            "Title Douluo"         // 91-99
    };

    public static final int MAX_LEVEL = 99;
    public static final int MAX_RINGS = 9;
    public static final float MAX_ENERGY = 100f;

    public static String rankName(int level) {
        int idx = Math.max(0, Math.min(RANKS.length - 1, (level - 1) / 10));
        return RANKS[idx];
    }

    /** Level gerbang: untuk naik melewati level 10/20/.../90 pemain wajib punya spirit ring tambahan. */
    public static boolean isGateLevel(int level) {
        return level > 0 && level % 10 == 0 && level < MAX_LEVEL;
    }

    /** Jumlah ring minimum agar boleh naik dari level gerbang tersebut. */
    public static int ringsRequiredAtGate(int level) {
        return level / 10;
    }

    /** XP yang dibutuhkan untuk naik dari level saat ini ke level berikutnya. */
    public static int xpForNextLevel(int level) {
        return 100 + level * 25;
    }
}
