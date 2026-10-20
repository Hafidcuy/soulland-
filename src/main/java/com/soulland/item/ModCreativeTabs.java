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
                        output.accept(ModItems.SOUL_CARD.get());
                        output.accept(ModItems.SOUL_ESSENCE.get());
                        output.accept(ModItems.EVOLUTION_CORE.get());
                        output.accept(ModItems.SECT_TOKEN.get());
                        output.accept(ModItems.MEDITATION_INCENSE.get());
                        output.accept(ModItems.MARTIAL_SOUL_MANUAL.get());
                        output.accept(ModItems.SOUL_CORE.get());
                        output.accept(ModItems.SECT_MEDALLION_TANG.get());
                        output.accept(ModItems.SECT_MEDALLION_SHREK.get());
                        output.accept(ModItems.SECT_MEDALLION_STARLUO.get());
                        output.accept(ModItems.SECT_MEDALLION_QIBAO.get());
                        output.accept(ModItems.SOUL_GUIDE.get());
                        output.accept(ModItems.QI_PILL.get());
                        output.accept(ModItems.SOUL_CRYSTAL.get());
                        output.accept(ModItems.RING_FORGE.get());
                        output.accept(ModItems.HUNT_CONTRACT.get());
                    })
                    .build());
}
