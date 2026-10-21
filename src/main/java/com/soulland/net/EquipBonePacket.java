package com.soulland.net;

import com.soulland.bone.SpiritBoneManager;
import com.soulland.bone.SpiritBoneSlot;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Client -> server: pemain memasang tulang ke sebuah slot.
 *
 * <p>Server memeriksa semua aturan: item harus tulang, slot harus cocok,
 * syarat spirit ring harus terpenuhi, dan tulangnya harus benar-benar ada
 * di tas. Client hanya boleh meminta.
 */
public record EquipBonePacket(int slot, String itemId) {

    public static void encode(EquipBonePacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.slot());
        buf.writeUtf(msg.itemId() == null ? "" : msg.itemId(), 96);
    }

    public static EquipBonePacket decode(FriendlyByteBuf buf) {
        return new EquipBonePacket(buf.readVarInt(), buf.readUtf(96));
    }

    public static void handle(EquipBonePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            SpiritBoneSlot slot = SpiritBoneSlot.byIndex(msg.slot());
            if (slot == null) return;
            SpiritBoneManager.Status status = SpiritBoneManager.equip(player, slot, msg.itemId());
            SpiritBoneManager.sync(player, status);
        });
        ctx.get().setPacketHandled(true);
    }
}
