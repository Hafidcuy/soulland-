package com.soulland.client;

import com.soulland.net.SyncCombatPacket;
import net.minecraft.client.Minecraft;

import java.util.HashMap;
import java.util.Map;

/**
 * Salinan status True Body dan Domain di sisi client.
 *
 * <p>Status ini dipakai HUD untuk pemain sendiri, dan dipakai renderer untuk
 * pemain lain: manifestasi dan halo mereka membesar ketika True Body aktif.
 */
public final class ClientCombatData {
    private ClientCombatData() {}

    /** Status True Body dan Domain satu pemain. */
    public record State(
            String soulId,
            long trueBodyUntil,
            long trueBodyCooldownUntil,
            boolean domainActive,
            int domainDrainPerSecond
    ) {
        /** True kalau True Body sedang aktif pada waktu game tertentu. */
        public boolean trueBodyActive(long gameTime) {
            return gameTime < trueBodyUntil;
        }

        /** Sisa waktu True Body dalam detik, 0 kalau tidak aktif. */
        public int trueBodySeconds(long gameTime) {
            long remaining = trueBodyUntil - gameTime;
            if (remaining <= 0L) return 0;
            return (int) ((remaining + 19) / 20);
        }

        /** Sisa jeda True Body dalam detik, 0 kalau jeda sudah lewat. */
        public int cooldownSeconds(long gameTime) {
            long remaining = trueBodyCooldownUntil - gameTime;
            if (remaining <= 0L) return 0;
            return (int) ((remaining + 19) / 20);
        }
    }

    private static final Map<Integer, State> BY_ENTITY = new HashMap<>();

    private static State local = new State("", 0L, 0L, false, 0);

    /** Simpan status yang baru diterima dari server. */
    public static void set(SyncCombatPacket packet) {
        State state = new State(packet.soulId(), packet.trueBodyUntil(), packet.trueBodyCooldownUntil(),
                packet.domainActive(), packet.domainDrainPerSecond());
        BY_ENTITY.put(packet.entityId(), state);
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && packet.entityId() == mc.player.getId()) {
            local = state;
        }
    }

    /** Status pemain dengan id tertentu, atau null kalau belum pernah disinkronkan. */
    public static State stateOf(int entityId) {
        return BY_ENTITY.get(entityId);
    }

    /** Status pemain lokal. */
    public static State local() {
        return local;
    }

    /** True kalau True Body pemain dengan id tertentu sedang aktif. */
    public static boolean trueBodyActive(int entityId, long gameTime) {
        State state = BY_ENTITY.get(entityId);
        return state != null && state.trueBodyActive(gameTime);
    }

    /** Buang status pemain yang sudah tidak ada di dunia. */
    public static void prune(java.util.function.IntPredicate alive) {
        BY_ENTITY.keySet().removeIf(id -> !alive.test(id));
    }
}
