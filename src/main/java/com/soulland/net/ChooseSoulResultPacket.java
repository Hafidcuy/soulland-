package com.soulland.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Server -> client: hasil pilihan martial soul.
 * {@code accepted} true berarti upacara selesai; false berarti pilihan ditolak dan
 * {@code messageKey} berisi kunci translatable alasannya.
 */
public record ChooseSoulResultPacket(boolean accepted, String messageKey) {

    public static void encode(ChooseSoulResultPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.accepted);
        buf.writeUtf(msg.messageKey() == null ? "" : msg.messageKey(), 64);
    }

    public static ChooseSoulResultPacket decode(FriendlyByteBuf buf) {
        return new ChooseSoulResultPacket(buf.readBoolean(), buf.readUtf(64));
    }

    public static void handle(ChooseSoulResultPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientBridge.onChooseSoulResult(msg));
        ctx.get().setPacketHandled(true);
    }
}
