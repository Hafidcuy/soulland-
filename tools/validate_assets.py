#!/usr/bin/env python3
"""Validasi aset dan terjemahan mod Soul Land.

Pemeriksaan:
1. Setiap model JSON merujuk tekstur yang benar-benar ada.
2. Setiap kunci bahasa yang dipakai kode ada di en_us.json DAN id_id.json.
3. Setiap item terdaftar punya kunci terjemahan, model, dan (bila perlu) tekstur.
4. Setiap soul dan skill di registry punya kunci nama + deskripsi di dua bahasa.
5. Tidak ada kelas client-only yang dirujuk dari kode common.

Keluar dengan kode 1 bila ada referensi yang hilang.
Jalankan: python3 tools/validate_assets.py
"""
import json
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
ASSETS = ROOT / "src/main/resources/assets/soulland"
JAVA = ROOT / "src/main/java/com/soulland"
LANG_FILES = [ASSETS / "lang/en_us.json", ASSETS / "lang/id_id.json"]

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
    r'"((?:item|itemGroup|key|gui|soul|skill)\.[a-z0-9_]+(?:\.[a-z0-9_]+)*)"'
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


def main():
    langs = load_lang()
    check_models(langs)
    check_lang_keys(langs)
    check_client_isolation()

    if problems:
        print(f"\n{len(problems)} masalah:")
        for problem in problems:
            print(f"  - {problem}")
        return 1
    print("\nSemua pemeriksaan lolos: 0 referensi hilang.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
