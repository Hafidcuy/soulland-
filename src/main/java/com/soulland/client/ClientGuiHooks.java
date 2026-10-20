package com.soulland.client;

import com.soulland.client.gui.AwakeningScreen;
import com.soulland.net.ChooseSoulResultPacket;
import net.minecraft.client.Minecraft;

/**
 * Jembatan antara packet server dan GUI client. Dipanggil hanya dari sisi client
 * (lewat {@code DistExecutor}), jadi kelas ini tidak pernah dibebani di server khusus.
 */
public final class ClientGuiHooks {
    private ClientGuiHooks() {}

    /** Membuka layar Upacara Kebangkitan. reroll = true berarti memilih soul ulang. */
    public static void openAwakening(boolean reroll) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        mc.setScreen(new AwakeningScreen(reroll));
    }

    /** Menyalakan hasil pilihan soul dari server ke layar yang sedang terbuka. */
    public static void onChooseSoulResult(ChooseSoulResultPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof AwakeningScreen screen) {
            screen.onServerResult(packet.accepted(), packet.messageKey());
        }
    }
}
