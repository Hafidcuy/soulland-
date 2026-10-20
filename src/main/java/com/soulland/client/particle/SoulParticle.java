package com.soulland.client.particle;

import com.soulland.client.particle.SoulParticle.Kind;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;

/**
 * Partikel soul dengan gerakan sendiri.
 *
 * <p>Semuanya memakai satu texture 16x16 per jenis, jadi bentuknya tajam dan terbaca
 * partikel kecil.
 */
public class SoulParticle extends TextureSheetParticle {

    public enum Kind {
        FLAME(18, 0.10f, true),
        SPARK(14, 0.06f, true),
        SHADOW(30, 0.14f, false),
        RIPPLE(20, 0.16f, false),
        STAR(24, 0.10f, true),
        BLOOD(16, 0.07f, true),
        FROST(28, 0.08f, false),
        THUNDER(10, 0.12f, true),
        MIST(40, 0.18f, false),
        EMBERS(26, 0.09f, true),
        SHARDS(22, 0.11f, false);

        private final int lifetime;
        private final float size;
        private final boolean additive;

        Kind(int lifetime, float size, boolean additive) {
            this.lifetime = lifetime;
            this.size = size;
            this.additive = additive;
        }
    }

    private final Kind kind;
    private final double originX;
    private final double originY;
    private final double originZ;

    public SoulParticle(ClientLevel level, double x, double y, double z,
                        double xSpeed, double ySpeed, double zSpeed, Kind kind) {
        super(level, x, y, z, 0.0, 0.0, 0.0);
        this.kind = kind;
        this.originX = x;
        this.originY = y;
        this.originZ = z;
        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;
        this.lifetime = kind.lifetime + (int) (level.random.nextFloat() * 6.0);
        this.quadSize = kind.size;
        this.hasPhysics = kind == Kind.SPARK || kind == Kind.BLOOD || kind == Kind.EMBERS || kind == Kind.SHARDS;
    }

    @Override
    public void tick() {
        super.tick();
        switch (kind) {
            case FLAME -> {
                xd *= 0.92;
                zd *= 0.92;
                yd = 0.012 + (random.nextDouble() - 0.5) * 0.004;
                quadSize *= 0.97;
            }
            case SPARK -> {
                yd -= 0.012;
                xd *= 0.98;
                zd *= 0.98;
            }
            case SHADOW -> {
                xd *= 0.95;
                yd = 0.004;
                quadSize *= 0.985;
            }
            case RIPPLE -> {
                xd *= 0.86;
                zd *= 0.86;
                yd *= 0.5;
                quadSize *= 1.06;
            }
            case STAR -> {
                xd *= 0.90;
                yd *= 0.90;
                zd *= 0.90;
                quadSize = kind.size * (0.75f + 0.35f * (float) Math.sin(lifetime * 0.6));
            }
            case BLOOD -> {
                yd -= 0.02;
                quadSize *= 0.98;
            }
            case FROST -> {
                yd = 0.006;
                xd *= 0.97;
                zd *= 0.97;
            }
            case THUNDER -> {
                yd -= 0.05;
                quadSize *= 0.94;
            }
            case MIST -> {
                yd = 0.010;
                xd *= 0.97;
                zd *= 0.97;
                quadSize *= 1.01;
            }
            case EMBERS -> {
                yd = 0.016;
                xd *= 0.94;
                zd *= 0.94;
            }
            case SHARDS -> {
                yd -= 0.006;
                xd *= 0.96;
                zd *= 0.96;
            }
        }
    }

    /** Jarak partikel dari titik asal, dipakai untuk animasi riak. */
    public double distanceFromOrigin() {
        double dx = x - originX;
        double dy = y - originY;
        double dz = z - originZ;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    @Override
    public void pickSprite(SpriteSet set) {
        setSprite(set.get(0, 0));
    }

    @Override
    public void setSpriteFromAge(SpriteSet set) {
        setSprite(set.get(0, 0));
    }

    @Override
    public ParticleRenderType getRenderType() {
        return kind.additive ? ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT
                : ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }
}
