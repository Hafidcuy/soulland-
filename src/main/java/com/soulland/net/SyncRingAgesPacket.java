package com.soulland.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Server -> client: umur spirit ring milik sebuah entity.
 * Dikirim ke pemain di sekitar supaya halo ring pemain lain juga terlihat, bukan hanya milik sendiri.
 */
public record SyncRingAgesPacket(int entityId, int[] ringAges) {

    public static void encode(SyncRingAgesPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.entityId());
        buf.writeVarIntArray(msg.ringAges());
    }

    public static SyncRingAgesPacket decode(FriendlyByteBuf buf) {
        return new SyncRingAgesPacket(buf.readInt(), buf.readVarIntArray());
    }

    public static void handle(SyncRingAgesPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientBridge.onRingAgesSync(msg));
        ctx.get().setPacketHandled(true);
    }
}
