package com.soulland.skill;

import com.soulland.cultivation.CultivationCapability;
import com.soulland.cultivation.CultivationManager;
import com.soulland.soul.MartialSoul;
import com.soulland.soul.MartialSouls;
import com.soulland.soul.SkillDef;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/** Eksekusi seluruh skill martial soul. Berjalan sepenuhnya di server (aman untuk multiplayer). */
public final class SkillHandler {
    private SkillHandler() {}

    public static void activate(ServerPlayer player, int slot) {
        CultivationCapability.get(player).ifPresent(cap -> {
            if (!cap.isAwakened()) return;
            MartialSoul soul = MartialSouls.get(cap.getMartialSoulId());
            if (soul == null) return;
            SkillDef skill = soul.skill(slot);
            if (skill == null) return;

            if (cap.getRings() < skill.ringsRequired()) {
                player.sendSystemMessage(Component.literal("§cSkill \"" + skill.name() + "\" terkunci. Butuh " + skill.ringsRequired() + " spirit ring."));
                return;
            }
            long now = player.level().getGameTime();
            long until = cap.getCooldownUntil(skill.id());
            if (now < until) {
                long remain = (until - now) / 20;
                player.sendSystemMessage(Component.literal("§cSkill masih cooldown: " + remain + " detik."));
                return;
            }
            if (cap.getEnergy() < skill.energyCost()) {
                player.sendSystemMessage(Component.literal("§cSpirit Power tidak cukup (butuh " + (int) skill.energyCost() + ")."));
                return;
            }

            if (!execute(player, skill.id())) return;

            cap.setEnergy(cap.getEnergy() - skill.energyCost());
            cap.setCooldown(skill.id(), now + skill.cooldownTicks());
            CultivationManager.sync(player);
            player.sendSystemMessage(Component.literal("§b✦ " + skill.name() + " ✦"));
        });
    }

    private static boolean execute(ServerPlayer p, String id) {
        ServerLevel level = p.serverLevel();
        double scale = 1.0 + p.getCapability(com.soulland.cultivation.CultivationCapability.CAP)
                .map(c -> (c.getLevel() - 1) * 0.02).orElse(0.0);
        List<LivingEntity> around6 = entitiesAround(p, 6);
        Vec3 look = p.getLookAngle();

        switch (id) {
            // Cangkul
            case "hoe_heal" -> { p.heal(6f); effect(p, MobEffects.SATURATION, 60, 0); particles(level, p, ParticleTypes.HAPPY_VILLAGER); }
            case "hoe_slam" -> { aoeDamage(p, around6, 5 * scale, 0.6); sound(level, p, SoundEvents.PLAYER_ATTACK_SWEEP); }
            // Blue Silver Grass
            case "bsg_entangle" -> {
                for (LivingEntity e : entitiesAround(p, 8)) {
                    effect(e, MobEffects.MOVEMENT_SLOWDOWN, 100, 6);
                    effect(e, MobEffects.WEAKNESS, 100, 1);
                }
                particles(level, p, ParticleTypes.SPORE_BLOSSOM_AIR);
            }
            case "bsg_parasite" -> {
                LivingEntity t = nearest(p, 10);
                if (t == null) { p.sendSystemMessage(Component.literal("§cTidak ada musuh di jangkauan.")); return false; }
                effect(t, MobEffects.POISON, 160, 1);
                p.heal(4f);
            }
            // Hell Civet
            case "civet_dash" -> {
                p.setDeltaMovement(look.scale(1.8).add(0, 0.25, 0));
                p.hurtMarked = true;
                LivingEntity t = nearest(p, 5);
                if (t != null) t.hurt(p.damageSources().playerAttack(p), (float) (9 * scale));
                sound(level, p, SoundEvents.PLAYER_ATTACK_SWEEP);
            }
            case "civet_slash" -> { aoeDamage(p, entitiesAround(p, 4), 10 * scale, 0.3); effect(p, MobEffects.MOVEMENT_SPEED, 200, 1); }
            // Sosis Oscar
            case "sausage_heal" -> { p.heal(8f); effect(p, MobEffects.SATURATION, 100, 1); particles(level, p, ParticleTypes.HEART); }
            case "sausage_fly" -> { effect(p, MobEffects.MOVEMENT_SPEED, 400, 1); effect(p, MobEffects.SLOW_FALLING, 400, 0); effect(p, MobEffects.JUMP, 400, 1); p.resetFallDistance(); }
            // Pagoda Ning Rongrong
            case "pagoda_power" -> {
                for (ServerPlayer ally : allies(p, 12)) {
                    effect(ally, MobEffects.DAMAGE_BOOST, 600, 1);
                    effect(ally, MobEffects.ABSORPTION, 600, 1);
                }
                particles(level, p, ParticleTypes.END_ROD);
            }
            case "pagoda_heal" -> {
                for (ServerPlayer ally : allies(p, 12)) {
                    ally.heal(10f);
                    effect(ally, MobEffects.REGENERATION, 200, 0);
                }
                particles(level, p, ParticleTypes.TOTEM_OF_UNDYING);
            }
            // Phoenix Ma Hongjun
            case "phoenix_line" -> {
                for (LivingEntity e : entitiesAround(p, 7)) {
                    Vec3 to = e.position().subtract(p.position()).normalize();
                    if (to.dot(look) > 0.5) {
                        e.hurt(p.damageSources().playerAttack(p), (float) (8 * scale));
                        e.setSecondsOnFire(5);
                    }
                }
                particles(level, p, ParticleTypes.FLAME);
                sound(level, p, SoundEvents.BLAZE_SHOOT);
            }
            case "phoenix_meteor" -> {
                Vec3 center = p.position().add(look.scale(8));
                for (int i = 0; i < 5; i++) {
                    SmallFireball fb = new SmallFireball(level, p,
                            (level.random.nextDouble() - 0.5) * 0.4, -2.2, (level.random.nextDouble() - 0.5) * 0.4);
                    fb.setPos(center.x + (level.random.nextDouble() - 0.5) * 6, center.y + 12, center.z + (level.random.nextDouble() - 0.5) * 6);
                    level.addFreshEntity(fb);
                }
            }
            // White Tiger Dai Mubai
            case "tiger_wave" -> {
                for (LivingEntity e : entitiesAround(p, 10)) {
                    Vec3 to = e.position().subtract(p.position());
                    if (to.normalize().dot(look) > 0.7) {
                        e.hurt(p.damageSources().playerAttack(p), (float) (12 * scale));
                        e.setDeltaMovement(look.scale(1.2).add(0, 0.3, 0));
                        e.hurtMarked = true;
                    }
                }
                sound(level, p, SoundEvents.GENERIC_EXPLODE);
            }
            case "tiger_vajra" -> { effect(p, MobEffects.DAMAGE_BOOST, 600, 1); effect(p, MobEffects.DAMAGE_RESISTANCE, 600, 1); particles(level, p, ParticleTypes.GLOW); }
            // Kelinci Xiao Wu
            case "rabbit_charm" -> {
                for (LivingEntity e : entitiesAround(p, 8)) {
                    effect(e, MobEffects.CONFUSION, 160, 0);
                    effect(e, MobEffects.WEAKNESS, 160, 1);
                    if (e instanceof net.minecraft.world.entity.Mob mob) mob.setTarget(null);
                }
                particles(level, p, ParticleTypes.HEART);
            }
            case "rabbit_blink" -> {
                Vec3 dest = p.position().add(look.scale(8));
                p.teleportTo(dest.x, dest.y, dest.z);
                p.resetFallDistance();
                particles(level, p, ParticleTypes.PORTAL);
            }
            // Ular Dugu Yan
            case "serpent_mist" -> {
                for (LivingEntity e : around6) {
                    effect(e, MobEffects.POISON, 200, 1);
                    effect(e, MobEffects.MOVEMENT_SLOWDOWN, 200, 1);
                }
                particles(level, p, ParticleTypes.SNEEZE);
            }
            case "serpent_bite" -> {
                LivingEntity t = nearest(p, 4.5);
                if (t == null) { p.sendSystemMessage(Component.literal("§cTidak ada musuh cukup dekat.")); return false; }
                t.hurt(p.damageSources().playerAttack(p), (float) (14 * scale));
                effect(t, MobEffects.POISON, 160, 2);
            }
            // Clear Sky Hammer
            case "hammer_slam" -> {
                for (LivingEntity e : entitiesAround(p, 7)) {
                    e.hurt(p.damageSources().playerAttack(p), (float) (16 * scale));
                    e.setDeltaMovement(e.position().subtract(p.position()).normalize().scale(1.0).add(0, 0.9, 0));
                    e.hurtMarked = true;
                }
                particles(level, p, ParticleTypes.EXPLOSION);
                sound(level, p, SoundEvents.GENERIC_EXPLODE);
            }
            case "hammer_cloak" -> { effect(p, MobEffects.DAMAGE_BOOST, 400, 2); effect(p, MobEffects.MOVEMENT_SPEED, 400, 1); particles(level, p, ParticleTypes.CRIT); }
            default -> { return false; }
        }
        return true;
    }

    private static List<LivingEntity> entitiesAround(ServerPlayer p, double r) {
        return p.level().getEntitiesOfClass(LivingEntity.class,
                new AABB(p.position().subtract(r, r, r), p.position().add(r, r, r)),
                e -> e != p && e.isAlive() && !(e instanceof ServerPlayer));
    }

    private static LivingEntity nearest(ServerPlayer p, double r) {
        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (LivingEntity e : entitiesAround(p, r)) {
            double d = e.distanceToSqr(p);
            if (d < bestDist) { bestDist = d; best = e; }
        }
        return best;
    }

    private static List<ServerPlayer> allies(ServerPlayer p, double r) {
        return p.serverLevel().players().stream()
                .filter(o -> o.distanceTo(p) <= r)
                .toList();
    }

    private static void aoeDamage(ServerPlayer p, List<LivingEntity> targets, double dmg, double knockUp) {
        for (LivingEntity e : targets) {
            e.hurt(p.damageSources().playerAttack(p), (float) dmg);
            if (knockUp > 0) {
                e.setDeltaMovement(e.getDeltaMovement().add(0, knockUp, 0));
                e.hurtMarked = true;
            }
        }
    }

    private static void effect(LivingEntity e, net.minecraft.world.effect.MobEffect effect, int ticks, int amp) {
        e.addEffect(new MobEffectInstance(effect, ticks, amp, false, true));
    }

    private static void particles(ServerLevel level, ServerPlayer p, net.minecraft.core.particles.ParticleOptions particle) {
        level.sendParticles(particle, p.getX(), p.getY() + 1, p.getZ(), 24, 1.2, 1.2, 1.2, 0.05);
    }

    private static void sound(ServerLevel level, ServerPlayer p, net.minecraft.sounds.SoundEvent sound) {
        level.playSound(null, p.blockPosition(), sound, SoundSource.PLAYERS, 1f, 1f);
    }
}
