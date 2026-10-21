#!/usr/bin/env python3
"""Ikon True Body dan Domain untuk 43 martial soul.

Warna dasar dibaca dari registry Java: warna soul untuk warna dasar ikon dan
warna elemen untuk aksen. Bentuk ikon bukan satu template: tiap soul memakai
sigil sendiri yang dipilih lewat hash id, dan generator memeriksa siluetnya
supaya tidak ada dua ikon yang terlihat sebagai benda yang sama.

Dua keluarga bentuk:
  * True Body: sigil ascensi, vertikal, mahkota di atas dan kaki di bawah.
  * Domain: sigil wilayah, horizontal, cincin tanah dengan sinar dari pusat.

Modul ini hanya berisi gambar. Penulisan file PNG, manifest, dan lembar
kontak dilakukan oleh `tools/generate_assets.py`.
"""
from __future__ import annotations

import hashlib
import math
import os
import re
import sys

import numpy as np

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from generate_assets import Canvas  # noqa: E402

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SOUL_FILE = os.path.join(ROOT, "src/main/java/com/soulland/soul/MartialSouls.java")
ELEMENT_FILE = os.path.join(ROOT, "src/main/java/com/soulland/soul/Element.java")

SIZE = 16

# Warna tinta dari DESIGN.md, dipakai sebagai dasar pencampuran warna soul.
INK = (0x14, 0x16, 0x1A)

# Batas kemiripan siluet dua ikon. Sama dengan batas yang dipakai validator,
# dengan margin supaya hasilnya tidak meleset di batas.
MAX_IOU = 0.62

SOUL_PATTERN = re.compile(
    r'register\(new MartialSoul\(\s*"(?P<id>[^"]+)",\s*"[^"]*",\s*"[^"]*",\s*"[^"]*",\s*"[^"]*",'
    r'\s*"[^"]*",\s*"[^"]*",\s*"[^"]*",\s*SoulLineage\.(?P<lineage>\w+),\s*Element\.\w+,'
    r'\s*"[^"]*",\s*"(?P<element>[^"]*)",\s*(?P<color>0x[0-9A-Fa-f]+)',
    re.S,
)

ELEMENT_PATTERN = re.compile(r'^\s+(\w+)\("[^"]*",\s*(0x[0-9A-Fa-f]+)\)', re.M)


# ---------------------------------------------------------------- data registry

def soul_records() -> list[dict]:
    """Baca id, lineage, elemen, dan warna soul langsung dari registry Java."""
    source = open(SOUL_FILE, encoding="utf-8").read()
    records = []
    for match in SOUL_PATTERN.finditer(source):
        records.append({
            "id": match.group("id"),
            "lineage": match.group("lineage"),
            "element": match.group("element"),
            "color": int(match.group("color"), 16),
        })
    return records


def element_colors() -> dict:
    """Warna tiap elemen, dibaca dari enum Element di kode Java."""
    source = open(ELEMENT_FILE, encoding="utf-8").read()
    return {match.group(1): int(match.group(2), 16) for match in ELEMENT_PATTERN.finditer(source)}


def opaque(color: tuple) -> tuple:
    """Warna RGB jadi RGBA pekat."""
    return (color[0], color[1], color[2], 255)


def hex_of(color: tuple) -> str:
    """Warna RGB jadi teks hex untuk manifest."""
    return "#{:02X}{:02X}{:02X}".format(*color)


def mix(color: tuple, target: tuple, amount: float) -> tuple:
    """Campur warna dengan warna target, amount 0 berarti warna asli."""
    return tuple(int(round(channel + (other - channel) * amount))
                 for channel, other in zip(color, target))


def rgb(value: int) -> tuple:
    return ((value >> 16) & 0xFF, (value >> 8) & 0xFF, value & 0xFF)


# ---------------------------------------------------------------- warna ikon

def true_body_palette(soul: dict, elements: dict) -> list:
    """Palet ikon True Body: warna soul digabung dengan warna elemen."""
    base = rgb(soul["color"])
    accent = rgb(elements.get(soul["element"], 0xC8C8C8))
    dark = mix(base, INK, 0.66)
    light = mix(base, (255, 255, 255), 0.52)
    return [dark, base, light, accent]


def domain_palette(soul: dict, elements: dict) -> list:
    """Palet ikon Domain: warna soul dengan campuran lebih ringan dari True Body."""
    base = rgb(soul["color"])
    accent = rgb(elements.get(soul["element"], 0xC8C8C8))
    dark = mix(base, INK, 0.44)
    light = mix(base, (255, 255, 255), 0.26)
    return [dark, base, light, accent]


# ---------------------------------------------------------------- parameter

def params_for(soul_id: str, kind: str, attempt: int) -> dict:
    """Parameter gambar yang ditentukan hash, jadi hasilnya selalu sama."""
    digest = hashlib.sha256(f"{kind}:{soul_id}:{attempt}".encode("utf-8")).digest()
    if kind == "true_body":
        return {
            "crown": digest[0] % 4,
            "arms": 2 + digest[1] % 5,
            "arm_len": 3 + digest[2] % 4,
            "spread": 18 + digest[3] % 44,
            "spine": 1 + digest[4] % 3,
            "tilt": (digest[5] % 5) - 2,
            "base": digest[6] % 4,
            "gem": digest[7] % 3,
            "direction": 1 if digest[8] % 2 else -1,
            "ornament": digest[9] % 4,
        }
    return {
        "ring_style": digest[0] % 5,
        "ring_rx": 4 + digest[1] % 4,
        "ring_ry": 2 + digest[2] % 3,
        "spokes": 3 + digest[3] % 5,
        "spoke_len": 2 + digest[4] % 3,
        "tilt": (digest[5] % 5) - 2,
        "core": digest[6] % 4,
        "dome": digest[7] % 3,
        "spoke_dir": digest[8] % 3,
        "ornament": digest[9] % 6,
    }


# ---------------------------------------------------------------- gambar

def thin_ellipse(canvas: Canvas, cx: float, cy: float, rx: float, ry: float, color, width: int = 1) -> None:
    """Cincin oval tipis, dipakai untuk cincin tanah domain."""
    for y in range(canvas.size):
        for x in range(canvas.size):
            nx = (x + 0.5 - cx) / rx
            ny = (y + 0.5 - cy) / ry
            value = nx * nx + ny * ny
            if 0.70 <= value <= 1.30:
                canvas.px(x, y, color)


def draw_true_body(canvas: Canvas, palette: list, p: dict) -> None:
    """Sigil ascensi: mahkota, lengan yang mengembang, batang, dan batu kaki."""
    dark, mid, light, accent = (opaque(color) for color in palette)
    cx = 8 + p["tilt"]
    crown_bottom = 5

    if p["crown"] == 0:
        canvas.ellipse(cx, 4.0, 4.0, 3.0, light, fill=False)
    elif p["crown"] == 1:
        canvas.arc(cx, 4.0, 4.5, 3.5, math.pi, 2.0 * math.pi, light, 1)
        canvas.rect(cx - 4, 3, cx + 4, 4, light)
    elif p["crown"] == 2:
        canvas.rect(cx - 4, 3, cx + 4, 4, light)
        for offset in (-3, 0, 3):
            canvas.poly([(cx + offset - 1, 3), (cx + offset, 0), (cx + offset + 1, 3)], light)
    else:
        canvas.rect(cx - 5, 3, cx + 5, 4, light)
        canvas.px(cx, 1, accent)
        canvas.px(cx - 1, 2, accent)
        canvas.px(cx + 1, 2, accent)

    arms = p["arms"]
    for index in range(arms):
        offset = index - (arms - 1) / 2.0
        spread = p["spread"] * offset / max(1.0, (arms - 1) / 2.0)
        angle = math.radians(spread)
        dx = math.sin(angle) * p["arm_len"]
        dy = math.cos(angle) * p["arm_len"] * p["direction"]
        canvas.line(cx, crown_bottom, cx + dx, crown_bottom + dy, mid, 1)

    spine_left = cx - p["spine"] // 2
    canvas.rect(spine_left, crown_bottom, spine_left + p["spine"] - 1, 13, mid)
    canvas.rect(spine_left, crown_bottom, spine_left, 13, dark)

    gem = p["gem"]
    if gem == 0:
        canvas.rect(cx - 1, 6, cx, 7, accent)
    elif gem == 1:
        canvas.rect(cx - 1, 6, cx, 8, accent)
        canvas.px(cx, 7, light)
    else:
        canvas.poly([(cx, 5), (cx + 1, 7), (cx, 9), (cx - 1, 7)], accent)

    base = p["base"]
    if base == 1:
        canvas.rect(cx - 2, 13, cx + 2, 14, dark)
    elif base == 2:
        canvas.rect(cx - 3, 13, cx + 3, 15, dark)
        canvas.rect(cx - 3, 13, cx + 3, 13, mid)
    elif base == 3:
        canvas.rect(cx - 1, 12, cx, 15, dark)
        canvas.rect(cx - 3, 14, cx + 3, 15, mid)

    ornament = p["ornament"]
    if ornament == 1:
        canvas.rect(cx + 3, 6, cx + 5, 6, light)
        canvas.rect(cx + 5, 5, cx + 5, 8, light)
    elif ornament == 2:
        canvas.rect(cx - 5, 8, cx - 3, 8, accent)
        canvas.rect(cx - 5, 9, cx - 4, 11, accent)
    elif ornament == 3:
        canvas.rect(cx - 4, 11, cx - 3, 14, dark)


def draw_domain(canvas: Canvas, palette: list, p: dict) -> None:
    """Sigil wilayah: cincin tanah, sinar dari pusat, inti, dan hiasan."""
    dark, mid, light, accent = (opaque(color) for color in palette)
    cx = 8 + p["tilt"]
    cy = 11
    rx = p["ring_rx"]
    ry = p["ring_ry"]

    style = p["ring_style"]
    if style == 0:
        thin_ellipse(canvas, cx, cy, rx, ry, accent)
    elif style == 1:
        thin_ellipse(canvas, cx, cy, rx, ry, accent)
        for offset in (-rx, rx):
            canvas.poly([(cx + offset, cy - 1), (cx + offset, cy - 3), (cx + offset + 1, cy - 1)], accent)
    elif style == 2:
        thin_ellipse(canvas, cx, cy, rx, ry, accent)
        thin_ellipse(canvas, cx, cy, max(2.0, rx - 3), max(1.0, ry - 1), mid)
    elif style == 3:
        canvas.rect(cx - rx, cy - ry, cx + rx, cy + ry, accent)
        canvas.rect(cx - rx + 1, cy - ry + 1, cx + rx - 1, cy + ry - 1, (0, 0, 0, 0))
    else:
        canvas.arc(cx, cy, rx, ry + 1, 0.15, math.pi - 0.15, accent, 1)
        canvas.rect(cx - rx, cy, cx + rx, cy, accent)

    spokes = p["spokes"]
    for index in range(spokes):
        angle = math.pi * (index + 0.5) / spokes
        if p["spoke_dir"] == 1:
            angle = 2.0 * math.pi * index / spokes
        elif p["spoke_dir"] == 2:
            angle = math.pi * index / max(1, spokes - 1)
        dx = math.cos(angle) * p["spoke_len"]
        dy = -math.sin(angle) * p["spoke_len"]
        canvas.line(cx, cy, cx + dx, cy + dy, light, 1)

    if p["dome"] == 1:
        canvas.arc(cx, cy - 1, 4, 4, math.pi, 2.0 * math.pi, dark, 1)
    elif p["dome"] == 2:
        canvas.arc(cx, cy - 2, 3, 3, math.pi, 2.0 * math.pi, mid, 1)

    core = p["core"]
    if core == 0:
        canvas.rect(cx - 1, cy - 1, cx, cy, light)
    elif core == 1:
        canvas.poly([(cx, cy - 2), (cx + 1, cy), (cx, cy + 1), (cx - 1, cy)], light)
    elif core == 2:
        canvas.rect(cx - 1, cy - 2, cx, cy, mid)
        canvas.px(cx, cy - 1, accent)
    else:
        canvas.rect(cx - 1, cy - 1, cx + 1, cy + 1, light)
        canvas.rect(cx, cy, cx, cy, accent)

    ornament = p["ornament"]
    if ornament == 1:
        canvas.rect(cx + rx + 1, cy - 1, cx + rx + 2, cy + 3, accent)
        canvas.rect(cx + rx + 2, cy - 1, cx + rx + 2, cy - 3, accent)
    elif ornament == 2:
        canvas.rect(cx - rx - 2, cy - 1, cx - rx - 1, cy + 3, light)
    elif ornament == 3:
        canvas.rect(cx - 3, cy + ry + 1, cx + 3, cy + ry + 2, dark)
    elif ornament == 4:
        canvas.rect(cx + rx - 1, cy - ry - 2, cx + rx, cy - ry - 1, mid)
    elif ornament == 5:
        canvas.rect(cx - 4, cy - 5, cx + 4, cy - 5, dark)


# ---------------------------------------------------------------- siluet

def mask_of(image) -> np.ndarray:
    """Mask alpha sebuah ikon, sama aturan ambangnya dengan validator."""
    return np.array(image)[:, :, 3] > 40


def shifted_iou(first: np.ndarray, second: np.ndarray) -> float:
    """IoU tertinggi dari mask alpha dengan pergeseran -2..2 piksel."""
    padded = np.zeros((SIZE + 8, SIZE + 8), dtype=bool)
    padded[4:4 + SIZE, 4:4 + SIZE] = second
    best = 0.0
    for dy in range(-2, 3):
        for dx in range(-2, 3):
            shifted = padded[4 + dy:4 + dy + SIZE, 4 + dx:4 + dx + SIZE]
            union = np.logical_or(first, shifted).sum()
            if union:
                inter = np.logical_and(first, shifted).sum()
                best = max(best, inter / union)
    return best


def best_iou(first: np.ndarray, second: np.ndarray) -> float:
    """IoU tertinggi dengan kedua arah pergeseran, jadi simetris.

    <p>Validator memakai satu arah saja, jadi di sini dipakai yang paling
    ketat: dua arah sekaligus. Dengan begitu dijamin bahwa ikon yang lolos
    generator juga lolos validator.
    """
    return max(shifted_iou(first, second), shifted_iou(second, first))


def build_icons(records: list, elements: dict) -> dict:
    """Bikin semua ikon, satu per soul untuk True Body dan satu untuk Domain."""
    icons = {}
    masks = []
    for record in records:
        for kind, palette_of in (("true_body", true_body_palette), ("domain", domain_palette)):
            palette = palette_of(record, elements)
            icon_id = f"{kind}_{record['id']}"
            for attempt in range(400):
                params = params_for(record["id"], kind, attempt)
                canvas = Canvas(SIZE)
                if kind == "true_body":
                    draw_true_body(canvas, palette, params)
                else:
                    draw_domain(canvas, palette, params)
                mask = mask_of(canvas.image)
                if all(best_iou(mask, other) < MAX_IOU for other in masks):
                    icons[icon_id] = canvas.image
                    masks.append(mask)
                    break
            else:
                raise RuntimeError(f"tidak ada bentuk unik untuk {icon_id}")
    return icons


def split_icon_id(icon_id: str) -> tuple:
    """Pecah id ikon jadi jenis dan id soul."""
    for kind in ("true_body", "domain"):
        if icon_id.startswith(kind + "_"):
            return kind, icon_id[len(kind) + 1:]
    raise KeyError(icon_id)


def palette_of_icon(icon_id: str, records: list, elements: dict) -> list:
    """Palet ikon, dihitung ulang dari id ikon supaya manifest dan gambar sama."""
    kind, soul_id = split_icon_id(icon_id)
    record = next(item for item in records if item["id"] == soul_id)
    if kind == "true_body":
        return true_body_palette(record, elements)
    return domain_palette(record, elements)


def build_manifest_entries(records: list, elements: dict, icons: dict) -> list:
    """Daftar entri manifest untuk ikon v1.5."""
    entries = []
    for icon_id in icons:
        kind, soul_id = split_icon_id(icon_id)
        entries.append({
            "id": icon_id,
            "kind": kind,
            "soul": soul_id,
            "texture": f"gui/{icon_id}.png",
            "palette": [hex_of(color) for color in palette_of_icon(icon_id, records, elements)],
        })
    return entries
