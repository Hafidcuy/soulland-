# DESIGN.md - Soul Land: Douluo Dalu (Mod Minecraft)

Arah visual untuk semua-pixel-art dan antarmuka di mod ini.

## Identitas

**Artefak kultivasi kuno Benua Douluo.** Setiap elemen terasa seperti benda yang dibawa
pemain keluar dari peti: lencana geopolitik, giok, sutra, ukiran pedang, dan Bara.
Bukan "UI gelap dengan neon ungu", bukan sci-fi hologram.

Referensi rasa: lencana Tionghoa abad ke-9, pigmen mineral tua (malakit, azurit, cinnabar),
kertas sutra yang sudah menguning, dan ukiran pedang baja. Bukan: neon cyberpunk, glassmorphism,
gradient ungu-biru.

**Dials:** ENERGY 2 / RHYTHM 2 / MOTION 2

## Palet

| Token | Hex | Arti | Dipakai untuk |
|---|---|---|---|
| Soul Jade | `#4FC08D` | energi, kehidupan, kanal utama | spirit power, bar aktif, aria "hidup" |
| Imperial Gold | `#E8C46A` | peringkat, pencapaian, kemuliaan | level, rank, achievement, legendary |
| Ink Black | `#14161A` | bidang, kedalaman, tekstur | latar panel, bayangan |
| Crimson Soul | `#D8442F` | bahaya, damage, boss | slot skill damage, peringatan |
| Violet Evolution | `#6A3FD1` | evolusi dan legenda | form evolusi, skill legendary (jarang) |

Maksimal 3 warna inti + 1 aksen di satu layar. Violet hanya muncul saat ada evolusi
aktif, jadi ia tetap event langka.

**Latar Minecraft = netral, tidak dihitung.** Warna mod hanya untuk hal yang punya
makna. Tidak ada warna dekoratif.

## Type & spacing

- Font: font Minecraft bawaan. Tidak ada font custom, tidak ada gaya terminal.
- Skala: `9px` label, `11px` judul kartu, `13px` nama skill. Tidak ada skala lain.
- Spacing: kelipatan 2px (2, 4, 6, 8, 12, 16). Jarak antar-baris 2px.
- Radius: 0 untuk pixel art, 2px untuk panel GUI. Tidak ada bentuk pill.

## Pixel art

- Ukuran: 16x16 untuk item dan portret, 32x32 untuk panel kartu soul.
- Palette dibatasi 6 warna per sprite: warna dasar soul, warna highlight, warna shadow,
  dan aksen (mata atau sumber energi). Setiap soul punya warna utama sendiri.
- Silhouette lebih penting dari detail. Bentuk utama harus terbaca kalau dibalik
  jadi hitam-putih.
- Tidak ada gradient dalam sprite. Highlight adalah 1-2 piksel.
- Setiap texture punya alpha; tidak ada background opaque di portret.

## Sistem potret soul

1.995 potret soul digambar oleh `tools/generate_assets.py`, bukan satu gambar untuk semua.
- **11 kelompok** (alat, binatang, tumbuhan, naga, elemen, badan, manusiawi, makanan, langit,
  gelap, fusion) masing-masing punya **3 sampai 8 siluet** berbeda.
- Siluet dipilih lewat hash id soul, jadi dua soul dalam satu kelompok tidak pernah
  mendapat bentuk yang sama.
- **Detail per soul**: beberapa soul mendapat tanduk, kilau, atau titik warna tambahan
  sesuai hash, supaya portret tidak kembar meski warnanya mirip.
- **16 soul ikonik** (Clear Sky Hammer, Blue Silver Grass, White Tiger, Pagoda, Phoenix,
  Naga Emas, dan seterusnya) memakai potret yang digambar tangan, satu per satu.
- Warna dasar mengikuti elemen soul, sehingga Api, Es, Petir, dan Bayangan langsung terbaca
  dari potretnya.
- Bentuk tahun ring (10.000 dan 100.000 tahun) memakai siluet yang sama dengan warna
  aksen lebih terang, jadi bentuk yang lebih tua terlihat lebih berbahaya.

## Item

15 texture item, sebagian berlapis (dua lapis di atas texture utama):
- Empat spirit ring: cincin + lapisan kilat, makin mahal makin/local.
- Kartu soul dan manual soul: kartu + bingkai emas.
- Empat lencana sekte: medalion + lapisan kilat, masing-masing warna sekte.
- Inti soul, inti evolusi, dan lencana sekte: bulatan + lapisan kilat.
- Esensi jiwa, dupa meditasi, dan sosis ajaib: tekstur tunggal.

## Antarmuka

### HUD (kiri atas)

```
[portret 16x16]  Lv.24  Spirit Master
                  Ring 3/9
  [1][2][3][4][5]   <- slot skill
  [=========----]   <- spirit power (Soul Jade)
```

- Slot skill: 20x20. Angka 1-5 di kiri atas. Isi: warna skill (merah=serangan,
  hijau=sembuh, emas=buff, ungu=kontrol, cyan=gerak). Abu gelap = terkunci.
  Cooldown: sapuan gelap dari atas ke bawah.
- Tidak ada tombol GUI di HUD. Semua aksi lewat keyboard.

### Soul Card GUI (tombol J)

```
+--------------------------------------------------+
|  [portret 32x32]   |  SOUL | SKILLS | RINGS     |
|  Nama Soul         |  ----------------------------- |
|  Rarity / Tipe     |  isi tab aktif                |
|  Elemen            |                              |
|  Deskripsi         |                              |
+--------------------------------------------------+
```

- Tab aktif diberi garis bawah 2px warna aksen, bukan capsule.
- Baris skill: nama (putih), deskripsi (abu), dan di kanan metadata nyata
  (energi, cooldown, syarat ring). Skill terkunci seluruh baris digelapkan.
- Tidak ada tombol yang tidak melakukan apa-apa.

## Gerak

| Kejadian | Gerakan | Alasan |
|---|---|---|
| Skill kecil | partikel + suara | konfirmasi aksi |
| Skill besar | kamera goyang + layar menyala | bobot terasa |
| Companion aktif | hover pelan, cincin berputar | tanda "hidup" |
| Companion mengeksekusi skill | companion maju lalu kembali | skill terasa punya sumber |
| Absorb ring | cincin naik mengelilingi pemain | momen ekspresi |

Tidak ada animasi loop yang tidak dipicu. Shake dan flash punya durasi <= 12 tick.

## Larangan

- Emoji sebagai ikon. Semua ikon texture pixel art.
- Placeholder (ender pearl, `minecraft:item/ender_pearl`).
- Gradient ungu-biru, glow di semua tempat, panel rounded seragam.
- Angka yang tidak berasal dari state nyata.
- Teks "Coming soon" tanpa fitur yang benar-benar ada.
