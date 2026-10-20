package com.soulland.net;

import com.soulland.cultivation.CultivationCapability;
import com.soulland.cultivation.CultivationManager;
import com.soulland.soul.MartialSouls;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Permintaan memasang skill ke slot tombol, atau mengosongkan slot. */
public record AssignSkillPacket(int slot, String skillId) {

    public static void encode(AssignSkillPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.slot());
        buf.writeUtf(msg.skillId(), 64);
    }

    public static AssignSkillPacket decode(FriendlyByteBuf buf) {
        return new AssignSkillPacket(buf.readInt(), buf.readUtf(64));
    }

    public static void handle(AssignSkillPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            CultivationCapability.get(player).ifPresent(cap -> {
                if (msg.slot() < 0 || msg.slot() > 4) return;
                if (!msg.skillId().isEmpty() && MartialSouls.skillById(msg.skillId()) == null) return;
                cap.setLoadoutSkill(msg.slot(), msg.skillId());
                CultivationManager.sync(player);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
