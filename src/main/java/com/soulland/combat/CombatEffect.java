package com.soulland.combat;

/**
 * Kosakata efek yang dipakai True Body dan Domain.
 *
 * <p>Enum ini sengaja tidak menyentuh kelas Minecraft supaya seluruh data
 * True Body dan Domain bisa diuji tanpa bootup registry. Pemetaan ke
 * {@code MobEffects} milik Minecraft dilakukan terpisah di
 * {@link CombatEffects}.
 */
public enum CombatEffect {

    /** Kekuatan serangan naik. */
    STRENGTH("DAMAGE_BOOST"),

    /** Damage yang diterima turun. */
    RESISTANCE("DAMAGE_RESISTANCE"),

    /** Kecepatan gerak naik. */
    SPEED("MOVEMENT_SPEED"),

    /** Darah pulih sendiri. */
    REGENERATION("REGENERATION"),

    /** Perisai sementara dari damage. */
    ABSORPTION("ABSORPTION"),

    /** Kebal terhadap api. */
    FIRE_RESISTANCE("FIRE_RESISTANCE"),

    /** Racun yang menumpuk. */
    POISON("POISON"),

    /** Serangan korban melemah. */
    WEAKNESS("WEAKNESS"),

    /** Gerak korban melambat. */
    SLOWDOWN("MOVEMENT_SLOWDOWN"),

    /** Penglihatan korban kabur. */
    BLINDNESS("BLINDNESS"),

    /** Arah gerak korban kacau. */
    CONFUSION("CONFUSION"),

    /** Target terlihat oleh semua orang. */
    GLOWING("GLOWING"),

    /** Laparan belakang. */
    HUNGER("HUNGER"),

    /** Melayang tanpa menyentuh tanah. */
    LEVITATION("LEVITATION"),

    /** Penglihatan berkurang drastis. */
    DARKNESS("DARKNESS"),

    /** Terlihat, tapi tertembus seperti bayangan. */
    INVISIBILITY("INVISIBILITY"),

    /** HP maksimum naik sementara. */
    HEALTH_BOOST("HEALTH_BOOST"),

    /** Menggali dan menambang lebih cepat. */
    HASTE("DIG_SPEED");

    private final String minecraftId;

    CombatEffect(String minecraftId) {
        this.minecraftId = minecraftId;
    }

    /** Id efek di registry Minecraft, dipakai untuk pencocokan sisi server. */
    public String minecraftId() {
        return minecraftId;
    }
}
