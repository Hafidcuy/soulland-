package com.soulland.bone;

import java.util.Random;

/**
 * Perhitungan peluang drop tulang spirit.
 *
 * <p>Kelas ini murni: angka peluang masuk sebagai parameter, keluarannya
 * deterministik kalau {@link Random} yang diberikan bisa diulang. Dengan
 * begitu aturan peluang bisa diuji unit tanpa server berjalan.
 */
public final class BoneDropLogic {
    private BoneDropLogic() {}

    /**
     * Peluang mob biasa menjatuhkan tulang, diskalakan jumlah spirit ring
     * pemain.
     *
     * @param baseChance peluang dasar saat pemain tidak punya ring
     * @param ringScale pengali per spirit ring yang dimiliki
     * @param maxChance plafon peluang
     * @param rings jumlah spirit ring pemain
     */
    public static double hostileChance(double baseChance, double ringScale, double maxChance, int rings) {
        if (baseChance <= 0.0) return 0.0;
        if (maxChance <= 0.0) return 0.0;
        int safeRings = Math.max(0, rings);
        double chance = baseChance * (1.0 + safeRings * ringScale);
        return Math.min(maxChance, Math.max(0.0, chance));
    }

    /**
     * Peluang drop dari boss.
     *
     * <p>Boss memakai peluang tetap per sumber, bukan skala ring, supaya
     * berburu boss tetap terasa seperti kejadian besar.
     */
    public static double bossChance(double configuredChance) {
        if (configuredChance <= 0.0) return 0.0;
        return Math.min(1.0, configuredChance);
    }

    /** True kalau undian berhasil. */
    public static boolean roll(double chance, Random random) {
        if (chance <= 0.0) return false;
        if (chance >= 1.0) return true;
        return random.nextDouble() < chance;
    }

    /**
     * Memilih tier dari sekumpulan bobot.
     *
     * <p>Bobotnya urut sesuai {@link SpiritBoneTier#values()}. Kalau semua
     * bobot nol, tier terendah dipakai supaya drop tetap terjadi.
     */
    public static SpiritBoneTier pickTier(int[] weights, Random random) {
        SpiritBoneTier[] tiers = SpiritBoneTier.values();
        if (weights == null || weights.length != tiers.length) return tiers[0];
        int total = 0;
        for (int weight : weights) {
            if (weight > 0) total += weight;
        }
        if (total <= 0) return tiers[0];

        int roll = random.nextInt(total);
        int cursor = 0;
        for (int index = 0; index < tiers.length; index++) {
            int weight = Math.max(0, weights[index]);
            if (roll < cursor + weight) return tiers[index];
            cursor += weight;
        }
        return tiers[tiers.length - 1];
    }

    /** Memilih slot tujuan secara merata dari semua slot. */
    public static SpiritBoneSlot pickSlot(Random random) {
        SpiritBoneSlot[] slots = SpiritBoneSlot.values();
        return slots[random.nextInt(slots.length)];
    }
}
