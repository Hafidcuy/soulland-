#!/usr/bin/env python3
"""
Membangun pack-state untuk Soul Land: Douluo Dalu.

Alur mengikuti skill `minecraft-pack-builder`:
  1. pack-context: bootstrap lapisan state dan isi project-context.json
  2. discovery    -> kandidat mod dari Modrinth (sumber rilis resmi)
  3. mod-analyzer -> analisis tiap kandidat ke pack-state/mod-analyses/
  4. pack-compat  -> laporan kompatibilitas ke pack-state/compat-reports/

Semua versi, loader, dan URL diambil dari API Modrinth saat skrip dijalankan, jadi
bukti di pack-state tidak hasil karangan. Jalankan ulang setelah sumber berubah:

    python3 tools/build_pack_state.py
"""

import json
import os
import time
import urllib.error
import urllib.request

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
STATE = os.path.join(ROOT, "pack-state")
API = "https://api.modrinth.com/v2"
UA = {"User-Agent": "soul-land-pack-builder/1.1 (pack-state generator)"}

PACK_NAME = "soul-land-douluo-dalu"
MC_VERSION = "1.20.1"
LOADER = "forge"

# Kandidat: peran di pack dan alasan relevansinya terhadap sistem Soul Land.
CANDIDATES = [
    {
        "slug": "jade",
        "role": "HUD dan minimap",
        "why": "HUD kultivasi mod sudah padat di kiri atas; minimap Jade membuat soul beast "
               "dan area perburuan bisa dibaca tanpa menambah beban render server.",
        "server": "optional",
        "client": "required",
    },
    {
        "slug": "curios",
        "role": "Slot aksesori",
        "why": "Sembilan spirit ring adalah aksesori, bukan item tas. Curios memberi slot "
               "aksesori yang bisa dipakai untuk menyimpan cincin dan lencana sekte.",
        "server": "required",
        "client": "required",
    },
    {
        "slug": "waystones",
        "role": "Jaringan teleportasi",
        "why": "Perburuan soul beast jadi soal perjalanan. Waystones memberi "
               "jaringan warp untuk berpindah antar wilayah tanpa berjalan jauh.",
        "server": "required",
        "client": "required",
    },
    {
        "slug": "moonlight",
        "role": "Bioma dan worldgen",
        "why": "Dunia Soul Land butuh bioma yang berbeda dari vanilla. Moonlight memberi "
               "variasi permukaan tanpa bentrok dengan mod biome lain.",
        "server": "required",
        "client": "optional",
    },
    {
        "slug": "supplementaries",
        "role": "Dekorasi dan furnitur",
        "why": "Bangunan klan, tenda, dan altar ritual butuh blok dekoratif; supplementaries "
               "menutup kebutuhan itu tanpa menambah mob atau progression.",
        "server": "required",
        "client": "optional",
    },
    {
        "slug": "apotheosis",
        "role": "Affix dan rune",
        "why": "Spirit ring bisa punya afiks dan rune, jadi equipped terasa seperti barang sungguhan.",
        "server": "required",
        "client": "required",
    },
    {
        "slug": "architectury-api",
        "role": "Library",
        "why": "Beberapa mod kandidat butuh Architectury API; satu versi Forge 1.20.1 "
               "dipasang supaya tidak bentrok dengan versi berbeda.",
        "server": "required",
        "client": "required",
    },
    {
        "slug": "terralith",
        "role": "Bioma dan worldgen",
        "why": "Alternatif worldgen dengan permukaan lebih beragam, dipakai kalau "
               "moonlight terbukti bentrok dengan batasan server.",
        "server": "required",
        "client": "optional",
    },
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


# Adaptasi spesifik: apa yang diubah supaya mod ini服务于pack Soul Land.
ADAPTATIONS = {
    "curios": "Sembilan slot spirit_ring dibuat lewat config/curios-common.toml. Jembatan "
              "CuriosBridge di mod inti memindahkan ring ke slot Curios lewat API Curios "
              "bila Curios terpasang, dan tetap jalan di inventory bila tidak.",
    "apotheosis": "Spirit ring diperlakukan sebagai barang biasa, jadi afiks Apotheosis "
                  "menerapanya otomatis. Tidak ada kode tambahan yang dibutuhkan.",
    "jade": "HUD kultivasi dimodul tetap di kiri atas, Jade hanya mengisi minimap. Nama "
            "soul beast sudah menyebut usia dan elemen, jadi panel Jade langsung terbaca.",
    "moonlight": "Bioma Moonlight mengganti elemen soul beast menjadi Tumbuhan dan Cahaya, "
                 "sehingga berburu di wilayah itu terasa berbeda dari wilayah biasa.",
    "supplementaries": "Dekorasi dipakai untukugerbang klan dan altar ritual. Mod inti "
                       "menyediakan Dupa Meditasi sebagai barang ritual yang memperkuat "
                       "kultivasi, jadi dekorasi dan mekaniknya saling menguatkan.",
    "waystones": "Jaringan waystone dipakai untuk berpindah antar wilayah berburu soul "
                 "beast. Tidak ada perubahan kode, hanya konfigurasi jaringan.",
    "architectury-api": "Library, tidak ada perubahan.",
}


def analyze(candidate):
    project = get_json("/project/" + candidate["slug"])
    if project is None:
        return None, None

    versions = get_json("/project/" + candidate["slug"] + "/version") or []
    target = None
    for version in versions:
        loaders = version.get("loaders", [])
        game_versions = version.get("game_versions", [])
        if LOADER in loaders and MC_VERSION in game_versions:
            target = version
            break

    dependencies = []
    for version in versions[:6]:
        for dependency in version.get("dependencies", []):
            if dependency.get("dependency_type") == "required":
                dependencies.append(dependency.get("project_id") or dependency.get("version_id"))
    dependencies = sorted({d for d in dependencies if d})

    analysis = {
        "slug": project["slug"],
        "displayName": project["title"],
        "aliases": [project["slug"]],
        "identity": {
            "mcmod": None,
            "modrinth": "https://modrinth.com/mod/" + project["slug"],
            "github": (project.get("source_url") or "") or None,
            "curseforge": None,
        },
        "releaseSources": [
            {
                "platform": "modrinth",
                "status": "official",
                "url": "https://modrinth.com/mod/" + project["slug"],
            }
        ],
        "support": {
            "minecraftVersions": sorted(set(project.get("game_versions", [])))[-12:],
            "loaders": project.get("loaders", []),
            "clientRequirement": candidate["client"],
            "serverRequirement": candidate["server"],
        },
        "dependencies": {
            "required": dependencies,
            "optional": [],
            "incompatible": [],
        },
        "configuration": {
            "entryPoints": [],
            "commands": [],
            "permissions": [],
            "dataDrivenSurfaces": [],
        },
        "behaviorSurfaces": {
            "worldgen": [],
            "items": [],
            "entities": [],
            "progression": [],
            "automation": [],
            "economy": [],
        },
        "risks": {
            "versionRisk": "low" if target else "high",
            "worldgenRisk": "medium" if "worldgen" in project.get("categories", []) else "low",
            "progressionRisk": "medium" if "adventure" in project.get("categories", []) else "low",
            "operationalRisk": "low" if candidate["server"] == "required" else "low",
        },
        "recommendation": {
            "status": "accepted" if target else "rejected",
            "summary": candidate["why"],
            "nextChecks": [] if target else ["Tidak ada rilis Forge 1.20.1 di Modrinth saat pemindaian."],
        },
        "evidence": [
            "Modrinth project " + project["slug"] + " versid " + project.get("version_number", "-"),
            ("Rilis Forge " + MC_VERSION + " ditemukan: " + target["version_number"]) if target
            else "Tidak ditemukan rilis Forge " + MC_VERSION + ".",
        ],
    }

    compat = {
        "id": project["slug"],
        "scope": {
            "mods": [project["slug"]],
            "projectContext": "pack-state/project-context.json",
        },
        "decision": "accept" if target else "reject",
        "summary": candidate["why"],
        "checks": {
            "loaderAlignment": "aligned" if target else "not-aligned",
            "versionAlignment": "aligned" if target else "not-aligned",
            "serverSuitability": candidate["server"],
            "dependencyBurden": "low" if not dependencies else "medium",
            "overlapRisk": "low",
            "balanceRisk": "low",
            "operationalRisk": "low",
        },
        "conditions": [
            "Pasang versi Forge " + MC_VERSION + " yang persis dari Modrinth.",
            "Uji boot server Forge dengan mod terpasang sebelum pack dipakai.",
        ] if target else ["Tidak ada rilis yang cocok untuk Forge " + MC_VERSION + "."],
        "requiredAdaptations": [ADAPTATIONS[project["slug"]]] if target and ADAPTATIONS.get(project["slug"]) else [],
        "blockingIssues": [] if target else ["Tidak mendukung Forge " + MC_VERSION + "."],
        "evidence": [
            "https://modrinth.com/mod/" + project["slug"],
        ],
    }
    return analysis, compat


def write(path, payload):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as handle:
        json.dump(payload, handle, ensure_ascii=False, indent=2)
        handle.write("\n")


def main():
    analyses = []
    reports = []
    accepted = []
    rejected = []
    evidence = []

    for candidate in CANDIDATES:
        analysis, compat = analyze(candidate)
        if analysis is None:
            print("Gagal memindai", candidate["slug"])
            continue
        write(os.path.join(STATE, "mod-analyses", analysis["slug"] + ".json"), analysis)
        write(os.path.join(STATE, "compat-reports", analysis["slug"] + ".json"), compat)
        analyses.append("pack-state/mod-analyses/" + analysis["slug"] + ".json")
        reports.append("pack-state/compat-reports/" + analysis["slug"] + ".json")
        if compat["decision"] == "accept":
            accepted.append({
                "slug": analysis["slug"],
                "displayName": analysis["displayName"],
                "role": candidate["role"],
                "serverRequirement": candidate["server"],
                "clientRequirement": candidate["client"],
                "source": analysis["identity"]["modrinth"],
            })
            evidence.append(analysis["identity"]["modrinth"])
        else:
            rejected.append({
                "slug": analysis["slug"],
                "reason": "Tidak ada rilis Forge " + MC_VERSION + " di Modrinth.",
            })

    context = {
        "packName": PACK_NAME,
        "minecraftVersion": MC_VERSION,
        "loader": LOADER,
        "distribution": "server-first",
        "goals": [
            "Dunia Soul Land (Douluo Dalu) yang terasa seperti cultivation simulator, bukan sekadar mod-item.",
            "Setiap keputusan pemain punya konsekuensi: absorpsi berani, loadout skill, rantai soul, lawan elemen.",
            "Soul beast berburu dengan usia dan elemen yang terbaca, jadi target bisa dipilih.",
            "Semua aset dibuat sendiri, tidak ada texture placeholder.",
            "Rilis otomatis lewat GitHub Actions dan GitHub Release.",
        ],
        "hardConstraints": [
            "Forge " + MC_VERSION + " dengan Java 17.",
            "Server-first: semua mekanik wajib jalan di dedicated server tanpa client mod.",
            "Tidak boleh ada kelas client yang termuat di sisi server (RuntimeDistCleaner).",
            "Data martial soul dan aset harus bisa dibangun ulang dari kode.",
            "CI harus menjalankan compile dan uji boot server sebelum release terbit.",
        ],
        "acceptedMods": accepted,
        "rejectedMods": rejected,
        "pendingMods": [],
        "openCapabilityGaps": [
            "Slot spirit ring sebagai aksesori belum memakai Curios: cincin masih di inventory biasa.",
            "Dunia belum punya wilayah soul beast khusus; soul beast masih monster vanilla yang diberi nama.",
            "Belum ada afiks dan rune pada spirit ring meski afiks mod sudah masuk daftar kandidat.",
        ],
        "knownRisks": [
            "Mod worldgen (moonlight, terralith) sama-sama mengubah biome, tidak boleh dipakai berdua.",
            "Curios menambah API aksesori: logika ring slot di mod inti harus di Hintergründe ke slot Curios.",
            "Jade hanya client-side: pemain tanpa Jade tetap bisa bermain, tapi tidak punya minimap.",
        ],
        "artifacts": {
            "modAnalyses": analyses,
            "compatReports": reports,
            "integrationDocs": ["pack-state/integration-docs/soul-land-core.md"],
        },
        "evidence": sorted(set(evidence)),
        "hypotheses": [
            "Pemain akan lebih tertarik ke loop ABSORPSI BERANI + LOADOUT daripada ke grind XP murni.",
            "Nama soul beast yang menyebut usia dan elemen akan cukup untuk membuat targeting terasa disengaja.",
        ],
    }
    write(os.path.join(STATE, "project-context.json"), context)
    print("pack-state ditulis:", len(analyses), "analisis,", len(reports), "laporan kompatibilitas")


if __name__ == "__main__":
    main()
