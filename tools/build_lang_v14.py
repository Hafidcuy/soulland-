#!/usr/bin/env python3
"""Tambahkan kunci bahasa v1.4 (katalog 43 soul) ke en_us.json dan id_id.json.

Skrip ini idempoten: kunci yang sudah ada tidak ditimpa, jadi aman dijalankan
ulang. Isi kedua file dijaga tetap sinkron (nama dan jumlah kunci sama).

Kunci yang dipakai kode dibaca langsung dari registry Java, jadi kalau ada soul
atau skill baru di kode tanpa terjemahan, skrip ini gagal dengan pesan yang
jelas, bukan diam-diam meninggalkan kunci kosong.

Teks memakai satu tanda persen tunggal (``%s``), karena formatter milik mod
mengganti setiap ``%s`` dengan argumen berurutan dan tidak memproses ``%%``.
"""
import json
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
LANG = ROOT / "src/main/resources/assets/soulland/lang"
SOUL_FILE = ROOT / "src/main/java/com/soulland/soul/MartialSouls.java"

# ---------------------------------------------------------------- terjemahan soul

# id soul -> (nama Indonesia, deskripsi Indonesia)
SOULS = {
    "golden_dragon": (
        "Naga Emas",
        "Naga keemasan yang sisiknya menyala makin terang setiap abad yang dilaluinya.",
    ),
    "abyssal_demon_dragon": (
        "Naga Demon Jurang",
        "Naga yang menarik cahaya bintang dari jurang dan memakainya seperti kulit kedua.",
    ),
    "abyssal_ice_demon_dragon": (
        "Naga Demon Es Jurang",
        "Kembaran dingin naga jurang, membekukan segala yang dilaluinya menjadi sunyi.",
    ),
    "ultimate_fire_dragon": (
        "Naga Api Tertinggi",
        "Naga yang dibesarkan di gunung api selama seribu tahun, cukup tua untuk mengingat nyala pertama.",
    ),
    "ultimate_ice_dragon": (
        "Naga Es Tertinggi",
        "Naga terakhir dari gletser, lambat bangun dan mustahil dibantah.",
    ),
    "thunder_elephant": (
        "Gajah Petir",
        "Gajah yang berjalan menembus badai, membawa petir di rongga gadingnya.",
    ),
    "golden_ant": (
        "Semut Emas",
        "Semut pekerja yang cukup kecil untuk bersembunyi, cukup kuat untuk mengikis tebing.",
    ),
    "titan_giant_ape": (
        "Kera Raksasa",
        "Gorila sebesar benteng, masih marah pada gunung yang tidak pernah ia minta.",
    ),
    "sea_devil_manatee": (
        "Manati Setan Laut",
        "Bentuk abu-abu yang lambat menarik kapal turun dari pergelangan kaki, dan tidak pernah buru-buru.",
    ),
    "bright_wing_moth": (
        "Ngengat Sayap Cerah",
        "Ngengat yang sayapnya menebar cahaya yang membutakan siapa pun yang mengekornya.",
    ),
    "phoenix": (
        "Phoenix",
        "Burung yang terbakar lalu dilahirkan lagi, makin tua dan makin tenang tiap reincarnasi.",
    ),
    "blue_silver_spear": (
        "Tombak Blue Silver",
        "Tombak yang tumbuh dari rumput blue silver yang sama, dikeraskan sampai tidak mau mengalah.",
    ),
    "seven_kill_sword": (
        "Pedang Tujuh Bunuh",
        "Pedang yang menghitung jumlah yang ia bunuh dan tidak pernah puas dengan angkanya.",
    ),
    "giant_hammer": (
        "Palu Raksasa",
        "Palu pandai besi yang tumbuh melebihi tuannya dan sekarang punya pendapat tentang landasan.",
    ),
    "blood_lash": (
        "Cambuk Darah",
        "Cambuk dari tali darah kering yang mengingat setiap target yang pernah ia robek.",
    ),
    "frost_needle": (
        "Jarum Es",
        "Jarum es setipis yang hanya terlihat saat sudah berada di dalam tubuh seseorang.",
    ),
    "thunder_spear": (
        "Tombak Petir",
        "Tombak yang hanya terlihat ketika petir di dalamnya menyala.",
    ),
    "spirit_sword": (
        "Pedang Spirit",
        "Pedang biasa yang menyimpan rahasianya dan bertarung dengan aturan sendiri.",
    ),
    "iron_anchor": (
        "Jangkar Besi",
        "Jangkar tertua guild, diturunkan pelaut yang tidak pernah naik lagi.",
    ),
    "bronze_mirror": (
        "Cermin Perunggu",
        "Cermin yang menunjukkan musuh sekte momen terburuk mereka sendiri.",
    ),
    "blue_silver_emperor": (
        "Kaisar Blue Silver",
        "Rumput yang sudah cukup tua untuk memerintah: sepuluh ribu tahun otoritas hijau yang tenang.",
    ),
    "iron_body": (
        "Tubuh Besi",
        "Tubuh yang belajar menjadi logam, dan membayar harganya dalam berat dan dingin.",
    ),
    "bone_shell": (
        "Cangkang Tulang",
        "Tulang yang tumbuh ke luar menjadi cangkang, lapis demi lapis, tahun demi tahun.",
    ),
    "stone_skin": (
        "Kulit Batu",
        "Kulit biasa yang mengeras setelah sepuluh tahun berdiri di bawah hujan.",
    ),
    "flesh_regrowth": (
        "Tumbuh Otot",
        "Jaringan luka yang menutup lebih cepat dari seharusnya, dengan harga ingatan.",
    ),
    "flame_body": (
        "Tubuh Bara",
        "Tubuh yang terbakar dari dalam dan menjawab panas dengan panas.",
    ),
    "spirit_veins": (
        "Urat Spirit",
        "Spirit power mengalir di bawah kulit, bukan di dalam dada, dan tubuh bergerak jauh lebih cepat.",
    ),
    "soul_mirror": (
        "Cermin Soul",
        "Cermin yang menunjukkan bentuk soul dan tidak peduli apa yang ditunjukkan.",
    ),
    "spirit_lamp": (
        "Lentera Spirit",
        "Lentera yang menyala tanpa minyak dan menerangi apa pun yang sedang dipikirkan pemiliknya.",
    ),
    "soul_forge": (
        "Ketuk Tempa Soul",
        "Ketuk Tempa yang panaskan soul, bukan logam, dan membentuk apa yang keluar darinya.",
    ),
    "star_array": (
        "Susunan Bintang",
        "Tujuh titik cahaya dan ruang di antaranya, ditata persis begitu.",
    ),
    "demon_eye": (
        "Mata Setan",
        "Mata yang tidak pernah berkedip, karena tidak ada yang perlu ia takutkan.",
    ),
    "ancient_seal": (
        "Segel Purba",
        "Segel yang lebih tua dari sekte yang mencetaknya, dan tetap menjawab tangan pemiliknya.",
    ),
}

# ---------------------------------------------------------------- terjemahan skill

# id skill -> (nama Indonesia, deskripsi Indonesia)
SKILLS = {
    # Golden Dragon
    "gold_dragon_fang": ("Taring Api", "Satu gigitan membakar yang membakar target dari dalam."),
    "gold_dragon_scales": ("Dinding Sisik", "Sisik keemasan mengeras menjadi cangkang yang menyerap pukulan berikutnya."),
    "gold_dragon_sun": ("Pancaaran Matahari", "Matahari yang tersimpan meledak di sekitarmu dan melempar musuh."),
    "gold_dragon_ascension": ("Ascensi Emas", "Naga melingkar dan menerjang: nova bara yang menyapu seluruh arena."),
    # Abyssal Demon Dragon
    "demon_dragon_graze": ("Cakaran Jurang", "Cakaran jurang membuka luka yang mengeluarkan bayangan, bukan darah."),
    "demon_dragon_void": ("Tarikan Kekosongan", "Jurang menarik setiap musuh di dekatnya ke arah cakarumu."),
    "demon_dragon_gaze": ("Pandang Tanpa Bintang", "Pandangan yang mencabut jiwa: target ditandai dan goyah."),
    "demon_dragon_abyss": ("Putusan Jurang", "Naga menyelam menembus tanah dan meledak dari bawah arena."),
    # Abyssal Ice Demon Dragon
    "ice_dragon_frost": ("Cacar Es", "Cakar yang membekukan target dan membuatnya rapuh."),
    "ice_dragon_hail": ("Penjara Es", "Cincin es mengunci setiap musuh di tempatnya."),
    "ice_dragon_chain": ("Rantai Beku", "Es melilit dari cengkeramanmu ke tiga musuh sekaligus."),
    "ice_dragon_winter": ("Musim Dingin", "Musim dingin turun: nova es yang memperlambat semuanya yang disentuhnya."),
    # Ultimate Fire Dragon
    "fire_dragon_breath": ("Napas Bara", "Napas api singkat melapisi semua yang ada di depanmu."),
    "fire_dragon_wing": ("Sayap Bara", "Sayap bara menghembuskan udara dan menyebarkan bara ke seluruh arena."),
    "fire_dragon_scale": ("Kulit Lava", "Kulitmu mengeras menjadi lava dingin: ketahanan dan lapisan panas."),
    "fire_dragon_caldera": ("Kaldera", "Naga membuka rahangnya dan pilar api meledak dari tanah."),
    # Ultimate Ice Dragon
    "ice_dragon_lance": ("Tombak Gletser", "Tombak es bening terbentuk di tanganmu dan menusuk satu target."),
    "ice_dragon_veil": ("Tirai Es", "Tubuhmu mengeras menjadi es: ketahanan dan lapisan baja tipis."),
    "ice_dragon_shatter": ("Hancur", "Satu nada beku menghancurkan keberanian setiap musuh di dekatmu."),
    "ice_dragon_frostfall": ("Hujan Es", "Langit sendiri jatuh: nova es yang mengunci tanah di tempatnya."),
    # Thunder Elephant
    "thunder_elephant_stomp": ("Hantam Petir", "Satu langkah berat mengguncang tanah dan menyentakkan satu target."),
    "thunder_elephant_trample": ("Injak", "Langkah berat meratakan semua musuh di sekitar."),
    "thunder_elephant_charge": ("Semangat Melesat", "Momentum terkumpul lalu menabrak: hantaman berat yang membuat target pusing."),
    "thunder_elephant_storm": ("Kawanan Badai", "Kawanan itu berlari bersamamu: nova petir yang menyapu arena."),
    # Golden Ant
    "golden_ant_bite": ("Gigitan Rahang", "Rahang kecil menemukan celah di pertahanan musuhmu."),
    "golden_ant_acid": ("Semprotan Asam", "Kabut asam melemahkan semua yang disentuhnya."),
    "golden_ant_dig": ("Terowongan", "Menyelam ke bawah tanah dan meledak di bawah tiga musuh."),
    "golden_ant_swarm": ("Barisan Semut", "Gelombang semut emas membanjiri arena dan membawanya pergi."),
    # Titan Giant Ape
    "giant_ape_smash": ("Hantam Gunung", "Kedua tinju turun menimpa satu musuh dengan beban gunung."),
    "giant_ape_roar": ("Teriak Perang", "Teriakan mencabut keberanian dari setiap musuh dalam jangkauan."),
    "giant_ape_throw": ("Lempar Batu", "Batu menembus tiga musuh dalam satu garis lurus."),
    "giant_ape_tremor": ("Guncang", "Kera menghantam tanah dan tanah ikut bergetar."),
    # Sea Devil Manatee
    "manatee_tether": ("Tali Besi", "Tali air menangkap satu musuh dan tidak mau melepaskannya."),
    "manatee_drag": ("Arus Bawah", "Air di bawah arena menarik setiap musuh ke arahmu."),
    "manatee_venom": ("Racun Garam", "Air garam dan darah lama: racun yang menyebar dari luka."),
    "manatee_deep": ("Jauh di Bawah", "Sesuatu yang sangat besar naik dari dasar air dan menyeret arena turun."),
    # Bright Wing Moth
    "moth_dust": ("Debu Bercahaya", "Awan debu terang menyengat mata satu musuh."),
    "moth_wingbeat": ("Kedip Sayap", "Sayap menghembuskan udara menjadi badai yang mendorong musuh."),
    "moth_dazzle": ("Silau", "Seribu sayap memantulkan cahaya sampai target kehilangan keberanian."),
    "moth_swarm": ("Malam Sayap", "Ngengat mengosongkan udara: nova pucat yang membutakan dan memperlambat semuanya."),
    # Phoenix
    "phoenix_ember": ("Sambar Bara", "Terbang turun singkat yang berakhir dengan Gigitan api."),
    "phoenix_rebirth": ("Lahir Kembali", "Terbakar habis lalu pulih: ketahanan dan perisai cahaya hangat."),
    "phoenix_talon": ("Menukik Cakar", "Feng Huang turun dari atas dan mencakar tiga musuh sekaligus."),
    "phoenix_reignite": ("Menyala Lagi", "Burung bangkit dari abunya sendiri dalam lingkaran api."),
    # Blue Silver Spear
    "bss_pierce": ("Tusukan Perak", "Tombak menemukan celah dengan satu tusukan."),
    "bss_bind": ("Ikat Rumput", "Sulur blue silver membungkus target dan menahannya."),
    "bss_venom": ("Racun Perak", "Ujung tombak meneteskan racun yang terus bekerja setelah tusukan."),
    "bss_empire": ("Jangkauan Kaisar", "Setiap helai rumput di arena menunjuk arah yang sama, dan menghantam bersamaan."),
    # Seven Kill Sword
    "seven_kill_mark": ("Takik Pertama", "Bilah membuka luka kecil dan menghitungnya sebagai satu."),
    "seven_kill_drain": ("Catatan Darah", "Bilah minum apa yang ia buka dan meneruskannya ke lenganmu."),
    "seven_kill_notch": ("Takik Ketujuh", "Bilah melompat antar musuh dan meninggalkan takik pada setiap orang."),
    "seven_kill_finish": ("Hitungan Akhir", "Stroke ketujuh jatuh pada yang terlemah dan mengakhiri semuanya."),
    # Giant Hammer
    "giant_hammer_down": ("Pukulan dari Atas", "Palu turun menimpa satu musuh dengan dentang yang melelahkan."),
    "giant_hammer_sweep": ("Ayunan Lebar", "Lengkung lambat yang menangkap semua yang berdiri di depanmu."),
    "giant_hammer_anchor": ("Sikap Berlabuh", "Tancapkan palu dan tahan barisan: ketahanan dan kekuatan."),
    "giant_hammer_quake": ("Hantam Gempa", "Palu bertemu tanah dan gelombang kejutnya menyebar ke luar."),
    # Blood Lash
    "blood_lash_cut": ("Potongan Pertama", "Cambuk membuka garis tipis dan membiarkan darah bekerja."),
    "blood_lash_drain": ("Luka Penghisap", "Tali minum apa yang tumpah dan meneruskannya ke lenganmu."),
    "blood_lash_crack": ("Cambukan Keras", "Satu tekukan melintasi arena dan memukul semua yang dilaluinya."),
    "blood_lash_rite": ("Ritual Tali", "Cambuk mengurai dan melilit ke segala arah sekaligus."),
    # Frost Needle
    "frost_needle_prick": ("Tusukan Dingin", "Satu jarum, satu target, satu garis es yang menyebar."),
    "frost_needle_spread": ("Es Menyebar", "Dingin merambat dari jarum dan mati rasa seluruh area."),
    "frost_needle_venom": ("Racun Mati Rasa", "Racun beku bercampur racun biasa: target berhenti pulih."),
    "frost_needle_storm": ("Badai Jarum", "Seratus jarum memenuhi udara dan turun seperti es."),
    # Thunder Spear
    "thunder_spear_jab": ("Tusukan Statis", "Tusukan pendek membuat target bergeming."),
    "thunder_spear_sweep": ("Ayunan Arus", "Arus listrik melengkung melewati semua yang ada di depanmu."),
    "thunder_spear_chain": ("Arus Bercabang", "Arus bercabang ke tiga musuh dan membuat mereka kejang."),
    "thunder_spear_storm": ("Kepala Badai", "Kepala badai terbuka di atasmu lalu jatuh ke arena."),
    # Spirit Sword
    "spirit_sword_cut": ("Tebasan Sederhana", "Tebasan bersih, tidak lebih, tidak kurang."),
    "spirit_sword_guard": ("Langkah Tahan", "Pedang terangkat: ketahanan dan napas yang teratur."),
    "spirit_sword_riposte": ("Balasan", "Tunggu musuh datang padamu, lalu jawab."),
    "spirit_sword_finish": ("Kata Terakhir", "Satu tebasan desperate, diambil saat pertahanan terbuka."),
    # Iron Anchor
    "iron_anchor_drop": ("Jangkar Jatuh", "Jangkar jatuh menimpa satu musuh dan menahannya di tempat."),
    "iron_anchor_hold": ("Tahan Erat", "Semua yang terjangkau tertarik ke arah kailmu."),
    "iron_anchor_swing": ("Ayunan Jangkar", "Ayunan lebar di ujung rantai menangkap seluruh barisan."),
    "iron_anchor_moor": ("Tambat Armada", "Rantai menyambar ke segala arah dan menahan arena tetap kokoh."),
    # Bronze Mirror
    "bronze_mirror_gleam": ("Kilau", "Kilau cahaya perunggu membutakan satu musuh."),
    "bronze_mirror_ward": ("Perisai Perunggu", "Cermin memantulkan cahaya: ketahanan untuk semua orang di dekatmu."),
    "bronze_mirror_show": ("Tunjukkan Kebenaran", "Setiap musuh di jangkauan melihat dirinya sendiri dan kehilangan niat bertarung."),
    "bronze_mirror_reflect": ("Arena Terpantul", "Cermin memantulkan kembali semua yang ada di arena luncurkan padamu."),
    # Blue Silver Emperor
    "bsemp_roots": ("Akar Kaisar", "Akar dalam menghantam: ketahanan dan pemulihan lambat."),
    "bsemp_tide": ("Arus Biru", "Arena dibanjiri blue silver dan memperlambat semuanya."),
    "bsemp_regrowth": ("Tumbuh Kembali", "Jaringan menyatu kembali dan menopang seluruh barisan."),
    "bsemp_crown": ("Mahkota Rumput", "Mahkota blue silver naik dan turun di atas arena."),
    # Iron Body
    "iron_body_set": ("Kunci Rangka", "Kunci rangka tubuh: ketahanan dan lapisan besi dingin."),
    "iron_body_ram": ("Bentrok Bahu", "Bahu lebih dulu: hantaman berat ke satu target."),
    "iron_body_brace": ("Barisan Bertahan", "Semua orang di dekatnya tegakkan kaki dan menahan barisan bersama."),
    "iron_body_toll": ("Lonceng Besi", "Seluruh tubuh berdering seperti lonceng, dan arena ikut berdering."),
    # Bone Shell
    "bone_shell_grow": ("Pertumbuhan Cangkang", "Tulang baru menutupi yang lama: ketahanan dan kulit keras."),
    "bone_shell_spear": ("Tombak Tulang", "Serpihan tulang meninggalkan cangkang dan menemukan tempat yang lemah."),
    "bone_shell_bind": ("Kurungan Tulang", "Kurungan menutup: musuh tertangkap dan ditahan di tempat."),
    "bone_shell_burst": ("Cangkang Pecah", "Cangkang pecah ke luar dan pecakannya menemukan setiap musuh."),
    # Stone Skin
    "stone_skin_harden": ("Mengeras", "Kulit berubah menjadi batu untuk waktu yang singkat."),
    "stone_skin_slam": ("Pukulan Batu", "Kepalan berat yang tidak peduli banyak pada targetnya."),
    "stone_skin_sink": ("Tenggelam", "Tanah membuka dan menyeret setiap musuh ke dalamnya."),
    "stone_skin_quake": ("Guncang", "Tubuh batu menghantam tanah dan tanah menjawab."),
    # Flesh Regrowth
    "regrowth_close": ("Menutup", "Luka menutup dan darah kembali mengalir."),
    "regrowth_bloom": ("Bunga", "Jaringan baru menyebar keluar dan menyembuhkan semua orang di dekatmu."),
    "regrowth_thorn": ("Kulit Berduri", "Kulit baru tumbuh berduri dan pukulan berikutnya mengeluarkan darah."),
    "regrowth_rebirth": ("Bunga Lahir Kembali", "Ladang bunga merah terbuka dan menutupi pertarungan."),
    # Flame Body
    "flame_body_ignite": ("Nyalakan", "Tubuh berkobar dan melempar musuh."),
    "flame_body_walk": ("Jalan di Atas Api", "Api berjalan di tanah dan membakar semua yang dilaluinya."),
    "flame_body_ward": ("Perisai Abu", "Api surut jadi armor abu: ketahanan dan tepi yang membakar."),
    "flame_body_pillar": ("Pilar Api", "Pilar api berdiri di tengah arena."),
    # Spirit Veins
    "veins_surge": ("Lonjakan", "Spirit power melompati celah urat dan langkahmu makin cepat."),
    "veins_dash": ("Lompatan Urat", "Kedip menyusuri urat nadi: dua langkah dalam satu langkah."),
    "veins_spark": ("Percikan", "Statis mengalir di lengan dan masuk ke musuh terdekat."),
    "veins_circuit": ("Sirkuit Tertutup", "Setiap urat di arena menyala sekaligus."),
    # Soul Mirror
    "soul_mirror_show": ("Tunjukkan Jiwa", "Cermin menunjukkan musuh bentuk dirinya sendiri, dan musuh goyah."),
    "soul_mirror_drain": ("Isap Gambaran", "Cermin minum apa yang ditunjukkan musuh dan meneruskannya kepadamu."),
    "soul_mirror_refuse": ("Tolak Pandangan", "Setiap musuh yang menatap cermin tertahan di tempat."),
    "soul_mirror_shatter": ("Hancurkan Gambaran", "Cermin pecah menjadi seribu pantulan dan setiap pantulan memotong."),
    # Spirit Lamp
    "lamp_wick": ("Nyalakan Sumbu", "Cahaya stabil menghangatkan satu target dan meredam ketegangannya."),
    "lamp_aura": ("Lingkaran Lentera", "Cahaya menetap di seluruh barisan dan menenangkannya."),
    "lamp_mend": ("Sembuhkan dengan Cahaya", "Lentera membakar habis penyakit semua orang di dekatmu."),
    "lamp_douse": ("Padamkan Arena", "Lentera meredup dan cahaya menelan setiap musuh yang terlihat."),
    # Soul Forge
    "forge_strike": ("Ketuk Tempa", "Satu pukulan palu, masih panas untuk menggigit."),
    "forge_heat": ("Panaskan Bilah", "Ketuk Tempa menyala putih dan seluruh barisan ikut mengeras."),
    "forge_temper": ("Pendinginan", "Celupkan target: damage dan kelemahan bertanda yang bertahan."),
    "forge_bellows": ("Tiupan Blower", "Blower menarik napas dan api bergulung di seluruh arena."),
    # Star Array
    "star_first_point": ("Titik Pertama", "Satu titik susunan menyala dan jatuh ke satu target."),
    "star_binding": ("Susunan Pengikat", "Susunan melipat ruang dan menahan setiap musuh di dalamnya."),
    "star_chain": ("Konstelasi", "Cahaya berjalan dari bintang ke bintang dan menghantam tiga musuh bergantian."),
    "star_collapse": ("Runtuh", "Susunan melipat dirinya sendiri dan arena menjadi putih."),
    # Demon Eye
    "demon_eye_stare": ("Tatap", "Satu tatapan cukup membuat target ragu."),
    "demon_eye_drain": ("Tarik Jiwa", "Mata menarik jiwa target dan tarikan itu sampai ke tanganmu."),
    "demon_eye_hex": ("Kutukan", "Kutukan menimpa semua yang terlalu lama melihat."),
    "demon_eye_open": ("Mata Terbuka", "Mata terbuka penuh dan arena dilihat melaluinya."),
    # Ancient Seal
    "seal_stamp": ("Segel Pertama", "Segel ditekan ke satu target dan meninggalkan tandanya."),
    "seal_bond": ("Ikatan Tersegel", "Segel mengikat seluruh barisan terhadap bahaya."),
    "seal_ward": ("Perlindungan Kuno", "Perlindungan kuno menjawab panggilan dan arena menyingkirkan pukulan berikutnya."),
    "seal_unbroken": ("Segel Tak Terputus", "Segel terbuka dan seluruh susunan menembak sekaligus."),
}

# ---------------------------------------------------------------- pelabelsan lain

LINEAGE_LABELS = {
    "beast": ("Beast", "Hewan"),
    "tool": ("Tool", "Alat"),
    "body": ("Body", "Tubuh"),
    "special": ("Special", "Khusus"),
}

AFFINITY_LABELS = {
    "none": ("No Element", "Tanpa Elemen"),
    "fire": ("Fire", "Api"),
    "ice": ("Ice", "Es"),
    "lightning": ("Lightning", "Petir"),
    "wind": ("Wind", "Angin"),
    "water": ("Water", "Air"),
    "earth": ("Earth", "Tanah"),
    "poison": ("Poison", "Racun"),
    "nature": ("Nature", "Tumbuhan"),
    "light": ("Light", "Cahaya"),
    "dark": ("Dark", "Gelap"),
}

TYPE_LABELS = {
    "serangan": ("Attack", "Serangan"),
    "pertahanan": ("Defense", "Pertahanan"),
    "penyembuhan": ("Healing", "Penyembuhan"),
}

GUI_LABELS = {
    "gui.soulland.category.serangan": ("Attack", "Serangan"),
    "gui.soulland.category.pertahanan": ("Defense", "Pertahanan"),
    "gui.soulland.category.penyembuhan": ("Healing", "Penyembuhan"),
    "gui.soulland.lineage.all": ("All", "Semua"),
    "gui.soulland.category.all": ("All", "Semua"),
    "gui.soulland.awakening.lineage": ("Lineage: %s", "Lineage: %s"),
    "gui.soulland.awakening.affinity": ("Affinity: %s", "Afinitas: %s"),
    "gui.soulland.awakening.counter": ("%s of %s", "%s dari %s"),
}

MESSAGE_LABELS = {
    "message.soulland.affinity.resonant": (
        "Affinity resonates: damage +20% against your target.",
        "Afinitas resonan: damage +20% terhadap lawannya.",
    ),
    "message.soulland.affinity.discordant": (
        "Affinity clashes: damage -15% against your target.",
        "Afinitas disonan: damage -15% terhadap lawannya.",
    ),
}

RARITY_LABELS = {
    "common": ("Common", "Common"),
    "uncommon": ("Uncommon", "Uncommon"),
    "rare": ("Rare", "Rare"),
    "epic": ("Epic", "Epic"),
    "legendary": ("Legendary", "Legendary"),
}

# ---------------------------------------------------------------- baca registry

SOUL_PATTERN = re.compile(
    r'register\(new MartialSoul\(\s*"(?P<soul_id>[^"]+)",\s*"(?P<name>[^"]*)",\s*"[^"]*",'
    r'\s*"(?P<owner>[^"]*)",\s*"[^"]*",\s*"(?P<stype>[^"]*)",\s*"[^"]*",\s*"(?P<rarity>[^"]*)",'
    r'\s*SoulLineage\.(?P<lineage>\w+),\s*Element\.(?P<affinity>\w+),'
    r'\s*"(?P<description>(?:[^"\\]|\\.)*)",',
    re.S,
)
SKILL_PATTERN = re.compile(
    r'(?:skill|arc)\("(?P<skill_id>[^"]+)",\s*"(?P<skill_name>[^"]*)",\s*"(?P<skill_description>(?:[^"\\]|\\.)*)"',
    re.S,
)


def read_registry():
    """Baca id soul, skill, owner, tipe, lineage, dan afinitas dari kode.

    Nama dan deskripsi bahasa Inggris juga diambil dari sini, supaya tidak
    ada teks yang ditulis dua kali di dua tempat.
    """
    source = SOUL_FILE.read_text(encoding="utf-8")
    souls = []
    for match in SOUL_PATTERN.finditer(source):
        soul = match.groupdict()
        souls.append({
            "id": soul["soul_id"],
            "name": soul["name"],
            "description": soul["description"],
            "owner": soul["owner"],
            "type": soul["stype"],
            "rarity": soul["rarity"],
            "lineage": soul["lineage"].lower(),
            "affinity": soul["affinity"].lower(),
        })
    skills = []
    for match in SKILL_PATTERN.finditer(source):
        skill = match.groupdict()
        skills.append((skill["skill_id"], skill["skill_name"], skill["skill_description"]))
    return souls, skills


def type_slug(stype: str) -> str:
    return re.sub(r"[^a-z0-9]+", "_", stype.lower()).strip("_")


def build_entries(souls, skills):
    """Bentuk seluruh kunci baru yang ditambahkan ke kedua file bahasa.

    Nilai bahasa Inggris diambil dari registry Java supaya tidak ada teks yang
    ditulis dua kali. Tabel di atas hanya berisi terjemahan Bahasa Indonesia.
    """
    entries = {}

    for soul in souls:
        soul_id = soul["id"]
        if soul_id in SOULS:
            name_id, description_id = SOULS[soul_id]
            entries[f"soul.soulland.{soul_id}.name"] = (soul["name"], name_id)
            entries[f"soul.soulland.{soul_id}.description"] = (soul["description"], description_id)
        entries[f"soul.soulland.owner.{soul_id}"] = (soul["owner"], soul["owner"])
        entries[f"soul.soulland.type.{type_slug(soul['type'])}"] = TYPE_LABELS.get(
            type_slug(soul["type"]), (soul["type"], soul["type"]))
        entries[f"soul.soulland.rarity.{soul['rarity'].lower()}"] = RARITY_LABELS[soul["rarity"].lower()]
        entries[f"soul.soulland.lineage.{soul['lineage']}"] = LINEAGE_LABELS[soul["lineage"]]
        entries[f"soul.soulland.affinity.{soul['affinity']}"] = AFFINITY_LABELS[soul["affinity"]]
        entries[f"item.soulland.manifestation_{soul_id}"] = (
            f"{soul['name']} Manifestation",
            f"Manifestasi {soul['name']}",
        )

    for skill_id, skill_name, skill_description in skills:
        if skill_id not in SKILLS:
            continue
        name_id, description_id = SKILLS[skill_id]
        entries[f"skill.soulland.{skill_id}.name"] = (skill_name, name_id)
        entries[f"skill.soulland.{skill_id}.description"] = (skill_description, description_id)

    entries.update(GUI_LABELS)
    entries.update(MESSAGE_LABELS)
    return entries


def main() -> None:
    souls, skills = read_registry()
    entries = build_entries(souls, skills)

    en_path = LANG / "en_us.json"
    id_path = LANG / "id_id.json"
    en = json.loads(en_path.read_text(encoding="utf-8"))
    id_ = json.loads(id_path.read_text(encoding="utf-8"))

    # Skill yang kuncinya belum ada di file bahasa wajib punya terjemahan.
    missing_skill = [skill_id for skill_id, _, _ in skills
                     if f"skill.soulland.{skill_id}.name" not in en and skill_id not in SKILLS]
    if missing_skill:
        print(f"ERROR: {len(missing_skill)} skill tanpa terjemahan: {missing_skill[:8]}")
        return 1

    added = 0
    for key, (en_value, id_value) in entries.items():
        if key not in en:
            en[key] = en_value
            added += 1
        if key not in id_:
            id_[key] = id_value
            added += 1

    en_path.write_text(json.dumps(en, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    id_path.write_text(json.dumps(id_, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    print(f"added {added} entries; en_us={len(en)} keys, id_id={len(id_)} keys")
    if set(en) != set(id_):
        print("WARNING: key sets differ")
        print("only en:", sorted(set(en) - set(id_))[:10])
        print("only id:", sorted(set(id_) - set(en))[:10])
    return 0


if __name__ == "__main__":
    sys.exit(main())
