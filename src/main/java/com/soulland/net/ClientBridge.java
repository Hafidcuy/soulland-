package com.soulland.net;

import java.util.function.Supplier;

/**
 * Jembatan antara paket server dan sisi client.
 *
 * <p>Kelas ini sengaja tidak mengimpor apa pun dari paket {@code client},
 * sehingga kode common tidak pernah memuat kelas yang butuh Minecraft client
 * dan server khusus tetap aman. Sisi client mendaftarkan implementasinya satu
 * kali saat client setup; bila tidak terdaftar, semua panggilan di sini
 * diabaikan tanpa error.
 */
public final class ClientBridge {
    private ClientBridge() {}

    /** Penerima payload yang hanya bermakna di sisi client. */
    public interface Handler {
        void openAwakening(boolean reroll);

        void onChooseSoulResult(ChooseSoulResultPacket packet);

        void onCultivationSync(SyncCultivationPacket packet);

        void onRingAgesSync(SyncRingAgesPacket packet);

        void onManifestation(ManifestationPacket packet);
    }

    private static volatile Handler handler;

    private static volatile Supplier<String> soulIdSupplier = () -> "";

    /**
     * Id martial soul pemain ini menurut salinan sisi client. Kosong di server
     * karena server membaca soul langsung dari capability.
     */
    public static String soulId() {
        Supplier<String> supplier = soulIdSupplier;
        return supplier == null ? "" : supplier.get();
    }

    /** Dipanggil sekali dari sisi client untuk menyambungkan salinan soul. */
    public static void setSoulIdSupplier(Supplier<String> supplier) {
        soulIdSupplier = supplier == null ? () -> "" : supplier;
    }

    /** Dipanggil sekali dari sisi client (client setup). */
    public static void set(Handler newHandler) {
        handler = newHandler;
    }

    public static void openAwakening(boolean reroll) {
        Handler current = handler;
        if (current != null) current.openAwakening(reroll);
    }

    public static void onChooseSoulResult(ChooseSoulResultPacket packet) {
        Handler current = handler;
        if (current != null) current.onChooseSoulResult(packet);
    }

    public static void onCultivationSync(SyncCultivationPacket packet) {
        Handler current = handler;
        if (current != null) current.onCultivationSync(packet);
    }

    public static void onRingAgesSync(SyncRingAgesPacket packet) {
        Handler current = handler;
        if (current != null) current.onRingAgesSync(packet);
    }

    public static void onManifestation(ManifestationPacket packet) {
        Handler current = handler;
        if (current != null) current.onManifestation(packet);
    }
}
