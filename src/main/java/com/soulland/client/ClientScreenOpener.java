package com.soulland.client;

import com.soulland.screen.GuideScreen;


import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Satu-satunya tempat yang boleh menyentuh kelas tampilan.
 *
 * <p>Kelas packet di sisi umum memanggilnya lewat method reference, bukan {@code new} langsung,
 * supaya verifier JVM tidak pernah memuat kelas client di dedicated server.
 */
@OnlyIn(Dist.CLIENT)
public final class ClientScreenOpener {
    private ClientScreenOpener() {}

    public static void openWelcome() {
        Minecraft.getInstance().setScreen(new WelcomeScreen());
    }

    public static void openGuide() {
        Minecraft.getInstance().setScreen(new GuideScreen());
    }

    public static void openSoulCard() {
        Minecraft.getInstance().setScreen(new SoulCardScreen());
    }

    public static void openSectTab() {
        Minecraft.getInstance().setScreen(new SoulCardScreen(3));
    }
}
