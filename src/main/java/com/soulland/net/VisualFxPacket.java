package com.soulland.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Kabar visual dari server ke client: guncangan kamera dan kilat layar.
 *
 * <p>Paket ini murni kosmetik. Isinya cuma angka, tidak pernah mengubah state pemain,
 * jadi tidak ada yang bisa disalahgunakan dari sisi client.
 */
public record VisualFxPacket(float shake, int flashColor, int flashAlpha, int durationTicks) {

    public static void encode(VisualFxPacket msg, FriendlyByteBuf buf) {
        buf.writeFloat(msg.shake());
        buf.writeInt(msg.flashColor());
        buf.writeByte(msg.flashAlpha());
        buf.writeShort(msg.durationTicks());
    }

    public static VisualFxPacket decode(FriendlyByteBuf buf) {
        return new VisualFxPacket(buf.readFloat(), buf.readInt(), buf.readByte(), buf.readShort());
    }

    public static void handle(VisualFxPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> com.soulland.client.ClientFx.receive(msg.shake(), msg.flashColor(),
                        msg.flashAlpha(), msg.durationTicks())));
        ctx.get().setPacketHandled(true);
    }
}
