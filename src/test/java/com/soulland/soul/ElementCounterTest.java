package com.soulland.soul;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Memastikan aturan lawan elemen tetap konsisten dan tidak saling bertentangan. */
class ElementCounterTest {

    @Test
    void apiMengalahiTanahDanTumbuhan() {
        assertTrue(Element.FIRE.beats(Element.EARTH));
        assertTrue(Element.FIRE.beats(Element.NATURE));
        assertEquals(1.25, Element.FIRE.multiplierAgainst(Element.NATURE), 0.001);
    }

    @Test
    void kalahMemberiPengaliLebihKecilDariSatu() {
        assertTrue(Element.WATER.beats(Element.FIRE));
        assertEquals(0.85, Element.FIRE.multiplierAgainst(Element.WATER), 0.001);
    }

    @Test
    void elemenTanpaLawanMemberiPengaliSatu() {
        assertEquals(1.0, Element.WATER.multiplierAgainst(Element.WATER), 0.001);
        assertEquals(1.0, Element.FIRE.multiplierAgainst(Element.NONE), 0.001);
    }

    @Test
    void tidakAdaSiklusYangSalingMembatalkan() {
        for (Element element : Element.values()) {
            for (Element other : Element.values()) {
                if (element == other) continue;
                boolean forward = element.beats(other);
                boolean backward = other.beats(element);
                assertFalse(forward && backward, element + " dan " + other + " saling mengalahkan");
            }
        }
    }

    @Test
    void elemenMartialSoulTerpetakanKeElemenTempur() {
        assertEquals(Element.FIRE, Element.fromSoulElement("Fire"));
        assertEquals(Element.ICE, Element.fromSoulElement("Ice"));
        assertEquals(Element.LIGHTNING, Element.fromSoulElement("Lightning"));
        assertEquals(Element.DARK, Element.fromSoulElement("Dark"));
        assertEquals(Element.NONE, Element.fromSoulElement("Tidak Diketahui"));
    }
}
