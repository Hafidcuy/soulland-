package com.soulland.net;

import com.soulland.client.ClientCultivationData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/** Sinkronisasi data kultivasi server -> client (untuk HUD, kartu soul, dan bar skill). */
public record SyncCultivationPacket(
        int level,
        int xp,
        int rings,
        float energy,
        String soulId,
        String sect,
        int evolutionStage,
        Map<String, Integer> cooldowns
) {
    /** Skill id hanya berisi huruf, angka, dan garis bawah, jadi pemisah ini aman. */
    private static final char ENTRY_SEPARATOR = ';';
    private static final char KEY_SEPARATOR = '=';

    public static void encode(SyncCultivationPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.level());
        buf.writeInt(msg.xp());
        buf.writeInt(msg.rings());
        buf.writeFloat(msg.energy());
        buf.writeUtf(msg.soulId(), 64);
        buf.writeUtf(msg.sect(), 32);
        buf.writeInt(msg.evolutionStage());
        buf.writeUtf(encodeCooldowns(msg.cooldowns()), 512);
    }

    public static SyncCultivationPacket decode(FriendlyByteBuf buf) {
        return new SyncCultivationPacket(buf.readInt(), buf.readInt(), buf.readInt(), buf.readFloat(),
                buf.readUtf(64), buf.readUtf(32), buf.readInt(),
                decodeCooldowns(buf.readUtf(512)));
    }

    public static void handle(SyncCultivationPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientCultivationData.set(msg)));
        ctx.get().setPacketHandled(true);
    }

    private static String encodeCooldowns(Map<String, Integer> cooldowns) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Integer> entry : cooldowns.entrySet()) {
            if (builder.length() > 0) builder.append(ENTRY_SEPARATOR);
            builder.append(entry.getKey()).append(KEY_SEPARATOR).append(entry.getValue());
        }
        return builder.toString();
    }

    private static Map<String, Integer> decodeCooldowns(String text) {
        Map<String, Integer> cooldowns = new LinkedHashMap<>();
        if (text == null || text.isEmpty()) return cooldowns;
        for (String entry : text.split(String.valueOf(ENTRY_SEPARATOR))) {
            int split = entry.indexOf(KEY_SEPARATOR);
            if (split <= 0) continue;
            try {
                cooldowns.put(entry.substring(0, split), Integer.parseInt(entry.substring(split + 1)));
            } catch (NumberFormatException ignored) {
                // data rusak diabaikan, bukan membuat seluruh sinkronisasi gagal
            }
        }
        return cooldowns;
    }
}
