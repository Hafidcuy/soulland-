package com.soulland.client.render;

import com.soulland.SoulLandMod;
import com.soulland.entity.ModEntities;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Pendaftaran renderer kustom mod ini. */
@Mod.EventBusSubscriber(modid = SoulLandMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientRenderSetup {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.SOUL_VFX.get(), SoulVfxEntityRenderer::new);
        event.registerEntityRenderer(ModEntities.MARTIAL_SOUL.get(), MartialSoulEntityRenderer::new);
    }
}
