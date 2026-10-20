package com.soulland.soul;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Elemen tempur dan aturan lawannya.
 *
 * <p>Setiap soul beast punya elemen yang tertulis pada namanya, jadi pemain bisa membaca
 * target dan memilih soul yang mengalahinya. Elemen pemain datang dari martial soul-nya.
 */
public enum Element {

    NONE("Tanpa Elemen", 0xC8C8C8),
    FIRE("Api", 0xFF8A3C),
    ICE("Es", 0xA8E4FF),
    LIGHTNING("Petir", 0x9BC8FF),
    WIND("Angin", 0xC8F0D8),
    WATER("Air", 0x5BC8E0),
    EARTH("Tanah", 0xB08A55),
    POISON("Racun", 0x7BE0B0),
    NATURE("Tumbuhan", 0x8FD694),
    LIGHT("Cahaya", 0xFFF3C4),
    DARK("Gelap", 0x8B7AC8);

    private final String label;
    private final int color;
    private final List<Element> beats = new ArrayList<>();

    Element(String label, int color) {
        this.label = label;
        this.color = color;
    }

    public String label() {
        return label;
    }

    public int color() {
        return color;
    }

    /** True kalau elemen ini mengalahi elemen lain. */
    public boolean beats(Element other) {
        return beats.contains(other);
    }

    /**
     * Pengali damage pemain terhadap musuh.
     *
     * <p>Unsur-unsurnya: 1.25x kalau elemen pemain menang, 0.85x kalau kalah, 1.0x netral.
     */
    public double multiplierAgainst(Element other) {
        if (other == null || other == NONE) return 1.0;
        if (beats(other)) return 1.25;
        if (other.beats(this)) return 0.85;
        return 1.0;
    }

    private void setBeats(Element... others) {
        beats.addAll(Arrays.asList(others));
    }

    static {
        FIRE.setBeats(EARTH, NATURE);
        ICE.setBeats(WATER, WIND);
        LIGHTNING.setBeats(WIND, WATER);
        WIND.setBeats(FIRE, NATURE);
        WATER.setBeats(FIRE, EARTH);
        EARTH.setBeats(LIGHTNING, POISON);
        POISON.setBeats(NATURE, WATER);
        NATURE.setBeats(WATER, EARTH);
        LIGHT.setBeats(DARK);
    }

    /** Elemen tempur dari nama elemen martial soul. */
    public static Element fromSoulElement(String soulElement) {
        if (soulElement == null) return NONE;
        return switch (soulElement) {
            case "Fire" -> FIRE;
            case "Ice" -> ICE;
            case "Lightning" -> LIGHTNING;
            case "Wind" -> WIND;
            case "Water" -> WATER;
            case "Earth" -> EARTH;
            case "Poison" -> POISON;
            case "Life" -> NATURE;
            case "Light" -> LIGHT;
            case "Dark" -> DARK;
            default -> NONE;
        };
    }

    /** Daftar elemen yang bisa muncul pada soul beast, untuk generate nama. */
    public static List<Element> beastElements() {
        return List.of(FIRE, ICE, LIGHTNING, WIND, WATER, EARTH, POISON, NATURE, LIGHT, DARK);
    }
}
