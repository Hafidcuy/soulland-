package com.soulland.net;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Server -> client: buka layar stasiun alchemy.
 *
 * <p>Dikirim saat pemain klik kanan blok stasiun. Layar hanya menampilkan
 * daftar resep; racikan tetap divalidasi server. Posisi stasiun ikut dikirim
 * supaya server bisa memastikan pemain masih berdiri di dekatnya saat meracik.
 */
public record OpenAlchemyPacket(BlockPos stationPos) {

    public static void encode(OpenAlchemyPacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.stationPos());
    }

    public static OpenAlchemyPacket decode(FriendlyByteBuf buf) {
        return new OpenAlchemyPacket(buf.readBlockPos());
    }

    public static void handle(OpenAlchemyPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientBridge.openAlchemy(msg.stationPos()));
        ctx.get().setPacketHandled(true);
    }
}
