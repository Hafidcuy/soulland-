package com.soulland.combat;

import com.soulland.cultivation.ICultivation;
import com.soulland.net.ModNetwork;
import com.soulland.net.SyncCombatPacket;
import com.soulland.soul.MartialSoul;
import com.soulland.soul.MartialSouls;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

/**
 * Penyebaran status True Body dan Domain ke client.
 *
 * <p>Status dikirim ke pemilik state dan ke pemain di sekitar, karena
 * manifestasi yang membesar dan halo yang melebar harus kelihatan oleh orang
 * lain, bukan cuma oleh pemiliknya.
 */
public final class CombatSync {
    private CombatSync() {}

    /** Jangkauan pandang status combat, sama dengan jangkauan animasi manifestasi. */
    private static final double VIEW_RANGE = 48.0;

    /** Kirim status combat seorang pemain ke dirinya dan ke pemain di sekitar. */
    public static void broadcast(ServerPlayer player, ICultivation cap) {
        MartialSoul soul = MartialSouls.effective(cap.getMartialSoulId(), cap.getEvolutionStage());
        String soulId = soul == null ? "" : soul.id();
        int drain = 0;
        if (cap.isDomainActive() && soul != null) {
            DomainDef def = DomainCatalog.get(soul.id());
            if (def != null) drain = def.drainPerSecond();
        }

        SyncCombatPacket packet = new SyncCombatPacket(player.getId(), soulId,
                cap.getTrueBodyUntil(), cap.getTrueBodyCooldownUntil(), cap.isDomainActive(), drain);
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
        for (ServerPlayer other : player.serverLevel().players()) {
            if (other != player && other.distanceTo(player) <= VIEW_RANGE) {
                ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> other), packet);
            }
        }
    }
}
