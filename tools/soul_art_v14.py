#!/usr/bin/env python3
"""Gambar 33 texture manifestasi martial soul baru untuk katalog v1.4.

Setiap soul digambar sendiri, tidak dari satu template: siluet, palet, dan
motifnya berbeda. Aturan aset v1.2A tetap berlaku: tidak ada dua item dengan
siluet atau palet yang sama.

Modul ini hanya berisi gambar. Penulisan file PNG, model JSON, manifest, dan
lembar kontak dilakukan oleh `tools/generate_assets.py`.
"""
from __future__ import annotations

import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from generate_assets import Canvas, rgba  # noqa: E402

SIZE = 16


# ---------------------------------------------------------------- palet

# (gelap, tengah, terang) per soul. Tuple sorted dari setiap tiga warna ini
# harus berbeda dari semua item lain, dan itu diperiksa validator.
PALETTES = {
    "golden_dragon": ("#6B3A10", "#E8A33C", "#FFE9A8"),
    "abyssal_demon_dragon": ("#241436", "#7A3FC0", "#D8B4F0"),
    "abyssal_ice_demon_dragon": ("#123A52", "#4FA8D8", "#C8ECFF"),
    "ultimate_fire_dragon": ("#5A1408", "#E0522A", "#FFB44A"),
    "ultimate_ice_dragon": ("#1B3A56", "#8FC8E8", "#EAF8FF"),
    "thunder_elephant": ("#2A3060", "#6E8FE0", "#C4D8FF"),
    "golden_ant": ("#4A3A0C", "#C9A227", "#F0DC84"),
    "titan_giant_ape": ("#3A2A1C", "#9A7048", "#D8B88C"),
    "sea_devil_manatee": ("#123038", "#3C7A8C", "#8CC8CC"),
    "bright_wing_moth": ("#4A3A1E", "#E8D48C", "#FFF8DC"),
    "phoenix": ("#5C2410", "#F0B24A", "#FFE8B0"),
    "blue_silver_spear": ("#16386E", "#3E6FB8", "#A8D4F0"),
    "seven_kill_sword": ("#120E18", "#4A3050", "#B48CC8"),
    "giant_hammer": ("#2E2A22", "#7A6A58", "#C0B090"),
    "blood_lash": ("#3A0E14", "#8E2434", "#D4687A"),
    "frost_needle": ("#1E4256", "#9AD8E8", "#E8F8FF"),
    "thunder_spear": ("#23285E", "#6E8FE0", "#E0E8FF"),
    "spirit_sword": ("#3A4250", "#B8C4D4", "#F0F6FF"),
    "iron_anchor": ("#1E2428", "#5E6A6E", "#A8B2B6"),
    "bronze_mirror": ("#4A2E10", "#C08A4A", "#F0D49A"),
    "blue_silver_emperor": ("#14304E", "#2E5C9E", "#8FC0E8"),
    "iron_body": ("#242830", "#6E7480", "#B4BCC8"),
    "bone_shell": ("#6E6046", "#D6CBB0", "#F4ECD8"),
    "stone_skin": ("#3A382F", "#8A8578", "#C4BFB0"),
    "flesh_regrowth": ("#4E1E2C", "#C2687E", "#F0A0B4"),
    "flame_body": ("#4A1408", "#D4562A", "#FFB067"),
    "spirit_veins": ("#1E3244", "#8FB8D8", "#D8F0FF"),
    "soul_mirror": ("#2A2038", "#E4DCF0", "#FFFFFF"),
    "spirit_lamp": ("#4A3A16", "#E8C878", "#FFF0C0"),
    "soul_forge": ("#3E1C0C", "#B05A2A", "#F0A868"),
    "star_array": ("#3A3A6E", "#F2E9B0", "#FFFDF0"),
    "demon_eye": ("#240E34", "#7A4A8C", "#D8A8F0"),
    "ancient_seal": ("#3A3018", "#8C7A4A", "#D8C48C"),
}


def _colors(soul_id: str):
    dark, mid, light = PALETTES[soul_id]
    return rgba(dark), rgba(mid), rgba(light)


# ---------------------------------------------------------------- primitif turunan

def _bar(c: Canvas, x0, y0, x1, y1, color, width=1) -> None:
    c.line(x0, y0, x1, y1, color, width)


def _spike(c: Canvas, x, y, size, color, direction=0) -> None:
    """Segitiga kecil yang menunjuk ke arah 0=atas, 1=kanan, 2=bawah, 3=kiri."""
    if direction == 0:
        points = [(x, y - size), (x - size, y + size), (x + size, y + size)]
    elif direction == 1:
        points = [(x + size, y), (x - size, y - size), (x - size, y + size)]
    elif direction == 2:
        points = [(x, y + size), (x - size, y - size), (x + size, y - size)]
    else:
        points = [(x - size, y), (x + size, y - size), (x + size, y + size)]
    c.poly(points, color)


def _leaf(c: Canvas, x, y, length, color, angle=0.0) -> None:
    """Daun panjang yang melengkung; angle dalam radian, 0 menunjuk ke atas."""
    import math
    tipx = x + math.sin(angle) * length
    tipy = y - math.cos(angle) * length
    midx = (x + tipx) / 2 + math.cos(angle) * length * 0.28
    midy = (y + tipy) / 2 - math.sin(angle) * length * 0.28
    c.poly([(x, y), (midx, midy), (tipx, tipy)], color)


# ---------------------------------------------------------------- 33 gambar soul

def golden_dragon() -> Image.Image:  # noqa: F821
    """Kepala naga emas menghadap kanan: rahang terbuka, tanduk panjang, mata menyala."""
    c = Canvas()
    dark, mid, light = _colors("golden_dragon")
    # tanduk panjang menyapu ke belakang kiri atas
    c.poly([(5, 3), (0, 0), (2, 5)], dark)
    c.poly([(6, 2), (3, 0), (4, 4)], mid)
    # tengkorak
    c.poly([(3, 4), (9, 3), (11, 6), (8, 8), (4, 8)], mid)
    # rahang atas membuka ke kanan
    c.poly([(8, 4), (15, 5), (15, 7), (9, 7)], light)
    # rahang bawah
    c.poly([(8, 8), (14, 8), (12, 11), (7, 10)], dark)
    # mulut gelap
    c.rect(9, 7, 13, 7, dark)
    # mata menyala
    c.px(7, 5, light)
    c.px(8, 5, dark)
    return c.image


def abyssal_demon_dragon() -> Image.Image:  # noqa: F821
    """Naga jurang melingkar: badan S dari bawah ke atas, kepala bertanduk."""
    c = Canvas()
    dark, mid, light = _colors("abyssal_demon_dragon")
    # badan melingkar
    c.arc(8, 10, 5, 5, 0.2, 3.0, dark, 2)
    c.arc(8, 10, 5, 5, 0.4, 2.8, mid, 1)
    # kepala di kiri atas
    c.poly([(3, 2), (8, 3), (9, 6), (4, 6)], mid)
    c.poly([(2, 3), (5, 3), (3, 6)], light)
    # tanduk jarring
    c.poly([(4, 2), (1, 0), (2, 3)], dark)
    c.poly([(7, 2), (6, 0), (8, 3)], dark)
    # rahang bawah
    c.poly([(4, 6), (9, 6), (8, 8), (5, 8)], dark)
    c.px(6, 4, light)
    return c.image


def abyssal_ice_demon_dragon() -> Image.Image:  # noqa: F821
    """Kepala naga es bersudut: duri kristal menjajar, moncong sempit ke kanan."""
    c = Canvas()
    dark, mid, light = _colors("abyssal_ice_demon_dragon")
    # duri kristal ke kiri
    c.poly([(1, 1), (4, 5), (0, 5)], dark)
    c.poly([(1, 4), (4, 7), (0, 7)], mid)
    c.poly([(2, 7), (5, 9), (1, 10)], dark)
    # tengkorak dan moncong
    c.poly([(4, 3), (9, 5), (14, 7), (13, 9), (5, 9)], mid)
    c.poly([(9, 6), (15, 7), (14, 9), (9, 9)], light)
    # rahang dan mata
    c.poly([(5, 9), (12, 9), (11, 11), (6, 11)], dark)
    c.px(8, 6, light)
    c.px(9, 6, dark)
    return c.image


def ultimate_fire_dragon() -> Image.Image:  # noqa: F821
    """Naga api: badan panjang tipis melingkar dari kiri bawah ke kanan atas."""
    c = Canvas()
    dark, mid, light = _colors("ultimate_fire_dragon")
    # badan diagonal tipis
    c.line(1, 14, 10, 6, dark, 2)
    c.line(1, 14, 10, 6, mid, 1)
    # sirip dorsal
    c.poly([(3, 12), (1, 9), (4, 11)], dark)
    c.poly([(6, 8), (4, 5), (7, 7)], dark)
    # kepala kecil
    c.poly([(9, 3), (13, 4), (14, 6), (10, 7)], mid)
    c.poly([(12, 4), (15, 5), (14, 6), (12, 6)], light)
    c.px(11, 4, light)
    return c.image


def ultimate_ice_dragon() -> Image.Image:  # noqa: F821
    """Naga es: sayap lebar digambar sebagai kerangka tulang, tubuh kecil di tengah."""
    c = Canvas()
    dark, mid, light = _colors("ultimate_ice_dragon")
    # kerangka sayap kiri
    c.line(7, 5, 0, 1, mid, 1)
    c.line(0, 1, 2, 8, mid, 1)
    c.line(2, 8, 7, 9, mid, 1)
    c.line(7, 5, 2, 8, dark, 1)
    # kerangka sayap kanan
    c.line(8, 5, 15, 1, mid, 1)
    c.line(15, 1, 13, 8, mid, 1)
    c.line(13, 8, 8, 9, mid, 1)
    c.line(8, 5, 13, 8, dark, 1)
    # tubuh
    c.rect(6, 5, 9, 11, dark)
    c.rect(7, 5, 8, 8, mid)
    # kepala
    c.ellipse(7, 3, 2, 2, mid)
    c.px(6, 3, light)
    c.px(8, 3, light)
    return c.image


def thunder_elephant() -> Image.Image:  # noqa: F821
    """Kepala gajah: tengkorak bulat, gading dua, belalai melengkung ke bawah."""
    c = Canvas()
    dark, mid, light = _colors("thunder_elephant")
    # tengkorak
    c.ellipse(7, 6, 5, 4, mid)
    c.ellipse(7, 5, 4, 3, light)
    # telinga
    c.ellipse(2, 6, 2, 3, dark)
    # gading
    c.poly([(5, 9), (7, 9), (5, 14), (3, 14)], light)
    c.poly([(9, 9), (11, 9), (13, 14), (11, 14)], light)
    # belalai melengkung
    c.arc(8, 9, 4, 6, 0.0, 1.4, dark, 2)
    c.arc(8, 9, 4, 6, 0.2, 1.2, mid, 1)
    c.px(6, 4, dark)
    return c.image


def golden_ant() -> Image.Image:  # noqa: F821
    """Semut emas: tiga ruas tubuh, enam kaki, dua antena."""
    c = Canvas()
    dark, mid, light = _colors("golden_ant")
    # kepala
    c.ellipse(8, 3, 2, 2, mid)
    c.px(7, 2, light)
    c.px(9, 2, light)
    # antena
    c.line(7, 2, 4, 0, dark, 1)
    c.line(9, 2, 12, 0, dark, 1)
    # thoraks dan abdomen
    c.ellipse(8, 7, 2, 2, mid)
    c.ellipse(8, 11, 2, 3, dark)
    c.ellipse(8, 10, 1, 2, mid)
    # kaki
    for y in (6, 8, 10):
        c.line(6, y, 2, y + 2, dark, 1)
        c.line(10, y, 14, y + 2, dark, 1)
    return c.image


def titan_giant_ape() -> Image.Image:  # noqa: F821
    """Kepala kera raksasa: tengkorak lebar, alis berat, rahang besar, lengan pendek."""
    c = Canvas()
    dark, mid, light = _colors("titan_giant_ape")
    # lengan pendek di sisi
    c.rect(0, 7, 3, 9, dark)
    c.rect(12, 7, 15, 9, dark)
    # tengkorak lebar
    c.poly([(3, 2), (12, 2), (13, 7), (11, 10), (4, 10), (2, 7)], mid)
    # dahi
    c.poly([(4, 3), (11, 3), (11, 5), (4, 5)], light)
    # alis berat
    c.rect(3, 5, 12, 6, dark)
    # rahang besar
    c.poly([(4, 7), (11, 7), (10, 11), (5, 11)], dark)
    # mata
    c.px(5, 4, dark)
    c.px(10, 4, dark)
    return c.image


def sea_devil_manatee() -> Image.Image:  # noqa: F821
    """Manati setan laut: tubuh panjang yang rata di dasar, sirip ekor mendongak."""
    c = Canvas()
    dark, mid, light = _colors("sea_devil_manatee")
    # tubuh memanjang horizontal di baris bawah
    c.ellipse(7, 11, 6, 2, mid)
    c.ellipse(7, 10, 5, 1, light)
    # moncong tumpul ke kiri
    c.ellipse(2, 11, 2, 1, dark)
    # sirip ekor tegak ke kanan atas
    c.poly([(12, 10), (15, 4), (15, 10)], dark)
    c.poly([(13, 9), (15, 6), (15, 9)], mid)
    # sirip depan
    c.poly([(6, 13), (9, 13), (8, 15), (5, 15)], dark)
    c.px(3, 10, dark)
    return c.image


def bright_wing_moth() -> Image.Image:  # noqa: F821
    """Ngengat sayap cerah: tubuh ramping tegak, sayap digambar sebagai kerangka."""
    c = Canvas()
    dark, mid, light = _colors("bright_wing_moth")
    # kerangka sayap atas
    c.line(7, 4, 1, 0, mid, 1)
    c.line(1, 0, 0, 7, mid, 1)
    c.line(0, 7, 6, 8, mid, 1)
    c.line(7, 4, 0, 7, dark, 1)
    c.line(8, 4, 14, 0, mid, 1)
    c.line(14, 0, 15, 7, mid, 1)
    c.line(15, 7, 9, 8, mid, 1)
    c.line(8, 4, 15, 7, dark, 1)
    # sayap bawah sebagai garis
    c.line(7, 9, 2, 8, dark, 1)
    c.line(2, 8, 3, 14, dark, 1)
    c.line(3, 14, 7, 13, dark, 1)
    c.line(8, 9, 13, 8, dark, 1)
    c.line(13, 8, 12, 14, dark, 1)
    c.line(12, 14, 8, 13, dark, 1)
    # tubuh
    c.rect(7, 3, 8, 13, light)
    c.px(7, 2, dark)
    c.px(8, 2, dark)
    return c.image


def phoenix() -> Image.Image:  # noqa: F821
    """Burung api: tubuh tegak, ekor berbulu kipas ke kiri bawah."""
    c = Canvas()
    dark, mid, light = _colors("phoenix")
    # ekor kipas
    c.poly([(6, 8), (0, 6), (1, 10), (5, 11)], dark)
    c.poly([(6, 9), (1, 9), (2, 12), (5, 12)], mid)
    c.poly([(6, 10), (2, 12), (4, 14), (6, 12)], dark)
    # tubuh
    c.poly([(6, 2), (10, 3), (10, 9), (6, 10)], mid)
    c.poly([(7, 3), (9, 3), (9, 8), (7, 8)], light)
    # sayap kecil ke kanan
    c.poly([(10, 4), (15, 2), (14, 6), (10, 7)], dark)
    # kepala dan paruh
    c.ellipse(8, 2, 2, 2, light)
    c.poly([(10, 1), (13, 2), (10, 3)], dark)
    c.px(7, 1, dark)
    return c.image


def blue_silver_spear() -> Image.Image:  # noqa: F821
    """Tombak Blue Silver tegak: bilah bercabang tiga, collars, dan duri pangkal."""
    c = Canvas()
    dark, mid, light = _colors("blue_silver_spear")
    # bilah bercabang tiga
    c.poly([(8, 0), (11, 4), (9, 7), (6, 7), (4, 4)], mid)
    c.poly([(8, 1), (10, 4), (8, 6), (6, 6), (5, 4)], light)
    c.poly([(4, 3), (1, 0), (2, 5)], dark)
    c.poly([(11, 3), (14, 0), (13, 5)], dark)
    # collars
    c.rect(5, 7, 10, 8, dark)
    # batang
    c.rect(7, 8, 8, 14, mid)
    c.rect(7, 8, 7, 14, dark)
    # duri pangkal
    c.poly([(7, 15), (5, 12), (9, 12)], dark)
    return c.image


def seven_kill_sword() -> Image.Image:  # noqa: F821
    """Pedang tujuh bunuh tegak: bilah panjang, pelindung lebar, gagang dan bonggol."""
    c = Canvas()
    dark, mid, light = _colors("seven_kill_sword")
    # bilah
    c.poly([(7, 0), (9, 0), (9, 10), (7, 12)], mid)
    c.rect(7, 1, 7, 9, light)
    # pelindung
    c.rect(3, 10, 12, 11, light)
    c.rect(3, 10, 12, 10, dark)
    # gagang dan bonggol
    c.rect(7, 11, 8, 14, dark)
    c.ellipse(7, 15, 2, 1, light)
    # ukiran blood groove
    c.px(8, 3, dark)
    c.px(8, 5, dark)
    return c.image


def giant_hammer() -> Image.Image:  # noqa: F821
    """Palu raksasa: kepala batu bertaring di kanan atas, gagang tebal ke kiri bawah."""
    c = Canvas()
    dark, mid, light = _colors("giant_hammer")
    # kepala palu bertaring
    c.poly([(5, 1), (11, 0), (15, 3), (14, 7), (6, 7)], mid)
    c.poly([(7, 2), (11, 1), (13, 3), (12, 5), (7, 5)], light)
    c.rect(4, 3, 6, 6, dark)
    # gagang tebal
    c.line(6, 7, 1, 15, dark, 3)
    c.line(6, 7, 1, 15, mid, 1)
    # bungkus kulit di pangkal gagang
    c.rect(0, 13, 2, 15, light)
    return c.image


def blood_lash() -> Image.Image:  # noqa: F821
    """Cambuk darah: gagang di kanan bawah, pelengkungan panjang ke kiri atas."""
    c = Canvas()
    dark, mid, light = _colors("blood_lash")
    # pelengkungan cambuk
    c.arc(9, 9, 8, 8, 2.2, 3.6, dark, 2)
    c.arc(9, 9, 8, 8, 2.4, 3.4, mid, 1)
    # ujung cambuk
    c.poly([(1, 1), (4, 2), (2, 4)], light)
    # gagang
    c.line(11, 11, 15, 15, dark, 3)
    c.line(11, 11, 15, 15, mid, 1)
    c.px(12, 12, light)
    return c.image


def frost_needle() -> Image.Image:  # noqa: F821
    """Jarum es: batang tipis panjang dari kiri bawah ke kanan atas."""
    c = Canvas()
    dark, mid, light = _colors("frost_needle")
    # batang jarum
    c.line(1, 14, 14, 2, dark, 3)
    c.line(1, 14, 14, 2, mid, 1)
    # ujung runcing
    c.poly([(14, 1), (15, 2), (13, 4)], light)
    # cincin tangkai
    c.ellipse(3, 12, 2, 2, light, fill=False)
    # kristal kecil menempel
    c.px(7, 9, light)
    c.px(8, 8, light)
    return c.image


def thunder_spear() -> Image.Image:  # noqa: F821
    """Tombak petir: tombak miring ke kanan, kilat zigzag di sebelah kiri."""
    c = Canvas()
    dark, mid, light = _colors("thunder_spear")
    # tombak
    c.line(3, 15, 11, 5, dark, 3)
    c.line(3, 15, 11, 5, mid, 1)
    c.poly([(11, 5), (14, 1), (13, 6)], light)
    # zigzag petir
    c.line(2, 2, 5, 6, light, 1)
    c.line(5, 6, 2, 9, light, 1)
    c.line(2, 9, 6, 13, light, 1)
    return c.image


def spirit_sword() -> Image.Image:  # noqa: F821
    """Pedang pendek mendatar: bilah ke kanan, pelindung tegak, gagang ke kiri."""
    c = Canvas()
    dark, mid, light = _colors("spirit_sword")
    # bilah mendatar
    c.poly([(5, 6), (15, 6), (15, 8), (5, 9)], light)
    c.rect(6, 6, 14, 6, mid)
    # pelindung tegak
    c.rect(4, 3, 5, 12, mid)
    # gagang
    c.rect(1, 7, 3, 8, dark)
    c.rect(0, 6, 0, 9, light)
    return c.image


def iron_anchor() -> Image.Image:  # noqa: F821
    """Jangkar besi: cincin atas, batang tegak, dua cakar melengkung di bawah."""
    c = Canvas()
    dark, mid, light = _colors("iron_anchor")
    # cincin atas
    c.ellipse(8, 2, 3, 3, mid, fill=False)
    # batang
    c.rect(7, 4, 8, 12, mid)
    c.rect(7, 4, 7, 12, dark)
    # cakar melengkung
    c.arc(8, 10, 6, 5, 0.2, 1.5, dark, 2)
    c.arc(8, 10, 6, 5, 1.6, 2.9, dark, 2)
    c.poly([(1, 12), (4, 12), (2, 15)], light)
    c.poly([(14, 12), (11, 12), (13, 15)], light)
    return c.image


def bronze_mirror() -> Image.Image:  # noqa: F821
    """Cermin perunggu: oval berongga dengan gagang pendek di bawah."""
    c = Canvas()
    dark, mid, light = _colors("bronze_mirror")
    # badan oval digambar sebagai cincin
    c.ellipse(8, 6, 5, 6, mid, fill=False)
    c.ellipse(8, 6, 3, 4, light, fill=False)
    # pantulan kecil
    c.px(6, 4, light)
    c.px(7, 3, light)
    # gagang
    c.rect(7, 11, 8, 14, dark)
    c.rect(6, 14, 9, 15, mid)
    return c.image


def blue_silver_emperor() -> Image.Image:  # noqa: F821
    """Mahkota rumput: lima bilah daun tumbuh dari garis dasar melengkung."""
    c = Canvas()
    dark, mid, light = _colors("blue_silver_emperor")
    # alas mahkota
    c.arc(8, 12, 7, 6, 3.4, 5.9, dark, 2)
    # bilah daun
    _leaf(c, 3, 12, 8, mid, -0.5)
    _leaf(c, 5, 11, 10, light, -0.2)
    _leaf(c, 8, 10, 12, light, 0.0)
    _leaf(c, 11, 11, 10, mid, 0.2)
    _leaf(c, 13, 12, 8, mid, 0.5)
    return c.image


def iron_body() -> Image.Image:  # noqa: F821
    """Tubuh besi: kolom ramping dengan dua bahu kecil di atas dan keling rapat."""
    c = Canvas()
    dark, mid, light = _colors("iron_body")
    # kolom tubuh sempit
    c.rect(6, 4, 9, 14, mid)
    c.rect(6, 4, 6, 14, dark)
    # bahu kecil menghadap keluar
    c.poly([(6, 4), (3, 2), (3, 5)], light)
    c.poly([(9, 4), (12, 2), (12, 5)], light)
    # keling rapat
    c.px(7, 7, light)
    c.px(8, 7, light)
    c.px(7, 10, light)
    c.px(8, 10, light)
    c.px(7, 13, light)
    return c.image


def bone_shell() -> Image.Image:  # noqa: F821
    """Cangkang tulang: tulang belakang tegak dengan tulang rusuk melengkung terpisah."""
    c = Canvas()
    dark, mid, light = _colors("bone_shell")
    # tulang belakang
    c.rect(7, 1, 8, 15, mid)
    for y in range(1, 15, 3):
        c.px(7, y, dark)
    # rusuk kiri dan kanan, melengkung ke bawah
    for y in (3, 7, 11):
        c.arc(8, y, 5, 3, 3.1, 4.6, dark, 1)
        c.arc(8, y, 5, 3, 1.7, 3.2, dark, 1)
    # tulang kencing
    c.px(3, 5, light)
    c.px(12, 9, light)
    c.px(4, 12, light)
    return c.image


def stone_skin() -> Image.Image:  # noqa: F821
    """Kulit batu: tiga bongkar kecil berjajar diagonal dengan celah di antaranya."""
    c = Canvas()
    dark, mid, light = _colors("stone_skin")
    # batu bawah kanan
    c.poly([(9, 10), (14, 9), (15, 14), (10, 15)], dark)
    # batu tengah
    c.poly([(4, 6), (9, 5), (10, 10), (5, 11)], mid)
    # batu atas kiri
    c.poly([(1, 1), (5, 0), (6, 4), (2, 5)], light)
    # retakan
    c.px(7, 8, dark)
    c.px(12, 12, dark)
    return c.image


def flesh_regrowth() -> Image.Image:  # noqa: F821
    """Tumbuh otot: kepalan tangan di bawah dengan tunas daun besar di atasnya."""
    c = Canvas()
    dark, mid, light = _colors("flesh_regrowth")
    # kepalan tangan
    c.poly([(4, 10), (11, 10), (11, 14), (4, 14)], mid)
    c.rect(5, 11, 6, 13, light)
    c.rect(8, 11, 9, 13, light)
    # tunas daun dari buku jari
    _leaf(c, 5, 10, 9, light, -0.45)
    _leaf(c, 8, 9, 11, mid, 0.0)
    _leaf(c, 10, 10, 8, light, 0.5)
    return c.image


def flame_body() -> Image.Image:  # noqa: F821
    """Tubuh bara: siluet torso ramping dengan nyala api di kepala."""
    c = Canvas()
    dark, mid, light = _colors("flame_body")
    # nyala atas
    c.poly([(6, 6), (7, 1), (8, 4), (9, 2), (9, 6)], light)
    c.poly([(7, 6), (7, 3), (8, 5), (8, 6)], mid)
    # torso ramping
    c.poly([(5, 6), (10, 6), (10, 12), (9, 15), (6, 15), (5, 12)], mid)
    # dada digambar sebagai bingkai
    c.frame(6, 8, 9, 11, dark)
    # bara
    c.px(7, 9, light)
    return c.image


def spirit_veins() -> Image.Image:  # noqa: F821
    """Urat spirit: lengan miring ramping dengan sambaran petir di sepanjang tulang."""
    c = Canvas()
    dark, mid, light = _colors("spirit_veins")
    # lengan ramping dari kiri bawah ke kanan atas
    c.poly([(1, 12), (4, 10), (13, 3), (15, 5), (6, 13)], mid)
    c.poly([(3, 11), (5, 10), (12, 4), (13, 5), (6, 12)], light)
    # sambaran petir di dalam lengan
    c.line(4, 11, 7, 8, light, 1)
    c.line(7, 8, 6, 10, light, 1)
    c.line(6, 10, 10, 7, light, 1)
    c.line(10, 7, 9, 9, light, 1)
    # tangan
    c.rect(13, 2, 15, 6, dark)
    return c.image


def soul_mirror() -> Image.Image:  # noqa: F821
    """Cermin soul: bingkai persegi berongga dengan wajah samar di dalamnya."""
    c = Canvas()
    dark, mid, light = _colors("soul_mirror")
    # bingkai
    c.frame(3, 1, 12, 14, mid)
    c.frame(4, 2, 11, 13, dark)
    # wajah samar kecil
    c.ellipse(7, 7, 2, 3, mid)
    c.px(6, 6, dark)
    c.px(9, 6, dark)
    # alas
    c.rect(5, 14, 10, 15, mid)
    return c.image


def spirit_lamp() -> Image.Image:  # noqa: F821
    """Lentera spirit: badan berongga, handle melengkung di atas, nyala di dalam."""
    c = Canvas()
    dark, mid, light = _colors("spirit_lamp")
    # handle melengkung di atas
    c.arc(8, 2, 3, 2, 3.2, 6.1, mid, 1)
    # tutup
    c.poly([(4, 4), (12, 4), (10, 6), (6, 6)], mid)
    # badan lentera sebagai kerangka
    c.line(6, 6, 5, 13, mid, 1)
    c.line(10, 6, 11, 13, mid, 1)
    c.line(5, 13, 11, 13, mid, 1)
    # nyala di dalam
    c.poly([(7, 12), (8, 9), (9, 12)], light)
    c.px(8, 12, mid)
    # alas
    c.rect(4, 13, 11, 14, dark)
    return c.image


def soul_forge() -> Image.Image:  # noqa: F821
    """Tempa soul: badan tempa berongga dengan tanduk kiri dan bara di bak."""
    c = Canvas()
    dark, mid, light = _colors("soul_forge")
    # landasan
    c.rect(4, 13, 12, 15, dark)
    # badan tempa digambar sebagai kerangka
    c.rect(4, 8, 12, 12, mid)
    # bagian dalam dikosongkan: gambar ulang tepi dalam
    c.rect(5, 9, 11, 12, dark)
    # muka atas
    c.rect(3, 6, 13, 7, light)
    # tanduk kiri
    c.poly([(4, 8), (0, 9), (4, 10)], light)
    # bara
    c.px(7, 9, light)
    c.px(9, 10, light)
    return c.image


def star_array() -> Image.Image:  # noqa: F821
    """Susunan bintang: tujuh bintang kecil yang dihubungkan garis tipis."""
    c = Canvas()
    dark, mid, light = _colors("star_array")
    stars = [(2, 3), (7, 1), (13, 4), (4, 9), (9, 7), (14, 11), (6, 14)]
    for a, b in zip(stars, stars[1:] + stars[:1]):
        c.line(a[0], a[1], b[0], b[1], dark, 1)
    for x, y in stars:
        c.px(x, y, light)
        c.px(x - 1, y, mid)
        c.px(x + 1, y, mid)
        c.px(x, y - 1, mid)
        c.px(x, y + 1, mid)
    return c.image


def demon_eye() -> Image.Image:  # noqa: F821
    """Mata setan: almond berongga dengan pupil vertikal kecil dan alis berat."""
    c = Canvas()
    dark, mid, light = _colors("demon_eye")
    # almond mata digambar sebagai cincin
    c.ellipse(8, 8, 7, 4, mid, fill=False)
    c.ellipse(8, 8, 5, 2, dark, fill=False)
    # pupil vertikal kecil
    c.rect(7, 6, 8, 10, light)
    c.rect(7, 7, 7, 9, dark)
    # alis
    c.poly([(1, 4), (8, 2), (15, 4), (14, 5), (8, 4), (2, 5)], dark)
    return c.image


def ancient_seal() -> Image.Image:  # noqa: F821
    """Segel purba: bingkai batu berongga dengan sigil spiral kecil di tengah."""
    c = Canvas()
    dark, mid, light = _colors("ancient_seal")
    # bingkai segel
    c.frame(2, 2, 13, 13, mid)
    c.frame(3, 3, 12, 12, dark)
    # sudut pecah
    c.poly([(2, 2), (5, 2), (2, 5)], light)
    c.poly([(13, 13), (10, 13), (13, 10)], light)
    # sigil spiral kecil
    c.arc(8, 8, 3, 3, 0.0, 4.2, light, 1)
    c.px(8, 8, mid)
    return c.image


# ---------------------------------------------------------------- daftar

V14_SOUL_ART = {
    "golden_dragon": golden_dragon,
    "abyssal_demon_dragon": abyssal_demon_dragon,
    "abyssal_ice_demon_dragon": abyssal_ice_demon_dragon,
    "ultimate_fire_dragon": ultimate_fire_dragon,
    "ultimate_ice_dragon": ultimate_ice_dragon,
    "thunder_elephant": thunder_elephant,
    "golden_ant": golden_ant,
    "titan_giant_ape": titan_giant_ape,
    "sea_devil_manatee": sea_devil_manatee,
    "bright_wing_moth": bright_wing_moth,
    "phoenix": phoenix,
    "blue_silver_spear": blue_silver_spear,
    "seven_kill_sword": seven_kill_sword,
    "giant_hammer": giant_hammer,
    "blood_lash": blood_lash,
    "frost_needle": frost_needle,
    "thunder_spear": thunder_spear,
    "spirit_sword": spirit_sword,
    "iron_anchor": iron_anchor,
    "bronze_mirror": bronze_mirror,
    "blue_silver_emperor": blue_silver_emperor,
    "iron_body": iron_body,
    "bone_shell": bone_shell,
    "stone_skin": stone_skin,
    "flesh_regrowth": flesh_regrowth,
    "flame_body": flame_body,
    "spirit_veins": spirit_veins,
    "soul_mirror": soul_mirror,
    "spirit_lamp": spirit_lamp,
    "soul_forge": soul_forge,
    "star_array": star_array,
    "demon_eye": demon_eye,
    "ancient_seal": ancient_seal,
}
