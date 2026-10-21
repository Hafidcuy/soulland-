package com.soulland.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Client -> server: permintaan membuka True Body.
 *
 * <p>Paket ini hanya permintaan. Semua syarat unlock ring, jeda cooldown, dan
 * spirit power diperiksa ulang di server.
 */
public record ActivateTrueBodyPacket() {

    public static void encode(ActivateTrueBodyPacket msg, FriendlyByteBuf buf) {
        // Tidak ada field: permintaannya kosong.
    }

    public static ActivateTrueBodyPacket decode(FriendlyByteBuf buf) {
        return new ActivateTrueBodyPacket();
    }

    public static void handle(ActivateTrueBodyPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> com.soulland.combat.TrueBodyHandler.activate(ctx.get().getSender()));
        ctx.get().setPacketHandled(true);
    }
}
