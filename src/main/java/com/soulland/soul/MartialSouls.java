package com.soulland.soul;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Daftar seluruh martial soul yang bisa dipilih saat Upacara Kebangkitan.
 * Setiap soul punya 4 skill: slot 1 (1 ring), slot 2 (3 ring), slot 3 (5 ring),
 * slot 4 (7 ring, ultimate).
 *
 * <p>Registry ini kurasi: 43 soul dengan aset, visual, dan item manifestasi
 * khas masing-masing. Sebagian besar diturunkan dari canon Douluo Dalu yang
 * released ke publik, sisanya orisinal. Martial soul dipilih pemain sendiri
 * di upacara, jadi di sini tidak ada undian/gacha.
 *
 * <p>Setiap soul punya dua klasifikasi baru pada v1.4:
 * <ul>
 *   <li>{@link SoulLineage}: bentuk manifestasi (Beast, Tool, Body, Special).</li>
 *   <li>{@link Element} afinitas: unsur yang membuat soul ini beresonansi.
 *       Aturan pengali damage-nya ada di {@link SoulAffinity}.</li>
 * </ul>
 * Field {@code family} lama tetap utuh karena dipakai kode lain.
 */
public final class MartialSouls {
    private MartialSouls() {}

    private static final Map<String, MartialSoul> REGISTRY = new LinkedHashMap<>();
    private static final Map<String, SkillDef> SKILL_INDEX = new LinkedHashMap<>();

    public static MartialSoul get(String id) {
        if (id == null) return null;
        return REGISTRY.get(id);
    }

    public static List<MartialSoul> all() {
        return new ArrayList<>(REGISTRY.values());
    }

    /** Soul unggulan yang tampil di Guide Book. */
    public static List<MartialSoul> notable() {
        List<MartialSoul> result = new ArrayList<>();
        for (MartialSoul soul : REGISTRY.values()) {
            if (soul.notable()) result.add(soul);
        }
        return result;
    }

    /** Soul efektif pemain: bentuk evolusi bila tahap evolusi aktif dan evolusinya terdaftar. */
    public static MartialSoul effective(String soulId, int evolutionStage) {
        MartialSoul base = get(soulId);
        if (base == null) return null;
        if (evolutionStage > 0 && base.hasEvolution()) {
            MartialSoul evolved = get(base.evolutionId());
            if (evolved != null) return evolved;
        }
        return base;
    }

    public static SkillDef skillById(String id) {
        if (id == null) return null;
        return SKILL_INDEX.get(id);
    }

    /** Elemen tempur sebuah soul, dari nama elemen yang tercatat di datanya. */
    public static Element elementOf(MartialSoul soul) {
        if (soul == null || soul.element() == null || soul.element().isEmpty()) return Element.NONE;
        try {
            return Element.valueOf(soul.element());
        } catch (IllegalArgumentException e) {
            return Element.NONE;
        }
    }

    /** Elemen afinitas sebuah soul. Null berarti soul itu tidak beresonansi. */
    public static Element affinityOf(MartialSoul soul) {
        return soul == null ? Element.NONE : soul.affinity();
    }

    /** Lineage sebuah soul. Null berarti soul itu belum diklasifikasikan. */
    public static SoulLineage lineageOf(MartialSoul soul) {
        return soul == null ? null : soul.lineage();
    }

    /** Jumlah soul yang terdaftar, dipakai log startup. */
    public static int registeredCount() {
        return REGISTRY.size();
    }

    private static void register(MartialSoul soul) {
        REGISTRY.put(soul.id(), soul);
        for (SkillDef skill : soul.skills()) {
            SKILL_INDEX.put(skill.id(), skill);
        }
    }

    private static SkillDef skill(String id, String name, String description, float cost, int cooldown, int rings) {
        return SkillDef.builder(id, name).desc(description).cost(cost).cd(cooldown).rings(rings).build();
    }

    /**
     * Skill dengan arketipe, untuk 132 skill baru.
     *
     * <p>Angka di sini yang membedakan satu skill dari skill lain: damage,
     * heal, radius, durasi, dan amplifier. Tanda visualnya diturunkan dari
     * bentuk dan warna skill, jadi dua skill tidak pernah terlihat sama.
     */
    private static SkillDef arc(String id, String name, String description,
                                SkillArchetype archetype, float damage, float heal, double radius,
                                int duration, int amplifier,
                                float cost, int cooldown, int rings,
                                FxShape shape, int fxColor) {
        float size = (float) Math.max(2.0, Math.min(5.0, radius * 0.35));
        int count = 18 + Math.min(46, (int) (damage * 1.5f) + duration / 8);
        return SkillDef.builder(id, name)
                .desc(description)
                .archetype(archetype)
                .target(targetOf(archetype))
                .dmg(damage)
                .heal(heal)
                .radius(radius)
                .effectTicks(duration)
                .effectAmplifier(amplifier)
                .cost(cost)
                .cd(cooldown)
                .rings(rings)
                .fx(new SkillVisual(shape, fxColor, 0xFFFFFF, size, count, null, 0f, 0))
                .build();
    }

    /** Target skill according to the shape of its archetype. */
    private static SkillTarget targetOf(SkillArchetype archetype) {
        return switch (archetype) {
            case STRIKE, LIFESTEAL, MARK, EXECUTE -> SkillTarget.ENEMY_NEAREST;
            case SHIELD, BUFF, HEAL, BLINK, DASH -> SkillTarget.SELF;
            case AURA, MEND -> SkillTarget.ALLY_AURA;
            default -> SkillTarget.ENEMY_AOE;
        };
    }

    static {
        register(new MartialSoul(
                "hoe", "Cangkul Tua", "", "Petani biasa", "", "Alat", "Tool", "Common",
                SoulLineage.TOOL, Element.WATER,
                "Martial soul rakyat jelata. Tapi di tangan yang tekun, cangkul pun bisa menembus langit.",
                "EARTH", 0xB08954, "", "", true,
                List.of(
                        skill("hoe_heal", "Panen Raya", "Memulihkan 6 HP dan memberi efek saturation.", 20f, 200, 1),
                        skill("hoe_slam", "Bajak Bumi", "Hantaman AoE kecil ke musuh di sekitar.", 30f, 300, 3),
                        skill("hoe_harvest", "Bajak Ladang", "Cangkul membasahi tanah: hantaman AoE radius 5 dan pulihkan 3 HP.", 45f, 600, 5),
                        skill("hoe_season", "Musim Panen", "Musim panen tiba: hantaman AoE radius 8, sembuhkan sekutu 8 HP, dan beri Absorption 30 detik.", 80f, 1200, 7)
                )));

        register(new MartialSoul(
                "blue_silver_grass", "Blue Silver Grass", "", "Tang San", "Tang Sect", "Kontrol", "Plant", "Rare",
                SoulLineage.BODY, Element.WATER,
                "Rumput biru perak yang tampak lemah namun bisa mengikat dan meracuni segalanya.",
                "NATURE", 0x4A7BC8, "", "", true,
                List.of(
                        skill("bsg_entangle", "Blue Silver Entangle", "Akar biru mengikat musuh di sekitar: slow berat + lemah.", 25f, 240, 1),
                        skill("bsg_parasite", "Parasitic Seed", "Menanam benih parasit pada musuh terdekat: racun 8 detik, kamu pulih.", 35f, 400, 3),
                        skill("bsg_nest", "Sarang Blue Silver", "Akar mengikat tiga musuh terdekat dan menahan langkah mereka.", 50f, 650, 5),
                        skill("bsg_rain", "Hujan Blue Silver", "Hujan rumput biru menyapu area luas: racun dan pelemahan secara bersamaan.", 85f, 1300, 7)
                )));

        register(new MartialSoul(
                "hell_civet", "Hell Civet", "", "Zhu Zhuqing", "Star Luo", "Agility", "Beast", "Uncommon",
                SoulLineage.BEAST, Element.LIGHT,
                "Kucing neraka yang gesit: serangan cepat dan mematikan dari bayangan.",
                "DARK", 0x5B4A8A, "", "", true,
                List.of(
                        skill("civet_dash", "Nether Thrust", "Melesat ke depan dan melukai musuh yang dilewati.", 25f, 240, 1),
                        skill("civet_slash", "Nether Slash", "Tebasan AoE cepat di sekitarmu + kecepatan gerak.", 35f, 360, 3),
                        skill("civet_shadow", "Bayangan Melesat", "Tubuh menghilang di bayangan: dash 6 blok dan Speed II selama 5 detik.", 50f, 600, 5),
                        skill("civet_storm", "Badai Nether", "Badai bayangan menerjang: dash yang melukai musuh serta Speed III selama 8 detik.", 85f, 1200, 7)
                )));

        register(new MartialSoul(
                "sausage", "Sosis Ajaib", "", "Oscar", "Shrek", "Support", "Food", "Uncommon",
                SoulLineage.SPECIAL, Element.NATURE,
                "Sosis buatan Food System Master: penyembuh dan pembersih terbaik di medan perang.",
                "NONE", 0xE08A4C, "", "", true,
                List.of(
                        skill("sausage_heal", "Recovery Sausage", "Memulihkan 8 HP dan memberi saturation penuh.", 25f, 240, 1),
                        skill("sausage_fly", "Flying Mushroom", "Terbang melayang + kecepatan gerak selama 20 detik.", 40f, 600, 3),
                        skill("sausage_soup", "Sup Sosis Panas", "Sup sosis hangat: sembuhkan sekutu 8 HP dan beri Regenerasi 10 detik.", 55f, 700, 5),
                        skill("sausage_banquet", "Perjamuan Sosis", "Perjamuan sosis: pulihkan 15 HP dan Resistance II selama 20 detik.", 90f, 1400, 7)
                )));

        register(new MartialSoul(
                "seven_treasure_pagoda", "Seven Treasure Glazed Tile Pagoda", "", "Ning Rongrong", "Seven Treasure Glazed Tile Sect", "Support", "Tool", "Epic",
                SoulLineage.SPECIAL, Element.DARK,
                "Pagoda tujuh lantai: martial soul support terkuat, memperkuat seluruh tim.",
                "LIGHT", 0xE8C46A, "", "", true,
                List.of(
                        skill("pagoda_power", "Berkah Kekuatan", "Sekutu di sekitarmu dapat Strength II dan Absorption 30 detik.", 40f, 600, 1),
                        skill("pagoda_heal", "Berkah Kehidupan", "Menyembuhkan 10 HP sekutu di sekitarmu + Regeneration.", 50f, 800, 3),
                        skill("pagoda_aegis", "Berkah Ketahanan", "Berkah pelindung: sekutu dapat Resistance II dan Speed I selama 30 detik.", 65f, 900, 5),
                        skill("pagoda_blood", "Darah Pagoda", "Darah suci pagoda mengalir: sembuhkan 12 HP dan Damage Boost III untuk sekutu.", 95f, 1600, 7)
                )));

        register(new MartialSoul(
                "evil_fire_phoenix", "Evil Fire Phoenix", "", "Ma Hongjun", "Shrek", "Serangan Api", "Beast", "Epic",
                SoulLineage.BEAST, Element.EARTH,
                "Phoenix api jahat: membakar segalanya dengan api yang tak pernah padam.",
                "FIRE", 0xFF6A3C, "", "", true,
                List.of(
                        skill("phoenix_line", "Phoenix Fireline", "Semburan api kerucut ke arah pandanganmu.", 30f, 300, 1),
                        skill("phoenix_meteor", "Phoenix Meteor", "Hujan bola api menghantam area di depanmu.", 55f, 700, 3),
                        skill("phoenix_wings", "Sayap Api", "Sayap api membentang: cincin api melukai dan membakar musuh sekitarmu.", 65f, 900, 5),
                        skill("phoenix_ruin", "Kehancuran Phoenix", "Bola api besar meledak dan menghancurkan area yang luas.", 95f, 1700, 7)
                )));

        register(new MartialSoul(
                "white_tiger", "White Tiger", "", "Dai Mubai", "Star Luo", "Power", "Beast", "Rare",
                SoulLineage.BEAST, Element.WIND,
                "Harimau putih berekor: kekuatan fisik dan pertahanan yang brutal.",
                "LIGHT", 0xD8DCE8, "", "", true,
                List.of(
                        skill("tiger_wave", "White Tiger Light Wave", "Gelombang cahaya harimau: damage garis lurus + knockback.", 30f, 300, 1),
                        skill("tiger_vajra", "Vajra Transformation", "Transformasi: Strength II + Resistance II selama 30 detik.", 45f, 600, 3),
                        skill("tiger_claw", "Cakar Harimau", "Cakar mengunci satu target: hantaman burst besar.", 60f, 850, 5),
                        skill("tiger_roar", "Tiger Mengaum", "Auman yang stumblingkan: damage AoE dan knockback kuat ke semua musuh.", 90f, 1500, 7)
                )));

        register(new MartialSoul(
                "soft_bone_rabbit", "Soft Bone Rabbit", "", "Xiao Wu", "Shrek", "Agility", "Beast", "Rare",
                SoulLineage.BEAST, Element.LIGHTNING,
                "Kelinci tulang lunak: pesona memikat dan gerakan secepat kilat.",
                "WIND", 0xF0A8C0, "", "", true,
                List.of(
                        skill("rabbit_charm", "Charm", "Memikat monster di sekitar: mereka berhenti menyerangmu.", 30f, 400, 1),
                        skill("rabbit_blink", "Teleport", "Berkedip 8 blok ke arah pandanganmu.", 25f, 200, 3),
                        skill("rabbit_hop", "Lompatan Kelinci", "Lompatan ringan: lompat tinggi dan Speed II selama 6 detik.", 55f, 700, 5),
                        skill("rabbit_sprint", "Lari Terobosan", "Lari menembus arena: teleport 12 blok dan mengacaukan musuh yang dilewati.", 90f, 1300, 7)
                )));

        register(new MartialSoul(
                "jade_serpent", "Jade Phosphor Serpent", "", "Dugu Yan", "", "Racun", "Beast", "Rare",
                SoulLineage.BEAST, Element.WATER,
                "Ular giok beracun: kabut dan gigitan yang melumpuhkan musuh perlahan.",
                "POISON", 0x3CC88A, "", "", true,
                List.of(
                        skill("serpent_mist", "Poison Mist", "Kabut racun AoE: poison + slow 10 detik.", 30f, 360, 1),
                        skill("serpent_bite", "Serpent Bite", "Gigitan berbisa pada musuh terdekat: damage besar + poison kuat.", 40f, 400, 3),
                        skill("serpent_siphon", "Sifon Racun", "Sifon racun: serap HP musuh terdekat ke tubuhmu sendiri.", 60f, 750, 5),
                        skill("serpent_death", "Kabut Maut", "Kabut maut dalam radius 10: racun dan Wither yang mengikis vitalitas.", 90f, 1400, 7)
                )));

        register(new MartialSoul(
                "clear_sky_hammer", "Clear Sky Hammer", "", "Tang San / Tang Hao", "Clear Sky Sect", "Power", "Tool", "Legendary",
                SoulLineage.TOOL, Element.LIGHTNING,
                "Palu Langit Jernih: martial soul legendaris klan Clear Sky, penghancur segalanya.",
                "EARTH", 0x7A8AA8, "", "", true,
                List.of(
                        skill("hammer_slam", "Hammer Slam", "Hantaman palu raksasa: damage AoE masif + knockback ke atas.", 45f, 500, 1),
                        skill("hammer_cloak", "Chaotic Cloak", "Teknik palu berputar: Strength III + Speed II selama 20 detik.", 60f, 900, 3),
                        skill("hammer_thunder", "Palu Petir", "Palu memetik petir: satu target dihantam damage luar biasa.", 70f, 1000, 5),
                        skill("hammer_clear_sky", "Palu Langit Jernih", "Langit jernih runtuh: hantaman AoE masif dengan knockback ke atas.", 95f, 1800, 7)
                )));

        // --------------------------------------------------------- v1.4: katalog 43 soul

        register(new MartialSoul(
                "golden_dragon", "Golden Dragon", "", "Long Tian", "Golden Dragon Clan", "Serangan", "Dragon", "Legendary",
                SoulLineage.BEAST, Element.EARTH,
                "A golden wyrm whose scales burn brighter with every century it survives.",
                "FIRE", 0xE8A33C, "", "", true,
                List.of(
                        arc("gold_dragon_fang", "Fire Fang", "A single burning bite that chars the target from the inside.",
                                SkillArchetype.STRIKE, 16f, 0f, 6.0, 0, 0, 25f, 300, 1, FxShape.LINE, 0xF2A03C),
                        arc("gold_dragon_scales", "Scale Wall", "Golden scales harden into a shell that soaks the next blows.",
                                SkillArchetype.SHIELD, 0f, 0f, 4.0, 600, 1, 40f, 600, 3, FxShape.RING, 0xFFD98C),
                        arc("gold_dragon_sun", "Sun Flare", "A stored sunburst erupts around you and throws the enemy back.",
                                SkillArchetype.BLAST, 20f, 0f, 9.0, 0, 0, 60f, 900, 5, FxShape.SPHERE, 0xFFE9A8),
                        arc("gold_dragon_ascension", "Golden Ascension", "The wyrm unwinds and dives: a burning nova that flings everything away.",
                                SkillArchetype.NOVA, 34f, 0f, 11.0, 0, 0, 95f, 1800, 7, FxShape.BURST, 0xFFC24A)
                )));

        register(new MartialSoul(
                "abyssal_demon_dragon", "Abyssal Demon Dragon", "", "Ye Gu", "Abyssal Sect", "Serangan", "Dragon", "Legendary",
                SoulLineage.BEAST, Element.LIGHT,
                "A dragon that drags starlight up from the abyss and wears it like a second skin.",
                "DARK", 0x6B3FA0, "", "", true,
                List.of(
                        arc("demon_dragon_graze", "Abyssal Graze", "Claws of the abyss open a wound that bleeds shadow instead of blood.",
                                SkillArchetype.LIFESTEAL, 15f, 0f, 6.0, 200, 0, 25f, 320, 1, FxShape.LINE, 0x8A4FD0),
                        arc("demon_dragon_void", "Void Pull", "The abyss drags every nearby enemy toward your claws.",
                                SkillArchetype.LURE, 0f, 0f, 9.0, 300, 1, 45f, 700, 3, FxShape.CONE, 0x5A2E8C),
                        arc("demon_dragon_gaze", "Starless Gaze", "A stare that unseats the soul: the target is marked and left reeling.",
                                SkillArchetype.MARK, 10f, 0f, 7.0, 600, 0, 60f, 950, 5, FxShape.SPHERE, 0xB37CF0),
                        arc("demon_dragon_abyss", "Abyssal Verdict", "The dragon dives through the floor and erupts beneath the field.",
                                SkillArchetype.NOVA, 32f, 0f, 12.0, 0, 0, 95f, 1750, 7, FxShape.BURST, 0x4A1E78)
                )));

        register(new MartialSoul(
                "abyssal_ice_demon_dragon", "Abyssal Ice Demon Dragon", "", "Ye Ying", "Abyssal Sect", "Serangan", "Dragon", "Epic",
                SoulLineage.BEAST, Element.WATER,
                "The cold twin of the abyssal dragon, freezing everything it passes into silence.",
                "ICE", 0x5AA8D8, "", "", true,
                List.of(
                        arc("ice_dragon_frost", "Frost Rend", "Frozen claws split the target and leave it brittle.",
                                SkillArchetype.STRIKE, 15f, 0f, 6.0, 0, 0, 25f, 300, 1, FxShape.LINE, 0xA8E4FF),
                        arc("ice_dragon_hail", "Hail Prison", "A ring of ice locks every enemy in place.",
                                SkillArchetype.ROOT, 0f, 0f, 8.0, 300, 2, 45f, 700, 3, FxShape.RING, 0x7FC8F0),
                        arc("ice_dragon_chain", "Frozen Chain", "Ice lashes out from your grip into three enemies at once.",
                                SkillArchetype.CHAIN, 13f, 0f, 8.0, 0, 0, 60f, 900, 5, FxShape.LINE, 0xC8F0FF),
                        arc("ice_dragon_winter", "Deep Winter", "Winter closes in: a nova of ice that slows everything it touches.",
                                SkillArchetype.NOVA, 30f, 0f, 11.0, 400, 1, 95f, 1700, 7, FxShape.BURST, 0xE4F8FF)
                )));

        register(new MartialSoul(
                "ultimate_fire_dragon", "Ultimate Fire Dragon", "", "Yan Ling", "Fire Sect", "Serangan Api", "Dragon", "Epic",
                SoulLineage.BEAST, Element.EARTH,
                "A dragon bred in a volcano for a thousand years, old enough to remember the first flame.",
                "FIRE", 0xE0522A, "", "", true,
                List.of(
                        arc("fire_dragon_breath", "Ember Breath", "A short breath of living flame scorches everything in front of you.",
                                SkillArchetype.WAVE, 14f, 0f, 9.0, 0, 0, 28f, 340, 1, FxShape.CONE, 0xFF7A3C),
                        arc("fire_dragon_wing", "Wing of Cinders", "Cinder wings beat the air and scatter burning motes across the field.",
                                SkillArchetype.BLAST, 18f, 0f, 8.0, 200, 0, 50f, 750, 3, FxShape.SPHERE, 0xFFB04A),
                        arc("fire_dragon_scale", "Molten Hide", "Your skin hardens into cooled lava: resistance and a shield of heat.",
                                SkillArchetype.SHIELD, 0f, 0f, 4.0, 700, 2, 55f, 900, 5, FxShape.RING, 0xE0522A),
                        arc("fire_dragon_caldera", "Caldera", "The dragon opens its jaws and a column of fire erupts from the ground.",
                                SkillArchetype.NOVA, 31f, 0f, 10.0, 300, 1, 95f, 1650, 7, FxShape.COLUMN, 0xFFD05A)
                )));

        register(new MartialSoul(
                "ultimate_ice_dragon", "Ultimate Ice Dragon", "", "Shen Mu", "Ice Sect", "Serangan", "Dragon", "Epic",
                SoulLineage.BEAST, Element.WATER,
                "The glacier's last dragon, slow to wake and impossible to argue with.",
                "ICE", 0x8FC8E8, "", "", true,
                List.of(
                        arc("ice_dragon_lance", "Glacier Lance", "A spear of clear ice forms in your hand and pierces one target.",
                                SkillArchetype.STRIKE, 16f, 0f, 7.0, 0, 0, 26f, 320, 1, FxShape.LINE, 0xC8E8FF),
                        arc("ice_dragon_veil", "Frost Veil", "Your body hardens into frost: resistance and a thin layer of ice armor.",
                                SkillArchetype.SHIELD, 0f, 0f, 4.0, 700, 2, 48f, 850, 3, FxShape.RING, 0x8FC8E8),
                        arc("ice_dragon_shatter", "Shatter", "A single frozen note shatters the will of every enemy nearby.",
                                SkillArchetype.DISRUPT, 11f, 0f, 8.0, 400, 2, 60f, 950, 5, FxShape.SPHERE, 0xE4F8FF),
                        arc("ice_dragon_frostfall", "Frostfall", "The sky itself falls: a nova of ice that locks the ground in place.",
                                SkillArchetype.NOVA, 30f, 0f, 11.0, 500, 2, 95f, 1700, 7, FxShape.BURST, 0xDFF4FF)
                )));

        register(new MartialSoul(
                "thunder_elephant", "Thunder Elephant", "", "Lei Huang", "Thunder Sect", "Power", "Beast", "Rare",
                SoulLineage.BEAST, Element.WIND,
                "An elephant that walks through storms, carrying thunder in the hollow of its tusks.",
                "LIGHTNING", 0x7C9BE0, "", "", true,
                List.of(
                        arc("thunder_elephant_stomp", "Thunder Stomp", "One heavy footfall shakes the ground and jolts a single target.",
                                SkillArchetype.STRIKE, 17f, 0f, 6.0, 0, 0, 26f, 330, 1, FxShape.LINE, 0x9BC8FF),
                        arc("thunder_elephant_trample", "Trample", "A wide charge flattens everything around you and knocks it back.",
                                SkillArchetype.BLAST, 19f, 0f, 9.0, 0, 0, 52f, 780, 3, FxShape.PLANE, 0x7C9BE0),
                        arc("thunder_elephant_charge", "Charging Spirit", "Charge up and slam: a heavy hit that leaves the target stunned.",
                                SkillArchetype.STUN, 15f, 0f, 7.0, 300, 1, 62f, 950, 5, FxShape.SPHERE, 0xC8DCFF),
                        arc("thunder_elephant_storm", "Storm Herd", "The herd runs with you: a nova of thunder that throws the field aside.",
                                SkillArchetype.NOVA, 30f, 0f, 11.0, 0, 0, 92f, 1650, 7, FxShape.BURST, 0xB7D4FF)
                )));

        register(new MartialSoul(
                "golden_ant", "Golden Ant", "", "Bao Ling", "Ant Sect", "Serangan", "Beast", "Uncommon",
                SoulLineage.BEAST, Element.POISON,
                "A worker ant small enough to hide, strong enough to hollow out a cliff.",
                "EARTH", 0xC9A227, "", "", true,
                List.of(
                        arc("golden_ant_bite", "Mandible Bite", "Tiny jaws find the gap in your enemy's guard.",
                                SkillArchetype.STRIKE, 12f, 0f, 5.0, 0, 0, 22f, 280, 1, FxShape.LINE, 0xE0C24A),
                        arc("golden_ant_acid", "Acid Spray", "A mist of acid weakens everything it touches.",
                                SkillArchetype.VENOM, 8f, 0f, 7.0, 300, 1, 38f, 620, 3, FxShape.CONE, 0xB8A02C),
                        arc("golden_ant_dig", "Tunnel", "You sink into the ground and burst up under three enemies.",
                                SkillArchetype.CHAIN, 12f, 0f, 8.0, 0, 0, 52f, 800, 5, FxShape.LINE, 0xD4B84A),
                        arc("golden_ant_swarm", "Swarm March", "A wave of golden ants floods the field and carries it off.",
                                SkillArchetype.NOVA, 24f, 0f, 10.0, 300, 0, 85f, 1500, 7, FxShape.PLANE, 0xF0DC84)
                )));

        register(new MartialSoul(
                "titan_giant_ape", "Titan Giant Ape", "", "Chi Meng", "Titan Sect", "Power", "Beast", "Rare",
                SoulLineage.BEAST, Element.LIGHTNING,
                "A gorilla the size of a keep, still angry about the mountain it never asked for.",
                "EARTH", 0x9A7048, "", "", true,
                List.of(
                        arc("giant_ape_smash", "Mountain Smash", "Both fists come down on one enemy with the weight of a hill.",
                                SkillArchetype.STRIKE, 18f, 0f, 6.0, 0, 0, 27f, 340, 1, FxShape.LINE, 0xB08A5C),
                        arc("giant_ape_roar", "War Roar", "The roar shakes the courage out of every enemy in range.",
                                SkillArchetype.ROOT, 0f, 0f, 9.0, 300, 2, 48f, 800, 3, FxShape.RING, 0x8A6240),
                        arc("giant_ape_throw", "Stone Throw", "A boulder goes through three enemies in a straight line.",
                                SkillArchetype.CHAIN, 16f, 0f, 9.0, 0, 0, 62f, 950, 5, FxShape.LINE, 0xC4A078),
                        arc("giant_ape_tremor", "Tremor", "The ape pounds the earth and the whole plain shakes.",
                                SkillArchetype.NOVA, 29f, 0f, 12.0, 0, 0, 92f, 1600, 7, FxShape.PLANE, 0xD0B088)
                )));

        register(new MartialSoul(
                "sea_devil_manatee", "Sea Devil Manatee", "", "Hai Ling", "Sharks Sect", "Kontrol", "Beast", "Uncommon",
                SoulLineage.BEAST, Element.POISON,
                "A slow grey shape that hauls ships down by the ankle and never hurries.",
                "WATER", 0x3C7A8C, "", "", true,
                List.of(
                        arc("manatee_tether", "Iron Tether", "A rope of water catches one enemy and refuses to let go.",
                                SkillArchetype.MARK, 9f, 0f, 6.0, 600, 0, 24f, 300, 1, FxShape.LINE, 0x5AA8B4),
                        arc("manatee_drag", "Undertow", "The water beneath the field pulls every enemy toward you.",
                                SkillArchetype.LURE, 0f, 0f, 9.0, 300, 1, 42f, 700, 3, FxShape.CONE, 0x2E6474),
                        arc("manatee_venom", "Salt Venom", "Salt water and old blood: poison that spreads through the wound.",
                                SkillArchetype.VENOM, 10f, 0f, 8.0, 400, 2, 56f, 880, 5, FxShape.SPHERE, 0x7BC8C4),
                        arc("manatee_deep", "Deep Below", "Something enormous rises under the water and drags the field down.",
                                SkillArchetype.NOVA, 26f, 0f, 11.0, 400, 1, 88f, 1550, 7, FxShape.BURST, 0x9BE0D8)
                )));

        register(new MartialSoul(
                "bright_wing_moth", "Bright Wing Moth", "", "Su Mei", "Illusion Sect", "Kontrol", "Beast", "Uncommon",
                SoulLineage.BEAST, Element.LIGHT,
                "A moth whose wings scatter light that blinds whatever tries to follow it.",
                "WIND", 0xE8D48C, "", "", true,
                List.of(
                        arc("moth_dust", "Luminous Dust", "A cloud of bright dust stings the eyes of one enemy.",
                                SkillArchetype.STRIKE, 11f, 0f, 5.0, 0, 0, 22f, 280, 1, FxShape.LINE, 0xF4E4A8),
                        arc("moth_wingbeat", "Wingbeat", "Wings beat the air into a gale that pushes enemies back.",
                                SkillArchetype.WAVE, 13f, 0f, 9.0, 0, 0, 40f, 680, 3, FxShape.CONE, 0xE8D48C),
                        arc("moth_dazzle", "Dazzle", "A thousand wings reflect light until the target loses its nerve.",
                                SkillArchetype.DISRUPT, 9f, 0f, 8.0, 400, 2, 55f, 900, 5, FxShape.SPHERE, 0xFFF4C8),
                        arc("moth_swarm", "Night of Wings", "The moth empties the air: a pale nova that blinds and slows everything.",
                                SkillArchetype.NOVA, 25f, 0f, 11.0, 500, 2, 88f, 1500, 7, FxShape.BURST, 0xFFFCE0)
                )));

        register(new MartialSoul(
                "phoenix", "Phoenix", "", "Xiao Wu", "Shrek", "Serangan Api", "Beast", "Rare",
                SoulLineage.BEAST, Element.LIGHT,
                "The bird that burns and is born again, older and calmer each time.",
                "FIRE", 0xF0B24A, "", "", true,
                List.of(
                        arc("phoenix_ember", "Ember Peck", "A short dive ends in a peck of fire.",
                                SkillArchetype.STRIKE, 14f, 0f, 6.0, 0, 0, 24f, 300, 1, FxShape.LINE, 0xFFB84A),
                        arc("phoenix_rebirth", "Rebirth", "Burned away and restored: resistance and a shield of warm light.",
                                SkillArchetype.SHIELD, 0f, 4f, 4.0, 600, 1, 46f, 820, 3, FxShape.RING, 0xFFD98C),
                        arc("phoenix_talon", "Talon Dive", "The phoenix drops from above and rakes three enemies at once.",
                                SkillArchetype.CHAIN, 15f, 0f, 9.0, 0, 0, 60f, 920, 5, FxShape.LINE, 0xFF8A3C),
                        arc("phoenix_reignite", "Reignition", "The bird rises out of its own ashes in a ring of fire.",
                                SkillArchetype.NOVA, 30f, 0f, 11.0, 300, 1, 92f, 1600, 7, FxShape.BURST, 0xFFE9A8)
                )));

        register(new MartialSoul(
                "blue_silver_spear", "Blue Silver Spear", "", "Tang San", "Tang Sect", "Serangan", "Tool", "Rare",
                SoulLineage.TOOL, Element.WATER,
                "A spear grown from the same blue silver grass, hardened until it stops yielding.",
                "NATURE", 0x3E6FB8, "", "", true,
                List.of(
                        arc("bss_pierce", "Silver Thrust", "The spear finds the seam in a single thrust.",
                                SkillArchetype.STRIKE, 15f, 0f, 7.0, 0, 0, 25f, 310, 1, FxShape.LINE, 0x6FA0E0),
                        arc("bss_bind", "Grass Bind", "Blue silver vines wrap the target and hold it still.",
                                SkillArchetype.ROOT, 0f, 0f, 7.0, 300, 2, 44f, 720, 3, FxShape.RING, 0x4A7BC8),
                        arc("bss_venom", "Silver Venom", "The spearhead leaks a toxin that keeps working after the thrust.",
                                SkillArchetype.VENOM, 11f, 0f, 8.0, 400, 2, 58f, 900, 5, FxShape.CONE, 0x7FB4EC),
                        arc("bss_empire", "Emperor's Reach", "Every blade of grass in the field points the same way, and strikes together.",
                                SkillArchetype.NOVA, 28f, 0f, 11.0, 0, 0, 90f, 1550, 7, FxShape.BURST, 0x3E6FB8)
                )));

        register(new MartialSoul(
                "seven_kill_sword", "Seven Kill Sword", "", "Chi Lin", "Shrek", "Serangan", "Tool", "Legendary",
                SoulLineage.TOOL, Element.LIGHT,
                "A sword that counts the dead and is never satisfied with the number.",
                "DARK", 0x3A3040, "", "", true,
                List.of(
                        arc("seven_kill_mark", "First Notch", "The blade opens a small wound and counts it as one.",
                                SkillArchetype.MARK, 12f, 0f, 7.0, 600, 0, 26f, 320, 1, FxShape.LINE, 0x6E5A7A),
                        arc("seven_kill_drain", "Blood Ledger", "The sword drinks what it opens and hands part of it back to you.",
                                SkillArchetype.LIFESTEAL, 16f, 0f, 7.0, 300, 0, 52f, 780, 3, FxShape.SPHERE, 0x8A3040),
                        arc("seven_kill_notch", "Seventh Notch", "The blade jumps between enemies, leaving a notch on each.",
                                SkillArchetype.CHAIN, 18f, 0f, 9.0, 0, 0, 64f, 980, 5, FxShape.LINE, 0xB44A5A),
                        arc("seven_kill_finish", "Final Count", "The seventh stroke falls on the weakest of them and ends it.",
                                SkillArchetype.EXECUTE, 30f, 0f, 8.0, 0, 0, 95f, 1700, 7, FxShape.BURST, 0xC85A6A)
                )));

        register(new MartialSoul(
                "giant_hammer", "Giant Hammer", "", "Niu Mao", "Iron Wall Sect", "Power", "Tool", "Rare",
                SoulLineage.TOOL, Element.LIGHTNING,
                "A smith's hammer that outgrew the smith and now has opinions about anvils.",
                "EARTH", 0x7A6A58, "", "", true,
                List.of(
                        arc("giant_hammer_down", "Overhead Blow", "The hammer comes down on one target with a clang that hurts.",
                                SkillArchetype.STRIKE, 17f, 0f, 6.0, 0, 0, 27f, 340, 1, FxShape.LINE, 0xA08C6E),
                        arc("giant_hammer_sweep", "Wide Swing", "A slow arc that catches everyone standing in front of you.",
                                SkillArchetype.WAVE, 18f, 0f, 9.0, 0, 0, 50f, 760, 3, FxShape.CONE, 0x8C7A62),
                        arc("giant_hammer_anchor", "Anchored Stance", "Plant the hammer and hold the line: resistance and strength.",
                                SkillArchetype.BUFF, 0f, 0f, 4.0, 600, 1, 58f, 900, 5, FxShape.RING, 0xB09A78),
                        arc("giant_hammer_quake", "Quake Strike", "The hammer meets the ground and the shock wave rolls outward.",
                                SkillArchetype.NOVA, 30f, 0f, 12.0, 0, 0, 92f, 1600, 7, FxShape.PLANE, 0xC4B08C)
                )));

        register(new MartialSoul(
                "blood_lash", "Blood Lash", "", "Xue Hun", "Blood Sect", "Kontrol", "Tool", "Uncommon",
                SoulLineage.TOOL, Element.FIRE,
                "A whip woven from dried blood cord that remembers every target it has opened.",
                "DARK", 0x8E2434, "", "", true,
                List.of(
                        arc("blood_lash_cut", "First Cut", "The lash opens a thin line and lets the blood do the work.",
                                SkillArchetype.MARK, 11f, 0f, 6.0, 600, 0, 23f, 290, 1, FxShape.LINE, 0xB03344),
                        arc("blood_lash_drain", "Sucking Wound", "The cord drinks what spills and passes it into your arm.",
                                SkillArchetype.LIFESTEAL, 13f, 0f, 6.0, 300, 0, 40f, 660, 3, FxShape.SPHERE, 0x8E2434),
                        arc("blood_lash_crack", "Crack of the Lash", "A single crack crosses the field and bruises everything it passes.",
                                SkillArchetype.WAVE, 14f, 0f, 9.0, 0, 0, 56f, 860, 5, FxShape.CONE, 0xC4485A),
                        arc("blood_lash_rite", "Rite of the Cord", "The lash unspools and lashes out from every direction at once.",
                                SkillArchetype.NOVA, 25f, 0f, 11.0, 300, 0, 86f, 1500, 7, FxShape.BURST, 0x6E1A28)
                )));

        register(new MartialSoul(
                "frost_needle", "Frost Needle", "", "Han Xue", "Snow Sect", "Serangan", "Tool", "Uncommon",
                SoulLineage.TOOL, Element.WATER,
                "A needle of ice so thin it only shows when it is already inside someone.",
                "ICE", 0x9AD8E8, "", "", true,
                List.of(
                        arc("frost_needle_prick", "Cold Pin", "One needle, one target, one line of frost spreading outward.",
                                SkillArchetype.STRIKE, 12f, 0f, 6.0, 0, 0, 22f, 280, 1, FxShape.LINE, 0xC4E8F4),
                        arc("frost_needle_spread", "Spreading Frost", "The cold walks out from the needle and numbs the whole area.",
                                SkillArchetype.ROOT, 0f, 0f, 8.0, 300, 1, 40f, 700, 3, FxShape.RING, 0x7FC8E8),
                        arc("frost_needle_venom", "Numbing Toxin", "Frostbite and poison together: the target stops healing.",
                                SkillArchetype.VENOM, 10f, 0f, 8.0, 400, 2, 54f, 860, 5, FxShape.CONE, 0xA8D0E0),
                        arc("frost_needle_storm", "Needle Storm", "A hundred needles fill the air and fall like hail.",
                                SkillArchetype.NOVA, 24f, 0f, 10.0, 300, 1, 85f, 1450, 7, FxShape.BURST, 0xE0F4FF)
                )));

        register(new MartialSoul(
                "thunder_spear", "Thunder Spear", "", "Dian Lei", "Thunder Sect", "Serangan", "Tool", "Rare",
                SoulLineage.TOOL, Element.WIND,
                "A spear that is only visible when the lightning inside it is.",
                "LIGHTNING", 0x6E8FE0, "", "", true,
                List.of(
                        arc("thunder_spear_jab", "Static Jab", "A short thrust leaves the target ringing.",
                                SkillArchetype.STRIKE, 15f, 0f, 7.0, 0, 0, 25f, 310, 1, FxShape.LINE, 0x9BC8FF),
                        arc("thunder_spear_sweep", "Arc Sweep", "A wide arc of current passes through everything in front of you.",
                                SkillArchetype.WAVE, 16f, 0f, 9.0, 0, 0, 50f, 760, 3, FxShape.CONE, 0x6E8FE0),
                        arc("thunder_spear_chain", "Forked Current", "The current forks into three enemies and leaves them convulsing.",
                                SkillArchetype.CHAIN, 15f, 0f, 9.0, 300, 1, 60f, 920, 5, FxShape.LINE, 0xC8E0FF),
                        arc("thunder_spear_storm", "Stormhead", "A thunderhead opens above you and drops into the field.",
                                SkillArchetype.NOVA, 29f, 0f, 11.0, 0, 0, 92f, 1600, 7, FxShape.BURST, 0xEAF2FF)
                )));

        register(new MartialSoul(
                "spirit_sword", "Spirit Sword", "", "Lin Feng", "Wanderer", "Serangan", "Tool", "Common",
                SoulLineage.TOOL, Element.DARK,
                "A plain sword that keeps its own counsel and fights on its own terms.",
                "LIGHT", 0xB8C4D4, "", "", true,
                List.of(
                        arc("spirit_sword_cut", "Plain Cut", "A clean cut, nothing more, nothing less.",
                                SkillArchetype.STRIKE, 12f, 0f, 6.0, 0, 0, 22f, 280, 1, FxShape.LINE, 0xD4DCE8),
                        arc("spirit_sword_guard", "Guard Step", "The sword comes up: resistance and a measured breath.",
                                SkillArchetype.SHIELD, 0f, 0f, 4.0, 500, 0, 38f, 700, 3, FxShape.RING, 0x9AA8BC),
                        arc("spirit_sword_riposte", "Riposte", "Wait for the enemy to come to you, then answer.",
                                SkillArchetype.MARK, 10f, 0f, 7.0, 600, 0, 52f, 840, 5, FxShape.SPHERE, 0xC8D4E4),
                        arc("spirit_sword_finish", "Last Word", "One decisive stroke, taken at the moment the guard drops.",
                                SkillArchetype.EXECUTE, 26f, 0f, 8.0, 0, 0, 85f, 1500, 7, FxShape.BURST, 0xEAF0F8)
                )));

        register(new MartialSoul(
                "iron_anchor", "Iron Anchor", "", "Jaka Wibawa", "Maritime Guild", "Alat", "Tool", "Common",
                SoulLineage.TOOL, Element.EARTH,
                "The guild's oldest anchor, dropped by sailors who never came back up.",
                "WATER", 0x5E6A6E, "", "", true,
                List.of(
                        arc("iron_anchor_drop", "Drop Anchor", "The anchor falls on one enemy and pins it in place.",
                                SkillArchetype.STRIKE, 13f, 0f, 6.0, 300, 1, 23f, 300, 1, FxShape.LINE, 0x7E8A8E),
                        arc("iron_anchor_hold", "Hold Fast", "Everything in reach is dragged toward your hook.",
                                SkillArchetype.LURE, 0f, 0f, 9.0, 300, 1, 40f, 700, 3, FxShape.CONE, 0x5E6A6E),
                        arc("iron_anchor_swing", "Anchor Swing", "A wide swing at the end of a chain catches the whole line.",
                                SkillArchetype.WAVE, 15f, 0f, 9.0, 0, 0, 54f, 840, 5, FxShape.CONE, 0x96A2A6),
                        arc("iron_anchor_moor", "Moor the Fleet", "The chain whips out in every direction and holds the field fast.",
                                SkillArchetype.NOVA, 26f, 0f, 11.0, 300, 0, 86f, 1500, 7, FxShape.BURST, 0xBAC4C8)
                )));

        register(new MartialSoul(
                "bronze_mirror", "Bronze Mirror", "", "Shen Ying", "Mirror Sect", "Support", "Tool", "Uncommon",
                SoulLineage.TOOL, Element.DARK,
                "A mirror that shows the sect's enemies their own worst moment.",
                "LIGHT", 0xC08A4A, "", "", true,
                List.of(
                        arc("bronze_mirror_gleam", "Gleam", "A flash of bronze light dazzles one enemy.",
                                SkillArchetype.STRIKE, 10f, 0f, 6.0, 0, 0, 22f, 280, 1, FxShape.LINE, 0xE0AE6A),
                        arc("bronze_mirror_ward", "Bronze Ward", "The mirror turns the light aside: resistance for everyone nearby.",
                                SkillArchetype.AURA, 0f, 0f, 12.0, 500, 1, 44f, 800, 3, FxShape.RING, 0xC08A4A),
                        arc("bronze_mirror_show", "Show the Truth", "Every enemy in reach sees itself and loses the will to fight.",
                                SkillArchetype.DISRUPT, 9f, 0f, 8.0, 400, 2, 56f, 900, 5, FxShape.SPHERE, 0xF0D49A),
                        arc("bronze_mirror_reflect", "Reflected Field", "The mirror throws back everything the field throws at you.",
                                SkillArchetype.NOVA, 24f, 0f, 11.0, 300, 1, 86f, 1500, 7, FxShape.BURST, 0xFFE8B8)
                )));

        register(new MartialSoul(
                "blue_silver_emperor", "Blue Silver Emperor", "", "Tang San", "Tang Sect", "Pertahanan", "Plant", "Epic",
                SoulLineage.BODY, Element.WATER,
                "The grass grown old enough to rule: ten thousand years of quiet green authority.",
                "NATURE", 0x2E5C9E, "", "", true,
                List.of(
                        arc("bsemp_roots", "Imperial Roots", "Deep roots take hold: resistance and a slow recovery.",
                                SkillArchetype.SHIELD, 0f, 6f, 4.0, 700, 1, 40f, 800, 1, FxShape.RING, 0x3D6FBE),
                        arc("bsemp_tide", "Green Tide", "The field floods with blue silver and slows everything in it.",
                                SkillArchetype.ROOT, 0f, 0f, 10.0, 400, 2, 50f, 850, 3, FxShape.PLANE, 0x2E5C9E),
                        arc("bsemp_regrowth", "Regrowth", "Tissue knits itself back together and carries the whole line.",
                                SkillArchetype.MEND, 0f, 10f, 12.0, 400, 0, 60f, 950, 5, FxShape.RING, 0x7FB4EC),
                        arc("bsemp_crown", "Crown of Grass", "A crown of blue silver rises and falls over the field.",
                                SkillArchetype.NOVA, 32f, 0f, 12.0, 400, 1, 95f, 1700, 7, FxShape.BURST, 0x4A7BC8)
                )));

        register(new MartialSoul(
                "iron_body", "Iron Body", "", "Aru Pamungkas", "Iron Sect", "Pertahanan", "Body", "Uncommon",
                SoulLineage.BODY, Element.LIGHTNING,
                "A body that learned to be metal, and pays for it in weight and cold.",
                "EARTH", 0x6E7480, "", "", true,
                List.of(
                        arc("iron_body_set", "Set the Bones", "Lock the frame: resistance and a skin of cold iron.",
                                SkillArchetype.SHIELD, 0f, 0f, 4.0, 600, 1, 36f, 700, 1, FxShape.RING, 0x8C939E),
                        arc("iron_body_ram", "Ram", "Shoulder first: a heavy blow to a single target.",
                                SkillArchetype.STRIKE, 15f, 0f, 6.0, 0, 0, 26f, 330, 3, FxShape.LINE, 0x9AA2AE),
                        arc("iron_body_brace", "Braced Line", "Everyone nearby squares up and holds the line together.",
                                SkillArchetype.AURA, 0f, 0f, 12.0, 600, 1, 50f, 850, 5, FxShape.RING, 0x6E7480),
                        arc("iron_body_toll", "Bell Toll", "The whole body rings like a bell, and the field rings with it.",
                                SkillArchetype.NOVA, 27f, 0f, 11.0, 0, 0, 88f, 1550, 7, FxShape.BURST, 0xB4BCC8)
                )));

        register(new MartialSoul(
                "bone_shell", "Bone Shell", "", "Rangga Wijaya", "Bone Sect", "Pertahanan", "Body", "Rare",
                SoulLineage.BODY, Element.LIGHTNING,
                "Bone grown outward into a shell, layer after layer, year after year.",
                "EARTH", 0xD6CBB0, "", "", true,
                List.of(
                        arc("bone_shell_grow", "Shell Growth", "New bone plates over old ones: resistance and a hard skin.",
                                SkillArchetype.SHIELD, 0f, 0f, 4.0, 700, 1, 38f, 750, 1, FxShape.RING, 0xE8DCC0),
                        arc("bone_shell_spear", "Bone Spear", "A splinter of bone leaves the shell and finds a soft place.",
                                SkillArchetype.STRIKE, 15f, 0f, 7.0, 0, 0, 26f, 330, 3, FxShape.LINE, 0xC8B892),
                        arc("bone_shell_bind", "Bone Cage", "The cage closes: enemies are caught and held in place.",
                                SkillArchetype.ROOT, 0f, 0f, 8.0, 400, 2, 52f, 860, 5, FxShape.RING, 0xA89878),
                        arc("bone_shell_burst", "Shell Burst", "The shell breaks outward and the fragments find every enemy.",
                                SkillArchetype.NOVA, 29f, 0f, 11.0, 0, 0, 90f, 1600, 7, FxShape.BURST, 0xF0E6CC)
                )));

        register(new MartialSoul(
                "stone_skin", "Stone Skin", "", "Bima Santoso", "Wanderer", "Pertahanan", "Body", "Common",
                SoulLineage.BODY, Element.ICE,
                "Ordinary skin that hardened after ten years of standing in the rain.",
                "EARTH", 0x8A8578, "", "", true,
                List.of(
                        arc("stone_skin_harden", "Harden", "The skin turns to stone for a short while.",
                                SkillArchetype.SHIELD, 0f, 0f, 4.0, 500, 0, 32f, 650, 1, FxShape.RING, 0xA8A296),
                        arc("stone_skin_slam", "Stone Fist", "A heavy fist that does not care much about the target.",
                                SkillArchetype.STRIKE, 13f, 0f, 6.0, 0, 0, 24f, 300, 3, FxShape.LINE, 0x8A8578),
                        arc("stone_skin_sink", "Sink", "The ground opens and drags every enemy into it.",
                                SkillArchetype.LURE, 0f, 0f, 9.0, 300, 1, 44f, 780, 5, FxShape.CONE, 0x6E6A60),
                        arc("stone_skin_quake", "Quake", "The stone body strikes the ground and the plain answers.",
                                SkillArchetype.NOVA, 25f, 0f, 11.0, 0, 0, 86f, 1500, 7, FxShape.PLANE, 0xB4AEA0)
                )));

        register(new MartialSoul(
                "flesh_regrowth", "Flesh Regrowth", "", "Sekar Wangi", "Herbalist Guild", "Penyembuhan", "Body", "Uncommon",
                SoulLineage.BODY, Element.WATER,
                "Wound tissue that closes faster than it should, at the price of memory.",
                "NATURE", 0xC2687E, "", "", true,
                List.of(
                        arc("regrowth_close", "Close", "The wound closes and the blood comes back.",
                                SkillArchetype.HEAL, 0f, 8f, 4.0, 0, 0, 28f, 500, 1, FxShape.RING, 0xE08A9E),
                        arc("regrowth_bloom", "Bloom", "Fresh tissue spreads outward and mends everyone nearby.",
                                SkillArchetype.MEND, 0f, 10f, 12.0, 400, 0, 46f, 800, 3, FxShape.RING, 0xC2687E),
                        arc("regrowth_thorn", "Thorn Skin", "The new skin grows thorns and the next hit draws blood.",
                                SkillArchetype.MARK, 11f, 0f, 7.0, 600, 0, 56f, 900, 5, FxShape.SPHERE, 0xA85066),
                        arc("regrowth_rebirth", "Rebirth Bloom", "A field of red flowers opens and closes over the battle.",
                                SkillArchetype.NOVA, 24f, 12f, 11.0, 400, 0, 88f, 1500, 7, FxShape.BURST, 0xF0A0B4)
                )));

        register(new MartialSoul(
                "flame_body", "Flame Body", "", "Elang Mahaka", "Fire Sect", "Serangan", "Body", "Rare",
                SoulLineage.BODY, Element.EARTH,
                "A body that burns from the inside and answers heat with heat.",
                "FIRE", 0xD4562A, "", "", true,
                List.of(
                        arc("flame_body_ignite", "Ignite", "The body flares and throws the enemy back.",
                                SkillArchetype.STRIKE, 16f, 0f, 6.0, 200, 0, 27f, 340, 1, FxShape.LINE, 0xF08A4A),
                        arc("flame_body_walk", "Walk on Fire", "Fire runs along the ground and burns everything it crosses.",
                                SkillArchetype.WAVE, 15f, 0f, 9.0, 300, 1, 50f, 780, 3, FxShape.CONE, 0xD4562A),
                        arc("flame_body_ward", "Ash Ward", "The flame banks down into ash armor: resistance and a burning edge.",
                                SkillArchetype.SHIELD, 0f, 0f, 4.0, 600, 1, 58f, 900, 5, FxShape.RING, 0xFF9A5A),
                        arc("flame_body_pillar", "Pillar of Fire", "A column of fire stands in the middle of the field.",
                                SkillArchetype.NOVA, 30f, 0f, 11.0, 300, 1, 92f, 1600, 7, FxShape.COLUMN, 0xFFC07A)
                )));

        register(new MartialSoul(
                "spirit_veins", "Spirit Veins", "", "Nadia Pratama", "Soul Guide Academy", "Agility", "Body", "Uncommon",
                SoulLineage.BODY, Element.WIND,
                "Spirit power runs under the skin instead of inside the chest, and the body moves like it.",
                "LIGHTNING", 0x8FB8D8, "", "", true,
                List.of(
                        arc("veins_surge", "Surge", "Spirit power jumps the gap between veins and the step quickens.",
                                SkillArchetype.BUFF, 0f, 0f, 4.0, 400, 0, 32f, 600, 1, FxShape.RING, 0xC8E0F0),
                        arc("veins_dash", "Vein Dash", "A blink down the line of veins: two steps in one.",
                                SkillArchetype.DASH, 14f, 0f, 6.0, 0, 0, 40f, 700, 3, FxShape.LINE, 0x8FB8D8),
                        arc("veins_spark", "Spark", "Static runs down the arms and into the nearest enemy.",
                                SkillArchetype.CHAIN, 13f, 0f, 8.0, 0, 0, 56f, 880, 5, FxShape.LINE, 0xE0F0FF),
                        arc("veins_circuit", "Closed Circuit", "Every vein in the field lights up at once.",
                                SkillArchetype.NOVA, 27f, 0f, 11.0, 0, 0, 88f, 1500, 7, FxShape.BURST, 0xB0D8F0)
                )));

        register(new MartialSoul(
                "soul_mirror", "Soul Mirror", "", "Laras Wangi", "Mirror Sect", "Kontrol", "Special", "Rare",
                SoulLineage.SPECIAL, Element.DARK,
                "A mirror that shows the shape of a soul, and does not care what it shows.",
                "LIGHT", 0xE4DCF0, "", "", true,
                List.of(
                        arc("soul_mirror_show", "Show the Soul", "The mirror shows the enemy its own shape, and the enemy falters.",
                                SkillArchetype.MARK, 10f, 0f, 7.0, 600, 0, 26f, 320, 1, FxShape.SPHERE, 0xF0EAF8),
                        arc("soul_mirror_drain", "Drain the Image", "The mirror drinks what the enemy shows and passes it on.",
                                SkillArchetype.LIFESTEAL, 14f, 0f, 7.0, 300, 0, 46f, 760, 3, FxShape.SPHERE, 0xC8B8E0),
                        arc("soul_mirror_refuse", "Refuse the Gaze", "Every enemy that looks at the mirror is held in place.",
                                SkillArchetype.ROOT, 0f, 0f, 9.0, 400, 2, 58f, 900, 5, FxShape.RING, 0xE4DCF0),
                        arc("soul_mirror_shatter", "Shatter the Image", "The mirror breaks into a thousand reflections and every one cuts.",
                                SkillArchetype.NOVA, 28f, 0f, 11.0, 0, 0, 92f, 1600, 7, FxShape.BURST, 0xF8F4FF)
                )));

        register(new MartialSoul(
                "spirit_lamp", "Spirit Lamp", "", "Gilang Rama", "Lamp Guild", "Support", "Special", "Uncommon",
                SoulLineage.SPECIAL, Element.DARK,
                "A lamp that burns without oil and lights whatever the holder is thinking about.",
                "LIGHT", 0xE8C878, "", "", true,
                List.of(
                        arc("lamp_wick", "Light the Wick", "A steady light warms one target and takes the edge off it.",
                                SkillArchetype.MARK, 9f, 0f, 6.0, 600, 0, 24f, 300, 1, FxShape.SPHERE, 0xF4DC9C),
                        arc("lamp_aura", "Lamp Circle", "Light settles over the whole line and steadies it.",
                                SkillArchetype.AURA, 0f, 0f, 12.0, 500, 1, 42f, 780, 3, FxShape.RING, 0xE8C878),
                        arc("lamp_mend", "Mend by Lamplight", "The lamp burns away what ails everyone nearby.",
                                SkillArchetype.MEND, 0f, 9f, 12.0, 400, 0, 54f, 880, 5, FxShape.RING, 0xF0D89C),
                        arc("lamp_douse", "Douse the Field", "The lamp gutters and the light swallows every enemy in sight.",
                                SkillArchetype.NOVA, 24f, 0f, 11.0, 400, 1, 86f, 1500, 7, FxShape.BURST, 0xFFF0C0)
                )));

        register(new MartialSoul(
                "soul_forge", "Soul Forge", "", "Mitra Besi", "Smith Guild", "Alat", "Special", "Rare",
                SoulLineage.SPECIAL, Element.EARTH,
                "A forge that heats soul instead of metal, and shapes what comes out of it.",
                "FIRE", 0xB05A2A, "", "", true,
                List.of(
                        arc("forge_strike", "Hammer Out", "A single stroke of the hammer, still hot enough to bite.",
                                SkillArchetype.STRIKE, 16f, 0f, 6.0, 200, 0, 27f, 340, 1, FxShape.LINE, 0xD0763C),
                        arc("forge_heat", "Heat the Stock", "The forge glows white and the whole line hardens with it.",
                                SkillArchetype.AURA, 0f, 0f, 12.0, 600, 1, 48f, 850, 3, FxShape.RING, 0xB05A2A),
                        arc("forge_temper", "Temper", "Quench the target: damage and a marked weakness that lasts.",
                                SkillArchetype.MARK, 12f, 0f, 7.0, 600, 0, 58f, 900, 5, FxShape.SPHERE, 0xE89A54),
                        arc("forge_bellows", "Bellows Blast", "The bellows breathe and the fire rolls over the whole field.",
                                SkillArchetype.NOVA, 29f, 0f, 12.0, 300, 1, 92f, 1600, 7, FxShape.BURST, 0xFFB067)
                )));

        register(new MartialSoul(
                "star_array", "Star Array", "", "Rangga Kirana", "Star Sect", "Serangan", "Special", "Legendary",
                SoulLineage.SPECIAL, Element.DARK,
                "Seven points of light and the space between them, arranged just so.",
                "LIGHT", 0xF2E9B0, "", "", true,
                List.of(
                        arc("star_first_point", "First Point", "One point of the array ignites and falls on a single target.",
                                SkillArchetype.STRIKE, 17f, 0f, 7.0, 0, 0, 28f, 350, 1, FxShape.SPHERE, 0xF8F0C4),
                        arc("star_binding", "Binding Array", "The array folds space and holds every enemy inside it.",
                                SkillArchetype.ROOT, 0f, 0f, 10.0, 400, 2, 50f, 850, 3, FxShape.RING, 0xE0D090),
                        arc("star_chain", "Constellation", "Light runs from star to star and strikes three enemies in turn.",
                                SkillArchetype.CHAIN, 17f, 0f, 9.0, 0, 0, 64f, 980, 5, FxShape.LINE, 0xFFF8DC),
                        arc("star_collapse", "Collapse", "The array folds onto itself and the field goes white.",
                                SkillArchetype.NOVA, 34f, 0f, 13.0, 0, 0, 98f, 1800, 7, FxShape.BURST, 0xFFFCEC)
                )));

        register(new MartialSoul(
                "demon_eye", "Demon Eye", "", "Bayu Gelap", "Shadow Sect", "Kontrol", "Special", "Uncommon",
                SoulLineage.SPECIAL, Element.LIGHT,
                "An eye that never blinks, because it has nothing to be afraid of.",
                "DARK", 0x7A4A8C, "", "", true,
                List.of(
                        arc("demon_eye_stare", "Stare", "One look is enough to make a target hesitate.",
                                SkillArchetype.MARK, 10f, 0f, 7.0, 600, 0, 24f, 300, 1, FxShape.SPHERE, 0xA06AB4),
                        arc("demon_eye_drain", "Draw the Spirit", "The eye pulls at the target's spirit and the pull reaches you.",
                                SkillArchetype.LIFESTEAL, 13f, 0f, 7.0, 300, 0, 42f, 740, 3, FxShape.SPHERE, 0x7A4A8C),
                        arc("demon_eye_hex", "Hex", "A curse settles on everyone who looked too long.",
                                SkillArchetype.DISRUPT, 10f, 0f, 9.0, 400, 2, 56f, 900, 5, FxShape.CONE, 0xC08AD4),
                        arc("demon_eye_open", "The Eye Opens", "The eye opens fully and the field is seen through it.",
                                SkillArchetype.NOVA, 26f, 0f, 11.0, 400, 1, 88f, 1550, 7, FxShape.BURST, 0xB070C8)
                )));

        register(new MartialSoul(
                "ancient_seal", "Ancient Seal", "", "Wira Nirmala", "Seal Sect", "Support", "Special", "Rare",
                SoulLineage.SPECIAL, Element.LIGHTNING,
                "A seal older than the sect that stamped it, and it still answers to the hand.",
                "EARTH", 0x8C7A4A, "", "", true,
                List.of(
                        arc("seal_stamp", "First Seal", "The seal presses down on one target and leaves its mark.",
                                SkillArchetype.MARK, 11f, 0f, 7.0, 600, 0, 25f, 310, 1, FxShape.SPHERE, 0xB8A25E),
                        arc("seal_bond", "Sealed Bond", "The seal ties the whole line together against harm.",
                                SkillArchetype.AURA, 0f, 0f, 12.0, 600, 1, 46f, 820, 3, FxShape.RING, 0x8C7A4A),
                        arc("seal_ward", "Sealed Ward", "Old wards answer the call and the field turns aside the next blow.",
                                SkillArchetype.SHIELD, 0f, 0f, 4.0, 700, 2, 58f, 900, 5, FxShape.RING, 0xD0BC78),
                        arc("seal_unbroken", "Unbroken Seal", "The seal breaks open and the whole array fires at once.",
                                SkillArchetype.NOVA, 30f, 0f, 12.0, 0, 0, 94f, 1650, 7, FxShape.BURST, 0xE8D494)
                )));
    }
}
