package com.soulland.soul;

import java.util.List;

/**
 * Martial Soul (Wu Hun) seperti di dunia Soul Land.
 *
 * <p>Field dikelompokkan menjadi tiga lapis:
 * <ul>
 *   <li><b>Identitas</b>: id, nama, pemilik asli, afiliasi.</li>
 *   <li><b>Klasifikasi</b>: {@code type} (peran dalam pertarungan), {@code family}
 *       (kelompok turunannya), {@code rarity}, {@code lineage} (bentuk manifestasi),
 *       {@code element} (unsur dasar soul), dan {@code affinity} (unsur yang
 *       membuat soul ini beresonansi).</li>
 *   <li><b>Isi</b>: deskripsi, warna, potret, jalur evolusi, dan empat skill.</li>
 * </ul>
 *
 * <p>Penambahan {@code lineage} dan {@code affinity} pada v1.4 bersifat aditif:
 * {@code family} lama tetap utuh karena dipakai kode lain, dan seluruh accessor
 * lama tidak berubah.
 */
public record MartialSoul(
        String id,
        String name,
        String nameZh,
        String owner,
        String affiliation,
        String type,
        String family,
        String rarity,
        SoulLineage lineage,
        Element affinity,
        String description,
        String element,
        int color,
        String portrait,
        String evolutionId,
        boolean notable,
        List<SkillDef> skills
) {
    public SkillDef skill(int slot) {
        if (slot < 0 || slot >= skills.size()) return null;
        return skills.get(slot);
    }

    public boolean hasEvolution() {
        return evolutionId != null && !evolutionId.isEmpty();
    }
}
