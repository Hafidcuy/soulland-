package com.soulland.screen;

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

    private static final String[] TITLES = {
            "Mulai", "Martial Soul", "Skill dan Loadout", "Spirit Ring",
            "Kultivasi", "Sekte", "Pertarungan"
    };

    private static final String[][] BODIES = {
            {
                    "Saat pertama masuk dunia, kamu undergoes upward ceremony: martial soul",
                    "diundi secara acak, dan ritual itu diulang setiap kali kamu masuk lagi.",
                    "",
                    "Urutan yang wajar:",
                    "1. Bunuh soul beast untuk mendapat XP kultivasi.",
                    "2. Serap spirit ring yang jatuh dari soul beast.",
                    "3. Absorpsi brave: tahan Shift, bisa gagal tapi imbalannya lebih besar.",
                    "4. Rakit loadout skill di tab SKILLS pada kartu soul.",
                    "5. Saat naik peringkat, tekan C untuk upacara terobosan."
            },
            {
                    "Pool undian berisi 1.434 martial soul dari seluruh Benua Douluo:",
                    "bentuk dasar, bentuk evolusi, bentuk tahun ring, soul gabungan, dan",
                    "body soul setelah Ignition kedua.",
                    "",
                    "Setiap soul punya satu elemen: Api, Es, Petir, Angin, Air, Tanah,",
                    "Racun, Tumbuhan, Cahaya, atau Gelap. Elemen itu menentukan",
                    "bagian mana dari soul beast yang mudah kamu kalahkan.",
                    "",
                    "Undi ulang manual hanya di mode Creative: /martialsoul reroll"
            },
            {
                    "Setiap martial soul punya 5 skill yang terbuka di spirit ring",
                    "1, 2, 3, 5, dan 7. Tiap spirit ring juga membuka satu skill ring.",
                    "",
                    "Lima slot tombol (R G F V Z) bisa diisi skill bebas. Buka kartu soul",
                    "dengan J, masuk tab SKILLS, lalu klik skill untuk memasangnya.",
                    "Klik slot untuk mengosongkan dan kembali ke urutan bawaan.",
                    "",
                    "Tiap skill punya bentuk efek sendiri: bola, garis, kerucut, cincin,",
                    "cakram, pilar, atau badai. Tidak ada dua skill yang sama."
            },
            {
                    "Spirit ring datang dari soul beast, sesuai usianya: 100 tahun,",
                    "1.000 tahun, 10.000 tahun, dan 100.000 tahun. Tiap ring memberi",
                    "+2 HP maksimum dan +0.5 attack damage.",
                    "",
                    "Absorpsi biasa selalu berhasil. Absorpsi brave (tahan Shift) biaya",
                    "20% HP dan 40 Spirit Power, dengan peluang 55% ring naik satu",
                    "tingkat dan satu skill ring tambahan terbuka. Kalau gagal, ring",
                    "hancur dan kamu kehilangan 25% HP maksimum.",
                    "",
                    "Tekan X untuk ledakan spirit ring: seluruh energi ring dilepas",
                    "sekaligus, radius dan damage mengikuti jumlah ring."
            },
            {
                    "Level kultivasi 1 sampai 99, sepuluh peringkat dari Spirit Scholar",
                    "sampai Title Douluo. Setiap 10 level ada gerbang peringkat.",
                    "",
                    "Gerbang tidak bisa dilewati hanya dengan XP. Kamu harus menjalankan",
                    "upacara terobosan: tahan C dan bermeditasi 3 detik penuh,",
                    "sekaligus memakai 1 Esensi Jiwa.",
                    "",
                    "Meditasi biasa (tahan C) mengisi Spirit Power dan menambah XP.",
                    "Bergerak akan memutus meditasi."
            },
            {
                    "Sekte memberi bonus pasif yang selalu berlaku dan tidak pernah",
                    "merugikan. Pilih lewat tab SECT pada kartu soul, atau klik lencana",
                    "sekte langsung.",
                    "",
                    "Sekte Tang: +20% XP kultivasi.",
                    "Sekte Shrek: +12% damage.",
                    "Sekte Bintang Luo: +10% kecepatan gerak dan +10% HP maksimum.",
                    "Sekte Tujuh Harta: +20% HP maksimum dan +25% penyembuhan."
            },
            {
                    "Rantai soul: setiap skill yang berhasil menambah satu mata rantai.",
                    "Rantai bertahan 4 detik, tiap mata menambah 8% damage skill",
                    "berikutnya, maksimal 5 mata (+40%).",
                    "",
                    "Lawan elemen: nama soul beast menyebut elemennya. +25% damage",
                    "kalau elemen soul-mu menang, -15% kalau kalah.",
                    "",
                    "Bakar darah (B): tukar 25 HP jadi 30 Spirit Power, 8 detik",
                    "damage +50%. Tidak boleh dipakai di bawah 35 HP.",
                    "",
                    "Boss: Elder Guardian 120 HP, Wither 600 HP, Warden 800 HP,",
                    "Ender Dragon 1000 HP sebagai boss akhir."
            }
    };

    private static final ResourceLocation PANEL =
            new ResourceLocation("soulland", "textures/gui/panel_guide.png");

    private int activeSection = 0;
    private int panelLeft;
    private int panelTop;
    private int panelWidth;
    private int panelHeight;
    private final List<int[]> navBounds = new ArrayList<>();

    public GuideScreen() {
        super(Component.translatable("screen.soulland.guide"));
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
        gui.drawString(this.font, "PANDUAN", x, y - 10, IMPERIAL_GOLD);
        for (int i = 0; i < TITLES.length; i++) {
            int top = y + i * 24;
            boolean active = i == activeSection;
            boolean hovered = mouseX >= x - 2 && mouseX <= x + 128 && mouseY >= top && mouseY <= top + 18;
            ResourceLocation icon = new ResourceLocation("soulland", "textures/item/" + ICONS[i] + ".png");
            gui.blit(icon, x, top, 0, 0, 16, 16, 16, 16);
            gui.drawString(this.font, TITLES[i], x + 20, top + 4, active ? PAPER : (hovered ? 0xFFFFFF : 0x909090));
            if (active) {
                gui.fill(x + 20, top + 14, x + 20 + this.font.width(TITLES[i]), top + 15, SOUL_JADE);
            }
            navBounds.add(new int[]{x - 2, top, x + 130, top + 18});
        }
    }

    private void drawContent(GuiGraphics gui) {
        int x = panelLeft + 150;
        int y = panelTop + 16;
        int width = panelWidth - 162;
        gui.drawString(this.font, TITLES[activeSection], x, y, PAPER);
        gui.fill(x, y + 11, x + this.font.width(TITLES[activeSection]), y + 12, IMPERIAL_GOLD);
        y += 20;

        for (String line : BODIES[activeSection]) {
            if (line.isEmpty()) {
                y += 6;
                continue;
            }
            for (FormattedCharSequence wrapped : this.font.split(Component.literal(line), width)) {
                gui.drawString(this.font, wrapped, x, y, 0xC8C0A8);
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
            activeSection = (activeSection + 1) % TITLES.length;
            return true;
        }
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_UP) {
            activeSection = (activeSection + TITLES.length - 1) % TITLES.length;
            return true;
        }
        for (int i = 0; i < TITLES.length; i++) {
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
