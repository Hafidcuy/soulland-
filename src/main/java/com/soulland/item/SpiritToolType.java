package com.soulland.item;

/**
 * Sepuluh arketipe tool soul. Tiap soul punya satu arketipe dan tiap arketipe
 * dipakai satu soul, jadi tidak ada dua tool dengan siluet yang sama.
 *
 * <p>Urutan konstan: damage dasar dan kecepatan serang per arketipe.
 */
public enum SpiritToolType {
    SWORD("sword", "hoe", 4, 1.6f, 3.2f),
    SPEAR("spear", "blue_silver_grass", 4, 1.4f, 3.6f),
    FANG("fang", "hell_civet", 3, 1.9f, 2.8f),
    FAN("fan", "sausage", 3, 1.8f, 3.0f),
    SEAL("seal", "seven_treasure_pagoda", 4, 1.3f, 3.4f),
    BOW("bow", "evil_fire_phoenix", 3, 1.1f, 4.0f),
    CLAW("claw", "white_tiger", 3, 2.0f, 2.6f),
    STAFF("staff", "soft_bone_rabbit", 4, 1.5f, 3.4f),
    DAGGER("dagger", "jade_serpent", 2, 2.2f, 2.4f),
    HAMMER("hammer", "clear_sky_hammer", 6, 0.9f, 4.0f);

    private final String id;
    private final String soulId;
    private final int baseDamage;
    private final float attackSpeed;
    private final float chargeRadius;

    SpiritToolType(String id, String soulId, int baseDamage, float attackSpeed, float chargeRadius) {
        this.id = id;
        this.soulId = soulId;
        this.baseDamage = baseDamage;
        this.attackSpeed = attackSpeed;
        this.chargeRadius = chargeRadius;
    }

    public String id() {
        return id;
    }

    public String soulId() {
        return soulId;
    }

    public int baseDamage() {
        return baseDamage;
    }

    public float attackSpeed() {
        return attackSpeed;
    }

    /** Radius serangan bermuatan, tiap arketipe punya jarak sendiri. */
    public float chargeRadius() {
        return chargeRadius;
    }

    public static SpiritToolType bySoul(String soulId) {
        for (SpiritToolType type : values()) {
            if (type.soulId.equals(soulId)) return type;
        }
        return null;
    }
}
