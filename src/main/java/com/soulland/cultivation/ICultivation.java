package com.soulland.cultivation;

import net.minecraft.nbt.CompoundTag;

/** Data kultivasi seorang pemain: level spirit power, martial soul, spirit ring, energi, cooldown. */
public interface ICultivation {
    boolean isAwakened();
    void setAwakened(boolean awakened);

    String getMartialSoulId();
    void setMartialSoulId(String id);

    int getLevel();
    void setLevel(int level);

    int getXp();
    void setXp(int xp);

    int getRings();
    void setRings(int rings);

    float getEnergy();
    void setEnergy(float energy);

    long getCooldownUntil(String skillId);
    void setCooldown(String skillId, long gameTimeUntil);

    /** Memberi akses ke semua cooldown yang tersimpan, dipakai untuk sinkronisasi ke client. */
    void forEachCooldown(java.util.function.BiConsumer<String, Long> consumer);

    boolean isGateNotified();
    void setGateNotified(boolean notified);

    /** Umur tiap spirit ring yang diserap (dalam tahun), urut dari ring pertama. Panjangnya = jumlah ring. */
    int[] getRingAges();

    void setRingAges(int[] ages);

    /** Menambah satu spirit ring baru beserta umurnya; jumlah ring dan daftar umur selalu sinkron. */
    void addRing(int age);

    CompoundTag serializeNBT();
    void deserializeNBT(CompoundTag nbt);
}
