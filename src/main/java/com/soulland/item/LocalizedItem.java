package com.soulland.item;

import com.soulland.lang.SoulLang;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Item yang nama tampilnya mengikuti bahasa yang dipilih pemain.
 *
 * <p>Bahasa vanilla Minecraft hanya mengenal satu bahasa aktif, jadi nama
 * item bawaan mod selalu mengikuti bahasa client. Mod ini memilih sendiri:
 * nama item di-resolve dari file lang mod memakai bahasa pemain.
 */
public class LocalizedItem extends Item {

    public LocalizedItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        return name(this);
    }

    /** Nama item dalam bahasa pemain, dengan nama registry sebagai cadangan. */
    public static Component name(Item item) {
        return name(item, SoulLang.activeLanguage());
    }

    /**
     * Nama item dalam bahasa tertentu. Server memakai ini supaya pesan yang
     * dikirim ke setiap pemain memakai bahasa yang ia pilih, bukan bahasa
     * client lain yang kebetulan tersambung.
     */
    public static Component name(Item item, String language) {
        return SoulLang.text(language, item.getDescriptionId(), fallbackName(item));
    }

    /**
     * Nama cadangan yang jujur: dibaca dari id registry, misalnya
     * {@code spirit_ring_1000} menjadi "Spirit Ring 1000".
     */
    public static String fallbackName(Item item) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        String path = id == null ? item.toString() : id.getPath();
        StringBuilder builder = new StringBuilder();
        for (String part : path.split("_")) {
            if (part.isEmpty()) continue;
            if (builder.length() > 0) builder.append(' ');
            builder.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return builder.length() == 0 ? path : builder.toString();
    }
}
