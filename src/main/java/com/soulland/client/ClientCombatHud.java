package com.soulland.client;

import com.soulland.combat.TrueBodyRules;
import com.soulland.lang.SoulLang;
import com.soulland.soul.MartialSoul;
import com.soulland.soul.MartialSouls;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/**
 * Baris status True Body dan Domain di HUD.
 *
 * <p>Setiap baris punya ikon pixel art milik soul pemain, jadi bentuk ikon
 * langsung memberi tahu True Body dan Domain mana yang sedang aktif.
 * Teksnya diletakkan di atas bidang tinta supaya kontrasnya tetap terbaca
 * di atas langit terang maupun arena gelap.
 */
public final class ClientCombatHud {
    private ClientCombatHud() {}

    /** Warna teks status aktif: jade, warna energi di DESIGN.md. */
    private static final int ACTIVE = 0x4FC08D;

    /** Warna teks status biasa: bisu, warna teks sekunder di DESIGN.md. */
    private static final int MUTED = 0x98A2AD;

    /** Warna teks penolakan: danger, dipakai kalau syarat belum terpenuhi. */
    private static final int DANGER = 0xE0796A;

    /** Ukuran ikon di HUD, lebih kecil dari portret 16x16. */
    private static final int ICON_SIZE = 12;

    /** tinggi satu baris status. */
    private static final int ROW_HEIGHT = 14;

    /** Gambar blok status True Body dan Domain. */
    public static void render(GuiGraphics gui, int x, int y) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        String soulId = ClientCultivationData.soulId;
        MartialSoul soul = MartialSouls.effective(soulId, ClientCultivationData.evolutionStage);
        if (soul == null) return;

        String language = ClientCultivationData.effectiveLanguage();
        ClientCombatData.State state = ClientCombatData.local();
        long gameTime = mc.level.getGameTime();

        drawRow(gui, icon("true_body", soul.id()),
                trueBodyText(language, soul, state, gameTime), x, y, trueBodyColor(state, gameTime));
        drawRow(gui, icon("domain", soul.id()),
                domainText(language, soul, state), x, y + ROW_HEIGHT, domainColor(state));
    }

    private static String trueBodyText(String language, MartialSoul soul,
                                       ClientCombatData.State state, long gameTime) {
        String label = SoulLang.translate(language, "gui.soulland.hud.truebody", "True Body");
        String name = SoulLang.translate(language, "soul.soulland.truebody." + soul.id() + ".name", soul.id());
        if (ClientCultivationData.rings < TrueBodyRules.UNLOCK_RINGS) {
            return label + ": " + name + " - " + SoulLang.translate(language,
                    "gui.soulland.hud.truebody.locked", "butuh %s ring")
                    .replace("%s", String.valueOf(TrueBodyRules.UNLOCK_RINGS));
        }
        if (state.trueBodyActive(gameTime)) {
            return label + ": " + name + " - " + SoulLang.translate(language,
                    "gui.soulland.hud.truebody.active", "aktif %s dtk")
                    .replace("%s", String.valueOf(state.trueBodySeconds(gameTime)));
        }
        int cooldown = state.cooldownSeconds(gameTime);
        if (cooldown > 0) {
            return label + ": " + name + " - " + SoulLang.translate(language,
                    "gui.soulland.hud.truebody.cooldown", "cooldown %s dtk")
                    .replace("%s", String.valueOf(cooldown));
        }
        return label + ": " + name + " - " + SoulLang.translate(language,
                "gui.soulland.hud.truebody.ready", "siap");
    }

    private static String domainText(String language, MartialSoul soul, ClientCombatData.State state) {
        String label = SoulLang.translate(language, "gui.soulland.hud.domain", "Domain");
        String name = SoulLang.translate(language, "soul.soulland.domain." + soul.id() + ".name", soul.id());
        if (!state.domainActive()) {
            return label + ": " + name + " - " + SoulLang.translate(language,
                    "gui.soulland.hud.domain.off", "mati");
        }
        return label + ": " + name + " - " + SoulLang.translate(language,
                "gui.soulland.hud.domain.active", "aktif %s SP/dtk")
                .replace("%s", String.valueOf(state.domainDrainPerSecond()));
    }

    private static int trueBodyColor(ClientCombatData.State state, long gameTime) {
        if (state.trueBodyActive(gameTime)) return ACTIVE;
        if (state.cooldownSeconds(gameTime) > 0) return MUTED;
        if (ClientCultivationData.rings < TrueBodyRules.UNLOCK_RINGS) return DANGER;
        return MUTED;
    }

    private static int domainColor(ClientCombatData.State state) {
        return state.domainActive() ? ACTIVE : MUTED;
    }

    private static ResourceLocation icon(String kind, String soulId) {
        return new ResourceLocation("soulland", "textures/gui/" + kind + "_" + soulId);
    }

    private static void drawRow(GuiGraphics gui, ResourceLocation icon, String text, int x, int y, int color) {
        Minecraft mc = Minecraft.getInstance();
        gui.fill(x, y, x + ICON_SIZE, y + ICON_SIZE, 0xB014161A);
        gui.blit(icon, x, y, 0, 0, ICON_SIZE, ICON_SIZE, 16, 16);

        int textX = x + ICON_SIZE + 2;
        int width = mc.font.width(text);
        gui.fill(textX - 1, y, textX + width + 1, y + 11, 0xB014161A);
        gui.drawString(mc.font, text, textX, y + 1, color);
    }
}
