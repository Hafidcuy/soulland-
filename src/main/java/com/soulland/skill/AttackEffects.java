package com.soulland.skill;

import com.soulland.cultivation.CultivationCapability;
import com.soulland.cultivation.ICultivation;
import com.soulland.soul.SoulVisuals;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

/**
 * Efek kecil tiap soul saat pemain menyerang melee biasa.
 * Efek ini ringan, punya cooldown internal 3 detik per pemain, dan tidak menumpuk
 * dengan damage skill (damage skill ditandai flag sehingga hook ini melewatinya).
 */
public final class AttackEffects {
    private AttackEffects() {}

    /** Flag pada korban menandai damage yang berasal dari skill, bukan serangan melee biasa. */
    public static final String SKILL_DAMAGE_FLAG = "soulland_skill_damage";

    private static final String COOLDOWN_KEY = "soulland_attack_fx_cd";
    private static final long COOLDOWN_TICKS = 60L;

    /** Dipanggil dari hook LivingHurtEvent. */
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide) return;

        LivingEntity victim = event.getEntity();
        if (victim.getPersistentData().getBoolean(SKILL_DAMAGE_FLAG)) return;
        if (!event.getSource().is(DamageTypes.PLAYER_ATTACK)) return;
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;

        long now = player.level().getGameTime();
        if (player.getPersistentData().getLong(COOLDOWN_KEY) > now) return;

        String soulId = CultivationCapability.get(player)
                .map(ICultivation::getMartialSoulId)
                .orElse("");
        if (soulId.isEmpty()) return;

        apply(player, victim, soulId, event);
        player.getPersistentData().putLong(COOLDOWN_KEY, now + COOLDOWN_TICKS);
    }

    private static void apply(ServerPlayer player, LivingEntity victim, String soulId, LivingHurtEvent event) {
        ServerLevel level = player.serverLevel();
        switch (soulId) {
            case "hoe" -> {
                if (isUndead(victim)) {
                    event.setAmount(event.getAmount() + 2f);
                }
                spawnParticles(level, player, SoulVisuals.soil());
            }
            case "blue_silver_grass" -> {
                effect(victim, MobEffects.MOVEMENT_SLOWDOWN, 20, 0);
                spawnParticles(level, player, ParticleTypes.SPORE_BLOSSOM_AIR);
            }
            case "hell_civet" -> {
                effect(player, MobEffects.MOVEMENT_SPEED, 40, 1);
                spawnParticles(level, player, ParticleTypes.SMOKE);
            }
            case "sausage" -> {
                player.heal(1f);
                spawnParticles(level, player, ParticleTypes.HEART);
            }
            case "seven_treasure_pagoda" -> {
                ServerPlayer ally = nearestAlly(player);
                if (ally != null) ally.heal(1f);
                spawnParticles(level, player, ParticleTypes.END_ROD);
            }
            case "evil_fire_phoenix" -> {
                victim.setSecondsOnFire(2);
                spawnParticles(level, player, ParticleTypes.FLAME);
            }
            case "white_tiger" -> {
                Vec3 push = victim.position().subtract(player.position()).normalize().scale(0.4);
                victim.setDeltaMovement(victim.getDeltaMovement().add(push));
                victim.hurtMarked = true;
                spawnParticles(level, player, ParticleTypes.CLOUD);
            }
            case "soft_bone_rabbit" -> {
                effect(player, MobEffects.JUMP, 60, 1);
                spawnParticles(level, player, ParticleTypes.WHITE_ASH);
            }
            case "jade_serpent" -> {
                effect(victim, MobEffects.POISON, 40, 0);
                spawnParticles(level, player, ParticleTypes.SNEEZE);
            }
            case "clear_sky_hammer" -> {
                event.setAmount(event.getAmount() + 2f);
                spawnParticles(level, player, ParticleTypes.ELECTRIC_SPARK);
            }
            default -> {
            }
        }
    }

    private static boolean isUndead(LivingEntity victim) {
        return victim instanceof Zombie
                || victim instanceof Skeleton
                || victim instanceof WitherSkeleton
                || victim instanceof ZombieVillager;
    }

    private static ServerPlayer nearestAlly(ServerPlayer player) {
        ServerPlayer best = null;
        double bestDistance = 12.0;
        for (ServerPlayer other : player.serverLevel().players()) {
            if (other == player) continue;
            double distance = other.distanceTo(player);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = other;
            }
        }
        return best;
    }

    private static void effect(LivingEntity entity, net.minecraft.world.effect.MobEffect effect, int ticks, int amplifier) {
        entity.addEffect(new MobEffectInstance(effect, ticks, amplifier, false, true));
    }

    private static void spawnParticles(ServerLevel level, ServerPlayer player, net.minecraft.core.particles.ParticleOptions particle) {
        level.sendParticles(particle, player.getX(), player.getY() + 1, player.getZ(), 6, 0.4, 0.6, 0.4, 0.02);
    }
}
