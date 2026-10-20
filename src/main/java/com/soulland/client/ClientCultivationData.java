package com.soulland.client;

import com.soulland.net.SyncCultivationPacket;

/** Salinan data kultivasi di sisi client, hanya untuk tampilan (HUD, halo ring, GUI upacara). */
public final class ClientCultivationData {
    private ClientCultivationData() {}

    public static int level = 1;
    public static int xp = 0;
    public static int rings = 0;
    public static float energy = 100f;
    public static String soulId = "";
    /** Umur tiap spirit ring pemain ini (tahun), urut dari ring pertama. */
    public static int[] ringAges = new int[0];
    /** Sisa cooldown (detik) tiap slot skill, 4 slot. */
    public static int[] skillCooldowns = new int[4];
    /** True bila upacara kebangkitan sudah selesai (soul sudah dipilih). */
    public static boolean awakened = false;

    public static void set(SyncCultivationPacket packet) {
        level = packet.level();
        xp = packet.xp();
        rings = packet.rings();
        energy = packet.energy();
        soulId = packet.soulId();
        ringAges = packet.ringAges() == null ? new int[0] : packet.ringAges();
        skillCooldowns = packet.skillCooldowns() == null ? new int[4] : packet.skillCooldowns();
        awakened = packet.awakened();
    }

    /** Sisa cooldown slot skill tertentu dalam detik; 0 bila tidak cooldown atau slot tidak ada. */
    public static int cooldown(int slot) {
        if (slot < 0 || slot >= skillCooldowns.length) return 0;
        return Math.max(0, skillCooldowns[slot]);
    }
}
