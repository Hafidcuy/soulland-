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
}
