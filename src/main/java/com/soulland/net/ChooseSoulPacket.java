package com.soulland.net;

import com.soulland.soul.SoulSelection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Client -> server: permintaan memilih martial soul pada upacara kebangkitan. */
public record ChooseSoulPacket(String soulId) {

    public static void encode(ChooseSoulPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.soulId(), 64);
    }

    public static ChooseSoulPacket decode(FriendlyByteBuf buf) {
        return new ChooseSoulPacket(buf.readUtf(64));
    }

    public static void handle(ChooseSoulPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                SoulSelection.handleChoice(player, msg.soulId());
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
