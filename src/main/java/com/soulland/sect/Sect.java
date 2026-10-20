package com.soulland.sect;

/**
 * Sekte yang bisa dipilih pemain.
 *
 * <p>Sekte memberi bonus pasif yang selalu berlaku. Tidak ada penalti, jadi memilih
 * sekte tidak bisa membuat pemain tersesat.
 */
public record Sect(
        String id,
        String name,
        String description,
        double damageBonus,
        double xpBonus,
        double healthBonus,
        double speedBonus,
        double healBonus
) {
    public static final Sect TANG = new Sect("tang", "Sekte Tang",
            "Klan Tang, pewaris Clear Sky Hammer. Latihan keras dan disiplin, +20% XP kultivasi.",
            0.0, 0.20, 0.0, 0.0, 0.0);

    public static final Sect SHREK = new Sect("shrek", "Sekte Shrek",
            "Sekte Seven Devils Seven Shrek. Gaya bertarung brutal, +12% damage dan +2 attack damage.",
            0.12, 0.0, 0.0, 0.0, 0.0);

    public static final Sect STARLUO = new Sect("starluo", "Sekte Bintang Luo",
            "Kerajaan Bintang Luo. Lompatan dan kelincahan, +10% kecepatan gerak dan +10% HP maksimum.",
            0.0, 0.0, 0.10, 0.10, 0.0);

    public static final Sect QIBAO = new Sect("qibao", "Sekte Tujuh Harta",
            "Sekte Tujuh Harta Benteng Konstelasi Tujuh Harta. Tubuh tahan banting, +20% HP maksimum dan +25% penyembuhan.",
            0.0, 0.0, 0.20, 0.0, 0.25);

    private static final java.util.List<Sect> ALL = java.util.List.of(TANG, SHREK, STARLUO, QIBAO);

    public static java.util.List<Sect> all() {
        return ALL;
    }

    public static Sect get(String id) {
        if (id == null || id.isEmpty()) return null;
        for (Sect sect : ALL) {
            if (sect.id().equals(id)) return sect;
        }
        return null;
    }
}
