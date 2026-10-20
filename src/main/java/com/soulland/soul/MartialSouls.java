package com.soulland.soul;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Daftar seluruh martial soul yang bisa dipilih saat Upacara Kebangkitan.
 * Setiap soul punya 4 skill: slot 1 (1 ring), slot 2 (3 ring), slot 3 (5 ring),
 * slot 4 (7 ring, ultimate).
 */
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

    static {
        register(new MartialSoul(
                "hoe", "Cangkul Tua", "Petani biasa", "Alat", "Common",
                "Martial soul rakyat jelata. Tapi di tangan yang tekun, cangkul pun bisa menembus langit.",
                List.of(
                        new SkillDef("hoe_heal", "Panen Raya", "Memulihkan 6 HP dan memberi efek saturation.", 20f, 200, 1),
                        new SkillDef("hoe_slam", "Bajak Bumi", "Hantaman AoE kecil ke musuh di sekitar.", 30f, 300, 3),
                        new SkillDef("hoe_harvest", "Bajak Ladang", "Cangkul membasahi tanah: hantaman AoE radius 5 dan pulihkan 3 HP.", 45f, 600, 5),
                        new SkillDef("hoe_season", "Musim Panen", "Musim panen tiba: hantaman AoE radius 8, sembuhkan sekutu 8 HP, dan beri Absorption 30 detik.", 80f, 1200, 7)
                )));

        register(new MartialSoul(
                "blue_silver_grass", "Blue Silver Grass", "Tang San", "Kontrol", "Rare",
                "Rumput biru perak yang tampak lemah namun bisa mengikat dan meracuni segalanya.",
                List.of(
                        new SkillDef("bsg_entangle", "Blue Silver Entangle", "Akar biru mengikat musuh di sekitar: slow berat + lemah.", 25f, 240, 1),
                        new SkillDef("bsg_parasite", "Parasitic Seed", "Menanam benih parasit pada musuh terdekat: racun 8 detik, kamu pulih.", 35f, 400, 3),
                        new SkillDef("bsg_nest", "Sarang Blue Silver", "Akar mengikat tiga musuh terdekat dan menahan langkah mereka.", 50f, 650, 5),
                        new SkillDef("bsg_rain", "Hujan Blue Silver", "Hujan rumput biru menyapu area luas: racun dan pelemahan secara bersamaan.", 85f, 1300, 7)
                )));

        register(new MartialSoul(
                "hell_civet", "Hell Civet", "Zhu Zhuqing", "Agility", "Uncommon",
                "Kucing neraka yang gesit: serangan cepat dan mematikan dari bayangan.",
                List.of(
                        new SkillDef("civet_dash", "Nether Thrust", "Melesat ke depan dan melukai musuh yang dilewati.", 25f, 240, 1),
                        new SkillDef("civet_slash", "Nether Slash", "Tebasan AoE cepat di sekitarmu + kecepatan gerak.", 35f, 360, 3),
                        new SkillDef("civet_shadow", "Bayangan Melesat", "Tubuh menghilang di bayangan: dash 6 blok dan Speed II selama 5 detik.", 50f, 600, 5),
                        new SkillDef("civet_storm", "Badai Nether", "Badai bayangan menerjang: dash yang melukai musuh serta Speed III selama 8 detik.", 85f, 1200, 7)
                )));

        register(new MartialSoul(
                "sausage", "Sosis Ajaib", "Oscar", "Support", "Uncommon",
                "Sosis buatan Food System Master: penyembuh dan pembersih terbaik di medan perang.",
                List.of(
                        new SkillDef("sausage_heal", "Recovery Sausage", "Memulihkan 8 HP dan memberi saturation penuh.", 25f, 240, 1),
                        new SkillDef("sausage_fly", "Flying Mushroom", "Terbang melayang + kecepatan gerak selama 20 detik.", 40f, 600, 3),
                        new SkillDef("sausage_soup", "Sup Sosis Panas", "Sup sosis hangat: sembuhkan sekutu 8 HP dan beri Regenerasi 10 detik.", 55f, 700, 5),
                        new SkillDef("sausage_banquet", "Banquet Sosis", "Perjamuan sosis: pulihkan 15 HP dan Resistance II selama 20 detik.", 90f, 1400, 7)
                )));

        register(new MartialSoul(
                "seven_treasure_pagoda", "Seven Treasure Glazed Tile Pagoda", "Ning Rongrong", "Support", "Epic",
                "Pagoda tujuh lantai: martial soul support terkuat, memperkuat seluruh tim.",
                List.of(
                        new SkillDef("pagoda_power", "Berkah Kekuatan", "Sekutu di sekitarmu dapat Strength II dan Absorption 30 detik.", 40f, 600, 1),
                        new SkillDef("pagoda_heal", "Berkah Kehidupan", "Menyembuhkan 10 HP sekutu di sekitarmu + Regeneration.", 50f, 800, 3),
                        new SkillDef("pagoda_aegis", "Berkah Ketahanan", "Berkah pelindung: sekutu dapat Resistance II dan Speed I selama 30 detik.", 65f, 900, 5),
                        new SkillDef("pagoda_blood", "Darah Pagoda", "Darah suci pagoda mengalir: sembuhkan 12 HP dan Damage Boost III untuk sekutu.", 95f, 1600, 7)
                )));

        register(new MartialSoul(
                "evil_fire_phoenix", "Evil Fire Phoenix", "Ma Hongjun", "Serangan Api", "Epic",
                "Phoenix api jahat: membakar segalanya dengan api yang tak pernah padam.",
                List.of(
                        new SkillDef("phoenix_line", "Phoenix Fireline", "Semburan api kerucut ke arah pandanganmu.", 30f, 300, 1),
                        new SkillDef("phoenix_meteor", "Phoenix Meteor", "Hujan bola api menghantam area di depanmu.", 55f, 700, 3),
                        new SkillDef("phoenix_wings", "Sayap Api", "Sayap api membentang: cincin api melukai dan membakar musuh sekitarmu.", 65f, 900, 5),
                        new SkillDef("phoenix_ruin", "Kehancuran Phoenix", "Bola api besar meledak dan menghancurkan area yang luas.", 95f, 1700, 7)
                )));

        register(new MartialSoul(
                "white_tiger", "White Tiger", "Dai Mubai", "Power", "Rare",
                "Harimau putih berekor: kekuatan fisik dan pertahanan yang brutal.",
                List.of(
                        new SkillDef("tiger_wave", "White Tiger Light Wave", "Gelombang cahaya harimau: damage garis lurus + knockback.", 30f, 300, 1),
                        new SkillDef("tiger_vajra", "Vajra Transformation", "Transformasi: Strength II + Resistance II selama 30 detik.", 45f, 600, 3),
                        new SkillDef("tiger_claw", "Cakar Harimau", "Cakar mengunci satu target: hantaman burst besar.", 60f, 850, 5),
                        new SkillDef("tiger_roar", "Tiger Mengaum", "Auman yang stumblingkan: damage AoE dan knockback kuat ke semua musuh.", 90f, 1500, 7)
                )));

        register(new MartialSoul(
                "soft_bone_rabbit", "Soft Bone Rabbit", "Xiao Wu", "Agility", "Rare",
                "Kelinci tulang lunak: pesona memikat dan gerakan secepat kilat.",
                List.of(
                        new SkillDef("rabbit_charm", "Charm", "Memikat monster di sekitar: mereka berhenti menyerangmu.", 30f, 400, 1),
                        new SkillDef("rabbit_blink", "Teleport", "Berkedip 8 blok ke arah pandanganmu.", 25f, 200, 3),
                        new SkillDef("rabbit_hop", "Lompatan Kelinci", "Lompatan ringan: lompat tinggi dan Speed II selama 6 detik.", 55f, 700, 5),
                        new SkillDef("rabbit_sprint", "Lari Terobosan", "Lari menembus arena: teleport 12 blok dan mengacaukan musuh yang dilewati.", 90f, 1300, 7)
                )));

        register(new MartialSoul(
                "jade_serpent", "Jade Phosphor Serpent", "Dugu Yan", "Racun", "Rare",
                "Ular giok beracun: kabut dan gigitan yang melumpuhkan musuh perlahan.",
                List.of(
                        new SkillDef("serpent_mist", "Poison Mist", "Kabut racun AoE: poison + slow 10 detik.", 30f, 360, 1),
                        new SkillDef("serpent_bite", "Serpent Bite", "Gigitan berbisa pada musuh terdekat: damage besar + poison kuat.", 40f, 400, 3),
                        new SkillDef("serpent_siphon", "Sifon Racun", "Sifon racun: serap HP musuh terdekat ke tubuhmu sendiri.", 60f, 750, 5),
                        new SkillDef("serpent_death", "Kabut Maut", "Kabut maut dalam radius 10: racun dan Wither yang mengikis vitalitas.", 90f, 1400, 7)
                )));

        register(new MartialSoul(
                "clear_sky_hammer", "Clear Sky Hammer", "Tang San / Tang Hao", "Power", "Legendary",
                "Palu Langit Jernih: martial soul legendaris klan Clear Sky, penghancur segalanya.",
                List.of(
                        new SkillDef("hammer_slam", "Hammer Slam", "Hantaman palu raksasa: damage AoE masif + knockback ke atas.", 45f, 500, 1),
                        new SkillDef("hammer_cloak", "Chaotic Cloak", "Teknik palu berputar: Strength III + Speed II selama 20 detik.", 60f, 900, 3),
                        new SkillDef("hammer_thunder", "Palu Petir", "Palu memetik petir: satu target dihantam damage luar biasa.", 70f, 1000, 5),
                        new SkillDef("hammer_clear_sky", "Palu Langit Jernih", "Langit jernih runtuh: hantaman AoE masif dengan knockback ke atas.", 95f, 1800, 7)
                )));
    }
}
