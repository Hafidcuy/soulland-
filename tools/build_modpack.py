#!/usr/bin/env python3
"""
Membangun paket modpack Soul Land: Douluo Dalu.

Hasilnya:
  modpack/modrinth.index.json   index format Modrinth (dipakai launcher Modrinth)
  modpack/config/               config yang ikut paket
  modpack/instance/             setelan awal instance
  modpack/MODS.md               daftar mod, alasan, dan versi persisnya

Data versi diambil dari API Modrinth saat skrip ini dijalankan, jadi tidak ada versi
yang dikarang.

    python3 tools/build_modpack.py
"""

import json
import os
import time
import urllib.error
import urllib.request

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
PACK = os.path.join(ROOT, "modpack")
API = "https://api.modrinth.com/v2"
UA = {"User-Agent": "soul-land-modpack-builder/1.2 (pack generator)"}
MC_VERSION = "1.20.1"
LOADER = "forge"

# slug Modrinth, peran di pack, dan alasannya
MODS = [
    ("curios", "Slot aksesori",
     "Sembilan spirit ring adalah aksesori, bukan isi tas. Curios memberi slot ring khusus "
     "sehingga cincin tidak memenuhi tas, dan menyisakan ruang untuk bahan halus."),
    ("jade", "HUD, minimap, dan info pemain",
     "HUD kultivasi mod inti sudah occupies kiri atas, jadi Jade dipasang hanya untuk minimap "
     "dan informasi target. Minimap penting untuk berburu soul beast yang-tier-nya jelas."),
    ("waystones", "Teleportasi antar wilayah",
     "Perburuan soul beast jadi soal perjalanan, bukan jarak berjalan. Waystones memberi "
     "jaringan warp yang bisa dibatasi per wilayah."),
    ("moonlight", "Bioma dan worldgen",
     "Variasi permukaan untuk Benua Douluo: bukit, rawa, dan hutan yang berbeda dari vanilla, "
     "supaya berburu terasa seperti menjelajah wilayah."),
    ("supplementaries", "Dekorasi dan furnitur",
     "Bangunan klan, tenda, altar, dan perapian ritual butuh blok dekoratif. Supplementaries "
     "tidak menambah progression, jadi aman digabung."),
    ("apotheosis", "Affix dan rune",
     "Spirit ring bisa punya afiks dan rune. Ini yang membuat cincin terasa sebagai barang "
     "yang diburu, bukan sekadar angka."),
    ("architectury-api", "Library",
     "Beberapa mod di pack ini menarik API-nya lewat Architectury. Satu versi Forge 1.20.1 "
     "saja yang dipasang supaya tidak bentrok."),
]


def get_json(path):
    request = urllib.request.Request(API + path, headers=UA)
    for attempt in range(3):
        try:
            with urllib.request.urlopen(request, timeout=25) as response:
                return json.loads(response.read().decode("utf-8"))
        except (urllib.error.URLError, urllib.error.HTTPError, TimeoutError, json.JSONDecodeError):
            time.sleep(1.5 * (attempt + 1))
    return None


def find_version(slug):
    versions = get_json("/project/" + slug + "/version")
    if not versions:
        return None, None
    for version in versions:
        if LOADER in version.get("loaders", []) and MC_VERSION in version.get("game_versions", []):
            return version["id"], version["version_number"]
    return None, None


def main():
    os.makedirs(PACK, exist_ok=True)

    files = []
    dependencies = {}
    lines = []

    for slug, role, why in MODS:
        project = get_json("/project/" + slug)
        version_id, version_number = find_version(slug)
        if not project or not version_id:
            lines.append(f"- {slug}: TIDAK DITEMUKAN untuk Forge {MC_VERSION}, dilewati.")
            continue
        dependencies[slug] = {"version_id": version_id, "project_id": project["id"]}
        download = get_json("/version/" + version_id)
        url = ""
        sha1 = ""
        size = 0
        if download and download.get("files"):
            primary = download["files"][0]
            url = primary.get("url", "")
            sha1 = primary.get("hashes", {}).get("sha1", "")
            size = primary.get("size", 0)
        filename = os.path.basename(url.split("?")[0]) if url else project["slug"] + "-" + version_number + ".jar"
        files.append({
            "path": "mods/" + filename,
            "hashes": {"sha1": sha1},
            "downloads": [url] if url else [],
            "fileSize": size,
            "env": {"client": "required", "server": "required"},
        })
        lines.append(f"| {project['title']} | {role} | {version_number} | {why} |")
        print("terambil:", slug, version_number)

    # Mod inti: jar-nya dibangun CI, jadi index menunjuk ke file lokal.
    files.append({"path": "mods/soulland-1.2.0.jar", "hashes": {"sha1": "", "sha512": ""},
                  "downloads": [], "fileSize": 0, "env": {"client": "required", "server": "required"}})

    index = {
        "formatVersion": 1,
        "game": "minecraft",
        "versionId": "1.2.0",
        "name": "Soul Land: Douluo Dalu",
        "summary": "Cultivation simulator Benua Douluo: 1.995 martial soul, 9.975 skill, "
                   "spirit ring, sekte, evolusi soul, dan companion martial soul.",
        "files": files,
        "dependencies": dependencies,
    }
    with open(os.path.join(PACK, "modrinth.index.json"), "w", encoding="utf-8") as handle:
        json.dump(index, handle, ensure_ascii=False, indent=2)
        handle.write("\n")

    header = [
        "# Modpack: Soul Land: Douluo Dalu",
        "",
        "Paket ini berisi mod inti `soulland` (kultivasi Benua Douluo) plus mod pendukung",
        "yang_role-nya saling melengkapi. Semua versi diambil dari Modrinth saat",
        "`tools/build_modpack.py` dijalankan, jadi angkanya selalu terverifikasi.",
        "",
        "| Mod | Peran | Versi | Kenapa ada |",
        "|---|---|---|---|",
    ]
    with open(os.path.join(PACK, "MODS.md"), "w", encoding="utf-8") as handle:
        handle.write("\n".join(header + lines) + "\n")

    os.makedirs(os.path.join(PACK, "config"), exist_ok=True)
    curios_source = os.path.join(ROOT, "config", "curios-common.toml")
    if os.path.exists(curios_source):
        with open(curios_source, encoding="utf-8") as src, \
                open(os.path.join(PACK, "config", "curios-common.toml"), "w", encoding="utf-8") as dst:
            dst.write(src.read())

    instance = os.path.join(PACK, "instance", "options.txt")
    os.makedirs(os.path.dirname(instance), exist_ok=True)
    with open(instance, "w", encoding="utf-8") as handle:
        handle.write("\n".join([
            "fov:0.0",
            "renderDistance:10",
            "simulationDistance:8",
            "graphicsMode:1",
            "particles:2",
            "ao:true",
            "entityShadows:true",
            "maxFps:120",
            "guiScale:3",
            "soundCategory_master:0.6",
        ]) + "\n")

    print("modpack ditulis ke", os.path.relpath(PACK, ROOT))


if __name__ == "__main__":
    main()
