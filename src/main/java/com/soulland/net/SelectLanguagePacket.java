package com.soulland.net;

import com.soulland.cultivation.CultivationCapability;
import com.soulland.cultivation.CultivationManager;
import com.soulland.lang.SoulLang;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Client -> server: pemain memilih bahasa antarmuka mod.
 *
 * <p>Server adalah sumber kebenaran: kode bahasa divalidasi, disimpan di
 * capability, lalu disinkronkan kembali ke client. Bahasa vanilla Minecraft
 * tidak pernah diubah oleh paket ini.
 */
public record SelectLanguagePacket(String language) {

    public static void encode(SelectLanguagePacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.language(), 8);
    }

    public static SelectLanguagePacket decode(FriendlyByteBuf buf) {
        return new SelectLanguagePacket(buf.readUtf(8));
    }

    public static void handle(SelectLanguagePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            String code = SoulLang.normalize(msg.language());
            if (code.isEmpty()) return;
            CultivationCapability.get(player).ifPresent(cap -> {
                cap.setLanguage(code);
                CultivationManager.sync(player);
                // Baru setelah bahasa dipilih, upacara kebangkitan boleh dibuka.
                if (!cap.isAwakened()) {
                    com.soulland.soul.SoulSelection.openCeremony(player, false);
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
