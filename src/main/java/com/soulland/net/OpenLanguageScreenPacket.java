package com.soulland.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Server -> client: buka layar pilih bahasa.
 *
 * <p>Kirim server saat pemain login pertama kali (bahasa belum dipilih, layar
 * wajib ditutup dengan memilih bahasa) atau saat pemain menjalankan
 * {@code /soul language} (layar boleh ditutup tanpa memilih).
 */
public record OpenLanguageScreenPacket(boolean mandatory) {

    public static void encode(OpenLanguageScreenPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.mandatory());
    }

    public static OpenLanguageScreenPacket decode(FriendlyByteBuf buf) {
        return new OpenLanguageScreenPacket(buf.readBoolean());
    }

    public static void handle(OpenLanguageScreenPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientBridge.openLanguageScreen(msg.mandatory()));
        ctx.get().setPacketHandled(true);
    }
}
