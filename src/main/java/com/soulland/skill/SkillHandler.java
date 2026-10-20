package com.soulland.skill;

import com.soulland.cultivation.CultivationCapability;
import com.soulland.cultivation.CultivationManager;
import com.soulland.entity.SoulCompanion;
import com.soulland.net.ModNetwork;
import com.soulland.net.VisualFxPacket;
import com.soulland.soul.MartialSoul;
import com.soulland.soul.MartialSouls;
import com.soulland.soul.SkillDef;
import com.soulland.soul.SkillTarget;
import com.soulland.soul.SkillVisual;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;

/**
 * Eksekusi skill martial soul.
 *
 * <p>Semua efek dihitung dari {@link SkillDef}, jadi menambah skill berarti menambah data,
 * bukan menambah cabang logika. Server yang memegang kendali penuh: client hanya
 * mengirim permintaan.
 */
public final class SkillHandler {
    private SkillHandler() {}

    public static void activate(ServerPlayer player, int slot) {
        CultivationCapability.get(player).ifPresent(cap -> {
            if (!cap.isAwakened()) {
                player.sendSystemMessage(Component.literal("Belum ada martial soul. Masuk kembali ke dunia untuk kebangkitan."));
                return;
            }

            MartialSoul soul = MartialSouls.effective(cap.getMartialSoulId(), cap.getEvolutionStage());
            if (soul == null) return;

            SkillDef skill = soul.skill(slot);
            if (skill == null) {
                player.sendSystemMessage(Component.literal("Slot skill " + (slot + 1) + " kosong untuk martial soul ini."));
                return;
            }

            if (cap.getRings() < skill.ringsRequired()) {
                player.sendSystemMessage(Component.literal("Skill \"" + skill.name() + "\" terkunci. Butuh "
                        + skill.ringsRequired() + " spirit ring, kamu punya " + cap.getRings() + "."));
                return;
            }

            long now = player.level().getGameTime();
            long until = cap.getCooldownUntil(skill.id());
            if (now < until) {
                long remain = (until - now + 19) / 20;
                player.sendSystemMessage(Component.literal("Skill \"" + skill.name() + "\" cooldown " + remain + " detik lagi."));
                return;
            }

            if (cap.getEnergy() < skill.energyCost()) {
                player.sendSystemMessage(Component.literal("Spirit Power tidak cukup untuk \"" + skill.name()
                        + "\" (butuh " + (int) skill.energyCost() + ", punya " + (int) cap.getEnergy() + ")."));
                return;
            }

            if (!execute(player, cap.getLevel(), cap.getRings(), skill)) {
                return;
            }

            cap.setEnergy(cap.getEnergy() - skill.energyCost());
            cap.setCooldown(skill.id(), now + skill.cooldownTicks());
            CultivationManager.sync(player);

            SkillFx.play(player.serverLevel(), player, skill);
            sendVisualFx(player, skill.visual());
            SoulCompanion.echo(player);

            player.sendSystemMessage(Component.literal("[" + skill.name() + "]"));
        });
    }

    private static void sendVisualFx(ServerPlayer player, SkillVisual visual) {
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new VisualFxPacket(visual.shake(), visual.flashRgb(), visual.flashAlpha(), 14));
    }

    /**
     * Ledakan spirit ring: energi dari seluruh ring yang diserap dilepas sekaligus.
     *
     * <p>Semakin banyak ring, semakin besar radius dan damage-nya. Ini cara pemain
     * memakai investasinya di masa lalu saat bertarung.
     */
    public static void activateRingBurst(ServerPlayer player) {
        CultivationCapability.get(player).ifPresent(cap -> {
            if (cap.getRings() <= 0) {
                player.sendSystemMessage(Component.literal("Belum ada spirit ring yang bisa dilepaskan."));
                return;
            }
            if (cap.getEnergy() < 25f) {
                player.sendSystemMessage(Component.literal("Spirit Power tidak cukup untuk ledakan spirit ring."));
                return;
            }

            long now = player.level().getGameTime();
            long until = cap.getCooldownUntil("ring_burst");
            if (now < until) {
                long remain = (until - now + 19) / 20;
                player.sendSystemMessage(Component.literal("Ledakan spirit ring cooldown " + remain + " detik lagi."));
                return;
            }

            int rings = cap.getRings();
            double scale = 1.0 + (cap.getLevel() - 1) * 0.02 + rings * 0.05;
            float damage = (float) (6.0 * rings * scale);
            double radius = 3.0 + rings * 0.6;

            ServerLevel world = player.serverLevel();
            for (LivingEntity entity : around(player, radius)) {
                entity.hurt(player.damageSources().playerAttack(player), damage);
                Vec3 push = entity.position().subtract(player.position()).normalize().scale(0.8);
                entity.setDeltaMovement(entity.getDeltaMovement().add(push.x, 0.4, push.z));
                entity.hurtMarked = true;
            }

            cap.setEnergy(cap.getEnergy() - 25f);
            cap.setCooldown("ring_burst", now + 300);
            CultivationManager.sync(player);

            SkillVisual visual = new SkillVisual(
                    com.soulland.soul.FxShape.RING, 0xE8C46A, 0xFFFFFF,
                    (float) radius, 120, null, 0.35f, 0x50E8C46A);
            SkillFx.play(world, player, visual);
            sendVisualFx(player, visual);
            SoulCompanion.echo(player);
            player.sendSystemMessage(Component.literal("Ledakan spirit ring: " + rings + " ring melepaskan energinya."));
        });
    }

    // ------------------------------------------------------------------ eksekusi

    private static boolean execute(ServerPlayer player, int level, int rings, SkillDef skill) {
        ServerLevel world = player.serverLevel();
        double scale = 1.0 + (level - 1) * 0.02 + rings * 0.05;
        float damage = (float) (skill.damage() * scale);
        float heal = (float) (skill.heal() * (1.0 + rings * 0.03));
        double radius = skill.radius();

        switch (skill.target()) {
            case SELF -> {
                if (heal > 0f) player.heal(heal);
                applyEffect(player, skill.selfEffect(), skill.selfEffectTicks(), skill.selfEffectAmplifier());
            }
            case ENEMY_NEAREST -> {
                LivingEntity target = nearest(player, radius);
                if (target == null) {
                    player.sendSystemMessage(Component.literal("Tidak ada musuh dalam jangkauan untuk \"" + skill.name() + "\"."));
                    return false;
                }
                hurt(world, player, target, damage, skill, radius);
            }
            case ENEMY_AOE -> {
                for (LivingEntity entity : around(player, radius)) {
                    hurt(world, player, entity, damage, skill, radius);
                }
            }
            case ENEMY_LINE -> {
                Vec3 look = player.getLookAngle();
                Vec3 end = player.position().add(look.scale(radius));
                AABB box = new AABB(
                        player.position().min(end).subtract(1.5, 1.5, 1.5),
                        player.position().max(end).add(1.5, 1.5, 1.5));
                for (LivingEntity entity : world.getEntitiesOfClass(LivingEntity.class, box,
                        e -> e != player && e.isAlive() && !(e instanceof ServerPlayer))) {
                    hurt(world, player, entity, damage, skill, radius);
                }
            }
            case ENEMY_CONE -> {
                Vec3 look = player.getLookAngle();
                for (LivingEntity entity : around(player, radius)) {
                    Vec3 to = entity.position().subtract(player.position()).normalize();
                    if (to.dot(look) > 0.55) {
                        hurt(world, player, entity, damage, skill, radius);
                    }
                }
            }
            case POINT_AOE -> {
                Vec3 look = player.getLookAngle();
                Vec3 center = player.position().add(look.scale(Math.min(radius, 6.0)));
                AABB box = new AABB(center.subtract(radius, radius, radius), center.add(radius, radius, radius));
                for (LivingEntity entity : world.getEntitiesOfClass(LivingEntity.class, box,
                        e -> e != player && e.isAlive() && !(e instanceof ServerPlayer))) {
                    hurt(world, player, entity, damage, skill, radius);
                }
            }
            case ALLY_AURA -> {
                for (ServerPlayer ally : allies(player, radius)) {
                    if (heal > 0f) ally.heal(heal);
                    applyEffect(ally, skill.effect(), skill.effectTicks(), skill.effectAmplifier());
                }
                if (heal > 0f) player.heal(heal);
                applyEffect(player, skill.effect(), skill.effectTicks(), skill.effectAmplifier());
            }
        }
        return true;
    }

    private static void hurt(ServerLevel world, ServerPlayer player, LivingEntity target, float damage,
                             SkillDef skill, double radius) {
        if (damage > 0f) {
            target.hurt(player.damageSources().playerAttack(player), damage);
        }
        applyEffect(target, skill.effect(), skill.effectTicks(), skill.effectAmplifier());

        boolean heavy = skill.visual().shape() == com.soulland.soul.FxShape.PLANE
                || skill.visual().shape() == com.soulland.soul.FxShape.COLUMN;
        if (heavy) {
            Vec3 push = target.position().subtract(player.position()).normalize().scale(0.9);
            target.setDeltaMovement(target.getDeltaMovement().add(push.x, 0.6, push.z));
            target.hurtMarked = true;
        }
    }

    private static void applyEffect(LivingEntity entity, MobEffect effect, int ticks, int amplifier) {
        if (effect == null || ticks <= 0) return;
        entity.addEffect(new MobEffectInstance(effect, ticks, amplifier, false, true));
    }

    private static java.util.List<LivingEntity> around(ServerPlayer player, double radius) {
        AABB box = new AABB(
                player.position().subtract(radius, radius, radius),
                player.position().add(radius, radius, radius));
        return player.serverLevel().getEntitiesOfClass(LivingEntity.class, box,
                e -> e != player && e.isAlive() && !(e instanceof ServerPlayer));
    }

    private static LivingEntity nearest(ServerPlayer player, double radius) {
        LivingEntity best = null;
        double bestDistance = Double.MAX_VALUE;
        for (LivingEntity entity : around(player, radius)) {
            double distance = entity.distanceToSqr(player);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = entity;
            }
        }
        return best;
    }

    private static java.util.List<ServerPlayer> allies(ServerPlayer player, double radius) {
        return player.serverLevel().players().stream()
                .filter(other -> other.distanceTo(player) <= radius)
                .toList();
    }
}
