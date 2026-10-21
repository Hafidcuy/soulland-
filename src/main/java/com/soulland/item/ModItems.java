package com.soulland.item;

import com.soulland.SoulLandMod;
import com.soulland.bone.SpiritBoneItem;
import com.soulland.bone.SpiritBoneSlot;
import com.soulland.bone.SpiritBoneTier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, SoulLandMod.MODID);

    public static final RegistryObject<Item> SOUL_ESSENCE = ITEMS.register("soul_essence",
            () -> new LocalizedItem(new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> SOUL_CORE = ITEMS.register("soul_core",
            () -> new LocalizedItem(new Item.Properties().rarity(Rarity.RARE)));

    public static final RegistryObject<SpiritRingItem> SPIRIT_RING_100 = ITEMS.register("spirit_ring_100",
            () -> new SpiritRingItem(SpiritRingItem.Tier.Y100, new Item.Properties().rarity(Rarity.COMMON)));
    public static final RegistryObject<SpiritRingItem> SPIRIT_RING_1000 = ITEMS.register("spirit_ring_1000",
            () -> new SpiritRingItem(SpiritRingItem.Tier.Y1000, new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<SpiritRingItem> SPIRIT_RING_10000 = ITEMS.register("spirit_ring_10000",
            () -> new SpiritRingItem(SpiritRingItem.Tier.Y10000, new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<SpiritRingItem> SPIRIT_RING_100000 = ITEMS.register("spirit_ring_100000",
            () -> new SpiritRingItem(SpiritRingItem.Tier.Y100000, new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<SpiritRingItem> SPIRIT_RING_1000000 = ITEMS.register("spirit_ring_1000000",
            () -> new SpiritRingItem(SpiritRingItem.Tier.Y1000000, new Item.Properties().rarity(Rarity.EPIC)));

    // Item manifestasi: bentuk visual martial soul yang muncul saat skill diaktifkan.
    public static final RegistryObject<Item> MANIFESTATION_HOE = ITEMS.register("manifestation_hoe",
            () -> new LocalizedItem(new Item.Properties().rarity(Rarity.COMMON)));
    public static final RegistryObject<Item> MANIFESTATION_BLUE_SILVER_GRASS = ITEMS.register("manifestation_blue_silver_grass",
            () -> new LocalizedItem(new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> MANIFESTATION_HELL_CIVET = ITEMS.register("manifestation_hell_civet",
            () -> new LocalizedItem(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> MANIFESTATION_SAUSAGE = ITEMS.register("manifestation_sausage",
            () -> new LocalizedItem(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> MANIFESTATION_SEVEN_TREASURE_PAGODA = ITEMS.register("manifestation_seven_treasure_pagoda",
            () -> new LocalizedItem(new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> MANIFESTATION_EVIL_FIRE_PHOENIX = ITEMS.register("manifestation_evil_fire_phoenix",
            () -> new LocalizedItem(new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> MANIFESTATION_WHITE_TIGER = ITEMS.register("manifestation_white_tiger",
            () -> new LocalizedItem(new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> MANIFESTATION_SOFT_BONE_RABBIT = ITEMS.register("manifestation_soft_bone_rabbit",
            () -> new LocalizedItem(new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> MANIFESTATION_JADE_SERPENT = ITEMS.register("manifestation_jade_serpent",
            () -> new LocalizedItem(new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> MANIFESTATION_CLEAR_SKY_HAMMER = ITEMS.register("manifestation_clear_sky_hammer",
            () -> new LocalizedItem(new Item.Properties().rarity(Rarity.EPIC)));

    // v1.4: manifestasi 33 soul baru dari katalog 43 soul.
    public static final RegistryObject<Item> MANIFESTATION_GOLDEN_DRAGON = ITEMS.register("manifestation_golden_dragon",
            () -> new LocalizedItem(new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> MANIFESTATION_ABYSSAL_DEMON_DRAGON = ITEMS.register("manifestation_abyssal_demon_dragon",
            () -> new LocalizedItem(new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> MANIFESTATION_ABYSSAL_ICE_DEMON_DRAGON = ITEMS.register("manifestation_abyssal_ice_demon_dragon",
            () -> new LocalizedItem(new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> MANIFESTATION_ULTIMATE_FIRE_DRAGON = ITEMS.register("manifestation_ultimate_fire_dragon",
            () -> new LocalizedItem(new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> MANIFESTATION_ULTIMATE_ICE_DRAGON = ITEMS.register("manifestation_ultimate_ice_dragon",
            () -> new LocalizedItem(new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> MANIFESTATION_THUNDER_ELEPHANT = ITEMS.register("manifestation_thunder_elephant",
            () -> new LocalizedItem(new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> MANIFESTATION_GOLDEN_ANT = ITEMS.register("manifestation_golden_ant",
            () -> new LocalizedItem(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> MANIFESTATION_TITAN_GIANT_APE = ITEMS.register("manifestation_titan_giant_ape",
            () -> new LocalizedItem(new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> MANIFESTATION_SEA_DEVIL_MANATEE = ITEMS.register("manifestation_sea_devil_manatee",
            () -> new LocalizedItem(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> MANIFESTATION_BRIGHT_WING_MOTH = ITEMS.register("manifestation_bright_wing_moth",
            () -> new LocalizedItem(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> MANIFESTATION_PHOENIX = ITEMS.register("manifestation_phoenix",
            () -> new LocalizedItem(new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> MANIFESTATION_BLUE_SILVER_SPEAR = ITEMS.register("manifestation_blue_silver_spear",
            () -> new LocalizedItem(new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> MANIFESTATION_SEVEN_KILL_SWORD = ITEMS.register("manifestation_seven_kill_sword",
            () -> new LocalizedItem(new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> MANIFESTATION_GIANT_HAMMER = ITEMS.register("manifestation_giant_hammer",
            () -> new LocalizedItem(new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> MANIFESTATION_BLOOD_LASH = ITEMS.register("manifestation_blood_lash",
            () -> new LocalizedItem(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> MANIFESTATION_FROST_NEEDLE = ITEMS.register("manifestation_frost_needle",
            () -> new LocalizedItem(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> MANIFESTATION_THUNDER_SPEAR = ITEMS.register("manifestation_thunder_spear",
            () -> new LocalizedItem(new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> MANIFESTATION_SPIRIT_SWORD = ITEMS.register("manifestation_spirit_sword",
            () -> new LocalizedItem(new Item.Properties().rarity(Rarity.COMMON)));
    public static final RegistryObject<Item> MANIFESTATION_IRON_ANCHOR = ITEMS.register("manifestation_iron_anchor",
            () -> new LocalizedItem(new Item.Properties().rarity(Rarity.COMMON)));
    public static final RegistryObject<Item> MANIFESTATION_BRONZE_MIRROR = ITEMS.register("manifestation_bronze_mirror",
            () -> new LocalizedItem(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> MANIFESTATION_BLUE_SILVER_EMPEROR = ITEMS.register("manifestation_blue_silver_emperor",
            () -> new LocalizedItem(new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> MANIFESTATION_IRON_BODY = ITEMS.register("manifestation_iron_body",
            () -> new LocalizedItem(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> MANIFESTATION_BONE_SHELL = ITEMS.register("manifestation_bone_shell",
            () -> new LocalizedItem(new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> MANIFESTATION_STONE_SKIN = ITEMS.register("manifestation_stone_skin",
            () -> new LocalizedItem(new Item.Properties().rarity(Rarity.COMMON)));
    public static final RegistryObject<Item> MANIFESTATION_FLESH_REGROWTH = ITEMS.register("manifestation_flesh_regrowth",
            () -> new LocalizedItem(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> MANIFESTATION_FLAME_BODY = ITEMS.register("manifestation_flame_body",
            () -> new LocalizedItem(new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> MANIFESTATION_SPIRIT_VEINS = ITEMS.register("manifestation_spirit_veins",
            () -> new LocalizedItem(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> MANIFESTATION_SOUL_MIRROR = ITEMS.register("manifestation_soul_mirror",
            () -> new LocalizedItem(new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> MANIFESTATION_SPIRIT_LAMP = ITEMS.register("manifestation_spirit_lamp",
            () -> new LocalizedItem(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> MANIFESTATION_SOUL_FORGE = ITEMS.register("manifestation_soul_forge",
            () -> new LocalizedItem(new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> MANIFESTATION_STAR_ARRAY = ITEMS.register("manifestation_star_array",
            () -> new LocalizedItem(new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> MANIFESTATION_DEMON_EYE = ITEMS.register("manifestation_demon_eye",
            () -> new LocalizedItem(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> MANIFESTATION_ANCIENT_SEAL = ITEMS.register("manifestation_ancient_seal",
            () -> new LocalizedItem(new Item.Properties().rarity(Rarity.RARE)));

    // Material tempa: satu ingot per mutu tool.
    public static final RegistryObject<Item> SPIRIT_IRON_INGOT = ITEMS.register("spirit_iron_ingot",
            () -> new LocalizedItem(new Item.Properties().rarity(Rarity.COMMON)));
    public static final RegistryObject<Item> SPIRIT_STEEL_INGOT = ITEMS.register("spirit_steel_ingot",
            () -> new LocalizedItem(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> DEEP_SILVER_INGOT = ITEMS.register("deep_silver_ingot",
            () -> new LocalizedItem(new Item.Properties().rarity(Rarity.RARE)));

    // Kristal soul: bahan upgrade tool, juga berfungsi sebagai template smithing.
    public static final RegistryObject<Item> SOUL_CRYSTAL_SHARD = ITEMS.register("soul_crystal_shard",
            () -> new LocalizedItem(new Item.Properties().rarity(Rarity.UNCOMMON)));

    // Consumable spirit: tiga herbal dan tiga pil.
    public static final RegistryObject<Item> SPIRIT_DEW_LEAF = ITEMS.register("spirit_dew_leaf",
            () -> new SpiritConsumableItem(SpiritConsumableItem.Effect.HEAL, 6,
                    new Item.Properties().stacksTo(16).rarity(Rarity.COMMON)));
    public static final RegistryObject<Item> SPIRIT_MOSS_SPROUT = ITEMS.register("spirit_moss_sprout",
            () -> new SpiritConsumableItem(SpiritConsumableItem.Effect.REGENERATION, 8,
                    new Item.Properties().stacksTo(16).rarity(Rarity.COMMON)));
    public static final RegistryObject<Item> SPIRIT_EMBER_LEAF = ITEMS.register("spirit_ember_leaf",
            () -> new SpiritConsumableItem(SpiritConsumableItem.Effect.SPIRIT_POWER, 25,
                    new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> CULTIVATION_PILL = ITEMS.register("cultivation_pill",
            () -> new SpiritConsumableItem(SpiritConsumableItem.Effect.CULTIVATION_XP, 120,
                    new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> SWIFT_PILL = ITEMS.register("swift_pill",
            () -> new SpiritConsumableItem(SpiritConsumableItem.Effect.SWIFT, 0,
                    new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON)));
    /** Pil Fondasi: menambah 25 poin fondasi kultivasi. */
    public static final RegistryObject<Item> FOUNDATION_PILL = ITEMS.register("foundation_pill",
            () -> new SpiritConsumableItem(SpiritConsumableItem.Effect.FOUNDATION, 25,
                    new Item.Properties().stacksTo(16).rarity(Rarity.RARE)));

    /** Item workstation alchemy, dipakai sebagai kunci crafting. */
    public static final RegistryObject<Item> ALCHEMY_STATION_ITEM = ITEMS.register("alchemy_station",
            () -> new BlockItem(com.soulland.block.ModBlocks.ALCHEMY_STATION.get(),
                    new Item.Properties().stacksTo(1)));

    /**
     * 30 tool soul: 10 arketipe x 3 mutu material. Id mengikuti pola
     * {@code <mutu>_<soul>_<tipe>} supaya mudah dibaca dan dicari.
     */
    public static final List<RegistryObject<SpiritToolItem>> SPIRIT_TOOLS = new ArrayList<>();

    static {
        for (SpiritToolType type : SpiritToolType.values()) {
            for (SpiritToolTier tier : SpiritToolTier.values()) {
                String id = tier.id() + "_" + type.soulId() + "_" + type.id();
                SPIRIT_TOOLS.add(ITEMS.register(id, () -> new SpiritToolItem(tier, type)));
            }
        }
    }

    /**
     * 35 item tulang spirit: 5 mutu umur x 7 slot tubuh. Id mengikuti pola
     * {@code spirit_bone_<mutu>_<slot>}.
     */
    public static final List<RegistryObject<SpiritBoneItem>> SPIRIT_BONES = new ArrayList<>();

    static {
        for (SpiritBoneTier tier : SpiritBoneTier.values()) {
            for (SpiritBoneSlot slot : SpiritBoneSlot.values()) {
                String id = boneId(tier, slot);
                SPIRIT_BONES.add(ITEMS.register(id, () -> new SpiritBoneItem(tier, slot,
                        new Item.Properties().stacksTo(16).rarity(boneRarity(tier)))));
            }
        }
    }

    /** Id registry item tulang untuk sebuah mutu dan slot. */
    public static String boneId(SpiritBoneTier tier, SpiritBoneSlot slot) {
        return "spirit_bone_" + tier.id() + "_" + slot.id();
    }

    /** Rarity item tulang mengikuti mutu tulangnya. */
    public static Rarity boneRarity(SpiritBoneTier tier) {
        return switch (tier) {
            case Y100 -> Rarity.COMMON;
            case Y1000 -> Rarity.UNCOMMON;
            case Y10000 -> Rarity.RARE;
            case Y100000 -> Rarity.EPIC;
            case Y1000000 -> Rarity.EPIC;
        };
    }

    /** Registry object tulang untuk sebuah mutu dan slot tertentu. */
    public static RegistryObject<SpiritBoneItem> bone(SpiritBoneTier tier, SpiritBoneSlot slot) {
        String id = boneId(tier, slot);
        for (RegistryObject<SpiritBoneItem> entry : SPIRIT_BONES) {
            if (entry.getId().equals(new ResourceLocation(SoulLandMod.MODID, id))) {
                return entry;
            }
        }
        return null;
    }

    /**
     * Stack item manifestasi untuk sebuah soul. Dikembalikan kosong bila soul
     * tidak punya item manifestasi (misalnya karena data soul berubah).
     */
    public static ItemStack manifestationStack(String soulId) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(SoulLandMod.MODID, "manifestation_" + soulId));
        return item == null ? ItemStack.EMPTY : new ItemStack(item);
    }

    /** Registry object tool untuk sebuah soul dan mutu tertentu. */
    public static RegistryObject<SpiritToolItem> tool(String soulId, SpiritToolTier tier) {
        SpiritToolType type = SpiritToolType.bySoul(soulId);
        if (type == null) return null;
        String id = tier.id() + "_" + soulId + "_" + type.id();
        for (RegistryObject<SpiritToolItem> entry : SPIRIT_TOOLS) {
            if (entry.getId().equals(new ResourceLocation(SoulLandMod.MODID, id))) {
                return entry;
            }
        }
        return null;
    }
}
