package com.soulland.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Permintaan ledakan spirit ring: energi seluruh spirit ring dilepaskan sekaligus. */
public record RingBurstPacket() {

    public static void encode(RingBurstPacket msg, FriendlyByteBuf buf) {
    }

    public static RingBurstPacket decode(FriendlyByteBuf buf) {
        return new RingBurstPacket();
    }

    public static void handle(RingBurstPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                com.soulland.skill.SkillHandler.activateRingBurst(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
