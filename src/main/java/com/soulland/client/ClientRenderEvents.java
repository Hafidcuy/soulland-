package com.soulland.client;

import com.soulland.SoulLandMod;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Hook render sisi client: menggambar halo spirit ring dan manifestasi martial
 * soul untuk setiap pemain yang sedang dirender.
 */
@Mod.EventBusSubscriber(modid = SoulLandMod.MODID, value = Dist.CLIENT)
public class ClientRenderEvents {

    @SubscribeEvent
    public static void onRenderPlayerPost(RenderPlayerEvent.Post event) {
        Player player = event.getEntity();
        HaloRenderer.render(player, event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight(), event.getPartialTick());
        ManifestationRenderer.render(player, event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight());
    }
}
