package com.soulland.client.gui;

import com.soulland.SoulLandMod;
import com.soulland.alchemy.AlchemyRecipe;
import com.soulland.alchemy.AlchemyRecipes;
import com.soulland.lang.SoulLang;
import com.soulland.net.CraftAlchemyPacket;
import com.soulland.net.ModNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * Layar stasiun alchemy.
 *
 * <p>Satu panel berisi satu baris per resep: hasil racikan di kiri, bahan yang
 * dibawa pemain di bawahnya, dan tombol racik di kanan. Baris yang bahannya
 * kurang diberi penanda merah, jadi pemain tahu persis apa yang masih kurang
 * tanpa perlu membuka inventaris.
 *
 * <p>Kerangka visualnya sama dengan {@link LanguageSelectScreen} dan
 * {@link AwakeningScreen}: tinta pekat, batu, kertas, dan jade sebagai satu
 *-satunya aksen. Ikon memakai gambar item bawaan mod, bukan emoji.
 */
public class AlchemyScreen extends Screen {

    private static final int PANEL_W = 268;
    private static final int PANEL_H = 176;

    private static final int MARGIN = 12;
    private static final int TITLE_Y = 9;
    private static final int SUBTITLE_Y = 21;
    private static final int RULE_Y = 33;
    private static final int BODY_Y = 42;
    private static final int ROW_H = 38;
    private static final int ROW_STEP = 40;
    private static final int STATUS_Y = 165;

    private static final int ICON_X = MARGIN + 4;
    private static final int TEXT_X = MARGIN + 24;
    private static final int BUTTON_W = 44;
    private static final int BUTTON_H = 12;

    private static final int INK = 0xF00B0E13;
    private static final int STONE = 0xFF161C22;
    private static final int PAPER = 0xFFE9E3D5;
    private static final int MUTED = 0xFF98A2AD;
    private static final int JADE = 0xFF3FC7A3;
    private static final int JADE_DIM = 0xFF1B4A41;
    private static final int HAIRLINE = 0xFF2C353E;
    private static final int DANGER = 0xFFE0796A;

    private final String language;
    private final List<AlchemyRecipe> recipes;
    private final BlockPos stationPos;

    private int focus = 0;
    private boolean pending = false;
    /** Hasil racikan terakhir: null berarti belum ada percobaan, true berarti berhasil. */
    private Boolean lastCraftSucceeded = null;

    public AlchemyScreen(BlockPos stationPos) {
        super(Component.literal(""));
        this.language = SoulLang.activeLanguage();
        this.recipes = AlchemyRecipes.all();
        this.stationPos = stationPos;
    }

    private Component text(String key, String fallback) {
        return SoulLang.text(language, key, fallback);
    }

    private Component text(String key, String fallback, Object... args) {
        return SoulLang.text(language, key, fallback, args);
    }

    // ---------------------------------------------------------------- input

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        switch (keyCode) {
            case GLFW.GLFW_KEY_ESCAPE -> {
                onClose();
                return true;
            }
            case GLFW.GLFW_KEY_UP -> {
                moveFocus(-1);
                return true;
            }
            case GLFW.GLFW_KEY_DOWN -> {
                moveFocus(1);
                return true;
            }
            case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_SPACE, GLFW.GLFW_KEY_KP_ENTER -> {
                craft(focus);
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
        for (int i = 0; i < recipes.size(); i++) {
            int rowY = rowY(i);
            if (ly < rowY || ly >= rowY + ROW_H) continue;
            if (lx >= buttonX() && lx < buttonX() + BUTTON_W) {
                craft(i);
                return true;
            }
            if (lx >= MARGIN && lx < PANEL_W - MARGIN) {
                focus = i;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void moveFocus(int direction) {
        if (recipes.isEmpty()) return;
        focus = (focus + direction + recipes.size()) % recipes.size();
    }

    private void craft(int index) {
        if (pending || index < 0 || index >= recipes.size()) return;
        pending = true;
        lastCraftSucceeded = null;
        ModNetwork.CHANNEL.sendToServer(new CraftAlchemyPacket(recipes.get(index).id(), stationPos));
    }

    /** Dipanggil dari client hook ketika server menjawab permintaan racik. */
    public void onServerResult(boolean crafted, String recipeId) {
        pending = false;
        lastCraftSucceeded = crafted;
        for (int i = 0; i < recipes.size(); i++) {
            if (recipes.get(i).id().equals(recipeId)) {
                focus = i;
                break;
            }
        }
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

    private int rowY(int index) {
        return BODY_Y + index * ROW_STEP;
    }

    private int buttonX() {
        return PANEL_W - MARGIN - BUTTON_W;
    }

    // ---------------------------------------------------------------- data

    private Item item(String itemId) {
        if (itemId == null || itemId.isEmpty()) return null;
        return ForgeRegistries.ITEMS.getValue(new ResourceLocation(SoulLandMod.MODID, itemId));
    }

    private int count(String itemId) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return 0;
        Item item = item(itemId);
        if (item == null) return 0;
        int total = 0;
        for (ItemStack stack : mc.player.getInventory().items) {
            if (stack.is(item)) total += stack.getCount();
        }
        for (ItemStack stack : mc.player.getInventory().offhand) {
            if (stack.is(item)) total += stack.getCount();
        }
        for (ItemStack stack : mc.player.getInventory().armor) {
            if (stack.is(item)) total += stack.getCount();
        }
        return total;
    }

    private boolean canCraft(AlchemyRecipe recipe) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.getAbilities().instabuild) return true;
        for (AlchemyRecipe.Ingredient ingredient : recipe.ingredients()) {
            if (count(ingredient.itemId()) < ingredient.count()) return false;
        }
        return true;
    }

    private String itemName(String itemId) {
        Item item = item(itemId);
        return item == null ? itemId : SoulLang.text(language, item.getDescriptionId(), itemId).getString();
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
        drawRecipes(guiGraphics);
        drawStatus(guiGraphics);

        guiGraphics.pose().popPose();
    }

    private void drawHeader(GuiGraphics guiGraphics) {
        Component title = text("gui.soulland.alchemy.title", "Stasiun Alchemy")
                .copy().withStyle(style -> style.withBold(true));
        guiGraphics.drawString(this.font, title, MARGIN, TITLE_Y, PAPER);
        guiGraphics.drawString(this.font,
                text("gui.soulland.alchemy.subtitle", "Pilih resep, server yang meracik dan memotong bahan"),
                MARGIN, SUBTITLE_Y, MUTED);
        guiGraphics.fill(MARGIN, RULE_Y, PANEL_W - MARGIN, RULE_Y + 1, JADE);
    }

    private void drawRecipes(GuiGraphics guiGraphics) {
        if (recipes.isEmpty()) {
            guiGraphics.drawString(this.font, text("gui.soulland.alchemy.empty", "Belum ada resep yang bisa diracik"),
                    MARGIN, BODY_Y + 6, MUTED);
            return;
        }
        for (int i = 0; i < recipes.size(); i++) {
            drawRecipe(guiGraphics, recipes.get(i), i);
        }
    }

    private void drawRecipe(GuiGraphics guiGraphics, AlchemyRecipe recipe, int index) {
        int y = rowY(index);
        boolean selected = index == focus;
        boolean ready = canCraft(recipe);

        guiGraphics.fill(MARGIN, y, PANEL_W - MARGIN, y + ROW_H, STONE);
        if (selected) {
            guiGraphics.fill(MARGIN + 1, y + 1, PANEL_W - MARGIN - 1, y + ROW_H - 1, JADE_DIM);
        }

        Item result = item(recipe.resultItemId());
        if (result != null) {
            guiGraphics.renderItem(new ItemStack(result), ICON_X, y + 11);
        }

        String amount = recipe.resultCount() + "x ";
        guiGraphics.drawString(this.font, amount + itemName(recipe.resultItemId()), TEXT_X, y + 6,
                ready ? PAPER : MUTED);

        int slot = 0;
        for (AlchemyRecipe.Ingredient ingredient : recipe.ingredients()) {
            int ix = TEXT_X + slot * 40;
            int iy = y + 18;
            Item ingredientItem = item(ingredient.itemId());
            if (ingredientItem != null) {
                guiGraphics.renderItem(new ItemStack(ingredientItem), ix, iy);
            }
            int have = count(ingredient.itemId());
            boolean enough = have >= ingredient.count();
            guiGraphics.drawString(this.font, have + "/" + ingredient.count(), ix + 18, iy + 4,
                    enough ? JADE : DANGER);
            if (!enough) {
                guiGraphics.fill(ix, iy, ix + 16, iy + 16, 0x80301810);
            }
            slot++;
        }

        int bx = buttonX();
        int by = y + 13;
        guiGraphics.fill(bx, by, bx + BUTTON_W, by + BUTTON_H, pending ? STONE : JADE_DIM);
        String label = text("gui.soulland.alchemy.craft", "RACIK").getString();
        int labelColor = pending ? MUTED : PAPER;
        guiGraphics.drawString(this.font, label, bx + (BUTTON_W - this.font.width(label)) / 2, by + 3, labelColor);
        if (selected) {
            outline(guiGraphics, MARGIN, y, PANEL_W - MARGIN - 1, y + ROW_H - 1, JADE);
        }
    }

    private void drawStatus(GuiGraphics guiGraphics) {
        if (pending) {
            guiGraphics.drawString(this.font, text("gui.soulland.alchemy.status.pending", "Meracik..."),
                    MARGIN, STATUS_Y, MUTED);
            return;
        }
        if (lastCraftSucceeded != null) {
            boolean crafted = lastCraftSucceeded;
            guiGraphics.drawString(this.font,
                    text(crafted ? "gui.soulland.alchemy.status.crafted" : "gui.soulland.alchemy.status.failed",
                            crafted ? "Racikan selesai." : "Bahan kurang, racikan gagal."),
                    MARGIN, STATUS_Y, crafted ? JADE : DANGER);
            return;
        }
        guiGraphics.drawString(this.font,
                text("gui.soulland.alchemy.hint", "Panah atas bawah memilih resep, Enter meracik, Escape menutup"),
                MARGIN, STATUS_Y, MUTED);
    }

    private static void outline(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int color) {
        guiGraphics.fill(x1, y1, x2 + 1, y1 + 1, color);
        guiGraphics.fill(x1, y2, x2 + 1, y2 + 1, color);
        guiGraphics.fill(x1, y1, x1 + 1, y2 + 1, color);
        guiGraphics.fill(x2, y1, x2 + 1, y2 + 1, color);
    }
}
