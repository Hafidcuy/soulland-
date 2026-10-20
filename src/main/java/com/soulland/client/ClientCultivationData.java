package com.soulland.client;

import com.soulland.net.SyncCultivationPacket;

import java.util.Map;

/** Salinan data kultivasi di sisi client, hanya untuk tampilan (HUD dan kartu soul). */
public final class ClientCultivationData {
    private ClientCultivationData() {}

    public static int level = 1;
    public static int xp = 0;
    public static int rings = 0;
    public static float energy = 100f;
    public static String soulId = "";
    public static String sect = "";
    public static int evolutionStage = 0;
    public static Map<String, Integer> cooldowns = Map.of();
    public static int chainCount = 0;
    public static String[] loadout = new String[5];
    public static int ringSkills = 0;

    public static void set(SyncCultivationPacket packet) {
        level = packet.level();
        xp = packet.xp();
        rings = packet.rings();
        energy = packet.energy();
        soulId = packet.soulId();
        sect = packet.sect();
        evolutionStage = packet.evolutionStage();
        cooldowns = packet.cooldowns();
        chainCount = packet.chainCount();
        loadout = packet.loadout();
        ringSkills = packet.ringSkills();
    }
}
