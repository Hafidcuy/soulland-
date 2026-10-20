package com.soulland.client.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Pemenggalan teks di dalam panel GUI.
 *
 * <p>Teks panjang harus dibungkus ke beberapa baris supaya tidak keluar dari
 * panel. Baris yang melebihi batas dipotong dengan elips, jadi tidak ada teks
 * yang menumpuk dengan blok lain di bawahnya.
 */
public final class PanelText {
    private PanelText() {}

    /** Lebar elips dalam piksel font Minecraft. */
    private static final int ELLIPSIS_WIDTH = 6;

    /**
     * Menggambar teks yang dibungkus ke beberapa baris.
     *
     * @param maxLines jumlah baris yang boleh dipakai, sisanya dipotong elips
     * @return jumlah baris yang benar-benar digambar
     */
    public static int drawWrapped(GuiGraphics graphics, Font font, Component text, int x, int y,
                                  int width, int color, int maxLines) {
        List<String> lines = wrap(font, text.getString(), width);
        int shown = Math.min(maxLines, lines.size());
        for (int i = 0; i < shown; i++) {
            String line = lines.get(i);
            if (i == shown - 1 && lines.size() > maxLines) {
                line = font.plainSubstrByWidth(line, Math.max(0, width - ELLIPSIS_WIDTH)) + "...";
            }
            graphics.drawString(font, line, x, y + i * font.lineHeight, color);
        }
        return shown;
    }

    /**
     * Memotong teks jadi baris-baris yang muat dalam lebar yang diberikan.
     *
     * <p>Baris diisi kata per kata. Satu kata yang lebih lebar dari panel
     * dipotong di tengah, supaya tidak ada karakter yang keluar dari panel.
     */
    private static List<String> wrap(Font font, String text, int width) {
        List<String> lines = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String source : text.split(" ")) {
            String word = source;
            if (current.length() == 0) {
                current.append(word);
            } else {
                String candidate = current + " " + word;
                if (font.width(candidate) <= width) {
                    current.setLength(0);
                    current.append(candidate);
                    continue;
                }
                lines.add(current.toString());
                current.setLength(0);
                current.append(word);
            }
            while (font.width(current.toString()) > width && current.length() > 1) {
                int cut = current.length();
                while (cut > 1 && font.width(current.substring(0, cut)) > width) {
                    cut--;
                }
                lines.add(current.substring(0, cut));
                current.delete(0, cut);
            }
        }
        if (current.length() > 0) {
            lines.add(current.toString());
        }
        if (lines.isEmpty()) {
            lines.add("");
        }
        return lines;
    }
}
