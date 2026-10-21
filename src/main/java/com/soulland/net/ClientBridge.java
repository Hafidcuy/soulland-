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

        void openLanguageScreen(boolean mandatory);

        void openAlchemy(net.minecraft.core.BlockPos stationPos);

        void onAlchemyResult(AlchemyResultPacket packet);

        void onChooseSoulResult(ChooseSoulResultPacket packet);

        void onCultivationSync(SyncCultivationPacket packet);

        void onRingAgesSync(SyncRingAgesPacket packet);

        void onManifestation(ManifestationPacket packet);

        void onBonesSync(SyncBonesPacket packet);

        void onCombatSync(SyncCombatPacket packet);
    }

    private static volatile Handler handler;

    private static volatile Supplier<String> soulIdSupplier = () -> "";

    private static volatile Supplier<String> languageSupplier = () -> "";

    private static volatile Supplier<Integer> levelSupplier = () -> 0;

    private static volatile Supplier<Integer> ringsSupplier = () -> 0;

    /**
     * Id martial soul pemain ini menurut salinan sisi client. Kosong di server
     * karena server membaca soul langsung dari capability.
     */
    public static String soulId() {
        Supplier<String> supplier = soulIdSupplier;
        return supplier == null ? "" : supplier.get();
    }

    /**
     * Jumlah spirit ring pemain ini menurut salinan sisi client. 0 di server
     * khusus karena server membaca jumlah ring langsung dari capability.
     */
    public static int rings() {
        Supplier<Integer> supplier = ringsSupplier;
        if (supplier == null) return 0;
        Integer value = supplier.get();
        return value == null ? 0 : value;
    }

    /** Dipanggil sekali dari sisi client untuk menyambungkan jumlah ring. */
    public static void setRingsSupplier(Supplier<Integer> supplier) {
        ringsSupplier = supplier == null ? () -> 0 : supplier;
    }

    /**
     * Bahasa yang dipilih pemain ini menurut salinan sisi client. Kosong di
     * server khusus karena server membaca bahasa langsung dari capability.
     */
    public static String language() {
        Supplier<String> supplier = languageSupplier;
        return supplier == null ? "" : supplier.get();
    }

    /** Dipanggil sekali dari sisi client untuk menyambungkan salinan soul. */
    public static void setSoulIdSupplier(Supplier<String> supplier) {
        soulIdSupplier = supplier == null ? () -> "" : supplier;
    }

    /** Dipanggil sekali dari sisi client untuk menyambungkan bahasa pemain. */
    public static void setLanguageSupplier(Supplier<String> supplier) {
        languageSupplier = supplier == null ? () -> "" : supplier;
    }

    /**
     * Level kultivasi pemain ini menurut salinan sisi client. 0 di server
     * khusus karena server membaca level langsung dari capability.
     */
    public static int level() {
        Supplier<Integer> supplier = levelSupplier;
        if (supplier == null) return 0;
        Integer value = supplier.get();
        return value == null ? 0 : value;
    }

    /** Dipanggil sekali dari sisi client untuk menyambungkan level pemain. */
    public static void setLevelSupplier(Supplier<Integer> supplier) {
        levelSupplier = supplier == null ? () -> 0 : supplier;
    }

    /** Dipanggil sekali dari sisi client (client setup). */
    public static void set(Handler newHandler) {
        handler = newHandler;
    }

    public static void openAwakening(boolean reroll) {
        Handler current = handler;
        if (current != null) current.openAwakening(reroll);
    }

    public static void openLanguageScreen(boolean mandatory) {
        Handler current = handler;
        if (current != null) current.openLanguageScreen(mandatory);
    }

    public static void openAlchemy(net.minecraft.core.BlockPos stationPos) {
        Handler current = handler;
        if (current != null) current.openAlchemy(stationPos);
    }

    public static void onAlchemyResult(AlchemyResultPacket packet) {
        Handler current = handler;
        if (current != null) current.onAlchemyResult(packet);
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

    public static void onBonesSync(SyncBonesPacket packet) {
        Handler current = handler;
        if (current != null) current.onBonesSync(packet);
    }

    public static void onCombatSync(SyncCombatPacket packet) {
        Handler current = handler;
        if (current != null) current.onCombatSync(packet);
    }
}
