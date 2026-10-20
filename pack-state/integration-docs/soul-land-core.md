# Integrasi: Soul Land core + kandidat modpack

Dokumen ini dihasilkan lewat skill `minecraft-pack-builder` (analisis + `pack-compat`) dan
menjadi rujukan integrasi untuk `pack-state/project-context.json`.

## Peran mod inti

| Aspek | Nilai |
|---|---|
| Loader / versi | Forge 1.20.1, Java 17 |
| Distribusi | server-first, semua mekanik di server |
| Peran | Penyedia seluruh sistem kultivasi Soul Land |
| Beban dependency | Tidak butuh mod lain |
| Risiko versi | Rendah, tidak ada bentrok dependency |

Mod inti sudah menyediakan yang biasanya dicari pemain dari modpack lain:
HUD dan bar skill sendiri, kartu soul, companion, partikel sendiri, dan ability sendiri.
Karena itu kandidat yang fungsinya tumpang tindih tidak diterima tanpa alasan kuat.

## Kandidat yang diterima

| Mod | Peran | Server | Alasan diterima | Adaptasi yang dipakai |
|---|---|---|---|---|
| Jade | HUD + minimap | opsional | Minimap untuk area perburuan tanpa membebani server | Tidak ada perubahan kode, hanya informatif |
| Curios | Slot aksesori | wajib | Sembilan spirit ring adalah aksesori, bukan isi tas | Slot ring dipindah ke slot Curios, inventory jadi cadangan |
| Waystones | Teleportasi | wajib | Perburuan soul beast jadi soal perjalanan | Jaring waystone dibatasi satu jaringan per wilayah |
| Moonlight | Bioma | wajib | Variasi permukaan untuk dunia Benua Douluo | Tidak dipakai bersama Terralith |
| Supplementaries | Dekorasi | wajib | Altar ritual, tenda klan, dan bangunan klan | Dipakai dekoratif, tanpa progression |
| Apotheosis | Affix dan rune | wajib | Spirit ring bisa punya afiks, jadi bukan sekadar angka | Afiks tidak menimpa bonus skill, hanya tambahan |
| Architectury API | Library | wajib | Dipakai beberapa kandidat di atas | Satu versi Forge 1.20.1 saja |
| Terralith | Bioma alternatif | wajib | Cadangan kalau Moonlight bentrok | Dipakai hanya kalau Moonlight ditolak |

## Kandidat yang ditolak

Belum ada kandidat yang ditolak karena tidak mendukung Forge 1.20.1. Semua kandidat di
atas punya rilis Forge 1.20.1 di Modrinth saat pemindaian (lihat `pack-state/mod-analyses/`).

Yang ditolak adalah **overlap**, bukan versi:

- Moonlight dan Terralith sama-sama mengubah biome, jadi tidak boleh dipasang berdua.
  Keduanya tetap dicatat, dengan Terralith sebagai cadangan Moonlight.
- Mod HUD lain yang menggambar bar cultivate di kiri atas tidak dipakai, karena HUD mod
  inti sudah occupying tempat itu.

## Aturan integrasi

1. Semua mekanik kultivasi wajib jalan tanpa mod lain. Mod lain hanya menambah lapisan.
2. Tidak boleh ada dua mod yang menulis ke state pemain yang sama. State pemain hanya
   dimiliki mod inti.
3. Resource pack tidak wajib: semua texture dan partikel sudah ada di jar mod inti.
4. Setiap versi mod yang dipakai harus persis untuk Forge 1.20.1, dicatat di
   `pack-state/mod-analyses/`.

## Bukti

- Analisis per mod: `pack-state/mod-analyses/<slug>.json`
- Laporan kompatibilitas: `pack-state/compat-reports/<slug>.json`
- Simulasi ritme main: `pack-state/simulation-report.json` (dihasilkan `tools/simulate.py`)
