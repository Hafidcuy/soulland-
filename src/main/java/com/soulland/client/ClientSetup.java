package com.soulland.client;

import com.soulland.SoulLandMod;
import com.soulland.net.ClientBridge;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Mendaftarkan jembatan client saat client setup.
 * Hanya sisi client yang menyentuh kelas ini.
 */
@Mod.EventBusSubscriber(modid = SoulLandMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ClientBridge.set(new ClientBridgeImpl()));
    }
}
