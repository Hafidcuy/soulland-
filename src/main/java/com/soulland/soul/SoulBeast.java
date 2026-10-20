package com.soulland.soul;

import net.minecraft.world.entity.LivingEntity;

/**
 * Identitas soul beast: usia dan elemen tempurnya.
 *
 * <p>Data disimpan di persistent data entity supaya nama dan elemen tetap sama setelah
 * server restart, dan supaya pembacaan elemen cepat tanpa scan dunia.
 */
public final class SoulBeast {
    private SoulBeast() {}

    private static final String KEY_ELEMENT = "soulland_element";
    private static final String KEY_YEARS = "soulland_years";

    public static void assign(LivingEntity entity, Element element, int years) {
        entity.getPersistentData().putInt(KEY_ELEMENT, element.ordinal());
        entity.getPersistentData().putInt(KEY_YEARS, years);
    }

    public static Element elementOf(LivingEntity entity) {
        if (entity == null) return Element.NONE;
        var data = entity.getPersistentData();
        if (!data.contains(KEY_ELEMENT)) return Element.NONE;
        Element[] values = Element.values();
        int index = data.getInt(KEY_ELEMENT);
        if (index < 0 || index >= values.length) return Element.NONE;
        return values[index];
    }

    public static int yearsOf(LivingEntity entity) {
        if (entity == null) return 0;
        return entity.getPersistentData().getInt(KEY_YEARS);
    }
}
