#!/usr/bin/env python3
"""Cek cepat kemiripan siluet 33 texture manifestasi v1.4.

Dipakai saat menggambar:vikara memberi tahu pasangan yang terlangsung.
"""
import itertools
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


def main():
    manifests = ["v1.2a", "v1.2b", "v1.3", "v1.4"]
    entries = []
    for name in manifests:
        path = ROOT / f"docs/{name}-asset-manifest.json"
        if not path.exists():
            continue
        for entry in json.loads(path.read_text(encoding="utf-8"))["items"]:
            entries.append(entry)
    masks = {}
    for entry in entries:
        path = ASSETS / "textures/item" / f"{entry['id']}.png"
        masks[entry["id"]] = alpha_mask(path)

    failures = []
    worst = (0.0, None, None)
    for first, second in itertools.combinations(entries, 2):
        if first.get("slot") is not None and first.get("slot") == second.get("slot"):
            continue
        value = best_iou(masks[first["id"]], masks[second["id"]])
        if value > worst[0]:
            worst = (value, first["id"], second["id"])
        if value >= MAX_IOU:
            failures.append((value, first["id"], second["id"]))
    failures.sort(reverse=True)
    for value, first, second in failures:
        print(f"GAGAL {value:.3f}  {first}  |  {second}")
    print(f"tertinggi: {worst[0]:.3f} ({worst[1]} | {worst[2]})")
    return 1 if failures else 0


if __name__ == "__main__":
    sys.exit(main())
