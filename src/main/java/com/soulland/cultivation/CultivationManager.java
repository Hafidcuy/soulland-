package com.soulland.cultivation;

import com.soulland.CultivationRanks;
import com.soulland.bone.BoneSetBonus;
import com.soulland.bone.BoneStats;
import com.soulland.bone.SpiritBoneManager;
import com.soulland.lang.SoulLang;
import com.soulland.net.ModNetwork;
import com.soulland.net.SyncCultivationPacket;
import com.soulland.sect.Sect;
import com.soulland.soul.MartialSoul;
import com.soulland.soul.MartialSouls;
import com.soulland.soul.SkillDef;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;

/** Logika bersama kultivasi: XP, level up, fondasi, bonus stat, sinkronisasi. Semua dijalankan di server. */
public final class CultivationManager {
    private CultivationManager() {}

    /** Jumlah slot skill yang ditampilkan di HUD. */
    public static final int SKILL_SLOTS = 4;

    /** Bahasa yang dipilih pemain; kosong kalau belum memilih. */
    public static String languageOf(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return SoulLang.activeLanguage();
        }
        return CultivationCapability.get(serverPlayer)
                .map(ICultivation::getLanguage)
                .map(SoulLang::normalize)
                .orElse(SoulLang.UNSET);
    }

    /** Bahasa yang dipakai untuk pesan ke pemain: pilihannya, atau default mod. */
    public static String messageLanguage(Player player) {
        String language = languageOf(player);
        return language.isEmpty() ? SoulLang.DEFAULT : language;
    }

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
                                cap.getMartialSoulId(), cap.getSect(), cap.getEvolutionStage(), cooldowns,
                                cap.getChainCount(), cap.getLoadout(), cap.getRingSkills(),
                                cap.getRingAges(), skillCooldowns(cap, player), cap.isAwakened(),
                                cap.getLanguage(), cap.getFoundation(), cap.isTribulationReady(),
                                cap.getMutationCount(), cap.getEnergyCap()));
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

    /**
     * Menerapkan bonus HP, serangan, defense, dan batas spirit power.
     *
     * <p>Sumber bonus: level kultivasi, jumlah spirit ring, mutasi ring, sekte,
     * dan tulang spirit yang terpasang. Tulang menambah angka flat; set bonus
     * menambah persentase.
     */
    public static void applyStats(Player player) {
        CultivationCapability.get(player).ifPresent(cap -> {
            double ringBonus = cap.getRings() * 2.0;
            double mutationBonus = RingMutation.healthBonus(cap.getMutationCount());
            Sect sect = Sect.get(cap.getSect());
            double sectHealth = sect == null ? 0.0 : sect.healthBonus();
            double sectSpeed = sect == null ? 0.0 : sect.speedBonus();

            BoneStats bones = SpiritBoneManager.stats(player);
            BoneSetBonus set = SpiritBoneManager.setBonus(player);

            double maxHealth = 20.0 + (cap.getLevel() - 1) * 0.5 + ringBonus + mutationBonus;
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
                double value = 1.0 + cap.getRings() * 0.5
                        + RingMutation.damageBonus(cap.getMutationCount())
                        + bones.attack();
                attack.setBaseValue(value * (1.0 + set.attackPercent() / 100.0));
            }

            var armor = player.getAttribute(Attributes.ARMOR);
            if (armor != null) {
                armor.setBaseValue(bones.defense());
            }

            var speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speed != null) {
                speed.setBaseValue(0.1 * (1.0 + sectSpeed));
            }

            cap.setEnergyCap((int) (CultivationRanks.MAX_ENERGY + bones.energy() + set.energy()));
        });
    }

    /**
     * *_true kalau pemain sedang berdiri di gerbang peringkat dan sudah siap(upacara).
     *
     * <p>Gerbang Cultivation_Ranks butuh spirit ring, fondasi yang cukup, dan
     * puncak ritual: meditasi penuh selama 3 detik dengan satu Esensi Jiwa.
     */
    public static boolean canBreakthrough(ICultivation cap) {
        int level = cap.getLevel();
        if (!CultivationRanks.isGateLevel(level)) return false;
        if (cap.getRings() < CultivationRanks.ringsRequiredAtGate(level)) return false;
        if (!Foundation.canAttempt(cap.getFoundation())) return false;
        return cap.getXp() >= CultivationRanks.xpForNextLevel(level);
    }

    /**
     * Menaikkan peringkat satu tingkat lewat upacara terobosan.
     *
     * <p>Peluang berhasil mengikuti fondasi pemain: fondasi penuh berarti pasti
     * berhasil, fondasi kosong masih memberi peluang 60 persen. Gagal berarti
     * fondasi melemah dan ceremony harus diulang.
     */
    public static void breakthrough(Player player) {
        CultivationCapability.get(player).ifPresent(cap -> {
            if (!canBreakthrough(cap)) return;
            int level = cap.getLevel();
            String language = messageLanguage(player);
            int chance = Foundation.successChancePercent(cap.getFoundation());

            if (player.getRandom().nextInt(100) >= chance) {
                cap.setFoundation(Foundation.afterFailure(cap.getFoundation()));
                player.sendSystemMessage(SoulLang.text(language, "message.soulland.breakthrough_failed",
                        "Terobosan goyah. Fondasi melemah, kamu harus memeditasi lagi."));
                player.sendSystemMessage(SoulLang.text(language, "message.soulland.breakthrough_chance",
                        "Peluang terobosan: %s%% (fondasi %s/100).",
                        chance, cap.getFoundation()));
                sync(player);
                return;
            }

            cap.setXp(cap.getXp() - CultivationRanks.xpForNextLevel(level));
            cap.setLevel(level + 1);
            cap.setGateNotified(false);
            cap.setFoundation(0);
            applyStats(player);
            sync(player);
            player.sendSystemMessage(SoulLang.text(language, "message.soulland.breakthrough_success",
                    "Upacara terobosan berhasil! Peringkat baru: %s.",
                    CultivationRanks.rankName(cap.getLevel())));
            if (cap.getLevel() % 10 == 1) {
                player.sendSystemMessage(SoulLang.text(language, "message.soulland.skill_unlock_next",
                        "Skill martial soul terbuka di spirit ring berikutnya."));
            }
            if (cap.getLevel() >= CultivationRanks.GOD_THRESHOLD) {
                markTribulationReady(player, cap, language);
            }
        });
    }

    /**
     * Tanda ambang dewa: fondasi sudah cukup kuat untuk menahan tribulasi.
     * Sistem tribulasi lengkap menyusul di v1.6, jadi v1.2B hanya menandai
     * dan Grounds lewat kilat serta pesan dunia.
     */
    private static void markTribulationReady(Player player, ICultivation cap, String language) {
        if (cap.isTribulationReady()) return;
        cap.setTribulationReady(true);
        player.sendSystemMessage(SoulLang.text(language, "message.soulland.tribulation_ready",
                "Langit bergemuruh. Ambang dewa terbuka: tribulasi akan turun."));
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.serverLevel().sendParticles(net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK,
                    player.getX(), player.getY() + 1.2, player.getZ(), 60, 0.8, 1.4, 0.8, 0.05);
            serverPlayer.serverLevel().sendParticles(net.minecraft.core.particles.ParticleTypes.FLASH,
                    player.getX(), player.getY() + 1.0, player.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
            serverPlayer.serverLevel().getServer().getPlayerList().broadcastSystemMessage(
                    SoulLang.text(language, "message.soulland.tribulation_world",
                            "Langit di atas %s bergemuruh. Seseorang mencapai ambang dewa!", player.getName().getString()),
                    false);
        }
        sync(player);
    }

    /** Menambah XP kultivasi. Level berhenti di gerbang (batas peringkat) sampai Players naik lewat upacara. */
    public static void addXp(Player player, int amount) {
        CultivationCapability.get(player).ifPresent(cap -> {
            if (cap.getLevel() >= CultivationRanks.MAX_LEVEL) return;

            Sect sect = Sect.get(cap.getSect());
            int gained = sect == null ? amount : (int) Math.round(amount * (1.0 + sect.xpBonus()));
            String language = messageLanguage(player);

            int xp = cap.getXp() + gained;
            boolean leveled = false;

            while (cap.getLevel() < CultivationRanks.MAX_LEVEL && xp >= CultivationRanks.xpForNextLevel(cap.getLevel())) {
                int lvl = cap.getLevel();
                if (CultivationRanks.isGateLevel(lvl) && cap.getRings() < CultivationRanks.ringsRequiredAtGate(lvl)) {
                    xp = CultivationRanks.xpForNextLevel(lvl);
                    if (!cap.isGateNotified()) {
                        cap.setGateNotified(true);
                        player.sendSystemMessage(SoulLang.text(language, "message.soulland.gate_blocked",
                                "§6Gerbang level %s (%s) menutup jalanmu. "
                                        + "Burulah soul beast dan serap Spirit Ring baru, lalu tahan C "
                                        + "bermeditasi 3 detik dengan 1 Esensi Jiwa untuk upacara terobosan.",
                                lvl, CultivationRanks.rankName(lvl)));
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
                player.sendSystemMessage(SoulLang.text(language, "message.soulland.level_up",
                        "§a⬆ Level Spirit Power naik ke %s: %s!", cap.getLevel(),
                        CultivationRanks.rankName(cap.getLevel())));
                if (cap.getLevel() >= CultivationRanks.GOD_THRESHOLD && !cap.isTribulationReady()) {
                    markTribulationReady(player, cap, language);
                }
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
