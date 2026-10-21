package com.soulland.bone;

/**
 * Bonus set yang sedang aktif dari tulang-tulang terpasang.
 *
 * <p>Karena hanya ada tujuh slot, dua tier tidak mungkin sama-sama mencapai
 * empat tulang. Jadi paling banyak satu set aktif, dan satu lagi bonus
 * puncak kalau slotnya hampir penuh.
 */
public record BoneSetBonus(SpiritBoneTier tier, int count, boolean peak,
                           float attackPercent, float defensePercent, float spiritRegenPercent,
                           int energy, float regen) {

    /** Tidak ada set yang aktif. */
    public static final BoneSetBonus NONE =
            new BoneSetBonus(null, 0, false, 0f, 0f, 0f, 0, 0f);

    /** True kalau ada set aktif. */
    public boolean active() {
        return tier != null;
    }

    /** Bonus set dan puncak digabung menjadi satu nilai, siap dipakai. */
    public BoneSetBonus plus(BoneSetBonus other) {
        if (other == null || !other.active()) return this;
        if (!active()) return other;
        return new BoneSetBonus(
                tier,
                Math.max(count, other.count),
                peak || other.peak,
                attackPercent + other.attackPercent,
                defensePercent + other.defensePercent,
                spiritRegenPercent + other.spiritRegenPercent,
                energy + other.energy,
                regen + other.regen);
    }
}
