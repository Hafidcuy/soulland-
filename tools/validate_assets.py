#!/usr/bin/env python3
"""Validasi aset dan terjemahan mod Soul Land.

Pemeriksaan:
1. Setiap model JSON merujuk tekstur yang benar-benar ada.
2. Setiap kunci bahasa yang dipakai kode ada di en_us.json DAN id_id.json.
3. Setiap item terdaftar punya kunci terjemahan, model, dan (bila perlu) tekstur.
4. Setiap soul dan skill di registry punya kunci nama + deskripsi di dua bahasa.
5. Tidak ada kelas client-only yang dirujuk dari kode common.
6. v1.2A: manifest aset lengkap, signature palet unik, siluet antar item
   tidak kembar, dan semua resep menunjuk item yang benar-benar ada.

Keluar dengan kode 1 bila ada referensi yang hilang.
Jalankan: python3 tools/validate_assets.py
"""
import itertools
import json
import pathlib
import re
import sys

from PIL import Image

ROOT = pathlib.Path(__file__).resolve().parent.parent
ASSETS = ROOT / "src/main/resources/assets/soulland"
JAVA = ROOT / "src/main/java/com/soulland"
LANG_FILES = [ASSETS / "lang/en_us.json", ASSETS / "lang/id_id.json"]
MANIFEST = ROOT / "docs/v1.2a-asset-manifest.json"
RECIPE_DIR = ROOT / "src/main/resources/data/soulland/recipes"

# Batas kemiripan siluet dua item. Diukur dengan IoU mask alpha setelah
# pergeseran terbaik sampai 2 piksel. Semua pasangan harus di bawah batas ini
# supaya tidak ada dua item yang terlihat sebagai obyek yang sama.
MAX_SILHOUETTE_IOU = 0.70

# Kelas yang hanya boleh dimuat di sisi client.
CLIENT_ONLY = {
    "com.soulland.client.ClientGuiHooks",
    "com.soulland.client.ClientManifestations",
    "com.soulland.client.ClientHaloData",
    "com.soulland.client.ClientCultivationData",
    "com.soulland.client.KeyBindings",
    "com.soulland.client.ClientEvents",
    "com.soulland.client.gui.AwakeningScreen",
}

KEY_PATTERN = re.compile(
    r'"((?:item|itemGroup|key|gui|soul|skill|tooltip|tier|message)\.[a-z0-9_]+(?:\.[a-z0-9_]+)*)"'
)

problems = []


def fail(message):
    problems.append(message)


def load_lang():
    langs = {}
    for path in LANG_FILES:
        if not path.exists():
            fail(f"berkas bahasa hilang: {path.relative_to(ROOT)}")
            continue
        try:
            langs[path.name] = json.loads(path.read_text(encoding="utf-8"))
        except json.JSONDecodeError as exc:
            fail(f"{path.name} bukan JSON valid: {exc}")
    return langs


def check_models(langs):
    for model in sorted(ASSETS.glob("models/**/*.json")):
        try:
            data = json.loads(model.read_text(encoding="utf-8"))
        except json.JSONDecodeError as exc:
            fail(f"{model.relative_to(ROOT)} bukan JSON valid: {exc}")
            continue
        for ref in data.get("textures", {}).values():
            if ref.startswith("#"):
                continue
            namespace, _, path = ref.partition(":")
            if not path:
                namespace, path = "soulland", namespace
            texture = ASSETS / "textures" / f"{path}.png"
            if not texture.exists():
                fail(f"{model.relative_to(ROOT)} merujuk tekster tidak ada: {ref}")
    for name, lang in langs.items():
        for key in lang:
            if not key.startswith("item.soulland."):
                continue
            item = key[len("item.soulland."):]
            model = ASSETS / "models/item" / f"{item}.json"
            if not model.exists():
                fail(f"{name}: {key} tidak punya model item")


def java_sources():
    return sorted(JAVA.rglob("*.java"))


def registered_item_ids():
    """Id item yang didaftarkan di ModItems, dibaca langsung dari kode."""
    source = (JAVA / "item/ModItems.java").read_text(encoding="utf-8")
    return set(re.findall(r'ITEMS\.register\(\s*"([^"]+)"', source))


def check_lang_keys(langs):
    literal_keys = set()
    for source in java_sources():
        text = source.read_text(encoding="utf-8")
        for key in KEY_PATTERN.findall(text):
            literal_keys.add(key)

    # Kunci yang dirakit dari data registry.
    soul_file = (JAVA / "soul/MartialSouls.java").read_text(encoding="utf-8")
    souls = re.findall(
        r'register\(new MartialSoul\(\s*"([^"]+)",\s*"([^"]*)",\s*"([^"]*)",\s*"([^"]*)",\s*"([^"]*)"',
        soul_file,
    )
    for sid, _, _, stype, rarity in souls:
        literal_keys.add(f"soul.soulland.{sid}.name")
        literal_keys.add(f"soul.soulland.{sid}.description")
        literal_keys.add(f"soul.soulland.owner.{sid}")
        literal_keys.add(f"soul.soulland.type.{re.sub(r'[^a-z0-9]+', '_', stype.lower()).strip('_')}")
        literal_keys.add(f"soul.soulland.rarity.{rarity.lower()}")
        literal_keys.add(f"item.soulland.manifestation_{sid}")
    for skill_id in re.findall(r'new SkillDef\("([^"]+)"', soul_file):
        literal_keys.add(f"skill.soulland.{skill_id}.name")
        literal_keys.add(f"skill.soulland.{skill_id}.description")

    # Mutu material dan tooltip tool dirakit dari enum.
    tier_file = (JAVA / "item/SpiritToolTier.java").read_text(encoding="utf-8")
    for tier_id in re.findall(r'\("([a-z_]+)",\s*\d+,\s*[\d.]+f', tier_file):
        literal_keys.add(f"tier.soulland.{tier_id}")
    for key in ("soul", "tier", "damage", "mining", "battery", "charge_hint",
                "synergy_on", "synergy_off"):
        literal_keys.add(f"tooltip.soulland.{key}")
    for key in ("consumed", "charge_no_battery", "charge_no_energy", "synergy_active"):
        literal_keys.add(f"message.soulland.{key}")
    literal_keys.add("itemGroup.soulland.spirit_gear")

    for name, lang in langs.items():
        missing = sorted(key for key in literal_keys if key not in lang)
        for key in missing:
            fail(f"{name}: kunci hilang {key}")
        print(f"{name}: {len(lang)} kunci, {len(literal_keys)} kunci dipakai kode")


def check_client_isolation():
    for source in java_sources():
        relative = source.relative_to(ROOT)
        if source.relative_to(JAVA).parts[0] == "client":
            continue
        text = source.read_text(encoding="utf-8")
        for match in re.finditer(r"import\s+(com\.soulland\.client\.[\w.]+);", text):
            target = match.group(1)
            if target in CLIENT_ONLY:
                fail(f"{relative} mengimpor kelas client-only {target}")


# ------------------------------------------------------------------ v1.2A

def load_manifest():
    if not MANIFEST.exists():
        fail(f"manifest aset hilang: {MANIFEST.relative_to(ROOT)}")
        return []
    try:
        return json.loads(MANIFEST.read_text(encoding="utf-8"))["items"]
    except (json.JSONDecodeError, KeyError) as exc:
        fail(f"manifest aset rusak: {exc}")
        return []


def check_manifest_items(manifest, langs):
    """Setiap item baru punya texture, model, dan terjemahan di dua bahasa."""
    for entry in manifest:
        item_id = entry["id"]
        texture = ASSETS / "textures/item" / f"{item_id}.png"
        model = ASSETS / "models/item" / f"{item_id}.json"
        if not texture.exists():
            fail(f"texture hilang untuk {item_id}")
        if not model.exists():
            fail(f"model hilang untuk {item_id}")
        for name, lang in langs.items():
            key = f"item.soulland.{item_id}"
            if key not in lang:
                fail(f"{name}: kunci hilang {key}")


def check_palette_uniqueness(manifest):
    """Tiap item punya palet inti sendiri, tidak ada dua item dengan palet sama."""
    signatures = {}
    for entry in manifest:
        signatures.setdefault(tuple(sorted(entry["palette"])), []).append(entry["id"])
    for palette, ids in signatures.items():
        if len(ids) > 1:
            fail(f"palet sama dipakai lebih dari satu item: {ids}")
    print(f"signature palet unik: {len(signatures)} dari {len(manifest)} item")


def alpha_mask(item_id):
    image = Image.open(ASSETS / "textures/item" / f"{item_id}.png").convert("RGBA")
    return [1 if pixel[3] > 40 else 0 for pixel in image.getdata()]


def best_iou(first, second):
    """IoU tertinggi dari mask alpha dengan pergeseran -2..2 piksel."""
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


def check_silhouette_uniqueness(manifest):
    """Tidak ada dua item dengan siluet yang sama."""
    ids = [entry["id"] for entry in manifest]
    masks = {item_id: alpha_mask(item_id) for item_id in ids}
    worst = (0.0, None, None)
    for first, second in itertools.combinations(ids, 2):
        value = best_iou(masks[first], masks[second])
        if value > worst[0]:
            worst = (value, first, second)
        if value >= MAX_SILHOUETTE_IOU:
            fail(f"siluet terlalu mirip ({value:.3f} >= {MAX_SILHOUETTE_IOU}): {first} | {second}")
    print(f"kemiripan siluet tertinggi: {worst[0]:.3f} ({worst[1]} | {worst[2]})")


def collect_recipe_items(payload):
    """Semua id item yang disebut sebuah resep."""
    found = []

    def walk(node):
        if isinstance(node, dict):
            if "item" in node and isinstance(node["item"], str):
                found.append(node["item"])
            for value in node.values():
                walk(value)
        elif isinstance(node, list):
            for value in node:
                walk(value)
        elif isinstance(node, str) and ":" in node and not node.startswith("minecraft:"):
            found.append(node)

    walk(payload)
    return found


def check_recipes(manifest):
    """Semua resep harus menunjuk item yang benar-benar ada di mod."""
    known = {entry["id"] for entry in manifest}
    known.update(registered_item_ids())
    known.update({"manifestation_hoe", "spirit_ring_100", "spirit_ring_1000",
                   "spirit_ring_10000", "spirit_ring_100000"})
    if not RECIPE_DIR.exists():
        fail("folder data/soulland/recipes tidak ada")
        return
    count = 0
    for path in sorted(RECIPE_DIR.glob("*.json")):
        try:
            payload = json.loads(path.read_text(encoding="utf-8"))
        except json.JSONDecodeError as exc:
            fail(f"{path.relative_to(ROOT)} bukan JSON valid: {exc}")
            continue
        count += 1
        if "type" not in payload:
            fail(f"{path.relative_to(ROOT)} tidak punya type")
        for reference in collect_recipe_items(payload):
            namespace, _, name = reference.partition(":")
            if namespace != "soulland":
                continue
            if name not in known:
                fail(f"{path.relative_to(ROOT)} menunjuk item mod yang tidak ada: {reference}")
    print(f"resep diperiksa: {count} berkas")


def main():
    langs = load_lang()
    check_models(langs)
    check_lang_keys(langs)
    check_client_isolation()

    manifest = load_manifest()
    if manifest:
        check_manifest_items(manifest, langs)
        check_palette_uniqueness(manifest)
        check_silhouette_uniqueness(manifest)
        check_recipes(manifest)

    if problems:
        print(f"\n{len(problems)} masalah:")
        for problem in problems:
            print(f"  - {problem}")
        return 1
    print("\nSemua pemeriksaan lolos: 0 referensi hilang.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
