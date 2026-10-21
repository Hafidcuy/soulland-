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


# ================================================================ v1.2A

# Tiga mutu material tool: warna logam terang/gelap + warna motif mutu.
TIER_METAL = {
    "spirit_iron": ("#B4BEC9", "#6E7885"),
    "spirit_steel": ("#8E97B4", "#4A5168"),
    "deep_silver": ("#DDE7F5", "#55657A"),
}
TIER_ACCENT = {
    "spirit_iron": "#E8EDF2",
    "spirit_steel": "#C6CEEA",
    "deep_silver": "#7FE8E0",
}
TIER_ORDER = ("spirit_iron", "spirit_steel", "deep_silver")

# Identitas warna dan motif tiap soul (warna utama, warna kedua, motif).
SOUL_PALETTE = {
    "hoe": ("#B8C6D8", "#3C6BA8", "chevron"),
    "blue_silver_grass": ("#4A7FD1", "#DCEEFF", "spiral"),
    "hell_civet": ("#6A3FA0", "#191622", "mist"),
    "sausage": ("#E2703A", "#F3E3C2", "smoke"),
    "seven_treasure_pagoda": ("#2E9E92", "#E8A33C", "layers"),
    "evil_fire_phoenix": ("#C8321E", "#F2C13C", "feather"),
    "white_tiger": ("#F1EEE8", "#2B2622", "stripes"),
    "soft_bone_rabbit": ("#E4DCC4", "#8C8578", "ribs"),
    "jade_serpent": ("#2F9E78", "#9FE8C4", "scales"),
    "clear_sky_hammer": ("#4C5563", "#F0C23C", "bolt"),
}

# Soul -> (tipe tool, nama EN, nama ID, label singkat untuk lembar kontak).
SOUL_TOOL = {
    "hoe": ("sword", "Old Hoe Blade", "Bilah Cangkul Tua", "HOE"),
    "blue_silver_grass": ("spear", "Blue Silver Spear", "Tombak Rumput Biru-Perak", "BSG"),
    "hell_civet": ("fang", "Hell Civet Fang", "Taring Anceng Neraka", "CIVET"),
    "sausage": ("fan", "Sausage Fan", "Kipas Sosis", "SAUSAGE"),
    "seven_treasure_pagoda": ("seal", "Seven Treasure Seal", "Segel Tujuh Harta", "PAGODA"),
    "evil_fire_phoenix": ("bow", "Evil Fire Phoenix Bow", "Busur Phoenix Api", "PHOENIX"),
    "white_tiger": ("claw", "White Tiger Claw", "Cakar Harimau Putih", "TIGER"),
    "soft_bone_rabbit": ("staff", "Soft Bone Staff", "Tongkat Kelinci Tulang", "RABBIT"),
    "jade_serpent": ("dagger", "Jade Serpent Dagger", "Belati Ular Giok", "SERPENT"),
    "clear_sky_hammer": ("hammer", "Clear Sky Hammer", "Palu Langit Jernih", "HAMMER"),
}

TIER_NAME_EN = {
    "spirit_iron": "Spirit Iron",
    "spirit_steel": "Spirit Steel",
    "deep_silver": "Deep Silver",
}
TIER_NAME_ID = {
    "spirit_iron": "Besi Spirit",
    "spirit_steel": "Baja Spirit",
    "deep_silver": "Perak Dalam",
}


def tool_colors(tier: str, soul: str) -> dict:
    """Warna drawing untuk satu tool: logam mutu + warna soul + motif mutu."""
    metal_light, metal_dark = TIER_METAL[tier]
    soul_light, soul_dark, _ = SOUL_PALETTE[soul]
    return {
        "m1": rgba(metal_light),
        "m2": rgba(metal_dark),
        "acc": rgba(TIER_ACCENT[tier]),
        "s1": rgba(soul_light),
        "s2": rgba(soul_dark),
    }


def _curve(c: Canvas, p0, p1, p2, color, width: int = 1) -> None:
    """Kurva kuadrat dari tiga titik, dipakai untuk cakar dan busur."""
    previous = None
    for step in range(19):
        t = step / 18.0
        x = (1 - t) ** 2 * p0[0] + 2 * (1 - t) * t * p1[0] + t * t * p2[0]
        y = (1 - t) ** 2 * p0[1] + 2 * (1 - t) * t * p1[1] + t * t * p2[1]
        if previous is not None:
            c.line(previous[0], previous[1], x, y, color, width)
        previous = (x, y)


def _shards(c: Canvas, spots, colors) -> None:
    """Serpihan kristal melayang: massa terpisah di luar badan tool."""
    for x, y, size in spots:
        c.poly([(x, y - size), (x + size * 0.6, y), (x, y + size), (x - size * 0.6, y)], colors["acc"])
        c.px(x, y, colors["m1"])


def _crystal(c: Canvas, cx: float, cy: float, half_w: float, half_h: float, colors) -> None:
    """Kristal bersudut dengan facet dalam."""
    c.poly([
        (cx, cy - half_h),
        (cx + half_w, cy - half_h * 0.25),
        (cx + half_w * 0.55, cy + half_h),
        (cx - half_w * 0.55, cy + half_h),
        (cx - half_w, cy - half_h * 0.25),
    ], colors["m1"])
    c.poly([
        (cx, cy - half_h),
        (cx + half_w, cy - half_h * 0.25),
        (cx, cy + half_h * 0.1),
    ], colors["acc"])
    c.poly([
        (cx - half_w, cy - half_h * 0.25),
        (cx, cy - half_h),
        (cx, cy + half_h * 0.1),
    ], colors["m2"])


# ------------------------------------------------------------------ bentuk dasar
# Tiap arketipe menempati wilayah kanvas yang berbeda supaya siluet antar
# archetype tidak pernah bertumpuk. Motif soul dipakai sebagai detail warna
# di dalam bentuk, bukan sebagai coretan terpisah.


def base_sword(c: Canvas, col) -> None:
    """Bilah Cangkul Tua: pedang tegak, pelindung lebar, motif chevron."""
    c.rect(7, 2, 8, 9, col["m1"])
    c.rect(7, 2, 7, 9, col["m2"])
    c.rect(4, 10, 11, 10, col["s2"])
    c.rect(5, 11, 10, 11, col["s2"])
    c.rect(6, 12, 9, 12, col["acc"])
    c.rect(7, 13, 8, 13, col["m2"])
    c.rect(7, 14, 8, 14, col["m2"])
    for step in range(3):
        c.px(5 + step * 3, 10, col["acc"])


def base_spear(c: Canvas, col) -> None:
    """Tombak Rumput Biru-Perak: tombak tegak pendek, bilah daun, spiral sulur."""
    c.rect(7, 6, 8, 14, col["m2"])
    c.rect(8, 6, 8, 14, col["m1"])
    c.poly([(7, 6), (5, 2), (8, 0), (10, 2), (8, 6)], col["m1"])
    c.poly([(7, 5), (6, 2), (8, 1), (8, 4)], col["acc"])
    c.arc(7.5, 11.0, 1.8, 1.8, math.pi * 0.1, math.pi * 1.7, col["s1"], 1)
    c.dot(7.5, 11.0, col["s2"], 1)
    c.px(7, 9, col["s1"])


def base_fang(c: Canvas, col) -> None:
    """Taring Anceng Neraka: tiga cakar melengkung berjajar, kabut ungu."""
    c.line(6.0, 2.5, 4.5, 9.5, col["m1"], 2)
    c.line(6.0, 2.5, 4.5, 9.5, col["m2"], 1)
    c.line(10.0, 4.5, 8.5, 11.5, col["m1"], 2)
    c.line(10.0, 4.5, 8.5, 11.5, col["m2"], 1)
    c.line(13.5, 7.0, 12.0, 14.0, col["m1"], 2)
    c.line(13.5, 7.0, 12.0, 14.0, col["m2"], 1)
    for x, y in ((6.0, 2.5), (10.0, 4.5), (13.5, 7.0)):
        c.dot(x, y, col["s1"], 2)
    for x, y in ((3.0, 5.0), (7.5, 13.0), (14.5, 3.0)):
        c.dot(x, y, col["s2"], 2)


def base_fan(c: Canvas, col) -> None:
    """Kipas Sosis: kipas terbuka melingkar dari poros bawah, pilinan asap."""
    pivot = (7.5, 14.5)
    for step in range(5):
        angle = math.pi * (0.78 + step * 0.11)
        c.line(pivot[0], pivot[1], pivot[0] + math.cos(angle) * 11.0, pivot[1] - math.sin(angle) * 12.0,
               col["m1"] if step % 2 == 0 else col["s1"], 1)
    c.poly([(7.5, 14.5), (2, 5), (4, 3), (7.5, 8)], col["s1"])
    c.poly([(7.5, 14.5), (7.5, 3), (10, 4), (7.5, 8)], col["s1"])
    c.poly([(7.5, 14.5), (13, 5), (11, 3), (7.5, 8)], col["s1"])
    c.rect(6, 13, 9, 15, col["s1"])
    c.px(7.5, 14.5, col["acc"])


def base_seal(c: Canvas, col) -> None:
    """Segel Tujuh Harta: dua kubus rapat dengan motif tujuh lapis."""
    c.rect(6, 3, 10, 7, col["m1"])
    c.rect(5, 9, 11, 14, col["m1"])
    c.rect(7, 4, 9, 5, col["m2"])
    c.rect(6, 10, 10, 12, col["m2"])
    c.rect(7, 9, 9, 9, col["s1"])
    c.px(8, 4, col["acc"])
    c.px(8, 11, col["acc"])


def base_bow(c: Canvas, col) -> None:
    """Busur Phoenix Api: busur melengkung dengan senar dan bulu ekor."""
    c.arc(13.0, 8.0, 8.0, 7.0, math.pi * 0.58, math.pi * 1.42, col["m1"], 2)
    c.arc(13.0, 8.0, 8.0, 7.0, math.pi * 0.62, math.pi * 1.38, col["s1"], 1)
    c.line(8.4, 2.5, 8.4, 13.5, col["m2"], 1)
    c.poly([(11, 4), (14, 1), (12, 1)], col["acc"])
    c.poly([(11, 12), (14, 15), (12, 15)], col["acc"])
    c.rect(7, 7, 9, 9, col["m2"])
    c.px(8, 8, col["s1"])


def base_claw(c: Canvas, col) -> None:
    """Cakar Harimau Putih: tiga bilah cakar menyapu dari sudut kiri atas."""
    c.line(1.5, 1.5, 13.0, 5.0, col["m1"], 2)
    c.line(1.5, 1.5, 13.0, 5.0, col["m2"], 1)
    c.line(1.5, 1.5, 11.0, 9.5, col["m1"], 2)
    c.line(1.5, 1.5, 11.0, 9.5, col["m2"], 1)
    c.line(1.5, 1.5, 7.0, 12.5, col["m1"], 2)
    c.line(1.5, 1.5, 7.0, 12.5, col["m2"], 1)
    c.dot(1.5, 1.5, col["s1"], 3)
    for x, y in ((9, 4), (7, 7), (4, 9)):
        c.px(x, y, col["s2"])
    c.px(13, 5, col["acc"])


def base_staff(c: Canvas, col) -> None:
    """Tongkat Kelinci Tulang: bonggol tulang bertulang rusuk di ujung atas."""
    c.rect(10, 6, 11, 15, col["m2"])
    c.rect(10, 6, 10, 15, col["m1"])
    c.ellipse(10.5, 3.6, 3.0, 2.4, col["m1"])
    c.ellipse(8.2, 2.4, 1.6, 1.4, col["m1"])
    c.ellipse(12.8, 2.4, 1.6, 1.4, col["m1"])
    c.ellipse(10.5, 4.0, 1.4, 1.2, col["s1"])
    for step in range(3):
        c.arc(10.5, 9.0 + step * 1.4, 1.8, 1.8, math.pi * 1.2, math.pi * 1.8, col["s1"], 1)
    c.px(10, 13, col["acc"])


def base_dagger(c: Canvas, col) -> None:
    """Belati Ular Giok: bilah pendek melengkung, motif sisik hijau."""
    c.poly([(6, 12), (7, 5), (10, 6), (9, 12)], col["m1"])
    c.poly([(7, 11), (7.6, 6), (8.8, 6.4), (8.2, 11)], col["acc"])
    c.rect(5, 12, 10, 13, col["m2"])
    c.rect(7, 14, 8, 15, col["s2"])
    for step in range(3):
        c.arc(6.0 + step * 2.0, 9.0 - step * 0.4, 1.4, 1.2, math.pi * 0.1, math.pi * 1.1, col["s1"], 1)


def base_hammer(c: Canvas, col) -> None:
    """Palu Langit Jernih: kepala masif di kiri atas, gagang diagonal."""
    c.poly([(1, 1), (10, 1), (10, 6), (1, 6)], col["m1"])
    c.poly([(2, 2), (8, 2), (8, 3), (2, 3)], col["acc"])
    c.rect(1, 4, 10, 5, col["m2"])
    c.line(7.5, 6.5, 14.0, 13.5, col["m2"], 3)
    c.line(7.5, 6.5, 14.0, 13.5, col["m1"], 1)
    c.poly([(5, 6), (4, 10), (7, 7)], col["s1"])


BASE_DRAW = {
    "sword": base_sword,
    "spear": base_spear,
    "fang": base_fang,
    "fan": base_fan,
    "seal": base_seal,
    "bow": base_bow,
    "claw": base_claw,
    "staff": base_staff,
    "dagger": base_dagger,
    "hammer": base_hammer,
}


# ------------------------------------------------------------------ bentuk tempa
# Bentuk Spirit Steel: arketipe yang sama tapi diktempa ulang jadi senjata
# utuh, besar dan menyeberang kanvas. Inilah yang membedakan siluet Spirit
# Iron (alat kecil tegak) dari Spirit Steel (senjata tempa) tanpa jadi satu
# template yang diwarnai ulang.


def forged_sword(c: Canvas, col) -> None:
    """Bilah tempa panjang diagonal dengan pelindung sayap lebar."""
    c.line(3.0, 13.0, 13.0, 2.0, col["m2"], 5)
    c.line(3.0, 13.0, 13.0, 2.0, col["m1"], 2)
    c.poly([(12, 2), (15, 0), (14, 4)], col["m1"])
    c.poly([(13, 2), (14, 1), (13.4, 3)], col["acc"])
    c.poly([(0, 10), (5, 13), (6, 15), (0, 15)], col["s1"])
    c.poly([(0, 11), (4, 13), (4, 14)], col["acc"])
    c.rect(0, 14, 2, 15, col["m2"])
    c.px(8, 8, col["s1"])


def forged_spear(c: Canvas, col) -> None:
    """Tombak tempa: batang penuh menyeberang, bilah daun besar di ujung."""
    c.line(15.0, 15.0, 4.0, 4.0, col["m2"], 5)
    c.line(15.0, 15.0, 4.0, 4.0, col["m1"], 2)
    c.poly([(6, 7), (3, 1), (0, 3), (1, 7), (4, 9)], col["m1"])
    c.poly([(5, 6), (3, 2), (1, 4), (4, 8)], col["acc"])
    c.poly([(11, 10), (14, 11), (12, 14), (9, 13)], col["s1"])
    c.px(9, 9, col["s1"])
    c.px(6, 6, col["s1"])


def forged_fang(c: Canvas, col) -> None:
    """Cakar tempa: tiga cakar melengkung menjulang dari dasar kiri bawah."""
    _curve(c, (1.0, 14.0), (4.0, 4.0), (13.0, 2.0), col["m1"], 3)
    _curve(c, (1.0, 14.0), (5.0, 7.0), (15.0, 7.0), col["m1"], 3)
    _curve(c, (1.0, 14.0), (6.0, 11.0), (13.0, 12.0), col["m1"], 3)
    for tip in ((13.0, 2.0), (15.0, 7.0), (13.0, 12.0)):
        c.dot(tip[0], tip[1], col["s1"], 3)
    c.dot(1.0, 14.0, col["s2"], 3)
    c.px(6, 8, col["acc"])


def forged_fan(c: Canvas, col) -> None:
    """Kipas tempa: lebar penuh dari poros bawah ke semua sisi atas."""
    pivot = (7.5, 15.0)
    for step in range(7):
        angle = math.pi * (0.66 + step * 0.095)
        c.line(pivot[0], pivot[1], pivot[0] + math.cos(angle) * 13.0, pivot[1] - math.sin(angle) * 14.0,
               col["m1"] if step % 2 == 0 else col["s1"], 1)
    c.poly([(7.5, 15.0), (0, 6), (2, 2), (7.5, 8)], col["s1"])
    c.poly([(7.5, 15.0), (7.5, 1), (10, 2), (7.5, 8)], col["s1"])
    c.poly([(7.5, 15.0), (15, 6), (13, 2), (7.5, 8)], col["s1"])
    c.rect(5, 14, 10, 15, col["acc"])
    c.px(7.5, 15.0, col["s1"])


def forged_seal(c: Canvas, col) -> None:
    """Segel tempa: kolom pagoda berlapis dengan cincin jin di atas."""
    c.ellipse(8.0, 1.0, 5.0, 1.6, col["acc"], fill=False)
    c.px(8, 0, col["s1"])
    c.rect(7, 3, 9, 4, col["s1"])
    c.rect(6, 6, 10, 7, col["m1"])
    c.rect(5, 9, 11, 10, col["m1"])
    c.rect(4, 12, 12, 13, col["m1"])
    c.rect(2, 15, 14, 15, col["m1"])
    for y in (6, 9, 12):
        c.rect(6, y, 10, y, col["m2"])
    c.rect(7, 6, 9, 6, col["acc"])
    c.px(8, 10, col["acc"])


def forged_bow(c: Canvas, col) -> None:
    """Busur tempa: busur besar di kanan dengan panah soul yang terpasang."""
    c.arc(12.0, 8.0, 11.0, 7.5, math.pi * 0.55, math.pi * 1.45, col["m1"], 2)
    c.arc(12.0, 8.0, 11.0, 7.5, math.pi * 0.60, math.pi * 1.40, col["s1"], 1)
    c.line(6.0, 2.0, 6.0, 14.0, col["m2"], 1)
    c.line(1.0, 8.0, 11.0, 8.0, col["m2"], 1)
    c.poly([(11, 8), (15, 6), (15, 10)], col["acc"])
    c.px(2, 8, col["s1"])
    c.px(13, 1, col["acc"])


def forged_claw(c: Canvas, col) -> None:
    """Cakar tempa: tiga bilah menyapu dari kiri atas ke kanan bawah."""
    c.line(0.0, 0.0, 15.0, 6.0, col["m1"], 4)
    c.line(0.0, 0.0, 15.0, 6.0, col["m2"], 2)
    c.line(0.0, 2.0, 13.0, 11.0, col["m1"], 4)
    c.line(0.0, 2.0, 13.0, 11.0, col["m2"], 2)
    c.line(0.0, 4.0, 9.0, 15.0, col["m1"], 4)
    c.line(0.0, 4.0, 9.0, 15.0, col["m2"], 2)
    c.dot(0.0, 1.5, col["s1"], 3)
    for x, y in ((11, 5), (9, 8), (6, 11)):
        c.px(x, y, col["s2"])


def forged_staff(c: Canvas, col) -> None:
    """Tongkat tempa: batang tegak di kanan dengan bonggol tulang besar."""
    c.rect(11, 8, 13, 15, col["m2"])
    c.rect(12, 8, 12, 15, col["m1"])
    c.ellipse(12.0, 5.0, 4.0, 3.2, col["m1"])
    c.ellipse(8.5, 3.0, 2.0, 1.8, col["m1"])
    c.ellipse(15.5, 3.0, 2.0, 1.8, col["m1"])
    c.ellipse(12.0, 5.4, 2.0, 1.8, col["s1"])
    for step in range(3):
        c.arc(12.0, 10.0 + step * 1.5, 2.4, 2.4, math.pi * 1.2, math.pi * 1.8, col["s1"], 1)


def forged_dagger(c: Canvas, col) -> None:
    """Belati tempa: bilah pendek melengkung seperti cakar, pita pendek."""
    c.line(6.0, 13.0, 10.0, 8.0, col["m2"], 4)
    c.line(6.0, 13.0, 10.0, 8.0, col["m1"], 2)
    c.poly([(9, 9), (13, 4), (14, 7), (11, 11)], col["m1"])
    c.poly([(10, 8), (12, 5), (13, 7)], col["acc"])
    c.poly([(5, 12), (2, 15), (7, 15), (8, 13)], col["s1"])
    c.px(3, 14, col["acc"])


def forged_hammer(c: Canvas, col) -> None:
    """Palu tempa: kepala raksasa melintang atas, gagang diagonal panjang."""
    c.poly([(0, 0), (15, 0), (15, 5), (0, 5)], col["m1"])
    c.poly([(1, 1), (13, 1), (13, 2), (1, 2)], col["acc"])
    c.rect(0, 3, 15, 4, col["m2"])
    c.line(4.0, 5.0, 14.0, 15.0, col["m2"], 5)
    c.line(4.0, 5.0, 14.0, 15.0, col["m1"], 2)
    c.poly([(6, 5), (4, 10), (8, 6)], col["s1"])


FORGED_DRAW = {
    "sword": forged_sword,
    "spear": forged_spear,
    "fang": forged_fang,
    "fan": forged_fan,
    "seal": forged_seal,
    "bow": forged_bow,
    "claw": forged_claw,
    "staff": forged_staff,
    "dagger": forged_dagger,
    "hammer": forged_hammer,
}


# ------------------------------------------------------------------ bentuk roh
# Serpihan melayang per arketipe, ditempatkan di luar badan tool supaya
# bentuk Perak Dalam tidak pernah menimpa bentuk yang lain.


DEEP_SHARDS = {
    "sword": [(2.0, 4.0, 1.4), (13.5, 11.0, 1.2), (6.0, 15.0, 1.0)],
    "spear": [(13.5, 8.0, 1.3), (4.0, 3.5, 1.1), (9.0, 14.5, 1.0)],
    "fang": [(2.5, 3.0, 1.3), (6.0, 14.5, 1.1)],
    "fan": [(14.0, 12.0, 1.2), (11.0, 14.5, 1.0)],
    "seal": [(2.0, 1.5, 1.0), (13.5, 4.0, 1.0)],
    "bow": [(2.5, 4.0, 1.2), (3.0, 12.0, 1.2), (10.0, 14.5, 1.0)],
    "claw": [(14.0, 2.5, 1.2), (1.5, 13.5, 1.2)],
    "staff": [(2.0, 4.0, 1.2), (6.0, 14.5, 1.1)],
    "dagger": [(2.0, 4.5, 1.2), (13.0, 11.0, 1.2), (4.0, 14.5, 1.0)],
    "hammer": [(2.0, 12.0, 1.2), (13.5, 11.0, 1.2)],
}


def _spectral(base: Image.Image, colors, spots) -> Image.Image:
    """Bentuk Roh: badan tool digambar sebagai bayangan kristal.

    Tepi menyala penuh, isi berupa kisi logam dan warna soul yang jarang,
    ditambah serpihan melayang. Hasilnya terbaca sebagai versi spirit dari
    tool yang sama, bukan warna lain.
    """
    width, height = base.size
    src = base.convert("RGBA")
    pixels = src.load()
    solid = [[pixels[x, y][3] > 40 for x in range(width)] for y in range(height)]
    out = Canvas(height)
    for y in range(height):
        for x in range(width):
            if not solid[y][x]:
                continue
            edge = (
                x == 0 or y == 0 or x == width - 1 or y == height - 1
                or not solid[y - 1][x] or not solid[y + 1][x]
                or not solid[y][x - 1] or not solid[y][x + 1]
            )
            if edge:
                out.px(x, y, colors["acc"])
            elif (x * 7 + y * 13) % 4 == 0:
                out.px(x, y, colors["s1"] if (x + y) % 2 == 0 else colors["m1"])
    _shards(out, spots, colors)
    return out.image


def build_tool(tier: str, soul: str) -> Image.Image:
    """Bangun satu texture tool dari arketipe, mutu, dan soul.

    Spirit Iron memakai bentuk alat kecil, Spirit Steel bentuk senjata tempa
    yang jauh berbeda siluetnya, Spirit Silver bentuk bayangan kristal.
    """
    tool_type = SOUL_TOOL[soul][0]
    colors = tool_colors(tier, soul)
    canvas = Canvas()
    if tier == "spirit_iron":
        BASE_DRAW[tool_type](canvas, colors)
        return canvas.image
    FORGED_DRAW[tool_type](canvas, colors)
    if tier == "spirit_steel":
        STEEL_ORNAMENT[tool_type](canvas, colors)
        return canvas.image
    turned = canvas.image.transpose(Image.ROTATE_90)
    return _spectral(turned, colors, DEEP_SHARDS[tool_type])


# ------------------------------------------------------------------ ornamen baja
# Ornamen mutu Spirit Steel menambah tonjolan yang mengubah siluet, bukan
# hanya warna. Jadi Spirit Iron dan Spirit Steel dari soul yang sama punya
# bentuk yang berbeda jelas, bukan satu template yang diwarnai ulang.


def steel_sword(c: Canvas, col) -> None:
    """Sayap pelindung melebar dan gemuk pommel."""
    c.poly([(2, 11), (7, 9), (7, 11), (2, 13)], col["s1"])
    c.poly([(13, 11), (8, 9), (8, 11), (13, 13)], col["s1"])
    c.poly([(4, 11), (7, 10), (7, 11)], col["acc"])
    c.poly([(11, 11), (8, 10), (8, 11)], col["acc"])
    c.ellipse(7.5, 14.5, 2.4, 1.6, col["m1"])
    c.dot(7.5, 14.5, col["s1"], 2)


def steel_spear(c: Canvas, col) -> None:
    """Pita panjang melayang dan cincin pengikat batang."""
    c.poly([(6, 10), (9, 11), (7, 13), (4, 12)], col["s1"])
    c.poly([(8, 8), (11, 9), (9, 11), (6, 10)], col["acc"])
    c.ellipse(4.0, 12.0, 2.2, 2.2, col["m1"], fill=False)
    c.px(4, 12, col["s1"])


def steel_fang(c: Canvas, col) -> None:
    """Sarung busur melingkar mengelilingi tiga cakar."""
    c.arc(9.5, 8.0, 7.5, 7.5, math.pi * 0.15, math.pi * 0.95, col["s2"], 2)
    c.px(2, 12, col["acc"])
    c.px(3, 13, col["acc"])


def steel_fan(c: Canvas, col) -> None:
    """Rim luar kipas yang melengkung penuh."""
    c.arc(7.5, 14.5, 11.5, 12.0, math.pi * 0.72, math.pi * 1.28, col["acc"], 1)
    c.poly([(7.5, 14.5), (0, 6), (2, 3), (7.5, 8)], col["s2"])
    c.px(1, 5, col["s1"])


def steel_seal(c: Canvas, col) -> None:
    """Cincin jin yang melayang di atas segel."""
    c.ellipse(8.0, 1.0, 4.0, 1.4, col["acc"], fill=False)
    c.px(8, 0, col["s1"])
    c.rect(2, 11, 3, 13, col["s1"])


def steel_bow(c: Canvas, col) -> None:
    """Sayap bulu ekor dan senar ganda."""
    c.poly([(10, 4), (15, 0), (13, 6), (9, 7)], col["acc"])
    c.poly([(10, 12), (15, 16), (13, 10), (9, 9)], col["acc"])
    c.arc(13.0, 8.0, 8.0, 7.0, math.pi * 0.66, math.pi * 1.34, col["s2"], 1)
    c.px(14, 2, col["s1"])


def steel_claw(c: Canvas, col) -> None:
    """Gelang buku jari yang menyatukan pangkal tiga bilah."""
    c.rect(0, 0, 4, 4, col["s2"])
    c.rect(1, 1, 3, 3, col["s1"])
    c.px(2, 2, col["acc"])
    c.px(11, 8, col["acc"])


def steel_staff(c: Canvas, col) -> None:
    """Pita yang melilit batang dan bonggol tulang membesar."""
    c.poly([(9, 9), (13, 10), (12, 12), (9, 11)], col["s1"])
    c.poly([(10, 12), (14, 13), (13, 15), (10, 14)], col["acc"])
    c.ellipse(10.5, 3.0, 3.8, 3.0, col["s1"])
    c.ellipse(10.5, 3.4, 2.0, 1.6, col["m1"])


def steel_dagger(c: Canvas, col) -> None:
    """Pita flowsan yang mengikuti bentuk bilah."""
    c.poly([(7, 6), (11, 4), (12, 6), (8, 8)], col["s1"])
    c.poly([(6, 12), (10, 13), (9, 15), (5, 14)], col["acc"])
    c.px(11, 5, col["s1"])


def steel_hammer(c: Canvas, col) -> None:
    """Sayap kilat pada sisi kepala palu."""
    c.poly([(1, 0), (5, 3), (1, 6)], col["acc"])
    c.poly([(14, 2), (12, 5), (15, 6)], col["acc"])
    c.px(3, 3, col["s1"])
    c.px(13, 4, col["s1"])


STEEL_ORNAMENT = {
    "sword": steel_sword,
    "spear": steel_spear,
    "fang": steel_fang,
    "fan": steel_fan,
    "seal": steel_seal,
    "bow": steel_bow,
    "claw": steel_claw,
    "staff": steel_staff,
    "dagger": steel_dagger,
    "hammer": steel_hammer,
}


# ------------------------------------------------------------------ bahan

def draw_spirit_iron_ingot(c: Canvas) -> None:
    """Ingot Besi Spirit: parallelogram tempa dengan muka atas terang."""
    metal, dark, mark = rgba("#B4BEC9"), rgba("#6E7885"), rgba("#E8EDF2")
    c.poly([(1, 9), (5, 5), (13, 5), (15, 8), (11, 11), (4, 11)], metal)
    c.poly([(5, 5), (13, 5), (13, 6), (4, 6)], mark)
    c.poly([(1, 9), (4, 6), (13, 6), (15, 8), (11, 11), (4, 11)], dark)
    c.poly([(2, 9), (5, 6), (12, 6), (14, 8), (11, 10), (5, 10)], metal)
    c.poly([(5, 6), (12, 6), (12, 7), (4, 7)], mark)
    c.rect(7, 8, 9, 9, dark)
    c.px(8, 8, mark)


def draw_spirit_steel_ingot(c: Canvas) -> None:
    """Ingot Baja Spirit: billet heksagon pendek dengan pita tempa menyilang."""
    metal, dark, mark = rgba("#8E97B4"), rgba("#4A5168"), rgba("#C6CEEA")
    c.poly([(7, 6), (11, 8), (13, 11), (10, 14), (5, 14), (3, 11), (5, 8)], metal)
    c.poly([(5, 8), (7, 6), (11, 8), (6, 10)], mark)
    c.line(5, 12, 11, 12, dark, 1)
    c.line(8, 7, 8, 13, dark, 1)
    c.px(8, 10, mark)


def draw_deep_silver_ingot(c: Canvas) -> None:
    """Ingot Perak Dalam: gulungan perak melingkar dengan inti cyan."""
    metal, dark, core = rgba("#DDE7F5"), rgba("#55657A"), rgba("#7FE8E0")
    c.ellipse(7.5, 8.0, 5.4, 5.4, metal, fill=False)
    c.ellipse(7.5, 8.0, 3.0, 3.0, metal, fill=False)
    c.arc(7.5, 8.0, 4.2, 4.2, math.pi * 0.05, math.pi * 0.95, dark, 1)
    c.arc(7.5, 8.0, 1.8, 1.8, math.pi * 1.05, math.pi * 1.95, dark, 1)
    c.dot(7.5, 8.0, core, 2)
    c.dot(7.5, 8.0, rgba("#EAFBFF"), 1)
    c.px(13, 12, core)


def draw_soul_crystal_shard(c: Canvas) -> None:
    """Serpihan Kristal Soul: kristal bersudut melayang di kanan atas."""
    body, deep, light = rgba("#6FC7E8"), rgba("#9A6FD6"), rgba("#EAFBFF")
    c.poly([(11, 0), (15, 4), (13, 11), (9, 9), (8, 3)], body)
    c.poly([(11, 0), (15, 4), (11, 6)], light)
    c.poly([(8, 3), (11, 6), (9, 9)], deep)
    c.line(11, 1, 12, 9, light, 1)
    c.poly([(3, 2), (5, 1), (4, 4)], light)
    c.poly([(1, 9), (3, 8), (2, 11)], deep)


# ------------------------------------------------------------------ consumable

def draw_spirit_dew_leaf(c: Canvas) -> None:
    """Daun Embun Spirit: daun lebar memanjang dengan tetes embun."""
    leaf, vein, dew = rgba("#4FB8A8"), rgba("#1F6B5E"), rgba("#CFEFE9")
    c.poly([(0, 8), (3, 3), (9, 0), (12, 4), (8, 9), (3, 11)], leaf)
    c.line(1, 10, 10, 3, vein, 1)
    c.line(4, 6, 7, 8, vein, 1)
    c.dot(6, 5, dew, 2)
    c.dot(9, 6, dew, 1)
    c.px(2, 9, vein)


def draw_spirit_moss_sprout(c: Canvas) -> None:
    """Tunas Lumut Spirit: rumpun lumut tak beraturan dengan tunas menjulang."""
    moss, deep, tip = rgba("#6FA83C"), rgba("#2F5E24"), rgba("#BFD98A")
    c.ellipse(6.5, 12.0, 4.2, 3.2, moss)
    c.ellipse(4.0, 13.5, 2.2, 1.6, moss)
    c.ellipse(9.0, 13.8, 1.8, 1.4, moss)
    c.ellipse(6.0, 11.4, 2.4, 1.8, tip)
    c.line(6, 9, 6, 3, deep, 1)
    c.poly([(6, 1), (8, 5), (4, 5)], tip)
    c.line(10, 10, 13, 6, deep, 1)
    c.poly([(14, 4), (14, 8), (11, 7)], tip)
    c.px(3, 13, deep)


def draw_spirit_ember_leaf(c: Canvas) -> None:
    """Daun Bara Spirit: daun runcing dengan ujung api menyala."""
    leaf, deep, fire = rgba("#E2622A"), rgba("#7A3320"), rgba("#F6C46A")
    c.poly([(9, 15), (7, 10), (11, 6), (15, 7), (14, 12), (12, 15)], leaf)
    c.line(10, 15, 13, 8, deep, 1)
    c.poly([(11, 6), (9, 2), (12, 0), (14, 3), (13, 7)], fire)
    c.poly([(12, 4), (12, 1), (14, 3)], rgba("#FFE9A8"))
    c.px(8, 11, deep)
    c.px(9, 14, deep)


def draw_cultivation_pill(c: Canvas) -> None:
    """Pil Kultivasi: pil bulat mengambang dengan pusaran emas."""
    body, deep, glow = rgba("#E0A93B"), rgba("#A2701B"), rgba("#FFF0C0")
    c.ellipse(6.0, 6.0, 3.6, 3.6, body)
    c.ellipse(6.0, 6.0, 2.6, 2.6, glow)
    c.arc(6.0, 6.0, 1.8, 1.8, math.pi * 0.2, math.pi * 1.8, deep, 1)
    c.dot(6, 6, deep, 1)
    c.px(4, 4, rgba("#FFF6D8"))
    c.px(11, 10, rgba("#FFF6D8"))
    c.px(12, 3, rgba("#FFF6D8"))


def draw_swift_pill(c: Canvas) -> None:
    """Pil Kilat: kapsul miring dengan garis diagonal."""
    body, stripe, shine = rgba("#8A5FD6"), rgba("#C9D2DE"), rgba("#F0E9FF")
    c.poly([(1, 12), (3, 9), (9, 5), (12, 7), (10, 11), (4, 14)], body)
    c.poly([(3, 11), (4, 10), (9, 7), (10, 8), (5, 12)], stripe)
    c.px(2, 12, shine)
    c.px(10, 7, shine)


V12A_EXTRAS = {
    "spirit_iron_ingot": draw_spirit_iron_ingot,
    "spirit_steel_ingot": draw_spirit_steel_ingot,
    "deep_silver_ingot": draw_deep_silver_ingot,
    "soul_crystal_shard": draw_soul_crystal_shard,
    "spirit_dew_leaf": draw_spirit_dew_leaf,
    "spirit_moss_sprout": draw_spirit_moss_sprout,
    "spirit_ember_leaf": draw_spirit_ember_leaf,
    "cultivation_pill": draw_cultivation_pill,
    "swift_pill": draw_swift_pill,
}

# Nama bahan dan consumable dalam dua bahasa.
V12A_EXTRAS_NAME = {
    "spirit_iron_ingot": ("Spirit Iron Ingot", "Ingot Besi Spirit"),
    "spirit_steel_ingot": ("Spirit Steel Ingot", "Ingot Baja Spirit"),
    "deep_silver_ingot": ("Deep Silver Ingot", "Ingot Perak Dalam"),
    "soul_crystal_shard": ("Soul Crystal Shard", "Serpihan Kristal Soul"),
    "spirit_dew_leaf": ("Spirit Dew Leaf", "Daun Embun Spirit"),
    "spirit_moss_sprout": ("Spirit Moss Sprout", "Tunas Lumut Spirit"),
    "spirit_ember_leaf": ("Spirit Ember Leaf", "Daun Bara Spirit"),
    "cultivation_pill": ("Cultivation Pill", "Pil Kultivasi"),
    "swift_pill": ("Swift Pill", "Pil Kilat"),
}


def build_v12a_items():
    """Kembalikan dict id item -> Image untuk 39 item baru v1.2A."""
    items = {}
    for tier in TIER_ORDER:
        for soul in SOUL_TOOL:
            tool_type = SOUL_TOOL[soul][0]
            items[f"{tier}_{soul}_{tool_type}"] = build_tool(tier, soul)
    for item_id, builder in V12A_EXTRAS.items():
        canvas = Canvas()
        builder(canvas)
        items[item_id] = canvas.image
    return items


def v12a_palette(item_id: str):
    """Palet tiga warna inti yang dideklarasikan untuk sebuah item."""
    if item_id in V12A_EXTRAS_NAME:
        if item_id == "spirit_iron_ingot":
            return ("#B4BEC9", "#6E7885", "#E8EDF2")
        if item_id == "spirit_steel_ingot":
            return ("#8E97B4", "#4A5168", "#C6CEEA")
        if item_id == "deep_silver_ingot":
            return ("#DDE7F5", "#55657A", "#7FE8E0")
        if item_id == "soul_crystal_shard":
            return ("#6FC7E8", "#9A6FD6", "#EAFBFF")
        if item_id == "spirit_dew_leaf":
            return ("#4FB8A8", "#1F6B5E", "#CFEFE9")
        if item_id == "spirit_moss_sprout":
            return ("#6FA83C", "#2F5E24", "#BFD98A")
        if item_id == "spirit_ember_leaf":
            return ("#E2622A", "#7A3320", "#F6C46A")
        if item_id == "cultivation_pill":
            return ("#E0A93B", "#A2701B", "#FFF0C0")
        return ("#8A5FD6", "#C9D2DE", "#F0E9FF")
    tier = next(t for t in TIER_ORDER if item_id.startswith(t + "_"))
    rest = item_id[len(tier) + 1:]
    soul = next(s for s in SOUL_TOOL if rest.startswith(s + "_"))
    soul_light, soul_dark, _ = SOUL_PALETTE[soul]
    metal_light, metal_dark = TIER_METAL[tier]
    return (soul_light, soul_dark, metal_light)


# ------------------------------------------------------------------ contact sheet v1.2A

def build_v12a_contact_sheet(items: dict) -> str:
    """Lembar kontak semua texture baru v1.2A, disimpan ke docs/."""
    from PIL import ImageDraw

    zoom = 6
    columns = 8
    cell = 16 * zoom
    label_h = 14
    order = list(items.keys())
    rows = (len(order) + columns - 1) // columns
    sheet = Image.new("RGBA", (columns * cell, rows * (cell + label_h)), (26, 28, 34, 255))
    draw = ImageDraw.Draw(sheet)
    for index, item_id in enumerate(order):
        sprite = items[item_id].resize((cell, cell), Image.NEAREST)
        x = (index % columns) * cell
        y = (index // columns) * (cell + label_h)
        sheet.alpha_composite(sprite, (x, y))
        draw.rectangle([x, y, x + cell - 1, y + cell - 1], outline=(58, 62, 72, 255))
        draw.text((x + 3, y + cell + 2), v12a_short_label(item_id), fill=(196, 200, 210, 255))
    path = os.path.join(ROOT, "docs", "v1.2a-contact-sheet.png")
    os.makedirs(os.path.dirname(path), exist_ok=True)
    sheet.save(path)
    print(f"  wrote {os.path.relpath(path, ROOT)}")
    return path


def v12a_short_label(item_id: str) -> str:
    """Label ringkas pada lembar kontak, daftar lengkap ada di file markdown."""
    if item_id in V12A_EXTRAS_NAME:
        return item_id[:16]
    tier = next(t for t in TIER_ORDER if item_id.startswith(t + "_"))
    rest = item_id[len(tier) + 1:]
    soul = next(s for s in SOUL_TOOL if rest.startswith(s + "_"))
    tier_mark = {"spirit_iron": "I", "spirit_steel": "S", "deep_silver": "D"}[tier]
    return f"{SOUL_TOOL[soul][3]}-{tier_mark}"


def write_v12a_manifest(items: dict) -> None:
    """Catat metadata tiap item baru supaya validator bisa memeriksa keunikan."""
    entries = []
    for item_id in items:
        if item_id in V12A_EXTRAS_NAME:
            kind = "material" if item_id.endswith(("ingot", "shard")) else "consumable"
            entries.append({
                "id": item_id,
                "kind": kind,
                "tier": None,
                "soul": None,
                "archetype": None,
                "palette": list(v12a_palette(item_id)),
            })
            continue
        tier = next(t for t in TIER_ORDER if item_id.startswith(t + "_"))
        rest = item_id[len(tier) + 1:]
        soul = next(s for s in SOUL_TOOL if rest.startswith(s + "_"))
        entries.append({
            "id": item_id,
            "kind": "tool",
            "tier": tier,
            "soul": soul,
            "archetype": SOUL_TOOL[soul][0],
            "palette": list(v12a_palette(item_id)),
        })
    payload = {
        "design_read": "kumpulan alat dan bahan untuk pemain yang sudah familiar dengan martial soul, "
                       "gaya soul-tinted metal, dial ENERGY 2 / RHYTHM 3 / MOTION 1",
        "items": entries,
    }
    path = os.path.join(ROOT, "docs", "v1.2a-asset-manifest.json")
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as handle:
        json.dump(payload, handle, indent=2, ensure_ascii=False)
        handle.write("\n")
    print(f"  wrote {os.path.relpath(path, ROOT)}")

    lines = [
        "# Lembar kontak aset v1.2A",
        "",
        "Gambar: `docs/v1.2a-contact-sheet.png` (39 texture baru, zoom 6, 8 kolom).",
        "Label di gambar memakai kependekan: `SOUL-MUTU` dengan I = Besi Spirit,",
        "S = Baja Spirit, D = Perak Dalam.",
        "",
        "| No | Item | Jenis | Arketipe | Palet inti |",
        "|---|---|---|---|---|",
    ]
    for index, entry in enumerate(entries, start=1):
        palette = ", ".join(f"`{color}`" for color in entry["palette"])
        archetype = entry["archetype"] or entry["kind"]
        lines.append(f"| {index:02d} | `{entry['id']}` | {entry['kind']} | {archetype} | {palette} |")
    lines.append("")
    md_path = os.path.join(ROOT, "docs", "v1.2a-contact-sheet.md")
    with open(md_path, "w", encoding="utf-8") as handle:
        handle.write("\n".join(lines))
    print(f"  wrote {os.path.relpath(md_path, ROOT)}")



# ================================================================ v1.2B

# Tiga asset baru: Spirit Ring Emas, Station Alchemy, Pil Fondasi.
# Aturan keras v1.2B: setiap asset punya palet inti sendiri dan siluet yang
# tidak kembar dengan asset lain (dicek validate_assets.py).


def draw_gold_ring(c: Canvas) -> None:
    """Spirit Ring Emas: pita tebal dengan mahkota tiga puncak."""
    body, light, dark = rgba("#E8B84B"), rgba("#FFE9A8", 235), rgba("#9A6C12", 170)
    cx, cy = 7.5, 9.2
    rx, ry = 5.4, 3.4
    c.ellipse(cx, cy, rx, ry, body, fill=False)
    c.ellipse(cx, cy, rx - 0.9, ry - 0.9, light, fill=False)
    c.ellipse(cx, cy, rx - 2.2, ry - 2.2, body, fill=False)
    # mahkota tiga puncak di atas pita
    c.poly([(3.4, 5.6), (4.8, 2.0), (6.4, 5.0)], light)
    c.poly([(6.4, 4.6), (7.9, 0.9), (9.4, 4.4)], body)
    c.poly([(9.4, 5.2), (10.8, 2.2), (12.2, 5.8)], light)
    # kilau tepi
    c.arc(cx, cy, rx - 0.5, ry - 0.5, math.pi * 0.95, math.pi * 1.35, light, 1)
    c.arc(cx, cy, rx + 0.4, ry + 0.4, math.pi * 0.05, math.pi * 0.3, dark, 1)
    c.px(1, 11, light)
    c.px(14, 7, light)


def draw_alchemy_station(c: Canvas) -> None:
    """Texture blok Station Alchemy: tungku batu dengan kawah jade dan trim emas."""
    stone, stone_dark, stone_light = rgba("#4A5058"), rgba("#2E333A"), rgba("#6E7680")
    jade, jade_dark = rgba("#3FC7A3"), rgba("#1B4A41")
    gold = rgba("#E8B84B")
    c.rect(0, 0, 15, 15, stone_dark)
    c.rect(0, 0, 15, 1, stone)
    c.rect(0, 14, 15, 15, stone)
    c.rect(0, 0, 1, 15, stone)
    c.rect(14, 0, 15, 15, stone)
    # badan tungku
    c.rect(2, 5, 13, 13, stone)
    c.rect(2, 5, 13, 6, stone_light)
    # kawah
    c.ellipse(7.5, 8.0, 4.4, 2.4, jade_dark)
    c.ellipse(7.5, 7.6, 3.4, 1.6, jade)
    # gagang
    c.rect(0, 9, 2, 10, stone_light)
    c.rect(13, 9, 15, 10, stone_light)
    # kaki
    c.rect(3, 13, 5, 15, stone_dark)
    c.rect(10, 13, 12, 15, stone_dark)
    # trim emas
    c.line(3, 12, 12, 12, gold)


def draw_alchemy_station_icon(c: Canvas) -> None:
    """Ikon item Station Alchemy: tungku tinggi dengan kawah jade."""
    stone, stone_dark, stone_light = rgba("#4A5058"), rgba("#2E333A"), rgba("#6E7680")
    jade, jade_dark = rgba("#3FC7A3"), rgba("#1B4A41")
    gold = rgba("#E8B84B")
    c.rect(6, 1, 9, 2, stone_light)
    c.rect(7, 2, 8, 7, stone)
    c.ellipse(7.5, 8.0, 4.0, 2.4, stone)
    c.ellipse(7.5, 7.4, 3.2, 1.7, jade_dark)
    c.ellipse(7.5, 7.2, 2.2, 1.1, jade)
    c.rect(3, 11, 4, 14, stone_dark)
    c.rect(11, 11, 12, 14, stone_dark)
    c.line(4, 10, 11, 10, gold)


def draw_foundation_pill(c: Canvas) -> None:
    """Pil Fondasi: busur batu yang memEGANG permata jade."""
    rim, body, core = rgba("#D8C48A"), rgba("#8FBF6B"), rgba("#EAF7C8")
    c.arc(7.5, 8.5, 5.0, 5.0, math.pi * 0.25, math.pi * 1.60, rim, 3)
    c.arc(7.5, 8.5, 5.0, 5.0, math.pi * 0.45, math.pi * 1.30, body, 1)
    c.poly([(9.5, 1.0), (12.5, 4.0), (9.5, 7.0), (6.5, 4.0)], core)


V12B_ITEMS = {
    "spirit_ring_1000000": draw_gold_ring,
    "alchemy_station": draw_alchemy_station_icon,
    "foundation_pill": draw_foundation_pill,
}

# Texture blok (dipakai saat blok diletakkan di dunia, bukan ikon inventory).
V12B_BLOCKS = {
    "alchemy_station": draw_alchemy_station,
}

V12B_PALETTE = {
    "spirit_ring_1000000": ("#E8B84B", "#FFE9A8", "#9A6C12"),
    "alchemy_station": ("#4A5058", "#3FC7A3", "#E8B84B"),
    "foundation_pill": ("#8FBF6B", "#D8C48A", "#EAF7C8"),
}

V12B_KIND = {
    "spirit_ring_1000000": "ring",
    "alchemy_station": "block",
    "foundation_pill": "consumable",
}

TEX_BLOCK = os.path.join(ASSETS, "textures", "block")
MODEL_BLOCK = os.path.join(ASSETS, "models", "block")
BLOCKSTATE = os.path.join(ASSETS, "blockstates")


def write_block_assets(block_id: str) -> None:
    """Blok butuh texture, model kubus, blockstate, dan model item."""
    os.makedirs(TEX_BLOCK, exist_ok=True)
    os.makedirs(MODEL_BLOCK, exist_ok=True)
    os.makedirs(BLOCKSTATE, exist_ok=True)
    os.makedirs(MODEL_ITEM, exist_ok=True)
    block_model = {
        "parent": "minecraft:block/cube_all",
        "textures": {"all": f"soulland:block/{block_id}"},
    }
    with open(os.path.join(MODEL_BLOCK, f"{block_id}.json"), "w", encoding="utf-8") as handle:
        json.dump(block_model, handle, indent=2)
        handle.write("\n")
    blockstate = {"variants": {"": {"model": f"soulland:block/{block_id}"}}}
    with open(os.path.join(BLOCKSTATE, f"{block_id}.json"), "w", encoding="utf-8") as handle:
        json.dump(blockstate, handle, indent=2)
        handle.write("\n")
    item_model = {"parent": "item/generated", "textures": {"layer0": f"soulland:item/{block_id}"}}
    with open(os.path.join(MODEL_ITEM, f"{block_id}.json"), "w", encoding="utf-8") as handle:
        json.dump(item_model, handle, indent=2)
        handle.write("\n")
    print(f"  wrote assets block {block_id}")


def build_v12b_items() -> dict:
    """Kembalikan dict id asset -> Image untuk 3 asset baru v1.2B."""
    items = {}
    for item_id, builder in V12B_ITEMS.items():
        canvas = Canvas()
        builder(canvas)
        items[item_id] = canvas.image
    return items


def build_v12b_contact_sheet(items: dict) -> str:
    """Lembar kontak asset baru v1.2B, disimpan ke docs/."""
    from PIL import ImageDraw

    zoom = 10
    cell = 16 * zoom
    label_h = 20
    order = list(items.keys())
    sheet = Image.new("RGBA", (len(order) * cell, cell + label_h), (26, 28, 34, 255))
    draw = ImageDraw.Draw(sheet)
    for index, item_id in enumerate(order):
        sprite = items[item_id].resize((cell, cell), Image.NEAREST)
        x = index * cell
        sheet.alpha_composite(sprite, (x, 0))
        draw.rectangle([x, 0, x + cell - 1, cell - 1], outline=(58, 62, 72, 255))
        draw.text((x + 4, cell + 4), item_id[:22], fill=(196, 200, 210, 255))
    path = os.path.join(ROOT, "docs", "v1.2b-contact-sheet.png")
    os.makedirs(os.path.dirname(path), exist_ok=True)
    sheet.save(path)
    print(f"  wrote {os.path.relpath(path, ROOT)}")
    return path


def write_v12b_manifest(items: dict) -> None:
    """Catat metadata asset baru v1.2B supaya validator bisa memeriksanya."""
    entries = []
    for item_id in items:
        entries.append({
            "id": item_id,
            "kind": V12B_KIND[item_id],
            "palette": list(V12B_PALETTE[item_id]),
        })
    payload = {
        "design_read": "Tiga asset baru v1.2B: grade emas, station alchemy, pil fondasi. "
                       "Palet inti tiap asset unik dan siluetnya tidak kembar.",
        "items": entries,
    }
    path = os.path.join(ROOT, "docs", "v1.2b-asset-manifest.json")
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as handle:
        json.dump(payload, handle, indent=2, ensure_ascii=False)
        handle.write("\n")
    print(f"  wrote {os.path.relpath(path, ROOT)}")

    lines = [
        "# Lembar kontak aset v1.2B",
        "",
        "Gambar: `docs/v1.2b-contact-sheet.png` (3 asset baru, zoom 10).",
        "",
        "| No | Asset | Jenis | Palet inti |",
        "|---|---|---|---|",
    ]
    for index, entry in enumerate(entries, start=1):
        palette = ", ".join(f"`{color}`" for color in entry["palette"])
        lines.append(f"| {index:02d} | `{entry['id']}` | {entry['kind']} | {palette} |")
    lines.append("")
    md_path = os.path.join(ROOT, "docs", "v1.2b-contact-sheet.md")
    with open(md_path, "w", encoding="utf-8") as handle:
        handle.write("\n".join(lines))
    print(f"  wrote {os.path.relpath(md_path, ROOT)}")


# ================================================================ v1.3

# Tulang spirit: 5 mutu umur x 7 slot tubuh = 35 item.
# Aturan keras: siluet tiap slot harus berbeda sekilas (bukan cermin), dan
# tiap tier punya bahan tulang berbeda, bukan hanya warna. Palet inti tiap
# item unik supaya tidak ada dua item yang terbaca sebagai benda yang sama.

# Warna tier mengikuti grade spirit ring, sama persis dengan SpiritBoneTier
# di kode Java, supaya pemain bisa membaca "tulang ini sepadan dengan ring ini".
BONE_TIERS = [
    ("y100", "#E4E8EA", "#F7FAFB"),
    ("y1000", "#F0C040", "#FFE9A8"),
    ("y10000", "#B36BE0", "#E2C4FF"),
    ("y100000", "#D2453C", "#FFA79E"),
    ("y1000000", "#E8C46A", "#FFF0C0"),
]

# Aksen slot: warna小结 satu-satunya per slot, dipakai pada sendi atau inti
# benda. Tujuh warna berbeda supaya 35 item punya 35 palet inti berbeda.
BONE_SLOTS = [
    ("skull", "#7FD4E8"),
    ("torso", "#E0603F"),
    ("left_arm", "#8FBF6B"),
    ("right_arm", "#B98BD9"),
    ("left_leg", "#D9A441"),
    ("right_leg", "#6B8FD9"),
    ("external", "#6EE8C8"),
]


def bone_shape_skull(c: Canvas, body) -> None:
    """Tengkorak: kubah cranium berongga, alis tebal, dan rahang kecil.

    Kubah digambar sebagai garis luar, bukan blok penuh, supaya tengkorak
    langsung terbaca berlubang dan tidak mirip rongga dada yang padat.
    """
    c.ellipse(7.5, 5.0, 4.4, 3.6, body, fill=False)
    c.rect(4, 1, 11, 2, body)
    c.rect(6, 6, 9, 8, body)
    c.rect(7, 8, 8, 10, body)


def bone_shape_torso(c: Canvas, body) -> None:
    """Rongga dada: tulang belakang tegak dengan dua rusuk melintang."""
    c.rect(7, 1, 8, 14, body)
    c.rect(5, 3, 10, 4, body)
    c.rect(4, 6, 11, 7, body)
    c.rect(5, 9, 10, 10, body)
    c.rect(6, 12, 9, 13, body)


def bone_shape_left_arm(c: Canvas, body) -> None:
    """Lengan kiri: tulang miring dari bahu kiri atas ke siku kanan bawah."""
    c.dot(3.5, 2.5, body, 4)
    c.line(4.5, 3.5, 11.0, 11.5, body, 2)
    c.line(10.0, 11.0, 8.5, 14.5, body, 2)
    c.line(11.5, 11.5, 13.5, 14.0, body, 2)


def bone_shape_right_arm(c: Canvas, body) -> None:
    """Lengan kanan: batang tegak dengan benjolan siku di sisi kanan."""
    c.rect(6, 2, 7, 12, body)
    c.dot(10.0, 7.0, body, 4)
    c.line(6.0, 1.0, 5.0, 0.0, body, 1)
    c.line(7.0, 1.0, 8.0, 0.0, body, 1)
    c.line(6.0, 12.0, 6.0, 14.5, body, 2)
    c.line(7.0, 12.0, 9.0, 13.5, body, 1)


def bone_shape_left_leg(c: Canvas, body) -> None:
    """Kaki kiri: femur tegak dengan kepala bulat di atas dan lutut di kiri bawah."""
    c.dot(9.0, 2.5, body, 3)
    c.rect(7, 3, 8, 10, body)
    c.dot(5.5, 11.5, body, 5)
    c.line(8.0, 10.0, 9.0, 12.0, body, 1)


def bone_shape_right_leg(c: Canvas, body) -> None:
    """Kaki kanan: tibia bertapak lebar di atas, bertaji ke kanan, dan bercabang bawah."""
    c.rect(4, 1, 9, 2, body)
    c.rect(6, 3, 7, 11, body)
    c.poly([(8, 5), (13, 8), (8, 10)], body)
    c.line(5.0, 11.0, 4.0, 14.5, body, 2)
    c.line(7.0, 11.0, 8.0, 14.5, body, 2)


def bone_shape_external(c: Canvas, body) -> None:
    """Tulang Eksternal: gumpalan kristal, sengaja bukan struktur tulang."""
    c.poly([(8, 0), (11, 6), (8, 15), (5, 6)], body)
    c.poly([(2, 8), (4, 12), (5, 9)], body)
    c.poly([(12, 6), (14, 10), (11, 9)], body)


BONE_SHAPES = {
    "skull": bone_shape_skull,
    "torso": bone_shape_torso,
    "left_arm": bone_shape_left_arm,
    "right_arm": bone_shape_right_arm,
    "left_leg": bone_shape_left_leg,
    "right_leg": bone_shape_right_leg,
    "external": bone_shape_external,
}

# Titik aksen per slot: letak sendi, rongga mata, atau inti kristal.
BONE_ACCENT_POINTS = {
    "skull": [(6, 6), (7, 6), (8, 6), (9, 6), (7, 9), (8, 9)],
    "torso": [(7, 3), (8, 3), (4, 6), (11, 6), (7, 12), (8, 12)],
    "left_arm": [(4, 4), (5, 5), (6, 6), (7, 7), (10, 12)],
    "right_arm": [(9, 7), (10, 7), (6, 4), (6, 9), (6, 13)],
    "left_leg": [(9, 2), (7, 5), (7, 8), (6, 12), (7, 12)],
    "right_leg": [(8, 8), (9, 8), (6, 4), (6, 8), (4, 13), (7, 13)],
    "external": [(8, 3), (8, 4), (7, 8), (9, 8), (8, 12)],
}

# Piksel aksen tambahan untuk rongga mata tengkorak, yang harus digambar
# sebagai blok sendiri karena berada di dalam kubah yang berlubang.
BONE_ACCENT_BLOCKS = {
    "skull": [(5, 4), (6, 4), (9, 4), (10, 4)],
}


def bone_material(c: Canvas, tier_index: int, body, light, shadow) -> None:
    """Beri bahan tulang sesuai umur, hanya pada piksel yang sudah terisi.

    Tiap umur punya bahan berbeda, jadi dua tier tidak hanya beda warna:
    youngest mulus, kedua berurat, ketiga retak, keempat hangus, kelima berukir.
    """
    for y in range(c.size):
        for x in range(c.size):
            if c.pixels[x, y][3] == 0:
                continue
            if tier_index == 0:
                # Muda: permukaan mulus, sisi kiri atas lebih terang.
                if x + y <= 9:
                    c.pixels[x, y] = light
                elif x + y >= 20:
                    c.pixels[x, y] = shadow
            elif tier_index == 1:
                # Seribu: tulang hangat dengan uratangers.
                if (x * 3 + y * 5) % 7 == 0:
                    c.pixels[x, y] = light
                elif x + y <= 8:
                    c.pixels[x, y] = light
            elif tier_index == 2:
                # Sepuluh ribu: retak yang memancarkan cahaya.
                if (x + y * 2) % 5 == 0:
                    c.pixels[x, y] = light
                elif (x * 2 + y) % 6 == 0:
                    c.pixels[x, y] = shadow
            elif tier_index == 3:
                # Seratus ribu: hangus, bara di bagian bawah.
                if y >= 10:
                    c.pixels[x, y] = shadow
                elif (x * 7 + y * 3) % 11 == 0:
                    c.pixels[x, y] = light
            else:
                # Satu juta: ukiran tipis dengan sisi atas berkilau.
                if (x - y) % 4 == 0:
                    c.pixels[x, y] = shadow
                elif x + y <= 8:
                    c.pixels[x, y] = light


def build_bone_image(tier_index: int, tier_colors, slot_id: str, accent_hex: str) -> Image.Image:
    """Gambar satu item tulang: siluet slot, bahan tier, lalu aksen slot."""
    body_hex, light_hex = tier_colors
    body = rgba(body_hex)
    light = rgba(light_hex)
    shadow = rgba(body_hex, 190)
    dark = rgba(body_hex, 120)
    accent = rgba(accent_hex)

    canvas = Canvas()
    BONE_SHAPES[slot_id](canvas, body)

    # Bahan tier ditulis dulu, bayangan dipakai untuk sisi bawah.
    bone_material(canvas, tier_index, body, light, dark)

    # Aksen slot menandai sendi atau rongga mata.
    for x, y in BONE_ACCENT_POINTS[slot_id]:
        if canvas.pixels[x, y][3] > 0:
            canvas.pixels[x, y] = accent
    for x, y in BONE_ACCENT_BLOCKS.get(slot_id, ()):
        canvas.px(x, y, accent)

    # Sisi bawah diberi bayangan supaya bentuk terbacavolume, bukan rata.
    for y in range(canvas.size):
        for x in range(canvas.size):
            if canvas.pixels[x, y][3] == 0:
                continue
            if y >= 12 and canvas.pixels[x, y] == body:
                canvas.pixels[x, y] = shadow
    return canvas.image


def build_v13_items() -> dict:
    """Kembalikan dict id item -> Image untuk 35 item tulang spirit."""
    items = {}
    for tier_index, (tier_id, body_hex, light_hex) in enumerate(BONE_TIERS):
        for slot_id, accent_hex in BONE_SLOTS:
            item_id = f"spirit_bone_{tier_id}_{slot_id}"
            items[item_id] = build_bone_image(tier_index, (body_hex, light_hex), slot_id, accent_hex)
    return items


def v13_palette(item_id: str):
    """Palet tiga warna inti: warna tier, warna terang tier, aksen slot."""
    for tier_id, body_hex, light_hex in BONE_TIERS:
        for slot_id, accent_hex in BONE_SLOTS:
            if item_id == f"spirit_bone_{tier_id}_{slot_id}":
                return (body_hex, light_hex, accent_hex)
    raise KeyError(item_id)


def build_v13_contact_sheet(items: dict) -> str:
    """Lembar kontak 35 ikon tulang, disusun 7 kolom x 5 baris."""
    from PIL import ImageDraw

    zoom = 6
    cell = 16 * zoom
    label_h = 16
    columns = 7
    order = list(items.keys())
    rows = (len(order) + columns - 1) // columns
    sheet = Image.new("RGBA", (columns * cell, rows * (cell + label_h)), (26, 28, 34, 255))
    draw = ImageDraw.Draw(sheet)
    for index, item_id in enumerate(order):
        sprite = items[item_id].resize((cell, cell), Image.NEAREST)
        x = (index % columns) * cell
        y = (index // columns) * (cell + label_h)
        sheet.alpha_composite(sprite, (x, y))
        draw.rectangle([x, y, x + cell - 1, y + cell - 1], outline=(58, 62, 72, 255))
        draw.text((x + 3, y + cell + 2), item_id.replace("spirit_bone_", "")[:16],
                  fill=(196, 200, 210, 255))
    path = os.path.join(ROOT, "docs", "v1.3-contact-sheet.png")
    os.makedirs(os.path.dirname(path), exist_ok=True)
    sheet.save(path)
    print(f"  wrote {os.path.relpath(path, ROOT)}")
    return path


def write_v13_manifest(items: dict) -> None:
    """Catat metadata 35 item tulang supaya validator bisa memeriksanya."""
    entries = []
    for item_id in items:
        tier_id, slot_id = item_id.replace("spirit_bone_", "").split("_", 1)
        entries.append({
            "id": item_id,
            "kind": "bone",
            "slot": slot_id,
            "tier": tier_id,
            "palette": list(v13_palette(item_id)),
        })
    payload = {
        "design_read": "Tulang spirit v1.3: siluet beda per slot tubuh, bahan beda per tier umur. "
                       "Tiap item punya palet inti sendiri dan siluetnya tidak kembar.",
        "items": entries,
    }
    path = os.path.join(ROOT, "docs", "v1.3-asset-manifest.json")
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as handle:
        json.dump(payload, handle, indent=2, ensure_ascii=False)
        handle.write("\n")
    print(f"  wrote {os.path.relpath(path, ROOT)}")

    lines = [
        "# Lembar kontak aset v1.3",
        "",
        "Gambar: `docs/v1.3-contact-sheet.png` (35 item tulang spirit, 7 kolom x 5 baris, zoom 6).",
        "",
        "| No | Item | Slot | Tier | Palet inti |",
        "|---|---|---|---|---|",
    ]
    for index, entry in enumerate(items, start=1):
        parts = entry.replace("spirit_bone_", "").split("_", 1)
        slot_id = parts[1]
        tier_id = parts[0]
        palette = ", ".join(f"`{color}`" for color in v13_palette(entry))
        lines.append(f"| {index:02d} | `{entry}` | {slot_id} | {tier_id} | {palette} |")
    lines.append("")
    md_path = os.path.join(ROOT, "docs", "v1.3-contact-sheet.md")
    with open(md_path, "w", encoding="utf-8") as handle:
        handle.write("\n".join(lines))
    print(f"  wrote {os.path.relpath(md_path, ROOT)}")


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

    print("Item v1.2A (39):")
    v12a_items = build_v12a_items()
    for item_id, image in v12a_items.items():
        write_png(image, os.path.join(TEX_ITEM, f"{item_id}.png"))
        write_model(item_id)

    build_v12a_contact_sheet(v12a_items)
    write_v12a_manifest(v12a_items)

    print("Asset v1.2B (3):")
    v12b_items = build_v12b_items()
    for item_id, image in v12b_items.items():
        write_png(image, os.path.join(TEX_ITEM, f"{item_id}.png"))
        write_model(item_id)
        if item_id in V12B_BLOCKS:
            block_canvas = Canvas()
            V12B_BLOCKS[item_id](block_canvas)
            write_png(block_canvas.image, os.path.join(TEX_BLOCK, f"{item_id}.png"))
            write_block_assets(item_id)
    build_v12b_contact_sheet(v12b_items)
    write_v12b_manifest(v12b_items)

    print("Item v1.3 Spirit Bone (35):")
    v13_items = build_v13_items()
    for item_id, image in v13_items.items():
        write_png(image, os.path.join(TEX_ITEM, f"{item_id}.png"))
        write_model(item_id)
    build_v13_contact_sheet(v13_items)
    write_v13_manifest(v13_items)
    print("Selesai.")


if __name__ == "__main__":
    main()
