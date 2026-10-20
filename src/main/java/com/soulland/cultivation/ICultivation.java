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

    boolean isGateNotified();
    void setGateNotified(boolean notified);

    CompoundTag serializeNBT();
    void deserializeNBT(CompoundTag nbt);
}
