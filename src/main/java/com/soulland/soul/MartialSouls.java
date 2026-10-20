package com.soulland.soul;

import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Daftar seluruh martial soul yang bisa didapat lewat gacha saat login pertama. */
public final class MartialSouls {
    private MartialSouls() {}

    private static final Map<String, MartialSoul> REGISTRY = new LinkedHashMap<>();

    private static void register(MartialSoul soul) {
        REGISTRY.put(soul.id(), soul);
    }

    public static MartialSoul get(String id) {
        return REGISTRY.get(id);
    }

    public static List<MartialSoul> all() {
        return new ArrayList<>(REGISTRY.values());
    }

    /** Gacha: martial soul diundi secara acak dengan bobot rarity, seperti takdir saat lahir di cerita. */
    public static MartialSoul roll(RandomSource random) {
        int total = 0;
        for (MartialSoul soul : REGISTRY.values()) total += soul.weight();
        int r = random.nextInt(total);
        for (MartialSoul soul : REGISTRY.values()) {
            r -= soul.weight();
            if (r < 0) return soul;
        }
        return REGISTRY.values().iterator().next();
    }

    static {
        register(new MartialSoul(
                "hoe", "Cangkul Tua", "Petani biasa", "Alat", "Common", 40,
                "Martial soul rakyat jelata. Tapi di tangan yang tekun, cangkul pun bisa menembus langit.",
                List.of(
                        new SkillDef("hoe_heal", "Panen Raya", "Memulihkan 6 HP dan memberi efek saturation.", 20f, 200, 1),
                        new SkillDef("hoe_slam", "Bajak Bumi", "Hantaman AoE kecil ke musuh di sekitar.", 30f, 300, 3)
                )));

        register(new MartialSoul(
                "blue_silver_grass", "Blue Silver Grass", "Tang San", "Kontrol", "Rare", 12,
                "Rumput biru perak yang tampak lemah namun bisa mengikat dan meracuni segalanya.",
                List.of(
                        new SkillDef("bsg_entangle", "Blue Silver Entangle", "Akar biru mengikat musuh di sekitar: slow berat + lemah.", 25f, 240, 1),
                        new SkillDef("bsg_parasite", "Parasitic Seed", "Menanam benih parasit pada musuh terdekat: racun 8 detik, kamu pulih.", 35f, 400, 3)
                )));

        register(new MartialSoul(
                "hell_civet", "Hell Civet", "Zhu Zhuqing", "Agility", "Uncommon", 20,
                "Kucing neraka yang gesit: serangan cepat dan mematikan dari bayangan.",
                List.of(
                        new SkillDef("civet_dash", "Nether Thrust", "Melesat ke depan dan melukai musuh yang dilewati.", 25f, 240, 1),
                        new SkillDef("civet_slash", "Nether Slash", "Tebasan AoE cepat di sekitarmu + kecepatan gerak.", 35f, 360, 3)
                )));

        register(new MartialSoul(
                "sausage", "Sosis Ajaib", "Oscar", "Support", "Uncommon", 20,
                "Sosis buatan Food System Master: penyembuh dan pembersih terbaik di medan perang.",
                List.of(
                        new SkillDef("sausage_heal", "Recovery Sausage", "Memulihkan 8 HP dan memberi saturation penuh.", 25f, 240, 1),
                        new SkillDef("sausage_fly", "Flying Mushroom", "Terbang melayang + kecepatan gerak selama 20 detik.", 40f, 600, 3)
                )));

        register(new MartialSoul(
                "seven_treasure_pagoda", "Seven Treasure Glazed Tile Pagoda", "Ning Rongrong", "Support", "Epic", 6,
                "Pagoda tujuh lantai: martial soul support terkuat, memperkuat seluruh tim.",
                List.of(
                        new SkillDef("pagoda_power", "Berkah Kekuatan", "Sekutu di sekitarmu dapat Strength II & Absorption 30 detik.", 40f, 600, 1),
                        new SkillDef("pagoda_heal", "Berkah Kehidupan", "Menyembuhkan 10 HP sekutu di sekitarmu + Regeneration.", 50f, 800, 3)
                )));

        register(new MartialSoul(
                "evil_fire_phoenix", "Evil Fire Phoenix", "Ma Hongjun", "Serangan Api", "Epic", 6,
                "Phoenix api jahat: membakar segalanya dengan api yang tak pernah padam.",
                List.of(
                        new SkillDef("phoenix_line", "Phoenix Fireline", "Semburan api kerucut ke arah pandanganmu.", 30f, 300, 1),
                        new SkillDef("phoenix_meteor", "Phoenix Meteor", "Hujan bola api menghantam area di depanmu.", 55f, 700, 3)
                )));

        register(new MartialSoul(
                "white_tiger", "White Tiger", "Dai Mubai", "Power", "Rare", 10,
                "Harimau putih berekor: kekuatan fisik dan pertahanan yang brutal.",
                List.of(
                        new SkillDef("tiger_wave", "White Tiger Light Wave", "Gelombang cahaya harimau: damage garis lurus + knockback.", 30f, 300, 1),
                        new SkillDef("tiger_vajra", "Vajra Transformation", "Transformasi: Strength II + Resistance II selama 30 detik.", 45f, 600, 3)
                )));

        register(new MartialSoul(
                "soft_bone_rabbit", "Soft Bone Rabbit", "Xiao Wu", "Agility", "Rare", 10,
                "Kelinci tulang lunak: pesona memikat dan gerakan secepat kilat.",
                List.of(
                        new SkillDef("rabbit_charm", "Charm", "Memikat monster di sekitar: mereka berhenti menyerangmu.", 30f, 400, 1),
                        new SkillDef("rabbit_blink", "Teleport", "Berkedip 8 blok ke arah pandanganmu.", 25f, 200, 3)
                )));

        register(new MartialSoul(
                "jade_serpent", "Jade Phosphor Serpent", "Dugu Yan", "Racun", "Rare", 10,
                "Ular giok beracun: kabut dan gigitan yang melumpuhkan musuh perlahan.",
                List.of(
                        new SkillDef("serpent_mist", "Poison Mist", "Kabut racun AoE: poison + slow 10 detik.", 30f, 360, 1),
                        new SkillDef("serpent_bite", "Serpent Bite", "Gigitan berbisa pada musuh terdekat: damage besar + poison kuat.", 40f, 400, 3)
                )));

        register(new MartialSoul(
                "clear_sky_hammer", "Clear Sky Hammer", "Tang San / Tang Hao", "Power", "Legendary", 3,
                "Palu Langit Jernih: martial soul legendaris klan Clear Sky, penghancur segalanya.",
                List.of(
                        new SkillDef("hammer_slam", "Hammer Slam", "Hantaman palu raksasa: damage AoE masif + knockback ke atas.", 45f, 500, 1),
                        new SkillDef("hammer_cloak", "Chaotic Cloak", "Teknik palu berputar: Strength III + Speed II selama 20 detik.", 60f, 900, 3)
                )));
    }
}
