package com.soulland.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Permintaan memunculkan atau menyembunyikan companion martial soul. */
public record ToggleSoulPacket() {

    public static void encode(ToggleSoulPacket msg, FriendlyByteBuf buf) {
    }

    public static ToggleSoulPacket decode(FriendlyByteBuf buf) {
        return new ToggleSoulPacket();
    }

    public static void handle(ToggleSoulPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                com.soulland.entity.SoulCompanion.toggle(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
