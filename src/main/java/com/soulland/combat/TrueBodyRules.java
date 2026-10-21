package com.soulland.combat;

/**
 * Aturan tetap True Body: syarat buka, lama aktif, dan jeda Antarctic.
 *
 * <p>Angka di sini sengaja konstan dan bukan hasil hitungan, karena tiga hal
 * ini jadi kontrak gameplay yang harus sama persis di server, client, dan
 * test:
 * <ul>
 *   <li>{@link #UNLOCK_RINGS} = 7 spirit ring, sama dengan slot skill terakhir.</li>
 *   <li>{@link #DURATION_TICKS} = 400 tick = 20 detik masa aktif.</li>
 *   <li>{@link #COOLDOWN_TICKS} = 6000 tick = 5 menit jeda.</li>
 * </ul>
 *
 * <p>Kelas ini murni Java tanpa kelas Minecraft, jadi seluruh matematikanya
 * bisa diuji tanpa bootup registry.
 */
public final class TrueBodyRules {
    private TrueBodyRules() {}

    /** Jumlah spirit ring minimal untuk membuka True Body. */
    public static final int UNLOCK_RINGS = 7;

    /** Lama True Body aktif, dalam tick. 400 tick = 20 detik. */
    public static final int DURATION_TICKS = 400;

    /** Lama True Body aktif, dalam detik. 400 tick = 20 detik. */
    public static final int DURATION_SECONDS = DURATION_TICKS / 20;

    /** Jeda setelah True Body selesai, dalam tick. 6000 tick = 5 menit. */
    public static final int COOLDOWN_TICKS = 6000;

    /** Jeda setelah True Body selesai, dalam detik. 6000 tick = 5 menit. */
    public static final int COOLDOWN_SECONDS = COOLDOWN_TICKS / 20;

    /** True kalau pemain sudah cukup ring untuk membuka True Body. */
    public static boolean isUnlocked(int rings) {
        return rings >= UNLOCK_RINGS;
    }

    /** Sisa waktu aktif True Body dalam tick; 0 kalau sudah selesai. */
    public static int remainingTicks(long gameTime, long until) {
        long remaining = until - gameTime;
        if (remaining <= 0L) return 0;
        return (int) Math.min(remaining, Integer.MAX_VALUE);
    }

    /** Sisa waktu aktif True Body dalam detik, dibulatkan ke atas. */
    public static int remainingSeconds(long gameTime, long until) {
        return (remainingTicks(gameTime, until) + 19) / 20;
    }

    /** Sisa jeda True Body dalam detik, dibulatkan ke atas; 0 kalau jeda sudah lewat. */
    public static int cooldownSeconds(long gameTime, long until) {
        return remainingSeconds(gameTime, until);
    }

    /** True kalau True Body sedang aktif pada waktu game tertentu. */
    public static boolean isActive(long gameTime, long until) {
        return gameTime < until;
    }

    /** True kalau jeda True Body masih berjalan pada waktu game tertentu. */
    public static boolean onCooldown(long gameTime, long cooldownUntil) {
        return gameTime < cooldownUntil;
    }

    /** Titik game saat True Body aktif selesai. */
    public static long activeUntil(long gameTime) {
        return gameTime + DURATION_TICKS;
    }

    /** Titik game saat jeda True Body selesai. */
    public static long cooldownUntil(long gameTime) {
        return gameTime + COOLDOWN_TICKS;
    }
}
