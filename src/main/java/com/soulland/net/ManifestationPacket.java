package com.soulland.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Server -> client: mulai animasi manifestasi martial soul pada sebuah entity. */
public record ManifestationPacket(int entityId, String soulId) {

    public static void encode(ManifestationPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.entityId());
        buf.writeUtf(msg.soulId(), 64);
    }

    public static ManifestationPacket decode(FriendlyByteBuf buf) {
        return new ManifestationPacket(buf.readInt(), buf.readUtf(64));
    }

    public static void handle(ManifestationPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientBridge.onManifestation(msg));
        ctx.get().setPacketHandled(true);
    }
}
