package com.soulland.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Permintaan membakar darah: tukar HP menjadi spirit power dan bonus damage singkat. */
public record BloodBurnPacket() {

    public static void encode(BloodBurnPacket msg, FriendlyByteBuf buf) {
    }

    public static BloodBurnPacket decode(FriendlyByteBuf buf) {
        return new BloodBurnPacket();
    }

    public static void handle(BloodBurnPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                com.soulland.skill.SkillHandler.activateBloodBurn(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
