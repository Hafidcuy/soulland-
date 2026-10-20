package com.soulland.net;

import com.soulland.skill.SkillHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Permintaan aktivasi skill dari client -> server. Semua validasi & efek dijalankan di server. */
public record ActivateSkillPacket(int slot) {

    public static void encode(ActivateSkillPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.slot);
    }

    public static ActivateSkillPacket decode(FriendlyByteBuf buf) {
        return new ActivateSkillPacket(buf.readInt());
    }

    public static void handle(ActivateSkillPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                SkillHandler.activate(player, msg.slot);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
