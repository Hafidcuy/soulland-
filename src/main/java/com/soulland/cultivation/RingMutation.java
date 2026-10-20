package com.soulland.cultivation;

/**
 * Aritmetika mutasi spirit ring. Kelas ini murni (tanpa kelas Minecraft)
 * supaya aturannya bisa diuji tanpa runtime game.
 *
 * <p>Saat penyerapan berhasil ada peluang kecil ring bermutasi: umur
 * efektifnya naik satu grade visual dan pemain mendapat bonus stat permanen
 * kecil yang tercatat di capability.
 */
public final class RingMutation {
    private RingMutation() {}

    /** Peluang mutasi dalam persen setiap penyerapan berhasil. */
    public static final int CHANCE_PERCENT = 1;

    /** Grade umur spirit ring, dari termuda ke tertua. */
    public static final int[] GRADES = {100, 1_000, 10_000, 100_000, 1_000_000};

    /** Tambahan HP maksimum per mutasi. */
    public static final float HEALTH_PER_MUTATION = 1.0f;

    /** Tambahan attack damage per mutasi. */
    public static final float DAMAGE_PER_MUTATION = 0.25f;

    /** Umur efektif ring setelah bermutasi: satu grade lebih tua. */
    public static int mutatedAge(int age) {
        for (int i = 0; i < GRADES.length - 1; i++) {
            if (age < GRADES[i + 1]) return GRADES[i + 1];
        }
        return GRADES[GRADES.length - 1];
    }

    /** True kalau umur ini sudah grade tertinggi sehingga tidak bisa naik lagi. */
    public static boolean isTopGrade(int age) {
        return age >= GRADES[GRADES.length - 1];
    }

    /** Bonus HP maksimum dari jumlah mutasi yang pernah terjadi. */
    public static float healthBonus(int mutationCount) {
        return Math.max(0, mutationCount) * HEALTH_PER_MUTATION;
    }

    /** Bonus attack damage dari jumlah mutasi yang pernah terjadi. */
    public static float damageBonus(int mutationCount) {
        return Math.max(0, mutationCount) * DAMAGE_PER_MUTATION;
    }
}
