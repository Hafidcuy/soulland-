package com.soulland.cultivation;

import com.soulland.CultivationRanks;
import net.minecraft.nbt.CompoundTag;

import java.util.Arrays;
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
    private final Map<String, Long> cooldowns = new HashMap<>();
    /** Umur tiap ring (tahun). Kosong bila belum punya ring. */
    private int[] ringAges = new int[0];

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

    @Override public boolean isGateNotified() { return gateNotified; }
    @Override public void setGateNotified(boolean notified) { this.gateNotified = notified; }

    @Override
    public int[] getRingAges() {
        return ringAges.clone();
    }

    @Override
    public void setRingAges(int[] ages) {
        this.ringAges = ages == null ? new int[0] : ages.clone();
    }

    @Override
    public void addRing(int age) {
        if (ringAges.length >= CultivationRanks.MAX_RINGS) return;
        int[] grown = Arrays.copyOf(ringAges, ringAges.length + 1);
        grown[ringAges.length] = age;
        ringAges = grown;
        setRings(ringAges.length);
    }

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
        CompoundTag cds = new CompoundTag();
        cooldowns.forEach((k, v) -> cds.putLong(k, v));
        tag.put("cooldowns", cds);
        tag.putIntArray("ringAges", ringAges);
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
        cooldowns.clear();
        if (tag.contains("cooldowns")) {
            CompoundTag cds = tag.getCompound("cooldowns");
            for (String key : cds.getAllKeys()) cooldowns.put(key, cds.getLong(key));
        }
        // Save lama belum menyimpan umur ring: daftar dianggap kosong lalu diisi umur default
        // sehingga jumlah ring tetap konsisten dengan data lama.
        int[] saved = tag.contains("ringAges") ? tag.getIntArray("ringAges") : new int[0];
        ringAges = new int[rings];
        for (int i = 0; i < rings; i++) {
            ringAges[i] = i < saved.length ? saved[i] : CultivationRanks.DEFAULT_RING_AGE;
        }
    }
}
