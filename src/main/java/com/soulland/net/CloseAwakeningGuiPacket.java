package com.soulland.net;

import com.soulland.soul.SoulSelection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Client -> server: pemain menutup layar Upacara Kebangkitan.
 * Server memakai sinyal ini untuk mencabut sesi pilih-ulang yang masih terbuka.
 */
public record CloseAwakeningGuiPacket() {

    public static void encode(CloseAwakeningGuiPacket msg, FriendlyByteBuf buf) {
        // Tanpa muatan: server hanya perlu tahu pemain menutup layar.
    }

    public static CloseAwakeningGuiPacket decode(FriendlyByteBuf buf) {
        return new CloseAwakeningGuiPacket();
    }

    public static void handle(CloseAwakeningGuiPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                SoulSelection.closeSession(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
