package com.soulland;

import com.soulland.command.SoulCommands;
import com.soulland.item.ModCreativeTabs;
import com.soulland.item.ModItems;
import com.soulland.net.ModNetwork;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(SoulLandMod.MODID)
public class SoulLandMod {
    public static final String MODID = "soulland";
    public static final Logger LOGGER = LogManager.getLogger();

    public SoulLandMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModItems.ITEMS.register(modBus);
        ModCreativeTabs.TABS.register(modBus);
        com.soulland.entity.ModEntities.ENTITIES.register(modBus);
        modBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(ModNetwork::register);
        LOGGER.info("Soul Land: Douluo Dalu mod dimuat. Selamat datang di Benua Douluo!");
    }

    private void registerCommands(final RegisterCommandsEvent event) {
        SoulCommands.register(event.getDispatcher());
    }
}
