package com.soulland.client.gui;

import com.soulland.client.ClientCultivationData;
import com.soulland.lang.SoulLang;
import com.soulland.net.ChooseSoulPacket;
import com.soulland.net.CloseAwakeningGuiPacket;
import com.soulland.net.ModNetwork;
import com.soulland.soul.MartialSoul;
import com.soulland.soul.MartialSouls;
import com.soulland.soul.SkillDef;
import com.soulland.soul.SoulCategories;
import com.soulland.soul.SoulLineage;
import com.soulland.soul.SoulRarity;
import com.soulland.soul.SoulText;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.FormattedCharSequence;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * Layar Upacara Kebangkitan Martial Soul.
 *
 * <p>Empat zona dalam satu panel: baris filter lineage, baris tab tipe, daftar
 * soul, dan panel pratinjau. Semua keputusan ada di layar ini supaya pemain
 * tidak pernah tersesat di langkah yang belum terlihat (R-26: tidak ada
 * kontrol mati).
 *
 * <p>Katalog v1.4 berisi 43 soul, jadi daftar digulir dan baris tab tipe
 * digeser horizontal. Gaya tetap sama seperti layar v1.1: tinta pekat, batu,
 * kertas, dan jade sebagai satu-satunya aksen. Pememain tetap memilih sendiri
 * soul-nya; tidak ada undian.
 */
public class AwakeningScreen extends Screen {

    // Ukuran desain panel dalam satuan GUI. Skala bulat dipilih agar teks tetap tajam.
    private static final int PANEL_W = 300;
    private static final int PANEL_H = 210;

    private static final int MARGIN = 12;
    private static final int TITLE_Y = 9;
    private static final int SUBTITLE_Y = 21;
    private static final int RULE_Y = 33;
    private static final int LINEAGE_Y = 37;
    private static final int LINEAGE_H = 11;
    private static final int TYPE_Y = 51;
    private static final int TYPE_H = 12;
    private static final int BODY_Y = 67;
    private static final int BODY_H = 126;
    private static final int LIST_X = MARGIN;
    private static final int LIST_W = 116;
    private static final int PREVIEW_X = 134;
    private static final int PREVIEW_W = 154;
    private static final int STATUS_Y = 197;

    // Palet (lihat blueprint bagian 0).
    private static final int INK = 0xF00B0E13;
    private static final int STONE = 0xFF161C22;
    private static final int PAPER = 0xFFE9E3D5;
    private static final int MUTED = 0xFF98A2AD;
    private static final int JADE = 0xFF3FC7A3;
    private static final int JADE_DIM = 0xFF1B4A41;
    private static final int HAIRLINE = 0xFF2C353E;
    private static final int DANGER = 0xFFE0796A;

    /** Lebar tombol panah pada baris tipe, dalam satuan GUI. */
    private static final int STRIP_ARROW_W = 9;
    /** Lebar minimum satu tab tipe supaya labelnya tidak gepeng. */
    private static final int TYPE_TAB_MIN_W = 42;
    /** Jumlah baris soul yang terlihat sekaligus di daftar. */
    private static final int VISIBLE_ROWS = 5;

    private enum Area {
        LINEAGE, TYPE, LIST, BUTTON
    }

    private final boolean reroll;
    private int categoryIndex = 0;
    private int soulIndex = -1;
    private int listScroll = 0;
    private int typeOffset = 0;
    private SoulLineage lineageFilter = null;
    private Area area = Area.LINEAGE;
    private boolean pending = false;
    private String errorKey = null;

    private final String language;

    public AwakeningScreen(boolean reroll) {
        super(Component.literal(""));
        this.reroll = reroll;
        this.language = SoulLang.activeLanguage();
    }

    /** Terjemahan layar ini memakai bahasa pemain, bukan bahasa client Minecraft. */
    private Component text(String key, String fallback) {
        return SoulLang.text(language, key, fallback);
    }

    /** Terjemahan layar ini dengan placeholder {@code %s}. */
    private Component text(String key, String fallback, Object... args) {
        return SoulLang.text(language, key, fallback, args);
    }

    // ---------------------------------------------------------------- state

    /** Tipe yang ada di lineage terpilih, urut registry. */
    private List<String> lineageTypes() {
        return SoulCategories.typesOf(lineageFilter);
    }

    /**
     * Jumlah pilihan kategori pada baris tipe.
     *
     * <p>Indeks 0 berarti "Semua tipe", jadi pemain selalu bisa melihat isi
     * penuh satu lineage tanpa memilih tipe tertentu.
     */
    private int categoryCount() {
        return lineageTypes().size() + 1;
    }

    /** Tipe aktif, null berarti semua tipe di lineage terpilih. */
    private String activeCategory() {
        if (categoryIndex <= 0) return null;
        List<String> types = lineageTypes();
        if (types.isEmpty()) return null;
        return types.get(Math.min(categoryIndex, types.size()) - 1);
    }

    private List<MartialSoul> soulsOfCurrentCategory() {
        return SoulCategories.soulsOf(lineageFilter, activeCategory());
    }

    private MartialSoul selectedSoul() {
        List<MartialSoul> souls = soulsOfCurrentCategory();
        if (soulIndex < 0 || soulIndex >= souls.size()) return null;
        return souls.get(soulIndex);
    }

    private boolean canConfirm() {
        return selectedSoul() != null && !pending;
    }

    // ---------------------------------------------------------------- input

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    @Override
    public void init() {
        this.errorKey = null;
        this.pending = false;
    }

    @Override
    public void onClose() {
        ModNetwork.CHANNEL.sendToServer(new CloseAwakeningGuiPacket());
        super.onClose();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean shift = this.hasShiftDown();
        switch (keyCode) {
            case GLFW.GLFW_KEY_ESCAPE -> {
                onClose();
                return true;
            }
            case GLFW.GLFW_KEY_TAB -> {
                cycleArea(shift ? -1 : 1);
                return true;
            }
            case GLFW.GLFW_KEY_UP -> {
                moveWithinArea(-1);
                return true;
            }
            case GLFW.GLFW_KEY_DOWN -> {
                moveWithinArea(1);
                return true;
            }
            case GLFW.GLFW_KEY_LEFT -> {
                if (area == Area.LINEAGE) changeLineage(-1);
                if (area == Area.TYPE) scrollType(-1);
                return true;
            }
            case GLFW.GLFW_KEY_RIGHT -> {
                if (area == Area.LINEAGE) changeLineage(1);
                if (area == Area.TYPE) scrollType(1);
                return true;
            }
            case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_SPACE, GLFW.GLFW_KEY_KP_ENTER -> {
                activateFocused();
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

        if (ly >= LINEAGE_Y && ly < LINEAGE_Y + LINEAGE_H && lx >= MARGIN && lx < MARGIN + (PANEL_W - 2 * MARGIN)) {
            int chipWidth = (PANEL_W - 2 * MARGIN) / (SoulLineage.values().length + 1);
            int index = (lx - MARGIN) / chipWidth;
            if (index <= SoulLineage.values().length) {
                lineageFilter = index == 0 ? null : SoulLineage.values()[index - 1];
                resetSelection();
                area = Area.LINEAGE;
                return true;
            }
        }

        if (ly >= TYPE_Y && ly < TYPE_Y + TYPE_H && lx >= MARGIN && lx < MARGIN + (PANEL_W - 2 * MARGIN)) {
            int arrowSpace = typeArrowSpace();
            int left = MARGIN + arrowSpace;
            int right = PANEL_W - MARGIN - arrowSpace;
            if (arrowSpace > 0 && lx < left) {
                scrollType(-1);
                return true;
            }
            if (arrowSpace > 0 && lx >= right) {
                scrollType(1);
                return true;
            }
            int tabWidth = typeTabWidth();
            int index = typeOffset + (lx - left) / tabWidth;
            if (index >= 0 && index < categoryCount()) {
                categoryIndex = index;
                soulIndex = -1;
                listScroll = 0;
                area = Area.TYPE;
                return true;
            }
        }

        List<MartialSoul> souls = soulsOfCurrentCategory();
        int rowTop = BODY_Y + 4;
        for (int i = 0; i < souls.size(); i++) {
            int rowY = rowTop + (i - listScroll) * rowHeight();
            if (rowY < BODY_Y || rowY + rowHeight() > BODY_Y + BODY_H) continue;
            if (ly >= rowY && ly < rowY + rowHeight() - 2 && lx >= LIST_X && lx < LIST_X + LIST_W) {
                soulIndex = i;
                area = Area.LIST;
                return true;
            }
        }

        if (lx >= buttonX() && lx < buttonX() + BUTTON_W && ly >= buttonY() && ly < buttonY() + BUTTON_H) {
            area = Area.BUTTON;
            confirm();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (area == Area.LIST) {
            moveWithinArea(delta > 0 ? -1 : 1);
            return true;
        }
        if (area == Area.TYPE) {
            scrollType(delta > 0 ? -1 : 1);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    private void cycleArea(int direction) {
        Area[] order = Area.values();
        int index = 0;
        for (int i = 0; i < order.length; i++) {
            if (order[i] == area) {
                index = i;
                break;
            }
        }
        index = (index + direction + order.length) % order.length;
        area = order[index];
    }

    private void moveWithinArea(int direction) {
        switch (area) {
            case LINEAGE -> changeLineage(direction);
            case TYPE -> {
                int count = categoryCount();
                if (count <= 0) return;
                categoryIndex = (categoryIndex + direction + count) % count;
                soulIndex = -1;
                listScroll = 0;
                keepTypeVisible();
            }
            case LIST -> {
                List<MartialSoul> souls = soulsOfCurrentCategory();
                if (souls.isEmpty()) return;
                int count = souls.size();
                if (soulIndex < 0) {
                    soulIndex = direction > 0 ? 0 : count - 1;
                } else {
                    soulIndex = (soulIndex + direction + count) % count;
                }
                keepSelectionVisible();
            }
            case BUTTON -> {
            }
        }
    }

    private void changeLineage(int direction) {
        int count = SoulLineage.values().length + 1;
        int index = lineageFilter == null ? 0 : lineageFilter.ordinal() + 1;
        index = (index + direction + count) % count;
        lineageFilter = index == 0 ? null : SoulLineage.values()[index - 1];
        resetSelection();
    }

    /** Mengembalikan filter ke keadaan awal setelah lineage berubah. */
    private void resetSelection() {
        categoryIndex = 0;
        typeOffset = 0;
        soulIndex = -1;
        listScroll = 0;
    }

    /** Menggeser baris tab tipe satu langkah ke kiri atau ke kanan. */
    private void scrollType(int direction) {
        if (typeArrowSpace() == 0) return;
        int visible = typeVisibleCount();
        int maxOffset = Math.max(0, categoryCount() - visible);
        typeOffset = Math.max(0, Math.min(maxOffset, typeOffset + direction));
    }

    /** Memastikan tab kategori yang aktif tetap terlihat di strip. */
    private void keepTypeVisible() {
        int visible = typeVisibleCount();
        if (categoryIndex < typeOffset) typeOffset = categoryIndex;
        if (categoryIndex >= typeOffset + visible) typeOffset = categoryIndex - visible + 1;
        int maxOffset = Math.max(0, categoryCount() - visible);
        typeOffset = Math.max(0, Math.min(maxOffset, typeOffset));
    }

    private void activateFocused() {
        switch (area) {
            case LINEAGE -> area = Area.TYPE;
            case TYPE -> area = Area.LIST;
            case LIST -> {
                if (selectedSoul() != null) area = Area.BUTTON;
            }
            case BUTTON -> confirm();
        }
    }

    private void confirm() {
        MartialSoul soul = selectedSoul();
        if (soul == null || pending) return;
        pending = true;
        errorKey = null;
        ModNetwork.CHANNEL.sendToServer(new ChooseSoulPacket(soul.id()));
    }

    /** Dipanggil dari client hook ketika server menjawab permintaan memilih soul. */
    public void onServerResult(boolean accepted, String messageKey) {
        pending = false;
        if (accepted) {
            onClose();
            return;
        }
        errorKey = messageKey == null || messageKey.isEmpty() ? "gui.soulland.awakening.error.generic" : messageKey;
    }

    // ---------------------------------------------------------------- layout helpers

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

    private int rowHeight() {
        return 21;
    }

    private int buttonX() {
        return PREVIEW_X + 6;
    }

    private int buttonY() {
        return BODY_Y + BODY_H - 18;
    }

    private static final int BUTTON_W = 90;
    private static final int BUTTON_H = 13;

    /** Ruang yang dipakai tombol panah, nol kalau stripnya muat semua tab. */
    private int typeArrowSpace() {
        int full = PANEL_W - 2 * MARGIN;
        int fitAll = Math.max(1, full / TYPE_TAB_MIN_W);
        return categoryCount() > fitAll ? 2 * STRIP_ARROW_W : 0;
    }

    /** Berapa tab tipe yang terlihat sekaligus di strip. */
    private int typeVisibleCount() {
        int usable = PANEL_W - 2 * MARGIN - typeArrowSpace();
        return Math.max(1, usable / TYPE_TAB_MIN_W);
    }

    /** Lebar satu tab tipe, seragam supaya strip mudah digeser. */
    private int typeTabWidth() {
        int usable = PANEL_W - 2 * MARGIN - typeArrowSpace();
        return Math.max(TYPE_TAB_MIN_W, usable / Math.max(1, typeVisibleCount()));
    }

    /** Lebar teks yang muat di daftar soul, di luar bantalan kiri dan jalur scrollbar. */
    private int listTextWidth() {
        return LIST_W - 12;
    }

    /** Menjaga pilihan tetap terlihat setelah daftar digulir. */
    private void keepSelectionVisible() {
        List<MartialSoul> souls = soulsOfCurrentCategory();
        int maxScroll = Math.max(0, souls.size() - VISIBLE_ROWS);
        if (soulIndex < listScroll) listScroll = soulIndex;
        if (soulIndex >= listScroll + VISIBLE_ROWS) listScroll = soulIndex - VISIBLE_ROWS + 1;
        listScroll = Math.max(0, Math.min(maxScroll, listScroll));
    }

    // ---------------------------------------------------------------- render

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
        drawLineageRow(guiGraphics);
        drawTypeRow(guiGraphics);
        drawList(guiGraphics);
        drawPreview(guiGraphics);
        drawStatus(guiGraphics);

        guiGraphics.pose().popPose();
    }

    private void drawHeader(GuiGraphics guiGraphics) {
        Component title = text(reroll ? "gui.soulland.awakening.title.reroll" : "gui.soulland.awakening.title",
                reroll ? "Choose a New Martial Soul" : "Martial Soul Awakening Ceremony")
                .copy().withStyle(style -> style.withBold(true));
        guiGraphics.drawString(this.font, title, MARGIN, TITLE_Y, PAPER);
        guiGraphics.drawString(this.font,
                text(reroll ? "gui.soulland.awakening.subtitle.reroll" : "gui.soulland.awakening.subtitle",
                        reroll ? "Creative mode: replace your awakened soul"
                                : "Choose the martial soul that will bind to your fate"),
                MARGIN, SUBTITLE_Y, MUTED);
        guiGraphics.fill(MARGIN, RULE_Y, PANEL_W - MARGIN, RULE_Y + 1, JADE);
    }

    /** Baris filter lineage: lima chip, "Semua" lalu empat lineage. */
    private void drawLineageRow(GuiGraphics guiGraphics) {
        int total = PANEL_W - 2 * MARGIN;
        int chipWidth = total / (SoulLineage.values().length + 1);
        for (int i = 0; i <= SoulLineage.values().length; i++) {
            int x = MARGIN + i * chipWidth;
            int w = (i == SoulLineage.values().length) ? (MARGIN + total) - x : chipWidth;
            boolean selected = (i == 0 && lineageFilter == null)
                    || (i > 0 && lineageFilter == SoulLineage.values()[i - 1]);
            guiGraphics.fill(x, LINEAGE_Y, x + w, LINEAGE_Y + LINEAGE_H, selected ? JADE_DIM : STONE);
            guiGraphics.fill(x, LINEAGE_Y, x + 1, LINEAGE_Y + LINEAGE_H, selected ? JADE : HAIRLINE);
            String label = i == 0
                    ? text("gui.soulland.lineage.all", "All").getString()
                    : text(SoulLineage.values()[i - 1].langKey(), SoulLineage.values()[i - 1].label()).getString();
            if (this.font.width(label) > w - 6) {
                label = this.font.plainSubstrByWidth(label, w - 6);
            }
            guiGraphics.drawString(this.font, label, x + (w - this.font.width(label)) / 2, LINEAGE_Y + 2,
                    selected ? PAPER : MUTED);
            if (area == Area.LINEAGE && selected) {
                outline(guiGraphics, x, LINEAGE_Y, x + w - 1, LINEAGE_Y + LINEAGE_H - 1, JADE);
            }
        }
    }

    /** Baris tab tipe. Strip digeser kalau tabnya tidak muat. */
    private void drawTypeRow(GuiGraphics guiGraphics) {
        int total = categoryCount();
        if (total <= 0) return;
        int arrowSpace = typeArrowSpace();
        int left = MARGIN + arrowSpace;
        int right = PANEL_W - MARGIN - arrowSpace;
        int visible = typeVisibleCount();
        int tabWidth = typeTabWidth();
        int maxOffset = Math.max(0, total - visible);

        guiGraphics.fill(MARGIN, TYPE_Y, PANEL_W - MARGIN, TYPE_Y + TYPE_H, STONE);

        int drawn = Math.min(visible, total - typeOffset);
        for (int i = 0; i < drawn; i++) {
            int index = typeOffset + i;
            int x = left + i * tabWidth;
            int w = (i == drawn - 1) ? (right - x) : tabWidth;
            boolean selected = index == categoryIndex;
            guiGraphics.fill(x, TYPE_Y, x + w, TYPE_Y + TYPE_H, selected ? JADE_DIM : STONE);
            guiGraphics.fill(x, TYPE_Y, x + 1, TYPE_Y + TYPE_H, selected ? JADE : HAIRLINE);
            String label = categoryLabel(index);
            if (this.font.width(label) > w - 6) {
                label = this.font.plainSubstrByWidth(label, w - 6);
            }
            guiGraphics.drawString(this.font, label, x + (w - this.font.width(label)) / 2, TYPE_Y + 2,
                    selected ? PAPER : MUTED);
            if (area == Area.TYPE && selected) {
                outline(guiGraphics, x, TYPE_Y, x + w - 1, TYPE_Y + TYPE_H - 1, JADE);
            }
        }

        // Tombol panah hanya digambar kalau strip memang bisa digeser.
        if (typeOffset > 0) drawArrow(guiGraphics, MARGIN, TYPE_Y, TYPE_H, true);
        if (typeOffset < maxOffset) drawArrow(guiGraphics, PANEL_W - MARGIN - STRIP_ARROW_W, TYPE_Y, TYPE_H, false);
    }

    /** Label tab kategori, indeks 0 berarti "Semua tipe". */
    private String categoryLabel(int index) {
        if (index <= 0) return text("gui.soulland.category.all", "All").getString();
        List<String> types = lineageTypes();
        if (index - 1 >= types.size()) return "";
        String type = types.get(index - 1);
        return text(SoulCategories.labelKey(type), type).getString();
    }

    /** Panah segitiga kecil, digambar sebagai piksel bukan ikon font. */
    private void drawArrow(GuiGraphics guiGraphics, int x, int y, int height, boolean left) {
        guiGraphics.fill(x, y, x + STRIP_ARROW_W, y + height, JADE_DIM);
        int middle = y + height / 2;
        for (int i = 0; i < 4; i++) {
            int offset = left ? 5 - i : 1 + i;
            guiGraphics.fill(x + offset, middle - i, x + offset + 1, middle + i, PAPER);
        }
    }

    private void drawList(GuiGraphics guiGraphics) {
        guiGraphics.fill(LIST_X, BODY_Y, LIST_X + LIST_W, BODY_Y + BODY_H, STONE);
        List<MartialSoul> souls = soulsOfCurrentCategory();
        if (souls.isEmpty()) {
            guiGraphics.drawString(this.font, text("gui.soulland.awakening.category_empty", "No soul in this category"),
                    LIST_X + 6, BODY_Y + 8, MUTED);
            return;
        }
        int top = BODY_Y + 4;
        for (int i = listScroll; i < souls.size() && i < listScroll + VISIBLE_ROWS; i++) {
            MartialSoul soul = souls.get(i);
            int y = top + (i - listScroll) * rowHeight();
            boolean selected = i == soulIndex;
            if (selected) {
                guiGraphics.fill(LIST_X + 1, y, LIST_X + LIST_W - 5, y + rowHeight() - 2, JADE_DIM);
            }
            String name = this.font.plainSubstrByWidth(
                    SoulText.soulName(soul, language).getString(), listTextWidth());
            guiGraphics.drawString(this.font, name, LIST_X + 6, y, PAPER);

            // Baris kedua: rarity tetap berwarna, pemilik dipotong supaya tidak keluar panel.
            MutableComponent rarity = rarityLabel(soul.rarity());
            guiGraphics.drawString(this.font, rarity, LIST_X + 6, y + 10, MUTED);
            int ownerX = LIST_X + 6 + this.font.width(rarity) + this.font.width(" · ");
            String owner = this.font.plainSubstrByWidth(
                    SoulText.soulOwner(soul, language).getString(), LIST_X + LIST_W - 6 - ownerX);
            guiGraphics.drawString(this.font, " · " + owner, ownerX - this.font.width(" · "), y + 10, MUTED);
            if (area == Area.LIST && selected) {
                outline(guiGraphics, LIST_X + 1, y, LIST_X + LIST_W - 6, y + rowHeight() - 3, JADE);
            }
        }
        drawScrollbar(guiGraphics, souls.size());
        String counter = text("gui.soulland.awakening.counter", "%s of %s",
                Integer.toString(Math.min(souls.size(), listScroll + VISIBLE_ROWS)),
                Integer.toString(souls.size())).getString();
        guiGraphics.drawString(this.font, counter, LIST_X + 6, BODY_Y + BODY_H - 10, MUTED);
    }

    /** Scrollbar tipis di kanan daftar, penanda posisi menggulir. */
    private void drawScrollbar(GuiGraphics guiGraphics, int total) {
        if (total <= VISIBLE_ROWS) return;
        int trackX = LIST_X + LIST_W - 3;
        int trackH = BODY_H - 18;
        guiGraphics.fill(trackX, BODY_Y + 4, trackX + 2, BODY_Y + 4 + trackH, HAIRLINE);
        int thumbH = Math.max(8, trackH * VISIBLE_ROWS / total);
        int maxScroll = Math.max(1, total - VISIBLE_ROWS);
        int thumbY = BODY_Y + 4 + (trackH - thumbH) * listScroll / maxScroll;
        guiGraphics.fill(trackX, thumbY, trackX + 2, thumbY + thumbH, JADE);
    }

    private void drawPreview(GuiGraphics guiGraphics) {
        guiGraphics.fill(PREVIEW_X, BODY_Y, PREVIEW_X + PREVIEW_W, BODY_Y + BODY_H, STONE);
        MartialSoul soul = selectedSoul();

        if (soul == null) {
            guiGraphics.drawString(this.font, text("gui.soulland.awakening.empty", "No soul selected yet"),
                    PREVIEW_X + 6, BODY_Y + 8, MUTED);
            String current = currentSoulName();
            if (!current.isEmpty()) {
                guiGraphics.drawString(this.font, text("gui.soulland.awakening.current_soul", "Current soul: %s", current),
                        PREVIEW_X + 6, BODY_Y + 22, MUTED);
            }
            drawButton(guiGraphics, false);
            return;
        }

        int x = PREVIEW_X + 6;
        guiGraphics.drawString(this.font, SoulText.soulName(soul, language), x, BODY_Y + 4, PAPER);

        MutableComponent meta = rarityLabel(soul.rarity()).copy()
                .append(Component.literal(" · ").withStyle(style -> style.withColor(MUTED)))
                .append(SoulText.soulType(soul, language).copy().withStyle(style -> style.withColor(MUTED)));
        guiGraphics.drawString(this.font, meta, x, BODY_Y + 14, MUTED);

        MutableComponent lineage = SoulText.soulLineage(soul, language).copy()
                .withStyle(style -> style.withColor(PAPER));
        guiGraphics.drawString(this.font, text("gui.soulland.awakening.lineage", "Lineage: %s",
                        lineage.getString()), x, BODY_Y + 24, MUTED);

        MutableComponent affinity = SoulText.soulAffinity(soul, language).copy()
                .withStyle(style -> style.withColor(PAPER));
        guiGraphics.drawString(this.font, text("gui.soulland.awakening.affinity", "Affinity: %s",
                        affinity.getString()), x, BODY_Y + 34, MUTED);

        List<FormattedCharSequence> lines = this.font.split(SoulText.soulDescription(soul, language), PREVIEW_W - 12);
        for (int i = 0; i < Math.min(2, lines.size()); i++) {
            guiGraphics.drawString(this.font, lines.get(i), x, BODY_Y + 44 + i * 10, MUTED);
        }

        guiGraphics.fill(x, BODY_Y + 64, PREVIEW_X + PREVIEW_W - 6, BODY_Y + 65, HAIRLINE);

        List<SkillDef> skills = soul.skills();
        for (int i = 0; i < skills.size() && i < 4; i++) {
            SkillDef skill = skills.get(i);
            int y = BODY_Y + 68 + i * 10;
            guiGraphics.drawString(this.font, Component.literal((i + 1) + ". "), x, y, MUTED);
            guiGraphics.drawString(this.font, SoulText.skillName(skill, language), x + 10, y, PAPER);
        }

        drawButton(guiGraphics, canConfirm());
    }

    private void drawButton(GuiGraphics guiGraphics, boolean enabled) {
        int x = buttonX();
        int y = buttonY();
        guiGraphics.fill(x, y, x + BUTTON_W, y + BUTTON_H, enabled ? JADE_DIM : STONE);
        String label = pending
                ? text("gui.soulland.awakening.confirming", "Confirming...").getString()
                : text("gui.soulland.awakening.confirm", "AWAKEN").getString();
        int color = enabled ? PAPER : MUTED;
        guiGraphics.drawString(this.font, label, x + (BUTTON_W - this.font.width(label)) / 2, y + 3, color);
        if (area == Area.BUTTON) {
            outline(guiGraphics, x, y, x + BUTTON_W - 1, y + BUTTON_H - 1, JADE);
        }
    }

    private void drawStatus(GuiGraphics guiGraphics) {
        if (errorKey != null) {
            guiGraphics.drawString(this.font, text(errorKey, errorFallback(errorKey)), MARGIN, STATUS_Y, DANGER);
            return;
        }
        if (pending) {
            guiGraphics.drawString(this.font, text("gui.soulland.awakening.pending",
                    "Waiting for the server to answer..."), MARGIN, STATUS_Y, MUTED);
            return;
        }
        String current = currentSoulName();
        if (reroll && !current.isEmpty()) {
            guiGraphics.drawString(this.font, text("gui.soulland.awakening.current_soul", "Current soul: %s", current),
                    MARGIN, STATUS_Y, MUTED);
            return;
        }
        guiGraphics.drawString(this.font, text("gui.soulland.awakening.hint",
                "Tab switches area, arrows select, Enter confirms"), MARGIN, STATUS_Y, MUTED);
    }

    private String currentSoulName() {
        MartialSoul soul = MartialSouls.get(ClientCultivationData.soulId);
        return soul == null ? "" : SoulText.soulName(soul, language).getString();
    }

    /** Teks cadangan untuk pesan galat yang dikirim server. */
    private String errorFallback(String key) {
        return switch (key) {
            case "gui.soulland.awakening.error.invalid_soul" -> "That martial soul does not exist";
            case "gui.soulland.awakening.error.already_awakened" -> "Your martial soul is already awakened";
            default -> "That martial soul could not be chosen";
        };
    }

    private MutableComponent rarityLabel(String rarity) {
        return SoulLang.text(language, SoulRarity.langKey(rarity), rarity)
                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(SoulRarity.color(rarity) & 0xFFFFFF)));
    }

    private static void outline(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int color) {
        guiGraphics.fill(x1, y1, x2 + 1, y1 + 1, color);
        guiGraphics.fill(x1, y2, x2 + 1, y2 + 1, color);
        guiGraphics.fill(x1, y1, x1 + 1, y2 + 1, color);
        guiGraphics.fill(x2, y1, x2 + 1, y2 + 1, color);
    }
}
