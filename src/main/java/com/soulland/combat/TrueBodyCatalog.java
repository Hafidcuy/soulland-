package com.soulland.combat;

import com.soulland.soul.Element;
import com.soulland.soul.SoulLineage;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Katalog True Body untuk 43 martial soul.
 *
 * <p>Setiap soul punya tepat satu definisi True Body. Angkanya adalah tabel
 * yang sudah diuji, bukan hasil hitungan runtime: pengali damage, profil buff
 * diri sendiri, aura untuk sekutu, aura untuk musuh, radius aura, dan biaya
 * spirit power.
 *
 * <p>Profil per lineage: Beast ditekan lewat Ceram (damage besar, aura luas),
 * Tool memusatkan pukulan (damage menengah, aura kecil), Body menahan
 * (damage kecil, ketahanan besar), dan Special memakai efek unik sesuai
 * elemennya.
 */
public final class TrueBodyCatalog {
    private TrueBodyCatalog() {}

    private static final Map<String, TrueBodyDef> REGISTRY = new LinkedHashMap<>();

    /** Definisi True Body milik sebuah soul, atau null kalau soul itu tidak punya. */
    public static TrueBodyDef get(String soulId) {
        if (soulId == null) return null;
        return REGISTRY.get(soulId);
    }

    /** Semua definisi True Body, urut sesuai registry soul. */
    public static List<TrueBodyDef> all() {
        return new ArrayList<>(REGISTRY.values());
    }

    /** Jumlah soul yang punya True Body. */
    public static int registeredCount() {
        return REGISTRY.size();
    }

    private static void register(TrueBodyDef def) {
        REGISTRY.put(def.soulId(), def);
    }

    private static BuffProfile buff(CombatEffect effect, int amplifier) {
        return BuffProfile.of(effect, amplifier);
    }

    static {
        register(new TrueBodyDef("hoe", "Furrow Sovereign", "Penguasa Bajak",
                SoulLineage.TOOL, Element.EARTH, 0xB08954,
                1.28f, buff(CombatEffect.STRENGTH, 1), buff(CombatEffect.HASTE, 0),
                buff(CombatEffect.WEAKNESS, 0), 6.0, 34f));

        register(new TrueBodyDef("blue_silver_grass", "Blue Silver Emperor Form", "Bentuk Kaisar Blue Silver",
                SoulLineage.BODY, Element.NATURE, 0x4A7BC8,
                1.20f, buff(CombatEffect.REGENERATION, 1), buff(CombatEffect.REGENERATION, 0),
                BuffProfile.none(), 6.0, 36f));

        register(new TrueBodyDef("hell_civet", "Nine Lives Shade", "Bayang Sembilan Nyawa",
                SoulLineage.BEAST, Element.DARK, 0x5B4A8A,
                1.50f, buff(CombatEffect.STRENGTH, 1), buff(CombatEffect.SPEED, 0),
                buff(CombatEffect.SLOWDOWN, 1), 8.0, 42f));

        register(new TrueBodyDef("sausage", "Feast Sovereign", "Penguasa Perjamuan",
                SoulLineage.SPECIAL, Element.NONE, 0xE08A4C,
                1.30f, buff(CombatEffect.REGENERATION, 1), buff(CombatEffect.REGENERATION, 0),
                buff(CombatEffect.HUNGER, 0), 7.0, 36f));

        register(new TrueBodyDef("seven_treasure_pagoda", "Seven Treasure Sovereign", "Penguasa Tujuh Harta",
                SoulLineage.SPECIAL, Element.LIGHT, 0xE8C46A,
                1.34f, buff(CombatEffect.ABSORPTION, 1), buff(CombatEffect.ABSORPTION, 1),
                BuffProfile.none(), 8.0, 40f));

        register(new TrueBodyDef("evil_fire_phoenix", "Malfire Phoenix Form", "Bentuk Phoenix Api Jahat",
                SoulLineage.BEAST, Element.FIRE, 0xFF6A3C,
                1.55f, buff(CombatEffect.FIRE_RESISTANCE, 1), buff(CombatEffect.STRENGTH, 0),
                buff(CombatEffect.SLOWDOWN, 1), 9.0, 48f));

        register(new TrueBodyDef("white_tiger", "Snowfield Tiger Form", "Bentuk Harimau Padang Salju",
                SoulLineage.BEAST, Element.LIGHT, 0xD8DCE8,
                1.48f, buff(CombatEffect.STRENGTH, 2), buff(CombatEffect.STRENGTH, 1),
                buff(CombatEffect.WEAKNESS, 1), 9.0, 45f));

        register(new TrueBodyDef("soft_bone_rabbit", "Softbone Mirage", "Bayangan Tulang Lunak",
                SoulLineage.BEAST, Element.WIND, 0xF0A8C0,
                1.45f, buff(CombatEffect.SPEED, 2), buff(CombatEffect.SPEED, 0),
                buff(CombatEffect.BLINDNESS, 0), 8.0, 40f));

        register(new TrueBodyDef("jade_serpent", "Jade Venom Sovereign", "Penguasa Racun Giok",
                SoulLineage.BEAST, Element.POISON, 0x3CC88A,
                1.42f, buff(CombatEffect.REGENERATION, 1), buff(CombatEffect.REGENERATION, 0),
                buff(CombatEffect.POISON, 1), 8.0, 42f));

        register(new TrueBodyDef("clear_sky_hammer", "Clear Sky Overlord", "Penguasa Langit Jernih",
                SoulLineage.TOOL, Element.EARTH, 0x7A8AA8,
                1.35f, buff(CombatEffect.STRENGTH, 2), buff(CombatEffect.STRENGTH, 1),
                buff(CombatEffect.WEAKNESS, 1), 8.0, 42f));

        register(new TrueBodyDef("golden_dragon", "Sunscale Wyrm", "Naga Sisik Matahari",
                SoulLineage.BEAST, Element.FIRE, 0xE8A33C,
                1.60f, buff(CombatEffect.STRENGTH, 2), buff(CombatEffect.STRENGTH, 1),
                buff(CombatEffect.SLOWDOWN, 1), 10.0, 50f));

        register(new TrueBodyDef("abyssal_demon_dragon", "Abyssal Starwyrm", "Naga Bintang Jurang",
                SoulLineage.BEAST, Element.DARK, 0x6B3FA0,
                1.58f, buff(CombatEffect.STRENGTH, 2), buff(CombatEffect.ABSORPTION, 0),
                buff(CombatEffect.DARKNESS, 0), 10.0, 50f));

        register(new TrueBodyDef("abyssal_ice_demon_dragon", "Rime Demon Sovereign", "Penguasa Demon Es",
                SoulLineage.BEAST, Element.ICE, 0x5AA8D8,
                1.52f, buff(CombatEffect.RESISTANCE, 1), buff(CombatEffect.RESISTANCE, 0),
                buff(CombatEffect.SLOWDOWN, 2), 9.0, 46f));

        register(new TrueBodyDef("ultimate_fire_dragon", "Caldera Dragon Form", "Bentuk Naga Kaldera",
                SoulLineage.BEAST, Element.FIRE, 0xE0522A,
                1.56f, buff(CombatEffect.FIRE_RESISTANCE, 1), buff(CombatEffect.STRENGTH, 0),
                buff(CombatEffect.SLOWDOWN, 1), 10.0, 50f));

        register(new TrueBodyDef("ultimate_ice_dragon", "Glacier Sovereign", "Penguasa Gletser",
                SoulLineage.BEAST, Element.ICE, 0x8FC8E8,
                1.50f, buff(CombatEffect.RESISTANCE, 1), buff(CombatEffect.RESISTANCE, 0),
                buff(CombatEffect.SLOWDOWN, 2), 9.0, 45f));

        register(new TrueBodyDef("thunder_elephant", "Thunder Herd Sovereign", "Penguasa Kawanan Petir",
                SoulLineage.BEAST, Element.LIGHTNING, 0x7C9BE0,
                1.54f, buff(CombatEffect.STRENGTH, 1), buff(CombatEffect.STRENGTH, 0),
                buff(CombatEffect.WEAKNESS, 1), 9.0, 44f));

        register(new TrueBodyDef("golden_ant", "Hollow Hill Form", "Bentuk Bukit Rongga",
                SoulLineage.BEAST, Element.EARTH, 0xC9A227,
                1.40f, buff(CombatEffect.SPEED, 1), buff(CombatEffect.SPEED, 0),
                buff(CombatEffect.POISON, 1), 7.0, 40f));

        register(new TrueBodyDef("titan_giant_ape", "Tremor Sovereign", "Penguasa Guncang",
                SoulLineage.BEAST, Element.EARTH, 0x9A7048,
                1.57f, buff(CombatEffect.STRENGTH, 2), buff(CombatEffect.RESISTANCE, 0),
                buff(CombatEffect.WEAKNESS, 1), 9.0, 47f));

        register(new TrueBodyDef("sea_devil_manatee", "Deep Tether Form", "Bentuk Tali Dasar",
                SoulLineage.BEAST, Element.WATER, 0x3C7A8C,
                1.38f, buff(CombatEffect.REGENERATION, 1), buff(CombatEffect.REGENERATION, 0),
                buff(CombatEffect.POISON, 1), 8.0, 40f));

        register(new TrueBodyDef("bright_wing_moth", "Night of Wings Form", "Bentuk Malam Sayap",
                SoulLineage.BEAST, Element.WIND, 0xE8D48C,
                1.44f, buff(CombatEffect.SPEED, 2), buff(CombatEffect.SPEED, 0),
                buff(CombatEffect.BLINDNESS, 0), 8.0, 41f));

        register(new TrueBodyDef("phoenix", "Reignited Sovereign", "Penguasa Menyala Lagi",
                SoulLineage.BEAST, Element.FIRE, 0xF0B24A,
                1.53f, buff(CombatEffect.FIRE_RESISTANCE, 1), buff(CombatEffect.REGENERATION, 0),
                buff(CombatEffect.SLOWDOWN, 1), 9.0, 46f));

        register(new TrueBodyDef("blue_silver_spear", "Emperor Reach Form", "Bentuk Jangkauan Kaisar",
                SoulLineage.TOOL, Element.NATURE, 0x3E6FB8,
                1.30f, buff(CombatEffect.STRENGTH, 1), buff(CombatEffect.STRENGTH, 0),
                buff(CombatEffect.WEAKNESS, 0), 7.0, 36f));

        register(new TrueBodyDef("seven_kill_sword", "Seven Notch Sovereign", "Penguasa Tujuh Takik",
                SoulLineage.TOOL, Element.DARK, 0x3A3040,
                1.38f, buff(CombatEffect.STRENGTH, 2), buff(CombatEffect.STRENGTH, 1),
                buff(CombatEffect.WEAKNESS, 1), 7.0, 42f));

        register(new TrueBodyDef("giant_hammer", "Anvil Sovereign", "Penguasa Landasan",
                SoulLineage.TOOL, Element.EARTH, 0x7A6A58,
                1.32f, buff(CombatEffect.STRENGTH, 1), buff(CombatEffect.RESISTANCE, 0),
                buff(CombatEffect.WEAKNESS, 0), 7.0, 38f));

        register(new TrueBodyDef("blood_lash", "Cord of Ends Form", "Bentuk Tali Akhir",
                SoulLineage.TOOL, Element.DARK, 0x8E2434,
                1.26f, buff(CombatEffect.SPEED, 1), buff(CombatEffect.SPEED, 0),
                buff(CombatEffect.WEAKNESS, 0), 6.0, 34f));

        register(new TrueBodyDef("frost_needle", "Winter Needle Form", "Bentuk Jarum Musim Dingin",
                SoulLineage.TOOL, Element.ICE, 0x9AD8E8,
                1.24f, buff(CombatEffect.RESISTANCE, 1), buff(CombatEffect.RESISTANCE, 0),
                buff(CombatEffect.SLOWDOWN, 1), 6.0, 34f));

        register(new TrueBodyDef("thunder_spear", "Stormhead Form", "Bentuk Kepala Badai",
                SoulLineage.TOOL, Element.LIGHTNING, 0x6E8FE0,
                1.29f, buff(CombatEffect.SPEED, 1), buff(CombatEffect.SPEED, 0),
                buff(CombatEffect.WEAKNESS, 0), 7.0, 36f));

        register(new TrueBodyDef("spirit_sword", "Quiet Blade Form", "Bentuk Bilah Senyap",
                SoulLineage.TOOL, Element.LIGHT, 0xB8C4D4,
                1.25f, buff(CombatEffect.STRENGTH, 1), buff(CombatEffect.STRENGTH, 0),
                buff(CombatEffect.WEAKNESS, 0), 6.0, 33f));

        register(new TrueBodyDef("iron_anchor", "Drowned Anchor Form", "Bentuk Jangkar Tenggelam",
                SoulLineage.TOOL, Element.WATER, 0x5E6A6E,
                1.22f, buff(CombatEffect.RESISTANCE, 1), buff(CombatEffect.RESISTANCE, 0),
                buff(CombatEffect.SLOWDOWN, 1), 6.0, 33f));

        register(new TrueBodyDef("bronze_mirror", "Truth Bearer Form", "Bentuk Pembawa Kebenaran",
                SoulLineage.TOOL, Element.LIGHT, 0xC08A4A,
                1.27f, buff(CombatEffect.RESISTANCE, 1), buff(CombatEffect.ABSORPTION, 0),
                buff(CombatEffect.BLINDNESS, 0), 7.0, 35f));

        register(new TrueBodyDef("blue_silver_emperor", "Ten Thousand Year Sovereign", "Penguasa Sepuluh Ribu Tahun",
                SoulLineage.BODY, Element.NATURE, 0x2E5C9E,
                1.24f, buff(CombatEffect.REGENERATION, 1), buff(CombatEffect.REGENERATION, 1),
                buff(CombatEffect.SLOWDOWN, 1), 7.0, 40f));

        register(new TrueBodyDef("iron_body", "Ironclad Form", "Bentuk Baja Berlapis",
                SoulLineage.BODY, Element.EARTH, 0x6E7480,
                1.16f, buff(CombatEffect.RESISTANCE, 2), buff(CombatEffect.RESISTANCE, 1),
                BuffProfile.none(), 5.0, 34f));

        register(new TrueBodyDef("bone_shell", "Carapace Sovereign", "Penguasa Cangkang",
                SoulLineage.BODY, Element.EARTH, 0xD6CBB0,
                1.18f, buff(CombatEffect.RESISTANCE, 2), buff(CombatEffect.ABSORPTION, 0),
                BuffProfile.none(), 5.0, 35f));

        register(new TrueBodyDef("stone_skin", "Bedrock Form", "Bentuk Batu Dasar",
                SoulLineage.BODY, Element.EARTH, 0x8A8578,
                1.12f, buff(CombatEffect.RESISTANCE, 1), buff(CombatEffect.RESISTANCE, 0),
                BuffProfile.none(), 5.0, 30f));

        register(new TrueBodyDef("flesh_regrowth", "Bloom Sovereign", "Penguasa Bunga",
                SoulLineage.BODY, Element.NATURE, 0xC2687E,
                1.15f, buff(CombatEffect.REGENERATION, 2), buff(CombatEffect.REGENERATION, 1),
                BuffProfile.none(), 6.0, 34f));

        register(new TrueBodyDef("flame_body", "Ashen Form", "Bentuk Abu",
                SoulLineage.BODY, Element.FIRE, 0xD4562A,
                1.21f, buff(CombatEffect.FIRE_RESISTANCE, 2), buff(CombatEffect.FIRE_RESISTANCE, 0),
                buff(CombatEffect.SLOWDOWN, 1), 6.0, 37f));

        register(new TrueBodyDef("spirit_veins", "Closed Circuit Form", "Bentuk Sirkuit Tertutup",
                SoulLineage.BODY, Element.LIGHTNING, 0x8FB8D8,
                1.19f, buff(CombatEffect.SPEED, 2), buff(CombatEffect.SPEED, 1),
                buff(CombatEffect.WEAKNESS, 0), 6.0, 35f));

        register(new TrueBodyDef("soul_mirror", "Shattered Image Form", "Bentuk Gambaran Pecah",
                SoulLineage.SPECIAL, Element.LIGHT, 0xE4DCF0,
                1.32f, buff(CombatEffect.INVISIBILITY, 0), buff(CombatEffect.ABSORPTION, 0),
                buff(CombatEffect.BLINDNESS, 0), 8.0, 40f));

        register(new TrueBodyDef("spirit_lamp", "Everlight Form", "Bentuk Cahaya Abadi",
                SoulLineage.SPECIAL, Element.LIGHT, 0xE8C878,
                1.30f, buff(CombatEffect.ABSORPTION, 1), buff(CombatEffect.REGENERATION, 0),
                buff(CombatEffect.BLINDNESS, 0), 7.0, 38f));

        register(new TrueBodyDef("soul_forge", "Tempered Soul Form", "Bentuk Soul Temper",
                SoulLineage.SPECIAL, Element.FIRE, 0xB05A2A,
                1.36f, buff(CombatEffect.FIRE_RESISTANCE, 1), buff(CombatEffect.STRENGTH, 1),
                buff(CombatEffect.SLOWDOWN, 1), 8.0, 42f));

        register(new TrueBodyDef("star_array", "Collapsed Array Form", "Bentuk Susunan Runtuh",
                SoulLineage.SPECIAL, Element.LIGHT, 0xF2E9B0,
                1.42f, buff(CombatEffect.ABSORPTION, 1), buff(CombatEffect.STRENGTH, 1),
                buff(CombatEffect.DARKNESS, 0), 9.0, 45f));

        register(new TrueBodyDef("demon_eye", "Open Eye Form", "Bentuk Mata Terbuka",
                SoulLineage.SPECIAL, Element.DARK, 0x7A4A8C,
                1.33f, buff(CombatEffect.INVISIBILITY, 0), buff(CombatEffect.HASTE, 0),
                buff(CombatEffect.GLOWING, 0), 8.0, 40f));

        register(new TrueBodyDef("ancient_seal", "Unbroken Seal Form", "Bentuk Segel Tak Terputus",
                SoulLineage.SPECIAL, Element.EARTH, 0x8C7A4A,
                1.35f, buff(CombatEffect.RESISTANCE, 1), buff(CombatEffect.RESISTANCE, 1),
                buff(CombatEffect.WEAKNESS, 1), 8.0, 40f));
    }
}
