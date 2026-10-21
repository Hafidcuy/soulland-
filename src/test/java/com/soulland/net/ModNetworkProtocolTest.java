package com.soulland.net;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Versi protokol jaringan.
 *
 * <p>Setiap kali field paket berubah, versinya harus naik. Kalau tidak, client
 * lama bisa tersambung ke server baru lalu gagal membaca paket baru.
 */
class ModNetworkProtocolTest {

    @Test
    void protokolV15NaikDariVersiV13() {
        assertEquals("5", NetworkProtocol.VERSION);
        assertNotEquals("4", NetworkProtocol.VERSION,
                "protokol harus naik karena v1.5 menambah field dan paket baru");
    }
}
