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
