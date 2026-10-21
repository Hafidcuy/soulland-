package com.soulland.net;

import com.soulland.bone.SpiritBoneManager;
import com.soulland.bone.SpiritBoneSlot;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Client -> server: pemain melepas tulang dari sebuah slot.
 *
 * <p>Server mengembalikan tulang ke tas pemain. Kalau tas penuh, operasi
 * ditolak dan tulang tetap terpasang di slotnya.
 */
public record UnequipBonePacket(int slot) {

    public static void encode(UnequipBonePacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.slot());
    }

    public static UnequipBonePacket decode(FriendlyByteBuf buf) {
        return new UnequipBonePacket(buf.readVarInt());
    }

    public static void handle(UnequipBonePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            SpiritBoneSlot slot = SpiritBoneSlot.byIndex(msg.slot());
            if (slot == null) return;
            SpiritBoneManager.Status status = SpiritBoneManager.unequip(player, slot);
            SpiritBoneManager.sync(player, status);
        });
        ctx.get().setPacketHandled(true);
    }
}
