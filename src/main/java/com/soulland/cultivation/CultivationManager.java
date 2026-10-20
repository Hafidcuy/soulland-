package com.soulland.cultivation;

import com.soulland.CultivationRanks;
import com.soulland.net.ModNetwork;
import com.soulland.net.SyncCultivationPacket;
import com.soulland.sect.Sect;
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
                long now = sp.level().getGameTime();
                java.util.Map<String, Integer> cooldowns = new java.util.HashMap<>();
                cap.forEachCooldown((skillId, until) -> {
                    long remaining = until - now;
                    if (remaining > 0) {
                        cooldowns.put(skillId, (int) ((remaining + 19) / 20));
                    }
                });
                ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> sp),
                        new SyncCultivationPacket(cap.getLevel(), cap.getXp(), cap.getRings(), cap.getEnergy(),
                                cap.getMartialSoulId(), cap.getSect(), cap.getEvolutionStage(), cooldowns));
            }
        });
    }

    /**
     * Menerapkan bonus HP, attack, dan kecepatan dari level, spirit ring, dan sekte.
     *
     * <p>Spirit ring memberi bonus yang menumpuk: makin banyak ring, makin kuat pemain,
     * dan itu yang membuat berburu ring terasa bernilai.
     */
    public static void applyStats(Player player) {
        CultivationCapability.get(player).ifPresent(cap -> {
            double ringBonus = cap.getRings() * 2.0;
            Sect sect = Sect.get(cap.getSect());
            double sectHealth = sect == null ? 0.0 : sect.healthBonus;
            double sectSpeed = sect == null ? 0.0 : sect.speedBonus;

            double maxHealth = 20.0 + (cap.getLevel() - 1) * 0.5 + ringBonus;
            maxHealth *= (1.0 + sectHealth);

            var health = player.getAttribute(Attributes.MAX_HEALTH);
            if (health != null) {
                health.setBaseValue(maxHealth);
                if (player.getHealth() > player.getMaxHealth()) {
                    player.setHealth(player.getMaxHealth());
                }
            }

            var attack = player.getAttribute(Attributes.ATTACK_DAMAGE);
            if (attack != null) {
                attack.setBaseValue(1.0 + cap.getRings() * 0.5);
            }

            var speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speed != null) {
                speed.setBaseValue(0.1 * (1.0 + sectSpeed));
            }
        });
    }

    /** Menambah XP kultivasi. Level berhenti di gerbang (10/20/...) sampai pemain menyerap ring baru. */
    public static void addXp(Player player, int amount) {
        CultivationCapability.get(player).ifPresent(cap -> {
            if (cap.getLevel() >= CultivationRanks.MAX_LEVEL) return;

            Sect sect = Sect.get(cap.getSect());
            int gained = sect == null ? amount : (int) Math.round(amount * (1.0 + sect.xpBonus));

            int xp = cap.getXp() + gained;
            boolean leveled = false;

            while (cap.getLevel() < CultivationRanks.MAX_LEVEL && xp >= CultivationRanks.xpForNextLevel(cap.getLevel())) {
                int lvl = cap.getLevel();
                if (CultivationRanks.isGateLevel(lvl) && cap.getRings() < CultivationRanks.ringsRequiredAtGate(lvl)) {
                    xp = CultivationRanks.xpForNextLevel(lvl);
                    if (!cap.isGateNotified()) {
                        cap.setGateNotified(true);
                        player.sendSystemMessage(Component.literal(
                                "§6Gerbang level " + lvl + " (" + CultivationRanks.rankName(lvl) + ") menutup jalanmu. "
                                        + "Burulah soul beast dan serap Spirit Ring baru untuk menembus peringkat berikutnya."));
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
                        "Level Spirit Power naik ke " + cap.getLevel() + ", peringkat " + CultivationRanks.rankName(cap.getLevel()) + "."));
            }
            sync(player);
        });
    }
}
