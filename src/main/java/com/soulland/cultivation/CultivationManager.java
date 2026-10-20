package com.soulland.cultivation;

import com.soulland.CultivationRanks;
import com.soulland.net.ModNetwork;
import com.soulland.net.SyncCultivationPacket;
import com.soulland.soul.MartialSoul;
import com.soulland.soul.MartialSouls;
import com.soulland.soul.SkillDef;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;

/** Logika bersama kultivasi: XP, level up, bonus stat, sinkronisasi. Semua dijalankan di server. */
public final class CultivationManager {
    private CultivationManager() {}

    /** Jumlah slot skill yang ditampilkan di HUD. */
    public static final int SKILL_SLOTS = 4;

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
                                cap.getMartialSoulId(), cap.getRingAges(), skillCooldowns(cap, player), cap.isAwakened()));
            }
        });
    }

    /** Sisa cooldown (detik) tiap slot skill milik martial soul pemain ini. */
    private static int[] skillCooldowns(ICultivation cap, Player player) {
        int[] remaining = new int[SKILL_SLOTS];
        MartialSoul soul = MartialSouls.get(cap.getMartialSoulId());
        if (soul == null) return remaining;
        long now = player.level().getGameTime();
        for (int slot = 0; slot < SKILL_SLOTS; slot++) {
            SkillDef skill = soul.skill(slot);
            if (skill == null) continue;
            long until = cap.getCooldownUntil(skill.id());
            if (until > now) remaining[slot] = (int) ((until - now + 19) / 20);
        }
        return remaining;
    }

    /** Menerapkan bonus HP berdasarkan level & jumlah spirit ring. */
    public static void applyStats(Player player) {
        CultivationCapability.get(player).ifPresent(cap -> {
            double ringBonus = cap.getRings() * 2.0;
            Sect sect = Sect.get(cap.getSect());
            double sectHealth = sect == null ? 0.0 : sect.healthBonus();
            double sectSpeed = sect == null ? 0.0 : sect.speedBonus();

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

    /**
     *_true kalau pemain sedang berdiri di gerbang peringkat dan sudah siap(upacara.
     *
     * <p>Gerbang Cultivation_Ranks butuh spirit ring, danuncak ritual: meditasi penuh
     * selama 3 detik dengan satu Esensi Jiwa.
     */
    public static boolean canBreakthrough(ICultivation cap) {
        int level = cap.getLevel();
        if (!CultivationRanks.isGateLevel(level)) return false;
        if (cap.getRings() < CultivationRanks.ringsRequiredAtGate(level)) return false;
        return cap.getXp() >= CultivationRanks.xpForNextLevel(level);
    }

    /** Menaikkan peringkat satu tingkat lewat upacara terobosan. */
    public static void breakthrough(Player player) {
        CultivationCapability.get(player).ifPresent(cap -> {
            if (!canBreakthrough(cap)) return;
            int level = cap.getLevel();
            cap.setXp(cap.getXp() - CultivationRanks.xpForNextLevel(level));
            cap.setLevel(level + 1);
            cap.setGateNotified(false);
            applyStats(player);
            sync(player);
            player.sendSystemMessage(Component.literal("Upacara terobosan berhasil! Peringkat baru: "
                    + CultivationRanks.rankName(cap.getLevel()) + "."));
            if (cap.getLevel() % 10 == 1) {
                player.sendSystemMessage(Component.literal("Skill martial soul terbuka di spirit ring berikutnya."));
            }
        });
    }

    /** Menambah XP kultivasi. Level berhenti di gerbang (10/20/...) sampai pemain menyerap ring baru. */
    public static void addXp(Player player, int amount) {
        CultivationCapability.get(player).ifPresent(cap -> {
            if (cap.getLevel() >= CultivationRanks.MAX_LEVEL) return;

            Sect sect = Sect.get(cap.getSect());
            int gained = sect == null ? amount : (int) Math.round(amount * (1.0 + sect.xpBonus()));

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
                                        + "Burulah soul beast dan serap Spirit Ring baru, lalu tahan C "
                                        + "bermeditasi 3 detik dengan 1 Esensi Jiwa untuk upacara terobosan."));
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
                        "§a⬆ Level Spirit Power naik ke " + cap.getLevel() + ": " + CultivationRanks.rankName(cap.getLevel()) + "!"));
            }
            sync(player);
        });
    }

    /** Daftar skill milik soul pemain (bisa null bila slot tidak ada). */
    public static List<SkillDef> skillsOf(ICultivation cap) {
        MartialSoul soul = MartialSouls.get(cap.getMartialSoulId());
        return soul == null ? List.of() : soul.skills();
    }
}
