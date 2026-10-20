package com.soulland.screen;

import com.soulland.lang.SoulLang;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

/**
 * Panduan Soul Land dalam bentuk tampilan sendiri.
 *
 * <p>Tujuh bagian, tiap bagian punya ikon pixel art dan teks yang bisa dibaca. Daftar bagian
 * bisa diklik, dan bisa juga dinavigasi dengan keyboard supaya tidak hanya untuk mouse.
 *
 * <p>Seluruh teksnya diambil dari file bahasa mod memakai bahasa yang dipilih
 * pemain, bukan bahasa client Minecraft.
 */
@OnlyIn(Dist.CLIENT)
public class GuideScreen extends Screen {

    private static final int INK = 0x14161A;
    private static final int SOUL_JADE = 0x4FC08D;
    private static final int IMPERIAL_GOLD = 0xE8C46A;
    private static final int PAPER = 0xE8E0CC;

    private static final String[] ICONS = {
            "martial_soul_manual", "soul_card", "spirit_ring_100000",
            "evolution_core", "meditation_incense", "sect_token", "soul_core"
    };

    /** Kunci bahasa untuk judul tiap bagian. */
    private static final String[] TITLE_KEYS = {
            "guide.soulland.title.1", "guide.soulland.title.2", "guide.soulland.title.3",
            "guide.soulland.title.4", "guide.soulland.title.5", "guide.soulland.title.6",
            "guide.soulland.title.7"
    };

    /** Kunci bahasa untuk isi tiap bagian. */
    private static final String[] BODY_KEYS = {
            "guide.soulland.section.1", "guide.soulland.section.2", "guide.soulland.section.3",
            "guide.soulland.section.4", "guide.soulland.section.5", "guide.soulland.section.6",
            "guide.soulland.section.7"
    };

    /** Kunci bahasa untuk tip tiap bagian. */
    private static final String[] TIP_KEYS = {
            "guide.soulland.tip.1", "guide.soulland.tip.2", "guide.soulland.tip.3",
            "guide.soulland.tip.4", "guide.soulland.tip.5", "guide.soulland.tip.6",
            "guide.soulland.tip.7"
    };

    private static final ResourceLocation PANEL =
            new ResourceLocation("soulland", "textures/gui/panel_guide.png");

    private final String language;
    private int activeSection = 0;
    private int panelLeft;
    private int panelTop;
    private int panelWidth;
    private int panelHeight;
    private final List<int[]> navBounds = new ArrayList<>();

    public GuideScreen() {
        super(Component.literal("Panduan Soul Land"));
        this.language = SoulLang.activeLanguage();
    }

    private String title(int index) {
        return SoulLang.translate(language, TITLE_KEYS[index], TITLE_KEYS[index]);
    }

    @Override
    protected void init() {
        panelWidth = Math.min(400, this.width - 20);
        panelHeight = Math.min(210, this.height - 20);
        panelLeft = (this.width - panelWidth) / 2;
        panelTop = (this.height - panelHeight) / 2;
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        renderBackground(gui);
        gui.blit(PANEL, panelLeft, panelTop, 0, 0, panelWidth, panelHeight, panelWidth, panelHeight);
        gui.fill(panelLeft + 4, panelTop + 4, panelLeft + panelWidth - 4, panelTop + 5, 0x50E8C46A);

        drawNavigation(gui, mouseX, mouseY);
        drawContent(gui);
    }

    private void drawNavigation(GuiGraphics gui, int mouseX, int mouseY) {
        navBounds.clear();
        int x = panelLeft + 12;
        int y = panelTop + 16;
        gui.drawString(this.font, SoulLang.translate(language, "guide.soulland.header", "PANDUAN"), x, y - 10, IMPERIAL_GOLD);
        for (int i = 0; i < TITLE_KEYS.length; i++) {
            int top = y + i * 24;
            boolean active = i == activeSection;
            boolean hovered = mouseX >= x - 2 && mouseX <= x + 128 && mouseY >= top && mouseY <= top + 18;
            ResourceLocation icon = new ResourceLocation("soulland", "textures/item/" + ICONS[i] + ".png");
            gui.blit(icon, x, top, 0, 0, 16, 16, 16, 16);
            String label = title(i);
            gui.drawString(this.font, label, x + 20, top + 4, active ? PAPER : (hovered ? 0xFFFFFF : 0x909090));
            if (active) {
                gui.fill(x + 20, top + 14, x + 20 + this.font.width(label), top + 15, SOUL_JADE);
            }
            navBounds.add(new int[]{x - 2, top, x + 130, top + 18});
        }
    }

    private void drawContent(GuiGraphics gui) {
        int x = panelLeft + 150;
        int y = panelTop + 16;
        int width = panelWidth - 162;
        String heading = title(activeSection);
        gui.drawString(this.font, heading, x, y, PAPER);
        gui.fill(x, y + 11, x + this.font.width(heading), y + 12, IMPERIAL_GOLD);
        y += 20;

        for (String paragraph : SoulLang.translate(language, BODY_KEYS[activeSection], "").split("\n", -1)) {
            if (paragraph.isEmpty()) {
                y += 6;
                continue;
            }
            for (FormattedCharSequence wrapped : this.font.split(Component.literal(paragraph), width)) {
                gui.drawString(this.font, wrapped, x, y, 0xC8C0A8);
                y += 10;
            }
        }

        String tip = SoulLang.translate(language, TIP_KEYS[activeSection], "");
        if (!tip.isEmpty() && y + 22 < panelTop + panelHeight) {
            y += 4;
            gui.fill(x, y, x + width, y + 1, 0x40E8C46A);
            y += 4;
            for (FormattedCharSequence wrapped : this.font.split(Component.literal(tip), width)) {
                if (y + 10 > panelTop + panelHeight) break;
                gui.drawString(this.font, wrapped, x, y, 0xE8C46A);
                y += 10;
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int mx = (int) mouseX;
        int my = (int) mouseY;
        for (int i = 0; i < navBounds.size(); i++) {
            int[] bounds = navBounds.get(i);
            if (mx >= bounds[0] && mx <= bounds[2] && my >= bounds[1] && my <= bounds[3]) {
                activeSection = i;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_DOWN) {
            activeSection = (activeSection + 1) % TITLE_KEYS.length;
            return true;
        }
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_UP) {
            activeSection = (activeSection + TITLE_KEYS.length - 1) % TITLE_KEYS.length;
            return true;
        }
        for (int i = 0; i < TITLE_KEYS.length; i++) {
            if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_1 + i) {
                activeSection = i;
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(null);
    }
}
