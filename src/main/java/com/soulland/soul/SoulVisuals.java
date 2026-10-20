package com.soulland.soul;

import com.soulland.SoulLandMod;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.Blocks;

import java.util.HashMap;
import java.util.Map;

/**
 * Aset visual tiap martial soul: partikel, suara, dan item model manifestasi.
 * Dipakai server (partikel dan suara saat skill aktif) dan client (model manifestasi).
 */
public final class SoulVisuals {
    private SoulVisuals() {}

    private record Look(ParticleOptions particle, SoundEvent sound) {}

    private static final Map<String, Look> LOOKS = new HashMap<>();

    private static void register(String soulId, ParticleOptions particle, SoundEvent sound) {
        LOOKS.put(soulId, new Look(particle, sound));
    }

    static {
        register("hoe", soil(), SoundEvents.GRAVEL_HIT);
        register("blue_silver_grass", ParticleTypes.SPORE_BLOSSOM_AIR, SoundEvents.AMETHYST_BLOCK_CHIME);
        register("hell_civet", ParticleTypes.SMOKE, SoundEvents.ENDERMAN_TELEPORT);
        register("sausage", ParticleTypes.HEART, SoundEvents.BREWING_STAND_BREW);
        register("seven_treasure_pagoda", ParticleTypes.END_ROD, SoundEvents.BEACON_ACTIVATE);
        register("evil_fire_phoenix", ParticleTypes.FLAME, SoundEvents.BLAZE_BURN);
        register("white_tiger", ParticleTypes.CLOUD, SoundEvents.ENDER_DRAGON_GROWL);
        register("soft_bone_rabbit", ParticleTypes.WHITE_ASH, SoundEvents.FIREWORK_ROCKET_LAUNCH);
        register("jade_serpent", ParticleTypes.SNEEZE, SoundEvents.WITCH_DRINK);
        register("clear_sky_hammer", ParticleTypes.ELECTRIC_SPARK, SoundEvents.LIGHTNING_BOLT_THUNDER);
    }

    /** Partikel khas soul saat manifestasi. */
    public static ParticleOptions particle(String soulId) {
        Look look = LOOKS.get(soulId);
        return look == null ? ParticleTypes.SMOKE : look.particle();
    }

    /** Partikel tanah untuk soul cangkul. FALLING_DUST butuh block state, jadi dibungkus. */
    public static ParticleOptions soil() {
        return new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.DIRT.defaultBlockState());
    }

    /** Suara khas soul saat manifestasi dimulai. */
    public static SoundEvent sound(String soulId) {
        Look look = LOOKS.get(soulId);
        return look == null ? SoundEvents.BEACON_ACTIVATE : look.sound();
    }

    /** ResourceLocation item model manifestasi milik soul. */
    public static ResourceLocation manifestationItem(String soulId) {
        return new ResourceLocation(SoulLandMod.MODID, "manifestation_" + soulId);
    }
}
