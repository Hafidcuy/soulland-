package com.soulland.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Client -> server: permintaan menyalakan atau mematikan Domain.
 *
 * <p>Domain adalah sakelar, jadi satu paket yang sama berarti pindah status:
 * hidup menjadi mati, mati menjadi hidup.
 */
public record ToggleDomainPacket() {

    public static void encode(ToggleDomainPacket msg, FriendlyByteBuf buf) {
        // Tidak ada field: permintaannya kosong.
    }

    public static ToggleDomainPacket decode(FriendlyByteBuf buf) {
        return new ToggleDomainPacket();
    }

    public static void handle(ToggleDomainPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> com.soulland.combat.DomainHandler.toggle(ctx.get().getSender()));
        ctx.get().setPacketHandled(true);
    }
}
