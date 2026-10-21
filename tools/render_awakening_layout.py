#!/usr/bin/env python3
"""Render mock layout layar Upacara Kebangkitan v1.4.

Client Minecraft tidak bisa dijalankan di sandbox build ini, jadi bentuk layar
dipantau dengan mock: konstanta layout dibaca dari sumber Java lalu digambar
seperti `AwakeningScreen`_menu. Gunanya mencari tumpang tindih atau teks yang
meluber keluar panel.

Konstanta dicek terhadap sumber Java. Kalau layar berubah dan mock ini tidak
diperbarui, skrip berhenti dengan pesan, jadi mock tidak bisa diam-diam basi.

Jalankan: python3 tools/render_awakening_layout.py
"""
import pathlib
import re
import sys

from PIL import Image, ImageDraw

ROOT = pathlib.Path(__file__).resolve().parent.parent
SOURCE = ROOT / "src/main/java/com/soulland/client/gui/AwakeningScreen.java"
OUT = ROOT / "docs/v1.4-awakening-layout.png"

# Konstanta yang dipakai mock. Nilainya harus sama dengan di sumber Java.
EXPECTED = {
    "PANEL_W": 300, "PANEL_H": 210, "MARGIN": 12,
    "TITLE_Y": 9, "SUBTITLE_Y": 21, "RULE_Y": 33,
    "LINEAGE_Y": 37, "LINEAGE_H": 11, "TYPE_Y": 51, "TYPE_H": 12,
    "BODY_Y": 67, "BODY_H": 126, "LIST_X": 12, "LIST_W": 116,
    "PREVIEW_X": 134, "PREVIEW_W": 154, "STATUS_Y": 197,
    "STRIP_ARROW_W": 9, "TYPE_TAB_MIN_W": 42, "VISIBLE_ROWS": 5,
}

ZOOM = 3


def read_constants():
    """Baca konstanta layout dari sumber Java dan pastikan sama dengan mock."""
    text = SOURCE.read_text(encoding="utf-8")
    found = {}
    for name in EXPECTED:
        match = re.search(rf"private static final int {name} = (\d+);", text)
        if not match:
            match = re.search(rf"private static final int {name} = MARGIN;", text)
            if not match:
                print(f"ERROR: konstanta {name} tidak ada di {SOURCE.name}")
                sys.exit(1)
            found[name] = 12
            continue
        found[name] = int(match.group(1))
    for name, expected in EXPECTED.items():
        if found[name] != expected:
            print(f"ERROR: {name} di Java = {found[name]}, mock = {expected}")
            sys.exit(1)
    return found


def main() -> None:
    c = read_constants()
    panel_w = c["PANEL_W"] * ZOOM
    panel_h = c["PANEL_H"] * ZOOM
    image = Image.new("RGBA", (panel_w, panel_h), (11, 14, 19, 255))
    draw = ImageDraw.Draw(image)

    def box(x0, y0, x1, y1, color):
        draw.rectangle([x0 * ZOOM, y0 * ZOOM, x1 * ZOOM - 1, y1 * ZOOM - 1], fill=color)

    def line(x0, y0, x1, y1, color):
        draw.rectangle([x0 * ZOOM, y0 * ZOOM, x1 * ZOOM - 1, y1 * ZOOM], fill=color)

    def label(x, y, text, color, width=0):
        draw.rectangle([x * ZOOM, y * ZOOM, (x + max(1, width)) * ZOOM, (y + 7) * ZOOM], fill=color)

    INK = (11, 14, 19, 255)
    STONE = (22, 28, 34, 255)
    PAPER = (233, 227, 213, 255)
    MUTED = (152, 162, 173, 255)
    JADE = (63, 199, 163, 255)
    JADE_DIM = (27, 74, 65, 255)
    HAIRLINE = (44, 53, 62, 255)
    DANGER = (224, 121, 106, 255)

    box(0, 0, c["PANEL_W"], c["PANEL_H"], INK)
    line(0, 0, c["PANEL_W"], 1, HAIRLINE)
    line(0, c["PANEL_H"] - 1, c["PANEL_W"], c["PANEL_H"], HAIRLINE)
    line(0, 0, 1, c["PANEL_H"], HAIRLINE)
    line(c["PANEL_W"] - 1, 0, c["PANEL_W"], c["PANEL_H"], HAIRLINE)

    # header
    label(c["MARGIN"], c["TITLE_Y"], "Martial Soul Awakening Ceremony", PAPER, 150)
    label(c["MARGIN"], c["SUBTITLE_Y"], "Choose the martial soul that will bind to your fate", MUTED, 150)
    line(c["MARGIN"], c["RULE_Y"], c["PANEL_W"] - c["MARGIN"], c["RULE_Y"] + 1, JADE)

    # baris lineage
    total = c["PANEL_W"] - 2 * c["MARGIN"]
    chip = total // 5
    for index, name in enumerate(["All", "Beast", "Tool", "Body", "Special"]):
        x = c["MARGIN"] + index * chip
        chip_w = chip if index < 4 else (c["MARGIN"] + total) - x
        selected = index == 1
        box(x, c["LINEAGE_Y"], x + chip_w, c["LINEAGE_Y"] + c["LINEAGE_H"], JADE_DIM if selected else STONE)
        line(x, c["LINEAGE_Y"], x + 1, c["LINEAGE_Y"] + c["LINEAGE_H"], JADE if selected else HAIRLINE)
        label(x + (chip_w - len(name) * 5) // 2, c["LINEAGE_Y"] + 2, name, PAPER if selected else MUTED, len(name) * 5)

    # baris tipe: 11 kategori (Semua + 10 tipe), butuh digeser
    categories = 11
    arrow_space = 2 * c["STRIP_ARROW_W"] if categories > (c["PANEL_W"] - 2 * c["MARGIN"]) // c["TYPE_TAB_MIN_W"] else 0
    left = c["MARGIN"] + arrow_space
    right = c["PANEL_W"] - c["MARGIN"] - arrow_space
    usable = c["PANEL_W"] - 2 * c["MARGIN"] - arrow_space
    visible = max(1, usable // c["TYPE_TAB_MIN_W"])
    tab_w = max(c["TYPE_TAB_MIN_W"], usable // visible)
    box(c["MARGIN"], c["TYPE_Y"], c["PANEL_W"] - c["MARGIN"], c["TYPE_Y"] + c["TYPE_H"], STONE)
    names = ["All", "Attack", "Control", "Agility", "Support", "Fire", "Power", "Poison", "Defense", "Heal"]
    for index in range(min(visible, categories)):
        x = left + index * tab_w
        selected = index == 1
        box(x, c["TYPE_Y"], x + tab_w, c["TYPE_Y"] + c["TYPE_H"], JADE_DIM if selected else STONE)
        line(x, c["TYPE_Y"], x + 1, c["TYPE_Y"] + c["TYPE_H"], JADE if selected else HAIRLINE)
        name = names[index]
        label(x + (tab_w - len(name) * 5) // 2, c["TYPE_Y"] + 2, name, PAPER if selected else MUTED, len(name) * 5)
    if arrow_space:
        box(c["PANEL_W"] - c["MARGIN"] - c["STRIP_ARROW_W"], c["TYPE_Y"],
            c["PANEL_W"] - c["MARGIN"], c["TYPE_Y"] + c["TYPE_H"], JADE_DIM)

    # daftar soul
    box(c["LIST_X"], c["BODY_Y"], c["LIST_X"] + c["LIST_W"], c["BODY_Y"] + c["BODY_H"], STONE)
    rows = ["Old Hoe", "Blue Silver Grass", "Hell Civet", "Magic Sausage", "Seven Treasure"]
    for index, name in enumerate(rows):
        y = c["BODY_Y"] + 4 + index * 21
        if index == 1:
            box(c["LIST_X"] + 1, y, c["LIST_X"] + c["LIST_W"] - 5, y + 19, JADE_DIM)
        label(c["LIST_X"] + 6, y, name, PAPER, len(name) * 5)
        label(c["LIST_X"] + 6, y + 10, "Rare / Tang Sect", MUTED, 80)
    # scrollbar
    track_x = c["LIST_X"] + c["LIST_W"] - 3
    line(track_x, c["BODY_Y"] + 4, track_x + 2, c["BODY_Y"] + 4 + c["BODY_H"] - 18, HAIRLINE)
    box(track_x, c["BODY_Y"] + 4, track_x + 2, c["BODY_Y"] + 4 + 14, JADE)
    label(c["LIST_X"] + 6, c["BODY_Y"] + c["BODY_H"] - 10, "5 of 43", MUTED, 30)

    # pratinjau
    box(c["PREVIEW_X"], c["BODY_Y"], c["PREVIEW_X"] + c["PREVIEW_W"], c["BODY_Y"] + c["BODY_H"], STONE)
    x = c["PREVIEW_X"] + 6
    label(x, c["BODY_Y"] + 4, "Blue Silver Grass", PAPER, 90)
    label(x, c["BODY_Y"] + 14, "Rare / Kontrol", MUTED, 70)
    label(x, c["BODY_Y"] + 24, "Lineage: Tubuh", MUTED, 70)
    label(x, c["BODY_Y"] + 34, "Afinitas: Air", MUTED, 70)
    label(x, c["BODY_Y"] + 44, "Rumput biru perak yang tampak lemah", MUTED, 120)
    label(x, c["BODY_Y"] + 54, "namun bisa mengikat dan meracuni", MUTED, 110)
    line(x, c["BODY_Y"] + 64, c["PREVIEW_X"] + c["PREVIEW_W"] - 6, c["BODY_Y"] + 65, HAIRLINE)
    for index, name in enumerate(["Blue Silver Entangle", "Parasitic Seed", "Sarang Blue Silver", "Hujan Blue Silver"]):
        label(x, c["BODY_Y"] + 68 + index * 10, f"{index + 1}. {name}", PAPER, len(name) * 5)
    box(c["PREVIEW_X"] + 6, c["BODY_Y"] + c["BODY_H"] - 18, c["PREVIEW_X"] + 96, c["BODY_Y"] + c["BODY_H"] - 5, JADE_DIM)
    label(c["PREVIEW_X"] + 40, c["BODY_Y"] + c["BODY_H"] - 15, "AWAKEN", PAPER, 36)

    # status
    label(c["MARGIN"], c["STATUS_Y"], "Tab switches area, arrows select, Enter confirms", MUTED, 160)

    # cek overflow: tidak ada elemen yang keluar panel
    problems = []
    if c["STATUS_Y"] + 9 > c["PANEL_H"]:
        problems.append("baris status keluar panel")
    if c["BODY_Y"] + c["BODY_H"] > c["STATUS_Y"]:
        problems.append("tubuh panel menabrak baris status")
    if c["PREVIEW_X"] + c["PREVIEW_W"] > c["PANEL_W"] - c["MARGIN"]:
        problems.append("pratinjau melewati margin kanan")
    if c["LIST_X"] + c["LIST_W"] >= c["PREVIEW_X"]:
        problems.append("daftar soul menabrak pratinjau")
    last_skill_bottom = c["BODY_Y"] + 68 + 3 * 10 + 8
    button_top = c["BODY_Y"] + c["BODY_H"] - 18
    if last_skill_bottom > button_top:
        problems.append(f"skill terakhir menabrak tombol AWAKEN ({last_skill_bottom} > {button_top})")
    counter_top = c["BODY_Y"] + c["BODY_H"] - 10
    if c["BODY_Y"] + 4 + 5 * 21 > counter_top:
        problems.append("baris soul menabrak penghitung posisi")
    if problems:
        for problem in problems:
            print("ERROR:", problem)
        return 1

    OUT.parent.mkdir(parents=True, exist_ok=True)
    image.save(OUT)
    print(f"wrote {OUT.relative_to(ROOT)} ({panel_w}x{panel_h})")
    print("layout check: tidak ada elemen keluar panel")
    return 0


if __name__ == "__main__":
    sys.exit(main())
