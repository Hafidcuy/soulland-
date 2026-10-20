# Soul Land: Douluo Dalu — Mod Minecraft Java (Forge 1.20.1)

Mod Minecraft berdasarkan dunia **Soul Land (Douluo Dalu)** karya Tang Jia San Shao.
Sistem kultivasi, gacha martial soul, spirit ring, skill, soul beast, dan boss yang diperkuat —
dengan **Ender Dragon sebagai boss akhir**.

## Fitur

- **Sistem kultivasi 10 peringkat**: Spirit Scholar → Spirit Master → Grandmaster → Elder →
  Ancestor → King → Emperor → Sage → Douluo → **Title Douluo** (level 1–99).
- **Gacha Martial Soul**: saat pemain pertama kali masuk dunia, martial soul-nya diundi
  secara acak dengan rarity (Common/Uncommon/Rare/Epic/Legendary), seperti takdir saat lahir.
  10 martial soul dari series: Blue Silver Grass, Clear Sky Hammer, Evil Fire Phoenix,
  Seven Treasure Glazed Tile Pagoda, White Tiger, Soft Bone Rabbit, Hell Civet,
  Jade Phosphor Serpent, Sausage, dan Cangkul.
- **Ubah hanya di Creative**: hasil gacha permanen; hanya bisa diundi ulang lewat
  `/martialsoul reroll` dalam mode Creative.
- **Spirit Ring**: bunuh soul beast (monster) untuk mendapatkan ring 100 / 1.000 / 10.000 tahun;
  ring 100.000 tahun hanya dari Ender Dragon. Klik kanan untuk menyerap (ada syarat level).
  Gerbang peringkat (level 10, 20, … 90) hanya bisa dilewati dengan ring baru.
- **20 skill**: tiap martial soul punya 2 skill, dibuka di 1 dan 3 spirit ring,
  diaktifkan dengan **R** (skill 1) dan **G** (skill 2). Memakai Spirit Power + cooldown.
- **Boss diperkuat**: Elder Guardian (HP 120), Wither (HP 600), Warden (HP 800),
  Ender Dragon (HP 1000) — semuanya soul beast tingkat tinggi.
- **Buku panduan otomatis**: setiap pemain baru mendapat "Panduan Dunia Soul Land"
  yang menjelaskan seluruh sistem, daftar skill tiap martial soul, dan cara mengaktifkannya.
- **Multiplayer-ready**: semua logika berjalan di server; data tiap pemain tersimpan
  dan tersinkron. Pasang di server Forge dan semua client.

## Cara build (butuh JDK 17)

1. Install **JDK 17** (mis. dari https://adoptium.net).
2. Buka folder proyek di terminal, jalankan:
   - Windows: `gradlew.bat build`
   - Linux/Mac: `./gradlew build`
3. File jadi ada di `build/libs/soulland-1.0.0.jar`.
4. Masukkan jar ke folder `mods` milik Forge 1.20.1 (client maupun server).

Build pertama mengunduh banyak file (ForgeGradle), butuh koneksi internet dan beberapa menit.

## Cara main

1. **Login pertama** → upacara kebangkitan: martial soul diundi + buku panduan masuk inventory.
2. **Berburu soul beast** (monster) → dapat XP kultivasi dan kadang Spirit Ring.
3. **Serap spirit ring** (klik kanan) saat level mencukupi untuk membuka skill & melewati gerbang.
4. Tekan **R** / **G** untuk skill martial soul.
5. Naik hingga **Title Douluo (level 91+, 9 ring)**, lalu taklukkan **Ender Dragon**
   dan serap Spirit Ring 100.000 tahun-nya.

## Perintah

- `/soulinfo` — status kultivasi
- `/martialsoul info` — detail martial soul
- `/martialsoul reroll` — undi ulang (Creative saja)
- `/cultivation set <pemain> <level>` — admin/OP

## Multiplayer

- Server: Forge 1.20.1 (47.2.0) + taruh jar di `mods` server.
- Semua pemain wajib memasang mod yang sama di client mereka.

## Kustomisasi

- Tambah martial soul baru di `src/main/java/com/soulland/soul/MartialSouls.java`,
  efek skill di `src/main/java/com/soulland/skill/SkillHandler.java`.
- Halaman buku panduan di `src/main/java/com/soulland/book/GuideBook.java`.
