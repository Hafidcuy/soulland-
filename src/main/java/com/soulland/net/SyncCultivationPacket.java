package com.soulland.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Sinkronisasi data kultivasi server -> client (untuk HUD, halo ring, kartu soul, dan bar skill).
 *
 * <p>Protokol jaringan naik ke "3" pada v1.2B karena paket ini menambah
 * {@code language}, {@code foundation}, {@code tribulationReady}, dan
 * {@code mutationCount}, lalu naik lagi ke "4" pada v1.3 karena paket ini
 * menambah {@code energyCap} (batas spirit power bisa naik karena tulang
 * spirit).
 */
public record SyncCultivationPacket(
        int level,
        int xp,
        int rings,
        float energy,
        String soulId,
        String sect,
        int evolutionStage,
        Map<String, Integer> cooldowns,
        int chainCount,
        String[] loadout,
        int ringSkills,
        int[] ringAges,
        int[] skillCooldowns,
        boolean awakened,
        String language,
        int foundation,
        boolean tribulationReady,
        int mutationCount,
        int energyCap
) {
    /** Skill id hanya berisi huruf, angka, dan garis bawah, jadi pemisah ini aman. */
    private static final char ENTRY_SEPARATOR = ';';
    private static final char KEY_SEPARATOR = '=';

    public static void encode(SyncCultivationPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.level);
        buf.writeInt(msg.xp);
        buf.writeInt(msg.rings);
        buf.writeFloat(msg.energy);
        buf.writeUtf(msg.soulId, 64);
        buf.writeUtf(msg.sect, 32);
        buf.writeInt(msg.evolutionStage);
        buf.writeUtf(encodeCooldowns(msg.cooldowns), 512);
        buf.writeInt(msg.chainCount);
        buf.writeUtf(encodeLoadout(msg.loadout), 256);
        buf.writeInt(msg.ringSkills);
        buf.writeVarIntArray(msg.ringAges);
        buf.writeVarIntArray(msg.skillCooldowns);
        buf.writeBoolean(msg.awakened);
        buf.writeUtf(msg.language == null ? "" : msg.language, 8);
        buf.writeInt(msg.foundation);
        buf.writeBoolean(msg.tribulationReady);
        buf.writeInt(msg.mutationCount);
        buf.writeInt(msg.energyCap);
    }

    public static SyncCultivationPacket decode(FriendlyByteBuf buf) {
        return new SyncCultivationPacket(
                buf.readInt(),
                buf.readInt(),
                buf.readInt(),
                buf.readFloat(),
                buf.readUtf(64),
                buf.readUtf(32),
                buf.readInt(),
                decodeCooldowns(buf.readUtf(512)),
                buf.readInt(),
                decodeLoadout(buf.readUtf(256)),
                buf.readInt(),
                buf.readVarIntArray(),
                buf.readVarIntArray(),
                buf.readBoolean(),
                buf.readUtf(8),
                buf.readInt(),
                buf.readBoolean(),
                buf.readInt(),
                buf.readInt());
    }

    public static void handle(SyncCultivationPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientBridge.onCultivationSync(msg));
        ctx.get().setPacketHandled(true);
    }

    private static String encodeLoadout(String[] loadout) {
        StringBuilder builder = new StringBuilder();
        if (loadout != null) {
            for (String skillId : loadout) {
                if (builder.length() > 0) builder.append(",");
                builder.append(skillId == null ? "" : skillId);
            }
        }
        return builder.toString();
    }

    private static String[] decodeLoadout(String text) {
        String[] loadout = new String[]{"", "", "", "", ""};
        if (text == null || text.isEmpty()) return loadout;
        String[] parts = text.split(",", -1);
        for (int i = 0; i < loadout.length && i < parts.length; i++) {
            loadout[i] = parts[i];
        }
        return loadout;
    }

    private static String encodeCooldowns(Map<String, Integer> cooldowns) {
        StringBuilder builder = new StringBuilder();
        if (cooldowns != null) {
            for (Map.Entry<String, Integer> entry : cooldowns.entrySet()) {
                if (builder.length() > 0) builder.append(ENTRY_SEPARATOR);
                builder.append(entry.getKey()).append(KEY_SEPARATOR).append(entry.getValue());
            }
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
