package com.soulland.net;

import com.soulland.cultivation.CultivationCapability;
import com.soulland.cultivation.CultivationManager;
import com.soulland.sect.Sect;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Permintaan memilih sekte. Server memvalidasi id sekte sebelum menyimpan. */
public record ChooseSectPacket(String sectId) {

    public static void encode(ChooseSectPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.sectId(), 32);
    }

    public static ChooseSectPacket decode(FriendlyByteBuf buf) {
        return new ChooseSectPacket(buf.readUtf(32));
    }

    public static void handle(ChooseSectPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            Sect sect = Sect.get(msg.sectId());
            if (sect == null) return;
            CultivationCapability.get(player).ifPresent(cap -> {
                cap.setSect(sect.id());
                CultivationManager.applyStats(player);
                CultivationManager.sync(player);
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "Kamu resmi bergabung dengan " + sect.name() + "."));
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
