package com.soulland.combat;

import com.soulland.cultivation.CultivationCapability;
import com.soulland.cultivation.CultivationManager;
import com.soulland.cultivation.ICultivation;
import com.soulland.lang.SoulLang;
import com.soulland.soul.MartialSoul;
import com.soulland.soul.MartialSouls;
import com.soulland.soul.SoulVisuals;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

/**
 * True Body: server adalah satu-satunya pihak yang boleh membukanya.
 *
 * <p>Aturan mainnya dikunci di {@link TrueBodyRules}: butuh 7 spirit ring,
 * aktif 400 tick, jeda 6000 tick. Semua penolakan memakai pesan yang jelas,
 * supaya pemain tahu bagian mana yang belum terpenuhi.
 */
public final class TrueBodyHandler {
    private TrueBodyHandler() {}

    /** Berapa tick efek True Body bertahan setelah bentuknya habis. */
    private static final int BUFF_GRACE_TICKS = 20;

    /** Buka True Body untuk seorang pemain. */
    public static void activate(ServerPlayer player) {
        CultivationCapability.get(player).ifPresent(cap -> {
            String language = CultivationManager.messageLanguage(player);
            if (!cap.isAwakened()) {
                deny(player, language, "message.soulland.truebody.not_awakened",
                        "Kamu harus menyelesaikan upacara kebangkitan sebelum membuka True Body.");
                return;
            }
            TrueBodyDef def = definitionOf(cap);
            if (def == null) {
                deny(player, language, "message.soulland.truebody.unknown",
                        "Martial soul ini tidak punya True Body.");
                return;
            }
            MartialSoul soul = MartialSouls.effective(cap.getMartialSoulId(), cap.getEvolutionStage());

            long now = player.level().getGameTime();
            if (!TrueBodyRules.isUnlocked(cap.getRings())) {
                deny(player, language, "message.soulland.truebody.need_rings",
                        "True Body butuh %s spirit ring. Punyamu cuma %s.",
                        TrueBodyRules.UNLOCK_RINGS, cap.getRings());
                return;
            }
            if (TrueBodyRules.onCooldown(now, cap.getTrueBodyCooldownUntil())) {
                deny(player, language, "message.soulland.truebody.cooldown",
                        "True Body masih cooldown %s detik.",
                        TrueBodyRules.cooldownSeconds(now, cap.getTrueBodyCooldownUntil()));
                return;
            }
            if (cap.getEnergy() < def.energyCost()) {
                deny(player, language, "message.soulland.truebody.need_energy",
                        "Spirit power tidak cukup untuk True Body. Butuh %s.",
                        (int) def.energyCost());
                return;
            }

            cap.setEnergy(cap.getEnergy() - def.energyCost());
            cap.setTrueBodyUntil(TrueBodyRules.activeUntil(now));
            cap.setTrueBodyCooldownUntil(cap.getTrueBodyUntil() + TrueBodyRules.COOLDOWN_TICKS);
            applySelfBuff(player, def);
            applyAura(player, def);
            CombatSync.broadcast(player, cap);

            int percent = Math.round((def.damageMultiplier() - 1f) * 100f);
            player.sendSystemMessage(SoulLang.text(language, "message.soulland.truebody.activated",
                            "True Body aktif: %s. Damage +%s%% selama %s detik.",
                            name(def, language), percent, TrueBodyRules.DURATION_SECONDS)
                    .withStyle(ChatFormatting.GOLD));
            playActivationFx(player, def, soul);
        });
    }

    /**
     * Perbarui True Body setiap tick: segarkan buff saat bentuknya habis, lalu
     * laporkan kalau jeda dimulai.
     */
    public static void tick(Player player, ICultivation cap) {
        long now = player.level().getGameTime();
        if (cap.getTrueBodyUntil() <= 0L) return;
        if (!TrueBodyRules.isActive(now, cap.getTrueBodyUntil())) {
            cap.setTrueBodyUntil(0L);
            if (player instanceof ServerPlayer sp) {
                sp.sendSystemMessage(SoulLang.text(CultivationManager.messageLanguage(sp),
                        "message.soulland.truebody.expired", "True Body berakhir. Jeda dimulai."));
                CombatSync.broadcast(sp, cap);
            }
            return;
        }
        if (player.tickCount % 20 != 0) return;

        TrueBodyDef def = definitionOf(cap);
        if (def == null) return;
        applySelfBuff(player, def);
        applyAura(player, def);
    }

    /** Pengali damage keluar selama True Body aktif. 1.0 kalau tidak aktif. */
    public static float damageMultiplierOf(Player player) {
        if (!(player instanceof ServerPlayer sp)) return 1f;
        return CultivationCapability.get(sp).map(cap -> {
            long now = sp.level().getGameTime();
            if (!TrueBodyRules.isActive(now, cap.getTrueBodyUntil())) return 1f;
            TrueBodyDef def = definitionOf(cap);
            return def == null ? 1f : def.damageMultiplier();
        }).orElse(1f);
    }

    /** Definisi True Body dari soul yang sedang dipakai pemain. */
    public static TrueBodyDef definitionOf(ICultivation cap) {
        MartialSoul soul = MartialSouls.effective(cap.getMartialSoulId(), cap.getEvolutionStage());
        return soul == null ? null : TrueBodyCatalog.get(soul.id());
    }

    private static void applySelfBuff(Player player, TrueBodyDef def) {
        applyEffect(player, def.selfBuff());
    }

    /** Aura True Body: buff untuk sekutu, debuff untuk musuh di sekitar. */
    private static void applyAura(Player player, TrueBodyDef def) {
        double radius = def.auraRadius();
        ServerLevel level = (ServerLevel) player.level();

        for (ServerPlayer ally : level.players()) {
            if (ally == player) continue;
            if (ally.distanceTo(player) > radius) continue;
            applyEffect(ally, def.auraBuff());
        }

        AABB box = player.getBoundingBox().inflate(radius);
        for (LivingEntity enemy : level.getEntitiesOfClass(LivingEntity.class, box,
                entity -> entity != player && entity.isAlive() && !(entity instanceof ServerPlayer))) {
            applyEffect(enemy, def.auraDebuff());
        }
    }

    private static void applyEffect(LivingEntity target, BuffProfile profile) {
        if (!profile.present()) return;
        MobEffect effect = CombatEffects.of(profile.effect());
        if (effect == null) return;
        target.addEffect(new MobEffectInstance(effect, TrueBodyRules.DURATION_TICKS + BUFF_GRACE_TICKS,
                profile.amplifier(), false, true));
    }

    private static void playActivationFx(ServerPlayer player, TrueBodyDef def, MartialSoul soul) {
        ServerLevel level = player.serverLevel();
        level.sendParticles(SoulVisuals.particle(soul == null ? "" : soul.id()),
                player.getX(), player.getY() + 1.0, player.getZ(), 48, 0.7, 0.9, 0.7, 0.05);
        level.sendParticles(new DustParticleOptions(
                        new org.joml.Vector3f(
                                ((def.color() >> 16) & 0xFF) / 255f,
                                ((def.color() >> 8) & 0xFF) / 255f,
                                (def.color() & 0xFF) / 255f), 1.4f),
                player.getX(), player.getY() + 1.0, player.getZ(), 32, 0.5, 0.8, 0.5, 0.02);
    }

    private static String name(TrueBodyDef def, String language) {
        return SoulLang.translate(language, "soul.soulland.truebody." + def.soulId() + ".name", def.nameEn());
    }

    private static void deny(ServerPlayer player, String language, String key, String fallback, Object... args) {
        player.sendSystemMessage(SoulLang.text(language, key, fallback, args).withStyle(ChatFormatting.RED));
    }
}
