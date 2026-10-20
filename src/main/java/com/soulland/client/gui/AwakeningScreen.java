package com.soulland.client.gui;

import com.soulland.client.ClientCultivationData;
import com.soulland.net.ChooseSoulPacket;
import com.soulland.net.CloseAwakeningGuiPacket;
import com.soulland.net.ModNetwork;
import com.soulland.soul.MartialSoul;
import com.soulland.soul.MartialSouls;
import com.soulland.soul.SkillDef;
import com.soulland.soul.SoulCategories;
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
 * <p>Tiga zona dalam satu panel: tab kategori, daftar soul, dan panel pratinjau.
 * Semua keputusan ada di layar ini supaya pemain tidak pernah tersesat di
 * langkah yang belum terlihat (R-26: tidak ada kontrol mati).
 *
 * <p>Palet mengikuti {@code docs/v1.1-blueprint.md}: tinta pekat, batu, kertas,
 * dan jade sebagai satu-satunya aksen.
 */
public class AwakeningScreen extends Screen {

    // Ukuran desain panel dalam satuan GUI. Skala bulat dipilih agar teks tetap tajam.
    private static final int PANEL_W = 300;
    private static final int PANEL_H = 186;

    private static final int MARGIN = 12;
    private static final int TITLE_Y = 9;
    private static final int SUBTITLE_Y = 21;
    private static final int RULE_Y = 33;
    private static final int TAB_Y = 38;
    private static final int TAB_H = 12;
    private static final int BODY_Y = 54;
    private static final int BODY_H = 112;
    private static final int LIST_X = MARGIN;
    private static final int LIST_W = 116;
    private static final int PREVIEW_X = 134;
    private static final int PREVIEW_W = 154;
    private static final int STATUS_Y = 170;

    // Palet (lihat blueprint bagian 0).
    private static final int INK = 0xF00B0E13;
    private static final int STONE = 0xFF161C22;
    private static final int PAPER = 0xFFE9E3D5;
    private static final int MUTED = 0xFF98A2AD;
    private static final int JADE = 0xFF3FC7A3;
    private static final int JADE_DIM = 0xFF1B4A41;
    private static final int HAIRLINE = 0xFF2C353E;
    private static final int DANGER = 0xFFE0796A;

    private enum Area {
        TAB, LIST, BUTTON
    }

    private final boolean reroll;
    private final List<String> categories;
    private int categoryIndex = 0;
    private int soulIndex = -1;
    private Area area = Area.TAB;
    private boolean pending = false;
    private String errorKey = null;

    public AwakeningScreen(boolean reroll) {
        super(Component.translatable(reroll ? "gui.soulland.awakening.title.reroll" : "gui.soulland.awakening.title"));
        this.reroll = reroll;
        this.categories = SoulCategories.types();
    }

    // ---------------------------------------------------------------- state

    private List<MartialSoul> soulsOfCurrentCategory() {
        if (categories.isEmpty()) return List.of();
        return SoulCategories.soulsOf(categories.get(Math.min(categoryIndex, categories.size() - 1)));
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
                if (area == Area.TAB) changeCategory(-1);
                return true;
            }
            case GLFW.GLFW_KEY_RIGHT -> {
                if (area == Area.TAB) changeCategory(1);
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

        if (ly >= TAB_Y && ly < TAB_Y + TAB_H && lx >= MARGIN && lx < MARGIN + (PANEL_W - 2 * MARGIN)) {
            int tabWidth = (PANEL_W - 2 * MARGIN) / Math.max(1, categories.size());
            int index = (lx - MARGIN) / tabWidth;
            if (index < categories.size()) {
                categoryIndex = index;
                soulIndex = -1;
                area = Area.TAB;
                return true;
            }
        }

        List<MartialSoul> souls = soulsOfCurrentCategory();
        int rowTop = BODY_Y + 4;
        for (int i = 0; i < souls.size(); i++) {
            int rowY = rowTop + i * rowHeight();
            if (ly >= rowY && ly < rowY + rowHeight() - 4 && lx >= LIST_X && lx < LIST_X + LIST_W) {
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
            case TAB -> changeCategory(direction);
            case LIST -> {
                List<MartialSoul> souls = soulsOfCurrentCategory();
                if (souls.isEmpty()) return;
                int count = souls.size();
                if (soulIndex < 0) {
                    soulIndex = direction > 0 ? 0 : count - 1;
                } else {
                    soulIndex = (soulIndex + direction + count) % count;
                }
            }
            case BUTTON -> {
            }
        }
    }

    private void changeCategory(int direction) {
        if (categories.isEmpty()) return;
        categoryIndex = (categoryIndex + direction + categories.size()) % categories.size();
        soulIndex = -1;
    }

    private void activateFocused() {
        switch (area) {
            case TAB -> area = Area.LIST;
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
        return 26;
    }

    private int buttonX() {
        return PREVIEW_X + 6;
    }

    private int buttonY() {
        return BODY_Y + BODY_H - 18;
    }

    private static final int BUTTON_W = 90;
    private static final int BUTTON_H = 13;

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
        drawTabs(guiGraphics);
        drawList(guiGraphics);
        drawPreview(guiGraphics);
        drawStatus(guiGraphics);

        guiGraphics.pose().popPose();
    }

    private void drawHeader(GuiGraphics guiGraphics) {
        Component title = this.title.copy().withStyle(style -> style.withBold(true));
        guiGraphics.drawString(this.font, title, MARGIN, TITLE_Y, PAPER);
        guiGraphics.drawString(this.font,
                Component.translatable(reroll ? "gui.soulland.awakening.subtitle.reroll" : "gui.soulland.awakening.subtitle"),
                MARGIN, SUBTITLE_Y, MUTED);
        guiGraphics.fill(MARGIN, RULE_Y, PANEL_W - MARGIN, RULE_Y + 1, JADE);
    }

    private void drawTabs(GuiGraphics guiGraphics) {
        if (categories.isEmpty()) return;
        int total = PANEL_W - 2 * MARGIN;
        int tabWidth = total / categories.size();
        for (int i = 0; i < categories.size(); i++) {
            int x = MARGIN + i * tabWidth;
            int w = (i == categories.size() - 1) ? (MARGIN + total) - x : tabWidth;
            boolean selected = i == categoryIndex;
            guiGraphics.fill(x, TAB_Y, x + w, TAB_Y + TAB_H, selected ? JADE_DIM : STONE);
            guiGraphics.fill(x, TAB_Y, x + 1, TAB_Y + TAB_H, selected ? JADE : HAIRLINE);
            String label = Component.translatable(SoulCategories.labelKey(categories.get(i))).getString();
            if (this.font.width(label) > w - 6) {
                label = this.font.plainSubstrByWidth(label, w - 6);
            }
            guiGraphics.drawString(this.font, label, x + (w - this.font.width(label)) / 2, TAB_Y + 2,
                    selected ? PAPER : MUTED);
            if (area == Area.TAB && selected) {
                outline(guiGraphics, x, TAB_Y, x + w - 1, TAB_Y + TAB_H - 1, JADE);
            }
        }
    }

    private void drawList(GuiGraphics guiGraphics) {
        guiGraphics.fill(LIST_X, BODY_Y, LIST_X + LIST_W, BODY_Y + BODY_H, STONE);
        List<MartialSoul> souls = soulsOfCurrentCategory();
        if (souls.isEmpty()) {
            guiGraphics.drawString(this.font, Component.translatable("gui.soulland.awakening.category_empty"),
                    LIST_X + 6, BODY_Y + 8, MUTED);
            return;
        }
        int top = BODY_Y + 4;
        for (int i = 0; i < souls.size(); i++) {
            MartialSoul soul = souls.get(i);
            int y = top + i * rowHeight();
            boolean selected = i == soulIndex;
            if (selected) {
                guiGraphics.fill(LIST_X + 1, y, LIST_X + LIST_W - 1, y + rowHeight() - 6, JADE_DIM);
            }
            guiGraphics.drawString(this.font, SoulText.soulName(soul), LIST_X + 6, y, PAPER);
            MutableComponent meta = rarityLabel(soul.rarity()).copy()
                    .append(Component.literal(" · ").withStyle(style -> style.withColor(MUTED)))
                    .append(SoulText.soulOwner(soul).copy().withStyle(style -> style.withColor(MUTED)));
            guiGraphics.drawString(this.font, meta, LIST_X + 6, y + 10, MUTED);
            if (area == Area.LIST && selected) {
                outline(guiGraphics, LIST_X + 1, y, LIST_X + LIST_W - 2, y + rowHeight() - 7, JADE);
            }
        }
    }

    private void drawPreview(GuiGraphics guiGraphics) {
        guiGraphics.fill(PREVIEW_X, BODY_Y, PREVIEW_X + PREVIEW_W, BODY_Y + BODY_H, STONE);
        MartialSoul soul = selectedSoul();

        if (soul == null) {
            guiGraphics.drawString(this.font, Component.translatable("gui.soulland.awakening.empty"),
                    PREVIEW_X + 6, BODY_Y + 8, MUTED);
            String current = currentSoulName();
            if (!current.isEmpty()) {
                guiGraphics.drawString(this.font, Component.translatable("gui.soulland.awakening.current_soul", current),
                        PREVIEW_X + 6, BODY_Y + 22, MUTED);
            }
            drawButton(guiGraphics, false);
            return;
        }

        int x = PREVIEW_X + 6;
        guiGraphics.drawString(this.font, SoulText.soulName(soul), x, BODY_Y + 4, PAPER);

        MutableComponent meta = rarityLabel(soul.rarity()).copy()
                .append(Component.literal(" · ").withStyle(style -> style.withColor(MUTED)))
                .append(SoulText.soulType(soul).copy().withStyle(style -> style.withColor(MUTED)));
        guiGraphics.drawString(this.font, meta, x, BODY_Y + 14, MUTED);

        guiGraphics.drawString(this.font, Component.translatable("gui.soulland.awakening.owner", SoulText.soulOwner(soul).getString()),
                x, BODY_Y + 24, MUTED);

        List<FormattedCharSequence> lines = this.font.split(SoulText.soulDescription(soul), PREVIEW_W - 12);
        for (int i = 0; i < Math.min(2, lines.size()); i++) {
            guiGraphics.drawString(this.font, lines.get(i), x, BODY_Y + 36 + i * 10, MUTED);
        }

        guiGraphics.fill(x, BODY_Y + 54, PREVIEW_X + PREVIEW_W - 6, BODY_Y + 55, HAIRLINE);

        List<SkillDef> skills = soul.skills();
        for (int i = 0; i < skills.size() && i < 4; i++) {
            SkillDef skill = skills.get(i);
            int y = BODY_Y + 58 + i * 10;
            guiGraphics.drawString(this.font, Component.literal((i + 1) + ". "), x, y, MUTED);
            guiGraphics.drawString(this.font, SoulText.skillName(skill), x + 10, y, PAPER);
            String cost = Integer.toString((int) skill.energyCost());
            guiGraphics.drawString(this.font, cost, PREVIEW_X + PREVIEW_W - 6 - this.font.width(cost), y, JADE);
        }

        drawButton(guiGraphics, canConfirm());
    }

    private void drawButton(GuiGraphics guiGraphics, boolean enabled) {
        int x = buttonX();
        int y = buttonY();
        guiGraphics.fill(x, y, x + BUTTON_W, y + BUTTON_H, enabled ? JADE_DIM : STONE);
        String label = pending
                ? Component.translatable("gui.soulland.awakening.confirming").getString()
                : Component.translatable("gui.soulland.awakening.confirm").getString();
        int color = enabled ? PAPER : MUTED;
        guiGraphics.drawString(this.font, label, x + (BUTTON_W - this.font.width(label)) / 2, y + 3, color);
        if (area == Area.BUTTON) {
            outline(guiGraphics, x, y, x + BUTTON_W - 1, y + BUTTON_H - 1, JADE);
        }
    }

    private void drawStatus(GuiGraphics guiGraphics) {
        if (errorKey != null) {
            guiGraphics.drawString(this.font, Component.translatable(errorKey), MARGIN, STATUS_Y, DANGER);
            return;
        }
        if (pending) {
            guiGraphics.drawString(this.font, Component.translatable("gui.soulland.awakening.pending"),
                    MARGIN, STATUS_Y, MUTED);
            return;
        }
        String current = currentSoulName();
        if (reroll && !current.isEmpty()) {
            guiGraphics.drawString(this.font, Component.translatable("gui.soulland.awakening.current_soul", current),
                    MARGIN, STATUS_Y, MUTED);
            return;
        }
        guiGraphics.drawString(this.font, Component.translatable("gui.soulland.awakening.hint"), MARGIN, STATUS_Y, MUTED);
    }

    private String currentSoulName() {
        MartialSoul soul = MartialSouls.get(ClientCultivationData.soulId);
        return soul == null ? "" : SoulText.soulName(soul).getString();
    }

    private static MutableComponent rarityLabel(String rarity) {
        return Component.translatable(SoulRarity.langKey(rarity))
                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(SoulRarity.color(rarity) & 0xFFFFFF)));
    }

    private static void outline(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int color) {
        guiGraphics.fill(x1, y1, x2 + 1, y1 + 1, color);
        guiGraphics.fill(x1, y2, x2 + 1, y2 + 1, color);
        guiGraphics.fill(x1, y1, x1 + 1, y2 + 1, color);
        guiGraphics.fill(x2, y1, x2 + 1, y2 + 1, color);
    }
}
