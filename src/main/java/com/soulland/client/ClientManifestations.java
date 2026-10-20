package com.soulland.client;

import net.minecraft.client.Minecraft;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Status animasi manifestasi martial soul di sisi client.
 * Server memberi tahu client lewat {@link com.soulland.net.ManifestationPacket},
 * client menggambarnya sebagai efek visual murni (tidak memengaruhi gameplay).
 */
public final class ClientManifestations {
    private ClientManifestations() {}

    /** Durasi animasi manifestasi (tick). */
    public static final int DURATION_TICKS = 60;

    public record Manifestation(String soulId, long startTick) {}

    private static final Map<Integer, Manifestation> ACTIVE = new ConcurrentHashMap<>();

    public static void start(int entityId, String soulId) {
        long tick = currentTick();
        ACTIVE.put(entityId, new Manifestation(soulId, tick));
    }

    public static Manifestation get(int entityId) {
        return ACTIVE.get(entityId);
    }

    public static void clear(int entityId) {
        ACTIVE.remove(entityId);
    }

    /** Buang manifestasi yang sudah selesai supaya peta tidak menumpuk. */
    public static void removeExpired(long currentTick) {
        ACTIVE.values().removeIf(m -> currentTick - m.startTick() > DURATION_TICKS);
    }

    private static long currentTick() {
        Minecraft mc = Minecraft.getInstance();
        return mc.level == null ? 0L : mc.level.getGameTime();
    }
}
