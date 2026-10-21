package com.soulland.net;

/**
 * Versi protokol jaringan mod Soul Land.
 *
 * <p>Angka ini naik setiap kali field paket berubah. Klien dan server dengan
 * versi berbeda tidak akan saling terhubung, dan itu memang yang
 * diinginkan: paket baru tidak boleh dibaca oleh parser lama.
 *
 * <p>Riwayat: "1" awal, "2" v1.1, "3" v1.2B, "4" v1.3 (tulang spirit),
 * "5" v1.5 (True Body dan Domain).
 */
public final class NetworkProtocol {
    private NetworkProtocol() {}

    /** Versi protokol yang dipakai mod ini. */
    public static final String VERSION = "5";
}
