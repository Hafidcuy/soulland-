package com.soulland.bone;

import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Memensure agregasi stat tulang dan aturan set bonus benar. */
class BoneStatsCalculatorTest {

    private static Map<SpiritBoneSlot, SpiritBoneTier> withBones(SpiritBoneTier tier,
                                                                 SpiritBoneSlot... slots) {
        Map<SpiritBoneSlot, SpiritBoneTier> equipped = new EnumMap<>(SpiritBoneSlot.class);
        for (SpiritBoneSlot slot : slots) equipped.put(slot, tier);
        return equipped;
    }

    @Test
    void tanpaTulangTidakAdaBonus() {
        assertTrue(BoneStatsCalculator.aggregate(null).isEmpty());
        assertTrue(BoneStatsCalculator.aggregate(new EnumMap<>(SpiritBoneSlot.class)).isEmpty());
        assertSame(BoneStats.EMPTY, BoneStatsCalculator.aggregate(null));
    }

    @Test
    void statTulangSamaDenganTierDikaliPengaliSlot() {
        BoneStats stats = BoneStatsCalculator.aggregate(
                withBones(SpiritBoneTier.Y1000, SpiritBoneSlot.SKULL));
        assertEquals(2.0f * 1.5f, stats.attack(), 0.0001f);
        assertEquals(2.0f * 1.2f, stats.defense(), 0.0001f);
        assertEquals(10 * 1.4f, stats.energy(), 0.0001f);
        assertEquals(0.5f * 1.0f, stats.regen(), 0.0001f);
    }

    @Test
    void jumlahTulangMenambahStatLinear() {
        BoneStats satu = BoneStatsCalculator.aggregate(
                withBones(SpiritBoneTier.Y100, SpiritBoneSlot.SKULL));
        BoneStats dua = BoneStatsCalculator.aggregate(
                withBones(SpiritBoneTier.Y100, SpiritBoneSlot.SKULL, SpiritBoneSlot.TORSO));
        // Tengkorak memberi 1.5 serangan, rongga dada 1.0, jadi totalnya 2.5.
        assertEquals(1.5f, satu.attack(), 0.0001f);
        assertEquals(2.5f, dua.attack(), 0.0001f);
        assertTrue(dua.attack() > satu.attack());
    }

    @Test
    void tierLebihTuaSelaluLebihKuat() {
        BoneStats muda = BoneStatsCalculator.aggregate(
                withBones(SpiritBoneTier.Y100, SpiritBoneSlot.TORSO));
        BoneStats tua = BoneStatsCalculator.aggregate(
                withBones(SpiritBoneTier.Y1000000, SpiritBoneSlot.TORSO));
        assertTrue(tua.attack() > muda.attack());
        assertTrue(tua.defense() > muda.defense());
        assertTrue(tua.energy() > muda.energy());
        assertTrue(tua.regen() > muda.regen());
    }

    @Test
    void jumlahTulangPerTierTerhitungBenar() {
        Map<SpiritBoneTier, Integer> counts = BoneStatsCalculator.countByTier(
                withBones(SpiritBoneTier.Y100, SpiritBoneSlot.SKULL, SpiritBoneSlot.TORSO));
        assertEquals(2, counts.get(SpiritBoneTier.Y100));
        assertEquals(1, counts.size());
    }

    @Test
    void setBonusAktifPadaEmpatTulangSatuTier() {
        BoneSetBonus set = BoneStatsCalculator.setBonus(
                withBones(SpiritBoneTier.Y1000, SpiritBoneSlot.SKULL, SpiritBoneSlot.TORSO,
                        SpiritBoneSlot.LEFT_ARM, SpiritBoneSlot.RIGHT_ARM));
        assertTrue(set.active());
        assertFalse(set.peak());
        assertEquals(SpiritBoneTier.Y1000, set.tier());
        assertEquals(4, set.count());
        assertEquals(10f, set.attackPercent(), 0.0001f);
        assertEquals(25f, set.spiritRegenPercent(), 0.0001f);
    }

    @Test
    void setBonusBelumAktifPadaTigaTulang() {
        BoneSetBonus set = BoneStatsCalculator.setBonus(
                withBones(SpiritBoneTier.Y1000, SpiritBoneSlot.SKULL, SpiritBoneSlot.TORSO,
                        SpiritBoneSlot.LEFT_ARM));
        assertFalse(set.active());
        assertEquals(0, set.count());
    }

    @Test
    void bonusPuncakAktifPadaEnamTulang() {
        BoneSetBonus set = BoneStatsCalculator.setBonus(
                withBones(SpiritBoneTier.Y100, SpiritBoneSlot.SKULL, SpiritBoneSlot.TORSO,
                        SpiritBoneSlot.LEFT_ARM, SpiritBoneSlot.RIGHT_ARM,
                        SpiritBoneSlot.LEFT_LEG, SpiritBoneSlot.RIGHT_LEG));
        assertTrue(set.active());
        assertTrue(set.peak());
        assertEquals(6, set.count());
        // Set 4+ memberi +5% defense, puncak 6+ menambah +5% attack.
        assertEquals(5f, set.defensePercent(), 0.0001f);
        assertEquals(5f, set.attackPercent(), 0.0001f);
    }

    @Test
    void hanyaSatuTierYangBisaAktifBersamaan() {
        BoneSetBonus set = BoneStatsCalculator.setBonus(
                withBones(SpiritBoneTier.Y1000, SpiritBoneSlot.SKULL, SpiritBoneSlot.TORSO,
                        SpiritBoneSlot.LEFT_ARM, SpiritBoneSlot.RIGHT_ARM));
        assertTrue(set.active());
        assertEquals(SpiritBoneTier.Y1000, set.tier());
        assertEquals(4, set.count());
    }

    @Test
    void hitunganTiapTierMenentukanTierYangAktif() {
        Map<SpiritBoneSlot, SpiritBoneTier> equipped = new HashMap<>();
        equipped.put(SpiritBoneSlot.SKULL, SpiritBoneTier.Y100);
        equipped.put(SpiritBoneSlot.TORSO, SpiritBoneTier.Y100);
        equipped.put(SpiritBoneSlot.LEFT_ARM, SpiritBoneTier.Y100);
        equipped.put(SpiritBoneSlot.RIGHT_ARM, SpiritBoneTier.Y100);
        equipped.put(SpiritBoneSlot.LEFT_LEG, SpiritBoneTier.Y1000000);
        BoneSetBonus set = BoneStatsCalculator.setBonus(equipped);
        assertTrue(set.active());
        assertEquals(SpiritBoneTier.Y100, set.tier());
    }

    @Test
    void statTulangKosongTidakMenggangguAgregat() {
        Map<SpiritBoneSlot, SpiritBoneTier> equipped = new EnumMap<>(SpiritBoneSlot.class);
        equipped.put(SpiritBoneSlot.SKULL, SpiritBoneTier.Y100);
        equipped.put(SpiritBoneSlot.TORSO, null);
        BoneStats stats = BoneStatsCalculator.aggregate(equipped);
        assertEquals(1.5f, stats.attack(), 0.0001f);
    }
}
