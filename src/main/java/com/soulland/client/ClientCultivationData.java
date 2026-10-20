package com.soulland.client;

import com.soulland.net.SyncCultivationPacket;

import java.util.Map;

/** Salinan data kultivasi di sisi client, hanya untuk tampilan (HUD, halo ring, GUI upacara, kartu soul). */
public final class ClientCultivationData {
    private ClientCultivationData() {}

    public static int level = 1;
    public static int xp = 0;
    public static int rings = 0;
    public static float energy = 100f;
    public static String soulId = "";
    public static String sect = "";
    public static int evolutionStage = 0;
    /** Sisa cooldown (detik) per id skill. */
    public static Map<String, Integer> cooldowns = Map.of();
    public static int chainCount = 0;
    public static String[] loadout = new String[]{"", "", "", "", ""};
    public static int ringSkills = 0;
    /** Umur tiap spirit ring pemain ini (tahun), urut dari ring pertama. */
    public static int[] ringAges = new int[0];
    /** Sisa cooldown (detik) tiap slot skill, 4 slot. */
    public static int[] skillCooldowns = new int[4];
    /** True bila upacara kebangkitan sudah selesai (soul sudah dipilih). */
    public static boolean awakened = false;
    /** Bahasa yang dipilih pemain: "id" atau "en". Kosong sebelum memilih. */
    public static String language = "";
    /** Fondasi kultivasi 0-100, kemajuan menuju ambang peringkat berikutnya. */
    public static int foundation = 0;
    /** True kalau pemain sudah mencapai ambang God dan menunggu tribulasi. */
    public static boolean tribulationReady = false;
    /** Berapa spirit ring yang pernah bermutasi. */
    public static int mutationCount = 0;
    /** Berapa kali sinkronisasi dari server diterima; dipakai layar untuk tahu jawaban server. */
    public static int syncCount = 0;

    /**
     * Bahasa yang dipakai tampilan: pilihan pemain kalau ada, kalau belum
     * memakai bahasa client Minecraft sebagai cadangan.
     */
    public static String effectiveLanguage() {
        if (!language.isEmpty()) return language;
        return com.soulland.lang.SoulLang.fromLocale(
                net.minecraft.client.Minecraft.getInstance().options.languageCode);
    }

    public static void set(SyncCultivationPacket packet) {
        level = packet.level();
        xp = packet.xp();
        rings = packet.rings();
        energy = packet.energy();
        soulId = packet.soulId();
        sect = packet.sect();
        evolutionStage = packet.evolutionStage();
        cooldowns = packet.cooldowns() == null ? Map.of() : packet.cooldowns();
        chainCount = packet.chainCount();
        loadout = packet.loadout() == null ? new String[]{"", "", "", "", ""} : packet.loadout();
        ringSkills = packet.ringSkills();
        ringAges = packet.ringAges() == null ? new int[0] : packet.ringAges();
        skillCooldowns = packet.skillCooldowns() == null ? new int[4] : packet.skillCooldowns();
        awakened = packet.awakened();
        language = packet.language() == null ? "" : packet.language();
        foundation = packet.foundation();
        tribulationReady = packet.tribulationReady();
        mutationCount = packet.mutationCount();
        syncCount++;
    }

    /** Sisa cooldown slot skill tertentu dalam detik; 0 bila tidak cooldown atau slot tidak ada. */
    public static int cooldown(int slot) {
        if (slot < 0 || slot >= skillCooldowns.length) return 0;
        return Math.max(0, skillCooldowns[slot]);
    }
}
