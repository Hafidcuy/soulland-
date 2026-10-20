package com.soulland.client.gui;

import com.soulland.client.ClientCultivationData;
import com.soulland.lang.SoulLang;
import com.soulland.net.ModNetwork;
import com.soulland.net.SelectLanguagePacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/**
 * Layar Pilih Bahasa.
 *
 * <p>Muncul sebelum Upacara Kebangkitan saat pemain pertama kali masuk, dan
 * bisa dipanggil kapan pun lewat {@code /soul language}. Kerangka visualnya
 * sengaja sama dengan {@link AwakeningScreen} (panel tinta, aksen jade
 * tunggal, tipografi yang sama) supaya keduanya terbaca sebagai satu keluarga
 * layar, bukan tambalan.
 *
 * <p>Server yang menyimpan pilihan: layar ini hanya mengirim kode bahasa dan
 * menunggu sinkronisasi balasan.
 */
public class LanguageSelectScreen extends Screen {

    // Ukuran desain panel dalam satuan GUI, mengikuti proporsi AwakeningScreen.
    private static final int PANEL_W = 250;
    private static final int PANEL_H = 116;

    private static final int MARGIN = 12;
    private static final int TITLE_Y = 9;
    private static final int SUBTITLE_Y = 21;
    private static final int RULE_Y = 41;
    private static final int BUTTON_Y = 48;
    private static final int BUTTON_H = 20;
    private static final int BUTTON_W = 108;
    private static final int BUTTON_GAP = 10;
    private static final int STATUS_Y = 73;
    private static final int NOTE_Y = 94;

    /** Lebar teks yang boleh dipakai sebelum dibungkus ke baris berikutnya. */
    private static final int TEXT_W = PANEL_W - 2 * MARGIN;

    /** Batas baris untuk tiap blok teks panjang. */
    private static final int SUBTITLE_LINES = 2;
    private static final int STATUS_LINES = 2;
    private static final int NOTE_LINES = 2;

    // Palet (sama dengan AwakeningScreen).
    private static final int INK = 0xF00B0E13;
    private static final int STONE = 0xFF161C22;
    private static final int PAPER = 0xFFE9E3D5;
    private static final int MUTED = 0xFF98A2AD;
    // Lapisan teks paling senyap, tetap di atas ambang kontras AA.
    private static final int NOTE_TEXT = 0xFF7C8794;
    private static final int JADE = 0xFF3FC7A3;
    private static final int JADE_DIM = 0xFF1B4A41;
    private static final int HAIRLINE = 0xFF2C353E;

    /** Bahasa yang sedang aktif di layar ini. */
    private final String language;

    /** True kalauPemilihan wajib, yaitu saat login pertama. */
    private final boolean mandatory;

    /** Tombol yang sedang difokuskan: 0 = Indonesia, 1 = Inggris. */
    private int focus = 0;

    /** True setelah tombol ditekan dan jawaban server belum datang. */
    private boolean pending = false;

    /** Penanda jumlah sinkronisasi saat tombol ditekan. */
    private int syncMark = 0;

    /** Berapa tick menunggu sebelum layar ditutup paksa. */
    private int waitTicks = 0;

    public LanguageSelectScreen(boolean mandatory) {
        super(Component.translatable("gui.soulland.language.title"));
        this.mandatory = mandatory;
        this.language = SoulLang.activeLanguage();
        String current = ClientCultivationData.language;
        if (SoulLang.ENGLISH.equals(current)) focus = 1;
    }

    // state

    private String languageOf(int index) {
        return index == 0 ? SoulLang.INDONESIAN : SoulLang.ENGLISH;
    }

    private String labelOf(int index) {
        return SoulLang.translate(language, "gui.soulland.language." + languageOf(index),
                index == 0 ? "Bahasa Indonesia" : "English");
    }

    private int buttonX(int index) {
        return MARGIN + index * (BUTTON_W + BUTTON_GAP);
    }

    private boolean isCurrent(int index) {
        return languageOf(index).equals(ClientCultivationData.language);
    }

    // input

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        switch (keyCode) {
            case GLFW.GLFW_KEY_ESCAPE -> {
                if (!mandatory) {
                    onClose();
                    return true;
                }
                return true;
            }
            case GLFW.GLFW_KEY_TAB -> {
                focus = (focus + (this.hasShiftDown() ? -1 : 1) + 2) % 2;
                return true;
            }
            case GLFW.GLFW_KEY_LEFT, GLFW.GLFW_KEY_RIGHT -> {
                focus = focus == 0 ? 1 : 0;
                return true;
            }
            case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_SPACE, GLFW.GLFW_KEY_KP_ENTER -> {
                choose(focus);
                return true;
            }
            default -> {
                return super.keyPressed(keyCode, scanCode, modifiers);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int lx = localX(mouseX);
        int ly = localY(mouseY);
        for (int i = 0; i < 2; i++) {
            int x = buttonX(i);
            if (lx >= x && lx < x + BUTTON_W && ly >= BUTTON_Y && ly < BUTTON_Y + BUTTON_H) {
                focus = i;
                choose(i);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void tick() {
        if (!pending) return;
        waitTicks++;
        boolean answered = ClientCultivationData.syncCount > syncMark
                && languageOf(focus).equals(ClientCultivationData.language);
        if (answered || waitTicks > 200) {
            pending = false;
            onClose();
        }
    }

    private void choose(int index) {
        if (pending) return;
        pending = true;
        waitTicks = 0;
        syncMark = ClientCultivationData.syncCount;
        ModNetwork.CHANNEL.sendToServer(new SelectLanguagePacket(languageOf(index)));
    }

    @Override
    public void onClose() {
        super.onClose();
    }

    // layout helpers

    private int scaleFactor() {
        return Math.max(1, Math.min(2, Math.min(this.width / PANEL_W, this.height / PANEL_H)));
    }

    private int panelX() {
        return (this.width - PANEL_W * scaleFactor()) / 2;
    }

    private int panelY() {
        return (this.height - PANEL_H * scaleFactor()) / 2;
    }

    private int localX(double mouseX) {
        return (int) ((mouseX - panelX()) / scaleFactor());
    }

    private int localY(double mouseY) {
        return (int) ((mouseY - panelY()) / scaleFactor());
    }

    // render

    @Override
    public void renderBackground(GuiGraphics guiGraphics) {
        guiGraphics.fill(0, 0, this.width, this.height, 0xD00B0E13);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int scale = scaleFactor();
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(panelX(), panelY(), 0);
        guiGraphics.pose().scale(scale, scale, 1);

        guiGraphics.fill(0, 0, PANEL_W, PANEL_H, INK);
        guiGraphics.fill(0, 0, PANEL_W, 1, HAIRLINE);
        guiGraphics.fill(0, PANEL_H - 1, PANEL_W, PANEL_H, HAIRLINE);
        guiGraphics.fill(0, 0, 1, PANEL_H, HAIRLINE);
        guiGraphics.fill(PANEL_W - 1, 0, PANEL_W, PANEL_H, HAIRLINE);

        drawHeader(guiGraphics);
        drawButtons(guiGraphics);
        drawStatus(guiGraphics);

        guiGraphics.pose().popPose();
    }

    private void drawHeader(GuiGraphics guiGraphics) {
        Component title = Component.literal(SoulLang.translate(language, "gui.soulland.language.title",
                "Pilih Bahasa")).copy().withStyle(style -> style.withBold(true));
        guiGraphics.drawString(this.font, title, MARGIN, TITLE_Y, PAPER);
        PanelText.drawWrapped(guiGraphics, this.font,
                Component.literal(SoulLang.translate(language, "gui.soulland.language.subtitle",
                        "Tentukan bahasa untuk seluruh antarmuka Soul Land.")),
                MARGIN, SUBTITLE_Y, TEXT_W, MUTED, SUBTITLE_LINES);
        guiGraphics.fill(MARGIN, RULE_Y, PANEL_W - MARGIN, RULE_Y + 1, JADE);
    }

    private void drawButtons(GuiGraphics guiGraphics) {
        for (int i = 0; i < 2; i++) {
            int x = buttonX(i);
            boolean selected = i == focus;
            boolean current = isCurrent(i);
            guiGraphics.fill(x, BUTTON_Y, x + BUTTON_W, BUTTON_Y + BUTTON_H, selected ? JADE_DIM : STONE);
            guiGraphics.fill(x, BUTTON_Y, x + 1, BUTTON_Y + BUTTON_H, current ? JADE : HAIRLINE);

            String label = labelOf(i);
            int textX = x + (BUTTON_W - this.font.width(label)) / 2;
            guiGraphics.drawString(this.font, label, textX, BUTTON_Y + 6, selected ? PAPER : MUTED);
            if (selected) {
                outline(guiGraphics, x, BUTTON_Y, x + BUTTON_W - 1, BUTTON_Y + BUTTON_H - 1, JADE);
            }
        }
    }

    private void drawStatus(GuiGraphics guiGraphics) {
        String status;
        int color;
        if (pending) {
            status = SoulLang.translate(language, "gui.soulland.language.pending",
                    "Menyimpan pilihan bahasa...");
            color = JADE;
        } else if (mandatory) {
            status = SoulLang.translate(language, "gui.soulland.language.required",
                    "Pilih bahasa untuk melanjutkan ke Upacara Kebangkitan.");
            color = PAPER;
        } else {
            status = SoulLang.translate(language, "gui.soulland.language.hint",
                    "Tab atau panah untuk pindah, Enter untuk memilih.");
            color = MUTED;
        }
        PanelText.drawWrapped(guiGraphics, this.font, Component.literal(status),
                MARGIN, STATUS_Y, TEXT_W, color, STATUS_LINES);

        String note = SoulLang.translate(language, "gui.soulland.language.note",
                "Pilihan ini dipakai HUD, upacara, buku panduan, dan pesan perintah.");
        PanelText.drawWrapped(guiGraphics, this.font, Component.literal(note),
                MARGIN, NOTE_Y, TEXT_W, NOTE_TEXT, NOTE_LINES);
    }

    private static void outline(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int color) {
        guiGraphics.fill(x1, y1, x2 + 1, y1 + 1, color);
        guiGraphics.fill(x1, y2, x2 + 1, y2 + 1, color);
        guiGraphics.fill(x1, y1, x1 + 1, y2 + 1, color);
        guiGraphics.fill(x2, y1, x2 + 1, y2 + 1, color);
    }
}
