package com.soulland.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Uji aritmetika tool soul tanpa runtime Minecraft.
 *
 * <p>Kelas yang diuji ({@link ToolMath}) tidak mengimpor kelas Minecraft,
 * jadi test berjalan tanpa perlu bootup registry.
 */
class ToolMathTest {

    @Test
    @DisplayName("Charge minimum 20, di bawah itu serangan bermuatan ditolak")
    void chargeThreshold() {
        assertFalse(ToolMath.chargeEnough(0));
        assertFalse(ToolMath.chargeEnough(19));
        assertTrue(ToolMath.chargeEnough(20));
        assertTrue(ToolMath.chargeEnough(100));
    }

    @Test
    @DisplayName("Rasio charge dibatasi 0 sampai 1")
    void chargeRatioClamped() {
        assertEquals(0f, ToolMath.chargeRatio(0), 0.0001f);
        assertEquals(0.5f, ToolMath.chargeRatio(50), 0.0001f);
        assertEquals(1.0f, ToolMath.chargeRatio(100), 0.0001f);
        assertEquals(1.0f, ToolMath.chargeRatio(140), 0.0001f);
    }

    @Test
    @DisplayName("Biaya battery mengikuti charge, minimal satu")
    void batteryCostScales() {
        assertEquals(1, ToolMath.batteryCost(0));
        assertEquals(13, ToolMath.batteryCost(50));
        assertEquals(25, ToolMath.batteryCost(100));
    }

    @Test
    @DisplayName("Damage bermuatan naik 1.5 kali rasio charge")
    void chargedDamageScales() {
        assertEquals(7f, ToolMath.chargedDamage(7f, 0), 0.0001f);
        assertEquals(12.25f, ToolMath.chargedDamage(7f, 50), 0.0001f);
        assertEquals(17.5f, ToolMath.chargedDamage(7f, 100), 0.0001f);
        assertEquals(9.1f, ToolMath.chargedDamage(7f, 20), 0.0001f);
    }

    @Test
    @DisplayName("Sinergi menaikkan damage 15 persen dan menurunkan cooldown 10 persen")
    void synergyMultipliers() {
        assertEquals(11.5f, ToolMath.synergyDamage(10f, true), 0.0001f);
        assertEquals(10f, ToolMath.synergyDamage(10f, false), 0.0001f);
        assertEquals(90L, ToolMath.synergyCooldown(100L, true));
        assertEquals(100L, ToolMath.synergyCooldown(100L, false));
    }
}
