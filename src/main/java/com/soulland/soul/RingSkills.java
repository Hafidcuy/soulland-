package com.soulland.soul;

import net.minecraft.world.effect.MobEffects;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Skill yang menempel pada spirit ring.
 *
 * <p>Di dunia Soul Land, setiap soul ring bisa menyumbang satu soul skill. Di sini setiap
 * ring yang diserap memberi satu skill yang bisa dipasang ke slot tombol mana pun, sehingga
 * pemain bebas menyusun build-nya sendiri.
 */
public final class RingSkills {
    private RingSkills() {}

    private static final Map<String, SkillDef> REGISTRY = new LinkedHashMap<>();

    private static void register(SkillDef skill) {
        REGISTRY.put(skill.id(), skill);
    }

    public static SkillDef get(String id) {
        if (id == null) return null;
        return REGISTRY.get(id);
    }

    public static List<SkillDef> all() {
        return new ArrayList<>(REGISTRY.values());
    }

    /** Skill ring untuk ring ke-n (1 sampai 9). */
    public static SkillDef forRing(int ringIndex) {
        int index = Math.max(1, Math.min(9, ringIndex)) - 1;
        return all().get(index);
    }

    static {
        register(SkillDef.builder("ring_guard", "Pelindung Ring")
                .desc("Cangkang spirit ring melekat di tubuh: Absorption selama 12 detik.")
                .cost(18f).cd(300).rings(1).target(SkillTarget.SELF)
                .fx(FxShape.RING, 0xE8C46A, 2.5f)
                .self(MobEffects.ABSORPTION, 240, 0).build());

        register(SkillDef.builder("ring_surge", "Semangat Ring")
                .desc("Darah soul berdesak: kecepatan gerak naik 12 detik.")
                .cost(16f).cd(280).rings(1).target(SkillTarget.SELF)
                .fx(FxShape.SPHERE, 0x9BE7FF, 2.0f)
                .self(MobEffects.MOVEMENT_SPEED, 240, 1).build());

        register(SkillDef.builder("ring_claw", "Badai Cakar")
                .desc("Cakar soul menyapu seluruh musuh di sekitar.")
                .cost(26f).cd(360).rings(2).target(SkillTarget.ENEMY_AOE)
                .fx(FxShape.CONE, 0xC8D2E0, 5f)
                .dmg(9f).radius(6).build());

        register(SkillDef.builder("ring_spear", "Tombak Soul")
                .desc("Tombak spirit menerobos musuh terdekat dan melemparnya.")
                .cost(28f).cd(360).rings(2).target(SkillTarget.ENEMY_NEAREST)
                .fx(FxShape.LINE, 0xE8EEF7, 8f)
                .dmg(17f).radius(9).build());

        register(SkillDef.builder("ring_king", "Raja Sejati")
                .desc("Wujud asli terbangun: Strength II dan Resistance II selama 25 detik.")
                .cost(40f).cd(600).rings(4).target(SkillTarget.SELF)
                .fx(FxShape.RING, 0xD8442F, 4.0f)
                .self(MobEffects.DAMAGE_BOOST, 500, 1)
                .self(MobEffects.DAMAGE_RESISTANCE, 500, 1).build());

        register(SkillDef.builder("ring_bulwark", "Pagar Soul")
                .desc("Pagar soul menahan serangan: Resistance II dan regenerasi 20 detik.")
                .cost(38f).cd(560).rings(4).target(SkillTarget.SELF)
                .fx(FxShape.RING, 0x8B5CF6, 4.5f)
                .self(MobEffects.DAMAGE_RESISTANCE, 400, 1)
                .self(MobEffects.REGENERATION, 400, 0).build());

        register(SkillDef.builder("ring_shatter", "Penghancur")
                .desc("Tenaga cincin memusnahkan semua musuh dalam area dan melempar mereka.")
                .cost(50f).cd(720).rings(6).target(SkillTarget.ENEMY_AOE)
                .fx(FxShape.BURST, 0xC9A227, 6.5f)
                .dmg(24f).radius(7)
                .effect(MobEffects.WEAKNESS, 200, 1).build());

        register(SkillDef.builder("ring_rebirth", "Bangkit")
                .desc("Luka menutup dan qi mengulang: pulih 14 HP dan buang semua efek buruk.")
                .cost(48f).cd(700).rings(6).target(SkillTarget.SELF)
                .fx(FxShape.SPHERE, 0x7BE0B0, 3.0f)
                .heal(14f)
                .self(MobEffects.REGENERATION, 300, 1).build());

        register(SkillDef.builder("ring_domain", "Domain Soul")
                .desc("Domain soul membentang: semua sekutu dapat Strength III dan Speed II selama 30 detik.")
                .cost(70f).cd(900).rings(8).target(SkillTarget.ALLY_AURA)
                .fx(FxShape.COLUMN, 0xFFF3C4, 4.0f)
                .radius(14)
                .effect(MobEffects.DAMAGE_BOOST, 600, 2)
                .effect(MobEffects.MOVEMENT_SPEED, 600, 1).build());
    }
}
