package com.soulland.screen;

import com.soulland.CultivationRanks;
import com.soulland.client.ClientCultivationData;
import com.soulland.client.ClientSkills;
import com.soulland.net.AssignSkillPacket;
import com.soulland.net.ChooseSectPacket;
import com.soulland.net.ModNetwork;
import com.soulland.sect.Sect;
import com.soulland.soul.MartialSoul;
import com.soulland.soul.MartialSouls;
import com.soulland.soul.RingSkills;
import com.soulland.soul.SkillDef;
import net.minecraft.ChatFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import com.soulland.compat.CuriosBridge;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * Kartu martial soul: potret besar, daftar skill, status spirit ring, dan pilihan sekte.
 *
 * <p>Empat tab, semuanya menampilkan data nyata dari state pemain. Tidak ada tombol
 * yang tidak melakukan apa-apa, dan setiap tab bisa reached lewat keyboard (tombol 1-4).
 */
@OnlyIn(Dist.CLIENT)
public class SoulCardScreen extends Screen {

    private static final int SOUL_JADE = 0x4FC08D;
    private static final int IMPERIAL_GOLD = 0xE8C46A;
    private static final int INK = 0x14161A;
    private static final int CRIMSON = 0xD8442F;

    private static final String[] TAB_NAMES = {"SOUL", "SKILLS", "RINGS", "SECT"};
    private static final int[] TAB_KEYS = {GLFW.GLFW_KEY_1, GLFW.GLFW_KEY_2, GLFW.GLFW_KEY_3, GLFW.GLFW_KEY_4};

    private int activeTab = 0;
    private int panelLeft;
    private int panelTop;
    private int panelWidth = 300;
    private int panelHeight = 180;
    private static final ResourceLocation PANEL =
            new ResourceLocation("soulland", "textures/gui/panel_card.png");
    private int[] tabLeft = new int[4];
    private int[] tabTop = new int[4];
    private int[] tabRight = new int[4];

    public SoulCardScreen() {
        this(0);
    }

    public SoulCardScreen(int tab) {
        super(Component.translatable("screen.soulland.soul_card"));
        this.activeTab = Math.max(0, Math.min(3, tab));
    }

    @Override
    protected void init() {
        panelWidth = Math.min(340, this.width - 20);
        panelHeight = Math.min(200, this.height - 20);
        panelLeft = (this.width - panelWidth) / 2;
        panelTop = (this.height - panelHeight) / 2;

        int tabX = panelLeft + 86;
        int tabY = panelTop + 12;
        for (int i = 0; i < TAB_NAMES.length; i++) {
            tabLeft[i] = tabX;
            tabRight[i] = tabX + this.font.width(TAB_NAMES[i]) + 2;
            tabTop[i] = tabY;
            tabX = tabRight[i] + 12;
        }
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
        gui.fill(panelLeft, panelTop + panelHeight - 1, panelLeft + panelWidth, panelTop + panelHeight, 0x40E8C46A);

        drawTabs(gui, mouseX, mouseY);
        switch (activeTab) {
            case 0 -> drawSoulTab(gui);
            case 1 -> drawSkillsTab(gui);
            case 2 -> drawRingsTab(gui);
            case 3 -> drawSectTab(gui, mouseX, mouseY);
            default -> {
            }
        }
    }

    private void drawTabs(GuiGraphics gui, int mouseX, int mouseY) {
        for (int i = 0; i < TAB_NAMES.length; i++) {
            boolean active = i == activeTab;
            int color = active ? IMPERIAL_GOLD : 0x8A8A8A;
            gui.drawString(this.font, TAB_NAMES[i], tabLeft[i], tabTop[i], color);
            if (active) {
                gui.fill(tabLeft[i], tabTop[i] + 9, tabRight[i], tabTop[i] + 10, IMPERIAL_GOLD);
            } else if (mouseX >= tabLeft[i] && mouseX <= tabRight[i]
                    && mouseY >= tabTop[i] && mouseY <= tabTop[i] + 9) {
                gui.fill(tabLeft[i], tabTop[i] + 9, tabRight[i], tabTop[i] + 10, 0x60E8C46A);
            }
        }
    }

    // ------------------------------------------------------------------ tab SOUL

    private void drawSoulTab(GuiGraphics gui) {
        MartialSoul soul = currentSoul();
        int x = panelLeft + 12;
        int y = panelTop + 28;

        if (soul == null) {
            gui.drawString(this.font, "Belum ada martial soul", x, y, 0xB0B0B0);
            gui.drawString(this.font, "Masuk kembali ke dunia untuk", x, y + 12, 0x808080);
            gui.drawString(this.font, "menjalankan upacara kebangkitan.", x, y + 22, 0x808080);
            return;
        }

        ResourceLocation portrait = new ResourceLocation("soulland", "textures/soul/" + soul.id() + ".png");
        gui.pose().pushPose();
        gui.pose().translate(x, y, 0);
        gui.pose().scale(4f, 4f, 1f);
        gui.blit(portrait, 0, 0, 0, 0, 16, 16, 16, 16);
        gui.pose().popPose();
        gui.fill(x, y, x + 64, y + 64, 0x30000000);

        int textX = x + 74;
        gui.drawString(this.font, soul.name(), textX, y, rarityColor(soul.rarity()));
        gui.drawString(this.font, soul.rarity() + "  " + soul.type(), textX, y + 11, 0x9A9A9A);
        gui.drawString(this.font, "Elemen: " + soul.element(), textX, y + 22, 0x9A9A9A);
        gui.drawString(this.font, "Pemilik asal: " + soul.owner(), textX, y + 33, 0x9A9A9A);
        gui.drawString(this.font, "Peringkat: " + CultivationRanks.rankName(ClientCultivationData.level),
                textX, y + 44, IMPERIAL_GOLD);

        int descY = y + 70;
        for (FormattedCharSequence line : this.font.split(Component.literal(soul.description()), panelWidth - 24)) {
            gui.drawString(this.font, line, x, descY, 0xC8C8C8);
            descY += 10;
        }

        descY += 6;
        gui.drawString(this.font, "Evolusi:", x, descY, 0x9A9A9A);
        descY += 11;
        if (soul.hasEvolution()) {
            MartialSoul evolved = MartialSouls.get(soul.evolutionId());
            if (ClientCultivationData.evolutionStage > 0) {
                gui.drawString(this.font, "Sudah berevolusi menjadi " + evolved.name(), x, descY, 0xB79CFF);
            } else {
                gui.drawString(this.font, "Bisa berevolusi menjadi " + evolved.name()
                        + " (butuh 6 ring + Inti Evolusi)", x, descY, 0x8A7AB8);
            }
        } else {
            gui.drawString(this.font, "Bentuk dasar, tidak punya bentuk evolusi.", x, descY, 0x8A8A8A);
        }
    }

    // ------------------------------------------------------------------ tab SKILLS

    private static final int SLOT_COUNT = 5;
    private final int[] slotBoxLeft = new int[SLOT_COUNT];
    private final int[] slotBoxRight = new int[SLOT_COUNT];
    private final int[] slotBoxTop = new int[SLOT_COUNT];
    private final int[] slotBoxBottom = new int[SLOT_COUNT];
    private final List<String> skillRowIds = new ArrayList<>();
    private final List<int[]> skillRowBounds = new ArrayList<>();

    private void drawSkillsTab(GuiGraphics gui) {
        MartialSoul soul = currentSoul();
        int x = panelLeft + 12;
        int y = panelTop + 30;
        int width = panelWidth - 24;
        skillRowIds.clear();
        skillRowBounds.clear();

        gui.drawString(this.font, "Klik skill untuk memasangnya ke slot. Klik slot untuk mengosongkan.",
                x, y - 12, 0x8A8A8A);

        int boxWidth = (width - 8) / SLOT_COUNT;
        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            int left = x + slot * (boxWidth + 2);
            SkillDef skill = ClientSkills.resolve(soul, slot);
            slotBoxLeft[slot] = left;
            slotBoxRight[slot] = left + boxWidth;
            slotBoxTop[slot] = y;
            slotBoxBottom[slot] = y + 18;
            gui.fill(left, y, left + boxWidth, y + 18, 0xB014161A);
            if (skill != null) {
                gui.fill(left, y, left + boxWidth, y + 1, skillColor(skill));
                String label = String.valueOf(slot + 1) + ". " + trim(gui, skill.name(), boxWidth - 4);
                gui.drawString(this.font, label, left + 2, y + 6, 0xE0E0E0);
            } else {
                gui.drawString(this.font, String.valueOf(slot + 1) + ". -", left + 2, y + 6, 0x707070);
            }
        }

        int listY = y + 26;
        listY = drawSkillSection(gui, "Skill martial soul", soul == null ? List.of() : soul.skills(), x, listY, width);
        listY += 4;
        drawSkillSection(gui, "Skill spirit ring", unlockedRingSkills(), x, listY, width);
    }

    private List<SkillDef> unlockedRingSkills() {
        List<SkillDef> unlocked = new ArrayList<>();
        List<SkillDef> all = RingSkills.all();
        for (int i = 0; i < Math.min(ClientCultivationData.ringSkills, all.size()); i++) {
            unlocked.add(all.get(i));
        }
        return unlocked;
    }

    private int drawSkillSection(GuiGraphics gui, String title, List<SkillDef> skills, int x, int y, int width) {
        gui.drawString(this.font, title, x, y, 0xE8C46A);
        y += 11;
        for (SkillDef skill : skills) {
            boolean unlocked = ClientSkills.isUnlocked(skill);
            int color = unlocked ? 0xE0E0E0 : 0x707070;
            String label = trim(gui, skill.name(), width - 70);
            gui.drawString(this.font, label, x, y, color);
            String meta = "e" + (int) skill.energyCost() + " " + (skill.cooldownTicks() / 20) + "s";
            gui.drawString(this.font, meta, x + width - 46, y, 0x7A7A7A);
            if (unlocked) {
                gui.drawString(this.font, "Pakai", x + width - 18, y, 0x4FC08D);
            }
            skillRowIds.add(skill.id());
            skillRowBounds.add(new int[]{x, y - 2, x + width, y + 10});
            y += 11;
        }
        return y;
    }

    private String trim(GuiGraphics gui, String text, int maxWidth) {
        if (this.font.width(text) <= maxWidth) return text;
        String cut = text;
        while (cut.length() > 1 && this.font.width(cut + "..") > maxWidth) {
            cut = cut.substring(0, cut.length() - 1);
        }
        return cut + "..";
    }

    private int skillColor(SkillDef skill) {
        return switch (skill.target()) {
            case SELF -> 0x4FC08D;
            case ENEMY_NEAREST, ENEMY_AOE, ENEMY_LINE, ENEMY_CONE, POINT_AOE -> 0xD8442F;
            case ALLY_AURA -> 0xE8C46A;
        };
    }

    // ------------------------------------------------------------------ tab RINGS

    private void drawRingsTab(GuiGraphics gui) {
        int x = panelLeft + 12;
        int y = panelTop + 30;
        gui.drawString(this.font, "Sembilan spirit ring melekat di tubuhmu.", x, y - 12, 0x8A8A8A);

        Minecraft mc = Minecraft.getInstance();
        int slotSize = 20;
        int gap = 3;
        for (int i = 0; i < 9; i++) {
            int left = x + i * (slotSize + gap);
            gui.fill(left, y, left + slotSize, y + slotSize, 0xB014161A);
            gui.fill(left, y, left + slotSize, y + 1, 0x50E8C46A);
            ItemStack stack = mc.player == null ? ItemStack.EMPTY : CuriosBridge.ringInSlot(mc.player, i);
            if (!stack.isEmpty()) {
                gui.renderItem(stack, left + 2, y + 2);
                gui.drawString(this.font, String.valueOf(i + 1), left + 12, y + 10, 0xE8C46A);
            } else {
                gui.drawString(this.font, String.valueOf(i + 1), left + 6, y + 6, 0x707070);
            }
        }
        y += slotSize + 8;

        int[] tierYears = {100, 100, 1000, 1000, 10000, 10000, 100000, 100000, 100000};
        for (int i = 0; i < 9; i++) {
            boolean owned = i < ClientCultivationData.rings;
            int color = owned ? IMPERIAL_GOLD : 0x707070;
            gui.drawString(this.font, "Cincin " + (i + 1), x, y, color);
            gui.drawString(this.font, owned ? tierYears[i] + " tahun" : "belum diserap", x + 56, y, color);
            gui.drawString(this.font, "HP +2  attack +0.5", x + 140, y, owned ? 0x8A9A8A : 0x606060);
            y += 12;
        }

        y += 4;
        gui.drawString(this.font, "Tahan Shift + klik kanan ring: absorpsi brave, 55% chance naik tingkat.",
                x, y, 0xD8A05A);
        y += 11;
        gui.drawString(this.font, "Tekan X: ledakan spirit ring, damage mengikuti jumlah ring.",
                x, y, SOUL_JADE);
    }

    // ------------------------------------------------------------------ tab SECT

    private void drawSectTab(GuiGraphics gui, int mouseX, int mouseY) {
        int x = panelLeft + 12;
        int y = panelTop + 30;
        gui.drawString(this.font, "Sekte memberi bonus pasif dan bisa diganti kapan saja.",
                x, y - 12, 0x8A8A8A);

        int index = 0;
        for (Sect sect : Sect.all()) {
            boolean current = sect.id().equals(ClientCultivationData.sect);
            int color = current ? IMPERIAL_GOLD : 0xC8C8C8;
            if (mouseX >= x && mouseX <= panelLeft + panelWidth - 12 && mouseY >= y - 2 && mouseY <= y + 9) {
                color = 0xFFFFFF;
            }
            gui.drawString(this.font, (current ? "> " : "  ") + sect.name(), x, y, color);
            y += 11;
            for (FormattedCharSequence line : this.font.split(Component.literal(sect.description()),
                    panelWidth - 30)) {
                gui.drawString(this.font, line, x + 12, y, 0x8A8A8A);
                y += 9;
            }
            y += 5;
            index++;
        }
    }

    // ------------------------------------------------------------------ input

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int mx = (int) mouseX;
        int my = (int) mouseY;
        for (int i = 0; i < TAB_NAMES.length; i++) {
            if (mx >= tabLeft[i] && mx <= tabRight[i] && my >= tabTop[i] && my <= tabTop[i] + 10) {
                activeTab = i;
                return true;
            }
        }
        if (activeTab == 1) {
            for (int slot = 0; slot < SLOT_COUNT; slot++) {
                if (mx >= slotBoxLeft[slot] && mx <= slotBoxRight[slot]
                        && my >= slotBoxTop[slot] && my <= slotBoxBottom[slot]) {
                    ModNetwork.CHANNEL.sendToServer(new AssignSkillPacket(slot, ""));
                    return true;
                }
            }
            for (int i = 0; i < skillRowBounds.size(); i++) {
                int[] bounds = skillRowBounds.get(i);
                if (mx >= bounds[0] && mx <= bounds[2] && my >= bounds[1] && my <= bounds[3]) {
                    int freeSlot = firstFreeSlot();
                    if (freeSlot >= 0) {
                        ModNetwork.CHANNEL.sendToServer(new AssignSkillPacket(freeSlot, skillRowIds.get(i)));
                    }
                    return true;
                }
            }
        }
        if (activeTab == 3 && mouseX >= panelLeft + 12 && mouseX <= panelLeft + panelWidth - 12) {
            int index = sectIndexAt(my);
            if (index >= 0 && index < Sect.all().size()) {
                Sect sect = Sect.all().get(index);
                ModNetwork.CHANNEL.sendToServer(new ChooseSectPacket(sect.id()));
                onClose();
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private int firstFreeSlot() {
        String[] loadout = ClientCultivationData.loadout;
        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            if (loadout == null || slot >= loadout.length) return slot;
            if (loadout[slot] == null || loadout[slot].isEmpty()) return slot;
        }
        return -1;
    }

    private int sectIndexAt(int mouseY) {
        int y = panelTop + 30;
        for (int i = 0; i < Sect.all().size(); i++) {
            Sect sect = Sect.all().get(i);
            y += 11;
            int lines = this.font.split(Component.literal(sect.description()), panelWidth - 30).size();
            y += lines * 9 + 5;
            if (mouseY < y) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (int i = 0; i < TAB_KEYS.length; i++) {
            if (keyCode == TAB_KEYS[i]) {
                activeTab = i;
                return true;
            }
        }
        if (keyCode == GLFW.GLFW_KEY_J) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(null);
    }

    private MartialSoul currentSoul() {
        return MartialSouls.effective(ClientCultivationData.soulId, ClientCultivationData.evolutionStage);
    }

    private static int rarityColor(String rarity) {
        return switch (rarity) {
            case "Common" -> 0xB0B0B0;
            case "Uncommon" -> 0x7FD48A;
            case "Rare" -> 0x6AA9E8;
            case "Epic" -> 0xB79CFF;
            case "Legendary" -> IMPERIAL_GOLD;
            default -> 0xFFFFFF;
        };
    }
}
