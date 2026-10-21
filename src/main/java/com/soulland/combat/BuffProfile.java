package com.soulland.combat;

/**
 * Profil buff: satu efek beserta tingkatnya.
 *
 * <p>{@code effect} boleh null. Itu berarti slot itu memang tidak memberi
 * efek apa pun, bukan efek yang belum diisi. {@link #none()} dipakai supaya
 * pemanggil tidak perlu membuat {@code null} sendiri.
 */
public record BuffProfile(CombatEffect effect, int amplifier) {

    /** Profil tanpa efek. */
    public static BuffProfile none() {
        return new BuffProfile(null, 0);
    }

    /** Profil dengan efek tertentu. */
    public static BuffProfile of(CombatEffect effect, int amplifier) {
        return new BuffProfile(effect, Math.max(0, amplifier));
    }

    /** True kalau profil ini memang membawa efek. */
    public boolean present() {
        return effect != null;
    }
}
