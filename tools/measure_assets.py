#!/usr/bin/env python3
"""Ukur keunikan siluet + palet texture v1.2A.

Metrik:
- IoU siluet terbaik (pergeseran -2..2 piksel) untuk tiap pasangan item.
- Signature palet: himpunan tiga warna inti yang dideklarasikan.
"""
import itertools
import json
import os
import pathlib

from PIL import Image

ROOT = pathlib.Path(__file__).resolve().parent.parent
TEX = ROOT / "src/main/resources/assets/soulland/textures/item"
MANIFEST = ROOT / "docs/v1.2a-asset-manifest.json"


def mask(item_id):
    image = Image.open(TEX / f"{item_id}.png").convert("RGBA")
    return [1 if p[3] > 40 else 0 for p in image.getdata()]


def best_iou(a, b):
    best = 0.0
    for dy in range(-2, 3):
        for dx in range(-2, 3):
            inter = 0
            union = 0
            for y in range(16):
                for x in range(16):
                    av = a[y * 16 + x]
                    sy, sx = y + dy, x + dx
                    bv = b[sy * 16 + sx] if 0 <= sy < 16 and 0 <= sx < 16 else 0
                    if av or bv:
                        union += 1
                    if av and bv:
                        inter += 1
            if union:
                best = max(best, inter / union)
    return best


def main():
    manifest = json.loads(MANIFEST.read_text(encoding="utf-8"))
    entries = manifest["items"]
    ids = [e["id"] for e in entries]
    meta = {e["id"]: e for e in entries}
    masks = {i: mask(i) for i in ids}

    pairs = []
    for a, b in itertools.combinations(ids, 2):
        pairs.append((best_iou(masks[a], masks[b]), a, b))
    pairs.sort(reverse=True)

    cross = [p for p in pairs if meta[p[1]]["archetype"] != meta[p[2]]["archetype"]]
    same = [p for p in pairs if meta[p[1]]["archetype"] == meta[p[2]]["archetype"]]
    same = [p for p in same if meta[p[1]]["tier"] != meta[p[2]]["tier"]]

    print(f"jumlah item: {len(ids)}")
    print("\nPASANGAN PALING MIRIP (semua):")
    for value, a, b in pairs[:12]:
        kind = "arsip sama" if meta[a]["archetype"] == meta[b]["archetype"] else "arsip beda"
        print(f"  {value:.3f}  [{kind}]  {a} | {b}")
    print(f"\nmax lintas arketipe : {cross[0][0]:.3f}  {cross[0][1]} | {cross[0][2]}")
    print(f"max searsip beda mutu: {same[0][0]:.3f}  {same[0][1]} | {same[0][2]}")

    signatures = {}
    for entry in entries:
        signatures.setdefault(tuple(sorted(entry["palette"])), []).append(entry["id"])
    collisions = {k: v for k, v in signatures.items() if len(v) > 1}
    print(f"\ntabrakan signature palet: {collisions if collisions else 'tidak ada'}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
