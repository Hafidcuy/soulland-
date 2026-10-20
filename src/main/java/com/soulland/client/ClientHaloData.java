package com.soulland.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Umur spirit ring milik setiap pemain di sisi client.
 * Diisi dari {@link com.soulland.net.SyncRingAgesPacket} supaya halo ring
 * pemain lain juga bisa digambar, bukan hanya pemain sendiri.
 */
public final class ClientHaloData {
    private ClientHaloData() {}

    private static final Map<Integer, int[]> RING_AGES = new ConcurrentHashMap<>();

    public static void set(int entityId, int[] ages) {
        RING_AGES.put(entityId, ages == null ? new int[0] : ages.clone());
    }

    /** Umur ring milik entity, atau null bila belum pernah disinkronkan. */
    public static int[] get(int entityId) {
        return RING_AGES.get(entityId);
    }

    public static void remove(int entityId) {
        RING_AGES.remove(entityId);
    }

    /** Membersihkan data entity yang sudah tidak ada di dunia (pemain keluar). */
    public static void prune(java.util.function.IntPredicate stillPresent) {
        RING_AGES.keySet().removeIf(id -> !stillPresent.test(id));
    }
}
