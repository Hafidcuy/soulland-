package com.soulland.soul;

import net.minecraft.world.entity.LivingEntity;

import java.util.UUID;

/**
 * Tanda "?!" yang ditinggalkan skill penanda pada satu musuh.
 *
 * <p>Tanda ini menyimpan siapa yang menandai, jadi hanya serangan pemilik tanda
 * yang bisa memakainya. Begitu pakai, tandanya langsung hilang: satu tanda,
 * satu serangan yang diperkuat. Kalau tidak dipakai dalam masa berlaku,
 * tanda itu kedaluwarsa sendiri.
 *
 * <p>Data disimpan di persistent data entity, sama seperti elemen dan umur
 * soul beast, supaya tetap konsisten setelah server restart.
 */
public final class SoulMark {
    private SoulMark() {}

    private static final String KEY_OWNER = "soulland_mark_owner";
    private static final String KEY_UNTIL = "soulland_mark_until";

    /** Lama tanda bertahan sebelum kedaluwarsa, dalam tick. */
    public static final int DEFAULT_DURATION_TICKS = 200;

    /** Pengali damage tambahan untuk serangan yang memakai tanda. */
    public static final float DAMAGE_MULTIPLIER = 1.25f;

    /** Menandai satu entity untuk satu pemilik tertentu. */
    public static void mark(LivingEntity target, UUID owner, long gameTime) {
        mark(target, owner, gameTime, DEFAULT_DURATION_TICKS);
    }

    /** Menandai satu entity untuk satu pemilik tertentu, dengan durasi tersurat. */
    public static void mark(LivingEntity target, UUID owner, long gameTime, int durationTicks) {
        if (target == null || owner == null) return;
        target.getPersistentData().putString(KEY_OWNER, owner.toString());
        target.getPersistentData().putLong(KEY_UNTIL, gameTime + Math.max(20, durationTicks));
    }

    /**
     * Memakai tanda milik pemilik tertentu.
     *
     * @return true kalau tanda berlaku dan baru saja dipakai
     */
    public static boolean consume(LivingEntity target, UUID owner, long gameTime) {
        if (target == null || owner == null) return false;
        var data = target.getPersistentData();
        if (!data.contains(KEY_OWNER) || !data.contains(KEY_UNTIL)) return false;
        if (data.getLong(KEY_UNTIL) < gameTime) {
            clear(target);
            return false;
        }
        if (!owner.toString().equals(data.getString(KEY_OWNER))) return false;
        clear(target);
        return true;
    }

    /** True kalau tanda masih berlaku, tanpa memakainya. */
    public static boolean isActive(LivingEntity target, UUID owner, long gameTime) {
        if (target == null || owner == null) return false;
        var data = target.getPersistentData();
        if (!data.contains(KEY_OWNER) || !data.contains(KEY_UNTIL)) return false;
        if (data.getLong(KEY_UNTIL) < gameTime) return false;
        return owner.toString().equals(data.getString(KEY_OWNER));
    }

    /** Menghapus tanda dari satu entity. */
    public static void clear(LivingEntity target) {
        if (target == null) return;
        target.getPersistentData().remove(KEY_OWNER);
        target.getPersistentData().remove(KEY_UNTIL);
    }
}
