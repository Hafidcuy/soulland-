package com.soulland.soul;

import java.util.List;

/** Martial Soul (Wu Hun) seperti di dunia Soul Land. */
public record MartialSoul(
        String id,
        String name,
        String owner,
        String type,
        String rarity,
        String description,
        List<SkillDef> skills
) {
    public SkillDef skill(int slot) {
        if (slot < 0 || slot >= skills.size()) return null;
        return skills.get(slot);
    }
}
