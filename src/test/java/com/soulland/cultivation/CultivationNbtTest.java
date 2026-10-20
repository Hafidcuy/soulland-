package com.soulland.cultivation;

import com.soulland.CultivationRanks;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Memastikan state pemain tidak hilang saat dunia disimpan dan dimuat lagi. */
class CultivationNbtTest {

    @Test
    void stateRoundTripMenyimpanSemuaNilaiPenting() {
        Cultivation before = new Cultivation();
        before.setAwakened(true);
        before.setMartialSoulId("clear_sky_hammer");
        before.setLevel(42);
        before.setXp(1234);
        before.setRings(6);
        before.setSect("shrek");
        before.setEvolutionStage(1);
        before.setChainCount(3);
        before.setChainUntil(12345L);
        before.setBloodBurnTicks(90);
        before.setRingSkills(7);
        before.setContractKills(4);
        before.setContractUntil(999L);
        before.setLoadoutSkill(0, "hammer_slam");
        before.setLoadoutSkill(2, "ring_guard");

        CompoundTag tag = before.serializeNBT();
        Cultivation after = new Cultivation();
        after.deserializeNBT(tag);

        assertTrue(after.isAwakened());
        assertEquals("clear_sky_hammer", after.getMartialSoulId());
        assertEquals(42, after.getLevel());
        assertEquals(1234, after.getXp());
        assertEquals(6, after.getRings());
        assertEquals("shrek", after.getSect());
        assertEquals(1, after.getEvolutionStage());
        assertEquals(3, after.getChainCount());
        assertEquals(12345L, after.getChainUntil());
        assertEquals(90, after.getBloodBurnTicks());
        assertEquals(7, after.getRingSkills());
        assertEquals(4, after.getContractKills());
        assertEquals(999L, after.getContractUntil());
        assertEquals("hammer_slam", after.getLoadoutSkill(0));
        assertEquals("ring_guard", after.getLoadoutSkill(2));
    }

    @Test
    void loadoutKosongTidakJadiNull() {
        Cultivation cultivation = new Cultivation();
        CompoundTag tag = cultivation.serializeNBT();
        Cultivation after = new Cultivation();
        after.deserializeNBT(tag);
        for (int slot = 0; slot < 5; slot++) {
            assertEquals("", after.getLoadoutSkill(slot));
        }
    }

    @Test
    void nilaiDibatasiAmankanDariDataRusak() {
        Cultivation cultivation = new Cultivation();
        cultivation.setLevel(-5);
        cultivation.setRings(99);
        cultivation.setEnergy(999f);
        assertEquals(CultivationRanks.MAX_RINGS, cultivation.getRings());
        assertEquals(CultivationRanks.MAX_ENERGY, cultivation.getEnergy(), 0.001);
        assertEquals(1, cultivation.getLevel());
    }
}
