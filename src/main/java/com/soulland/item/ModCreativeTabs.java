package com.soulland.item;

import com.soulland.SoulLandMod;
import com.soulland.bone.SpiritBoneItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SoulLandMod.MODID);

    public static final RegistryObject<CreativeModeTab> SOULLAND_TAB = TABS.register("soulland_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.soulland"))
                    .icon(() -> new ItemStack(ModItems.SPIRIT_RING_100000.get()))
                    .displayItems((params, output) -> {
                        output.accept(ModItems.SPIRIT_RING_100.get());
                        output.accept(ModItems.SPIRIT_RING_1000.get());
                        output.accept(ModItems.SPIRIT_RING_10000.get());
                        output.accept(ModItems.SPIRIT_RING_100000.get());
                        output.accept(ModItems.SPIRIT_RING_1000000.get());
                        output.accept(ModItems.MANIFESTATION_HOE.get());
                        output.accept(ModItems.MANIFESTATION_BLUE_SILVER_GRASS.get());
                        output.accept(ModItems.MANIFESTATION_HELL_CIVET.get());
                        output.accept(ModItems.MANIFESTATION_SAUSAGE.get());
                        output.accept(ModItems.MANIFESTATION_SEVEN_TREASURE_PAGODA.get());
                        output.accept(ModItems.MANIFESTATION_EVIL_FIRE_PHOENIX.get());
                        output.accept(ModItems.MANIFESTATION_WHITE_TIGER.get());
                        output.accept(ModItems.MANIFESTATION_SOFT_BONE_RABBIT.get());
                        output.accept(ModItems.MANIFESTATION_JADE_SERPENT.get());
                        output.accept(ModItems.MANIFESTATION_CLEAR_SKY_HAMMER.get());
                        output.accept(ModItems.MANIFESTATION_GOLDEN_DRAGON.get());
                        output.accept(ModItems.MANIFESTATION_ABYSSAL_DEMON_DRAGON.get());
                        output.accept(ModItems.MANIFESTATION_ABYSSAL_ICE_DEMON_DRAGON.get());
                        output.accept(ModItems.MANIFESTATION_ULTIMATE_FIRE_DRAGON.get());
                        output.accept(ModItems.MANIFESTATION_ULTIMATE_ICE_DRAGON.get());
                        output.accept(ModItems.MANIFESTATION_THUNDER_ELEPHANT.get());
                        output.accept(ModItems.MANIFESTATION_GOLDEN_ANT.get());
                        output.accept(ModItems.MANIFESTATION_TITAN_GIANT_APE.get());
                        output.accept(ModItems.MANIFESTATION_SEA_DEVIL_MANATEE.get());
                        output.accept(ModItems.MANIFESTATION_BRIGHT_WING_MOTH.get());
                        output.accept(ModItems.MANIFESTATION_PHOENIX.get());
                        output.accept(ModItems.MANIFESTATION_BLUE_SILVER_SPEAR.get());
                        output.accept(ModItems.MANIFESTATION_SEVEN_KILL_SWORD.get());
                        output.accept(ModItems.MANIFESTATION_GIANT_HAMMER.get());
                        output.accept(ModItems.MANIFESTATION_BLOOD_LASH.get());
                        output.accept(ModItems.MANIFESTATION_FROST_NEEDLE.get());
                        output.accept(ModItems.MANIFESTATION_THUNDER_SPEAR.get());
                        output.accept(ModItems.MANIFESTATION_SPIRIT_SWORD.get());
                        output.accept(ModItems.MANIFESTATION_IRON_ANCHOR.get());
                        output.accept(ModItems.MANIFESTATION_BRONZE_MIRROR.get());
                        output.accept(ModItems.MANIFESTATION_BLUE_SILVER_EMPEROR.get());
                        output.accept(ModItems.MANIFESTATION_IRON_BODY.get());
                        output.accept(ModItems.MANIFESTATION_BONE_SHELL.get());
                        output.accept(ModItems.MANIFESTATION_STONE_SKIN.get());
                        output.accept(ModItems.MANIFESTATION_FLESH_REGROWTH.get());
                        output.accept(ModItems.MANIFESTATION_FLAME_BODY.get());
                        output.accept(ModItems.MANIFESTATION_SPIRIT_VEINS.get());
                        output.accept(ModItems.MANIFESTATION_SOUL_MIRROR.get());
                        output.accept(ModItems.MANIFESTATION_SPIRIT_LAMP.get());
                        output.accept(ModItems.MANIFESTATION_SOUL_FORGE.get());
                        output.accept(ModItems.MANIFESTATION_STAR_ARRAY.get());
                        output.accept(ModItems.MANIFESTATION_DEMON_EYE.get());
                        output.accept(ModItems.MANIFESTATION_ANCIENT_SEAL.get());
                        output.accept(ModItems.SPIRIT_IRON_INGOT.get());
                        output.accept(ModItems.SPIRIT_STEEL_INGOT.get());
                        output.accept(ModItems.DEEP_SILVER_INGOT.get());
                        output.accept(ModItems.SOUL_CRYSTAL_SHARD.get());
                        output.accept(ModItems.SPIRIT_DEW_LEAF.get());
                        output.accept(ModItems.SPIRIT_MOSS_SPROUT.get());
                        output.accept(ModItems.SPIRIT_EMBER_LEAF.get());
                        output.accept(ModItems.CULTIVATION_PILL.get());
                        output.accept(ModItems.SWIFT_PILL.get());
                        output.accept(ModItems.FOUNDATION_PILL.get());
                        output.accept(ModItems.ALCHEMY_STATION_ITEM.get());
                    })
                    .build());

    /** Tab khusus tool soul, dikelompokkan per soul lalu per mutu material. */
    public static final RegistryObject<CreativeModeTab> SPIRIT_GEAR_TAB = TABS.register("soul_spirit_gear",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.soulland.spirit_gear"))
                    .icon(() -> new ItemStack(ModItems.SPIRIT_TOOLS.get(0).get()))
                    .displayItems((params, output) -> {
                        for (SpiritToolType type : SpiritToolType.values()) {
                            for (SpiritToolTier tier : SpiritToolTier.values()) {
                                RegistryObject<SpiritToolItem> tool = ModItems.tool(type.soulId(), tier);
                                if (tool != null) output.accept(tool.get());
                            }
                        }
                    })
                    .build());

    /** Tab khusus tulang spirit, dikelompokkan per mutu umur lalu per slot tubuh. */
    public static final RegistryObject<CreativeModeTab> SPIRIT_BONE_TAB = TABS.register("soul_spirit_bone",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.soulland.spirit_bone"))
                    .icon(() -> new ItemStack(ModItems.SPIRIT_BONES.get(0).get()))
                    .displayItems((params, output) -> {
                        for (RegistryObject<SpiritBoneItem> bone : ModItems.SPIRIT_BONES) {
                            output.accept(bone.get());
                        }
                    })
                    .build());
}
