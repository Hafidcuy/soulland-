#!/usr/bin/env python3
"""
Generator aset piksel untuk mod Soul Land.

Semua texture dibuat dari kode supaya bisa direproduksi: `python3 tools/generate_assets.py`
menghasilkan ulang berkas yang sama persis.

Aturan visual (lihat DESIGN.md):
- Ukuran 16x16 untuk item dan potret soul, 32x32 untuk tekstur efek.
- Tanpa gradient, hanya warna solid dan 1-2 piksel highlight.
- Silhouette lebih penting dari detail.
- Palet per sprite: gelap (garis), basis, terang (highlight), aksen (mata/energi).
"""

import io
import math
import os
import zlib
from PIL import Image, ImageDraw

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(ROOT, "src", "main", "resources", "assets", "soulland", "textures")

# ------------------------------------------------------------------ palet soul

SOUL_PALETTE = {
    "hoe":                   (0x8B6B3B, 0xB08A55, 0x4E3A20, 0xE0CBA0),
    "sausage":               (0xE07A5F, 0xF5A78D, 0x8E4231, 0xFFE0CE),
    "hell_civet":            (0x6A3FD1, 0x8B5CF6, 0x2E1065, 0xE6D2FF),
    "blue_silver_grass":     (0x3AA0D8, 0x63C2F0, 0x17506F, 0xCFF2FF),
    "white_tiger":           (0xEDEFF5, 0xFFFFFF, 0x8B93AD, 0x6A3FD1),
    "soft_bone_rabbit":      (0xF5D7E4, 0xFFFFFF, 0xB984A0, 0xE0558A),
    "jade_serpent":          (0x4FC08D, 0x7BE0B0, 0x225F45, 0xE9FFF5),
    "seven_treasure_pagoda": (0xF2C14E, 0xFFE9A8, 0x8A6116, 0xFFF6D2),
    "evil_fire_phoenix":     (0xD8442F, 0xFF8A5C, 0x6E1406, 0xFFD24A),
    "mirror_twin_beast":     (0x9AA6B8, 0xE8EEF7, 0x49525F, 0xB79CFF),
    "clear_sky_hammer":      (0xC9A227, 0xF2DE9B, 0x74550F, 0xFFF3C4),
    "golden_dragon":         (0xE8B44A, 0xFFE9A8, 0x94631A, 0xFFF0C0),
    "blue_silver_emperor":   (0x9BE7FF, 0xDFF7FF, 0x2E7FB4, 0xFFFFFF),
    "nine_treasure_pagoda":  (0xFFD24A, 0xFFF3C4, 0x9A6C08, 0xFFFFFF),
    "fire_phoenix":          (0xFF9A3C, 0xFFD9A0, 0x9A4409, 0xFFF0C0),
    "evil_eyed_white_tiger": (0xE6E9F5, 0xFFFFFF, 0x767E9B, 0xB79CFF),
}

OUTLINE = (0x1A1410, 0x1A1410, 0xFF)


# ------------------------------------------------------------------ util gambar

def new_image(size=16):
    return Image.new("RGBA", (size, size), (0, 0, 0, 0))


def disc(draw, cx, cy, r, color):
    draw.ellipse([cx - r, cy - r, cx + r, cy + r], fill=color)


def ellipse(draw, cx, cy, rx, ry, color):
    draw.ellipse([cx - rx, cy - ry, cx + rx, cy + ry], fill=color)


def rect(draw, x0, y0, x1, y1, color):
    draw.rectangle([x0, y0, x1, y1], fill=color)


def tri(draw, points, color):
    draw.polygon(points, fill=color)


def thick_line(draw, x0, y0, x1, y1, width, color):
    draw.line([x0, y0, x1, y1], fill=color, width=width)
    r = width // 2
    draw.ellipse([x1 - r, y1 - r, x1 + r, y1 + r], fill=color)


def add_outline(img, color=(0x14, 0x10, 0x0C, 255)):
    """Tambah garis gelap 1 piksel di tepi setiap piksel yang terisi."""
    src = img.load()
    w, h = img.size
    out = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    dst = out.load()
    for y in range(h):
        for x in range(w):
            if src[x, y][3] > 0:
                dst[x, y] = src[x, y]
    for y in range(h):
        for x in range(w):
            if src[x, y][3] == 0:
                for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                    nx, ny = x + dx, y + dy
                    if 0 <= nx < w and 0 <= ny < h and src[nx, ny][3] > 0:
                        dst[x, y] = color
                        break
    return out


def save(img, *parts):
    path = os.path.join(ASSETS, *parts)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img.save(path)
    return path


def rgba(value, alpha=255):
    return ((value >> 16) & 0xFF, (value >> 8) & 0xFF, value & 0xFF, alpha)


def c(value):
    """Warna palet ditulis sebagai hex integer; PIL butuh tuple RGBA."""
    return rgba(value)



# ------------------------------------------------------------------ varian per soul

def draw_beast_head(img, base, light, dark, accent):
    d = ImageDraw.Draw(img)
    tri(d, [(2, 8), (4, 2), (7, 7)], base)
    tri(d, [(14, 8), (12, 2), (9, 7)], base)
    ellipse(d, 8, 9, 5.2, 4.6, base)
    ellipse(d, 8, 10, 3.6, 3.0, light)
    rect(d, 4, 8, 5, 9, accent)
    rect(d, 11, 8, 12, 9, accent)
    rect(d, 7, 11, 8, 12, dark)


def draw_beast_horned(img, base, light, dark, accent):
    d = ImageDraw.Draw(img)
    thick_line(d, 4, 6, 1, 1, 1, accent)
    thick_line(d, 12, 6, 15, 1, 1, accent)
    ellipse(d, 8, 10, 5.0, 4.4, base)
    ellipse(d, 8, 11, 3.4, 2.8, light)
    rect(d, 4, 9, 5, 10, accent)
    rect(d, 11, 9, 12, 10, accent)
    rect(d, 7, 12, 8, 13, dark)


def draw_beast_winged(img, base, light, dark, accent):
    d = ImageDraw.Draw(img)
    tri(d, [(8, 7), (0, 2), (4, 11)], base)
    tri(d, [(8, 7), (16, 2), (12, 11)], base)
    tri(d, [(8, 8), (2, 4), (4, 10)], light)
    tri(d, [(8, 8), (14, 4), (12, 10)], light)
    ellipse(d, 8, 9, 2.8, 3.2, base)
    rect(d, 6, 8, 6, 9, accent)
    rect(d, 10, 8, 10, 9, accent)
    rect(d, 7, 12, 8, 12, dark)


def draw_beast_coiled(img, base, light, dark, accent):
    d = ImageDraw.Draw(img)
    points = [(12, 4), (8, 6), (5, 9), (8, 12), (12, 12)]
    for i in range(len(points) - 1):
        thick_line(d, points[i][0], points[i][1], points[i + 1][0], points[i + 1][1], 3, base)
    for i in range(len(points) - 1):
        thick_line(d, points[i][0], points[i][1] - 1, points[i + 1][0], points[i + 1][1] - 1, 1, light)
    disc(d, 12, 4, 2.2, base)
    disc(d, 11, 3, 0.8, accent)


def draw_beast_quadruped(img, base, light, dark, accent):
    d = ImageDraw.Draw(img)
    ellipse(d, 8, 9, 5.0, 3.0, base)
    ellipse(d, 8, 8, 3.6, 2.2, light)
    for x in (4, 6, 10, 12):
        thick_line(d, x, 11, x, 14, 1, dark)
    disc(d, 4, 6, 2.0, base)
    disc(d, 3, 6, 0.7, accent)
    thick_line(d, 12, 6, 14, 2, 1, dark)


def draw_beast_clawed(img, base, light, dark, accent):
    d = ImageDraw.Draw(img)
    ellipse(d, 8, 10, 4.6, 3.6, base)
    for i in range(3):
        x = 4 + i * 3
        tri(d, [(x, 7), (x + 2, 3), (x + 3, 7)], accent)
    rect(d, 6, 10, 7, 11, dark)
    rect(d, 9, 10, 10, 11, dark)


def draw_beast_eye(img, base, light, dark, accent):
    d = ImageDraw.Draw(img)
    ellipse(d, 8, 8, 6.0, 4.0, base)
    ellipse(d, 8, 8, 3.6, 2.4, light)
    disc(d, 8, 8, 1.6, accent)
    rect(d, 7, 7, 8, 8, dark)


def draw_beast_fanged(img, base, light, dark, accent):
    d = ImageDraw.Draw(img)
    ellipse(d, 8, 8, 5.4, 4.6, base)
    ellipse(d, 8, 9, 3.8, 3.0, light)
    rect(d, 5, 10, 6, 12, dark)
    rect(d, 10, 10, 11, 12, dark)
    rect(d, 4, 7, 5, 8, accent)
    rect(d, 11, 7, 12, 8, accent)


def draw_plant_blades(img, base, light, dark, accent):
    d = ImageDraw.Draw(img)
    for x0, y0, x1, y1 in ((3, 14, 2, 4), (6, 14, 6, 2), (9, 14, 10, 3), (12, 14, 13, 5)):
        thick_line(d, x0, y0, x1, y1, 2, base)
        thick_line(d, x0 - 1, y0 - 3, x1 - 1, y1, 1, light)
    rect(d, 2, 13, 13, 15, dark)
    disc(d, 8, 8, 1.6, accent)


def draw_plant_bloom(img, base, light, dark, accent):
    d = ImageDraw.Draw(img)
    thick_line(d, 8, 15, 8, 9, 1, dark)
    for a in range(0, 360, 60):
        x = 8 + math.cos(math.radians(a)) * 3
        y = 7 + math.sin(math.radians(a)) * 3
        disc(d, x, y, 2.0, base)
    disc(d, 8, 7, 1.6, accent)
    disc(d, 8, 7, 0.7, light)


def draw_plant_vine(img, base, light, dark, accent):
    d = ImageDraw.Draw(img)
    points = [(4, 15), (5, 11), (9, 9), (10, 5), (13, 3)]
    for i in range(len(points) - 1):
        thick_line(d, points[i][0], points[i][1], points[i + 1][0], points[i + 1][1], 2, base)
        thick_line(d, points[i][0], points[i][1] - 1, points[i + 1][0], points[i + 1][1] - 1, 1, light)
    for x, y in ((5, 12), (9, 8), (11, 4)):
        disc(d, x, y, 1.2, accent)


def draw_plant_thorn(img, base, light, dark, accent):
    d = ImageDraw.Draw(img)
    thick_line(d, 8, 15, 8, 4, 2, base)
    for y in (5, 8, 11):
        tri(d, [(8, y), (12, y - 2), (8, y - 1)], dark)
        tri(d, [(8, y), (4, y - 2), (8, y - 1)], dark)
    disc(d, 8, 3, 1.8, accent)
    rect(d, 6, 12, 9, 13, light)


def draw_dragon_head(img, base, light, dark, accent):
    d = ImageDraw.Draw(img)
    tri(d, [(3, 6), (4, 0), (7, 5)], base)
    tri(d, [(13, 6), (12, 0), (9, 5)], base)
    ellipse(d, 8, 9, 5.0, 4.4, base)
    ellipse(d, 8, 10, 3.6, 3.0, light)
    rect(d, 4, 8, 5, 9, accent)
    rect(d, 11, 8, 12, 9, accent)
    rect(d, 7, 12, 8, 12, dark)
    for side in (-1, 1):
        thick_line(d, 8 + side * 4, 11, 8 + side * 6, 13, 1, light)


def draw_dragon_wyrm(img, base, light, dark, accent):
    d = ImageDraw.Draw(img)
    for i in range(5):
        x = 2 + i * 3
        y = 12 - (i % 2) * 4
        disc(d, x, y, 2.2, base)
        disc(d, x, y - 1, 0.9, light)
    disc(d, 13, 10, 2.4, base)
    rect(d, 12, 9, 13, 10, accent)
    tri(d, [(3, 10), (1, 6), (5, 9)], dark)


def draw_dragon_winged(img, base, light, dark, accent):
    d = ImageDraw.Draw(img)
    tri(d, [(8, 6), (0, 1), (3, 9)], base)
    tri(d, [(8, 6), (16, 1), (13, 9)], base)
    tri(d, [(8, 7), (2, 3), (4, 8)], light)
    tri(d, [(8, 7), (14, 3), (12, 8)], light)
    ellipse(d, 8, 10, 3.2, 2.6, base)
    rect(d, 6, 9, 6, 10, accent)
    rect(d, 10, 9, 10, 10, accent)


def draw_dragon_horned(img, base, light, dark, accent):
    d = ImageDraw.Draw(img)
    for side in (-1, 1):
        thick_line(d, 8 + side * 3, 6, 8 + side * 6, 1, 1, accent)
    ellipse(d, 8, 10, 4.6, 3.8, base)
    ellipse(d, 8, 11, 3.2, 2.4, light)
    rect(d, 5, 9, 6, 10, accent)
    rect(d, 10, 9, 11, 10, accent)
    rect(d, 7, 13, 8, 13, dark)


def draw_tool_hammer(img, base, light, dark, accent):
    d = ImageDraw.Draw(img)
    rect(d, 2, 2, 10, 5, dark)
    rect(d, 3, 2, 9, 3, base)
    rect(d, 4, 2, 4, 3, accent)
    thick_line(d, 6, 5, 6, 15, 2, base)
    thick_line(d, 5, 6, 5, 14, 1, light)


def draw_tool_blade(img, base, light, dark, accent):
    d = ImageDraw.Draw(img)
    tri(d, [(8, 1), (11, 6), (8, 14), (5, 6)], base)
    tri(d, [(8, 3), (9, 6), (8, 12), (7, 6)], light)
    rect(d, 3, 9, 12, 10, dark)
    rect(d, 7, 10, 8, 15, accent)


def draw_tool_staff(img, base, light, dark, accent):
    d = ImageDraw.Draw(img)
    thick_line(d, 8, 15, 8, 5, 2, base)
    disc(d, 8, 4, 2.6, dark)
    disc(d, 8, 4, 1.6, accent)
    rect(d, 6, 9, 9, 10, light)


def draw_tool_ring(img, base, light, dark, accent):
    d = ImageDraw.Draw(img)
    disc(d, 8, 8, 6.0, dark)
    disc(d, 8, 8, 5.0, base)
    disc(d, 8, 8, 3.0, (0, 0, 0, 0))
    for a in range(0, 360, 45):
        disc(d, 8 + math.cos(math.radians(a)) * 4, 8 + math.sin(math.radians(a)) * 4, 0.8, light)
    disc(d, 8, 2, 1.0, accent)


def draw_beast_serpent(img, base, light, dark, accent):
    d = ImageDraw.Draw(img)
    points = [(3, 4), (6, 6), (5, 10), (9, 12), (12, 9), (12, 5)]
    for i in range(len(points) - 1):
        thick_line(d, points[i][0], points[i][1], points[i + 1][0], points[i + 1][1], 2, base)
        thick_line(d, points[i][0], points[i][1] - 1, points[i + 1][0], points[i + 1][1] - 1, 1, light)
    disc(d, 12, 4, 1.8, base)
    disc(d, 12, 3, 0.7, accent)


def draw_beast_spider(img, base, light, dark, accent):
    d = ImageDraw.Draw(img)
    disc(d, 8, 9, 2.6, base)
    disc(d, 8, 9, 1.4, light)
    for i in range(3):
        y = 7 + i * 2
        thick_line(d, 6, y, 2, y - 2, 1, dark)
        thick_line(d, 10, y, 14, y - 2, 1, dark)
    rect(d, 7, 6, 8, 7, accent)


def draw_beast_fish(img, base, light, dark, accent):
    d = ImageDraw.Draw(img)
    ellipse(d, 7, 8, 4.6, 2.8, base)
    ellipse(d, 6, 8, 3.0, 1.8, light)
    tri(d, [(11, 8), (15, 5), (15, 11)], dark)
    rect(d, 5, 7, 6, 8, accent)


def draw_beast_antler(img, base, light, dark, accent):
    d = ImageDraw.Draw(img)
    thick_line(d, 8, 15, 8, 8, 2, base)
    ellipse(d, 8, 6, 3.0, 2.8, base)
    for side in (-1, 1):
        thick_line(d, 8 + side * 2, 4, 8 + side * 5, 1, 1, accent)
        thick_line(d, 8 + side * 3, 3, 8 + side * 5, 5, 1, light)
    rect(d, 6, 5, 6, 6, dark)
    rect(d, 9, 5, 9, 6, dark)


def draw_element_orb(img, base, light, dark, accent):
    d = ImageDraw.Draw(img)
    disc(d, 8, 8, 5.2, dark)
    disc(d, 8, 8, 4.2, base)
    disc(d, 7, 7, 1.6, light)
    disc(d, 10, 10, 1.0, accent)


def draw_element_flame(img, base, light, dark, accent):
    d = ImageDraw.Draw(img)
    tri(d, [(8, 1), (12, 9), (8, 15), (4, 9)], base)
    tri(d, [(8, 4), (10, 9), (8, 13), (6, 9)], light)
    disc(d, 8, 9, 1.2, accent)


def draw_element_shard(img, base, light, dark, accent):
    d = ImageDraw.Draw(img)
    tri(d, [(8, 1), (13, 8), (8, 14), (3, 8)], dark)
    tri(d, [(8, 3), (11, 8), (8, 12), (5, 8)], base)
    tri(d, [(8, 5), (9, 8), (8, 10), (7, 8)], light)
    disc(d, 8, 1, 1.0, accent)


def draw_body_eye(img, base, light, dark, accent):
    d = ImageDraw.Draw(img)
    ellipse(d, 8, 8, 6.0, 4.0, base)
    ellipse(d, 8, 8, 3.6, 2.4, light)
    disc(d, 8, 8, 1.6, accent)
    rect(d, 7, 7, 8, 8, dark)


def draw_body_hand(img, base, light, dark, accent):
    d = ImageDraw.Draw(img)
    rect(d, 5, 8, 10, 15, base)
    for x in (4, 6, 8, 10, 12):
        rect(d, x, 3 + abs(x - 8) // 2, x, 8, light)
    rect(d, 5, 8, 6, 15, dark)
    disc(d, 8, 6, 1.2, accent)


def draw_body_bone(img, base, light, dark, accent):
    d = ImageDraw.Draw(img)
    thick_line(d, 4, 12, 12, 4, 2, base)
    for x, y in ((4, 12), (12, 4)):
        disc(d, x - 1, y, 1.6, light)
        disc(d, x + 1, y, 1.6, light)
    disc(d, 8, 8, 1.4, accent)


def draw_humanoid_figure(img, base, light, dark, accent):
    d = ImageDraw.Draw(img)
    disc(d, 8, 5, 2.6, base)
    tri(d, [(4, 15), (8, 8), (12, 15)], base)
    tri(d, [(5, 15), (8, 9), (7, 15)], light)
    rect(d, 7, 3, 8, 4, accent)
    rect(d, 4, 14, 11, 15, dark)


def draw_humanoid_angel(img, base, light, dark, accent):
    d = ImageDraw.Draw(img)
    tri(d, [(8, 6), (1, 2), (4, 10)], base)
    tri(d, [(8, 6), (15, 2), (12, 10)], base)
    disc(d, 8, 6, 2.4, light)
    disc(d, 8, 4, 1.4, accent)
    tri(d, [(5, 15), (8, 9), (11, 15)], base)


def draw_humanoid_shroud(img, base, light, dark, accent):
    d = ImageDraw.Draw(img)
    tri(d, [(3, 15), (8, 2), (13, 15)], base)
    tri(d, [(5, 15), (8, 4), (11, 15)], light)
    ellipse(d, 8, 6, 2.0, 2.4, dark)
    rect(d, 7, 5, 7, 6, accent)
    rect(d, 9, 5, 9, 6, accent)


def draw_food_sausage(img, base, light, dark, accent):
    d = ImageDraw.Draw(img)
    for i in range(10):
        t = i / 9
        x = 3 + t * 10
        y = 12 - t * 6 + math.sin(t * math.pi) * 1.4
        disc(d, x, y, 2.0, base)
    for i in range(4, 8):
        t = (i - 4) / 3
        x = 3 + t * 10
        y = 12 - t * 6 + math.sin(t * math.pi) * 1.4
        disc(d, x - 0.5, y - 0.7, 0.6, light)
    disc(d, 13, 5, 1.1, accent)


def draw_food_bun(img, base, light, dark, accent):
    d = ImageDraw.Draw(img)
    ellipse(d, 8, 9, 5.4, 4.4, base)
    ellipse(d, 8, 8, 4.0, 2.8, light)
    for x in (5, 8, 11):
        disc(d, x, 7, 0.8, accent)
    rect(d, 3, 12, 12, 13, dark)


def draw_food_fruit(img, base, light, dark, accent):
    d = ImageDraw.Draw(img)
    disc(d, 8, 10, 4.6, base)
    disc(d, 6, 8, 1.4, light)
    rect(d, 7, 3, 8, 5, dark)
    tri(d, [(8, 4), (12, 2), (10, 5)], light)


def draw_star_burst(img, base, light, dark, accent):
    d = ImageDraw.Draw(img)
    tri(d, [(8, 1), (10, 7), (15, 8), (10, 9), (8, 15), (6, 9), (1, 8), (6, 7)], base)
    tri(d, [(8, 3), (9, 8), (8, 13), (7, 8)], light)
    disc(d, 8, 8, 1.2, accent)


def draw_star_crescent(img, base, light, dark, accent):
    d = ImageDraw.Draw(img)
    disc(d, 8, 8, 6.0, base)
    disc(d, 11, 7, 5.0, (0, 0, 0, 0))
    for a in range(200, 340, 20):
        disc(d, 8 + math.cos(math.radians(a)) * 6, 8 + math.sin(math.radians(a)) * 6, 0.9, light)
    disc(d, 5, 5, 1.0, accent)


def draw_star_spiral(img, base, light, dark, accent):
    d = ImageDraw.Draw(img)
    for i in range(26):
        t = i / 25
        a = t * math.pi * 3
        r = 1.0 + t * 6.0
        disc(d, 8 + math.cos(a) * r, 8 + math.sin(a) * r, 0.9, light if i % 2 else base)
    disc(d, 8, 8, 1.4, accent)


def draw_evil_skull(img, base, light, dark, accent):
    d = ImageDraw.Draw(img)
    ellipse(d, 8, 8, 5.0, 4.8, base)
    ellipse(d, 8, 9, 3.4, 3.0, light)
    rect(d, 5, 7, 6, 9, dark)
    rect(d, 10, 7, 11, 9, dark)
    rect(d, 7, 12, 8, 12, dark)
    for x in (6, 7, 8, 9, 10):
        rect(d, x, 13, x, 14, dark)


def draw_evil_horned(img, base, light, dark, accent):
    d = ImageDraw.Draw(img)
    tri(d, [(2, 8), (5, 0), (7, 7)], dark)
    tri(d, [(14, 8), (11, 0), (9, 7)], dark)
    ellipse(d, 8, 9, 4.6, 4.2, base)
    rect(d, 5, 8, 6, 9, accent)
    rect(d, 10, 8, 11, 9, accent)
    rect(d, 7, 12, 8, 12, dark)


def draw_evil_eye(img, base, light, dark, accent):
    d = ImageDraw.Draw(img)
    disc(d, 8, 8, 5.4, dark)
    disc(d, 8, 8, 4.0, base)
    disc(d, 8, 8, 2.0, light)
    disc(d, 8, 8, 0.9, accent)
    for a in range(0, 360, 45):
        thick_line(d, 8, 8, 8 + math.cos(math.radians(a)) * 6, 8 + math.sin(math.radians(a)) * 6, 1, dark)


def draw_fusion_twins(img, base, light, dark, accent):
    d = ImageDraw.Draw(img)
    ellipse(d, 5, 8, 3.4, 4.0, base)
    ellipse(d, 11, 8, 3.4, 4.0, light)
    rect(d, 3, 7, 4, 8, accent)
    rect(d, 11, 7, 12, 8, accent)
    rect(d, 6, 13, 9, 14, dark)
    disc(d, 8, 8, 1.2, accent)


def draw_fusion_spiral(img, base, light, dark, accent):
    d = ImageDraw.Draw(img)
    for i in range(22):
        t = i / 21
        a = t * math.pi * 4
        r = 1.0 + t * 5.0
        disc(d, 8 + math.cos(a) * r, 8 + math.sin(a) * r, 1.0, base if i % 2 else light)
    disc(d, 8, 8, 1.4, accent)


def draw_fusion_orb(img, base, light, dark, accent):
    d = ImageDraw.Draw(img)
    disc(d, 6, 8, 3.6, base)
    disc(d, 10, 8, 3.6, light)
    disc(d, 8, 8, 2.0, dark)
    disc(d, 8, 8, 1.0, accent)


FAMILY_VARIANTS = {
    "Tool": [draw_tool_hammer, draw_tool_blade, draw_tool_staff, draw_tool_ring,
             draw_tool_hammer, draw_tool_staff, draw_tool_blade, draw_tool_ring],
    "Beast": [draw_beast_head, draw_beast_horned, draw_beast_winged, draw_beast_coiled,
              draw_beast_quadruped, draw_beast_clawed, draw_beast_eye, draw_beast_fanged,
              draw_beast_serpent, draw_beast_spider, draw_beast_fish, draw_beast_antler],
    "Plant": [draw_plant_blades, draw_plant_bloom, draw_plant_vine, draw_plant_thorn],
    "Dragon": [draw_dragon_head, draw_dragon_wyrm, draw_dragon_winged, draw_dragon_horned],
    "Element": [draw_element_orb, draw_element_flame, draw_element_shard],
    "Body": [draw_body_eye, draw_body_hand, draw_body_bone],
    "Humanoid": [draw_humanoid_figure, draw_humanoid_angel, draw_humanoid_shroud],
    "Food": [draw_food_sausage, draw_food_bun, draw_food_fruit],
    "Star": [draw_star_burst, draw_star_crescent, draw_star_spiral],
    "Evil": [draw_evil_skull, draw_evil_horned, draw_evil_eye],
    "Fusion": [draw_fusion_twins, draw_fusion_spiral, draw_fusion_orb],
}

# ------------------------------------------------------------------ template per keluarga

def shift(color, factor):
    r, g, b, a = color
    if factor > 1:
        return (min(255, int(r * factor)), min(255, int(g * factor)), min(255, int(b * factor)), a)
    return (int(r * factor), int(g * factor), int(b * factor), a)


def hash_color(text, salt=0):
    h = 0
    for ch in text:
        h = (h * 131 + ord(ch) + salt) & 0xFFFFFFFF
    return (128 + h % 128, 64 + (h >> 8) % 128, 64 + (h >> 16) % 128, 255)


def add_detail(img, soul_id, accent):
    """Tambah detail kecil sesuai sidik soul supaya potret tidak kembar."""
    d = ImageDraw.Draw(img)
    h = zlib.crc32(soul_id.encode("utf-8"))
    d2 = ImageDraw.Draw(img)
    if h % 3 == 0:
        thick_line(d2, 3, 3, 5, 1, 1, accent)
        thick_line(d2, 12, 3, 10, 1, 1, accent)
    elif h % 3 == 1:
        disc(d2, 1, 8, 0.9, accent)
        disc(d2, 14, 8, 0.9, accent)
    else:
        for x in (2, 6, 10, 14):
            disc(d2, x, 15, 0.7, accent)


def hue_to_rgb(hue, saturation, value):
    h = (hue % 360) / 60.0
    c = value * saturation
    x = c * (1 - abs((h % 2) - 1))
    m = value - c
    if h < 1:
        r, g, b = c, x, 0
    elif h < 2:
        r, g, b = x, c, 0
    elif h < 3:
        r, g, b = 0, c, x
    elif h < 4:
        r, g, b = 0, x, c
    elif h < 5:
        r, g, b = x,0, c
    else:
        r, g, b = c, 0, x
    return (int((r + m) * 255), int((g + m) * 255), int((b + m) * 255), 255)


def palette_for(soul):
    """Warna dasar diacak per soul, warna elemen dipakai sebagai aksen mata.

    Dengan begitu dua soul dalam keluarga yang sama tidak pernah punya warna sama,
    sementara elemen soul tetap terbaca dari mata dan detail.
    """
    seed = zlib.crc32(soul["id"].encode("utf-8"))

    hue = seed % 360
    saturation = 0.45 + ((seed >> 9) % 40) / 100.0
    value = 0.62 + ((seed >> 17) % 30) / 100.0

    base = hue_to_rgb(hue, saturation, value)
    light = hue_to_rgb(hue, min(1.0, saturation * 0.75), min(1.0, value * 1.35))
    dark = hue_to_rgb(hue, min(1.0, saturation * 1.25), value * 0.45)

    accent = c(soul["color"])
    if soul["family"] in ("Fusion", "Element"):
        accent = hue_to_rgb((hue + 150) % 360, 0.8, 1.0)
    return base, light, dark, accent


def draw_tool(img, base, light, dark, accent):
    d = ImageDraw.Draw(img)
    thick_line(d, 6, 15, 6, 5, 2, base)
    thick_line(d, 5, 14, 5, 6, 1, light)
    rect(d, 2, 2, 10, 5, dark)
    rect(d, 3, 2, 9, 3, base)
    rect(d, 4, 2, 4, 3, accent)


def draw_beast(img, base, light, dark, accent):
    d = ImageDraw.Draw(img)
    tri(d, [(2, 8), (4, 2), (7, 7)], base)
    tri(d, [(14, 8), (12, 2), (9, 7)], base)
    ellipse(d, 8, 9, 5.2, 4.6, base)
    ellipse(d, 8, 10, 3.6, 3.0, light)
    rect(d, 4, 8, 5, 9, accent)
    rect(d, 11, 8, 12, 9, accent)
    rect(d, 7, 11, 8, 12, dark)


def draw_plant(img, base, light, dark, accent):
    d = ImageDraw.Draw(img)
    for x0, y0, x1, y1 in ((3, 14, 2, 4), (6, 14, 6, 2), (9, 14, 10, 3), (12, 14, 13, 5)):
        thick_line(d, x0, y0, x1, y1, 2, base)
        thick_line(d, x0 - 1, y0 - 3, x1 - 1, y1, 1, light)
    rect(d, 2, 13, 13, 15, dark)
    disc(d, 8, 8, 1.6, accent)


def draw_dragon(img, base, light, dark, accent):
    d = ImageDraw.Draw(img)
    tri(d, [(3, 6), (4, 0), (7, 5)], base)
    tri(d, [(13, 6), (12, 0), (9, 5)], base)
    ellipse(d, 8, 9, 5.0, 4.4, base)
    ellipse(d, 8, 10, 3.6, 3.0, light)
    rect(d, 4, 8, 5, 9, accent)
    rect(d, 11, 8, 12, 9, accent)
    rect(d, 7, 12, 8, 12, dark)
    for side in (-1, 1):
        thick_line(d, 8 + side * 4, 11, 8 + side * 6, 13, 1, light)


def draw_element(img, base, light, dark, accent):
    d = ImageDraw.Draw(img)
    disc(d, 8, 8, 5.2, dark)
    disc(d, 8, 8, 4.2, base)
    disc(d, 7, 7, 1.6, light)
    disc(d, 10, 10, 1.0, accent)
    for x, y in ((3, 8), (13, 8), (8, 3), (8, 13)):
        disc(d, x, y, 0.9, accent)


def draw_body(img, base, light, dark, accent):
    d = ImageDraw.Draw(img)
    ellipse(d, 8, 8, 5.4, 5.4, base)
    ellipse(d, 8, 8, 3.6, 3.6, light)
    disc(d, 8, 8, 1.8, accent)
    rect(d, 7, 7, 8, 8, dark)
    for x, y in ((3, 3), (13, 3), (3, 13), (13, 13)):
        disc(d, x, y, 0.8, dark)


def draw_humanoid(img, base, light, dark, accent):
    d = ImageDraw.Draw(img)
    disc(d, 8, 5, 2.6, base)
    tri(d, [(4, 15), (8, 8), (12, 15)], base)
    tri(d, [(5, 15), (8, 9), (7, 15)], light)
    rect(d, 7, 3, 8, 4, accent)
    rect(d, 4, 14, 11, 15, dark)


def draw_food(img, base, light, dark, accent):
    d = ImageDraw.Draw(img)
    for i in range(10):
        t = i / 9
        x = 3 + t * 10
        y = 12 - t * 6 + math.sin(t * math.pi) * 1.4
        disc(d, x, y, 2.0, base)
    for i in range(4, 8):
        t = (i - 4) / 3
        x = 3 + t * 10
        y = 12 - t * 6 + math.sin(t * math.pi) * 1.4
        disc(d, x - 0.5, y - 0.7, 0.6, light)
    disc(d, 13, 5, 1.1, accent)


def draw_star(img, base, light, dark, accent):
    d = ImageDraw.Draw(img)
    tri(d, [(8, 1), (10, 7), (15, 8), (10, 9), (8, 15), (6, 9), (1, 8), (6, 7)], base)
    tri(d, [(8, 3), (9, 8), (8, 13), (7, 8)], light)
    disc(d, 8, 8, 1.2, accent)


def draw_evil(img, base, light, dark, accent):
    d = ImageDraw.Draw(img)
    ellipse(d, 8, 9, 5.2, 4.8, base)
    ellipse(d, 8, 10, 3.6, 3.2, dark)
    rect(d, 4, 7, 5, 9, accent)
    rect(d, 11, 7, 12, 9, accent)
    rect(d, 7, 12, 8, 12, accent)
    for x in (6, 7, 8, 9, 10):
        rect(d, x, 13, x, 14, light)
    tri(d, [(3, 6), (5, 1), (7, 6)], dark)
    tri(d, [(13, 6), (11, 1), (9, 6)], dark)


def draw_fusion(img, base, light, dark, accent):
    d = ImageDraw.Draw(img)
    ellipse(d, 5, 8, 3.4, 4.0, base)
    ellipse(d, 11, 8, 3.4, 4.0, light)
    rect(d, 3, 7, 4, 8, accent)
    rect(d, 11, 7, 12, 8, accent)
    rect(d, 6, 13, 9, 14, dark)
    disc(d, 8, 8, 1.2, accent)


FAMILY_TEMPLATE = {
    "Tool": draw_tool, "Beast": draw_beast, "Plant": draw_plant, "Dragon": draw_dragon,
    "Element": draw_element, "Body": draw_body, "Humanoid": draw_humanoid, "Food": draw_food,
    "Star": draw_star, "Evil": draw_evil, "Fusion": draw_fusion,
}

# ------------------------------------------------------------------ potret soul

def draw_hoe(img, base, light, dark, accent):
    d = ImageDraw.Draw(img)
    thick_line(d, 4, 13, 11, 4, 2, base)
    thick_line(d, 4, 12, 10, 4, 1, light)
    rect(d, 8, 1, 13, 3, dark)
    rect(d, 9, 1, 12, 2, accent)
    thick_line(d, 4, 14, 3, 15, 2, dark)


def draw_sausage(img, base, light, dark, accent):
    d = ImageDraw.Draw(img)
    for i in range(11):
        t = i / 10
        x = 2 + t * 11
        y = 11 - t * 6 + math.sin(t * math.pi) * 1.6
        disc(d, x, y, 2.1, base)
    for i in range(4, 9):
        t = (i - 4) / 4
        x = 2 + t * 11
        y = 11 - t * 6 + math.sin(t * math.pi) * 1.6
        disc(d, x - 0.6, y - 0.8, 0.7, light)
    disc(d, 13, 4, 1.2, accent)


def draw_hell_civet(img, base, light, dark, accent):
    d = ImageDraw.Draw(img)
    tri(d, [(2, 8), (4, 1), (7, 6)], base)
    tri(d, [(14, 8), (12, 1), (9, 6)], base)
    tri(d, [(3, 6), (4, 3), (5, 6)], dark)
    tri(d, [(13, 6), (12, 3), (11, 6)], dark)
    ellipse(d, 8, 9, 5.5, 4.8, base)
    ellipse(d, 8, 10, 4.0, 3.2, light)
    rect(d, 4, 8, 5, 9, accent)
    rect(d, 11, 8, 12, 9, accent)
    rect(d, 7, 11, 8, 12, dark)
    rect(d, 6, 12, 9, 12, dark)


def draw_grass(img, base, light, dark, accent, crown=False):
    d = ImageDraw.Draw(img)
    blades = [(3, 14, 2, 4), (6, 14, 6, 2), (9, 14, 10, 3), (12, 14, 13, 5)]
    for i, (x0, y0, x1, y1) in enumerate(blades):
        thick_line(d, x0, y0, x1, y1, 2, base)
        thick_line(d, x0 - 1, y0 - 3, x1 - 1, y1, 1, light)
    rect(d, 2, 13, 13, 15, dark)
    disc(d, 8, 8, 1.6, accent)
    disc(d, 8, 8, 0.7, light)
    if crown:
        tri(d, [(3, 4), (8, 0), (13, 4)], accent)
        tri(d, [(4, 4), (8, 1), (12, 4)], light)
        disc(d, 8, 4, 1.2, (255, 255, 255, 255))


def draw_tiger(img, base, light, dark, accent):
    d = ImageDraw.Draw(img)
    tri(d, [(2, 8), (3, 2), (7, 6)], base)
    tri(d, [(14, 8), (13, 2), (9, 6)], base)
    ellipse(d, 8, 9, 5.4, 5.0, base)
    ellipse(d, 8, 10, 4.0, 3.6, light)
    rect(d, 4, 8, 5, 9, accent)
    rect(d, 11, 8, 12, 9, accent)
    rect(d, 7, 11, 8, 12, dark)
    for i, x in enumerate((5, 8, 11)):
        rect(d, x, 3 + i % 2, x, 5, dark)
    rect(d, 4, 12, 6, 12, dark)
    rect(d, 10, 12, 12, 12, dark)


def draw_rabbit(img, base, light, dark, accent):
    d = ImageDraw.Draw(img)
    ellipse(d, 5, 4, 1.6, 4.0, base)
    ellipse(d, 10, 4, 1.6, 4.0, base)
    ellipse(d, 5, 4, 0.7, 2.8, light)
    ellipse(d, 10, 4, 0.7, 2.8, light)
    ellipse(d, 8, 10, 4.6, 4.0, base)
    ellipse(d, 8, 11, 3.2, 2.8, light)
    rect(d, 5, 9, 6, 10, accent)
    rect(d, 10, 9, 11, 10, accent)
    rect(d, 7, 12, 8, 12, dark)
    rect(d, 6, 13, 9, 13, dark)


def draw_serpent(img, base, light, dark, accent):
    d = ImageDraw.Draw(img)
    points = [(12, 3), (9, 5), (5, 7), (4, 10), (7, 12), (11, 12), (12, 9)]
    for i in range(len(points) - 1):
        thick_line(d, points[i][0], points[i][1], points[i + 1][0], points[i + 1][1], 3, base)
    for i in range(len(points) - 1):
        thick_line(d, points[i][0], points[i][1] - 1, points[i + 1][0], points[i + 1][1] - 1, 1, light)
    disc(d, 12, 3, 2.4, base)
    disc(d, 11, 2, 0.9, accent)
    rect(d, 11, 4, 13, 4, dark)


def draw_pagoda(img, base, light, dark, accent, tiers=3, gem=False):
    """Pagoda bertingkat: atap melangkah (bukan segitiga) supaya terbaca di 16 piksel."""
    d = ImageDraw.Draw(img)
    center = 7
    step = 4 if tiers == 3 else 3
    y = 1
    if gem:
        disc(d, center, 0, 1.2, accent)
        disc(d, center, 0, 0.6, (255, 255, 255, 255))
        y = 2
    for t in range(tiers):
        half = 2 + t * 2 if tiers == 3 else 1 + t * 2
        rect(d, center - half, y, center + half, y, dark)
        rect(d, center - half + 1, y + 1, center + half - 1, y + 1, base)
        rect(d, center - half + 1, y + 1, center, y + 1, light)
        body_top = y + 2
        body_height = step - 2
        rect(d, center - 1, body_top, center + 1, body_top + body_height - 1, base)
        rect(d, center - 1, body_top, center, body_top + body_height - 1, light)
        window_top = body_top + 1
        window_bottom = max(window_top, body_top + body_height - 2)
        rect(d, center, window_top, center, window_bottom, accent)
        y += step
    rect(d, center - 3, y, center + 3, y, dark)
    rect(d, center - 2, y + 1, center + 2, y + 1, base)


def draw_phoenix(img, base, light, dark, accent, evil=True):
    d = ImageDraw.Draw(img)
    for side in (-1, 1):
        tip = 8 + side * 7
        tri(d, [(8, 5), (tip, 1), (8 + side * 4, 10)], base)
        tri(d, [(8, 6), (tip, 3), (8 + side * 3, 9)], light)
    ellipse(d, 8, 7, 2.6, 3.2, base)
    ellipse(d, 8, 8, 1.6, 1.8, light)
    rect(d, 6, 6, 6, 7, accent)
    rect(d, 9, 6, 9, 7, accent)
    tri(d, [(7, 1), (8, 4), (9, 1)], accent)
    tri(d, [(6, 12), (8, 9), (10, 12)], dark)


def draw_mirror(img, base, light, dark, accent):
    d = ImageDraw.Draw(img)
    rect(d, 3, 1, 12, 14, base)
    rect(d, 4, 2, 11, 13, light)
    rect(d, 5, 3, 10, 12, dark)
    ellipse(d, 8, 8, 2.6, 3.4, base)
    ellipse(d, 8, 9, 1.6, 2.0, light)
    rect(d, 6, 7, 6, 8, accent)
    rect(d, 9, 7, 9, 8, accent)
    rect(d, 3, 1, 12, 1, accent)
    rect(d, 3, 14, 12, 14, dark)


def draw_hammer(img, base, light, dark, accent):
    d = ImageDraw.Draw(img)
    rect(d, 2, 2, 13, 6, base)
    rect(d, 3, 2, 12, 3, light)
    rect(d, 2, 5, 13, 6, dark)
    rect(d, 4, 3, 4, 4, accent)
    rect(d, 11, 3, 11, 4, accent)
    thick_line(d, 7, 6, 7, 14, 2, dark)
    thick_line(d, 6, 6, 6, 13, 1, base)


def draw_dragon(img, base, light, dark, accent):
    d = ImageDraw.Draw(img)
    tri(d, [(3, 6), (4, 0), (7, 5)], base)
    tri(d, [(13, 6), (12, 0), (9, 5)], base)
    ellipse(d, 8, 9, 5.0, 4.4, base)
    ellipse(d, 8, 10, 3.6, 3.0, light)
    rect(d, 4, 8, 5, 9, accent)
    rect(d, 11, 8, 12, 9, accent)
    rect(d, 7, 12, 8, 12, dark)
    for side in (-1, 1):
        thick_line(d, 8 + side * 4, 11, 8 + side * 6, 13, 1, light)
    rect(d, 6, 5, 9, 6, dark)


def draw_hammer_soul(img, base, light, dark, accent):
    draw_hammer(img, base, light, dark, accent)


# ------------------------------------------------------------------ item

def draw_ring_item(img, color, light, dark, sparkle=False):
    d = ImageDraw.Draw(img)
    disc(d, 8, 8, 6, dark)
    disc(d, 8, 8, 5, color)
    disc(d, 8, 8, 3, (0, 0, 0, 0))
    for a in range(0, 360, 30):
        x = 8 + math.cos(math.radians(a)) * 4
        y = 8 + math.sin(math.radians(a)) * 4
        disc(d, x, y, 0.9, light)
    if sparkle:
        disc(d, 8, 2, 1.2, (255, 255, 255, 255))
        disc(d, 3, 11, 0.9, (255, 255, 255, 255))


def draw_soul_card_item(img):
    d = ImageDraw.Draw(img)
    rect(d, 2, 2, 13, 13, (0xE8, 0xC4, 0x6A, 255))
    rect(d, 3, 3, 12, 12, (0x1E, 0x22, 0x28, 255))
    disc(d, 8, 7, 3.0, (0x4F, 0xC0, 0x8D, 255))
    disc(d, 8, 7, 1.6, (0xE8, 0xF0, 0xE8, 255))
    rect(d, 5, 11, 10, 11, (0xE8, 0xC4, 0x6A, 255))


def draw_essence_item(img):
    d = ImageDraw.Draw(img)
    disc(d, 8, 9, 4.2, (0x1F, 0x6B, 0x4B, 255))
    disc(d, 8, 9, 3.2, (0x4F, 0xC0, 0x8D, 255))
    disc(d, 7, 8, 1.2, (0xC8, 0xFF, 0xE6, 255))
    tri(d, [(8, 2), (10, 8), (6, 8)], (0x7B, 0xE0, 0xB0, 255))


def draw_evolution_core(img):
    d = ImageDraw.Draw(img)
    tri(d, [(8, 1), (13, 8), (8, 14), (3, 8)], (0x6A, 0x3F, 0xD1, 255))
    tri(d, [(8, 3), (11, 8), (8, 12), (5, 8)], (0x9B, 0xE7, 0xFF, 255))
    tri(d, [(8, 5), (9, 8), (8, 10), (7, 8)], (0xFF, 0xFF, 0xFF, 255))
    disc(d, 8, 2, 1.0, (0xD6, 0xF6, 0xFF, 255))


def draw_sect_token(img):
    d = ImageDraw.Draw(img)
    disc(d, 8, 8, 6, (0x8A, 0x61, 0x16, 255))
    disc(d, 8, 8, 5, (0xF2, 0xC1, 0x4E, 255))
    disc(d, 8, 8, 3.4, (0x1E, 0x22, 0x28, 255))
    tri(d, [(8, 4), (11, 9), (5, 9)], (0xF2, 0xC1, 0x4E, 255))
    rect(d, 7, 9, 8, 12, (0xF2, 0xC1, 0x4E, 255))



def draw_sect_medallion(img, main, light, dark, mark):
    d = ImageDraw.Draw(img)
    disc(d, 8, 8, 7, dark)
    disc(d, 8, 8, 6, main)
    disc(d, 8, 8, 6, light)
    disc(d, 8, 8, 4, dark)
    tri(d, [(8, 4), (11, 9), (5, 9)], main)
    rect(d, 7, 9, 8, 12, main)
    for x, y in ((4, 4), (11, 4), (4, 11), (11, 11)):
        disc(d, x, y, 0.8, mark)


def draw_manual(img):
    d = ImageDraw.Draw(img)
    rect(d, 2, 2, 13, 13, (0x6B, 0x2B, 0x2B, 255))
    rect(d, 3, 3, 12, 12, (0xE8, 0xC4, 0x6A, 255))
    rect(d, 4, 4, 11, 11, (0xF2, 0xE4, 0xC4, 255))
    for y in range(5, 12, 2):
        rect(d, 5, y, 10, y, (0x8A, 0x61, 0x16, 255))
    rect(d, 2, 2, 3, 13, (0x4A, 0x1E, 0x1E, 255))


def draw_soul_core(img):
    d = ImageDraw.Draw(img)
    disc(d, 8, 8, 6, (0x1F, 0x2A, 0x3A, 255))
    disc(d, 8, 8, 4.6, (0x4F, 0xC0, 0x8D, 255))
    disc(d, 6, 6, 1.6, (0xD8, 0xFF, 0xEC, 255))
    for a in range(0, 360, 45):
        disc(d, 8 + math.cos(math.radians(a)) * 6, 8 + math.sin(math.radians(a)) * 6, 0.9, (0xCF, 0xF2, 0xFF, 255))


def draw_guide_item(img):
    d = ImageDraw.Draw(img)
    rect(d, 2, 2, 13, 13, (0x2E, 0x4A, 0x38, 255))
    rect(d, 3, 3, 12, 12, (0x4F, 0x7A, 0x5E, 255))
    rect(d, 5, 4, 11, 12, (0xF0, 0xE8, 0xD0, 255))
    for y in range(6, 12, 2):
        rect(d, 6, y, 10, y, (0x8A, 0x61, 0x16, 255))
    rect(d, 2, 2, 3, 13, (0x1E, 0x30, 0x24, 255))
    disc(d, 8, 5, 1.4, (0xE8, 0xC4, 0x6A, 255))


def draw_qi_pill(img):
    d = ImageDraw.Draw(img)
    disc(d, 6, 10, 3.2, (0x1F, 0x6B, 0x4B, 255))
    disc(d, 6, 10, 2.4, (0x4F, 0xC0, 0x8D, 255))
    disc(d, 10, 6, 3.2, (0x1F, 0x6B, 0x4B, 255))
    disc(d, 10, 6, 2.4, (0x7B, 0xE0, 0xB0, 255))
    disc(d, 5, 9, 0.8, (0xD8, 0xFF, 0xEC, 240))
    disc(d, 9, 5, 0.8, (0xD8, 0xFF, 0xEC, 240))


def draw_soul_crystal(img):
    d = ImageDraw.Draw(img)
    tri(d, [(8, 1), (12, 7), (8, 14), (4, 7)], (0x3A, 0x1E, 0x66, 255))
    tri(d, [(8, 3), (10, 7), (8, 12), (6, 7)], (0x8B, 0x5C, 0xF6, 255))
    tri(d, [(8, 5), (9, 7), (8, 10), (7, 7)], (0xE6, 0xD2, 0xFF, 255))
    for x, y in ((2, 3), (13, 11), (4, 12)):
        disc(d, x, y, 0.8, (0xC8, 0xB0, 0xFF, 220))


def draw_ring_forge(img):
    d = ImageDraw.Draw(img)
    rect(d, 3, 10, 12, 13, (0x5A, 0x50, 0x46, 255))
    rect(d, 4, 9, 11, 10, (0x8A, 0x7C, 0x6A, 255))
    thick_line(d, 8, 2, 8, 9, 2, (0x9A, 0x6A, 0x2E, 255))
    rect(d, 5, 1, 11, 3, (0xC9, 0xA2, 0x27, 255))
    rect(d, 6, 1, 10, 1, (0xFF, 0xF3, 0xC4, 255))
    disc(d, 8, 6, 1.4, (0xE8, 0xC4, 0x6A, 255))


def draw_hunt_contract(img):
    d = ImageDraw.Draw(img)
    rect(d, 3, 2, 12, 13, (0xE8, 0xDC, 0xBE, 255))
    rect(d, 3, 2, 4, 13, (0xC0, 0xA8, 0x7C, 255))
    rect(d, 11, 2, 12, 13, (0xC0, 0xA8, 0x7C, 255))
    for y in range(4, 12, 2):
        rect(d, 6, y, 10, y, (0x6A, 0x4A, 0x2E, 255))
    disc(d, 11, 11, 1.8, (0xD8, 0x44, 0x2F, 255))
    disc(d, 11, 11, 0.9, (0xFF, 0x9A, 0x80, 255))


def draw_overlay_glow(img):
    d = ImageDraw.Draw(img)
    for r in range(7, 2, -1):
        d.ellipse([8 - r, 8 - r, 8 + r - 1, 8 + r - 1], outline=(255, 255, 255, int(120 * (8 - r) / 6)))


def draw_overlay_spark(img):
    d = ImageDraw.Draw(img)
    for i in range(16):
        d.point((i, 8), (255, 255, 255, 200))
        d.point((8, i), (255, 255, 255, 200))
    for x, y in ((2, 2), (13, 2), (2, 13), (13, 13)):
        d.point((x, y), (255, 255, 255, 180))


def draw_card_frame(img):
    d = ImageDraw.Draw(img)
    for x, y in ((1, 1), (14, 1), (1, 14), (14, 14)):
        rect(d, x, y, x, y, (0xE8, 0xC4, 0x6A, 255))
    rect(d, 1, 7, 14, 8, (0xE8, 0xC4, 0x6A, 90))
    rect(d, 7, 1, 8, 14, (0xE8, 0xC4, 0x6A, 90))


def draw_incense_item(img):
    d = ImageDraw.Draw(img)
    thick_line(d, 5, 15, 8, 6, 2, (0x8B, 0x6B, 0x3B, 255))
    disc(d, 8, 5, 1.4, (0xFF, 0x7A, 0x2F, 255))
    disc(d, 8, 5, 0.7, (0xFF, 0xF3, 0xC4, 255))
    for y in range(1, 5):
        disc(d, 9 + (y % 2), y, 0.8, (0xD0, 0xD8, 0xD8, 190))
    rect(d, 3, 14, 12, 15, (0x5E, 0x45, 0x26, 255))


# ------------------------------------------------------------------ tekstur efek

def draw_fx_ring():
    img = new_image(32)
    d = ImageDraw.Draw(img)
    for r in range(16, 10, -1):
        alpha = int(255 * (16 - r + 1) / 7)
        d.ellipse([16 - r, 16 - r, 16 + r - 1, 16 + r - 1], outline=(255, 255, 255, alpha))
    return img


def draw_fx_beam():
    img = new_image(16)
    d = ImageDraw.Draw(img)
    for y in range(16):
        distance = abs(y - 7.5) / 8.0
        alpha = int(255 * (1.0 - distance))
        for x in range(16):
            edge = min(x, 15 - x) / 7.5
            d.point((x, y), (255, 255, 255, int(alpha * min(1.0, 0.35 + edge))))
    return img


def draw_fx_glow():
    img = new_image(16)
    d = ImageDraw.Draw(img)
    for y in range(16):
        for x in range(16):
            distance = math.hypot(x - 7.5, y - 7.5) / 8.0
            alpha = int(255 * max(0.0, 1.0 - distance) ** 1.6)
            if alpha > 0:
                d.point((x, y), (255, 255, 255, alpha))
    return img


def draw_fx_spark():
    img = new_image(16)
    d = ImageDraw.Draw(img)
    for i in range(16):
        d.point((i, 7), (255, 255, 255, 235))
        d.point((7, i), (255, 255, 255, 235))
        d.point((i, 8), (255, 255, 255, 150))
        d.point((8, i), (255, 255, 255, 150))
    for x, y in ((2, 2), (13, 2), (2, 13), (13, 13)):
        d.point((x, y), (255, 255, 255, 190))
    return img


# ------------------------------------------------------------------ template tangan untuk soul ikonik

SOUL_DRAWERS = {
    "hoe": lambda img, p: draw_hoe(img, *p),
    "sausage": lambda img, p: draw_sausage(img, *p),
    "hell_civet": lambda img, p: draw_hell_civet(img, *p),
    "blue_silver_grass": lambda img, p: draw_grass(img, *p),
    "white_tiger": lambda img, p: draw_tiger(img, *p),
    "soft_bone_rabbit": lambda img, p: draw_rabbit(img, *p),
    "jade_serpent": lambda img, p: draw_serpent(img, *p),
    "seven_treasure_pagoda": lambda img, p: draw_pagoda(img, *p),
    "evil_fire_phoenix": lambda img, p: draw_phoenix(img, *p),
    "mirror_twin_beast": lambda img, p: draw_mirror(img, *p),
    "clear_sky_hammer": lambda img, p: draw_hammer_soul(img, *p),
    "golden_dragon": lambda img, p: draw_dragon(img, *p),
    "blue_silver_emperor": lambda img, p: draw_grass(img, *p, crown=True),
    "nine_treasure_pagoda": lambda img, p: draw_pagoda(img, *p, tiers=4, gem=True),
    "fire_phoenix": lambda img, p: draw_phoenix(img, *p, evil=False),
}

HAND_DRAWN = {
    "blue_silver_grass": "blue_silver_grass",
    "clear_sky_hammer": "clear_sky_hammer",
    "seven_treasures_glazed_pagoda": "seven_treasure_pagoda",
    "evil_eyed_white_tiger": "white_tiger",
    "hell_civet": "hell_civet",
    "soft_boned_rabbit": "soft_bone_rabbit",
    "jade_phosphor_serpent": "jade_serpent",
    "evil_fire_phoenix_fire_phoenix_after_purificatio": "evil_fire_phoenix",
    "sausage": "sausage",
    "golden_dragon": "golden_dragon",
    "blue_silver_emperor": "blue_silver_emperor",
    "nine_treasures_glazed_pagoda": "nine_treasure_pagoda",
    "fire_phoenix": "fire_phoenix",
    "seven_treasure_glazed_tile_pagoda": "seven_treasure_pagoda",
}


# ------------------------------------------------------------------ model item

MODELS = {
    "spirit_ring_100": {"layers": ["spirit_ring_100", "overlay_glow"]},
    "spirit_ring_1000": {"layers": ["spirit_ring_1000", "overlay_glow"]},
    "spirit_ring_10000": {"layers": ["spirit_ring_10000", "overlay_spark"]},
    "spirit_ring_100000": {"layers": ["spirit_ring_100000", "overlay_spark"]},
    "soul_card": {"layers": ["soul_card", "card_frame"]},
    "soul_core": {"layers": ["soul_core", "overlay_glow"]},
    "sect_medallion_tang": {"layers": ["sect_medallion_tang", "overlay_spark"]},
    "sect_medallion_shrek": {"layers": ["sect_medallion_shrek", "overlay_spark"]},
    "sect_medallion_starluo": {"layers": ["sect_medallion_starluo", "overlay_spark"]},
    "sect_medallion_qibao": {"layers": ["sect_medallion_qibao", "overlay_spark"]},
    "soul_essence": {"layers": ["soul_essence"]},
    "evolution_core": {"layers": ["evolution_core", "overlay_glow"]},
    "sect_token": {"layers": ["sect_token", "overlay_glow"]},
    "meditation_incense": {"layers": ["meditation_incense"]},
    "martial_soul_manual": {"layers": ["martial_soul_manual"], "gui": True},
    "soul_guide": {"layers": ["soul_guide"]},
    "qi_pill": {"layers": ["qi_pill"]},
    "soul_crystal": {"layers": ["soul_crystal", "overlay_glow"]},
    "ring_forge": {"layers": ["ring_forge"]},
    "hunt_contract": {"layers": ["hunt_contract"]},
}


def write_models():
    """Model JSON ditulis langsung supaya parent dan layer selalu sinkron dengan texture."""
    import json
    base = os.path.join(ROOT, "src", "main", "resources", "assets", "soulland", "models", "item")
    os.makedirs(base, exist_ok=True)
    for item_id, spec in MODELS.items():
        model = {"parent": "item/generated",
                 "textures": {"layer%d" % i: "soulland:item/" + layer for i, layer in enumerate(spec["layers"])}}
        if spec.get("gui"):
            model["overrides"] = [{"predicate": {"custom": {"gui": 1}},
                                   "model": "item/generated",
                                   "textures": {"layer0": "soulland:item/" + spec["layers"][0]}}]
        with open(os.path.join(base, item_id + ".json"), "w", encoding="utf-8") as handle:
            json.dump(model, handle, indent=2)
            handle.write("\n")


# ------------------------------------------------------------------ tekstur partikel

def draw_particle_textures():
    """Setiap partikel kustom punya texture sendiri di textures/particle/."""
    import math

    def new_particle():
        return new_image(16)

    # soul_flame: nyala naik, warm
    img = new_particle()
    d = ImageDraw.Draw(img)
    for i in range(16):
        t = i / 15
        half = 3.2 * (1.0 - t * 0.7)
        for y in range(16 - i - 6, 16 - i):
            if 0 <= y < 16:
                for x in range(int(7.5 - half), int(8.5 + half)):
                    d.point((x, y), (255, int(200 - t * 120), int(90 - t * 60), 220))
    disc(d, 8, 10, 1.6, (255, 244, 196, 235))
    save(img, "particle", "soul_flame.png")

    # soul_spark: titik terang dengan ekor
    img = new_particle()
    d = ImageDraw.Draw(img)
    disc(d, 8, 8, 1.5, (255, 255, 255, 240))
    disc(d, 8, 8, 0.8, (220, 240, 255, 255))
    for x, y in ((7, 9), (6, 10), (9, 7), (10, 6)):
        d.point((x, y), (180, 220, 255, 170))
    save(img, "particle", "soul_spark.png")

    # soul_shadow: gumpalan gelap
    img = new_particle()
    d = ImageDraw.Draw(img)
    for x, y, r in ((7, 9, 3.2), (9, 8, 2.6), (8, 10, 2.2)):
        disc(d, x, y, r, (40, 28, 58, 190))
    disc(d, 7, 8, 1.4, (86, 62, 116, 210))
    save(img, "particle", "soul_shadow.png")

    # soul_ripple: cincin tipis
    img = new_particle()
    d = ImageDraw.Draw(img)
    for r in (6.5, 4.5, 2.5):
        d.ellipse([8 - r, 8 - r * 0.6, 8 + r, 8 + r * 0.6], outline=(210, 240, 255, 190))
    save(img, "particle", "soul_ripple.png")

    # soul_star: bintang berkedip
    img = new_particle()
    d = ImageDraw.Draw(img)
    for i in range(16):
        d.point((8, i), (255, 250, 220, 230))
        d.point((i, 8), (255, 250, 220, 230))
    for x, y in ((5, 5), (11, 5), (5, 11), (11, 11)):
        d.point((x, y), (255, 240, 190, 190))
    disc(d, 8, 8, 1.2, (255, 255, 255, 255))
    save(img, "particle", "soul_star.png")

    # soul_blood: tetesan merah
    img = new_particle()
    d = ImageDraw.Draw(img)
    disc(d, 8, 9, 2.4, (150, 20, 24, 225))
    disc(d, 7, 8, 1.0, (226, 70, 60, 240))
    disc(d, 7, 7, 0.6, (255, 140, 120, 235))
    save(img, "particle", "soul_blood.png")

    # soul_frost: serpihan es
    img = new_particle()
    d = ImageDraw.Draw(img)
    for x, y, r in ((6, 6, 1.6), (10, 8, 1.3), (8, 11, 1.5)):
        disc(d, x, y, r, (200, 238, 255, 225))
        disc(d, x - 0.4, y - 0.4, 0.5, (255, 255, 255, 240))
    save(img, "particle", "soul_frost.png")

    # soul_thunder: kilat zigzag
    img = new_particle()
    d = ImageDraw.Draw(img)
    for x, y in ((8, 1), (7, 4), (9, 6), (6, 9), (8, 11), (7, 14)):
        d.point((x, y), (230, 244, 255, 245))
    for x, y in ((9, 3), (8, 7), (7, 12)):
        d.point((x, y), (150, 200, 255, 190))
    save(img, "particle", "soul_thunder.png")

    # soul_mist: kabut naik
    img = new_particle()
    d = ImageDraw.Draw(img)
    for x, y, r in ((6, 11, 3.0), (10, 10, 2.6), (8, 8, 2.2), (7, 6, 1.6)):
        disc(d, x, y, r, (190, 225, 215, 150))
    save(img, "particle", "soul_mist.png")

    # soul_embers: bara kecil naik
    img = new_particle()
    d = ImageDraw.Draw(img)
    for x, y, r in ((7, 10, 1.3), (9, 8, 1.0), (8, 5, 0.8)):
        disc(d, x, y, r, (255, 150, 60, 220))
    disc(d, 7, 10, 0.6, (255, 226, 160, 235))
    save(img, "particle", "soul_embers.png")

    # soul_shards: serpihan soul
    img = new_particle()
    d = ImageDraw.Draw(img)
    for x, y in ((6, 7), (9, 6), (7, 11), (10, 10)):
        d.point((x, y), (190, 235, 255, 230))
        d.point((x + 1, y), (140, 200, 240, 180))
    save(img, "particle", "soul_shards.png")


# ------------------------------------------------------------------ main

def main():
    import json

    soul_data = json.load(io.open(
        os.path.join(ROOT, "src", "main", "resources", "data", "soulland", "martial_souls.json"),
        encoding="utf-8"))

    hand_drawn = HAND_DRAWN

    count = 0
    for soul in soul_data["souls"]:
        soul_id = soul["id"]
        img = new_image(16)
        drawn = hand_drawn.get(soul_id)
        if drawn:
            SOUL_DRAWERS[drawn](img, tuple(c(v) for v in SOUL_PALETTE[drawn]))
        else:
            options = FAMILY_VARIANTS.get(soul["family"], [draw_beast])
            index = zlib.crc32(soul["id"].encode("utf-8")) % len(options)
            options[index](img, *palette_for(soul))
            add_detail(img, soul["id"], palette_for(soul)[3])
        img = add_outline(img)
        save(img, "soul", soul_id + ".png")
        count += 1

    items = {
        "spirit_ring_100": lambda img: draw_ring_item(img, c(0xC8CED8), c(0xFFFFFF), c(0x6B7280)),
        "spirit_ring_1000": lambda img: draw_ring_item(img, c(0x3AA0D8), c(0xCFF2FF), c(0x17506F)),
        "spirit_ring_10000": lambda img: draw_ring_item(img, c(0x8B5CF6), c(0xE6D2FF), c(0x3A1D72), sparkle=True),
        "spirit_ring_100000": lambda img: draw_ring_item(img, c(0xE8C46A), c(0xFFF6D2), c(0x8A6116), sparkle=True),
        "soul_card": draw_soul_card_item,
        "soul_essence": draw_essence_item,
        "evolution_core": draw_evolution_core,
        "sect_token": draw_sect_token,
        "meditation_incense": draw_incense_item,
        "martial_soul_manual": draw_manual,
        "soul_core": draw_soul_core,
        "sect_medallion_tang": lambda img: draw_sect_medallion(img, c(0x3A5F9E), c(0x7FA8E8), c(0x1B2C4A), c(0xE8C46A)),
        "sect_medallion_shrek": lambda img: draw_sect_medallion(img, c(0x7A2E2E), c(0xC05A4A), c(0x3A1414), c(0xFFD24A)),
        "sect_medallion_starluo": lambda img: draw_sect_medallion(img, c(0x2E6E52), c(0x63C79A), c(0x14301F), c(0xF2C14E)),
        "sect_medallion_qibao": lambda img: draw_sect_medallion(img, c(0x8A6116), c(0xE8C46A), c(0x3A2A0A), c(0xFFF3C4)),
        "soul_guide": draw_guide_item,
        "qi_pill": draw_qi_pill,
        "soul_crystal": draw_soul_crystal,
        "ring_forge": draw_ring_forge,
        "hunt_contract": draw_hunt_contract,
    }
    overlays = {
        "overlay_glow": draw_overlay_glow,
        "overlay_spark": draw_overlay_spark,
        "card_frame": draw_card_frame,
    }
    for overlay_id, drawer in overlays.items():
        img = new_image(16)
        drawer(img)
        save(img, "item", overlay_id + ".png")
    for item_id, drawer in items.items():
        img = new_image(16)
        drawer(img)
        img = add_outline(img)
        save(img, "item", item_id + ".png")

    write_models()

    draw_particle_textures()

    save(draw_fx_ring(), "fx", "ring.png")
    save(draw_fx_beam(), "fx", "beam.png")
    save(draw_fx_glow(), "fx", "glow.png")
    save(draw_fx_spark(), "fx", "spark.png")

    print("Aset piksel dibuat:", count, "potret soul,", len(items), "item, 4 tekstur efek.")


if __name__ == "__main__":
    main()
