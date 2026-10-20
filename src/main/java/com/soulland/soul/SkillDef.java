package com.soulland.soul;

import net.minecraft.world.effect.MobEffect;

/**
 * Definisi satu skill martial soul.
 *
 * <p>Data-driven: {@link com.soulland.skill.SkillHandler} membaca field di sini, bukan
 * menebak dari id. Menambah skill berarti menambah satu blok builder, bukan satu switch case.
 */
public record SkillDef(
        String id,
        String name,
        String description,
        float energyCost,
        int cooldownTicks,
        int ringsRequired,
        SkillTarget target,
        SkillVisual visual,
        float damage,
        float heal,
        double radius,
        MobEffect effect,
        int effectTicks,
        int effectAmplifier,
        MobEffect selfEffect,
        int selfEffectTicks,
        int selfEffectAmplifier
) {
    public boolean targetsEnemies() {
        return target != SkillTarget.SELF && target != SkillTarget.ALLY_AURA;
    }

    public static Builder builder(String id, String name) {
        return new Builder(id, name);
    }

    /** Pembuat skill yang ringkas, supaya tabel 60 skill tetap terbaca. */
    public static final class Builder {
        private final String id;
        private final String name;
        private String description = "";
        private float energyCost = 20f;
        private int cooldownTicks = 240;
        private int ringsRequired = 1;
        private SkillTarget target = SkillTarget.ENEMY_NEAREST;
        private SkillVisual visual = new SkillVisual(FxShape.SPHERE, 0xFFFFFF, 0, 4f, 24, null, 0f, 0);
        private float damage = 0f;
        private float heal = 0f;
        private double radius = 6.0;
        private MobEffect effect;
        private int effectTicks = 0;
        private int effectAmplifier = 0;
        private MobEffect selfEffect;
        private int selfEffectTicks = 0;
        private int selfEffectAmplifier = 0;

        private Builder(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public Builder desc(String value) {
            this.description = value;
            return this;
        }

        public Builder cost(float value) {
            this.energyCost = value;
            return this;
        }

        public Builder cd(int value) {
            this.cooldownTicks = value;
            return this;
        }

        public Builder rings(int value) {
            this.ringsRequired = value;
            return this;
        }

        public Builder target(SkillTarget value) {
            this.target = value;
            return this;
        }

        public Builder fx(FxShape shape, int color, float size) {
            this.visual = new SkillVisual(shape, color, 0, size, 24, null, 0f, 0);
            return this;
        }

        public Builder fx(SkillVisual value) {
            this.visual = value;
            return this;
        }

        public Builder dmg(float value) {
            this.damage = value;
            return this;
        }

        public Builder heal(float value) {
            this.heal = value;
            return this;
        }

        public Builder radius(double value) {
            this.radius = value;
            return this;
        }

        public Builder effect(MobEffect value, int ticks, int amplifier) {
            this.effect = value;
            this.effectTicks = ticks;
            this.effectAmplifier = amplifier;
            return this;
        }

        public Builder self(MobEffect value, int ticks, int amplifier) {
            this.selfEffect = value;
            this.selfEffectTicks = ticks;
            this.selfEffectAmplifier = amplifier;
            return this;
        }

        public SkillDef build() {
            return new SkillDef(id, name, description, energyCost, cooldownTicks, ringsRequired,
                    target, visual, damage, heal, radius,
                    effect, effectTicks, effectAmplifier,
                    selfEffect, selfEffectTicks, selfEffectAmplifier);
        }
    }
}
