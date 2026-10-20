package com.soulland.client;

import com.soulland.screen.GuideScreen;
import com.soulland.soul.MartialSoul;
import com.soulland.soul.MartialSouls;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

/**
 * Layar sambutan untuk pemain baru.
 *
 * <p>Isinya bukanorations: urutan cara main, angka yang benar-benar dipakai mod, dan
 * tombol yang melakukan sesuatu. Tidak ada teks hiasan dan tidak ada tombol mati.
 */
public class WelcomeScreen extends Screen {

    private static final ResourceLocation PANEL =
            new ResourceLocation("soulland", "textures/gui/panel_guide.png");

    private static final int IMPERIAL_GOLD = 0xE8C46A;
    private static final int SOUL_JADE = 0x4FC08D;
    private static final int PAPER = 0xE8E0CC;
    private static final int MUTED = 0x9A9284;

    private static final String[] STEPS = {
            "Buru soul beast. Namanya menyebut usianya dan elemennya, misalnya 1.000 Tahun (Api).",
            "Serap spirit ring yang jatuh. Ring 100 tahun butuh level 10, 1.000 butuh 20, 10.000 butuh 40, 100.000 butuh 70.",
            "Tahan Shift saat menyerap ring untuk absorpsi brave: 55% chance naik satu tingkat, dengan biaya 20% HP dan 40 Spirit Power. Kalau gagal, ring hancur.",
            "Lima slot skill (R G F V Z) bisa diisi bebas. Buka Kartu Soul (J), tab SKILLS, lalu klik skill untuk memasang.",
            "Setiap skill yang berhasil menambah satu mata rantai: +8% damage, maksimal 5 mata, bertahan 4 detik.",
            "Elemen menentukan target. Elemen soul yang menang memberi +25% damage, kalah memberi -15%.",
            "Naik peringkat butuh upacara terobosan: di level 10, 20, dan seterusnya, tahan C dan bermeditasi 3 detik dengan 1 Esensi Jiwa."
    };

    private static final String[][] KEYS = {
            {"R G F V Z", "Skill 1 sampai 5"},
            {"X", "Ledakan spirit ring"},
            {"B", "Bakar darah: 25 HP jadi 30 Spirit Power"},
            {"C tahan", "Meditasi dan upacara terobosan"},
            {"J", "Kartu Martial Soul"},
            {"K", "Panggil companion soul"},
            {"H", "Panduan lengkap"}
    };

    private int panelLeft;
    private int panelTop;
    private int panelWidth = 400;
    private int panelHeight = 210;
    private int soulCardLeft;
    private int soulCardTop;
    private int guideButtonLeft;
    private int guideButtonTop;

    public WelcomeScreen() {
        super(Component.translatable("screen.soulland.welcome"));
    }

    @Override
    protected void init() {
        panelWidth = Math.min(400, this.width - 16);
        panelHeight = Math.min(210, this.height - 16);
        panelLeft = (this.width - panelWidth) / 2;
        panelTop = (this.height - panelHeight) / 2;
        soulCardLeft = panelLeft + panelWidth - 78;
        soulCardTop = panelTop + 14;
        guideButtonLeft = panelLeft + 14;
        guideButtonTop = panelTop + panelHeight - 26;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        gui.fill(0, 0, this.width, this.height, 0xB0101418);
        gui.blit(PANEL, panelLeft, panelTop, 0, 0, panelWidth, panelHeight, panelWidth, panelHeight);

        int x = panelLeft + 14;
        int y = panelTop + 12;

        gui.drawString(this.font, "Selamat datang di Benua Douluo", x, y, IMPERIAL_GOLD);
        y += 12;
        gui.fill(x, y, x + 190, y + 1, 0x50E8C46A);
        y += 8;

        MartialSoul soul = MartialSouls.effective(ClientCultivationData.soulId, ClientCultivationData.evolutionStage);
        if (soul == null) {
            gui.drawString(this.font, "Martial soul-mu belum diundi. Masuk ulang ke dunia untuk mengundi.", x, y, MUTED);
            y += 12;
        } else {
            gui.drawString(this.font, "Martial soul: " + soul.name(), x, y, PAPER);
            gui.drawString(this.font, soul.rarity() + " | " + soul.type() + " | Elemen " + soul.element(),
                    x + 150, y, MUTED);
            y += 10;
            for (FormattedCharSequence line : this.font.split(Component.literal(soul.description()), 250)) {
                gui.drawString(this.font, line, x, y, 0xBDB5A4);
                y += 9;
            }
        }

        y += 6;
        gui.drawString(this.font, "Cara main", x, y, SOUL_JADE);
        y += 11;
        int step = 1;
        for (String text : STEPS) {
            gui.drawString(this.font, step + ".", x, y, IMPERIAL_GOLD);
            int lineY = y;
            for (FormattedCharSequence line : this.font.split(Component.literal(text), 236)) {
                gui.drawString(this.font, line, x + 12, lineY, 0xCFC7B6);
                lineY += 9;
            }
            y = lineY + 2;
            step++;
        }

        drawKeyHints(gui, x, panelTop + panelHeight - 92);
        drawButtons(gui, mouseX, mouseY);
    }

    private void drawKeyHints(GuiGraphics gui, int x, int y) {
        gui.drawString(this.font, "Tombol", x, y, SOUL_JADE);
        y += 11;
        int column = 0;
        int row = 0;
        for (String[] hint : KEYS) {
            int hx = x + column * 180;
            int hy = y + row * 10;
            gui.drawString(this.font, hint[0], hx, hy, IMPERIAL_GOLD);
            gui.drawString(this.font, hint[1], hx + 42, hy, 0xA79E8C);
            row++;
            if (row == 4) {
                row = 0;
                column++;
            }
        }
    }

    private void drawButtons(GuiGraphics gui, int mouseX, int mouseY) {
        int buttonHeight = 16;
        int width = 96;

        gui.fill(guideButtonLeft, guideButtonTop, guideButtonLeft + width, guideButtonTop + buttonHeight,
                hovered(mouseX, mouseY, guideButtonLeft, guideButtonTop, width, buttonHeight) ? 0xFF4FC08D : 0xFF2E6B52);
        gui.drawString(this.font, "Buka Panduan", guideButtonLeft + 8, guideButtonTop + 4, 0x102018);

        int cardLeft = guideButtonLeft + width + 8;
        gui.fill(cardLeft, guideButtonTop, cardLeft + width, guideButtonTop + buttonHeight,
                hovered(mouseX, mouseY, cardLeft, guideButtonTop, width, buttonHeight) ? 0xFFE8C46A : 0xFF9C7C2E);
        gui.drawString(this.font, "Kartu Soul", cardLeft + 10, guideButtonTop + 4, 0x201804);

        int closeLeft = cardLeft + width + 8;
        gui.fill(closeLeft, guideButtonTop, closeLeft + 74, guideButtonTop + buttonHeight,
                hovered(mouseX, mouseY, closeLeft, guideButtonTop, 74, buttonHeight) ? 0xFFC8C0B0 : 0xFF6E685C);
        gui.drawString(this.font, "Mulai", closeLeft + 22, guideButtonTop + 4, 0x14120E);

        // kartu soul mini di pojok kanan atas
        gui.fill(soulCardLeft, soulCardTop, soulCardLeft + 64, soulCardTop + 64, 0xC01A1E26);
        gui.fill(soulCardLeft, soulCardTop, soulCardLeft + 64, soulCardTop + 1, IMPERIAL_GOLD);
        MartialSoul soul = MartialSouls.effective(ClientCultivationData.soulId, ClientCultivationData.evolutionStage);
        if (soul != null) {
            ResourceLocation portrait = new ResourceLocation("soulland", "textures/soul/" + soul.id() + ".png");
            gui.pose().pushPose();
            gui.pose().translate(soulCardLeft + 16, soulCardTop + 16, 0);
            gui.pose().scale(2.0f, 2.0f, 1.0f);
            gui.blit(portrait, 0, 0, 0, 0, 16, 16, 16, 16);
            gui.pose().popPose();
            gui.drawString(this.font, "Lv." + ClientCultivationData.level, soulCardLeft + 4, soulCardTop + 40, PAPER);
            gui.drawString(this.font, "Ring " + ClientCultivationData.rings + "/9", soulCardLeft + 4, soulCardTop + 50, MUTED);
        }
    }

    private boolean hovered(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int mx = (int) mouseX;
        int my = (int) mouseY;
        int buttonHeight = 16;
        int width = 96;

        if (hovered(mx, my, guideButtonLeft, guideButtonTop, width, buttonHeight)) {
            this.minecraft.setScreen(new GuideScreen());
            return true;
        }
        int cardLeft = guideButtonLeft + width + 8;
        if (hovered(mx, my, cardLeft, guideButtonTop, width, buttonHeight)) {
            this.minecraft.setScreen(new SoulCardScreen());
            return true;
        }
        int closeLeft = cardLeft + width + 8;
        if (hovered(mx, my, closeLeft, guideButtonTop, 74, buttonHeight)) {
            onClose();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE || keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(null);
    }
}
