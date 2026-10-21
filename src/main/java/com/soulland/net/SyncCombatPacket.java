package com.soulland.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Server -> client: status True Body dan Domain milik satu pemain.
 *
 * <p>Paket ini dikirim ke pemilik state dan ke pemain di sekitar, karena
 * manifestasi yang membesar dan halo yang melebar harus terlihat oleh orang
 * lain, bukan cuma oleh pemiliknya.
 */
public record SyncCombatPacket(
        int entityId,
        String soulId,
        long trueBodyUntil,
        long trueBodyCooldownUntil,
        boolean domainActive,
        int domainDrainPerSecond
) {

    public static void encode(SyncCombatPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.entityId());
        buf.writeUtf(msg.soulId(), 64);
        buf.writeLong(msg.trueBodyUntil());
        buf.writeLong(msg.trueBodyCooldownUntil());
        buf.writeBoolean(msg.domainActive());
        buf.writeInt(msg.domainDrainPerSecond());
    }

    public static SyncCombatPacket decode(FriendlyByteBuf buf) {
        return new SyncCombatPacket(
                buf.readInt(),
                buf.readUtf(64),
                buf.readLong(),
                buf.readLong(),
                buf.readBoolean(),
                buf.readInt());
    }

    public static void handle(SyncCombatPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientBridge.onCombatSync(msg));
        ctx.get().setPacketHandled(true);
    }
}
