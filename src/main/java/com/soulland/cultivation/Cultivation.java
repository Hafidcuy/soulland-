package com.soulland.cultivation;

import com.soulland.CultivationRanks;
import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;
import java.util.Map;

public class Cultivation implements ICultivation {
    private boolean awakened = false;
    private String martialSoulId = "";
    private int level = 1;
    private int xp = 0;
    private int rings = 0;
    private float energy = CultivationRanks.MAX_ENERGY;
    private boolean gateNotified = false;
    private String sect = "";
    private int evolutionStage = 0;
    private boolean meditating = false;
    private int meditationTicks = 0;
    private final Map<String, Long> cooldowns = new HashMap<>();

    @Override public boolean isAwakened() { return awakened; }
    @Override public void setAwakened(boolean awakened) { this.awakened = awakened; }

    @Override public String getMartialSoulId() { return martialSoulId; }
    @Override public void setMartialSoulId(String id) { this.martialSoulId = id == null ? "" : id; }

    @Override public int getLevel() { return level; }
    @Override public void setLevel(int level) { this.level = Math.max(1, Math.min(CultivationRanks.MAX_LEVEL, level)); }

    @Override public int getXp() { return xp; }
    @Override public void setXp(int xp) { this.xp = Math.max(0, xp); }

    @Override public int getRings() { return rings; }
    @Override public void setRings(int rings) { this.rings = Math.max(0, Math.min(CultivationRanks.MAX_RINGS, rings)); }

    @Override public float getEnergy() { return energy; }
    @Override public void setEnergy(float energy) { this.energy = Math.max(0f, Math.min(CultivationRanks.MAX_ENERGY, energy)); }

    @Override public long getCooldownUntil(String skillId) { return cooldowns.getOrDefault(skillId, 0L); }
    @Override public void setCooldown(String skillId, long gameTimeUntil) { cooldowns.put(skillId, gameTimeUntil); }
    @Override public void forEachCooldown(java.util.function.BiConsumer<String, Long> consumer) { cooldowns.forEach(consumer); }

    @Override public boolean isGateNotified() { return gateNotified; }
    @Override public void setGateNotified(boolean notified) { this.gateNotified = notified; }

    @Override public String getSect() { return sect; }
    @Override public void setSect(String sect) { this.sect = sect == null ? "" : sect; }

    @Override public int getEvolutionStage() { return evolutionStage; }
    @Override public void setEvolutionStage(int stage) { this.evolutionStage = Math.max(0, Math.min(1, stage)); }

    @Override public boolean isMeditating() { return meditating; }
    @Override public void setMeditating(boolean meditating) { this.meditating = meditating; }

    @Override public int getMeditationTicks() { return meditationTicks; }
    @Override public void setMeditationTicks(int ticks) { this.meditationTicks = Math.max(0, ticks); }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("awakened", awakened);
        tag.putString("martialSoul", martialSoulId);
        tag.putInt("level", level);
        tag.putInt("xp", xp);
        tag.putInt("rings", rings);
        tag.putFloat("energy", energy);
        tag.putBoolean("gateNotified", gateNotified);
        tag.putString("sect", sect);
        tag.putInt("evolutionStage", evolutionStage);
        CompoundTag cds = new CompoundTag();
        cooldowns.forEach((k, v) -> cds.putLong(k, v));
        tag.put("cooldowns", cds);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        awakened = tag.getBoolean("awakened");
        martialSoulId = tag.getString("martialSoul");
        level = tag.getInt("level");
        xp = tag.getInt("xp");
        rings = tag.getInt("rings");
        energy = tag.contains("energy") ? tag.getFloat("energy") : CultivationRanks.MAX_ENERGY;
        gateNotified = tag.getBoolean("gateNotified");
        sect = tag.getString("sect");
        evolutionStage = tag.getInt("evolutionStage");
        cooldowns.clear();
        if (tag.contains("cooldowns")) {
            CompoundTag cds = tag.getCompound("cooldowns");
            for (String key : cds.getAllKeys()) cooldowns.put(key, cds.getLong(key));
        }
    }
}
