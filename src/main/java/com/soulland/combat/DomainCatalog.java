package com.soulland.combat;

import com.soulland.soul.Element;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Katalog Domain untuk 43 martial soul.
 *
 * <p>Setiap soul punya tepat satu definisi Domain. Domain adalah wilayah yang
 * berdiri, memberi buff kepada sekutu, memberi debuff kepada musuh, menahan
 * damage per detik, dan menguras spirit power pemilik setiap detik sampai
 * energinya habis.
 *
 * <p>Efek domain diturunkan dari elemen soul: Api dan Es melambat, Petir
 * melemahkan, Air dan Tumbuhan memulihkan, Cahaya membutakan, Gelap
 * menandai, dan Tanah menahan.
 */
public final class DomainCatalog {
    private DomainCatalog() {}

    private static final Map<String, DomainDef> REGISTRY = new LinkedHashMap<>();

    /** Definisi Domain milik sebuah soul, atau null kalau soul itu tidak punya. */
    public static DomainDef get(String soulId) {
        if (soulId == null) return null;
        return REGISTRY.get(soulId);
    }

    /** Semua definisi Domain, urut sesuai registry soul. */
    public static List<DomainDef> all() {
        return new ArrayList<>(REGISTRY.values());
    }

    /** Jumlah soul yang punya Domain. */
    public static int registeredCount() {
        return REGISTRY.size();
    }

    private static void register(DomainDef def) {
        REGISTRY.put(def.soulId(), def);
    }

    private static BuffProfile buff(CombatEffect effect, int amplifier) {
        return BuffProfile.of(effect, amplifier);
    }

    static {
        register(new DomainDef("hoe", "Harvest Field", "Wilayah Panen",
                Element.EARTH, Element.EARTH.color(),
                buff(CombatEffect.RESISTANCE, 0), buff(CombatEffect.SLOWDOWN, 1), 5, 4f));

        register(new DomainDef("blue_silver_grass", "Blue Silver Domain", "Domain Blue Silver",
                Element.NATURE, Element.NATURE.color(),
                buff(CombatEffect.REGENERATION, 0), buff(CombatEffect.POISON, 1), 5, 3f));

        register(new DomainDef("hell_civet", "Shadow Domain", "Wilayah Bayangan",
                Element.DARK, Element.DARK.color(),
                buff(CombatEffect.INVISIBILITY, 0), buff(CombatEffect.GLOWING, 0), 6, 4f));

        register(new DomainDef("sausage", "Banquet Domain", "Wilayah Perjamuan",
                Element.NONE, Element.NONE.color(),
                buff(CombatEffect.REGENERATION, 0), buff(CombatEffect.HUNGER, 0), 4, 2f));

        register(new DomainDef("seven_treasure_pagoda", "Pagoda Domain", "Wilayah Pagoda",
                Element.LIGHT, Element.LIGHT.color(),
                buff(CombatEffect.ABSORPTION, 1), buff(CombatEffect.BLINDNESS, 0), 6, 4f));

        register(new DomainDef("evil_fire_phoenix", "Cinder Domain", "Wilayah Bara",
                Element.FIRE, Element.FIRE.color(),
                buff(CombatEffect.FIRE_RESISTANCE, 0), buff(CombatEffect.SLOWDOWN, 1), 7, 6f));

        register(new DomainDef("white_tiger", "Snowfield Domain", "Wilayah Padang Salju",
                Element.LIGHT, Element.LIGHT.color(),
                buff(CombatEffect.STRENGTH, 1), buff(CombatEffect.WEAKNESS, 1), 6, 5f));

        register(new DomainDef("soft_bone_rabbit", "Mirage Domain", "Wilayah Bayangan Palsu",
                Element.WIND, Element.WIND.color(),
                buff(CombatEffect.SPEED, 1), buff(CombatEffect.BLINDNESS, 0), 5, 3f));

        register(new DomainDef("jade_serpent", "Venom Marsh Domain", "Wilayah Rawa Racun",
                Element.POISON, Element.POISON.color(),
                buff(CombatEffect.REGENERATION, 0), buff(CombatEffect.POISON, 2), 6, 4f));

        register(new DomainDef("clear_sky_hammer", "Clear Sky Domain", "Wilayah Langit Jernih",
                Element.EARTH, Element.EARTH.color(),
                buff(CombatEffect.STRENGTH, 1), buff(CombatEffect.WEAKNESS, 1), 7, 6f));

        register(new DomainDef("golden_dragon", "Molten Dominion", "Wilayah Bara Penyala",
                Element.FIRE, Element.FIRE.color(),
                buff(CombatEffect.STRENGTH, 1), buff(CombatEffect.SLOWDOWN, 2), 8, 8f));

        register(new DomainDef("abyssal_demon_dragon", "Starless Domain", "Wilayah Tanpa Bintang",
                Element.DARK, Element.DARK.color(),
                buff(CombatEffect.INVISIBILITY, 0), buff(CombatEffect.DARKNESS, 0), 8, 7f));

        register(new DomainDef("abyssal_ice_demon_dragon", "Silent Frost Domain", "Wilayah Frost Sunyi",
                Element.ICE, Element.ICE.color(),
                buff(CombatEffect.RESISTANCE, 1), buff(CombatEffect.SLOWDOWN, 2), 7, 6f));

        register(new DomainDef("ultimate_fire_dragon", "Caldera Domain", "Wilayah Kaldera",
                Element.FIRE, Element.FIRE.color(),
                buff(CombatEffect.FIRE_RESISTANCE, 1), buff(CombatEffect.SLOWDOWN, 2), 8, 8f));

        register(new DomainDef("ultimate_ice_dragon", "Deep Winter Domain", "Wilayah Musim Dingin",
                Element.ICE, Element.ICE.color(),
                buff(CombatEffect.RESISTANCE, 1), buff(CombatEffect.SLOWDOWN, 2), 7, 6f));

        register(new DomainDef("thunder_elephant", "Storm Domain", "Wilayah Badai",
                Element.LIGHTNING, Element.LIGHTNING.color(),
                buff(CombatEffect.SPEED, 1), buff(CombatEffect.WEAKNESS, 1), 7, 6f));

        register(new DomainDef("golden_ant", "Swarm Tunnel Domain", "Wilayah Terowongan",
                Element.EARTH, Element.EARTH.color(),
                buff(CombatEffect.SPEED, 1), buff(CombatEffect.POISON, 1), 5, 4f));

        register(new DomainDef("titan_giant_ape", "Quake Domain", "Wilayah Gempa",
                Element.EARTH, Element.EARTH.color(),
                buff(CombatEffect.RESISTANCE, 1), buff(CombatEffect.WEAKNESS, 2), 7, 6f));

        register(new DomainDef("sea_devil_manatee", "Undertow Domain", "Wilayah Arus Bawah",
                Element.WATER, Element.WATER.color(),
                buff(CombatEffect.REGENERATION, 1), buff(CombatEffect.SLOWDOWN, 1), 6, 4f));

        register(new DomainDef("bright_wing_moth", "Dazzle Domain", "Wilayah Silau",
                Element.WIND, Element.WIND.color(),
                buff(CombatEffect.SPEED, 1), buff(CombatEffect.BLINDNESS, 1), 5, 4f));

        register(new DomainDef("phoenix", "Reignition Domain", "Wilayah Menyala",
                Element.FIRE, Element.FIRE.color(),
                buff(CombatEffect.FIRE_RESISTANCE, 1), buff(CombatEffect.SLOWDOWN, 1), 7, 6f));

        register(new DomainDef("blue_silver_spear", "Grass Tide Domain", "Wilayah Arus Rumput",
                Element.NATURE, Element.NATURE.color(),
                buff(CombatEffect.REGENERATION, 1), buff(CombatEffect.POISON, 1), 6, 4f));

        register(new DomainDef("seven_kill_sword", "Killing God Domain", "Domain Pembunuh Dewa",
                Element.DARK, Element.DARK.color(),
                buff(CombatEffect.STRENGTH, 1), buff(CombatEffect.GLOWING, 0), 8, 7f));

        register(new DomainDef("giant_hammer", "Anvil Domain", "Wilayah Landasan",
                Element.EARTH, Element.EARTH.color(),
                buff(CombatEffect.RESISTANCE, 1), buff(CombatEffect.SLOWDOWN, 1), 6, 5f));

        register(new DomainDef("blood_lash", "Blood Ledger Domain", "Wilayah Catatan Darah",
                Element.DARK, Element.DARK.color(),
                buff(CombatEffect.SPEED, 1), buff(CombatEffect.WEAKNESS, 1), 6, 5f));

        register(new DomainDef("frost_needle", "Numbing Domain", "Wilayah Mati Rasa",
                Element.ICE, Element.ICE.color(),
                buff(CombatEffect.RESISTANCE, 1), buff(CombatEffect.SLOWDOWN, 2), 6, 5f));

        register(new DomainDef("thunder_spear", "Forked Current Domain", "Wilayah Arus Bercabang",
                Element.LIGHTNING, Element.LIGHTNING.color(),
                buff(CombatEffect.SPEED, 1), buff(CombatEffect.WEAKNESS, 1), 6, 5f));

        register(new DomainDef("spirit_sword", "Still Point Domain", "Wilayah Titik Diam",
                Element.LIGHT, Element.LIGHT.color(),
                buff(CombatEffect.STRENGTH, 1), buff(CombatEffect.BLINDNESS, 0), 5, 4f));

        register(new DomainDef("iron_anchor", "Mooring Domain", "Wilayah Tambat",
                Element.WATER, Element.WATER.color(),
                buff(CombatEffect.RESISTANCE, 1), buff(CombatEffect.SLOWDOWN, 2), 6, 4f));

        register(new DomainDef("bronze_mirror", "Reflected Field Domain", "Wilayah Arena Terpantul",
                Element.LIGHT, Element.LIGHT.color(),
                buff(CombatEffect.ABSORPTION, 1), buff(CombatEffect.BLINDNESS, 1), 6, 4f));

        register(new DomainDef("blue_silver_emperor", "Crown of Grass Domain", "Wilayah Mahkota Rumput",
                Element.NATURE, Element.NATURE.color(),
                buff(CombatEffect.REGENERATION, 1), buff(CombatEffect.POISON, 1), 7, 5f));

        register(new DomainDef("iron_body", "Bell Toll Domain", "Wilayah Lonceng",
                Element.EARTH, Element.EARTH.color(),
                buff(CombatEffect.RESISTANCE, 1), buff(CombatEffect.WEAKNESS, 1), 5, 4f));

        register(new DomainDef("bone_shell", "Bone Cage Domain", "Wilayah Kurungan Tulang",
                Element.EARTH, Element.EARTH.color(),
                buff(CombatEffect.ABSORPTION, 1), buff(CombatEffect.SLOWDOWN, 1), 5, 4f));

        register(new DomainDef("stone_skin", "Sinkhole Domain", "Wilayah Lubang Tenggelam",
                Element.EARTH, Element.EARTH.color(),
                buff(CombatEffect.RESISTANCE, 1), buff(CombatEffect.SLOWDOWN, 1), 5, 3f));

        register(new DomainDef("flesh_regrowth", "Red Bloom Domain", "Wilayah Bunga Merah",
                Element.NATURE, Element.NATURE.color(),
                buff(CombatEffect.REGENERATION, 2), buff(CombatEffect.POISON, 1), 5, 3f));

        register(new DomainDef("flame_body", "Pillar Domain", "Wilayah Pilar Api",
                Element.FIRE, Element.FIRE.color(),
                buff(CombatEffect.FIRE_RESISTANCE, 1), buff(CombatEffect.SLOWDOWN, 2), 7, 6f));

        register(new DomainDef("spirit_veins", "Circuit Domain", "Wilayah Sirkuit",
                Element.LIGHTNING, Element.LIGHTNING.color(),
                buff(CombatEffect.SPEED, 2), buff(CombatEffect.WEAKNESS, 1), 6, 5f));

        register(new DomainDef("soul_mirror", "Soul Mirror Domain", "Wilayah Cermin Soul",
                Element.LIGHT, Element.LIGHT.color(),
                buff(CombatEffect.ABSORPTION, 1), buff(CombatEffect.BLINDNESS, 1), 7, 5f));

        register(new DomainDef("spirit_lamp", "Lamplight Domain", "Wilayah Cahaya Lentera",
                Element.LIGHT, Element.LIGHT.color(),
                buff(CombatEffect.REGENERATION, 1), buff(CombatEffect.BLINDNESS, 0), 6, 4f));

        register(new DomainDef("soul_forge", "Forge Domain", "Wilayah Tempa",
                Element.FIRE, Element.FIRE.color(),
                buff(CombatEffect.STRENGTH, 1), buff(CombatEffect.SLOWDOWN, 2), 7, 6f));

        register(new DomainDef("star_array", "Constellation Domain", "Wilayah Konstelasi",
                Element.LIGHT, Element.LIGHT.color(),
                buff(CombatEffect.STRENGTH, 1), buff(CombatEffect.DARKNESS, 0), 8, 6f));

        register(new DomainDef("demon_eye", "Hex Domain", "Wilayah Kutukan",
                Element.DARK, Element.DARK.color(),
                buff(CombatEffect.INVISIBILITY, 0), buff(CombatEffect.GLOWING, 1), 7, 5f));

        register(new DomainDef("ancient_seal", "Ancient Seal Domain", "Wilayah Segel Kuno",
                Element.EARTH, Element.EARTH.color(),
                buff(CombatEffect.RESISTANCE, 1), buff(CombatEffect.WEAKNESS, 1), 6, 5f));
    }
}
