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
                    })
                    .build());
}
