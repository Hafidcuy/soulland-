#!/usr/bin/env python3
"""Alat bantu: hitung kemiripan siluet kandidat terhadap semua item yang ada.

Dipakai saat designing texture baru supaya siluetnya tidak kembar dengan
item yang sudah ada. Jalankan: python3 tools/measure_silhouette.py
"""
import json
import pathlib
import sys

from PIL import Image

ROOT = pathlib.Path(__file__).resolve().parent.parent
ASSETS = ROOT / "src/main/resources/assets/soulland"
MAX_IOU = 0.70


def alpha_mask(path):
    image = Image.open(path).convert("RGBA")
    return [1 if pixel[3] > 40 else 0 for pixel in image.getdata()]


def best_iou(first, second):
    best = 0.0
    for dy in range(-2, 3):
        for dx in range(-2, 3):
            inter = 0
            union = 0
            for y in range(16):
                for x in range(16):
                    a = first[y * 16 + x]
                    sy, sx = y + dy, x + dx
                    b = second[sy * 16 + sx] if 0 <= sy < 16 and 0 <= sx < 16 else 0
                    if a or b:
                        union += 1
                    if a and b:
                        inter += 1
            if union:
                best = max(best, inter / union)
    return best


def existing_items():
    items = []
    for path in sorted((ASSETS / "textures/item").glob("*.png")):
        items.append(path.name)
    return items


def main():
    if len(sys.argv) < 2:
        print("pemakaian: python3 tools/measure_silhouette.py <texture> [texture ...]")
        return 1
    others = {name: alpha_mask(ASSETS / "textures/item" / name) for name in existing_items()}
    worst = (0.0, None, None)
    failures = []
    for candidate in sys.argv[1:]:
        path = pathlib.Path(candidate)
        if not path.is_absolute():
            path = ROOT / path
        mask = alpha_mask(path)
        for name, other in others.items():
            if name == path.name:
                continue
            value = best_iou(mask, other)
            if value > worst[0]:
                worst = (value, path.name, name)
            if value >= MAX_IOU:
                failures.append(f"{path.name} vs {name} = {value:.3f}")
    print(f"kemiripan tertinggi: {worst[0]:.3f} ({worst[1]} | {worst[2]})")
    if failures:
        print("GAGAL:")
        for failure in failures:
            print(f"  - {failure}")
        return 1
    print("LOLOS: tidak ada siluet kembar.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
