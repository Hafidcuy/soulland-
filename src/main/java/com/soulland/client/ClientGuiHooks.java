package com.soulland.client;

import com.soulland.client.gui.AlchemyScreen;
import com.soulland.client.gui.AwakeningScreen;
import com.soulland.client.gui.LanguageSelectScreen;
import com.soulland.net.AlchemyResultPacket;
import com.soulland.net.ChooseSoulResultPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

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

    /**
     * Membuka layar Pilih Bahasa. mandatory = true dipakai saat login pertama
     * supaya pemain tidak bisa lanjut tanpa memilih bahasa.
     */
    public static void openLanguageScreen(boolean mandatory) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        mc.setScreen(new LanguageSelectScreen(mandatory));
    }

    /** Membuka layar stasiun alchemy. */
    public static void openAlchemy(BlockPos stationPos) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        mc.setScreen(new AlchemyScreen(stationPos));
    }

    /** Menyalakan jawaban racikan dari server ke layar alchemy yang terbuka. */
    public static void onAlchemyResult(AlchemyResultPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof AlchemyScreen screen) {
            screen.onServerResult(packet.crafted(), packet.recipeId());
        }
    }

    /** Menyalakan hasil pilihan soul dari server ke layar yang sedang terbuka. */
    public static void onChooseSoulResult(ChooseSoulResultPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof AwakeningScreen screen) {
            screen.onServerResult(packet.accepted(), packet.messageKey());
        }
    }
}
