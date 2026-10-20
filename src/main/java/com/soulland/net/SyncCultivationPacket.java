package com.soulland.net;

import com.soulland.client.ClientCultivationData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Sinkronisasi data kultivasi server -> client (untuk HUD dan tampilan). */
public record SyncCultivationPacket(int level, int xp, int rings, float energy, String soulId) {

    public static void encode(SyncCultivationPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.level);
        buf.writeInt(msg.xp);
        buf.writeInt(msg.rings);
        buf.writeFloat(msg.energy);
        buf.writeUtf(msg.soulId, 64);
    }

    public static SyncCultivationPacket decode(FriendlyByteBuf buf) {
        return new SyncCultivationPacket(buf.readInt(), buf.readInt(), buf.readInt(), buf.readFloat(), buf.readUtf(64));
    }

    public static void handle(SyncCultivationPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientCultivationData.set(msg)));
        ctx.get().setPacketHandled(true);
    }
}
