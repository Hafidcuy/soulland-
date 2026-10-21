package com.soulland.config;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Peluang drop tulang spirit.
 *
 * <p>Semua angka peluang ada di file konfigurasi, bukan di kode, supaya admin
 * server bisa mengubah tanpa rebuild mod. Nilai defaultnya sengaja kecil:
 * tulang adalah hadiah berburu, bukan hasil sampingan biasa.
 */
public final class BoneConfig {
    private BoneConfig() {}

    public static final ForgeConfigSpec SPEC;

    /** Peluang dasar mob biasa menjatuhkan tulang. */
    public static final ForgeConfigSpec.DoubleValue HOSTILE_BASE_CHANCE;
    /** Pengali peluang per spirit ring yang dimiliki pemain. */
    public static final ForgeConfigSpec.DoubleValue HOSTILE_RING_SCALE;
    /** Plafon peluang mob biasa. */
    public static final ForgeConfigSpec.DoubleValue HOSTILE_MAX_CHANCE;

    /** Peluang Ender Dragon menjatuhkan tulang. */
    public static final ForgeConfigSpec.DoubleValue DRAGON_CHANCE;
    /** Peluang Wither menjatuhkan tulang. */
    public static final ForgeConfigSpec.DoubleValue WITHER_CHANCE;
    /** Peluang Warden menjatuhkan tulang. */
    public static final ForgeConfigSpec.DoubleValue WARDEN_CHANCE;
    /** Peluang Elder Guardian menjatuhkan tulang. */
    public static final ForgeConfigSpec.DoubleValue GUARDIAN_CHANCE;

    /** Peluang Tulang Eksternal dari Ender Dragon. */
    public static final ForgeConfigSpec.DoubleValue EXTERNAL_DRAGON_CHANCE;
    /** Peluang Tulang Eksternal dari Wither. */
    public static final ForgeConfigSpec.DoubleValue EXTERNAL_WITHER_CHANCE;
    /** Peluang Tulang Eksternal dari Warden. */
    public static final ForgeConfigSpec.DoubleValue EXTERNAL_WARDEN_CHANCE;
    /** Peluang Tulang Eksternal dari Elder Guardian. */
    public static final ForgeConfigSpec.DoubleValue EXTERNAL_GUARDIAN_CHANCE;

    /** Bobot tier untuk drop mob biasa. */
    public static final ForgeConfigSpec.IntValue HOSTILE_WEIGHT_Y100;
    public static final ForgeConfigSpec.IntValue HOSTILE_WEIGHT_Y1000;
    public static final ForgeConfigSpec.IntValue HOSTILE_WEIGHT_Y10000;
    public static final ForgeConfigSpec.IntValue HOSTILE_WEIGHT_Y100000;
    public static final ForgeConfigSpec.IntValue HOSTILE_WEIGHT_Y1000000;

    /** Bobot tier untuk drop boss. */
    public static final ForgeConfigSpec.IntValue BOSS_WEIGHT_Y100;
    public static final ForgeConfigSpec.IntValue BOSS_WEIGHT_Y1000;
    public static final ForgeConfigSpec.IntValue BOSS_WEIGHT_Y10000;
    public static final ForgeConfigSpec.IntValue BOSS_WEIGHT_Y100000;
    public static final ForgeConfigSpec.IntValue BOSS_WEIGHT_Y1000000;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("Chance for a hostile mob to drop a spirit bone.").push("hostile");
        HOSTILE_BASE_CHANCE = builder
                .comment("Base chance for any hostile mob, before ring scaling.")
                .defineInRange("baseChance", 0.004, 0.0, 1.0);
        HOSTILE_RING_SCALE = builder
                .comment("Chance multiplier per spirit ring the player already owns.")
                .defineInRange("ringScale", 0.35, 0.0, 10.0);
        HOSTILE_MAX_CHANCE = builder
                .comment("Hard ceiling for hostile mob drops, so grinding stays capped.")
                .defineInRange("maxChance", 0.08, 0.0, 1.0);
        builder.pop();

        builder.comment("Chance for each boss to drop a spirit bone.").push("boss");
        DRAGON_CHANCE = builder
                .comment("Buffed Ender Dragon, the last soul beast emperor.")
                .defineInRange("dragonChance", 0.85, 0.0, 1.0);
        WITHER_CHANCE = builder
                .comment("Wither boss.")
                .defineInRange("witherChance", 0.75, 0.0, 1.0);
        WARDEN_CHANCE = builder
                .comment("Warden boss.")
                .defineInRange("wardenChance", 0.50, 0.0, 1.0);
        GUARDIAN_CHANCE = builder
                .comment("Elder Guardian boss.")
                .defineInRange("guardianChance", 0.45, 0.0, 1.0);
        builder.pop();

        builder.comment("Chance for the rare External Spirit Bone. Bosses only.").push("external");
        EXTERNAL_DRAGON_CHANCE = builder
                .comment("External bone from the buffed Ender Dragon.")
                .defineInRange("dragonChance", 0.020, 0.0, 1.0);
        EXTERNAL_WITHER_CHANCE = builder
                .comment("External bone from the Wither.")
                .defineInRange("witherChance", 0.015, 0.0, 1.0);
        EXTERNAL_WARDEN_CHANCE = builder
                .comment("External bone from the Warden.")
                .defineInRange("wardenChance", 0.010, 0.0, 1.0);
        EXTERNAL_GUARDIAN_CHANCE = builder
                .comment("External bone from the Elder Guardian.")
                .defineInRange("guardianChance", 0.010, 0.0, 1.0);
        builder.pop();

        builder.comment("Tier weights for hostile mob drops.").push("hostileWeights");
        HOSTILE_WEIGHT_Y100 = builder.defineInRange("y100", 70, 0, 1000);
        HOSTILE_WEIGHT_Y1000 = builder.defineInRange("y1000", 25, 0, 1000);
        HOSTILE_WEIGHT_Y10000 = builder.defineInRange("y10000", 5, 0, 1000);
        HOSTILE_WEIGHT_Y100000 = builder.defineInRange("y100000", 0, 0, 1000);
        HOSTILE_WEIGHT_Y1000000 = builder.defineInRange("y1000000", 0, 0, 1000);
        builder.pop();

        builder.comment("Tier weights for boss drops.").push("bossWeights");
        BOSS_WEIGHT_Y100 = builder.defineInRange("y100", 0, 0, 1000);
        BOSS_WEIGHT_Y1000 = builder.defineInRange("y1000", 0, 0, 1000);
        BOSS_WEIGHT_Y10000 = builder.defineInRange("y10000", 20, 0, 1000);
        BOSS_WEIGHT_Y100000 = builder.defineInRange("y100000", 50, 0, 1000);
        BOSS_WEIGHT_Y1000000 = builder.defineInRange("y1000000", 30, 0, 1000);
        builder.pop();

        SPEC = builder.build();
    }

    /** Mendaftarkan file konfigurasi ke Forge. */
    public static void register() {
        net.minecraftforge.fml.ModLoadingContext.get().registerConfig(
                net.minecraftforge.fml.config.ModConfig.Type.COMMON, SPEC, "soulland-bones");
    }

    /** Peluang dasar mob biasa, aman dipanggil sebelum config termuat. */
    public static double hostileBaseChance() {
        return read(HOSTILE_BASE_CHANCE);
    }

    /** Pengali peluang per spirit ring. */
    public static double hostileRingScale() {
        return read(HOSTILE_RING_SCALE);
    }

    /** Plafon peluang mob biasa. */
    public static double hostileMaxChance() {
        return read(HOSTILE_MAX_CHANCE);
    }

    /** Bobot tier untuk drop mob biasa. */
    public static int[] hostileWeights() {
        return new int[]{
                read(HOSTILE_WEIGHT_Y100),
                read(HOSTILE_WEIGHT_Y1000),
                read(HOSTILE_WEIGHT_Y10000),
                read(HOSTILE_WEIGHT_Y100000),
                read(HOSTILE_WEIGHT_Y1000000)};
    }

    /** Bobot tier untuk drop boss. */
    public static int[] bossWeights() {
        return new int[]{
                read(BOSS_WEIGHT_Y100),
                read(BOSS_WEIGHT_Y1000),
                read(BOSS_WEIGHT_Y10000),
                read(BOSS_WEIGHT_Y100000),
                read(BOSS_WEIGHT_Y1000000)};
    }

    /** Baca nilai config, jatuh ke nilai bawaan kalau config belum termuat. */
    private static double read(ForgeConfigSpec.DoubleValue value) {
        try {
            return value.get();
        } catch (IllegalStateException notLoaded) {
            return value.getDefault();
        }
    }

    /** Baca nilai config, jatuh ke nilai bawaan kalau config belum termuat. */
    private static int read(ForgeConfigSpec.IntValue value) {
        try {
            return value.get();
        } catch (IllegalStateException notLoaded) {
            return value.getDefault();
        }
    }
}
