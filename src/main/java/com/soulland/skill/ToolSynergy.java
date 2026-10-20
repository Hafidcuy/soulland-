package com.soulland.skill;

import com.soulland.cultivation.CultivationCapability;
import com.soulland.cultivation.ICultivation;
import com.soulland.item.SpiritToolItem;
import com.soulland.item.ToolMath;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * Tool-Soul Synergy: tool milik soul sendiri memperkuat skill.
 *
 * <p>Semua pemeriksaan dilakukan di server. Klien tidak pernah menentukan
 * bonusnya sendiri.
 */
public final class ToolSynergy {
    private ToolSynergy() {}

    /** Bonus damage skill saat memakai tool milik soul sendiri. */
    public static final float DAMAGE_MULTIPLIER = ToolMath.SYNERGY_DAMAGE_MULTIPLIER;
    /** Pengurangan cooldown skill saat memakai tool milik soul sendiri. */
    public static final float COOLDOWN_MULTIPLIER = ToolMath.SYNERGY_COOLDOWN_MULTIPLIER;

    /** True bila tool di tangan utama milik soul yang sama dengan martial soul pemain. */
    public static boolean isActive(ServerPlayer player) {
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof SpiritToolItem tool)) return false;
        String soulId = CultivationCapability.get(player)
                .map(ICultivation::getMartialSoulId)
                .orElse("");
        return !soulId.isEmpty() && tool.soulId().equals(soulId);
    }

    public static float damageMultiplier(ServerPlayer player) {
        return isActive(player) ? DAMAGE_MULTIPLIER : 1.0f;
    }

    public static float cooldownMultiplier(ServerPlayer player) {
        return isActive(player) ? COOLDOWN_MULTIPLIER : 1.0f;
    }

    /** Damage skill setelah multiplier synergistic, dipakai SkillHandler. */
    public static float scaleDamage(float damage, ServerPlayer player) {
        return ToolMath.synergyDamage(damage, isActive(player));
    }

    /** Cooldown skill setelah multiplier synergistic, dipakai SkillHandler. */
    public static long scaleCooldown(long cooldownTicks, ServerPlayer player) {
        return ToolMath.synergyCooldown(cooldownTicks, isActive(player));
    }
}
