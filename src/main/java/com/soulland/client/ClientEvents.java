package com.soulland.client;

import com.soulland.CultivationRanks;
import com.soulland.SoulLandMod;
import com.soulland.lang.SoulLang;
import com.soulland.net.ActivateSkillPacket;
import com.soulland.net.BloodBurnPacket;
import com.soulland.net.MeditatePacket;
import com.soulland.net.ModNetwork;
import com.soulland.net.RingBurstPacket;
import com.soulland.net.ToggleSoulPacket;
import com.soulland.soul.MartialSoul;
import com.soulland.soul.MartialSouls;
import com.soulland.soul.SkillDef;
import com.soulland.soul.SkillTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SoulLandMod.MODID, value = Dist.CLIENT)
public class ClientEvents {

    private static final int SOUL_JADE = 0x4FC08D;
    private static final int IMPERIAL_GOLD = 0xE8C46A;
    private static final int INK = 0x14161A;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        ClientFx.tick();

        if (mc.screen != null) return;

        if (KeyBindings.SKILL_1 != null && KeyBindings.SKILL_1.consumeClick()) {
            ModNetwork.CHANNEL.sendToServer(new ActivateSkillPacket(0));
        }
        if (KeyBindings.SKILL_2 != null && KeyBindings.SKILL_2.consumeClick()) {
            ModNetwork.CHANNEL.sendToServer(new ActivateSkillPacket(1));
        }
        if (KeyBindings.SKILL_3 != null && KeyBindings.SKILL_3.consumeClick()) {
            ModNetwork.CHANNEL.sendToServer(new ActivateSkillPacket(2));
        }
        if (KeyBindings.SKILL_4 != null && KeyBindings.SKILL_4.consumeClick()) {
            ModNetwork.CHANNEL.sendToServer(new ActivateSkillPacket(3));
        }
        if (KeyBindings.TRUE_BODY != null && KeyBindings.TRUE_BODY.consumeClick()) {
            ModNetwork.CHANNEL.sendToServer(new com.soulland.net.ActivateTrueBodyPacket());
        }
        if (KeyBindings.DOMAIN != null && KeyBindings.DOMAIN.consumeClick()) {
            ModNetwork.CHANNEL.sendToServer(new com.soulland.net.ToggleDomainPacket());
        }
    }

    /** Membersihkan data visual yang sudah tidak berlaku agar tidak menumpuk. */
    @SubscribeEvent
    public static void onClientCleanup(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        ClientManifestations.removeExpired(mc.level.getGameTime());
        ClientHaloData.prune(id -> mc.level.getEntity(id) != null);
        ClientCombatData.prune(id -> mc.level.getEntity(id) != null);
    }

    @Mod.EventBusSubscriber(modid = SoulLandMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class OverlayRegistrar {
        @SubscribeEvent
        public static void registerOverlays(RegisterGuiOverlaysEvent event) {
            event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "soulland_hud",
                    (forgeGui, guiGraphics, partialTick, screenWidth, screenHeight) -> renderHud(guiGraphics));
        }

        private static void renderHud(GuiGraphics gui) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.options.hideGui) return;

            String language = ClientCultivationData.effectiveLanguage();
            MartialSoul soul = MartialSouls.effective(ClientCultivationData.soulId, ClientCultivationData.evolutionStage);
            int x = 6;
            int y = 6;

            drawPortrait(gui, soul, x, y);

            String rank = SoulLang.rankName(ClientCultivationData.level, language).getString();
            gui.drawString(mc.font, SoulLang.text(language, "gui.soulland.hud.level", "Lv.%s  %s",
                    ClientCultivationData.level, rank), x + 21, y, IMPERIAL_GOLD);
            String ringText = SoulLang.text(language, "gui.soulland.hud.rings", "Ring %s/%s",
                    ClientCultivationData.rings, CultivationRanks.MAX_RINGS).getString();
            if (!ClientCultivationData.sect.isEmpty()) {
                ringText = ringText + "  " + ClientCultivationData.sect;
            }
            gui.drawString(mc.font, ringText, x + 21, y + 10, 0xB8D8C8);

            int foundationY = y + 20;
            if (ClientCultivationData.foundation > 0
                    || CultivationRanks.isGateLevel(ClientCultivationData.level)) {
                gui.drawString(mc.font, SoulLang.text(language, "gui.soulland.hud.foundation",
                                "Fondasi %s%%", ClientCultivationData.foundation).getString(),
                        x + 21, foundationY, 0x8FD8C8);
                foundationY += 10;
            }

            drawSkillBar(gui, soul, x, y + 24);
            drawEnergy(gui, mc, x, y + 52);
            ClientCombatHud.render(gui, x, y + 70);

            if (ClientCultivationData.chainCount > 0) {
                int bonus = (int) (com.soulland.skill.SkillHandler.CHAIN_STEP
                        * Math.min(com.soulland.skill.SkillHandler.MAX_CHAIN, ClientCultivationData.chainCount) * 100);
                gui.drawString(mc.font, SoulLang.text(language, "gui.soulland.hud.chain",
                                "Rantai %s (+%s%% damage)", ClientCultivationData.chainCount, bonus).getString(),
                        x, y + 100, 0xFFD24A);
            }
        }

        private static void drawPortrait(GuiGraphics gui, MartialSoul soul, int x, int y) {
            if (soul == null) {
                gui.fill(x, y, x + 16, y + 16, 0xC0101418);
                gui.fill(x, y, x + 16, y + 16, 0x60FFFFFF);
                return;
            }
            ResourceLocation portrait = new ResourceLocation("soulland", "textures/soul/" + soul.id() + ".png");
            gui.blit(portrait, x, y, 0, 0, 16, 16, 16, 16);
        }

        private static void drawSkillBar(GuiGraphics gui, MartialSoul soul, int x, int y) {
            Minecraft mc = Minecraft.getInstance();
            int slotSize = 20;
            int gap = 2;
            for (int slot = 0; slot < 5; slot++) {
                int left = x + slot * (slotSize + gap);
                SkillDef skill = ClientSkills.resolve(soul, slot);
                boolean unlocked = ClientSkills.isUnlocked(skill);

                gui.fill(left, y, left + slotSize, y + slotSize, 0xB014161A);

                if (skill != null) {
                    int color = skillColor(skill);
                    gui.fill(left, y, left + slotSize, y + slotSize, unlocked ? color & 0x50FFFFFF : 0x20FFFFFF);
                    gui.fill(left, y, left + slotSize, y + 1, unlocked ? color : 0x40FFFFFF);
                    gui.fill(left, y + slotSize - 1, left + slotSize, y + slotSize, unlocked ? color : 0x40FFFFFF);

                    if (unlocked) {
                        int remaining = ClientCultivationData.cooldowns.getOrDefault(skill.id(), 0);
                        int total = Math.max(1, skill.cooldownTicks() / 20);
                        if (remaining > 0) {
                            int height = (int) (slotSize * (1f - Math.min(1f, remaining / (float) total)));
                            gui.fill(left, y, left + slotSize, y + height, 0xA0000000);
                            gui.drawString(mc.font, remaining + "s", left + 3, y + 6, 0xFFFFFF);
                        }
                    } else {
                        gui.drawString(mc.font, "X", left + 7, y + 6, 0x707070);
                    }
                }
                gui.drawString(mc.font, String.valueOf(slot + 1), left + 2, y + 2, 0xFFFFFF);
            }
        }

        private static int skillColor(SkillDef skill) {
            return switch (skill.target()) {
                case SELF -> 0x4FC08D;
                case ENEMY_NEAREST, ENEMY_AOE, ENEMY_LINE, ENEMY_CONE, POINT_AOE -> 0xD8442F;
                case ALLY_AURA -> 0xE8C46A;
            };
        }

        private static void drawEnergy(GuiGraphics gui, Minecraft mc, int x, int y) {
            int width = 110;
            int height = 5;
            gui.fill(x, y, x + width, y + height, 0xB014161A);
            int cap = Math.max(1, ClientCultivationData.energyCap);
            int filled = (int) (width * (ClientCultivationData.energy / cap));
            if (filled > 0) {
                gui.fill(x, y, x + filled, y + height, SOUL_JADE);
            }
            String language = ClientCultivationData.effectiveLanguage();
            gui.drawString(mc.font, SoulLang.text(language, "gui.soulland.hud.energy", "SP %s/%s",
                    (int) ClientCultivationData.energy, cap).getString(),
                    x, y + 8, 0x9AD8C8);
        }
    }
}
