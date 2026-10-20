#!/usr/bin/env python3
"""
Memeriksa bahwa tidak ada aset yang hilang.

Yang dicek:
  1. Setiap martial soul di data punya potret PNG.
  2. Setiap model item menyebut texture yang benar-benar ada.
  3. Setiap item yang terdaftar punya kunci bahasa di en_us dan id_id.
  4. Setiap texture partikel kustom ada.
  5. Setiap skill punya nama, deskripsi, dan bentuk efek yang sah.

Keluarannya laporan PASS/FAIL per poin. Jalankan:

    python3 tools/verify_assets.py
"""

import json
import os
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(ROOT, "src", "main", "resources", "assets", "soulland")
DATA = os.path.join(ROOT, "src", "main", "resources", "data", "soulland", "martial_souls.json")
JAVA = os.path.join(ROOT, "src", "main", "java", "com", "soulland")

VALID_TARGETS = {"SELF", "ENEMY_NEAREST", "ENEMY_AOE", "ENEMY_LINE", "ENEMY_CONE", "POINT_AOE", "ALLY_AURA"}
VALID_SHAPES = {"SPHERE", "LINE", "CONE", "RING", "PLANE", "COLUMN", "BURST"}


def check_portraits(souls):
    missing = []
    for soul in souls:
        path = os.path.join(ASSETS, "textures", "soul", soul["id"] + ".png")
        if not os.path.exists(path):
            missing.append(soul["id"])
    return missing, f"{len(souls)} potret soul"


def check_models():
    missing = []
    count = 0
    base = os.path.join(ASSETS, "models", "item")
    for name in sorted(os.listdir(base)):
        if not name.endswith(".json"):
            continue
        count += 1
        with open(os.path.join(base, name), encoding="utf-8") as handle:
            model = json.load(handle)
        for tex in model.get("textures", {}).values():
            if not tex.startswith("soulland:"):
                continue
            path = os.path.join(ASSETS, "textures", tex.split(":", 1)[1] + ".png")
            if not os.path.exists(path):
                missing.append(tex)
    return missing, f"{count} model item"


def check_lang():
    def load(path):
        with open(path, encoding="utf-8") as handle:
            return json.load(handle)

    en = load(os.path.join(ASSETS, "lang", "en_us.json"))
    idn = load(os.path.join(ASSETS, "lang", "id_id.json"))
    with open(os.path.join(JAVA, "item", "ModItems.java"), encoding="utf-8") as handle:
        source = handle.read()
    items = sorted(set(re.findall(r'ITEMS\.register\("([a-z0-9_]+)"', source)))
    missing = []
    for item in items:
        for table, name in ((en, "en_us"), (idn, "id_id")):
            if "item.soulland." + item not in table:
                missing.append(name + ":" + item)
    return missing, f"{len(items)} item terdaftar"


def check_particles():
    base = os.path.join(JAVA, "particle", "ModParticles.java")
    with open(base, encoding="utf-8") as handle:
        source = handle.read()
    ids = sorted(set(re.findall(r'=\s*register\("([a-z0-9_]+)"\)', source)))
    missing = []
    for particle in ids:
        path = os.path.join(ASSETS, "textures", "particle", particle + ".png")
        if not os.path.exists(path):
            missing.append(particle)
    return missing, f"{len(ids)} partikel kustom"


def check_skills(souls):
    problems = []
    total = 0
    for soul in souls:
        if len(soul["skills"]) != 5:
            problems.append(soul["id"] + ": jumlah skill " + str(len(soul["skills"])))
        for skill in soul["skills"]:
            total += 1
            if not skill.get("name") or not skill.get("desc"):
                problems.append(skill.get("id", "?") + ": nama/deskripsi kosong")
            if skill.get("target") not in VALID_TARGETS:
                problems.append(skill.get("id", "?") + ": target tidak sah")
            if skill.get("shape") not in VALID_SHAPES:
                problems.append(skill.get("id", "?") + ": bentuk efek tidak sah")
            if skill.get("rings") not in (1, 2, 3, 5, 7):
                problems.append(skill.get("id", "?") + ": syarat ring tidak sah")
    return problems, f"{total} skill"


def main():
    with open(DATA, encoding="utf-8") as handle:
        souls = json.load(handle)["souls"]

    checks = [
        ("Potret martial soul", check_portraits(souls)),
        ("Model item", check_models()),
        ("Bahasa item", check_lang()),
        ("Texture partikel", check_particles()),
        ("Kelengkapan skill", check_skills(souls)),
    ]

    print("=" * 62)
    print("VERIFIKASI ASET SOUL LAND: DOULUO DALU")
    print("=" * 62)
    failed = False
    for label, (problems, detail) in checks:
        if problems:
            failed = True
            print(f"FAIL  {label} ({detail})")
            for problem in problems[:10]:
                print("        -", problem)
            if len(problems) > 10:
                print(f"        ... dan {len(problems) - 10} masalah lain")
        else:
            print(f"PASS  {label} ({detail})")

    print()
    if failed:
        print("HASIL: ada aset yang belum lengkap.")
        return 1
    print("HASIL: tidak ada aset yang hilang. Semua soul punya potret, semua model punya texture,")
    print("       semua item punya nama, semua partikel punya texture, semua skill lengkap.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
