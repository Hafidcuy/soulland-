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
    private final String[] loadout = {"", "", "", "", ""};
    private int ringSkills = 0;
    private int chainCount = 0;
    private long chainUntil = 0L;
    private int bloodBurnTicks = 0;
    private boolean breakthroughReady = false;
    private boolean welcomeSeen = false;
    private int hintFlags = 0;
    private int contractKills = 0;
    private long contractUntil = 0L;
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

    @Override public int getRingSkills() { return ringSkills; }
    @Override public void setRingSkills(int count) { this.ringSkills = Math.max(0, Math.min(10, count)); }

    @Override public String[] getLoadout() { return loadout; }
    @Override public void setLoadout(String[] values) {
        for (int i = 0; i < loadout.length; i++) {
            loadout[i] = (values != null && i < values.length && values[i] != null) ? values[i] : "";
        }
    }

    @Override public String getLoadoutSkill(int slot) {
        if (slot < 0 || slot >= loadout.length) return "";
        return loadout[slot];
    }

    @Override public void setLoadoutSkill(int slot, String skillId) {
        if (slot < 0 || slot >= loadout.length) return;
        loadout[slot] = skillId == null ? "" : skillId;
    }

    @Override public int getChainCount() { return chainCount; }
    @Override public void setChainCount(int count) { this.chainCount = Math.max(0, count); }

    @Override public long getChainUntil() { return chainUntil; }
    @Override public void setChainUntil(long gameTime) { this.chainUntil = gameTime; }

    @Override public int getBloodBurnTicks() { return bloodBurnTicks; }
    @Override public void setBloodBurnTicks(int ticks) { this.bloodBurnTicks = Math.max(0, ticks); }

    @Override public int getContractKills() { return contractKills; }
    @Override public void setContractKills(int kills) { this.contractKills = Math.max(0, kills); }

    @Override public long getContractUntil() { return contractUntil; }
    @Override public void setContractUntil(long gameTime) { this.contractUntil = gameTime; }

    @Override public int getHintFlags() { return hintFlags; }
    @Override public void setHintFlags(int flags) { this.hintFlags = flags; }

    @Override public boolean isWelcomeSeen() { return welcomeSeen; }
    @Override public void setWelcomeSeen(boolean seen) { this.welcomeSeen = seen; }

    @Override public boolean isBreakthroughReady() { return breakthroughReady; }
    @Override public void setBreakthroughReady(boolean ready) { this.breakthroughReady = ready; }

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
        tag.putInt("chainCount", chainCount);
        tag.putLong("chainUntil", chainUntil);
        tag.putInt("bloodBurnTicks", bloodBurnTicks);
        tag.putInt("ringSkills", ringSkills);
        tag.putInt("contractKills", contractKills);
        tag.putLong("contractUntil", contractUntil);
        tag.putBoolean("breakthroughReady", breakthroughReady);
        tag.putBoolean("welcomeSeen", welcomeSeen);
        tag.putInt("hintFlags", hintFlags);
        for (int i = 0; i < loadout.length; i++) {
            tag.putString("loadout" + i, loadout[i] == null ? "" : loadout[i]);
        }
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
        chainCount = tag.getInt("chainCount");
        chainUntil = tag.getLong("chainUntil");
        bloodBurnTicks = tag.getInt("bloodBurnTicks");
        ringSkills = tag.getInt("ringSkills");
        contractKills = tag.getInt("contractKills");
        contractUntil = tag.getLong("contractUntil");
        breakthroughReady = tag.getBoolean("breakthroughReady");
        welcomeSeen = tag.getBoolean("welcomeSeen");
        hintFlags = tag.getInt("hintFlags");
        for (int i = 0; i < loadout.length; i++) {
            loadout[i] = tag.getString("loadout" + i);
        }
        cooldowns.clear();
        if (tag.contains("cooldowns")) {
            CompoundTag cds = tag.getCompound("cooldowns");
            for (String key : cds.getAllKeys()) cooldowns.put(key, cds.getLong(key));
        }
    }
}
