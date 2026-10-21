package com.soulland.client.gui;

import com.soulland.bone.BoneStats;
import com.soulland.bone.SpiritBoneItem;
import com.soulland.bone.SpiritBoneSlot;
import com.soulland.bone.SpiritBoneTier;
import com.soulland.client.ClientBoneData;
import com.soulland.client.ClientCultivationData;
import com.soulland.lang.SoulLang;
import com.soulland.net.EquipBonePacket;
import com.soulland.net.ModNetwork;
import com.soulland.net.SyncBonesPacket;
import com.soulland.net.UnequipBonePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Layar Boutique Tulang Spirit.
 *
 * <p>Kolom kiri adalah kerangka tubuh: tujuh slot tempat tulang terpasang,
 * digambar sesuai anatomi supaya pemain tahu di mana tulangnya duduk. Kolom
 * kanan adalah daftar tulang yang dibawa pemain, diklik untuk memasang.
 * Strip paling bawah menampilkan bonus set yang sedang aktif.
 *
 * <p>Semua aksiQzakat dikirim ke server. Client hanya menampilkan keadaan
 * dan mengirim permintaan, tidak pernah decides sendiri.
 */
public class SpiritBoneScreen extends Screen {

    private static final int PANEL_W = 300;
    private static final int PANEL_H = 200;

    private static final int MARGIN = 12;
    private static final int TITLE_Y = 9;
    private static final int SUBTITLE_Y = 21;
    private static final int RULE_Y = 33;
    private static final int STATUS_Y = 182;

    private static final int SLOT_SIZE = 18;

    /** Posisi tiap slot di kolom kiri, urut sama dengan {@link SpiritBoneSlot}. */
    private static final int[][] SLOT_POS = {
            {62, 44},
            {62, 66},
            {38, 66},
            {86, 66},
            {48, 90},
            {76, 90},
            {12, 44}};

    private static final int LIST_X = 134;
    private static final int LIST_W = PANEL_W - MARGIN - LIST_X;
    private static final int LIST_HEADER_Y = 44;
    private static final int LIST_TOP = 56;
    private static final int LIST_ROW_H = 16;
    private static final int LIST_ROWS = 7;

    private static final int SET_HEADER_Y = 116;
    private static final int SET_TOP = 126;

    private static final int INK = 0xF00B0E13;
    private static final int STONE = 0xFF161C22;
    private static final int PAPER = 0xFFE9E3D5;
    private static final int MUTED = 0xFF98A2AD;
    private static final int JADE = 0xFF3FC7A3;
    private static final int JADE_DIM = 0xFF1B4A41;
    private static final int HAIRLINE = 0xFF2C353E;
    private static final int DANGER = 0xFFE0796A;

    private final String language;

    private int listFocus = 0;
    private int scroll = 0;
    private boolean pending = false;
    private int pendingIndex = -1;
    private String status = "";
    private boolean statusOk = true;

    public SpiritBoneScreen() {
        super(Component.literal(""));
        this.language = SoulLang.activeLanguage();
    }

    private Component text(String key, String fallback) {
        return SoulLang.text(language, key, fallback);
    }

    private Component text(String key, String fallback, Object... args) {
        return SoulLang.text(language, key, fallback, args);
    }

    // Input

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int max = Math.max(0, ownedBones().size() - LIST_ROWS);
        scroll = Math.max(0, Math.min(max, scroll - (int) Math.signum(delta)));
        if (listFocus < scroll) listFocus = scroll;
        if (listFocus > scroll + LIST_ROWS - 1) listFocus = scroll + LIST_ROWS - 1;
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        List<OwnedBone> bones = ownedBones();
        switch (keyCode) {
            case GLFW.GLFW_KEY_ESCAPE -> {
                onClose();
                return true;
            }
            case GLFW.GLFW_KEY_UP -> {
                moveFocus(bones, -1);
                return true;
            }
            case GLFW.GLFW_KEY_DOWN -> {
                moveFocus(bones, 1);
                return true;
            }
            case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_SPACE, GLFW.GLFW_KEY_KP_ENTER -> {
                toggle(bones, listFocus);
                return true;
            }
            case GLFW.GLFW_KEY_TAB, GLFW.GLFW_KEY_LEFT, GLFW.GLFW_KEY_RIGHT -> {
                onClose();
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

        for (SpiritBoneSlot slot : SpiritBoneSlot.values()) {
            int[] pos = SLOT_POS[slot.index()];
            if (lx >= pos[0] && lx < pos[0] + SLOT_SIZE && ly >= pos[1] && ly < pos[1] + SLOT_SIZE) {
                unequip(slot);
                return true;
            }
        }

        List<OwnedBone> bones = ownedBones();
        if (lx >= LIST_X && lx < LIST_X + LIST_W && ly >= LIST_TOP) {
            int index = scroll + (ly - LIST_TOP) / LIST_ROW_H;
            if (index >= 0 && index < bones.size()) {
                listFocus = index;
                toggle(bones, index);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void moveFocus(List<OwnedBone> bones, int direction) {
        if (bones.isEmpty()) return;
        listFocus = (listFocus + direction + bones.size()) % bones.size();
        int max = Math.max(0, bones.size() - LIST_ROWS);
        if (listFocus < scroll) scroll = listFocus;
        if (listFocus > scroll + LIST_ROWS - 1) scroll = Math.min(max, listFocus - LIST_ROWS + 1);
    }

    /** Memasang atau melepas tulang yang dipilih. */
    private void toggle(List<OwnedBone> bones, int index) {
        if (pending || index < 0 || index >= bones.size()) return;
        OwnedBone bone = bones.get(index);
        boolean equipped = isEquipped(bone.item());
        pending = true;
        pendingIndex = index;
        if (equipped) {
            ModNetwork.CHANNEL.sendToServer(new UnequipBonePacket(bone.item().slot().index()));
        } else {
            ModNetwork.CHANNEL.sendToServer(new EquipBonePacket(bone.item().slot().index(), bone.itemId()));
        }
    }

    private void unequip(SpiritBoneSlot slot) {
        if (pending) return;
        if (!isEquipped(slot)) return;
        pending = true;
        pendingIndex = -1;
        ModNetwork.CHANNEL.sendToServer(new UnequipBonePacket(slot.index()));
    }

    /** Dipanggil dari client hook ketika server menjawab permintaan. */
    public void onServerSync(SyncBonesPacket packet) {
        pending = false;
        pendingIndex = -1;
        status = packet.statusText() == null ? "" : packet.statusText();
        statusOk = packet.statusOk();
    }

    // Layout helpers

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

    // Data

    /** Tulang yang dibawa pemain, dikelompokkan per item dan diurutkan per mutu. */
    private List<OwnedBone> ownedBones() {
        Map<Item, Integer> counts = new LinkedHashMap<>();
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            collect(counts, mc.player.getInventory().items);
            collect(counts, mc.player.getInventory().offhand);
            collect(counts, mc.player.getInventory().armor);
        }
        List<OwnedBone> bones = new ArrayList<>();
        for (Map.Entry<Item, Integer> entry : counts.entrySet()) {
            if (entry.getKey() instanceof SpiritBoneItem bone) {
                bones.add(new OwnedBone(bone, entry.getValue()));
            }
        }
        bones.sort(Comparator
                .comparingInt((OwnedBone bone) -> bone.item().tier().ordinal())
                .thenComparingInt(bone -> bone.item().slot().ordinal()));
        return bones;
    }

    private void collect(Map<Item, Integer> counts, List<ItemStack> stacks) {
        for (ItemStack stack : stacks) {
            if (stack.isEmpty()) continue;
            counts.merge(stack.getItem(), stack.getCount(), Integer::sum);
        }
    }

    private boolean isEquipped(SpiritBoneItem bone) {
        for (SpiritBoneSlot slot : SpiritBoneSlot.values()) {
            String id = ClientBoneData.boneAt(slot.index());
            if (id.equals(bone.itemId())) return true;
        }
        return false;
    }

    private boolean isEquipped(SpiritBoneSlot slot) {
        return !ClientBoneData.boneAt(slot.index()).isEmpty();
    }

    private Item itemById(String itemId) {
        if (itemId == null || itemId.isEmpty()) return null;
        return ForgeRegistries.ITEMS.getValue(new ResourceLocation("soulland", itemId));
    }

    private SpiritBoneItem boneById(String itemId) {
        Item item = itemById(itemId);
        return item instanceof SpiritBoneItem bone ? bone : null;
    }

    private String tierName(SpiritBoneTier tier) {
        return SoulLang.translate(language, "bone.soulland.tier." + tier.id(), tier.id());
    }

    private String slotName(SpiritBoneSlot slot) {
        return SoulLang.translate(language, "bone.soulland.slot." + slot.id(), slot.id());
    }

    // Render

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
        drawSkeleton(guiGraphics);
        drawList(guiGraphics, mouseX, mouseY);
        drawSetBonus(guiGraphics);
        drawStatus(guiGraphics);

        guiGraphics.pose().popPose();

        drawTooltip(guiGraphics, mouseX, mouseY);
    }

    private void drawHeader(GuiGraphics guiGraphics) {
        Component title = text("gui.soulland.bone.title", "Boutique Tulang Spirit")
                .copy().withStyle(style -> style.withBold(true));
        guiGraphics.drawString(this.font, title, MARGIN, TITLE_Y, PAPER);
        guiGraphics.drawString(this.font,
                text("gui.soulland.bone.subtitle", "Klik tulang untuk memasang, klik slot untuk melepas"),
                MARGIN, SUBTITLE_Y, MUTED);
        guiGraphics.fill(MARGIN, RULE_Y, PANEL_W - MARGIN, RULE_Y + 1, JADE);
    }

    private void drawSkeleton(GuiGraphics guiGraphics) {
        for (SpiritBoneSlot slot : SpiritBoneSlot.values()) {
            int[] pos = SLOT_POS[slot.index()];
            int x = pos[0];
            int y = pos[1];
            boolean occupied = isEquipped(slot);

            guiGraphics.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, STONE);
            guiGraphics.fill(x, y, x + SLOT_SIZE, y + 1, HAIRLINE);
            guiGraphics.fill(x, y + SLOT_SIZE - 1, x + SLOT_SIZE, y + SLOT_SIZE, HAIRLINE);
            guiGraphics.fill(x, y, x + 1, y + SLOT_SIZE, HAIRLINE);
            guiGraphics.fill(x + SLOT_SIZE - 1, y, x + SLOT_SIZE, y + SLOT_SIZE, HAIRLINE);

            SpiritBoneItem bone = boneById(ClientBoneData.boneAt(slot.index()));
            if (bone != null) {
                guiGraphics.renderItem(new ItemStack(bone), x + 1, y + 1);
                guiGraphics.fill(x, y, x + 2, y + SLOT_SIZE, bone.tier().color());
            } else {
                guiGraphics.drawString(this.font, slotName(slot), x + 1, y + 6, MUTED);
            }
        }
    }

    private void drawList(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font,
                text("gui.soulland.bone.list_header", "TULANG YANG DIBAWA"),
                LIST_X, LIST_HEADER_Y, MUTED);
        guiGraphics.fill(LIST_X, LIST_HEADER_Y + 10, LIST_X + LIST_W, LIST_HEADER_Y + 11, HAIRLINE);

        List<OwnedBone> bones = ownedBones();
        if (bones.isEmpty()) {
            guiGraphics.drawString(this.font,
                    text("gui.soulland.bone.list_empty", "Belum ada tulang"),
                    LIST_X, LIST_TOP + 2, MUTED);
            guiGraphics.drawString(this.font,
                    text("gui.soulland.bone.list_empty_hint", "Tulang datang dari soul beast dan boss."),
                    LIST_X, LIST_TOP + 14, MUTED);
            return;
        }

        int lx = localX(mouseX);
        int ly = localY(mouseY);
        for (int row = 0; row < LIST_ROWS; row++) {
            int index = scroll + row;
            if (index >= bones.size()) break;
            OwnedBone bone = bones.get(index);
            int y = LIST_TOP + row * LIST_ROW_H;
            boolean selected = index == listFocus;
            boolean equipped = isEquipped(bone.item());
            boolean hovered = lx >= LIST_X && lx < LIST_X + LIST_W && ly >= y && ly < y + LIST_ROW_H;

            guiGraphics.fill(LIST_X, y, LIST_X + LIST_W, y + LIST_ROW_H - 1, STONE);
            if (equipped) {
                guiGraphics.fill(LIST_X + 1, y + 1, LIST_X + LIST_W - 1, y + LIST_ROW_H - 2, JADE_DIM);
            } else if (selected) {
                guiGraphics.fill(LIST_X + 1, y + 1, LIST_X + LIST_W - 1, y + LIST_ROW_H - 2, 0xFF1E262E);
            } else if (hovered) {
                guiGraphics.fill(LIST_X + 1, y + 1, LIST_X + LIST_W - 1, y + LIST_ROW_H - 2, 0xFF1E262E);
            }
            guiGraphics.fill(LIST_X, y, LIST_X + 2, y + LIST_ROW_H - 1, bone.item().tier().color());
            guiGraphics.renderItem(new ItemStack(bone.item()), LIST_X + 4, y);
            guiGraphics.drawString(this.font, slotName(bone.item().slot()), LIST_X + 22, y + 4, PAPER);

            String years = String.valueOf(bone.item().tier().years());
            int yearsWidth = this.font.width(years);
            guiGraphics.drawString(this.font, years, LIST_X + LIST_W - yearsWidth - 14, y + 4,
                    bone.item().tier().color());
            if (pending && index == pendingIndex) {
                guiGraphics.drawString(this.font, "...", LIST_X + LIST_W - 11, y + 4, MUTED);
            } else if (equipped) {
                drawCheck(guiGraphics, LIST_X + LIST_W - 10, y + 5);
            }
        }

        int max = Math.max(0, bones.size() - LIST_ROWS);
        if (max > 0) {
            int barX = LIST_X + LIST_W - 2;
            int barHeight = LIST_TOP + LIST_ROWS * LIST_ROW_H - LIST_TOP;
            guiGraphics.fill(barX, LIST_TOP, barX + 2, LIST_TOP + barHeight, 0xFF1E262E);
            int thumb = Math.max(10, barHeight * LIST_ROWS / bones.size());
            int thumbY = LIST_TOP + (barHeight - thumb) * scroll / max;
            guiGraphics.fill(barX, thumbY, barX + 2, thumbY + thumb, JADE_DIM);
        }
    }

    private void drawSetBonus(GuiGraphics guiGraphics) {
        guiGraphics.drawString(this.font, text("gui.soulland.bone.set_header", "BONUS SET"),
                MARGIN, SET_HEADER_Y, MUTED);
        guiGraphics.fill(MARGIN, SET_HEADER_Y + 10, PANEL_W - MARGIN, SET_HEADER_Y + 11, HAIRLINE);

        Map<SpiritBoneTier, Integer> counts = new java.util.EnumMap<>(SpiritBoneTier.class);
        for (SpiritBoneTier tier : clientEquipped().values()) counts.merge(tier, 1, Integer::sum);

        SpiritBoneTier active = null;
        int activeCount = 0;
        for (Map.Entry<SpiritBoneTier, Integer> entry : counts.entrySet()) {
            if (entry.getValue() >= SpiritBoneTier.SET_THRESHOLD) {
                active = entry.getKey();
                activeCount = entry.getValue();
                break;
            }
        }

        int y = SET_TOP;
        if (active == null) {
            guiGraphics.drawString(this.font,
                    text("gui.soulland.bone.set_none", "Belum ada set aktif"), MARGIN, y, MUTED);
            guiGraphics.drawString(this.font,
                    text("gui.soulland.bone.set_none_hint", "Pasang 4 tulang satu mutu untuk memunculkan bonus set."),
                    MARGIN, y + 10, MUTED);
            return;
        }

        boolean peak = activeCount >= SpiritBoneTier.PEAK_THRESHOLD;
        int threshold = peak ? SpiritBoneTier.PEAK_THRESHOLD : SpiritBoneTier.SET_THRESHOLD;
        guiGraphics.drawString(this.font, tierName(active), MARGIN, y, active.color());
        String counter = activeCount + "/" + threshold;
        guiGraphics.drawString(this.font, counter,
                PANEL_W - MARGIN - this.font.width(counter), y, MUTED);
        y += 10;

        SpiritBoneTier.SetBonus set = active.setBonus();
        List<String> percentLine = new ArrayList<>();
        List<String> flatLine = new ArrayList<>();
        if (set.attackPercent() > 0f) {
            percentLine.add(text("gui.soulland.bone.set_attack", "+%s% serangan",
                    (int) set.attackPercent()).getString());
        }
        if (set.defensePercent() > 0f) {
            percentLine.add(text("gui.soulland.bone.set_defense", "+%s% defense",
                    (int) set.defensePercent()).getString());
        }
        if (set.spiritRegenPercent() > 0f) {
            percentLine.add(text("gui.soulland.bone.set_spirit_regen", "+%s% regenerasi spirit power",
                    (int) set.spiritRegenPercent()).getString());
        }
        if (set.energy() > 0) {
            flatLine.add(text("gui.soulland.bone.set_energy", "+%s spirit power", set.energy()).getString());
        }
        if (set.regen() > 0f) {
            flatLine.add(text("gui.soulland.bone.set_regen", "+%s HP/dtk", format(set.regen())).getString());
        }
        if (!percentLine.isEmpty()) {
            guiGraphics.drawString(this.font, String.join(", ", percentLine), MARGIN, y, JADE);
            y += 10;
        }
        if (!flatLine.isEmpty()) {
            guiGraphics.drawString(this.font, String.join(", ", flatLine), MARGIN, y, JADE);
            y += 10;
        }

        if (peak) {
            SpiritBoneTier.PeakBonus peakBonus = active.peakBonus();
            List<String> peakLine = new ArrayList<>();
            if (peakBonus.attackPercent() > 0f) {
                peakLine.add(text("gui.soulland.bone.set_attack", "+%s% serangan",
                        (int) peakBonus.attackPercent()).getString());
            }
            if (peakBonus.defensePercent() > 0f) {
                peakLine.add(text("gui.soulland.bone.set_defense", "+%s% defense",
                        (int) peakBonus.defensePercent()).getString());
            }
            if (peakBonus.energy() > 0) {
                peakLine.add(text("gui.soulland.bone.set_energy", "+%s spirit power",
                        peakBonus.energy()).getString());
            }
            if (peakBonus.regen() > 0f) {
                peakLine.add(text("gui.soulland.bone.set_regen", "+%s HP/dtk",
                        format(peakBonus.regen())).getString());
            }
            if (!peakLine.isEmpty()) {
                String line = text("gui.soulland.bone.set_peak", "Puncak: %s").getString()
                        .replace("%s", String.join(", ", peakLine));
                guiGraphics.drawString(this.font, line, MARGIN, y, PAPER);
            }
        }
    }

    private Map<SpiritBoneSlot, SpiritBoneTier> clientEquipped() {
        Map<SpiritBoneSlot, SpiritBoneTier> equipped = new java.util.EnumMap<>(SpiritBoneSlot.class);
        for (SpiritBoneSlot slot : SpiritBoneSlot.values()) {
            SpiritBoneItem bone = boneById(ClientBoneData.boneAt(slot.index()));
            if (bone != null) equipped.put(slot, bone.tier());
        }
        return equipped;
    }

    private void drawStatus(GuiGraphics guiGraphics) {
        if (pending) {
            guiGraphics.drawString(this.font, text("gui.soulland.bone.status.pending", "Meminta server..."),
                    MARGIN, STATUS_Y, MUTED);
            return;
        }
        if (!status.isEmpty()) {
            guiGraphics.drawString(this.font, status, MARGIN, STATUS_Y, statusOk ? JADE : DANGER);
            return;
        }
        guiGraphics.drawString(this.font,
                text("gui.soulland.bone.hint", "Panah atas bawah memilih, Enter memasang, Escape menutup"),
                MARGIN, STATUS_Y, MUTED);
    }

    private void drawTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        OwnedBone hovered = hoveredBone(mouseX, mouseY);
        if (hovered == null) return;
        SpiritBoneItem bone = hovered.item();
        BoneStats stats = BoneStats.of(bone.tier(), bone.slot());

        List<Component> lines = new ArrayList<>();
        lines.add(SoulLang.text(language, "bone.soulland.tier." + bone.tier().id(), bone.tier().id())
                .copy().withStyle(style -> style.withBold(true).withColor(bone.tier().color())));
        lines.add(SoulLang.text(language, "bone.soulland.slot." + bone.slot().id(), bone.slot().id())
                .copy().withStyle(style -> style.withColor(MUTED)));
        lines.add(SoulLang.text(language, "tooltip.soulland.bone.attack", "+%s attack", format(stats.attack()))
                .copy().withStyle(style -> style.withColor(JADE)));
        lines.add(SoulLang.text(language, "tooltip.soulland.bone.defense", "+%s defense", format(stats.defense()))
                .copy().withStyle(style -> style.withColor(JADE)));
        lines.add(SoulLang.text(language, "tooltip.soulland.bone.energy", "+%s spirit power maksimum",
                (int) stats.energy()).copy().withStyle(style -> style.withColor(JADE)));
        lines.add(SoulLang.text(language, "tooltip.soulland.bone.regen", "+%s HP per detik", format(stats.regen()))
                .copy().withStyle(style -> style.withColor(JADE)));
        if (bone.tier().requiredRings() > 0) {
            int rings = ClientCultivationData.rings;
            lines.add(SoulLang.text(language, "tooltip.soulland.bone.need_rings",
                            "Butuh %s spirit ring (kamu punya %s)", bone.tier().requiredRings(), rings)
                    .copy().withStyle(style -> style.withColor(
                            rings >= bone.tier().requiredRings() ? MUTED : DANGER)));
        }

        int boxWidth = 0;
        for (Component line : lines) {
            boxWidth = Math.max(boxWidth, this.font.width(line));
        }
        int boxHeight = lines.size() * 10 + 8;
        int x = Math.min(mouseX + 8, this.width - boxWidth - 8);
        int y = Math.min(mouseY + 8, this.height - boxHeight - 8);
        x = Math.max(4, x);
        y = Math.max(4, y);

        guiGraphics.fill(x, y, x + boxWidth + 8, y + boxHeight, 0xF00B0E13);
        outline(guiGraphics, x, y, x + boxWidth + 7, y + boxHeight - 1, HAIRLINE);
        int lineY = y + 4;
        for (Component line : lines) {
            guiGraphics.drawString(this.font, line, x + 4, lineY, 0xFFFFFFFF);
            lineY += 10;
        }
    }

    private OwnedBone hoveredBone(int mouseX, int mouseY) {
        int lx = localX(mouseX);
        int ly = localY(mouseY);
        if (lx < LIST_X || lx >= LIST_X + LIST_W || ly < LIST_TOP) return null;
        int index = scroll + (ly - LIST_TOP) / LIST_ROW_H;
        List<OwnedBone> bones = ownedBones();
        if (index < 0 || index >= bones.size()) return null;
        return bones.get(index);
    }

    private static void outline(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int color) {
        guiGraphics.fill(x1, y1, x2 + 1, y1 + 1, color);
        guiGraphics.fill(x1, y2, x2 + 1, y2 + 1, color);
        guiGraphics.fill(x1, y1, x1 + 1, y2 + 1, color);
        guiGraphics.fill(x2, y1, x2 + 1, y2 + 1, color);
    }

    /** Tanda centang jade untuk baris tulang yang sedang terpasang. */
    private static void drawCheck(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.fill(x, y + 1, x + 1, y + 2, JADE);
        guiGraphics.fill(x + 1, y + 2, x + 2, y + 3, JADE);
        guiGraphics.fill(x + 2, y, x + 3, y + 1, JADE);
        guiGraphics.fill(x + 3, y - 1, x + 4, y, JADE);
    }

    private static String format(float value) {
        if (value == (long) value) return String.valueOf((long) value);
        return String.format(java.util.Locale.ROOT, "%.2f", value);
    }

    /** Tulang yang dibawa pemain beserta jumlahnya. */
    private record OwnedBone(SpiritBoneItem item, int count) {
        String itemId() {
            return item.itemId();
        }
    }
}
