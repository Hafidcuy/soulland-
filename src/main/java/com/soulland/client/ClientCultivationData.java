package com.soulland.client;

import com.soulland.net.SyncCultivationPacket;

/** Salinan data kultivasi di sisi client, hanya untuk tampilan (HUD). */
public final class ClientCultivationData {
    private ClientCultivationData() {}

    public static int level = 1;
    public static int xp = 0;
    public static int rings = 0;
    public static float energy = 100f;
    public static String soulId = "";

    public static void set(SyncCultivationPacket packet) {
        level = packet.level();
        xp = packet.xp();
        rings = packet.rings();
        energy = packet.energy();
        soulId = packet.soulId();
    }
}
