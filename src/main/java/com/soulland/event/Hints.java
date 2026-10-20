package com.soulland.event;

import com.soulland.cultivation.CultivationCapability;
import com.soulland.cultivation.ICultivation;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Arahan yang muncul satu kali per pemain, pemicunya aksi nyata.
 *
 * <p>Setiap pesan menyebut angka atau tombol yang benar-benar dipakai, jadi tidak ada
 * nasihat kosong yang cuma mengisi layar.
 */
public final class Hints {
    private Hints() {}

    public static final int RING = 1;
    public static final int SKILL = 2;
    public static final int ELEMENT = 4;
    public static final int GATE = 8;
    public static final int LOADOUT = 16;

    public static void show(ServerPlayer player, int flag, String message) {
        CultivationCapability.get(player).ifPresent(cap -> {
            if ((cap.getHintFlags() & flag) != 0) return;
            cap.setHintFlags(cap.getHintFlags() | flag);
            player.sendSystemMessage(Component.literal(message));
        });
    }
}
