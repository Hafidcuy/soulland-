package com.soulland.client;

import com.soulland.screen.SoulCardScreen;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Jembatan ke kode client.
 *
 * <p>Kelas ini hanya boleh dipanggil dari {@code DistExecutor.unsafeRunWhenOn(Dist.CLIENT, ...)}
 * memakai <i>method reference</i>. Kalau kelas client dipanggil langsung di dalam lambda kelas
 * comum, verifier JVM akan memuat kelas itu saat kelas comum dimuat, dan dedicated server
 * akan menolak karena {@code Screen} hanya ada di sisi client.
 */
@OnlyIn(Dist.CLIENT)
public final class ClientScreenOpener {
    private ClientScreenOpener() {}

    public static void openSoulCard() {
        Minecraft.getInstance().setScreen(new SoulCardScreen());
    }

    public static void openGuide() {
        Minecraft.getInstance().setScreen(new com.soulland.screen.GuideScreen());
    }

    public static void openSectTab() {
        Minecraft.getInstance().setScreen(new SoulCardScreen(3));
    }
}
