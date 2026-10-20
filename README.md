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
| `C` (tahan) | Meditasi |

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
