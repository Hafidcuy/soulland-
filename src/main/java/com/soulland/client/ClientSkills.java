package com.soulland.client;

import com.soulland.soul.MartialSoul;
import com.soulland.soul.MartialSouls;
import com.soulland.soul.RingSkills;
import com.soulland.soul.SkillDef;

import java.util.List;

/** Penyusun loadout di sisi client: skill mana yang occupies slot tombol tertentu. */
public final class ClientSkills {
    private ClientSkills() {}

    /** Skill yang terpasang di slot tertentu, atau skill bawaan soul kalau slot kosong. */
    public static SkillDef resolve(MartialSoul soul, int slot) {
        String[] loadout = ClientCultivationData.loadout;
        if (loadout != null && slot >= 0 && slot < loadout.length) {
            String id = loadout[slot];
            if (id != null && !id.isEmpty()) {
                SkillDef custom = MartialSouls.skillById(id);
                if (custom != null && isUnlocked(custom)) return custom;
            }
        }
        return soul == null ? null : soul.skill(slot);
    }

    /** True kalau skill sudah terbuka untuk pemain. */
    public static boolean isUnlocked(SkillDef skill) {
        if (skill == null) return false;
        List<SkillDef> ringSkills = RingSkills.all();
        int index = ringSkills.indexOf(skill);
        if (index >= 0) {
            return ClientCultivationData.ringSkills > index;
        }
        return ClientCultivationData.rings >= skill.ringsRequired();
    }
}
