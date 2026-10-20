package com.soulland.client;

import com.soulland.CultivationRanks;
import com.soulland.SoulLandMod;
import com.soulland.net.ActivateSkillPacket;
import com.soulland.net.ModNetwork;
import com.soulland.soul.MartialSoul;
import com.soulland.soul.MartialSouls;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SoulLandMod.MODID, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;
        if (KeyBindings.SKILL_1 != null && KeyBindings.SKILL_1.consumeClick()) {
            ModNetwork.CHANNEL.sendToServer(new ActivateSkillPacket(0));
        }
        if (KeyBindings.SKILL_2 != null && KeyBindings.SKILL_2.consumeClick()) {
            ModNetwork.CHANNEL.sendToServer(new ActivateSkillPacket(1));
        }
    }

    @Mod.EventBusSubscriber(modid = SoulLandMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class OverlayRegistrar {
        @SubscribeEvent
        public static void registerOverlays(RegisterGuiOverlaysEvent event) {
            event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "soulland_hud",
                    (forgeGui, guiGraphics, partialTick, screenWidth, screenHeight) ->
                            renderHud(guiGraphics));
        }

        private static void renderHud(GuiGraphics gui) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.options.hideGui) return;

            MartialSoul soul = MartialSouls.get(ClientCultivationData.soulId);
            String soulName = soul == null ? "" : soul.name();

            int x = 6, y = 6;
            gui.drawString(mc.font, "Lv." + ClientCultivationData.level + " " + CultivationRanks.rankName(ClientCultivationData.level), x, y, 0xFFD966);
            gui.drawString(mc.font, "Ring: " + ClientCultivationData.rings + "/9" + (soulName.isEmpty() ? "" : " | " + soulName), x, y + 10, 0xFF9DFF);
            int barW = 80;
            int filled = (int) (barW * (ClientCultivationData.energy / 100f));
            gui.fill(x, y + 21, x + barW, y + 26, 0x80000000);
            gui.fill(x, y + 21, x + filled, y + 26, 0xFF33AAFF);
            gui.drawString(mc.font, "SP " + (int) ClientCultivationData.energy + "/100", x + barW + 4, y + 20, 0x99CCFF);
        }
    }
}
