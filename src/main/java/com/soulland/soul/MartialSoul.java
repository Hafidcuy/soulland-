package com.soulland.soul;

import java.util.List;

/** Martial Soul (Wu Hun) seperti di dunia Soul Land. */
public record MartialSoul(
        String id,
        String name,
        String nameZh,
        String owner,
        String affiliation,
        String type,
        String family,
        String rarity,
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
