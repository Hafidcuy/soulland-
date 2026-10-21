package com.soulland.combat;

import com.soulland.cultivation.CultivationCapability;
import com.soulland.cultivation.CultivationManager;
import com.soulland.cultivation.ICultivation;
import com.soulland.lang.SoulLang;
import com.soulland.soul.Element;
import com.soulland.soul.MartialSoul;
import com.soulland.soul.MartialSouls;
import com.soulland.soul.SoulBeast;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;

/**
 * Domain: wilayah yang berdiri di sekitar pemain.
 *
 * <p>Domain adalah sakelar, bukan tombol sekali pakai. Selama aktif, server
 * menghitung efeknya tiap tick di dalam radius yang tetap, menguras spirit
 * power pemilik, dan menutup domain otomatis saat energi habis.
 */
public final class DomainHandler {
    private DomainHandler() {}

    /** Lama efek status bertahan setelah disegarkan. */
    private static final int EFFECT_DURATION_TICKS = 40;

    /** Nyala atau matikan domain seorang pemain. */
    public static void toggle(ServerPlayer player) {
        CultivationCapability.get(player).ifPresent(cap -> {
            String language = CultivationManager.messageLanguage(player);
            if (!cap.isAwakened()) {
                deny(player, language, "message.soulland.domain.not_awakened",
                        "Kamu harus menyelesaikan upacara kebangkitan sebelum berdiri di domain.");
                return;
            }
            MartialSoul soul = MartialSouls.effective(cap.getMartialSoulId(), cap.getEvolutionStage());
            DomainDef def = soul == null ? null : DomainCatalog.get(soul.id());
            if (def == null) {
                deny(player, language, "message.soulland.domain.unknown",
                        "Martial soul ini tidak punya domain.");
                return;
            }

            if (cap.isDomainActive()) {
                cap.setDomainActive(false);
                CombatSync.broadcast(player, cap);
                player.sendSystemMessage(SoulLang.text(language, "message.soulland.domain.closed",
                        "Domain ditutup.").withStyle(ChatFormatting.GRAY));
                return;
            }

            if (cap.getEnergy() < def.drainPerSecond()) {
                deny(player, language, "message.soulland.domain.need_energy",
                        "Spirit power tidak cukup untuk berdiri di domain. Butuh %s.",
                        def.drainPerSecond());
                return;
            }

            cap.setDomainActive(true);
            CombatSync.broadcast(player, cap);
            player.sendSystemMessage(SoulLang.text(language, "message.soulland.domain.opened",
                            "Domain berdiri: %s. Spirit power terkuras %s per detik.",
                            name(def, language), def.drainPerSecond())
                    .withStyle(ChatFormatting.GOLD));
        });
    }

    /**
     * Hitung Domain satu tick: kuras spirit power, beri efek di dalam radius,
     * dan tutup domain otomatis saat energi habis.
     */
    public static void tick(Player player, ICultivation cap) {
        if (!cap.isDomainActive()) return;

        MartialSoul soul = MartialSouls.effective(cap.getMartialSoulId(), cap.getEvolutionStage());
        DomainDef def = soul == null ? null : DomainCatalog.get(soul.id());
        if (def == null) {
            cap.setDomainActive(false);
            if (player instanceof ServerPlayer sp) CombatSync.broadcast(sp, cap);
            return;
        }

        cap.setEnergy(cap.getEnergy() - DomainRules.drainPerTick(def.drainPerSecond()));
        if (cap.getEnergy() <= 0f) {
            cap.setEnergy(0f);
            cap.setDomainActive(false);
            if (player instanceof ServerPlayer sp) {
                sp.sendSystemMessage(SoulLang.text(CultivationManager.messageLanguage(sp),
                        "message.soulland.domain.drained", "Spirit power habis, domain menutup diri."));
                CombatSync.broadcast(sp, cap);
            }
            return;
        }

        applyField(player, def, soul);
        drawRing(player, def);
    }

    /** Efek domain di dalam radius: buff untuk sekutu, debuff dan damage untuk musuh. */
    private static void applyField(Player player, DomainDef def, MartialSoul soul) {
        ServerLevel level = (ServerLevel) player.level();
        double radius = DomainRules.RADIUS;
        AABB box = player.getBoundingBox().inflate(radius);
        boolean refresh = player.tickCount % DomainRules.EFFECT_REFRESH_INTERVAL == 0;
        float damage = DomainRules.damagePerTick(def.damagePerSecond());
        Element element = MartialSouls.elementOf(soul);

        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, box,
                other -> other != player && other.isAlive())) {
            if (entity instanceof ServerPlayer ally) {
                if (refresh) applyEffect(ally, def.allyBuff());
                continue;
            }
            if (refresh) applyEffect(entity, def.enemyDebuff());
            if (damage > 0f) {
                float scaled = (float) (damage * element.multiplierAgainst(SoulBeast.elementOf(entity)));
                entity.hurt(player.damageSources().playerAttack(player), scaled);
            }
        }
    }

    /** Cincin partikel di tanah, warna cincin mengikuti elemen domain. */
    private static void drawRing(Player player, DomainDef def) {
        if (player.tickCount % DomainRules.RING_PARTICLE_INTERVAL != 0) return;
        ServerLevel level = (ServerLevel) player.level();
        double radius = DomainRules.RADIUS;
        double step = (Math.PI * 2.0) / DomainRules.RING_PARTICLES;
        DustParticleOptions dust = new DustParticleOptions(new Vector3f(
                ((def.color() >> 16) & 0xFF) / 255f,
                ((def.color() >> 8) & 0xFF) / 255f,
                (def.color() & 0xFF) / 255f), 1.1f);
        for (int index = 0; index < DomainRules.RING_PARTICLES; index++) {
            double angle = step * index + player.tickCount * 0.02;
            double x = player.getX() + Math.cos(angle) * radius;
            double z = player.getZ() + Math.sin(angle) * radius;
            level.sendParticles(dust, x, player.getY() + 0.1, z, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    private static void applyEffect(LivingEntity target, BuffProfile profile) {
        if (!profile.present()) return;
        MobEffect effect = CombatEffects.of(profile.effect());
        if (effect == null) return;
        target.addEffect(new MobEffectInstance(effect, EFFECT_DURATION_TICKS, profile.amplifier(), false, true));
    }

    private static String name(DomainDef def, String language) {
        return SoulLang.translate(language, "soul.soulland.domain." + def.soulId() + ".name", def.nameEn());
    }

    private static void deny(ServerPlayer player, String language, String key, String fallback, Object... args) {
        player.sendSystemMessage(SoulLang.text(language, key, fallback, args).withStyle(ChatFormatting.RED));
    }
}
