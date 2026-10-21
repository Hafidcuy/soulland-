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

        // v1.4: katalog 43 soul. Partikel dan suara dipilih per rasa soul,
        // supaya manifestasi tiap soul terbaca berbeda di layar.
        register("golden_dragon", ParticleTypes.FLAME, SoundEvents.ENDER_DRAGON_AMBIENT);
        register("abyssal_demon_dragon", ParticleTypes.SOUL, SoundEvents.ENDERMAN_TELEPORT);
        register("abyssal_ice_demon_dragon", ParticleTypes.SNOWFLAKE, SoundEvents.AMETHYST_CLUSTER_BREAK);
        register("ultimate_fire_dragon", ParticleTypes.LAVA, SoundEvents.BLAZE_BURN);
        register("ultimate_ice_dragon", ParticleTypes.SNOWFLAKE, SoundEvents.GLASS_BREAK);
        register("thunder_elephant", ParticleTypes.ELECTRIC_SPARK, SoundEvents.ANVIL_LAND);
        register("golden_ant", ParticleTypes.WAX_OFF, SoundEvents.SPIDER_AMBIENT);
        register("titan_giant_ape", ParticleTypes.EXPLOSION, SoundEvents.IRON_GOLEM_ATTACK);
        register("sea_devil_manatee", ParticleTypes.SPLASH, SoundEvents.BUBBLE_COLUMN_UPWARDS_AMBIENT);
        register("bright_wing_moth", ParticleTypes.FIREWORK, SoundEvents.BEE_LOOP);
        register("phoenix", ParticleTypes.SOUL_FIRE_FLAME, SoundEvents.ENDER_DRAGON_FLAP);
        register("blue_silver_spear", ParticleTypes.SPORE_BLOSSOM_AIR, SoundEvents.BAMBOO_SAPLING_PLACE);
        register("seven_kill_sword", ParticleTypes.SMOKE, SoundEvents.WITHER_SKELETON_HURT);
        register("giant_hammer", ParticleTypes.EXPLOSION, SoundEvents.ANVIL_USE);
        register("blood_lash", ParticleTypes.DAMAGE_INDICATOR, SoundEvents.SLIME_ATTACK);
        register("frost_needle", ParticleTypes.SNOWFLAKE, SoundEvents.ARROW_HIT);
        register("thunder_spear", ParticleTypes.ELECTRIC_SPARK, SoundEvents.LIGHTNING_BOLT_THUNDER);
        register("spirit_sword", ParticleTypes.END_ROD, SoundEvents.NETHERITE_BLOCK_HIT);
        register("iron_anchor", ParticleTypes.SPLASH, SoundEvents.RESPAWN_ANCHOR_SET_SPAWN);
        register("bronze_mirror", ParticleTypes.END_ROD, SoundEvents.AMETHYST_BLOCK_CHIME);
        register("blue_silver_emperor", ParticleTypes.SPORE_BLOSSOM_AIR, SoundEvents.GRASS_PLACE);
        register("iron_body", ParticleTypes.CRIT, SoundEvents.ARMOR_EQUIP_NETHERITE);
        register("bone_shell", ParticleTypes.WHITE_ASH, SoundEvents.BONE_BLOCK_BREAK);
        register("stone_skin", ParticleTypes.CLOUD, SoundEvents.STONE_PLACE);
        register("flesh_regrowth", ParticleTypes.HEART, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES);
        register("flame_body", ParticleTypes.FLAME, SoundEvents.FIRECHARGE_USE);
        register("spirit_veins", ParticleTypes.ELECTRIC_SPARK, SoundEvents.BEACON_ACTIVATE);
        register("soul_mirror", ParticleTypes.PORTAL, SoundEvents.SOUL_ESCAPE);
        register("spirit_lamp", ParticleTypes.FLAME, SoundEvents.CANDLE_AMBIENT);
        register("soul_forge", ParticleTypes.LAVA, SoundEvents.SMITHING_TABLE_USE);
        register("star_array", ParticleTypes.END_ROD, SoundEvents.ENCHANTMENT_TABLE_USE);
        register("demon_eye", ParticleTypes.SOUL, SoundEvents.WARDEN_NEARBY_CLOSE);
        register("ancient_seal", ParticleTypes.SMOKE, SoundEvents.BELL_RESONATE);
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

    /** Partikel soul yang mengelilingi pemain saat tool ditahan untuk charge. */
    public static void chargeParticles(net.minecraft.server.level.ServerPlayer player, String soulId) {
        net.minecraft.server.level.ServerLevel level = player.serverLevel();
        level.sendParticles(particle(soulId), player.getX(), player.getY() + 1.2, player.getZ(),
                4, 0.6, 0.5, 0.6, 0.01);
    }
}
