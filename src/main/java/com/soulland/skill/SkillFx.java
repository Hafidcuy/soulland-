package com.soulland.skill;

import com.soulland.entity.SoulVfxEntity;
import com.soulland.soul.FxShape;
import com.soulland.soul.SkillDef;
import com.soulland.soul.SkillVisual;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import org.joml.Vector3f;

/**
 * Penggambar efek visual skill.
 *
 * <p>Enam arketipe bentuk (SPHERE, LINE, CONE, RING, PLANE, COLUMN, BURST) plus satu
 * {@link SoulVfxEntity} untuk bentuk energi yang besar. Warna dan ukuran datang dari
 * {@link SkillVisual}, jadi dua skill tidak pernah terlihat sama walau bentuknya sama.
 */
public final class SkillFx {
    private SkillFx() {}

    private static final RandomSource RANDOM = RandomSource.create();

    public static void play(ServerLevel level, ServerPlayer player, SkillDef skill) {
        SkillVisual visual = skill.visual();
        Vec3 origin = player.position().add(0.0, 1.0, 0.0);
        Vec3 look = player.getLookAngle();

        switch (visual.shape()) {
            case SPHERE -> sphere(level, origin, visual);
            case LINE -> line(level, origin, look, visual);
            case CONE -> cone(level, origin, look, visual);
            case RING -> ring(level, origin, visual);
            case PLANE -> plane(level, origin, visual);
            case COLUMN -> column(level, origin, visual);
            case BURST -> burst(level, origin, look, visual);
        }

        if (visual.size() >= 5f) {
            spawnStructure(level, player, visual, origin, look);
        }

        playSound(level, player, visual);
    }

    // ------------------------------------------------------------------ bentuk

    private static void sphere(ServerLevel level, Vec3 origin, SkillVisual v) {
        int perShell = Math.max(3, v.count() / 6);
        for (int shell = 1; shell <= 3; shell++) {
            float radius = v.size() * shell / 3f;
            for (int i = 0; i < perShell; i++) {
                double u = RANDOM.nextDouble() * Math.PI * 2;
                double v2 = Math.acos(2 * RANDOM.nextDouble() - 1);
                double x = Math.sin(v2) * Math.cos(u) * radius;
                double y = Math.cos(v2) * radius * 0.7;
                double z = Math.sin(v2) * Math.sin(u) * radius;
                dust(level, origin.x + x, origin.y + y, origin.z + z, v.color(), 1, 0.02);
            }
        }
        secondary(level, ParticleTypes.END_ROD, origin, v, 0.6, 0.05);
    }

    private static void line(ServerLevel level, Vec3 origin, Vec3 look, SkillVisual v) {
        Vec3 end = origin.add(look.scale(v.size()));
        int steps = Math.max(6, v.count() / 3);
        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps;
            Vec3 p = origin.lerp(end, t);
            dust(level, p.x, p.y, p.z, v.color(), 2, 0.25);
        }
        secondary(level, ParticleTypes.ELECTRIC_SPARK, origin.add(look.scale(v.size() * 0.5)),
                v, v.size() * 0.4, 0.02);
    }

    private static void cone(ServerLevel level, Vec3 origin, Vec3 look, SkillVisual v) {
        int steps = Math.max(5, v.count() / 4);
        for (int i = 0; i < steps; i++) {
            double t = (double) i / steps;
            double dist = t * v.size();
            double angle = RANDOM.nextDouble() * Math.PI * 2;
            double spread = Math.tan(Math.toRadians(35)) * dist;
            Vec3 p = origin.add(look.scale(dist))
                    .add(Math.cos(angle) * spread * RANDOM.nextDouble(), 0.0, Math.sin(angle) * spread * RANDOM.nextDouble());
            dust(level, p.x, p.y, p.z, v.color(), 3, 0.2);
        }
        secondary(level, ParticleTypes.FLAME, origin.add(look.scale(v.size() * 0.6)), v, v.size() * 0.3, 0.03);
    }

    private static void ring(ServerLevel level, Vec3 origin, SkillVisual v) {
        int points = Math.max(16, v.count() * 2);
        for (int i = 0; i < points; i++) {
            double angle = Math.PI * 2 * i / points;
            double jitter = (RANDOM.nextDouble() - 0.5) * 0.4;
            double x = origin.x + Math.cos(angle) * (v.size() + jitter);
            double z = origin.z + Math.sin(angle) * (v.size() + jitter);
            dust(level, x, origin.y, z, v.color(), 1, 0.05);
        }
        secondary(level, ParticleTypes.END_ROD, origin, v, v.size() * 0.7, 0.02);
    }

    private static void plane(ServerLevel level, Vec3 origin, SkillVisual v) {
        int points = Math.max(12, v.count() * 2);
        for (int i = 0; i < points; i++) {
            double angle = RANDOM.nextDouble() * Math.PI * 2;
            double radius = Math.sqrt(RANDOM.nextDouble()) * v.size();
            double x = origin.x + Math.cos(angle) * radius;
            double z = origin.z + Math.sin(angle) * radius;
            dust(level, x, origin.y, z, v.color(), 1, 0.08);
        }
        level.sendParticles(ParticleTypes.EXPLOSION, origin.x, origin.y, origin.z, 1, 0.1, 0.1, 0.1, 0.0);
    }

    private static void column(ServerLevel level, Vec3 origin, SkillVisual v) {
        int rows = Math.max(6, v.count() / 3);
        for (int i = 0; i < rows; i++) {
            double t = (double) i / rows;
            double y = origin.y + t * v.size();
            double radius = v.size() * 0.3 * (1.0 - t * 0.6);
            for (int j = 0; j < 6; j++) {
                double angle = Math.PI * 2 * j / 6 + t * 2;
                dust(level, origin.x + Math.cos(angle) * radius, y, origin.z + Math.sin(angle) * radius,
                        v.color(), 2, 0.06);
            }
        }
        secondary(level, ParticleTypes.ELECTRIC_SPARK, origin, v, v.size() * 0.35, 0.05);
    }

    private static void burst(ServerLevel level, Vec3 origin, Vec3 look, SkillVisual v) {
        int points = Math.max(14, v.count());
        for (int i = 0; i < points; i++) {
            double u = RANDOM.nextDouble() * Math.PI * 2;
            double w = Math.acos(2 * RANDOM.nextDouble() - 1);
            double speed = v.size() * 0.35;
            double x = Math.sin(w) * Math.cos(u) * speed;
            double y = Math.abs(Math.cos(w)) * speed * 0.8;
            double z = Math.sin(w) * Math.sin(u) * speed;
            level.sendParticles(new DustParticleOptions(rgb(v.color()), 1.4f),
                    origin.x + x * 0.2, origin.y + y * 0.2, origin.z + z * 0.2, 1,
                    x * 0.35, y * 0.35, z * 0.35, 0.0);
        }
        level.sendParticles(ParticleTypes.SCULK_SOUL, origin.x, origin.y, origin.z, 12, 0.4, 0.4, 0.4, 0.02);
    }

    // ------------------------------------------------------------------ struktur energi

    private static void spawnStructure(ServerLevel level, ServerPlayer player, SkillVisual v, Vec3 origin, Vec3 look) {
        SoulVfxEntity entity = new SoulVfxEntity(level, modeFor(v.shape()));
        entity.setPos(origin.x, origin.y, origin.z);
        entity.setColors(v.color(), v.color2());
        entity.setLife(24);
        entity.setSize(v.size());
        entity.setDirection((float) look.x, (float) look.y, (float) look.z);
        level.addFreshEntity(entity);
    }

    private static SoulVfxEntity.Mode modeFor(FxShape shape) {
        return switch (shape) {
            case LINE -> SoulVfxEntity.Mode.BEAM;
            case COLUMN -> SoulVfxEntity.Mode.COLUMN;
            case BURST, CONE -> SoulVfxEntity.Mode.ORB;
            default -> SoulVfxEntity.Mode.WAVE;
        };
    }

    // ------------------------------------------------------------------ util

    private static void dust(ServerLevel level, double x, double y, double z, int color, int count, double spread) {
        level.sendParticles(new DustParticleOptions(rgb(color), 1.1f), x, y, z, count,
                spread, spread, spread, 0.0);
    }

    private static void secondary(ServerLevel level, ParticleOptions type, Vec3 at, SkillVisual v,
                                  double radius, double speed) {
        level.sendParticles(type, at.x, at.y, at.z, Math.max(4, v.count() / 3),
                radius, radius, radius, speed);
    }

    public static Vector3f rgb(int color) {
        return new Vector3f(
                Mth.clamp(((color >> 16) & 0xFF) / 255f, 0f, 1f),
                Mth.clamp(((color >> 8) & 0xFF) / 255f, 0f, 1f),
                Mth.clamp((color & 0xFF) / 255f, 0f, 1f));
    }

    private static void playSound(ServerLevel level, ServerPlayer player, SkillVisual v) {
        if (v.sound() == null || v.sound().isEmpty()) return;
        SoundEvent sound = ForgeRegistries.SOUND_EVENTS.getValue(
                new net.minecraft.resources.ResourceLocation(v.sound()));
        if (sound == null) return;
        level.playSound(null, player.blockPosition(), sound, SoundSource.PLAYERS, 0.8f, 1.0f);
    }
}
