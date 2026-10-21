package com.soulland.bone;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Memensure aturan peluang drop tulang spirit: skala ring, plafon, dan bobot tier. */
class BoneDropLogicTest {

    private static final double DELTA = 0.0000001;

    @Test
    void peluangMobNaikSeiringJumlahSpiritRing() {
        double tanpaRing = BoneDropLogic.hostileChance(0.004, 0.35, 0.08, 0);
        double satuRing = BoneDropLogic.hostileChance(0.004, 0.35, 0.08, 1);
        double banyakRing = BoneDropLogic.hostileChance(0.004, 0.35, 0.08, 5);
        assertTrue(tanpaRing < satuRing, "peluang harus naik bersama ring");
        assertTrue(satuRing < banyakRing, "peluang harus naik bersama ring");
        assertEquals(0.004, tanpaRing, DELTA);
        assertEquals(0.004 * 1.35, satuRing, DELTA);
    }

    @Test
    void peluangMobDibatasiPlafon() {
        double capped = BoneDropLogic.hostileChance(0.004, 0.35, 0.08, 900);
        assertEquals(0.08, capped, DELTA);
    }

    @Test
    void peluangNolSelaluNolDanJumlahRingNegatifDiabaikan() {
        assertEquals(0.0, BoneDropLogic.hostileChance(0.0, 0.35, 0.08, 5), DELTA);
        assertEquals(0.0, BoneDropLogic.hostileChance(0.004, 0.35, 0.0, 5), DELTA);
        assertEquals(0.004, BoneDropLogic.hostileChance(0.004, 0.35, 0.08, -3), DELTA);
    }

    @Test
    void peluangBossTetapDanTidakIkutSkalaRing() {
        assertEquals(0.85, BoneDropLogic.bossChance(0.85), DELTA);
        assertEquals(0.0, BoneDropLogic.bossChance(0.0), DELTA);
        assertEquals(1.0, BoneDropLogic.bossChance(1.4), DELTA);
    }

    @Test
    void undianSelaluBerhasilPadaPeluangPenuhDanSelaluGagalPadaPeluangNol() {
        Random random = new Random(7);
        assertTrue(BoneDropLogic.roll(1.0, random));
        assertFalse(BoneDropLogic.roll(0.0, random));
        assertFalse(BoneDropLogic.roll(-0.5, random));
    }

    @Test
    void undianMengikutiPeluangSekitarNilaiYangDiminta() {
        Random random = new Random(11);
        int sukses = 0;
        int percobaan = 20000;
        for (int i = 0; i < percobaan; i++) {
            if (BoneDropLogic.roll(0.25, random)) sukses++;
        }
        double hasil = (double) sukses / percobaan;
        assertTrue(hasil > 0.22 && hasil < 0.28, "hasil undian di luar toleransi: " + hasil);
    }

    @Test
    void tierDipilihMengikutiBobot() {
        int[] bobot = {70, 25, 5, 0, 0};
        Random random = new Random(3);
        Map<SpiritBoneTier, Integer> hitungan = new HashMap<>();
        for (int i = 0; i < 20000; i++) {
            SpiritBoneTier tier = BoneDropLogic.pickTier(bobot, random);
            hitungan.merge(tier, 1, Integer::sum);
        }
        assertEquals(20000, hitungan.values().stream().mapToInt(Integer::intValue).sum());
        assertTrue(hitungan.get(SpiritBoneTier.Y100) > hitungan.get(SpiritBoneTier.Y1000));
        assertTrue(hitungan.get(SpiritBoneTier.Y1000) > hitungan.get(SpiritBoneTier.Y10000));
        assertEquals(0, hitungan.getOrDefault(SpiritBoneTier.Y100000, 0));
        assertEquals(0, hitungan.getOrDefault(SpiritBoneTier.Y1000000, 0));
    }

    @Test
    void bobotTidakValidJatuhKeTierTerendah() {
        Random random = new Random(5);
        assertSame(SpiritBoneTier.Y100, BoneDropLogic.pickTier(new int[]{0, 0, 0, 0, 0}, random));
        assertSame(SpiritBoneTier.Y100, BoneDropLogic.pickTier(new int[]{1, 2}, random));
        assertSame(SpiritBoneTier.Y100, BoneDropLogic.pickTier(null, random));
    }

    @Test
    void bobotBossMemberiTierTinggiSaja() {
        int[] bobot = {0, 0, 20, 50, 30};
        Random random = new Random(13);
        for (int i = 0; i < 5000; i++) {
            SpiritBoneTier tier = BoneDropLogic.pickTier(bobot, random);
            assertTrue(tier == SpiritBoneTier.Y10000
                    || tier == SpiritBoneTier.Y100000
                    || tier == SpiritBoneTier.Y1000000, "tier tak terduga: " + tier.id());
        }
    }

    @Test
    void slotTujuanDipilihMerataDariTujuhSlot() {
        Random random = new Random(17);
        Map<SpiritBoneSlot, Integer> hitungan = new HashMap<>();
        for (int i = 0; i < 7000; i++) {
            hitungan.merge(BoneDropLogic.pickSlot(random), 1, Integer::sum);
        }
        assertEquals(7, hitungan.size());
        for (Integer jumlah : hitungan.values()) {
            assertTrue(jumlah > 700, "satu slot mengambil hampir semua drop: " + jumlah);
        }
    }
}
