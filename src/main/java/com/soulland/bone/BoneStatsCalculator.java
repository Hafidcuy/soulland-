package com.soulland.bone;

import java.util.EnumMap;
import java.util.Map;

/**
 * Perhitungan bonus stat dan set bonus dari tulang yang terpasang.
 *
 * <p>Kelas ini murni: tidak menyentuh Minecraft, capability, atau registry.
 * Semua masukan lewat {@link Map} slot ke tier, sehingga bisa diuji unit
 * tanpa bootup game.
 */
public final class BoneStatsCalculator {
    private BoneStatsCalculator() {}

    /**
     * Total bonus stat dari semua tulang terpasang.
     *
     * @param equipped isi slot dengan tier tulang yang terpasang di slot itu
     */
    public static BoneStats aggregate(Map<SpiritBoneSlot, SpiritBoneTier> equipped) {
        if (equipped == null || equipped.isEmpty()) return BoneStats.EMPTY;
        BoneStats total = BoneStats.EMPTY;
        for (Map.Entry<SpiritBoneSlot, SpiritBoneTier> entry : equipped.entrySet()) {
            total = total.plus(BoneStats.of(entry.getValue(), entry.getKey()));
        }
        return total;
    }

    /** Jumlah tulang terpasang per tier. */
    public static Map<SpiritBoneTier, Integer> countByTier(Map<SpiritBoneSlot, SpiritBoneTier> equipped) {
        Map<SpiritBoneTier, Integer> counts = new EnumMap<>(SpiritBoneTier.class);
        if (equipped == null) return counts;
        for (SpiritBoneTier tier : equipped.values()) {
            if (tier == null) continue;
            counts.merge(tier, 1, Integer::sum);
        }
        return counts;
    }

    /**
     * Bonus set yang aktif.
     *
     * <p>Set aktif saat sebuah tier mencapai empat tulang. Bonus puncak aktif
     * saat tier yang sama mencapai enam tulang. Karena tujuh slot, tidak
     * mungkin ada dua tier aktif bersamaan.
     */
    public static BoneSetBonus setBonus(Map<SpiritBoneSlot, SpiritBoneTier> equipped) {
        Map<SpiritBoneTier, Integer> counts = countByTier(equipped);
        BoneSetBonus best = BoneSetBonus.NONE;
        for (Map.Entry<SpiritBoneTier, Integer> entry : counts.entrySet()) {
            SpiritBoneTier tier = entry.getKey();
            int count = entry.getValue();
            if (count < SpiritBoneTier.SET_THRESHOLD) continue;

            SpiritBoneTier.SetBonus set = tier.setBonus();
            boolean peak = count >= SpiritBoneTier.PEAK_THRESHOLD;
            SpiritBoneTier.PeakBonus peakBonus = peak ? tier.peakBonus() : null;

            BoneSetBonus candidate = new BoneSetBonus(
                    tier,
                    count,
                    peak,
                    set.attackPercent() + (peakBonus == null ? 0f : peakBonus.attackPercent()),
                    set.defensePercent() + (peakBonus == null ? 0f : peakBonus.defensePercent()),
                    set.spiritRegenPercent(),
                    set.energy() + (peakBonus == null ? 0 : peakBonus.energy()),
                    set.regen() + (peakBonus == null ? 0f : peakBonus.regen()));
            if (!best.active() || count > best.count()) best = candidate;
        }
        return best;
    }
}
