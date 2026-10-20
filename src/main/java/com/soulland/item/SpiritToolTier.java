package com.soulland.item;

import com.soulland.SoulLandMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.TierSortingRegistry;

import java.util.List;

/**
 * Tiga mutu material tool soul. Setiap mutu menaikkan damage, kecepatan
 * tambang, dan ketahanan tool yang dibentuk darinya.
 *
 * <p>Urutan mutu: Spirit Iron -&gt; Spirit Steel -&gt; Deep Silver.
 */
public enum SpiritToolTier implements Tier {
    SPIRIT_IRON("spirit_iron", 260, 5.0f, 2, 1, 12, Items.IRON_INGOT),
    SPIRIT_STEEL("spirit_steel", 520, 7.5f, 3, 2, 16, Items.IRON_INGOT),
    DEEP_SILVER("deep_silver", 900, 10.5f, 4, 3, 20, Items.DIAMOND);

    private final String id;
    private final int uses;
    private final float speed;
    private final int level;
    private final int attackDamageBonus;
    private final int enchantmentValue;
    private final Ingredient repairIngredient;

    SpiritToolTier(String id, int uses, float speed, int level, int attackDamageBonus,
                   int enchantmentValue, Item repairItem) {
        this.id = id;
        this.uses = uses;
        this.speed = speed;
        this.level = level;
        this.attackDamageBonus = attackDamageBonus;
        this.enchantmentValue = enchantmentValue;
        this.repairIngredient = Ingredient.of(repairItem);
    }

    public String id() {
        return id;
    }

    public ResourceLocation key() {
        return new ResourceLocation(SoulLandMod.MODID, id);
    }

    /** Mutu berikutnya untuk upgrade smithing, null bila sudah paling atas. */
    public SpiritToolTier next() {
        return switch (this) {
            case SPIRIT_IRON -> SPIRIT_STEEL;
            case SPIRIT_STEEL -> DEEP_SILVER;
            case DEEP_SILVER -> null;
        };
    }

    public static SpiritToolTier byId(String id) {
        for (SpiritToolTier tier : values()) {
            if (tier.id.equals(id)) return tier;
        }
        return null;
    }

    @Override public int getUses() { return uses; }
    @Override public float getSpeed() { return speed; }
    @Override public float getAttackDamageBonus() { return attackDamageBonus; }
    @Override public int getLevel() { return level; }
    @Override public int getEnchantmentValue() { return enchantmentValue; }
    @Override public Ingredient getRepairIngredient() { return repairIngredient; }

    /**
     * Tag blok yang bisa ditambang tool mutu ini. Wajib diisi: Forge memakai
     * tag tier berikutnya saat membandingkan tool dengan blok, jadi tag null
     * akan membuat toolwelt atau gagal saat menambang.
     */
    @Override public TagKey<Block> getTag() { return BlockTags.MINEABLE_WITH_PICKAXE; }

    /**
     * Mendaftarkan urutan mutu ke Forge supaya level tool bisa dibandingkan
     * dengan block (blok dengan tier lebih tinggi tidak bisa di-drop tool ini).
     */
    public static void registerSorting() {
        TierSortingRegistry.registerTier(SPIRIT_IRON, SPIRIT_IRON.key(),
                List.of(Tiers.NETHERITE), List.of());
        TierSortingRegistry.registerTier(SPIRIT_STEEL, SPIRIT_STEEL.key(),
                List.of(SPIRIT_IRON), List.of());
        TierSortingRegistry.registerTier(DEEP_SILVER, DEEP_SILVER.key(),
                List.of(SPIRIT_STEEL), List.of());
    }
}
