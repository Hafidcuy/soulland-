package com.soulland.item;

import com.soulland.SoulLandMod;
import net.minecraft.world.item.Item;
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

    public static final RegistryObject<SoulCardItem> SOUL_CARD = ITEMS.register("soul_card",
            () -> new SoulCardItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> SOUL_ESSENCE = ITEMS.register("soul_essence",
            () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<EvolutionCoreItem> EVOLUTION_CORE = ITEMS.register("evolution_core",
            () -> new EvolutionCoreItem(new Item.Properties().stacksTo(16).rarity(Rarity.RARE)));

    public static final RegistryObject<SectTokenItem> SECT_TOKEN = ITEMS.register("sect_token",
            () -> new SectTokenItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<MeditationIncenseItem> MEDITATION_INCENSE = ITEMS.register("meditation_incense",
            () -> new MeditationIncenseItem(new Item.Properties().stacksTo(16)));

    public static final RegistryObject<SoulCardItem> MARTIAL_SOUL_MANUAL = ITEMS.register("martial_soul_manual",
            () -> new SoulCardItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> SOUL_CORE = ITEMS.register("soul_core",
            () -> new Item(new Item.Properties().rarity(Rarity.RARE)));

    public static final RegistryObject<SectMedallionItem> SECT_MEDALLION_TANG = ITEMS.register("sect_medallion_tang",
            () -> new SectMedallionItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON), "tang"));
    public static final RegistryObject<SectMedallionItem> SECT_MEDALLION_SHREK = ITEMS.register("sect_medallion_shrek",
            () -> new SectMedallionItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON), "shrek"));
    public static final RegistryObject<SectMedallionItem> SECT_MEDALLION_STARLUO = ITEMS.register("sect_medallion_starluo",
            () -> new SectMedallionItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON), "starluo"));
    public static final RegistryObject<SectMedallionItem> SECT_MEDALLION_QIBAO = ITEMS.register("sect_medallion_qibao",
            () -> new SectMedallionItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON), "qibao"));
}
