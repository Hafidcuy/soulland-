package com.soulland.soul;

import com.soulland.cultivation.CultivationCapability;
import com.soulland.cultivation.ICultivation;
import com.soulland.net.ModNetwork;
import com.soulland.net.SyncRingAgesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

/**
 * Menyinkronkan umur spirit ring ke pemain di sekitar supaya halo ring milik
 * setiap pemain terlihat, bukan hanya milik pemain itu sendiri.
 */
public final class RingHaloSync {
    private RingHaloSync() {}

    /** Jarak pandang sinkronisasi halo ring (blok). */
    public static final double SYNC_RANGE = 64.0;

    public static void broadcast(ServerPlayer player) {
        int[] ages = CultivationCapability.get(player)
                .map(ICultivation::getRingAges)
                .orElseGet(() -> new int[0]);
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new SyncRingAgesPacket(player.getId(), ages));
        for (ServerPlayer other : player.serverLevel().players()) {
            if (other != player && other.distanceTo(player) <= SYNC_RANGE) {
                ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> other),
                        new SyncRingAgesPacket(player.getId(), ages));
            }
        }
    }
}
