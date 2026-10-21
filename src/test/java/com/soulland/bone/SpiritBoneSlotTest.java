package com.soulland.bone;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Memastikan tujuh slot tubuh punya pengali yang masuk akal dan tidak kembar. */
class SpiritBoneSlotTest {

    @Test
    void adaTujuhSlotDenganIdUnik() {
        SpiritBoneSlot[] slots = SpiritBoneSlot.values();
        assertEquals(7, slots.length);
        Set<String> ids = new HashSet<>();
        for (SpiritBoneSlot slot : slots) {
            assertTrue(ids.add(slot.id()), "id slot kembar: " + slot.id());
        }
    }

    @Test
    void jumlahSlotConstanSesuaiEnumDanTidakLindungDariPerubahan() {
        // Packet dan capability memakai konstanta ini, jadi harus selalu sama
        // dengan isi enum.
        assertEquals(SpiritBoneSlot.values().length, SpiritBoneSlot.COUNT);
        assertEquals(7, SpiritBoneSlot.COUNT);
    }

    @Test
    void slotDicariLewatIdDanIndeks() {
        assertEquals(SpiritBoneSlot.TORSO, SpiritBoneSlot.byId("torso"));
        assertEquals(SpiritBoneSlot.EXTERNAL, SpiritBoneSlot.byId("external"));
        assertNull(SpiritBoneSlot.byId("tail"));
        assertNull(SpiritBoneSlot.byId(null));
        assertEquals(SpiritBoneSlot.SKULL, SpiritBoneSlot.byIndex(0));
        assertEquals(SpiritBoneSlot.EXTERNAL, SpiritBoneSlot.byIndex(6));
        assertNull(SpiritBoneSlot.byIndex(7));
        assertNull(SpiritBoneSlot.byIndex(-1));
    }

    @Test
    void indeksSlotSesuaiUrutanEnum() {
        SpiritBoneSlot[] slots = SpiritBoneSlot.values();
        for (int index = 0; index < slots.length; index++) {
            assertEquals(index, slots[index].index());
            assertEquals(slots[index], SpiritBoneSlot.byIndex(index));
        }
    }

    @Test
    void pengaliSlotDalamRentangWajar() {
        for (SpiritBoneSlot slot : SpiritBoneSlot.values()) {
            assertTrue(slot.attackMultiplier() > 0f, slot.id());
            assertTrue(slot.defenseMultiplier() > 0f, slot.id());
            assertTrue(slot.energyMultiplier() > 0f, slot.id());
            assertTrue(slot.regenMultiplier() > 0f, slot.id());
            assertTrue(slot.attackMultiplier() <= 2f, slot.id());
            assertTrue(slot.defenseMultiplier() <= 2f, slot.id());
        }
    }

    @Test
    void tengkorakKuatSeranganRonggaDadaKuatDefense() {
        assertTrue(SpiritBoneSlot.SKULL.attackMultiplier() > SpiritBoneSlot.SKULL.defenseMultiplier());
        assertTrue(SpiritBoneSlot.TORSO.defenseMultiplier() > SpiritBoneSlot.TORSO.attackMultiplier());
    }

    @Test
    void lenganKiriKananPunyaPengaliSamaTapiSlotBerbeda() {
        assertEquals(SpiritBoneSlot.LEFT_ARM.attackMultiplier(), SpiritBoneSlot.RIGHT_ARM.attackMultiplier());
        assertEquals(SpiritBoneSlot.LEFT_LEG.defenseMultiplier(), SpiritBoneSlot.RIGHT_LEG.defenseMultiplier());
        assertTrue(SpiritBoneSlot.LEFT_ARM != SpiritBoneSlot.RIGHT_ARM);
    }

    @Test
    void tulangEksternalPalingKuatDiSeranganEnergiDanRegen() {
        for (SpiritBoneSlot slot : SpiritBoneSlot.values()) {
            if (slot == SpiritBoneSlot.EXTERNAL) continue;
            assertTrue(SpiritBoneSlot.EXTERNAL.attackMultiplier() > slot.attackMultiplier(), slot.id());
            assertTrue(SpiritBoneSlot.EXTERNAL.energyMultiplier() > slot.energyMultiplier(), slot.id());
            assertTrue(SpiritBoneSlot.EXTERNAL.regenMultiplier() > slot.regenMultiplier(), slot.id());
        }
        // Defense: rongga dada tetap yang terkuat, Eksternal hanya di atas slot lain.
        assertTrue(SpiritBoneSlot.EXTERNAL.defenseMultiplier() > SpiritBoneSlot.SKULL.defenseMultiplier());
        assertTrue(SpiritBoneSlot.EXTERNAL.defenseMultiplier() < SpiritBoneSlot.TORSO.defenseMultiplier());
    }
}
