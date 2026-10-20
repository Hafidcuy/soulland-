package com.soulland.item;

import com.soulland.cultivation.CultivationCapability;
import com.soulland.cultivation.ICultivation;
import com.soulland.soul.SoulVisuals;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Serangan bermuatan (Tool Charge) dan Spirit Power Battery.
 *
 * <p>Pemain tahan klik kanan untuk mengisi charge, lalu lepas untuk
 * menyerang. Tier Deep Silver memakai battery sendiri, tier di bawahnya
 * memakai spirit power pemain sebagai bahan bakar.
 */
public final class ToolCharge {
    private ToolCharge() {}

    public static final String NBT_CHARGE = "soulland_charge";
    public static final String NBT_BATTERY = "soulland_battery";

    public static final int MAX_CHARGE = ToolMath.MAX_CHARGE;
    public static final int CHARGE_PER_TICK = 2;
    public static final int MIN_CHARGE = ToolMath.MIN_CHARGE;
    public static final int BATTERY_MAX = ToolMath.BATTERY_MAX;
    public static final int BATTERY_REGEN_INTERVAL = 40;
    public static final int BATTERY_COST = ToolMath.BATTERY_COST;
    public static final int ENERGY_COST = ToolMath.ENERGY_COST;
    public static final int STRIKE_COOLDOWN_TICKS = 20;

    public static int getCharge(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        return stack.getOrCreateTag().getInt(NBT_CHARGE);
    }

    public static void setCharge(ItemStack stack, int charge) {
        if (stack.isEmpty()) return;
        stack.getOrCreateTag().putInt(NBT_CHARGE, Math.max(0, Math.min(MAX_CHARGE, charge)));
    }

    public static int getBattery(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        return stack.getOrCreateTag().getInt(NBT_BATTERY);
    }

    public static void setBattery(ItemStack stack, int battery) {
        if (stack.isEmpty()) return;
        stack.getOrCreateTag().putInt(NBT_BATTERY, Math.max(0, Math.min(BATTERY_MAX, battery)));
    }
    /** Menambah charge selama tool ditahan. Dipanggil tiap tick server. */
    public static void tickCharge(ServerPlayer player, ItemStack stack) {
        if (!(stack.getItem() instanceof SpiritToolItem tool)) return;
        int charge = Math.min(MAX_CHARGE, getCharge(stack) + CHARGE_PER_TICK);
        setCharge(stack, charge);
        if (charge % 8 == 0) {
            SoulVisuals.chargeParticles(player, tool.soulId());
        }
    }

    /** Regenerasi battery tool Deep Silver yang sedang dipegang. */
    public static void regenBattery(ServerPlayer player, ItemStack stack) {
        if (!(stack.getItem() instanceof SpiritToolItem tool)) return;
        if (tool.tier() != SpiritToolTier.DEEP_SILVER) return;
        int battery = getBattery(stack);
        if (battery >= BATTERY_MAX) return;
        setBattery(stack, battery + 1);
    }

    /** Melepas serangan bermuatan. True bila serangan benar-benar terjadi. */
    public static boolean releaseStrike(ServerPlayer player, ItemStack stack) {
        int charge = getCharge(stack);
        setCharge(stack, 0);
        if (!(stack.getItem() instanceof SpiritToolItem tool)) return false;
        if (!ToolMath.chargeEnough(charge)) return false;

        int chargeCost = ToolMath.batteryCost(charge);
        if (tool.tier() == SpiritToolTier.DEEP_SILVER) {
            if (getBattery(stack) < chargeCost) {
                player.sendSystemMessage(Component.translatable("message.soulland.charge_no_battery")
                        .withStyle(ChatFormatting.RED));
                return false;
            }
        } else {
            boolean paid = CultivationCapability.get(player).map(cap -> {
                if (cap.getEnergy() < ENERGY_COST) return false;
                cap.setEnergy(cap.getEnergy() - ENERGY_COST);
                return true;
            }).orElse(false);
            if (!paid) {
                player.sendSystemMessage(Component.translatable("message.soulland.charge_no_energy")
                        .withStyle(ChatFormatting.RED));
                return false;
            }
        }

        float ratio = ToolMath.chargeRatio(charge);
        float damage = ToolMath.chargedDamage(1f + tool.getAttackDamage(), charge);

        if (tool.type() == SpiritToolType.BOW) {
            fireArrow(player, tool, damage);
        } else {
            strikeArea(player, tool, damage, ratio);
        }

        if (tool.tier() == SpiritToolTier.DEEP_SILVER) {
            setBattery(stack, getBattery(stack) - chargeCost);
        }
        player.getCooldowns().addCooldown(stack.getItem(), STRIKE_COOLDOWN_TICKS);
        return true;
    }

    private static void strikeArea(ServerPlayer player, SpiritToolItem tool, float damage, float ratio) {
        ServerLevel level = player.serverLevel();
        Vec3 look = player.getLookAngle();
        Vec3 center = player.position().add(look.scale(1.2));
        float radius = tool.type().chargeRadius();
        AABB box = new AABB(center, center).inflate(radius);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, box,
                e -> e != player && e.isAlive() && !(e instanceof ServerPlayer))) {
            target.hurt(player.damageSources().playerAttack(player), damage);
        }
        applySoulEffect(player, tool.soulId(), radius);
        level.playSound(null, player.blockPosition(), SoulVisuals.sound(tool.soulId()),
                SoundSource.PLAYERS, 1.0f, 0.8f + ratio * 0.4f);
    }

    private static void fireArrow(ServerPlayer player, SpiritToolItem tool, float damage) {
        ServerLevel level = player.serverLevel();
        Arrow arrow = new Arrow(level, player);
        arrow.setBaseDamage(damage);
        arrow.setSecondsOnFire(4);
        arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0f, 1f, 1f);
        level.addFreshEntity(arrow);
        applySoulEffect(player, tool.soulId(), 0f);
        level.playSound(null, player.blockPosition(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0f, 0.9f);
    }

    /** Efek kecil khas soul yang menempel pada serangan bermuatan. */
    private static void applySoulEffect(ServerPlayer player, String soulId, float radius) {
        ServerLevel level = player.serverLevel();
        switch (soulId) {
            case "hoe" -> {
                for (LivingEntity target : targetsAround(player, radius)) effect(target, MobEffects.MOVEMENT_SLOWDOWN, 40, 0);
                particles(level, player, SoulVisuals.soil());
            }
            case "blue_silver_grass" -> {
                for (LivingEntity target : targetsAround(player, radius)) effect(target, MobEffects.POISON, 60, 0);
                particles(level, player, ParticleTypes.SPORE_BLOSSOM_AIR);
            }
            case "hell_civet" -> {
                effect(player, MobEffects.MOVEMENT_SPEED, 60, 1);
                particles(level, player, ParticleTypes.SMOKE);
            }
            case "sausage" -> {
                player.heal(2f);
                particles(level, player, ParticleTypes.HEART);
            }
            case "seven_treasure_pagoda" -> {
                effect(player, MobEffects.ABSORPTION, 100, 0);
                particles(level, player, ParticleTypes.END_ROD);
            }
            case "evil_fire_phoenix" -> {
                for (LivingEntity target : targetsAround(player, radius)) target.setSecondsOnFire(3);
                particles(level, player, ParticleTypes.FLAME);
            }
            case "white_tiger" -> {
                for (LivingEntity target : targetsAround(player, radius)) knockback(player, target, 0.8, 0.2);
                particles(level, player, ParticleTypes.CLOUD);
            }
            case "soft_bone_rabbit" -> {
                effect(player, MobEffects.JUMP, 60, 1);
                particles(level, player, ParticleTypes.WHITE_ASH);
            }
            case "jade_serpent" -> {
                for (LivingEntity target : targetsAround(player, radius)) effect(target, MobEffects.POISON, 80, 1);
                particles(level, player, ParticleTypes.SNEEZE);
            }
            case "clear_sky_hammer" -> {
                for (LivingEntity target : targetsAround(player, radius)) knockback(player, target, 0.6, 0.9);
                particles(level, player, ParticleTypes.ELECTRIC_SPARK);
            }
            default -> {
            }
        }
    }

    private static java.util.List<LivingEntity> targetsAround(ServerPlayer player, float radius) {
        if (radius <= 0f) return java.util.List.of();
        ServerLevel level = player.serverLevel();
        AABB box = player.getBoundingBox().inflate(radius);
        return level.getEntitiesOfClass(LivingEntity.class, box,
                e -> e != player && e.isAlive() && !(e instanceof ServerPlayer));
    }

    private static void knockback(ServerPlayer player, LivingEntity target, double push, double lift) {
        Vec3 delta = target.position().subtract(player.position()).normalize().scale(push);
        target.setDeltaMovement(target.getDeltaMovement().add(delta.x, lift, delta.z));
        target.hurtMarked = true;
    }

    private static void effect(LivingEntity entity, net.minecraft.world.effect.MobEffect effect, int ticks, int amplifier) {
        entity.addEffect(new MobEffectInstance(effect, ticks, amplifier, false, true));
    }

    private static void particles(ServerLevel level, ServerPlayer player, ParticleOptions particle) {
        level.sendParticles(particle, player.getX(), player.getY() + 1, player.getZ(), 12, 0.5, 0.7, 0.5, 0.02);
    }

    /** Suara khas soul saat tool dilepas. */
    public static SoundEvent releaseSound(String soulId) {
        return SoulVisuals.sound(soulId);
    }
}
