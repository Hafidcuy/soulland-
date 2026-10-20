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

    /** Id sekte yang dipilih pemain, kosong kalau belum memilih. */
    String getSect();
    void setSect(String sect);

    /** Tahap evolusi martial soul: 0 = bentuk dasar, 1 = bentuk evolusi. */
    int getEvolutionStage();
    void setEvolutionStage(int stage);

    /** True selama pemain sedang bermeditasi. */
    boolean isMeditating();
    void setMeditating(boolean meditating);

    /** Tick meditasi yang sudah terakumulasi tanpa terputus. */
    int getMeditationTicks();
    void setMeditationTicks(int ticks);

    CompoundTag serializeNBT();
    void deserializeNBT(CompoundTag nbt);
}
