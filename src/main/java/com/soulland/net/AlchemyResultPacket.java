package com.soulland.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Server -> client: jawaban atas permintaan meracik.
 *
 * <p>Layar memakai jawaban ini untuk berpindah ke status berhasil atau gagal,
 * jadi pemain selalu tahu apa yang terjadi pada racikannya.
 */
public record AlchemyResultPacket(boolean crafted, String recipeId) {

    public static void encode(AlchemyResultPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.crafted());
        buf.writeUtf(msg.recipeId() == null ? "" : msg.recipeId(), 32);
    }

    public static AlchemyResultPacket decode(FriendlyByteBuf buf) {
        return new AlchemyResultPacket(buf.readBoolean(), buf.readUtf(32));
    }

    public static void handle(AlchemyResultPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientBridge.onAlchemyResult(msg));
        ctx.get().setPacketHandled(true);
    }
}
