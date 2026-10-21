package com.soulland.combat;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;

/**
 * Pemetaan kosakata efek combat ke efek Minecraft.
 *
 * <p>Enum {@link CombatEffect} sengaja bebas dari kelas Minecraft supaya data
 * True Body dan Domain bisa diuji tanpa bootup registry. Kelas ini yang
 * menjembataninya ke {@code MobEffects} saat server menerapkan efek.
 */
public final class CombatEffects {
    private CombatEffects() {}

    /** Efek Minecraft untuk sebuah kosakata combat, atau null kalau tidak ada. */
    public static MobEffect of(CombatEffect effect) {
        if (effect == null) return null;
        return switch (effect) {
            case STRENGTH -> MobEffects.DAMAGE_BOOST;
            case RESISTANCE -> MobEffects.DAMAGE_RESISTANCE;
            case SPEED -> MobEffects.MOVEMENT_SPEED;
            case REGENERATION -> MobEffects.REGENERATION;
            case ABSORPTION -> MobEffects.ABSORPTION;
            case FIRE_RESISTANCE -> MobEffects.FIRE_RESISTANCE;
            case POISON -> MobEffects.POISON;
            case WEAKNESS -> MobEffects.WEAKNESS;
            case SLOWDOWN -> MobEffects.MOVEMENT_SLOWDOWN;
            case BLINDNESS -> MobEffects.BLINDNESS;
            case CONFUSION -> MobEffects.CONFUSION;
            case GLOWING -> MobEffects.GLOWING;
            case HUNGER -> MobEffects.HUNGER;
            case LEVITATION -> MobEffects.LEVITATION;
            case DARKNESS -> MobEffects.DARKNESS;
            case INVISIBILITY -> MobEffects.INVISIBILITY;
            case HEALTH_BOOST -> MobEffects.HEALTH_BOOST;
            case HASTE -> MobEffects.DIG_SPEED;
        };
    }
}
