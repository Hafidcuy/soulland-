package com.soulland.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.LinkedHashMap;
import java.util.Map;
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

    private static String encodeLoadout(String[] loadout) {
        StringBuilder builder = new StringBuilder();
        for (String skillId : loadout) {
            if (builder.length() > 0) builder.append(",");
            builder.append(skillId == null ? "" : skillId);
        }
        return builder.toString();
    }

    private static String[] decodeLoadout(String text) {
        String[] loadout = new String[5];
        if (text == null || text.isEmpty()) return loadout;
        String[] parts = text.split(",", -1);
        for (int i = 0; i < loadout.length && i < parts.length; i++) {
            loadout[i] = parts[i];
        }
        return loadout;
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
