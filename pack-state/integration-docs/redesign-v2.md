# Rancang ulang v1.3: Benua Douluo

Dokumen rancang ulang, dibuat dengan alur `minecraft-pack-builder` (discovery, analisis,
kompatibilitas) dan `pack-context` (lapisan state). Semua keputusan di sini punya bukti di
`pack-state/`.

## 1. Masalah yang harus diselesaikan

v1.1 menambah banyak isi, tapi loop-nya tetap linear: bunuh monster, dapat XP, serap ring,
buka skill. Tidak ada keputusan yang diambil pemain, jadi tidak ada alasan untuk kembali.
v1.2 menambahkan tujuh mekanisme; simulasi `tools/simulate.py` dipakai untuk menguji
apakah ritmenya benar-benar enak, dan menemukan tiga cacat balance yang sudah diperbaiki.

## 2. Target dan batasan

**Target:** pemain Soul Land yang mencari cultivation yang layak, bukan sekadar mod-item.

**Batasan keras:**
- Forge 1.20.1, Java 17, server-first.
- Tidak boleh ada kelas client yang termuat di dedicated server.
- Semua aset dan data bisa dibangun ulang dari kode.
- CI menjalankan compile dan uji boot server sebelum release terbit.

## 3. Mekanisme (inti v1.3)

| Mekanisme | Keputusan pemain | Angka tuned |
|---|---|---|
| Undian tiap masuk | Martial soul baru setiap sesi, level dan ring tetap | 1.434 dari 1.995 soul bisa diundi |
| Absorpsi brave | Ring aman atau berisiko | 55% naik tingkat, 20% HP + 40 Spirit Power, gagal 25% HP |
| Loadout skill | Lima slot diisi skill bebas | 5 skill soul + sampai 10 skill ring |
| Rantai soul | Urutan skill | +8% per mata, maksimum 5 mata, 4 detik |
| Lawan elemen | Target mana yang diburu | +25% menang, -15% kalah |
| Bakar darah | Kapan mengorbankan HP | 25 HP jadi 30 Spirit Power, +50% damage 8 detik, cooldown 45 detik |
| Upacara terobosan | Kapan naik peringkat | meditasi 3 detik + 1 Esensi Jiwa |
| Kontrak perburuan | Target berburu | 10 soul beast, 10 menit, hadiah ring + 4 esensi |

## 4. Hasil simulasi (800 sesi, seed 20261020)

| Metrik | Nilai | Bacaan |
|---|---|---|
| Tuntas ke Title Douluo | 100% | progres tercapai |
| Giliran rata-rata | 452 | sekitar 8 sampai 15 menit berburu aktif |
| Kematian per sesi | 15 | cukup untuk terasa berbahaya tanpa grind |
| Bakar darah per sesi | 19 | mekanik ini hidup, bukan hiasan |
| Absorpsi brave berhasil | 54,9% | keputusan berarti, bukanReceh |
| Ring hilang per sesi | 2,3 | risiko nyata |
| Sesi mandek | 0% | tidak ada jebakan leveling |

Tiga cacat yang ketahuan dan diperbaiki:

1. Kurva XP `100 + 25 * level` membuat level 90-an butuh ribuan kill. Diganti `80 + 12 * level`.
2. Spirit power pulih 0,15 per tick, jadi Bakar Darah tidak pernah terpakai. Diganti 0,05.
3. Damage skill tumbuh 2% per level, terlalu datar. Diganti 5% per level dan 12% per ring.

## 5. Arsitektur

```
data/soulland/martial_souls.json   1.995 soul + 9.975 skill (dibangun tools/build_souls.py)
textures/soul/*.png                1.995 potret 16x16 (tools/generate_assets.py)
textures/particle/*.png            11 partikel kustom
soul/                              MartialSouls, SkillDef, SkillVisual, Element, RingSkills
skill/                             SkillHandler (data-driven), SkillFx (7 arketipe)
entity/ + client/render/           companion soul dan bentuk energi
screen/                            Kartu Martial Soul (4 tab) dan Panduan (7 bagian)
client/                            HUD, bar skill, shake, kilat layar
pack-state/                        memori proyek: konteks, analisis, kompatibilitas
```

Prinsip: skill data-driven. Menambah skill berarti menambah data, bukan menambah cabang
kode. Semua efek visual diturunkan dari `SkillVisual`, jadi dua skill tidak pernah
terlihat sama.

## 6. Modpack: mod yang dipakai dan cara dimodifikasi

Pack berisi mod inti `soulland` plus tujuh mod pendukung. Semuanya diambil dari Modrinth
dengan versi Forge 1.20.1 yang terverifikasi (`modpack/modrinth.index.json`).

| Mod | Yang diubah supaya dipakai pack ini |
|---|---|
| Curios | `config/curios-common.toml` membuat sembilan slot `spirit_ring`. `CuriosBridge` di mod inti memindahkan ring ke slot itu lewat API Curios (lewat refleksi supaya Curios tetap opsional), dan tetap jalan di inventory bila Curios tidak ada. |
| Apotheosis | Spirit ring diperlakukan sebagai barang biasa, jadi afiks dan rune langsung berlaku tanpa kode tambahan. |
| Jade | HUD kultivasi tetap di mod inti; Jade hanya untuk minimap. Nama soul beast sudah menyebut usia dan elemen, jadi panel Jade langsung terbaca. |
| Moonlight | Bioma Moonlight mengganti elemen soul beast menjadi Tumbuhan dan Cahaya, jadi berburu di wilayah itu terasa berbeda. |
| Supplementaries | Dekorasi klan dipakai untuk altar ritual; Dupa Meditasi di mod inti memperkuat kultivasi saat meditasi. |
| Waystones | Dipakai untuk berpindah antar wilayah berburu. Tidak ada perubahan kode, hanya konfigurasi jaringan. |
| Architectury API | Library. Satu versi Forge 1.20.1 saja yang dipasang. |

Terralith dicatat sebagai cadangan Moonlight karena dua mod worldgen tidak boleh berdua.

## 7. Bukti dan verifikasi

| Yang diuji | Cara | Status |
|---|---|---|
| Compile | `gradle build` | lulus |
| Boot dedicated server | `gradle runServer` | "Done" tanpa error |
| Ritme main | `tools/simulate.py` 800 sesi | 452 giliran, 15 kematian |
| Aset lengkap | `python3 tools/verify_assets.py` | 1.995 potret, 20 model, 11 partikel, 9.975 skill, 0 hilang |
| Unit test | `gradle test` (JUnit 5) | 14 test lulus: kurva XP, gerbang, matriks elemen, roundtrip NBT |
| Simulasi main | `python3 tools/simulate.py --sessions 800` | 452 giliran, 15 kematian, 0 sesi mandek |
| Rilis | GitHub Actions, tag `v*` | build + boot test + release |

## 7. Capability gap yang masih terbuka

1. Cincin spirit belum memakai slot Curios, masih di inventory biasa.
2. Belum ada wilayah soul beast khusus; soul beast masih monster vanilla yang diberi nama.
3. Affix Apotheosis belum disambungkan ke spirit ring.
