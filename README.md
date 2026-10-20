# Soul Land: Douluo Dalu — Mod Minecraft Java (Forge 1.20.1)

Mod Minecraft bertema dunia **Soul Land (Douluo Dalu)** karya Tang Jia San Shao. Sistem
kultivasi, **1.995 martial soul** dari seluruh Benua Douluo, **9.975 skill**, spirit ring,
sekte, evolusi soul, soul beast, dan boss yang diperkuat.

## Apa yang membuat v1.1 berbeda

- **Martial soul bisa dilihat, bukan cuma dibaca.** Tekan `J` untuk membuka Kartu Martial
  Soul (potret besar, daftar skill, status ring, pilihan sekte). Tekan `K` untuk memunculkan
  martial soul sungguhan yang melayang di belakangmu dan ikut maju saat kamu menyerang.
- **Setiap skill punya efek visual sendiri.** Tujuh arketipe bentuk (bola, garis, kerucut,
  cincin, cakram, pilar, badai) plus entitas energi khusus untuk teknik ikonik, guncangan
  kamera, dan kilat layar. Tidak ada dua skill yang terlihat sama.
- **Lima skill per martial soul**, terbuka bertahap di spirit ring 1, 2, 3, 5, dan 7.
- **1.995 martial soul** dibangun dari sumber lore resmi: tabel Moegirlpedia, daftar abjad
  wiki Soul Land, dan daftar lengkap 武魂大全. Termasuk bentuk evolusi, bentuk tahun ring
  (10.000 dan 100.000 tahun), soul gabungan (武魂融合技), body soul setelah awakening kedua,
  dan bentuk ultimate attribute.
- **Aset sendiri.** 1.995 potret soul 16x16 digambar dengan kode (`tools/generate_assets.py`),
  plus 15 texture item berlapis. Tidak ada satu pun item yang memakai texture placeholder.

## Mekanisme v1.2 (supaya tidak membosankan)

Tujuh mekanisme yang mengubah keputusan pemain, bukan cuma menambah konten:

| Mekanisme | Cara main | Kenapa tidak membosankan |
|---|---|---|
| **Absorpsi berani** | Tahan Shift + klik kanan ring: bayar 20% HP dan 40 Spirit Power, 55% chance ring naik satu tingkat dan skill ring tambahan terbuka. Gagal? Ring hancur, 25% HP hilang. | Absorpsi jadi keputusan, bukan formalitas |
| **Loadout skill** | Lima slot tombol (R G F V Z) diisi skill bebas: skill martial soul atau skill spirit ring. Atur di Kartu Martial Soul, tab SKILLS. | Build berbeda tiap pemain, tidak ada urutan tunggal |
| **Rantai soul** | Setiap skill yang berhasil menambah satu mata rantai, bertahan 4 detik. Tiap mata +8% damage, maksimal +40%. | Urutan skill jadi penting |
| **Lawan elemen** | Nama soul beast menyebut elemennya (Api, Es, Petir, Angin, Air, Tanah, Racun, Tumbuhan, Cahaya, Gelap). Elemen soul menang +25% damage, kalah -15%. | Target harus dipilih, bukan di spam |
| **Bakar darah** | Tombol B: tukar 25 HP jadi 30 Spirit Power, 8 detik damage +50%. Cooldown 45 detik, minimal 35 HP. | Ada jalan keluar desperate, ada risiko |
| **Upacara terobosan** | Di gerbang peringkat, tahan C dan bermeditasi 3 detik penuh dengan 1 Esensi Jiwa. | Naik peringkat butuh ritual, bukan grind |
| **Spirit ring skill** | Tiap ring membuka satu skill sendiri; skill ring bisa dipasang ke slot mana pun. | 9 ring berarti 9 skill ekstra |

## Sistem

| Sistem | Isi |
|---|---|
| Kultivasi | 10 peringkat, level 1-99, gerbang tiap 10 level |
| Martial soul | Gacha saat kebangkitan, 1.995 kemungkinan, 5 skill per soul |
| Spirit ring | 100 / 1.000 / 10.000 / 100.000 tahun, tiap ring +2 HP dan +0.5 attack |
| Ledakan ring | Tombol `X`: energi seluruh ring dilepas sekaligus |
| Sekte | Tang (+20% XP), Shrek (+12% damage), Bintang Luo (+10% kecepatan dan HP), Tujuh Harta (+20% HP dan penyembuhan) |
| Meditasi | Tahan `C`, diam di tempat, menambah XP dan Spirit Power |
| Evolusi | 6 ring + Inti Evolusi + 8 Esensi Jiwa, membuka bentuk lanjutan soul |
| Soul beast | Monster dinamai menurut usianya (10 - 10.000 tahun) dan menjatuhkan ring |
| Boss | Elder Guardian 120 HP, Wither 600 HP, Warden 800 HP, Ender Dragon 1000 HP |

## Tombol

| Tombol | Fungsi |
|---|---|
| `R` `G` `F` `V` `Z` | Skill martial soul 1 sampai 5 |
| `X` | Ledakan spirit ring |
| `J` | Buka Kartu Martial Soul |
| `K` | Panggil / sembunyikan companion martial soul |
| `C` (tahan) | Meditasi / upacara terobosan |
| `B` | Bakar darah (tukar HP jadi Spirit Power) |
| `H` | Buka Panduan Soul Land (tampilan sendiri) |
| `Shift` + klik kanan ring | Absorpsi berani |

## Perintah

- `/soulinfo` — status kultivasi
- `/martialsoul info` — detail martial soul
- `/martialsoul summon` — munculkan companion
- `/martialsoul reroll` — undi ulang (Creative saja)
- `/sect info`, `/sect choose <nama>` — lihat dan pilih sekte
- `/cultivation set <pemain> <level>` — admin/OP

## Cara build (butuh JDK 17)

1. Install **JDK 17** (mis. dari https://adoptium.net).
2. Jalankan `./gradlew build` (Windows: `gradlew.bat build`).
3. File jadi ada di `build/libs/soulland-1.1.0.jar`.

Build pertama mengunduh banyak file (ForgeGradle), butuh koneksi internet dan beberapa menit.

## Pack-state dan referensi

Proyek ini dikelola dengan alur skill `minecraft-pack-builder` dan `pack-context`, jadi
memiliki memori proyek yang persisten di `pack-state/`:

- `pack-state/project-context.json` - target, batasan, mod yang diterima, risiko, gap.
- `pack-state/mod-analyses/*.json` - analisis 8 kandidat mod dari data Modrinth nyata.
- `pack-state/compat-reports/*.json` - keputusan terima/tolak beserta alasannya.
- `pack-state/integration-docs/*.md` - dokumen integrasi dan rancang ulang v1.3.
- `pack-state/simulation-report.json` - hasil simulasi ritme main.

Kurva kultivasi, format config, dan aset diambil dari sumber internet yang tercatat di
`docs/references/REFERENCES.md`. Konfigurasi Curios untuk pack ini ada di `config/` dan
`datapacks/soulland_curios/`.

## Simulasi dan pengujian

```bash
python3 tools/simulate.py --sessions 800   # main 800 sesi, cetak ritme dan risiko
python3 tools/build_souls.py               # bangun ulang data 1.995 martial soul
python3 tools/generate_assets.py           # gambar ulang seluruh texture
python3 tools/build_pack_state.py         # scan ulang kandidat mod dari Modrinth
```

Simulasi menemukan tiga cacat balance yang sudah diperbaiki: kurva XP terlalu curam
(100 + 25 * level jadi 80 + 12 * level), Spirit power pulih terlalu cepat sehingga Bakar
Darah tidak pernah terpakai, dan damage skill tumbuh terlalu datar.

## Membangun ulang data & aset

Data martial soul dan seluruh texture dibuat dari kode, jadi bisa direproduksi:

```bash
python3 tools/build_souls.py      # membangun data/soulland/martial_souls.json dari sumber lore
python3 tools/generate_assets.py  # menggambar ulang seluruh texture soul dan item
```

Sumber lore mentah disimpan di `tools/data/` (tabel Moegirlpedia 中文/EN, daftar wiki, daftar
武魂大全). `DESIGN.md` berisi arahan visual, `docs/brainstorm/` berisi rancangan v1.1.

## Multiplayer

Semua logika berjalan di server: gacha, level, ring, skill, sekte, evolusi, dan meditasi
tersimpan per pemain. Semua pemain wajib memasang mod yang sama.

## Lisensi

MIT. Terinspirasi dari novel dan anime Soul Land (Douluo Dalu) karya Tang Jia San Shao.
