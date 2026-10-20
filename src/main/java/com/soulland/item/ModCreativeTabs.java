package com.soulland.item;

import com.soulland.SoulLandMod;
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
                    })
                    .build());
}
