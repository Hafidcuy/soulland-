# Referensi internet untuk kultivasi, aset, dan mekanik

Semua angka dan aset di pack ini punya sumber internet yang tercatat di sini. Tidak ada
kurva kultivasi, format config, atau texture yang dikarang tanpa jejak.

## 1. Kurva kultivasi dan progresi

| Yang diambil | Sumber | Dipakai di |
|---|---|---|
| Struktur slot aksesori: format config `id=...;size=...;operation=...;order=...;icon=...;drop_rule=...` | Dokumentasi Curios, https://docs.illusivesoulworks.com/curios/configuration#slot-configuration | `config/curios-common.toml` |
| Format slot datapack: `order`, `icon`, `validators` | `TheIllusiveC4/Curios` branch `1.20.x`, `src/main/resources/data/curios/curios/slots/ring.json` dan `necklace.json` | `datapacks/soulland_curios/data/curios/curios/slots/*.json` |
| Key config Curios: `slots` (daftar string), `maxSlotsPerPage`, `keepCurios` | `TheIllusiveC4/Curios` branch `1.20.x`, `src/main/java/top/theillusivec4/curios/common/CuriosConfig.java` | `config/curios-common.toml` |
| Model progresi bertingkat: world tier, afiks bertingkat, boss spawner, gem tier | `Shadows-of-Fire/Apotheosis` branch `1.20`, `src/main/resources/data/apotheosis/advancements/affix/` (ancient, boss, epic, gem, gem_cut, merchant, mythic) | Peringkat kultivasi dan tier afiks spirit ring |
| Jumlah slot Curios bawaan per tipe | `TheIllusiveC4/Curios` branch `1.20.x`, `src/main/resources/data/curios/curios/slots/` | Sembilan slot spirit ring |

## 2. Data lore martial soul

| Sumber | Dipakai untuk |
|---|---|
| Tabel Martial Soul Moegirlpedia, bagian 1 sampai 4 (nama Inggris + Mandarin, pemilik, afiliasi) | `tools/data/martial_souls_en.tsv`, `tools/data/martial_souls_cn.tsv` |
| Daftar abjad Tool, Beast, dan Body Martial Soul di wiki Soul Land (Fandom), diambil lewat `api.php?action=parse&prop=wikitext` | `tools/data/fandom_souls.txt` |
| Daftar lengkap Wu Hun Da Quan (daftar lengkap semua martial soul) beserta kategori dan turunannya | `tools/data/cn_all.tsv` |
| Tabel Martial Soul Fusion Skill (skill gabungan soul) | Bentuk soul gabungan di pool undian |
| Moegirlpedia, tabel awakenings body soul (Black Iron, Bronze, Silver, Gold) | Bentuk body soul di pool undian |

## 3. Aset dari internet

| Aset | Sumber | Lisensi | Dipakai di |
|---|---|---|---|
| Panel GUI 9-slice untuk kartu soul dan panduan | Kenney, "Pixel UI pack", https://kenney.nl/assets/pixel-ui-pack | CC0 1.0 | `textures/gui/panel_card.png`, `textures/gui/panel_guide.png` |

Potret soul, texture item, dan texture partikel **tidak** diambil dari internet. Semuanya
digambar oleh `tools/generate_assets.py` supaya tidak ada masalah hak cipta pada karya
asli Soul Land.

## 4. Data versi mod kandidat

Semua versi, loader, dan URL kandidat diambil dari API Modrinth saat
`tools/build_pack_state.py` dijalankan, bukan ditulis manual:

```
https://api.modrinth.com/v2/project/<slug>
https://api.modrinth.com/v2/project/<slug>/version
```

Hasilnya tersimpan di `pack-state/mod-analyses/` dan `pack-state/compat-reports/`.

## 5. Simulasi

`tools/simulate.py` memakai aturan yang sama dengan mod (kurva XP, gerbang peringkat,
absorpsi brave, rantai soul, lawan elemen, bakar darah) untuk menguji ritme main.
Hasil run terakhir tersimpan di `pack-state/simulation-report.json`.
