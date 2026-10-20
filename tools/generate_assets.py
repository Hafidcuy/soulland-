#!/usr/bin/env python3
"""Pembuat aset piksel untuk mod Soul Land.

Aset yang dihasilkan:
  * 10 texture manifestasi martial soul (16x16) - siluet, palet, dan motif tiap soul berbeda.
  * 4 texture spirit ring (16x16) - warna sesuai umur ring (putih/kuning/ungu/merah).
  * 1 sprite halo ring (64x64) - cincin putih yang di-tint per umur ring saat dirender.
  * Model JSON item yang menunjuk texture di atas.

Setiap sprite digambar dengan primitif (garis, poligon, lingkaran) supaya siluetnya
benar-benar berbeda satu sama lain, bukan varyasi satu template.

Jalankan ulang:  python3 tools/generate_assets.py
"""
from __future__ import annotations

import json
import math
import os

from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(ROOT, "src", "main", "resources", "assets", "soulland")
TEX_ITEM = os.path.join(ASSETS, "textures", "item")
TEX_ENTITY = os.path.join(ASSETS, "textures", "entity")
MODEL_ITEM = os.path.join(ASSETS, "models", "item")

SIZE = 16


# ---------------------------------------------------------------- warna

def rgba(value: str, alpha: int = 255) -> tuple[int, int, int, int]:
    value = value.lstrip("#")
    return int(value[0:2], 16), int(value[2:4], 16), int(value[4:6], 16), alpha


# ---------------------------------------------------------------- primitif

class Canvas:
    """Kanvas piksel 16x16 dengan primitif gambar."""

    def __init__(self, size: int = SIZE) -> None:
        self.size = size
        self.image = Image.new("RGBA", (size, size), (0, 0, 0, 0))
        self.pixels = self.image.load()

    def px(self, x: int, y: int, color) -> None:
        if 0 <= x < self.size and 0 <= y < self.size:
            self.pixels[x, y] = color

    def rect(self, x0: int, y0: int, x1: int, y1: int, color) -> None:
        for y in range(min(y0, y1), max(y0, y1) + 1):
            for x in range(min(x0, x1), max(x0, x1) + 1):
                self.px(x, y, color)

    def frame(self, x0: int, y0: int, x1: int, y1: int, color) -> None:
        for x in range(min(x0, x1), max(x0, x1) + 1):
            self.px(x, y0, color)
            self.px(x, y1, color)
        for y in range(min(y0, y1), max(y0, y1) + 1):
            self.px(x0, y, color)
            self.px(x1, y, color)

    def line(self, x0: float, y0: float, x1: float, y1: float, color, width: int = 1) -> None:
        steps = int(max(abs(x1 - x0), abs(y1 - y0)) * 2) + 1
        for step in range(steps + 1):
            t = step / steps
            cx = x0 + (x1 - x0) * t
            cy = y0 + (y1 - y0) * t
            self.dot(cx, cy, color, width)

    def dot(self, cx: float, cy: float, color, width: int = 1) -> None:
        radius = width / 2.0
        for y in range(int(math.floor(cy - radius)), int(math.ceil(cy + radius)) + 1):
            for x in range(int(math.floor(cx - radius)), int(math.ceil(cx + radius)) + 1):
                if (x + 0.5 - cx) ** 2 + (y + 0.5 - cy) ** 2 <= radius ** 2 + 0.3:
                    self.px(x, y, color)

    def poly(self, points, color) -> None:
        """Isi poligon dengan metode scanline (aturan even-odd)."""
        if len(points) < 3:
            return
        ys = [p[1] for p in points]
        for y in range(int(math.floor(min(ys))), int(math.ceil(max(ys))) + 1):
            crossings = []
            count = len(points)
            for index in range(count):
                x0, y0 = points[index]
                x1, y1 = points[(index + 1) % count]
                if (y0 <= y + 0.5 < y1) or (y1 <= y + 0.5 < y0):
                    t = (y + 0.5 - y0) / (y1 - y0)
                    crossings.append(x0 + (x1 - x0) * t)
            crossings.sort()
            for pair in range(0, len(crossings) - 1, 2):
                start = int(math.ceil(crossings[pair] - 0.5))
                end = int(math.floor(crossings[pair + 1] - 0.5))
                for x in range(start, end + 1):
                    self.px(x, y, color)

    def ellipse(self, cx: float, cy: float, rx: float, ry: float, color, fill: bool = True) -> None:
        for y in range(self.size):
            for x in range(self.size):
                nx = (x + 0.5 - cx) / rx
                ny = (y + 0.5 - cy) / ry
                value = nx * nx + ny * ny
                if fill and value <= 1.0:
                    self.px(x, y, color)
                elif not fill and 0.55 <= value <= 1.0:
                    self.px(x, y, color)

    def arc(self, cx: float, cy: float, rx: float, ry: float, start: float, end: float,
            color, width: int = 1) -> None:
        steps = max(24, int(abs(end - start) * 22))
        previous = None
        for step in range(steps + 1):
            angle = start + (end - start) * step / steps
            point = (cx + math.cos(angle) * rx, cy + math.sin(angle) * ry)
            if previous is not None:
                self.line(previous[0], previous[1], point[0], point[1], color, width)
            previous = point


def write_png(image: Image.Image, path: str) -> None:
    os.makedirs(os.path.dirname(path), exist_ok=True)
    image.save(path, "PNG")
    print(f"  wrote {os.path.relpath(path, ROOT)}")


def write_model(item_id: str) -> None:
    os.makedirs(MODEL_ITEM, exist_ok=True)
    path = os.path.join(MODEL_ITEM, f"{item_id}.json")
    payload = {"parent": "item/generated", "textures": {"layer0": f"soulland:item/{item_id}"}}
    with open(path, "w", encoding="utf-8") as handle:
        json.dump(payload, handle, indent=2)
        handle.write("\n")
    print(f"  wrote {os.path.relpath(path, ROOT)}")


# ---------------------------------------------------------------- manifestasi

def hoe() -> Image.Image:
    """Cangkul Tua: gagang kayu yang sudah aus + bilah besi. Nada tanah dan logam."""
    c = Canvas()
    wood_dark = rgba("#4E3320")
    wood = rgba("#8A6136")
    wood_light = rgba("#B98A52")
    iron_dark = rgba("#59606B")
    iron = rgba("#8C96A3")
    iron_light = rgba("#C4CEDA")

    # gagang: diagonal dari kiri-bawah ke kanan-atas
    c.line(1.5, 14.0, 8.5, 6.5, wood_dark, 3)
    c.line(1.5, 14.0, 8.5, 6.5, wood, 2)
    c.line(2.0, 13.4, 8.6, 6.6, wood_light, 1)
    # gagang melebar di pangkal
    c.rect(0, 14, 2, 15, wood_dark)
    c.rect(0, 14, 1, 15, wood)
    # bilah besi melintang di ujung
    c.poly([(7, 6), (13, 2), (15, 6), (9, 10)], iron_dark)
    c.poly([(7, 5), (13, 1), (15, 5), (9, 9)], iron)
    c.poly([(8, 4), (12, 2), (13, 4), (10, 7)], iron_light)
    c.line(7, 5, 13, 1, iron_light, 1)
    # baut pada bilah
    c.px(11, 4, iron_dark)
    c.px(12, 5, iron_dark)
    return c.image


def blue_silver_grass() -> Image.Image:
    """Blue Silver Grass: tiga bilah rumput biru perak di atas gumpalan tanah."""
    c = Canvas()
    deep = rgba("#1E3A6B")
    mid = rgba("#3D6FBE")
    light = rgba("#7FB4EC")
    silver = rgba("#DCEEFF")
    soil = rgba("#4A3520")
    soil_light = rgba("#6B4C2E")

    # bilah kiri
    c.line(3.0, 12.0, 1.5, 2.0, mid, 2)
    c.line(3.0, 12.0, 1.5, 2.0, light, 1)
    # bilah tengah (tunggal perak)
    c.line(8.0, 12.5, 8.0, 1.0, mid, 3)
    c.line(8.0, 12.5, 8.0, 1.0, silver, 1)
    # bilah kanan
    c.line(12.5, 12.0, 13.5, 3.0, mid, 2)
    c.line(12.5, 12.0, 13.5, 3.0, light, 1)
    # kilau perak di pangkal bilah tengah
    c.dot(8.0, 10.5, silver, 2)
    c.dot(9.0, 6.0, silver, 1)
    # tanah
    c.ellipse(8.0, 13.2, 6.4, 2.2, soil)
    c.ellipse(8.0, 12.7, 5.4, 1.5, soil_light)
    for x in (4, 7, 11):
        c.px(x, 12, soil_light)
    c.px(6, 15, deep)
    return c.image


def hell_civet() -> Image.Image:
    """Hell Civet: kepala kucing bayangan, mata ungu, ekor asap magenta."""
    c = Canvas()
    fur_dark = rgba("#211C29")
    fur = rgba("#3C3549")
    fur_light = rgba("#564D68")
    eye = rgba("#B45BE8")
    smoke = rgba("#C455BE")

    # ekor asap melingkar ke kiri-bawah
    c.line(6.5, 11.5, 3.0, 12.5, smoke, 2)
    c.line(3.0, 12.5, 1.5, 10.5, smoke, 1)
    c.dot(1.5, 10.5, smoke, 2)
    # kepala
    c.ellipse(9.5, 7.0, 4.6, 4.0, fur_dark)
    c.ellipse(9.5, 7.2, 3.9, 3.3, fur)
    c.ellipse(8.6, 6.4, 2.4, 2.0, fur_light)
    # telinga
    c.poly([(5.6, 4.6), (6.2, 1.2), (8.4, 3.6)], fur_dark)
    c.poly([(10.6, 3.6), (12.8, 1.2), (13.4, 4.6)], fur_dark)
    c.poly([(6.4, 4.0), (6.8, 2.4), (8.0, 3.8)], fur)
    c.poly([(11.0, 3.8), (12.2, 2.4), (12.6, 4.0)], fur)
    # mata
    c.rect(7, 6, 8, 7, eye)
    c.rect(11, 6, 12, 7, eye)
    c.px(7, 6, rgba("#F2D8FF"))
    c.px(11, 6, rgba("#F2D8FF"))
    # hidung
    c.px(9, 9, rgba("#120F16"))
    c.px(10, 9, rgba("#120F16"))
    # kumis
    c.line(9, 10, 6, 10, fur_light, 1)
    c.line(10, 10, 13, 10, fur_light, 1)
    return c.image


def sausage() -> Image.Image:
    """Sosis Ajaib: sosis panggang diagonal dengan tanda arang dan kilau."""
    c = Canvas()
    skin_dark = rgba("#7E2822")
    skin = rgba("#C4453A")
    skin_light = rgba("#E88468")
    char = rgba("#4E1C17")

    # kapsul diagonal
    c.line(3.5, 12.0, 11.0, 4.5, skin_dark, 5)
    c.line(3.5, 12.0, 11.0, 4.5, skin, 4)
    c.line(4.0, 11.2, 10.6, 4.6, skin_light, 1)
    # tanda arang
    for step in range(4):
        cx = 5.2 + step * 1.7
        cy = 10.2 - step * 1.6
        c.line(cx - 0.6, cy - 1.2, cx + 0.6, cy + 1.2, char, 1)
    # ujung terbakar
    c.dot(11.0, 4.5, char, 2)
    return c.image


def seven_treasure_pagoda() -> Image.Image:
    """Pagoda tujuh lantai: atap giok bersusun, tiang merah, pinnacle emas."""
    c = Canvas()
    gold = rgba("#C2922F")
    gold_light = rgba("#F2D071")
    teal = rgba("#2E8B84")
    teal_light = rgba("#56BDB0")
    dark = rgba("#6B4E1F")
    red = rgba("#B33A32")
    red_light = rgba("#D9614F")

    tiers = 6
    base_y = 13
    for tier in range(tiers):
        half = 6 - tier
        y = base_y - tier * 2
        roof = [(8 - half - 1, y), (8 + half + 1, y), (8 + half - 1, y - 1), (8 - half + 1, y - 1)]
        c.poly(roof, teal)
        c.line(roof[0][0], roof[0][1], roof[1][0], roof[1][1], teal_light, 1)
        c.poly([(8 - half, y + 1), (8 + half, y + 1), (8 + half - 1, y), (8 - half + 1, y)], dark)
        c.px(8 - half + 1, y, gold_light)
        c.px(8 + half - 1, y, gold_light)
    # badan
    c.rect(3, 12, 12, 13, red)
    c.line(3, 12, 12, 12, red_light, 1)
    # pinnacle
    c.line(8, 0, 8, 2, gold, 1)
    c.px(8, 0, gold_light)
    return c.image


def evil_fire_phoenix() -> Image.Image:
    """Evil Fire Phoenix: sayap api terentang, tubuh menyala, ekor bara."""
    c = Canvas()
    ember = rgba("#5E1A12")
    red = rgba("#B32A1E")
    orange = rgba("#E4572E")
    flame = rgba("#F0913C")
    gold = rgba("#FFD75E")
    spark = rgba("#FFF3C4")

    # sayap kiri
    c.poly([(7, 8), (0, 3), (1, 6), (0, 8), (3, 8), (5, 11)], red)
    c.poly([(7, 8), (1, 4), (2, 6), (1, 7), (4, 8)], orange)
    c.poly([(7, 8), (2, 5), (3, 7), (5, 8)], flame)
    # sayap kanan
    c.poly([(8, 8), (15, 3), (14, 6), (15, 8), (12, 8), (10, 11)], red)
    c.poly([(8, 8), (14, 4), (13, 6), (14, 7), (11, 8)], orange)
    c.poly([(8, 8), (13, 5), (12, 7), (10, 8)], flame)
    # kepala dan 몸
    c.ellipse(7.6, 6.4, 1.7, 2.1, orange)
    c.ellipse(7.4, 5.8, 1.2, 1.4, gold)
    c.px(7, 5, spark)
    c.ellipse(7.8, 8.6, 1.6, 2.4, orange)
    c.ellipse(7.8, 8.4, 1.0, 1.7, gold)
    # ekor bara
    c.line(7.6, 11.0, 6.4, 14.6, ember, 2)
    c.line(6.4, 14.6, 7.4, 15.4, ember, 1)
    c.line(8.2, 11.0, 9.6, 13.6, ember, 1)
    return c.image


def white_tiger() -> Image.Image:
    """White Tiger: kepala harimau putih, belang hitam, mata biru, hidung merah muda."""
    c = Canvas()
    white = rgba("#F2F0EC")
    shade = rgba("#CBC7BF")
    stripe = rgba("#2B2622")
    eye = rgba("#3E8FD0")
    nose = rgba("#D98A8A")
    outline = rgba("#8C8880")

    # telinga
    c.poly([(2, 5), (3, 1), (6, 3)], white)
    c.poly([(13, 5), (12, 1), (9, 3)], white)
    c.poly([(3, 4), (3.6, 2.2), (5.2, 3.4)], shade)
    c.poly([(12, 4), (11.4, 2.2), (9.8, 3.4)], shade)
    # kepala
    c.ellipse(7.5, 7.5, 5.0, 4.4, outline)
    c.ellipse(7.5, 7.5, 4.6, 4.0, white)
    c.ellipse(7.5, 8.8, 3.2, 2.8, shade)
    # belang di dahi dan pipi
    c.poly([(4, 3), (6, 3), (6, 6), (5, 7)], stripe)
    c.poly([(9, 3), (11, 3), (10, 7), (9, 6)], stripe)
    c.poly([(2, 7), (4, 7), (4, 9), (3, 9)], stripe)
    c.poly([(13, 7), (11, 7), (11, 9), (12, 9)], stripe)
    c.poly([(7, 3), (8, 3), (8, 5), (7, 5)], stripe)
    # mata
    c.rect(5, 7, 6, 8, eye)
    c.rect(9, 7, 10, 8, eye)
    c.px(5, 7, rgba("#BFE4FF"))
    c.px(9, 7, rgba("#BFE4FF"))
    # hidung dan mulut
    c.rect(7, 9, 8, 10, nose)
    c.line(7, 11, 6, 12, stripe, 1)
    c.line(8, 11, 9, 12, stripe, 1)
    # kumis
    c.line(6, 11, 2, 11, stripe, 1)
    c.line(9, 11, 13, 11, stripe, 1)
    return c.image


def soft_bone_rabbit() -> Image.Image:
    """Soft Bone Rabbit: kelinci tulang lunak, telinga panjang, ekor bulat."""
    c = Canvas()
    fur = rgba("#F4F1EC")
    shade = rgba("#CFC9C0")
    inner = rgba("#E8A9A9")
    eye = rgba("#2B2B33")
    bone = rgba("#D9CFA8")

    # telinga panjang dan miring ke luar
    c.poly([(3, 8), (1, 1), (4, 0), (5, 7)], fur)
    c.poly([(3, 7), (2, 2), (4, 2), (4, 6)], inner)
    c.poly([(12, 8), (14, 1), (11, 0), (10, 7)], fur)
    c.poly([(12, 7), (13, 2), (11, 2), (11, 6)], inner)
    # kepala
    c.ellipse(7.5, 8.2, 4.2, 3.6, fur)
    c.ellipse(7.5, 8.8, 2.8, 2.4, shade)
    # mata
    c.rect(5, 7, 6, 8, eye)
    c.rect(9, 7, 10, 8, eye)
    c.px(5, 7, rgba("#FFFFFF"))
    c.px(9, 7, rgba("#FFFFFF"))
    # hidung
    c.rect(7, 9, 8, 9, inner)
    c.line(7, 10, 6, 11, shade, 1)
    c.line(8, 10, 9, 11, shade, 1)
    # tulang lunak di dada
    c.line(7.5, 11.0, 7.5, 14.0, bone, 1)
    c.dot(7.5, 12.0, bone, 2)
    # ekor
    c.ellipse(12.6, 12.0, 1.6, 1.6, fur)
    c.dot(12.6, 12.0, shade, 1)
    return c.image


def jade_serpent() -> Image.Image:
    """Jade Phosphor Serpent: ular giok melingkar dengan mata emas dan sisik."""
    c = Canvas()
    dark = rgba("#143A2C")
    jade = rgba("#45B98A")
    light = rgba("#8FE3C2")
    gold = rgba("#F2D071")

    # tubuh melingkar (parabola)
    points = []
    for step in range(40):
        t = step / 39.0
        x = 3.0 + t * 9.0
        y = 12.0 - t * t * 8.5
        points.append((x, y))
    for index in range(len(points) - 1):
        c.line(points[index][0], points[index][1], points[index + 1][0], points[index + 1][1], jade, 3)
    for index in range(len(points) - 1):
        c.line(points[index][0], points[index][1] - 0.6, points[index + 1][0], points[index + 1][1] - 0.6, light, 1)
    # kepala
    c.ellipse(4.6, 3.6, 3.2, 2.6, jade)
    c.ellipse(4.4, 3.2, 2.4, 1.7, light)
    c.rect(3, 3, 4, 4, gold)
    c.px(3, 3, rgba("#6B4E1F"))
    # tongue
    c.line(1.6, 4.0, 0.4, 4.6, dark, 1)
    c.px(0, 5, dark)
    # sisik
    c.px(9, 8, dark)
    c.px(10, 6, dark)
    return c.image


def clear_sky_hammer() -> Image.Image:
    """Clear Sky Hammer: palu Langit Jernih, kepala baja biru dengan sambaran kilat."""
    c = Canvas()
    steel_dark = rgba("#4E5661")
    steel = rgba("#8E97A6")
    steel_light = rgba("#C3CBD6")
    azure = rgba("#3A6ED0")
    azure_light = rgba("#8FC0F5")
    bolt = rgba("#FFE86B")

    # kepala palu
    c.poly([(3, 3), (11, 3), (13, 5), (13, 8), (11, 10), (3, 10), (1, 8), (1, 5)], steel_dark)
    c.poly([(4, 4), (10, 4), (12, 6), (12, 7), (10, 8), (4, 8), (2, 7), (2, 5)], steel)
    c.poly([(4, 4), (10, 4), (11, 5), (4, 6), (3, 5)], azure)
    c.rect(5, 4, 9, 5, azure_light)
    c.px(4, 4, steel_light)
    # gagang diagonal ke kiri-bawah
    c.line(7.0, 10.0, 3.0, 15.0, steel_dark, 3)
    c.line(7.0, 10.0, 3.0, 15.0, steel, 2)
    c.line(7.0, 10.0, 3.4, 14.6, steel_light, 1)
    # sambaran kilat
    c.poly([(13, 0), (11, 3), (13, 3), (10, 6), (12, 4), (10, 4)], bolt)
    c.poly([(0, 6), (2, 8), (0, 8), (3, 11), (1, 9), (3, 9)], bolt)
    return c.image


MANIFESTATIONS = {
    "manifestation_hoe": hoe,
    "manifestation_blue_silver_grass": blue_silver_grass,
    "manifestation_hell_civet": hell_civet,
    "manifestation_sausage": sausage,
    "manifestation_seven_treasure_pagoda": seven_treasure_pagoda,
    "manifestation_evil_fire_phoenix": evil_fire_phoenix,
    "manifestation_white_tiger": white_tiger,
    "manifestation_soft_bone_rabbit": soft_bone_rabbit,
    "manifestation_jade_serpent": jade_serpent,
    "manifestation_clear_sky_hammer": clear_sky_hammer,
}


# ---------------------------------------------------------------- spirit ring

def spirit_ring(color: str, thickness: int = 1, spikes: bool = False, sparkle: bool = False) -> Image.Image:
    """Cincin pipih dilihat dari sudut: warna sesuai umur ring."""
    c = Canvas()
    body = rgba(color)
    light = rgba(color, 220)
    dark = rgba(color, 150)
    cx, cy = 7.5, 8.0
    rx, ry = 6.0, 4.0
    c.ellipse(cx, cy, rx, ry, body, fill=False)
    if thickness > 1:
        c.ellipse(cx, cy, rx - thickness, ry - thickness, body, fill=False)
    # kilau di busur kiri-atas
    c.arc(cx, cy, rx - 0.5, ry - 0.5, math.pi * 0.95, math.pi * 1.45, light, 1)
    c.arc(cx, cy, rx + 0.5, ry + 0.5, math.pi * 0.05, math.pi * 0.35, dark, 1)
    if sparkle:
        c.px(2, 4, light)
        c.px(13, 12, light)
    if spikes:
        for angle in (0.0, math.pi * 0.5, math.pi, math.pi * 1.5):
            x = cx + math.cos(angle) * (rx + 1.6)
            y = cy + math.sin(angle) * (ry + 1.6)
            c.dot(x, y, light, 2)
    return c.image


RINGS = {
    "spirit_ring_100": lambda: spirit_ring("#E4E8EA", 1),
    "spirit_ring_1000": lambda: spirit_ring("#F0C040", 1, sparkle=True),
    "spirit_ring_10000": lambda: spirit_ring("#B36BE0", 2),
    "spirit_ring_100000": lambda: spirit_ring("#D2453C", 2, spikes=True),
}


# ---------------------------------------------------------------- halo sprite

def build_halo_sprite() -> None:
    """Cincin putih 64x64: satu sprite untuk semua umur ring.

    Alpha memberi profile cincin (tepi terang, badan lembut) plus rune yang
    menyala, sehingga umur ring cukup diwujudkan lewat tint warna.
    """
    size = 64
    center = (size - 1) / 2.0
    outer = 30.0
    inner = 17.0
    runes = 6
    image = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    pixels = image.load()
    for y in range(size):
        for x in range(size):
            dx = x - center
            dy = y - center
            dist = math.sqrt(dx * dx + dy * dy)
            if dist > outer or dist < inner:
                continue
            t = (dist - inner) / (outer - inner)
            alpha = int(90 + 165 * (t ** 1.6))
            angle = math.atan2(dy, dx)
            segment = (angle + math.pi) / (2 * math.pi) * runes
            if abs(segment - round(segment)) < 0.14:
                alpha = min(255, alpha + 70)
            pixels[x, y] = (255, 255, 255, alpha)
    write_png(image, os.path.join(TEX_ENTITY, "spirit_ring.png"))


# ---------------------------------------------------------------- contact sheet

def build_contact_sheet() -> None:
    """Lembar kontak untuk pemeriksaan visual asset (di luar folder mod)."""
    zoom = 8
    columns = 5
    sheet = Image.new("RGBA", (columns * 16 * zoom, 3 * 16 * zoom), (24, 27, 32, 255))
    for index, builder in enumerate(MANIFESTATIONS.values()):
        sprite = builder().resize((16 * zoom, 16 * zoom), Image.NEAREST)
        sheet.alpha_composite(sprite, ((index % columns) * 16 * zoom, (index // columns) * 16 * zoom))
    for index, builder in enumerate(RINGS.values()):
        sprite = builder().resize((16 * zoom, 16 * zoom), Image.NEAREST)
        sheet.alpha_composite(sprite, (index * 16 * zoom, 2 * 16 * zoom))
    sheet.save(os.path.join("/tmp/opencode", "manifestation-sheet.png"))


# ---------------------------------------------------------------- main

def main() -> None:
    print("Manifestasi martial soul:")
    for item_id, builder in MANIFESTATIONS.items():
        write_png(builder(), os.path.join(TEX_ITEM, f"{item_id}.png"))
        write_model(item_id)

    print("Spirit ring:")
    for item_id, builder in RINGS.items():
        write_png(builder(), os.path.join(TEX_ITEM, f"{item_id}.png"))
        write_model(item_id)

    build_halo_sprite()
    build_contact_sheet()
    print("Selesai.")


if __name__ == "__main__":
    main()
