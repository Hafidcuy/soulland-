package com.soulland.bone;

/**
 * Tujuh tempat tubuh yang bisa dipakai tulang spirit.
 *
 * <p>Enam slot pertama mengikuti susunan tubuh: kepala, rongga dada, dua
 * lengan, dua kaki. Slot ketujuh, Tulang Eksternal, adalah tambahan kita:
 * tulang yang tidak berasal dari tubuh mana pun, jadi berdiri di luar kerangka.
 *
 * <p>Tiap slot punya pengali untuk empat bonus stat. Pengali lengan kiri dan
 * kanan sengaja sama karena perbedaan keduanya harus datang dari bentuk
 * aset, bukan dari angka supaya dua item itu tidak terasa kembar secara
 * mekanik.
 */
public enum SpiritBoneSlot {
    SKULL("skull", 1.5f, 1.2f, 1.4f, 1.0f),
    TORSO("torso", 1.0f, 1.8f, 1.2f, 1.3f),
    LEFT_ARM("left_arm", 1.1f, 0.8f, 0.8f, 0.9f),
    RIGHT_ARM("right_arm", 1.1f, 0.8f, 0.8f, 0.9f),
    LEFT_LEG("left_leg", 0.7f, 1.0f, 0.9f, 1.1f),
    RIGHT_LEG("right_leg", 0.7f, 1.0f, 0.9f, 1.1f),
    EXTERNAL("external", 2.0f, 1.5f, 1.5f, 1.5f);

    /** Jumlah slot tubuh yang bisa dipakai tulang spirit. */
    public static final int COUNT = values().length;

    private final String id;
    private final float attackMultiplier;
    private final float defenseMultiplier;
    private final float energyMultiplier;
    private final float regenMultiplier;

    SpiritBoneSlot(String id, float attackMultiplier, float defenseMultiplier, float energyMultiplier,
                   float regenMultiplier) {
        this.id = id;
        this.attackMultiplier = attackMultiplier;
        this.defenseMultiplier = defenseMultiplier;
        this.energyMultiplier = energyMultiplier;
        this.regenMultiplier = regenMultiplier;
    }

    /** Id slot, dipakai untuk id item dan kunci terjemahan. */
    public String id() {
        return id;
    }

    /** Pengali bonus serangan slot ini. */
    public float attackMultiplier() {
        return attackMultiplier;
    }

    /** Pengali bonus defense slot ini. */
    public float defenseMultiplier() {
        return defenseMultiplier;
    }

    /** Pengali bonus spirit power slot ini. */
    public float energyMultiplier() {
        return energyMultiplier;
    }

    /** Pengali regenerasi HP slot ini. */
    public float regenMultiplier() {
        return regenMultiplier;
    }

    /** Slot dari id-nya, atau null kalau id tidak dikenal. */
    public static SpiritBoneSlot byId(String id) {
        if (id == null) return null;
        for (SpiritBoneSlot slot : values()) {
            if (slot.id.equals(id)) return slot;
        }
        return null;
    }

    /** Slot dari indeksnya di daftar, atau null kalau indeks di luar rentang. */
    public static SpiritBoneSlot byIndex(int index) {
        SpiritBoneSlot[] all = values();
        if (index < 0 || index >= all.length) return null;
        return all[index];
    }

    /** Indeks slot di daftar, untuk packet. */
    public int index() {
        return ordinal();
    }
}
