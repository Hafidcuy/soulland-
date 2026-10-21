package com.soulland.combat;

/**
 * Aturan tetap Domain: jari-jari wilayah, pengurasan energi, dan ritme tick.
 *
 * <p>Domain tidak punya syarat ring: sebuah domain sudah paced oleh
 * pengurasan spirit power per detik dan mati sendiri saat energi habis, jadi
 * menambahkan syarat ring lagi hanya jadi aturan yang tidak diminta.
 *
 * <p>Kelas ini murni Java tanpa kelas Minecraft, jadi seluruh matematikanya
 * bisa diuji tanpa bootup registry.
 */
public final class DomainRules {
    private DomainRules() {}

    /** Jari-jari wilayah domain, dalam blok. */
    public static final double RADIUS = 12.0;

    /** Jari-jari wilayah domain, dalam blok kuadrat (dipakai query AABB). */
    public static final double RADIUS_SQUARED = RADIUS * RADIUS;

    /** Berapa tick satu detik, dipakai mengubah pengurasan per detik jadi per tick. */
    public static final int TICKS_PER_SECOND = 20;

    /** Berapa partikel cincin yang digambar di tanah per putaran. */
    public static final int RING_PARTICLES = 24;

    /** Berapa tick satu putaran cincin partikel. */
    public static final int RING_PARTICLE_INTERVAL = 4;

    /** Berapa tick efek status domain disegarkan ke semua yang ada di radius. */
    public static final int EFFECT_REFRESH_INTERVAL = 10;

    /** True kalau radius target masih berada di dalam wilayah domain. */
    public static boolean inRadius(double distanceSquared) {
        return distanceSquared <= RADIUS_SQUARED;
    }

    /** Energi yang terpakai dalam satu detik saat domain aktif. */
    public static float drainPerSecond(int drainPerSecond) {
        return Math.max(0f, drainPerSecond);
    }

    /** Energi yang terpakai dalam satu tick saat domain aktif. */
    public static float drainPerTick(int drainPerSecond) {
        return drainPerSecond(drainPerSecond) / TICKS_PER_SECOND;
    }

    /** Damage domain ke musuh dalam satu tick. */
    public static float damagePerTick(float damagePerSecond) {
        return Math.max(0f, damagePerSecond) / TICKS_PER_SECOND;
    }

    /** Damage domain ke musuh dalam satu detik. */
    public static float damagePerSecond(float damagePerSecond) {
        return Math.max(0f, damagePerSecond);
    }

    /**
     * Sisa energi setelah domain aktif selama sejumlah detik.
     *
     * <p>Dipakai test untuk memastikan domain benar-benar menguras energi dan
     * otomatis mati saat energi habis.
     */
    public static float energyAfterSeconds(float energy, int drainPerSecond, int seconds) {
        float drained = drainPerSecond(drainPerSecond) * Math.max(0, seconds);
        return Math.max(0f, energy - drained);
    }

    /**
     * True kalau energi habis dalam hitungan detik ke depan, jadi domain
     * harus dimatikan otomatis.
     */
    public static boolean runsOut(float energy, int drainPerSecond, int seconds) {
        return energyAfterSeconds(energy, drainPerSecond, seconds) <= 0f;
    }
}
