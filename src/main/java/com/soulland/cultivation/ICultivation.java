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

    /** Lima slot tombol skill, diisi skill bebas. Kosong berarti pakai urutan bawaan. */
    String[] getLoadout();
    void setLoadout(String[] loadout);

    /** Berapa skill spirit ring yang sudah terbuka. Normally sama dengan jumlah ring. */
    int getRingSkills();
    void setRingSkills(int count);

    /** Skill yang terpasang di slot tertentu, atau null kalau slot pakai urutan bawaan. */
    String getLoadoutSkill(int slot);
    void setLoadoutSkill(int slot, String skillId);

    /** Mata rantai soul yang sedang aktif. */
    int getChainCount();
    void setChainCount(int count);

    /** Waktu game saat rantai terakhir ditambahkan. */
    long getChainUntil();
    void setChainUntil(long gameTime);

    /** Sisa durasi bakar darah, dalam tick. */
    int getBloodBurnTicks();
    void setBloodBurnTicks(int ticks);

    /** Bitmask arahan yang sudah pernah ditampilkan ke pemain ini. */
    int getHintFlags();
    void setHintFlags(int flags);

    /** True kalau layar sambutan sudah pernah dilihat, supaya tidak muncul terus. */
    boolean isWelcomeSeen();
    void setWelcomeSeen(boolean seen);

    /** True kalau ritual terobosan sudah dituntaskan, siap naik peringkat. */
    boolean isBreakthroughReady();
    void setBreakthroughReady(boolean ready);

    /** Jumlah soul beast yang sudah diburu dalam kontrak perburuan berjalan. */
    int getContractKills();
    void setContractKills(int kills);

    /** Waktu game saat kontrak perburuan berakhir. */
    long getContractUntil();
    void setContractUntil(long gameTime);

    /** Bahasa yang dipilih pemain: "id" atau "en". Kosong kalau belum memilih. */
    String getLanguage();
    void setLanguage(String language);

    CompoundTag serializeNBT();
    void deserializeNBT(CompoundTag nbt);
}
