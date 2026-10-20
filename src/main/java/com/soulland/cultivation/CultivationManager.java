package com.soulland.cultivation;

import com.soulland.CultivationRanks;
import com.soulland.net.ModNetwork;
import com.soulland.net.SyncCultivationPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.PacketDistributor;

/** Logika bersama kultivasi: XP, level up, bonus stat, sinkronisasi. Semua dijalankan di server. */
public final class CultivationManager {
    private CultivationManager() {}

    public static void sync(Player player) {
        CultivationCapability.get(player).ifPresent(cap -> {
            if (player instanceof net.minecraft.server.level.ServerPlayer sp) {
                ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> sp),
                        new SyncCultivationPacket(cap.getLevel(), cap.getXp(), cap.getRings(), cap.getEnergy(), cap.getMartialSoulId()));
            }
        });
    }

    /** Menerapkan bonus HP berdasarkan level & jumlah spirit ring. */
    public static void applyStats(Player player) {
        CultivationCapability.get(player).ifPresent(cap -> {
            double bonus = (cap.getLevel() - 1) * 0.5 + cap.getRings() * 2.0;
            var attr = player.getAttribute(Attributes.MAX_HEALTH);
            if (attr != null) {
                attr.setBaseValue(20.0 + bonus);
                if (player.getHealth() > player.getMaxHealth()) {
                    player.setHealth(player.getMaxHealth());
                }
            }
        });
    }

    /** Menambah XP kultivasi. Level berhenti di gerbang (10/20/...) sampai pemain menyerap ring baru. */
    public static void addXp(Player player, int amount) {
        CultivationCapability.get(player).ifPresent(cap -> {
            if (cap.getLevel() >= CultivationRanks.MAX_LEVEL) return;
            int xp = cap.getXp() + amount;
            boolean leveled = false;

            while (cap.getLevel() < CultivationRanks.MAX_LEVEL && xp >= CultivationRanks.xpForNextLevel(cap.getLevel())) {
                int lvl = cap.getLevel();
                if (CultivationRanks.isGateLevel(lvl) && cap.getRings() < CultivationRanks.ringsRequiredAtGate(lvl)) {
                    xp = CultivationRanks.xpForNextLevel(lvl); // XP penuh, menunggu ring
                    if (!cap.isGateNotified()) {
                        cap.setGateNotified(true);
                        player.sendSystemMessage(Component.literal(
                                "§6⚠ Kamu mencapai gerbang level " + lvl + " (" + CultivationRanks.rankName(lvl) + "). " +
                                "Burulah soul beast dan serap Spirit Ring baru untuk menembus peringkat berikutnya!"));
                    }
                    break;
                }
                xp -= CultivationRanks.xpForNextLevel(lvl);
                cap.setLevel(lvl + 1);
                cap.setGateNotified(false);
                leveled = true;
            }

            cap.setXp(xp);
            if (leveled) {
                applyStats(player);
                player.sendSystemMessage(Component.literal(
                        "§a⬆ Level Spirit Power naik ke " + cap.getLevel() + " — " + CultivationRanks.rankName(cap.getLevel()) + "!"));
            }
            sync(player);
        });
    }
}
