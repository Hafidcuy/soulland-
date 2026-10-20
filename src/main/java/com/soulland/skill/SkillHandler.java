package com.soulland.skill;

import com.soulland.cultivation.CultivationCapability;
import com.soulland.cultivation.CultivationManager;
import com.soulland.net.ManifestationPacket;
import com.soulland.net.ModNetwork;
import com.soulland.soul.MartialSoul;
import com.soulland.soul.MartialSouls;
import com.soulland.soul.SkillDef;
import com.soulland.soul.SoulText;
import com.soulland.soul.SoulVisuals;
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
import net.minecraftforge.network.PacketDistributor;

import java.util.List;

/** Eksekusi seluruh skill martial soul. Berjalan sepenuhnya di server (aman untuk multiplayer). */
public final class SkillHandler {
    private SkillHandler() {}

    /** Jangkauan sinkronisasi animasi manifestasi ke pemain sekitar (blok). */
    private static final double MANIFESTATION_VIEW_RANGE = 48.0;

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
            player.sendSystemMessage(Component.literal("§b✦ ").append(SoulText.skillName(skill)).append(Component.literal(" ✦")));
            manifest(player, soul);
        });
    }

    /** Memicu manifestasi visual: packet ke pemain sekitar + partikel + suara khas soul. */
    private static void manifest(ServerPlayer player, MartialSoul soul) {
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new ManifestationPacket(player.getId(), soul.id()));
        for (ServerPlayer other : player.serverLevel().players()) {
            if (other != player && other.distanceTo(player) <= MANIFESTATION_VIEW_RANGE) {
                ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> other),
                        new ManifestationPacket(player.getId(), soul.id()));
            }
        }

        ServerLevel level = player.serverLevel();
        Vec3 look = player.getLookAngle();
        double mx = player.getX() - look.x * 1.2;
        double my = player.getY() + 1.9;
        double mz = player.getZ() - look.z * 1.2;
        level.sendParticles(SoulVisuals.particle(soul.id()), mx, my, mz, 40, 0.7, 0.9, 0.7, 0.05);
        level.playSound(null, player.blockPosition(), SoulVisuals.sound(soul.id()), SoundSource.PLAYERS, 0.9f, 1f);
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
            case "hoe_harvest" -> {
                aoeDamage(p, entitiesAround(p, 5), 6 * scale, 0.3);
                p.heal(3f);
                particles(level, p, SoulVisuals.soil());
                sound(level, p, SoundEvents.GRAVEL_HIT);
            }
            case "hoe_season" -> {
                aoeDamage(p, entitiesAround(p, 8), 9 * scale, 0.4);
                for (ServerPlayer ally : allies(p, 12)) {
                    ally.heal(8f);
                    effect(ally, MobEffects.ABSORPTION, 600, 1);
                }
                particles(level, p, ParticleTypes.SPORE_BLOSSOM_AIR);
                sound(level, p, SoundEvents.AMETHYST_BLOCK_CHIME);
            }
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
            case "bsg_nest" -> {
                List<LivingEntity> targets = entitiesAround(p, 8).stream().limit(3).toList();
                for (LivingEntity e : targets) {
                    effect(e, MobEffects.MOVEMENT_SLOWDOWN, 200, 6);
                    effect(e, MobEffects.WEAKNESS, 200, 2);
                }
                particles(level, p, ParticleTypes.SPORE_BLOSSOM_AIR);
                sound(level, p, SoundEvents.AMETHYST_BLOCK_CHIME);
            }
            case "bsg_rain" -> {
                for (LivingEntity e : entitiesAround(p, 8)) {
                    effect(e, MobEffects.POISON, 160, 1);
                    effect(e, MobEffects.MOVEMENT_SLOWDOWN, 160, 1);
                }
                particles(level, p, ParticleTypes.SPORE_BLOSSOM_AIR);
                sound(level, p, SoundEvents.AMETHYST_BLOCK_CHIME);
            }
            // Hell Civet
            case "civet_dash" -> {
                p.setDeltaMovement(look.scale(1.8).add(0, 0.25, 0));
                p.hurtMarked = true;
                LivingEntity t = nearest(p, 5);
                if (t != null) skillDamage(p, t, (float) (9 * scale));
                sound(level, p, SoundEvents.PLAYER_ATTACK_SWEEP);
            }
            case "civet_slash" -> { aoeDamage(p, entitiesAround(p, 4), 10 * scale, 0.3); effect(p, MobEffects.MOVEMENT_SPEED, 200, 1); }
            case "civet_shadow" -> {
                p.setDeltaMovement(look.scale(2.2).add(0, 0.3, 0));
                p.hurtMarked = true;
                effect(p, MobEffects.MOVEMENT_SPEED, 100, 1);
                particles(level, p, ParticleTypes.SMOKE);
                sound(level, p, SoundEvents.ENDERMAN_TELEPORT);
            }
            case "civet_storm" -> {
                p.setDeltaMovement(look.scale(2.8).add(0, 0.35, 0));
                p.hurtMarked = true;
                for (LivingEntity e : entitiesAround(p, 4)) skillDamage(p, e, (float) (11 * scale));
                effect(p, MobEffects.MOVEMENT_SPEED, 160, 2);
                particles(level, p, ParticleTypes.SMOKE);
                sound(level, p, SoundEvents.ENDERMAN_TELEPORT);
            }
            // Sosis Oscar
            case "sausage_heal" -> { p.heal(8f); effect(p, MobEffects.SATURATION, 100, 1); particles(level, p, ParticleTypes.HEART); }
            case "sausage_fly" -> { effect(p, MobEffects.MOVEMENT_SPEED, 400, 1); effect(p, MobEffects.SLOW_FALLING, 400, 0); effect(p, MobEffects.JUMP, 400, 1); p.resetFallDistance(); }
            case "sausage_soup" -> {
                for (ServerPlayer ally : allies(p, 12)) {
                    ally.heal(8f);
                    effect(ally, MobEffects.REGENERATION, 200, 0);
                }
                particles(level, p, ParticleTypes.HEART);
                sound(level, p, SoundEvents.BREWING_STAND_BREW);
            }
            case "sausage_banquet" -> {
                p.heal(15f);
                effect(p, MobEffects.DAMAGE_RESISTANCE, 400, 1);
                for (ServerPlayer ally : allies(p, 12)) ally.heal(8f);
                particles(level, p, ParticleTypes.HEART);
                sound(level, p, SoundEvents.BREWING_STAND_BREW);
            }
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
            case "pagoda_aegis" -> {
                for (ServerPlayer ally : allies(p, 12)) {
                    effect(ally, MobEffects.DAMAGE_RESISTANCE, 600, 1);
                    effect(ally, MobEffects.MOVEMENT_SPEED, 600, 0);
                }
                particles(level, p, ParticleTypes.END_ROD);
                sound(level, p, SoundEvents.BEACON_ACTIVATE);
            }
            case "pagoda_blood" -> {
                for (ServerPlayer ally : allies(p, 12)) {
                    ally.heal(12f);
                    effect(ally, MobEffects.DAMAGE_BOOST, 600, 2);
                }
                particles(level, p, ParticleTypes.TOTEM_OF_UNDYING);
                sound(level, p, SoundEvents.BEACON_ACTIVATE);
            }
            // Phoenix Ma Hongjun
            case "phoenix_line" -> {
                for (LivingEntity e : entitiesAround(p, 7)) {
                    Vec3 to = e.position().subtract(p.position()).normalize();
                    if (to.dot(look) > 0.5) {
                        skillDamage(p, e, (float) (8 * scale));
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
            case "phoenix_wings" -> {
                for (LivingEntity e : entitiesAround(p, 6)) {
                    skillDamage(p, e, (float) (8 * scale));
                    e.setSecondsOnFire(4);
                }
                particles(level, p, ParticleTypes.FLAME);
                sound(level, p, SoundEvents.BLAZE_BURN);
            }
            case "phoenix_ruin" -> {
                Vec3 center = p.position().add(look.scale(7));
                for (int i = 0; i < 6; i++) {
                    SmallFireball fb = new SmallFireball(level, p,
                            (level.random.nextDouble() - 0.5) * 0.5, -2.0, (level.random.nextDouble() - 0.5) * 0.5);
                    fb.setPos(center.x + (level.random.nextDouble() - 0.5) * 6, center.y + 10, center.z + (level.random.nextDouble() - 0.5) * 6);
                    level.addFreshEntity(fb);
                }
                for (LivingEntity e : entitiesAround(p, 10)) {
                    skillDamage(p, e, (float) (12 * scale));
                    e.setSecondsOnFire(5);
                }
                particles(level, p, ParticleTypes.EXPLOSION);
                sound(level, p, SoundEvents.GENERIC_EXPLODE);
            }
            // White Tiger Dai Mubai
            case "tiger_wave" -> {
                for (LivingEntity e : entitiesAround(p, 10)) {
                    Vec3 to = e.position().subtract(p.position());
                    if (to.normalize().dot(look) > 0.7) {
                        skillDamage(p, e, (float) (12 * scale));
                        e.setDeltaMovement(look.scale(1.2).add(0, 0.3, 0));
                        e.hurtMarked = true;
                    }
                }
                sound(level, p, SoundEvents.GENERIC_EXPLODE);
            }
            case "tiger_vajra" -> { effect(p, MobEffects.DAMAGE_BOOST, 600, 1); effect(p, MobEffects.DAMAGE_RESISTANCE, 600, 1); particles(level, p, ParticleTypes.GLOW); }
            case "tiger_claw" -> {
                LivingEntity t = nearest(p, 6);
                if (t == null) { p.sendSystemMessage(Component.literal("§cTidak ada musuh cukup dekat.")); return false; }
                skillDamage(p, t, (float) (26 * scale));
                particles(level, p, ParticleTypes.CRIT);
                sound(level, p, SoundEvents.ENDER_DRAGON_GROWL);
            }
            case "tiger_roar" -> {
                for (LivingEntity e : entitiesAround(p, 8)) {
                    skillDamage(p, e, (float) (14 * scale));
                    e.setDeltaMovement(e.position().subtract(p.position()).normalize().scale(1.2).add(0, 0.4, 0));
                    e.hurtMarked = true;
                }
                particles(level, p, ParticleTypes.CLOUD);
                sound(level, p, SoundEvents.ENDER_DRAGON_GROWL);
            }
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
            case "rabbit_hop" -> {
                p.setDeltaMovement(p.getDeltaMovement().add(0, 1.2, 0));
                p.hurtMarked = true;
                p.resetFallDistance();
                effect(p, MobEffects.MOVEMENT_SPEED, 120, 1);
                particles(level, p, ParticleTypes.WHITE_ASH);
                sound(level, p, SoundEvents.FIREWORK_ROCKET_LAUNCH);
            }
            case "rabbit_sprint" -> {
                Vec3 dest = p.position().add(look.scale(12));
                p.teleportTo(dest.x, dest.y, dest.z);
                p.resetFallDistance();
                for (LivingEntity e : entitiesAround(p, 6)) effect(e, MobEffects.CONFUSION, 200, 0);
                particles(level, p, ParticleTypes.PORTAL);
                sound(level, p, SoundEvents.FIREWORK_ROCKET_LAUNCH);
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
                skillDamage(p, t, (float) (14 * scale));
                effect(t, MobEffects.POISON, 160, 2);
            }
            case "serpent_siphon" -> {
                LivingEntity t = nearest(p, 8);
                if (t == null) { p.sendSystemMessage(Component.literal("§cTidak ada musuh di jangkauan.")); return false; }
                float drained = Math.min(6f, t.getHealth());
                skillDamage(p, t, drained);
                p.heal(drained);
                effect(t, MobEffects.POISON, 120, 1);
                particles(level, p, ParticleTypes.SNEEZE);
            }
            case "serpent_death" -> {
                for (LivingEntity e : entitiesAround(p, 10)) {
                    effect(e, MobEffects.POISON, 240, 2);
                    effect(e, MobEffects.WITHER, 120, 0);
                }
                particles(level, p, ParticleTypes.SNEEZE);
                sound(level, p, SoundEvents.WITCH_DRINK);
            }
            // Clear Sky Hammer
            case "hammer_slam" -> {
                for (LivingEntity e : entitiesAround(p, 7)) {
                    skillDamage(p, e, (float) (16 * scale));
                    e.setDeltaMovement(e.position().subtract(p.position()).normalize().scale(1.0).add(0, 0.9, 0));
                    e.hurtMarked = true;
                }
                particles(level, p, ParticleTypes.EXPLOSION);
                sound(level, p, SoundEvents.GENERIC_EXPLODE);
            }
            case "hammer_cloak" -> { effect(p, MobEffects.DAMAGE_BOOST, 400, 2); effect(p, MobEffects.MOVEMENT_SPEED, 400, 1); particles(level, p, ParticleTypes.CRIT); }
            case "hammer_thunder" -> {
                LivingEntity t = nearest(p, 8);
                if (t == null) { p.sendSystemMessage(Component.literal("§cTidak ada musuh cukup dekat.")); return false; }
                skillDamage(p, t, (float) (34 * scale));
                particles(level, p, ParticleTypes.ELECTRIC_SPARK);
                sound(level, p, SoundEvents.LIGHTNING_BOLT_THUNDER);
            }
            case "hammer_clear_sky" -> {
                for (LivingEntity e : entitiesAround(p, 9)) {
                    skillDamage(p, e, (float) (30 * scale));
                    e.setDeltaMovement(e.position().subtract(p.position()).normalize().scale(1.4).add(0, 1.0, 0));
                    e.hurtMarked = true;
                }
                particles(level, p, ParticleTypes.EXPLOSION);
                sound(level, p, SoundEvents.LIGHTNING_BOLT_THUNDER);
            }
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
            skillDamage(p, e, (float) dmg);
            if (knockUp > 0) {
                e.setDeltaMovement(e.getDeltaMovement().add(0, knockUp, 0));
                e.hurtMarked = true;
            }
        }
    }

    /**
     * Damage dari skill. Flag sementara pada korban memastikan efek serangan biasa
     * (F4) tidak ikut terpicu oleh damage skill.
     */
    private static void skillDamage(ServerPlayer source, LivingEntity target, float amount) {
        target.getPersistentData().putBoolean(AttackEffects.SKILL_DAMAGE_FLAG, true);
        try {
            target.hurt(source.damageSources().playerAttack(source), amount);
        } finally {
            target.getPersistentData().remove(AttackEffects.SKILL_DAMAGE_FLAG);
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
