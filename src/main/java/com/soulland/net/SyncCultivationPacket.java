package com.soulland.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Sinkronisasi data kultivasi server -> client (untuk HUD, halo ring, dan tampilan). */
public record SyncCultivationPacket(
        int level,
        int xp,
        int rings,
        float energy,
        String soulId,
        int[] ringAges,
        int[] skillCooldowns,
        boolean awakened
) {

    public static void encode(SyncCultivationPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.level);
        buf.writeInt(msg.xp);
        buf.writeInt(msg.rings);
        buf.writeFloat(msg.energy);
        buf.writeUtf(msg.soulId, 64);
        buf.writeVarIntArray(msg.ringAges);
        buf.writeVarIntArray(msg.skillCooldowns);
        buf.writeBoolean(msg.awakened);
    }

    public static SyncCultivationPacket decode(FriendlyByteBuf buf) {
        return new SyncCultivationPacket(
                buf.readInt(),
                buf.readInt(),
                buf.readInt(),
                buf.readFloat(),
                buf.readUtf(64),
                buf.readVarIntArray(),
                buf.readVarIntArray(),
                buf.readBoolean());
    }

    public static void handle(SyncCultivationPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientBridge.onCultivationSync(msg));
        ctx.get().setPacketHandled(true);
    }
}
