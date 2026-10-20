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
