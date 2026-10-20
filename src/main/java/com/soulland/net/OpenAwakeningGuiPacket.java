package com.soulland.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Server -> client: buka layar Upacara Kebangkitan (modeilkali|REROLL). */
public record OpenAwakeningGuiPacket(boolean reroll) {

    public static void encode(OpenAwakeningGuiPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.reroll);
    }

    public static OpenAwakeningGuiPacket decode(FriendlyByteBuf buf) {
        return new OpenAwakeningGuiPacket(buf.readBoolean());
    }

    public static void handle(OpenAwakeningGuiPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientBridge.openAwakening(msg.reroll()));
        ctx.get().setPacketHandled(true);
    }
}
