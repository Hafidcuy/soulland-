package com.soulland.item;

import com.soulland.SoulLandMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, SoulLandMod.MODID);

    public static final RegistryObject<SpiritRingItem> SPIRIT_RING_100 = ITEMS.register("spirit_ring_100",
            () -> new SpiritRingItem(SpiritRingItem.Tier.Y100, new Item.Properties().rarity(Rarity.COMMON)));
    public static final RegistryObject<SpiritRingItem> SPIRIT_RING_1000 = ITEMS.register("spirit_ring_1000",
            () -> new SpiritRingItem(SpiritRingItem.Tier.Y1000, new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<SpiritRingItem> SPIRIT_RING_10000 = ITEMS.register("spirit_ring_10000",
            () -> new SpiritRingItem(SpiritRingItem.Tier.Y10000, new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<SpiritRingItem> SPIRIT_RING_100000 = ITEMS.register("spirit_ring_100000",
            () -> new SpiritRingItem(SpiritRingItem.Tier.Y100000, new Item.Properties().rarity(Rarity.EPIC)));

    // Item manifestasi: bentuk visual martial soul yang muncul saat skill diaktifkan.
    public static final RegistryObject<Item> MANIFESTATION_HOE = ITEMS.register("manifestation_hoe",
            () -> new Item(new Item.Properties().rarity(Rarity.COMMON)));
    public static final RegistryObject<Item> MANIFESTATION_BLUE_SILVER_GRASS = ITEMS.register("manifestation_blue_silver_grass",
            () -> new Item(new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> MANIFESTATION_HELL_CIVET = ITEMS.register("manifestation_hell_civet",
            () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> MANIFESTATION_SAUSAGE = ITEMS.register("manifestation_sausage",
            () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> MANIFESTATION_SEVEN_TREASURE_PAGODA = ITEMS.register("manifestation_seven_treasure_pagoda",
            () -> new Item(new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> MANIFESTATION_EVIL_FIRE_PHOENIX = ITEMS.register("manifestation_evil_fire_phoenix",
            () -> new Item(new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> MANIFESTATION_WHITE_TIGER = ITEMS.register("manifestation_white_tiger",
            () -> new Item(new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> MANIFESTATION_SOFT_BONE_RABBIT = ITEMS.register("manifestation_soft_bone_rabbit",
            () -> new Item(new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> MANIFESTATION_JADE_SERPENT = ITEMS.register("manifestation_jade_serpent",
            () -> new Item(new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> MANIFESTATION_CLEAR_SKY_HAMMER = ITEMS.register("manifestation_clear_sky_hammer",
            () -> new Item(new Item.Properties().rarity(Rarity.EPIC)));

    /**
     * Stack item manifestasi untuk sebuah soul. Dikembalikan kosong bila soul
     * tidak punya item manifestasi (misalnya karena data soul berubah).
     */
    public static ItemStack manifestationStack(String soulId) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(SoulLandMod.MODID, "manifestation_" + soulId));
        return item == null ? ItemStack.EMPTY : new ItemStack(item);
    }
}
