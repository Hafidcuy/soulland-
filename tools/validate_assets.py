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
7. v1.2B: manifest kedua (ring emas, station alchemy, pil fondasi) ikut
   diperiksa, dan keunikan palet serta siluet diuji lintas kedua manifest.
8. v1.5: ikon True Body dan Domain untuk 43 soul ikut diperiksa: file
   teksturnya ada, palet inti tiap ikon unik, siluet antar ikon tidak
   kembar, dan tiap soul punya tepat satu ikon True Body dan satu ikon
   Domain. Ikon v1.5 adalah aset GUI, bukan item, jadi siluetnya diuji
   di dalam kelompoknya sendiri dan tidak dibandingkan dengan siluet item.

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
MANIFESTS = [
    ROOT / "docs/v1.2a-asset-manifest.json",
    ROOT / "docs/v1.2b-asset-manifest.json",
    ROOT / "docs/v1.3-asset-manifest.json",
    ROOT / "docs/v1.4-asset-manifest.json",
]
# Ikon True Body dan Domain v1.5. Aset GUI, jadi punya kelompok pemeriksaan
# sendiri dan tidak ikut dibandingkan dengan siluet item.
V15_MANIFEST = ROOT / "docs/v1.5-asset-manifest.json"
RECIPE_DIR = ROOT / "src/main/resources/data/soulland/recipes"

# Batas kemiripan siluet dua item. Diukur dengan IoU mask alpha setelah
# pergeseran terbaik sampai 2 piksel. Semua pasangan harus di bawah batas ini
# supaya tidak ada dua item yang terlihat sebagai obyek yang sama.
MAX_SILHOUETTE_IOU = 0.70

# Kelas yang hanya boleh dimuat di sisi client.
CLIENT_ONLY = {
    "com.soulland.client.ClientCombatData",
    "com.soulland.client.ClientCombatHud",
    "com.soulland.client.ClientGuiHooks",
    "com.soulland.client.ClientManifestations",
    "com.soulland.client.ClientHaloData",
    "com.soulland.client.ClientCultivationData",
    "com.soulland.client.KeyBindings",
    "com.soulland.client.ClientEvents",
    "com.soulland.client.gui.AwakeningScreen",
    "com.soulland.client.gui.LanguageSelectScreen",
    "com.soulland.client.gui.AlchemyScreen",
}

KEY_PATTERN = re.compile(
    r'"((?:item|itemGroup|key|gui|soul|skill|tooltip|tier|message|rank|book|guide|hint)'
    r'\.[a-z0-9_]+(?:\.[a-z0-9_]+)*)"'
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
        r'register\(new MartialSoul\(\s*"([^"]+)",\s*"([^"]*)",\s*"([^"]*)",\s*"([^"]*)",\s*"([^"]*)",\s*"([^"]*)",\s*"([^"]*)",\s*"([^"]*)"',
        soul_file,
    )
    for sid, _, _, _, _, stype, _, rarity in souls:
        literal_keys.add(f"soul.soulland.{sid}.name")
        literal_keys.add(f"soul.soulland.{sid}.description")
        literal_keys.add(f"soul.soulland.owner.{sid}")
        literal_keys.add(f"soul.soulland.type.{re.sub(r'[^a-z0-9]+', '_', stype.lower()).strip('_')}")
        literal_keys.add(f"soul.soulland.rarity.{rarity.lower()}")
        literal_keys.add(f"item.soulland.manifestation_{sid}")
        literal_keys.add(f"soul.soulland.truebody.{sid}.name")
        literal_keys.add(f"soul.soulland.truebody.{sid}.description")
        literal_keys.add(f"soul.soulland.domain.{sid}.name")
        literal_keys.add(f"soul.soulland.domain.{sid}.description")
    for skill_id in re.findall(r'\bskill\("([^"]+)",', soul_file):
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

    # Kunci v1.3 dirakit dari enum tier dan slot tulang spirit.
    bone_tier_file = (JAVA / "bone/SpiritBoneTier.java").read_text(encoding="utf-8")
    bone_slot_file = (JAVA / "bone/SpiritBoneSlot.java").read_text(encoding="utf-8")
    bone_tiers = re.findall(r'\("([a-z0-9_]+)",\s*\d+,', bone_tier_file)
    bone_slots = re.findall(r'\("([a-z0-9_]+)",\s*[\d.]+f', bone_slot_file)
    for tier_id in bone_tiers:
        literal_keys.add(f"bone.soulland.tier.{tier_id}")
    for slot_id in bone_slots:
        literal_keys.add(f"bone.soulland.slot.{slot_id}")
    for tier_id in bone_tiers:
        for slot_id in bone_slots:
            literal_keys.add(f"item.soulland.spirit_bone_{tier_id}_{slot_id}")
    literal_keys.add("itemGroup.soulland.spirit_bone")

    # Nama peringkat kultivasi dirakit dari tabel rank.
    rank_file = (JAVA / "CultivationRanks.java").read_text(encoding="utf-8")
    rank_block = re.search(r'RANKS\s*=\s*\{(.*?)\}', rank_file, re.S)
    if rank_block:
        for index in range(len(re.findall(r'"[^"]+"', rank_block.group(1)))):
            literal_keys.add(f"rank.soulland.{index}")

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


# ------------------------------------------------------------------ manifest

def load_manifests():
    """Muat semua manifest aset. Setiap manifest adalah dict id -> entri."""
    manifests = []
    for path in MANIFESTS:
        if not path.exists():
            fail(f"manifest aset hilang: {path.relative_to(ROOT)}")
            continue
        try:
            items = json.loads(path.read_text(encoding="utf-8"))["items"]
        except (json.JSONDecodeError, KeyError) as exc:
            fail(f"manifest aset rusak: {exc}")
            continue
        manifests.append((path.name, items))
    return manifests


def texture_path(entry):
    """Lokasi texture ikon sebuah entri manifest."""
    return ASSETS / "textures/item" / f"{entry['id']}.png"


def check_manifest_items(manifests, langs):
    """Setiap item baru punya texture, model, dan terjemahan di dua bahasa."""
    for name, items in manifests:
        for entry in items:
            item_id = entry["id"]
            texture = texture_path(entry)
            model = ASSETS / "models/item" / f"{item_id}.json"
            if not texture.exists():
                fail(f"texture hilang untuk {item_id}")
            if not model.exists():
                fail(f"model hilang untuk {item_id}")
            if entry.get("kind") == "block":
                for extra in (ASSETS / "textures/block" / f"{item_id}.png",
                              ASSETS / "models/block" / f"{item_id}.json",
                              ASSETS / "blockstates" / f"{item_id}.json"):
                    if not extra.exists():
                        fail(f"berkas blok hilang untuk {item_id}: {extra.relative_to(ROOT)}")
            for lang_name, lang in langs.items():
                key = f"item.soulland.{item_id}"
                if key not in lang:
                    fail(f"{lang_name}: kunci hilang {key}")
        print(f"manifest {name}: {len(items)} item diperiksa")


def check_palette_uniqueness(manifests):
    """Tiap item punya palet inti sendiri, tidak ada dua item dengan palet sama."""
    signatures = {}
    for _, items in manifests:
        for entry in items:
            signatures.setdefault(tuple(sorted(entry["palette"])), []).append(entry["id"])
    for palette, ids in signatures.items():
        if len(ids) > 1:
            fail(f"palet sama dipakai lebih dari satu item: {ids}")
    total = sum(len(items) for _, items in manifests)
    print(f"signature palet unik: {len(signatures)} dari {total} item")


def alpha_mask(entry):
    image = Image.open(texture_path(entry)).convert("RGBA")
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


def check_silhouette_uniqueness(manifests):
    """Tidak ada dua item dengan siluet yang sama.

    Pengecualian satu: tulang spirit memakai satu siluet per slot tubuh dan
    membedakan tier lewat bahan tulangnya, bukan lewat bentuk. Jadi dua item
    dengan slot yang sama (tier berbeda) memang sengaja berbagi siluet, dan
    yang diuji siluetnya adalah pasangan slot yang berbeda.
    """
    entries = [entry for _, items in manifests for entry in items]
    masks = {entry["id"]: alpha_mask(entry) for entry in entries}
    worst = (0.0, None, None)
    for first, second in itertools.combinations(entries, 2):
        if first.get("slot") is not None and first.get("slot") == second.get("slot"):
            continue
        value = best_iou(masks[first["id"]], masks[second["id"]])
        if value > worst[0]:
            worst = (value, first["id"], second["id"])
        if value >= MAX_SILHOUETTE_IOU:
            fail(f"siluet terlalu mirip ({value:.3f} >= {MAX_SILHOUETTE_IOU}): "
                 f"{first['id']} | {second['id']}")
    print(f"kemiripan siluet tertinggi: {worst[0]:.3f} ({worst[1]} | {worst[2]})")


# ------------------------------------------------------------------ ikon v1.5

def v15_icon_path(entry):
    """Lokasi texture ikon GUI v1.5."""
    return ASSETS / "textures" / entry["texture"]


def v15_mask(entry):
    """Mask alpha ikon GUI v1.5."""
    image = Image.open(v15_icon_path(entry)).convert("RGBA")
    return [1 if pixel[3] > 40 else 0 for pixel in image.getdata()]


def registry_soul_ids():
    """Id soul yang terdaftar di registry Java."""
    source = (JAVA / "soul/MartialSouls.java").read_text(encoding="utf-8")
    return re.findall(r'register\(new MartialSoul\(\s*"([^"]+)"', source)


def check_v15_icons():
    """Ikon True Body dan Domain: file ada, palet unik, siluet tidak kembar, cakupan 43 soul."""
    if not V15_MANIFEST.exists():
        fail(f"manifest ikon v1.5 hilang: {V15_MANIFEST.relative_to(ROOT)}")
        return
    try:
        icons = json.loads(V15_MANIFEST.read_text(encoding="utf-8"))["icons"]
    except (json.JSONDecodeError, KeyError) as exc:
        fail(f"manifest ikon v1.5 rusak: {exc}")
        return

    for entry in icons:
        if not v15_icon_path(entry).exists():
            fail(f"texture ikon hilang untuk {entry['id']}")

    signatures = {}
    for entry in icons:
        signatures.setdefault(tuple(sorted(entry["palette"])), []).append(entry["id"])
    for _, ids in signatures.items():
        if len(ids) > 1:
            fail(f"palet sama dipakai lebih dari satu ikon v1.5: {ids}")
    print(f"ikon v1.5: {len(icons)} ikon, {len(signatures)} palet unik")

    masks = {entry["id"]: v15_mask(entry) for entry in icons}
    worst = (0.0, None, None)
    for first, second in itertools.combinations(icons, 2):
        value = best_iou(masks[first["id"]], masks[second["id"]])
        if value > worst[0]:
            worst = (value, first["id"], second["id"])
        if value >= MAX_SILHOUETTE_IOU:
            fail(f"siluet ikon v1.5 terlalu mirip ({value:.3f} >= {MAX_SILHOUETTE_IOU}): "
                 f"{first['id']} | {second['id']}")
    print(f"kemiripan siluet ikon v1.5 tertinggi: {worst[0]:.3f} ({worst[1]} | {worst[2]})")

    souls = registry_soul_ids()
    kinds = {}
    for entry in icons:
        kinds.setdefault(entry["soul"], set()).add(entry["kind"])
    for soul in souls:
        found = kinds.get(soul, set())
        if found != {"true_body", "domain"}:
            fail(f"soul {soul} tidak punya tepat satu ikon True Body dan satu ikon Domain: {sorted(found)}")
    for soul in kinds:
        if soul not in souls:
            fail(f"ikon v1.5 untuk soul yang tidak terdaftar: {soul}")
    print(f"cakupan ikon v1.5: {len(souls)} soul tercover")


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


def check_recipes(manifests):
    """Semua resep harus menunjuk item yang benar-benar ada di mod."""
    known = {entry["id"] for _, items in manifests for entry in items}
    known.update(registered_item_ids())
    known.update({"manifestation_hoe", "spirit_ring_100", "spirit_ring_1000",
                   "spirit_ring_10000", "spirit_ring_100000", "spirit_ring_1000000"})
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

    manifests = load_manifests()
    if manifests:
        check_manifest_items(manifests, langs)
        check_palette_uniqueness(manifests)
        check_silhouette_uniqueness(manifests)
        check_recipes(manifests)

    check_v15_icons()

    if problems:
        print(f"\n{len(problems)} masalah:")
        for problem in problems:
            print(f"  - {problem}")
        return 1
    print("\nSemua pemeriksaan lolos: 0 referensi hilang.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
