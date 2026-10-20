package com.soulland.item;

/**
 * Aritmetika tool soul yang bebas dari kelas Minecraft.
 *
 * <p>Kelas ini sengaja tidak mengimpor apa pun dari Minecraft supaya bisa
 * diuji tanpa runtime game (lihat {@code src/test/java/com/soulland/item}).
 * Semua angka di sini punya satu sumber kebenaran, dipakai bersama oleh
 * {@link ToolCharge} dan {@link com.soulland.skill.ToolSynergy}.
 */
public final class ToolMath {
    private ToolMath() {}

    /** Charge minimum agar serangan bermuatan boleh dilepas. */
    public static final int MIN_CHARGE = 20;
    /** Charge maksimum. */
    public static final int MAX_CHARGE = 100;
    /** Baterai maksimum tool Spirit Silver. */
    public static final int BATTERY_MAX = 100;
    /** Biaya battery untuk serangan bermuatan penuh. */
    public static final int BATTERY_COST = 25;
    /** Spirit power pemain yang dipakai tool mutu di bawah Spirit Silver. */
    public static final int ENERGY_COST = 20;
    /** Bonus damage skill saat memakai tool milik soul sendiri. */
    public static final float SYNERGY_DAMAGE_MULTIPLIER = 1.15f;
    /** Pengurangan cooldown skill saat memakai tool milik soul sendiri. */
    public static final float SYNERGY_COOLDOWN_MULTIPLIER = 0.9f;

    /**
     * Rasio charge terhadap charge maksimum, dibatasi 0 sampai 1.
     *
     * <p>Ambang minimum charge ditangani terpisah oleh
     * {@link #chargeEnough(int)}, jadi rasio tidak perlu floor sendiri.
     */
    public static float chargeRatio(int charge) {
        return Math.max(0f, Math.min(1f, charge / (float) MAX_CHARGE));
    }

    /** Biaya battery untuk sebuah serangan bermuatan. */
    public static int batteryCost(int charge) {
        return Math.max(1, Math.round(charge / (float) MAX_CHARGE * BATTERY_COST));
    }

    /**
     * Damage serangan bermuatan.
     *
     * @param baseDamage damage dasar tool termasuk bonus mutu
     * @param charge     charge yang terkumpul, 0 sampai 100
     */
    public static float chargedDamage(float baseDamage, int charge) {
        return baseDamage * (1f + 1.5f * chargeRatio(charge));
    }

    /** Damage skill setelah multiplier synergistic. */
    public static float synergyDamage(float damage, boolean synergyActive) {
        return synergyActive ? damage * SYNERGY_DAMAGE_MULTIPLIER : damage;
    }

    /** Cooldown skill setelah multiplier synergistic. */
    public static long synergyCooldown(long cooldownTicks, boolean synergyActive) {
        if (!synergyActive) return cooldownTicks;
        return Math.round(cooldownTicks * SYNERGY_COOLDOWN_MULTIPLIER);
    }

    /** True bila charge cukup untuk melepas serangan bermuatan. */
    public static boolean chargeEnough(int charge) {
        return charge >= MIN_CHARGE;
    }
}
