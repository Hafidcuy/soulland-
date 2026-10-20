package com.soulland.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Permintaan mulai atau hentikan meditasi. */
public record MeditatePacket(boolean active) {

    public static void encode(MeditatePacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.active());
    }

    public static MeditatePacket decode(FriendlyByteBuf buf) {
        return new MeditatePacket(buf.readBoolean());
    }

    public static void handle(MeditatePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            com.soulland.cultivation.CultivationCapability.get(player).ifPresent(cap -> {
                if (msg.active()) {
                    if (cap.isMeditating()) return;
                    cap.setMeditating(true);
                    cap.setMeditationTicks(0);
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                            "Meditasi dimulai. Diam di tempat sampai kanal spiritual terisi."));
                } else {
                    cap.setMeditating(false);
                    cap.setMeditationTicks(0);
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
