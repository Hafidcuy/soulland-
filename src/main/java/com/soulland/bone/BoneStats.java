package com.soulland.bone;

/**
 * Total bonus stat dari sekumpulan tulang yang terpasang.
 *
 * <p>Nilai-nilai ini sudah memasukkan pengali slot, jadi tinggal dijumlahkan
 * lalu dipakai {@code CultivationManager.applyStats}.
 */
public record BoneStats(float attack, float defense, float energy, float regen) {

    /** Tidak ada tulang terpasang. */
    public static final BoneStats EMPTY = new BoneStats(0f, 0f, 0f, 0f);

    /** Penjumlahan dua kumpulan bonus. */
    public BoneStats plus(BoneStats other) {
        if (other == null) return this;
        return new BoneStats(
                attack + other.attack,
                defense + other.defense,
                energy + other.energy,
                regen + other.regen);
    }

    /** Bonus satu tulang pada tier dan slot tertentu. */
    public static BoneStats of(SpiritBoneTier tier, SpiritBoneSlot slot) {
        if (tier == null || slot == null) return EMPTY;
        return new BoneStats(
                tier.baseAttack() * slot.attackMultiplier(),
                tier.baseDefense() * slot.defenseMultiplier(),
                tier.baseEnergy() * slot.energyMultiplier(),
                tier.baseRegen() * slot.regenMultiplier());
    }

    /** True kalau tidak ada bonus sama sekali. */
    public boolean isEmpty() {
        return attack == 0f && defense == 0f && energy == 0f && regen == 0f;
    }
}
