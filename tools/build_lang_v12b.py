#!/usr/bin/env python3
"""Tambahkan kunci bahasa v1.2B ke en_us.json dan id_id.json.

Skrip ini idempoten: kunci yang sudah ada tidak ditimpa, jadi aman dijalankan
ulang. Isi kedua file dijaga tetap sinkron (jumlah dan nama kunci sama).
"""
import json
import pathlib

ROOT = pathlib.Path(__file__).resolve().parent.parent
LANG = ROOT / "src/main/resources/assets/soulland/lang"

NEW = {
    # ---------------------------------------------------------------- item
    "item.soulland.soul_essence": ("Soul Essence", "Esensi Jiwa"),
    "item.soulland.soul_core": ("Soul Core", "Inti Jiwa"),
    "item.soulland.spirit_ring_1000000": ("Spirit Ring (1,000,000 Years)", "Spirit Ring (1.000.000 Tahun)"),
    "item.soulland.alchemy_station": ("Alchemy Station", "Station Alchemy"),
    "item.soulland.foundation_pill": ("Foundation Pill", "Pil Fondasi"),

    # ---------------------------------------------------------------- rank
    "rank.soulland.0": ("Spirit Scholar", "Murid Spirit"),
    "rank.soulland.1": ("Spirit Master", "Master Spirit"),
    "rank.soulland.2": ("Spirit Grandmaster", "Agam Spirit"),
    "rank.soulland.3": ("Spirit Elder", "Tetua Spirit"),
    "rank.soulland.4": ("Spirit Ancestor", "Leluhur Spirit"),
    "rank.soulland.5": ("Spirit King", "Raja Spirit"),
    "rank.soulland.6": ("Spirit Emperor", "Kaisar Spirit"),
    "rank.soulland.7": ("Spirit Sage", "Cakrawala Spirit"),
    "rank.soulland.8": ("Spirit Douluo", "Douluo Spirit"),
    "rank.soulland.9": ("Title Douluo", "Douluo Bergelar"),
    "rank.soulland.10": ("Grand Title Douluo", "Douluo Bergelar Agung"),
    "rank.soulland.11": ("Limit Douluo", "Douluo Batas"),
    "rank.soulland.12": ("Quasi-God", "Semi-Dewa"),
    "rank.soulland.13": ("God", "Dewa"),

    # ---------------------------------------------------------------- hud
    "gui.soulland.hud.level": ("Lv.%s  %s", "Lv.%s  %s"),
    "gui.soulland.hud.rings": ("Ring %s/%s", "Ring %s/%s"),
    "gui.soulland.hud.foundation": ("Foundation %s%%", "Fondasi %s%%"),
    "gui.soulland.hud.energy": ("SP %s/%s", "SP %s/%s"),
    "gui.soulland.hud.chain": ("Chain %s (+%s%% damage)", "Rantai %s (+%s%% damage)"),

    # ---------------------------------------------------------------- bahasa
    "gui.soulland.language.title": ("Choose Language", "Pilih Bahasa"),
    "gui.soulland.language.subtitle": (
        "Set the language for the whole Soul Land interface.",
        "Tentukan bahasa untuk seluruh antarmuka Soul Land."),
    "gui.soulland.language.id": ("Bahasa Indonesia", "Bahasa Indonesia"),
    "gui.soulland.language.en": ("English", "English"),
    "gui.soulland.language.hint": (
        "Tab or arrows to move, Enter to choose.",
        "Tab atau panah untuk pindah, Enter untuk memilih."),
    "gui.soulland.language.pending": ("Saving your language choice...", "Menyimpan pilihan bahasa..."),
    "gui.soulland.language.required": (
        "Choose a language to continue to the Awakening Ceremony.",
        "Pilih bahasa untuk melanjutkan ke Upacara Kebangkitan."),
    "gui.soulland.language.note": (
        "This choice drives the HUD, the ceremony, the guide book and command messages.",
        "Pilihan ini dipakai HUD, upacara, buku panduan, dan pesan perintah."),

    # ---------------------------------------------------------------- alchemy
    "gui.soulland.alchemy.title": ("Alchemy Station", "Station Alchemy"),
    "gui.soulland.alchemy.subtitle": (
        "Brew pills from the spirit materials you carry.",
        "Racik pill dari bahan spirit yang kamu bawa."),
    "gui.soulland.alchemy.ready": ("Ready to brew", "Siap diracik"),
    "gui.soulland.alchemy.missing": ("Missing materials", "Bahan kurang"),
    "gui.soulland.alchemy.hint": (
        "Click a recipe to brew it. Arrows select, Enter brews, Escape closes.",
        "Klik sebuah resep untuk meracik. Panah memilih, Enter meracik, Escape menutup."),
    "gui.soulland.alchemy.recipe.cultivation": ("Cultivation Pill", "Pil Kultivasi"),
    "gui.soulland.alchemy.recipe.swift": ("Swift Pill", "Pil Kilat"),
    "gui.soulland.alchemy.recipe.foundation": ("Foundation Pill", "Pil Fondasi"),

    # ---------------------------------------------------------------- message
    "message.soulland.breakthrough_success": (
        "The breakthrough ceremony succeeded! New rank: %s.",
        "Upacara terobosan berhasil! Peringkat baru: %s."),
    "message.soulland.breakthrough_failed": (
        "The breakthrough slipped away. Your foundation weakens, meditate again.",
        "Terobosan goyah. Fondasi melemah, kamu harus memeditasi lagi."),
    "message.soulland.breakthrough_chance": (
        "Breakthrough chance: %s%% (foundation %s/100).",
        "Peluang terobosan: %s%% (fondasi %s/100)."),
    "message.soulland.breakthrough_progress": ("Breakthrough ceremony: %s/3", "Upacara terobosan: %s/3"),
    "message.soulland.breakthrough_essence": (
        "The breakthrough ceremony needs 1 Soul Essence.",
        "Butuh 1 Esensi Jiwa untuk upacara terobosan."),
    "message.soulland.gate_blocked": (
        "§6Rank gate %s (%s) blocks your path. Hunt soul beasts, absorb a new Spirit Ring, "
        + "then hold C and meditate 3 seconds with 1 Soul Essence to break through.",
        "§6Gerbang level %s (%s) menutup jalanmu. "
        + "Burulah soul beast dan serap Spirit Ring baru, lalu tahan C "
        + "bermeditasi 3 detik dengan 1 Esensi Jiwa untuk upacara terobosan."),
    "message.soulland.level_up": (
        "§a⬆ Spirit Power level rises to %s: %s!",
        "§a⬆ Level Spirit Power naik ke %s: %s!"),
    "message.soulland.skill_unlock_next": (
        "The next martial soul skill opens at the next spirit ring.",
        "Skill martial soul terbuka di spirit ring berikutnya."),
    "message.soulland.tribulation_ready": (
        "§eThe sky thunders. The god gate opens: tribulation will descend.",
        "§eLangit bergemuruh. Ambang dewa terbuka: tribulasi akan turun."),
    "message.soulland.tribulation_world": (
        "§6The sky above %s thunders. Someone reached the god gate!",
        "§6Langit di atas %s bergemuruh. Seseorang mencapai ambang dewa!"),
    "message.soulland.foundation_gain": (
        "§bYour cultivation foundation strengthens: %s/100.",
        "§bFondasi kultivasi menguat: %s/100."),
    "message.soulland.foundation_low": (
        "§eYour foundation is not ready for the breakthrough ceremony yet (need %s, now %s).",
        "§eFondasi belum cukup untuk upacara terobosan (butuh %s, sekarang %s)."),
    "message.soulland.meditation_broken": (
        "Meditation broke because you moved.",
        "Meditasi terputus karena kamu bergerak."),

    "message.soulland.ring.absorbed": (
        "§a✦ Spirit Ring %s years absorbed! You now carry %s spirit rings. ✦",
        "§a✦ Spirit Ring %s tahun diserap! Sekarang kamu punya %s spirit ring. ✦"),
    "message.soulland.ring.failed": (
        "§cThe absorption failed! The %s year Spirit Ring shattered.",
        "§cPenyerapannya gagal! Spirit Ring %s tahun hancur."),
    "message.soulland.ring.backlash": (
        "§cBacklash: vitality drops %s%% and your body stiffens.",
        "§cBacklash: vitalitas turun %s%% dan tubuhmu kaku."),
    "message.soulland.ring.mutated": (
        "§d✧ This ring mutated! Its effective age is now %s years and your permanent stat bonus grew. ✧",
        "§d✧ Ring ini bermutasi! Umur efektifnya %s tahun dan bonus stat permanen melonjak. ✧"),
    "message.soulland.ring.not_awakened": (
        "You have not awakened a martial soul yet. Rejoin the world to awaken.",
        "Kamu belum membangkitkan martial soul. Masuk kembali ke dunia untuk awakening."),
    "message.soulland.ring.full": (
        "Your spirit rings are full (%s rings). You already stand at the peak of humanity.",
        "Spirit ring-mu sudah penuh (%s ring). Kamu sudah di puncak manusia."),
    "message.soulland.ring.risk_warning": (
        "§cA %s year Spirit Ring is far above level %s. Risk %s: %s%% chance to fail.",
        "§cSpirit Ring %s tahun terlalu berat untuk level %s. Risiko %s: gagal %s%%."),
    "message.soulland.ring.risk_confirm": (
        "§eHold Shift and right click again to confirm the risky absorption.",
        "§eTahan Shift dan klik kanan lagi untuk mengonfirmasi penyerapan berisiko."),
    "message.soulland.ring.risk_safe": ("safe", "aman"),
    "message.soulland.ring.risk_mild": ("light", "ringan"),
    "message.soulland.ring.risk_moderate": ("medium", "sedang"),
    "message.soulland.ring.risk_severe": ("heavy", "berat"),
    "message.soulland.ring.skill_1": ("§eSkill 1 is open. Press [R] to use it.", "§eSkill 1 terbuka. Tekan [R] untuk menggunakannya."),
    "message.soulland.ring.skill_2": ("§eSkill 2 is open. Press [G] to use it.", "§eSkill 2 terbuka. Tekan [G] untuk menggunakannya."),
    "message.soulland.ring.skill_3": ("§eSkill 3 is open. Press [F] to use it.", "§eSkill 3 terbuka. Tekan [F] untuk menggunakannya."),
    "message.soulland.ring.skill_4": ("§eSkill 4 is open. Press [V] to use it.", "§eSkill 4 terbuka. Tekan [V] untuk menggunakannya."),

    "message.soulland.info.header": ("Cultivation Status", "Status Kultivasi"),
    "message.soulland.info.soul": ("Martial Soul: %s (%s)", "Martial Soul: %s (%s)"),
    "message.soulland.info.level": ("Spirit Power Level: %s - %s", "Level Spirit Power: %s - %s"),
    "message.soulland.info.xp": ("XP: %s / %s", "XP: %s / %s"),
    "message.soulland.info.foundation": (
        "Foundation: %s / %s (breakthrough chance %s%%)",
        "Fondasi: %s / %s (peluang terobosan %s%%)"),
    "message.soulland.info.rings": ("Spirit Ring: %s / %s", "Spirit Ring: %s / %s"),
    "message.soulland.info.mutations": ("Mutated rings: %s", "Ring bermutasi: %s"),
    "message.soulland.info.energy": ("Spirit Power: %s / %s", "Spirit Power: %s / %s"),
    "message.soulland.info.sect": ("Sect: %s", "Sekte: %s"),

    "message.soulland.soul.none": (
        "You have not awakened a martial soul yet.",
        "Kamu belum membangkitkan martial soul."),
    "message.soulland.soul.owner": ("§7Original owner: %s | Type: %s | %s", "§7Pemilik asli: %s | Tipe: %s | %s"),
    "message.soulland.soul.reroll_blocked": (
        "A martial soul is your fate from birth! Replacing it only works in Creative mode.",
        "Martial soul adalah takdir sejak lahir! Pilih ulang hanya bisa dilakukan dalam mode Creative."),
    "message.soulland.soul.reroll_opened": (
        "The Awakening Ceremony is open. Choose a new martial soul from the list.",
        "Upacara Kebangkitan dibuka. Pilih martial soul baru dari daftar."),

    "message.soulland.sect.none": ("not chosen yet", "belum memilih"),
    "message.soulland.sect.none_choice": (
        "You have not joined a sect yet. Pick one with /sect choose <name> or press J and open the SECT tab.",
        "Kamu belum memilih sekte. Pilih dengan /sect choose <nama> atau tekan J lalu tab SECT."),
    "message.soulland.sect.unknown": ("Unknown sect. Choices: %s", "Sekte tidak dikenal. Pilihan: %s"),
    "message.soulland.sect.chosen": ("You joined %s.", "Kamu bergabung dengan %s."),

    "message.soulland.language.opened": ("The language screen is open.", "Layar pilih bahasa dibuka."),

    "message.soulland.contract.progress": (
        "Hunt contract: %s/%s soul beasts.",
        "Kontrak perburuan: %s/%s soul beast."),
    "message.soulland.contract.done": (
        "Hunt contract finished. Reward: a 100 year spirit ring and 4 soul essences.",
        "Kontrak perburuan selesai. Hadiah: spirit ring 100 tahun dan 4 esensi jiwa."),

    "message.soulland.alchemy.unknown": ("That recipe does not exist.", "Resep itu tidak dikenal."),
    "message.soulland.alchemy.missing": (
        "You lack the materials to brew %s.",
        "Bahanmu belum cukup untuk meracik %s."),
    "message.soulland.alchemy.crafted": (
        "§bBrewing done: %s x%s.",
        "§bRacikan selesai: %s x%s."),

    "message.soulland.awakening.title": (
        "§eMARTIAL SOUL AWAKENING CEREMONY",
        "§eUPACARA KEBANGKITAN MARTIAL SOUL"),
    "message.soulland.awakening.soul": (
        "§fYour Martial Soul: §b%s §7(%s)",
        "§fMartial Soul-mu: §b%s §7(%s)"),
    "message.soulland.awakening.owner": (
        "§7Original owner: %s | Type: %s",
        "§7Pemilik asli: %s | Tipe: %s"),
    "message.soulland.awakening.guide": (
        "§fRead the §a[Soul Land Guide Book]§f in your inventory to learn everything.",
        "§fBaca §a[Buku Panduan Dunia Soul Land]§f di inventory-mu untuk mempelajari segalanya."),
    "message.soulland.awakening.hunt": (
        "§fHunt soul beasts, absorb Spirit Rings, then defeat the §5Ender Dragon§f!",
        "§fBerburu soul beast, serap Spirit Ring, lalu kalahkan §5Ender Dragon§f!"),
    "message.soulland.awakening.changed": (
        "§bYour martial soul changed to: %s (%s)",
        "§bMartial soul-mu berubah menjadi: %s (%s)"),

    # ---------------------------------------------------------------- tooltip
    "tooltip.soulland.ring.age": ("Spirit Ring %s years old", "Spirit Ring berusia %s tahun"),
    "tooltip.soulland.ring.safe_level": (
        "Safe limit: level %s (you are level %s)",
        "Batas aman: level %s (kamu level %s)"),
    "tooltip.soulland.ring.safe_level_unknown": ("Safe limit: level %s", "Batas aman: level %s"),
    "tooltip.soulland.ring.risk_safe": (
        "Risk: safe, the absorption always succeeds.",
        "Risiko: aman, penyerapan selalu berhasil."),
    "tooltip.soulland.ring.risk_mild": (
        "Light risk: %s%% chance to fail, the ring shatters.",
        "Risiko ringan: gagal %s%%, ring hancur."),
    "tooltip.soulland.ring.risk_moderate": (
        "Medium risk: %s%% chance to fail, backlash costs %s%% of max health.",
        "Risiko sedang: gagal %s%%, backlash %s%% HP maksimum."),
    "tooltip.soulland.ring.risk_severe": (
        "Heavy risk: %s%% chance to fail, backlash costs %s%% of max health.",
        "Risiko berat: gagal %s%%, backlash %s%% HP maksimum."),
    "tooltip.soulland.ring.bonus": (
        "Bonus: +2 max health and +0.5 attack damage",
        "Bonus: +2 HP maksimum dan +0.5 attack damage"),
    "tooltip.soulland.ring.use": ("Right click to absorb it.", "Klik kanan untuk menyerap."),
    "tooltip.soulland.ring.confirm": (
        "Hold Shift while right clicking to confirm a risky absorption.",
        "Tahan Shift saat klik kanan untuk mengonfirmasi penyerapan berisiko."),
    "tooltip.soulland.ring.mutation": (
        "Mutation chance: %s%% (effective age rises one grade, permanent stat bonus).",
        "Peluang mutasi: %s%% (umur efektif naik satu grade, bonus stat permanen)."),

    # ---------------------------------------------------------------- hint
    "hint.soulland.element": (
        "A soul beast name carries its age and element. Your stronger element gives +25% damage, "
        + "the weaker one costs 15%.",
        "Nama soul beast menyebut usianya dan elemennya. Elemen soul-mu yang menang "
  + "memberi +25% damage, kalah memberi -15%."),

    # ---------------------------------------------------------------- book
    "book.soulland.title": ("Soul Land Guide", "Panduan Dunia Soul Land"),
    "book.soulland.author": ("Soul Land Team", "Soul Land Team"),
    "book.soulland.page.welcome": (
        "WELCOME TO THE DOULUO CONTINENT\n\n"
        "You have awakened your Martial Soul! This book explains every system of the Soul Land world: "
        + "cultivation, spirit rings, skills, soul beasts, alchemy and the road to God.\n\n"
        "Read it once, then keep it in your inventory. Every page follows the language you chose.",
        "SELAMAT DATANG DI BENUA DOULUO\n\n"
        "Kamu telah membangkitkan Martial Soul! Buku ini menjelaskan seluruh sistem dunia Soul Land: "
        + "kultivasi, spirit ring, skill, soul beast, alchemy, dan jalan menuju Dewa.\n\n"
        "Baca sekali, lalu simpan di inventory. Semua halaman mengikuti bahasa yang kamu pilih."),
    "book.soulland.page.martial_soul": (
        "1. MARTIAL SOUL (WU HUN)\n\n"
        "The first time you join the world you pick the language, then the Awakening Ceremony opens. "
        + "You choose your martial soul yourself; there is no draw, no lottery.\n"
        "That choice is yours for life, and only Creative mode can replace it with "
        + "/martialsoul reroll.",
        "1. MARTIAL SOUL (WU HUN)\n\n"
        "Saat pertama masuk dunia, kamu memilih bahasa lalu Upacara Kebangkitan terbuka. "
        + "Martial soul kamu pilih sendiri; tidak ada undian, tidak ada lotre.\n"
        "Pilihan itu tetap selamanya, HANYA bisa diubah dalam mode Creative dengan perintah "
        + "/martialsoul reroll."),
    "book.soulland.page.level": (
        "2. SPIRIT POWER LEVEL\n\n"
        "Levels run from 1 to 120. Kill soul beasts (monsters) to gain cultivation XP.\n"
        "Every rank boundary is a gate: you cannot pass it without absorbing a new spirit ring, "
        + "holding a full meditation and spending one Soul Essence.",
        "2. LEVEL SPIRIT POWER\n\n"
        "Level berjalan dari 1 sampai 120. Bunuh soul beast (monster) untuk mendapat XP kultivasi.\n"
        "Setiap batas peringkat adalah gerbang: kamu tidak bisa melewatinya tanpa menyerap spirit ring "
        + "baru, bermeditasi penuh, dan memakai satu Esensi Jiwa."),
    "book.soulland.page.rings": (
        "3. SPIRIT RING\n\n"
        "Soul beasts drop rings according to their age: 100 years, 1,000 years, 10,000 years, "
        + "100,000 years and the gold tier of 1,000,000 years.\n"
        "The gold tier falls from the Ender Dragon and sometimes from the Warden.\n\n"
        "RISK: every ring has a safe level. Absorbing above it is dangerous: the server rolls your "
        + "chance to fail, and a failure destroys the ring and hits you with backlash. Hold Shift to "
        + "confirm a risky absorption.\n\n"
        "MUTATION: every successful absorption has a 1% chance to mutate the ring. Its effective age "
        + "rises one grade and your permanent stat bonus grows.",
        "3. SPIRIT RING\n\n"
        "Soul beast menjatuhkan ring sesuai usianya: 100 tahun, 1.000 tahun, 10.000 tahun, "
        + "100.000 tahun, dan tier emas 1.000.000 tahun.\n"
        "Tier emas jatuh dari Ender Dragon dan kadang dari Warden.\n\n"
        "RISIKO: tiap ring punya batas aman level. Menyerap di atas batas itu berbahaya: server "
        + "menghitung peluang gagalmu, dan kegagalan menghancurkan ring serta memberi backlash. "
        + "Tahan Shift untuk mengonfirmasi penyerapan berisiko.\n\n"
        "MUTASI: setiap penyerapan berhasil punya peluang 1% membuat ring bermutasi. Umur efektifnya "
        + "naik satu grade dan bonus stat permanenmu bertambah."),
    "book.soulland.page.foundation": (
        "4. FOUNDATION\n\n"
        "Meditation builds your foundation from 0 to 100. The foundation sets your breakthrough chance: "
        + "at 20 you may attempt the ceremony, at 100 the breakthrough always succeeds.\n"
        "A failed ceremony costs 25 foundation points, so meditate again before you try once more.\n\n"
        "The Foundation Pill adds 25 points at once. Reach level %s and the god gate opens: the sky "
        + "thunders over you.",
        "4. FONDASI\n\n"
        "Meditasi membangun fondasimu dari 0 sampai 100. Fondasi menentukan peluang terobosan: "
        + "di 20 kamu boleh mencoba upacara, di 100 terobosan selalu berhasil.\n"
        "Upacara yang gagal mengurangi 25 poin fondasi, jadi medic lagi sebelum mencoba lagi.\n\n"
        "Pil Fondasi menambah 25 poin sekaligus. Capai level %s dan ambang dewa terbuka: "
        + "langit bergemuruh di atasmu."),
    "book.soulland.page.skills": (
        "5. MARTIAL SOUL SKILLS\n\n"
        "Every martial soul has 4 skills. They open at spirit ring 1, 3, 5 and 7.\n"
        "Skills cost spirit power (the blue bar in the HUD) and have their own cooldown.\n"
        "Press R, G, F and V to use them, or open the Soul Card with J to rearrange the loadout.",
        "5. SKILL MARTIAL SOUL\n\n"
        "Setiap martial soul punya 4 skill. Skill terbuka di spirit ring 1, 3, 5, dan 7.\n"
        "Skill memakai Spirit Power (biru di HUD) dan punya cooldown sendiri.\n"
        "Tekan R, G, F, dan V untuk menggunakannya, atau buka Kartu Soul dengan J untuk menyusun loadout."),
    "book.soulland.page.beasts": (
        "6. SOUL BEAST AND BOSSES\n\n"
        "Monsters are soul beasts. Their name carries their age and element, so you always know what "
        + "you are hunting.\n"
        "Bosses of this world are far stronger than vanilla: Elder Guardian, Wither, Warden, and the "
        + "ENDER DRAGON as the final boss.",
        "6. SOUL BEAST DAN BOSS\n\n"
        "Monster adalah soul beast. Namanya menyebut usianya dan elemennya, jadi selalu tahu apa yang "
        + "kamu buru.\n"
        "Boss dunia ini jauh lebih kuat dari vanilla: Elder Guardian, Wither, Warden, dan "
        + "ENDER DRAGON sebagai boss akhir."),
    "book.soulland.page.alchemy": (
        "7. ALCHEMY STATION\n\n"
        "Craft an Alchemy Station from two Spirit Iron Ingots, a Soul Crystal Shard and a Soul Essence, "
        + "then right click it to open the brewing screen.\n"
        "Cultivation Pill: Spirit Dew, Spirit Ember, 1 Soul Essence.\n"
        "Swift Pill: Spirit Moss, Spirit Dew, 1 Soul Essence.\n"
        "Foundation Pill: Spirit Ember, Spirit Moss, 2 Soul Essence.",
        "7. STATION ALCHEMY\n\n"
        "Racik Station Alchemy dari dua Ingot Besi Spirit, Serpihan Kristal Soul, dan satu Esensi Jiwa, "
        + "lalu klik kanan untuk membuka layar peracikan.\n"
        "Pil Kultivasi: Daun Embun Spirit, Daun Bara Spirit, 1 Esensi Jiwa.\n"
        "Pil Kilat: Tunas Lumut Spirit, Daun Embun Spirit, 1 Esensi Jiwa.\n"
        "Pil Fondasi: Daun Bara Spirit, Tunas Lumut Spirit, 2 Esensi Jiwa."),
    "book.soulland.page.commands": (
        "8. COMMANDS\n\n"
        "/soulinfo - your cultivation status\n"
        "/soul language - open the language screen\n"
        "/martialsoul info - details about your martial soul\n"
        "/martialsoul reroll - replace your martial soul (Creative only)\n"
        "/sect choose <name> - join a sect\n"
        "/cultivation set <player> <level> - admin",
        "8. PERINTAH\n\n"
        "/soulinfo - status kultivasimu\n"
        "/soul language - buka layar pilih bahasa\n"
        "/martialsoul info - detail martial soul-mu\n"
        "/martialsoul reroll - pilih ulang martial soul (Creative saja)\n"
        "/sect choose <nama> - bergabung dengan sekte\n"
        "/cultivation set <pemain> <level> - admin"),
    "book.soulland.page.multiplayer": (
        "9. MULTIPLAYER\n\n"
        "Every system runs on the server: ceremony, levels, rings, skills and alchemy are saved per "
        + "player.\n"
        "Invite friends, build a team (the Pagoda support soul helps a lot) and take the bosses down "
        + "together.",
        "9. MULTIPLAYER\n\n"
        "Seluruh sistem berjalan di server: upacara, level, ring, skill, dan alchemy tersimpan "
        + "per pemain.\n"
        "Ajak temanmu, bentuk tim (support Pagoda sangat membantu!), dan taklukkan boss bersama."),
    "book.soulland.page.closing": (
        "KEEP CULTIVATING\n\n"
        "\"No martial soul is rubbish; only rubbish spirit masters are.\" - Grandmaster Yu Xiaogang\n\n"
        "Train, hunt as a team and never give up. The road to God starts at level 1.",
        "SELAMAT BERKULTIVASI!\n\n"
        "\"Tidak ada martial soul yang sampah, yang ada hanya spirit master yang sampah.\" "
        + "- Agam Yu Xiaogang\n\n"
        "Berlatihlah, berburu bersama tim, dan jangan menyerah. Jalan menuju dewa dimulai dari level 1."),
    "book.soulland.page.ranks.title": ("CULTIVATION RANK LADDER", "TANGGA PERINGKAT KULTIVASI"),
    "book.soulland.page.ranks.note": (
        "Every rank boundary is a gate: absorb a new spirit ring and meditate fully before you climb.",
        "Setiap batas peringkat adalah gerbang: serap spirit ring baru dan bermeditasi penuh sebelum naik."),
    "book.soulland.page.ranks.goal": (
        "THE FINAL GOAL\n\n"
        "The ladder ends at God, starting from level %s. Reaching it raises the tribulation flag: "
        + "the sky thunders and lightning strikes. The full tribulation system arrives in v1.6, so v1.2B "
        + "only marks the threshold.\n\n"
        "Absorb the gold 1,000,000 year ring, defeat the Ender Dragon, and your name becomes legend "
        + "on the Douluo Continent.",
        "TUJUAN AKHIR\n\n"
        "Tangga ini berakhir di Dewa, mulai level %s. Mencapainya memunculkan tanda tribulasi: langit "
        + "bergemuruh dan petir menyambar. Sistem tribulasi penuh menyusul di v1.6, jadi v1.2B hanya "
        + "menandai ambangnya.\n\n"
        "Serap ring emas 1.000.000 tahun, kalahkan Ender Dragon, dan namamu akan melegenda di "
        + "Benua Douluo."),
    "book.soulland.label.mandarin": ("Mandarin name: %s", "Nama Mandarin: %s"),
    "book.soulland.label.owner": ("Owner: %s", "Pemilik: %s"),
    "book.soulland.label.traits": ("Type: %s | %s | Element: %s", "Tipe: %s | %s | Elemen: %s"),
    "book.soulland.label.evolution": ("Evolved form: %s", "Bentuk evolusi: %s"),

    # ---------------------------------------------------------------- guide
    "guide.soulland.header": ("GUIDE", "PANDUAN"),
    "guide.soulland.title.1": ("How to Play", "Cara Main"),
    "guide.soulland.title.2": ("Martial Soul", "Martial Soul"),
    "guide.soulland.title.3": ("Skills and Loadout", "Skill dan Loadout"),
    "guide.soulland.title.4": ("Spirit Ring", "Spirit Ring"),
    "guide.soulland.title.5": ("Cultivation", "Kultivasi"),
    "guide.soulland.title.6": ("Sects", "Sekte"),
    "guide.soulland.title.7": ("Combat", "Pertarungan"),
    "guide.soulland.section.1": (
        "1. Hunt soul beasts. The name carries the age and the element, for example "
        + "\"1,000 Years (Fire)\" or \"10,000 Years (Ice)\".\n"
        "2. Absorb the spirit rings they drop.\n"
        "3. Rebuild your loadout in the Soul Card (J), tab SKILLS. Five slots, any order.\n"
        "4. Chain skills to raise damage: every link adds +8% for 4 seconds, up to +40%.\n"
        "5. Pick your target by element. Win +25% damage, lose -15%.",
        "1. Buru soul beast. Namanya menyebut usianya dan elemennya, "
        + "misalnya \"1.000 Tahun (Api)\" atau \"10.000 Tahun (Es)\".\n"
        "2. Serap spirit ring yang jatuh.\n"
        "3. Susun loadout di Kartu Soul (J), tab SKILLS. Lima slot, urutan bebas.\n"
        "4. Manfaatkan rantai soul: skill beruntun menambah +8% damage per mata, maksimal +40%.\n"
        "5. Pilih target sesuai elemen. Menang +25% damage, kalah -15%."),
    "guide.soulland.section.2": (
        "The first time you join the world you pick the language, then the Awakening Ceremony opens.\n"
        "You choose your martial soul yourself. There is no draw and no lottery.\n\n"
        "The natural order:\n"
        "1. Kill soul beasts for cultivation XP.\n"
        "2. Absorb the spirit rings they drop.\n"
        "3. Confirm risky absorptions with Shift.\n"
        "4. Build your skill loadout in the Soul Card, tab SKILLS.\n"
        "5. Run the breakthrough ceremony to climb a rank.",
        "Saat pertama masuk dunia, kamu memilih bahasa, lalu Upacara Kebangkitan terbuka.\n"
        "Martial soul kamu pilih sendiri. Tidak ada undian, tidak ada lotre.\n\n"
        "Urutan yang wajar:\n"
        "1. Bunuh soul beast untuk mendapat XP kultivasi.\n"
        "2. Serap spirit ring yang jatuh dari soul beast.\n"
        "3. Konfirmasi penyerapan berisiko dengan Shift.\n"
        "4. Rakit loadout skill di tab SKILLS pada kartu soul.\n"
        "5. Jalankan upacara terobosan untuk naik peringkat."),
    "guide.soulland.section.3": (
        "Every martial soul has 4 skills that open at spirit ring 1, 3, 5 and 7.\n\n"
        "Five button slots (R G F V Z) can be filled freely. Open the Soul Card with J, "
        + "go to the SKILLS tab, then click a skill to place it.\n"
        "Click a filled slot to empty it and fall back to the default order.\n\n"
        "Each skill has its own effect shape: ball, line, cone, ring, disc, pillar or storm.",
        "Setiap martial soul punya 4 skill yang terbuka di spirit ring 1, 3, 5, dan 7.\n\n"
        "Lima slot tombol (R G F V Z) bisa diisi skill bebas. Buka kartu soul dengan J, "
        + "masuk tab SKILLS, lalu klik skill untuk memasangnya.\n"
        "Klik slot untuk mengosongkan dan kembali ke urutan bawaan.\n\n"
        "Tiap skill punya bentuk efek sendiri: bola, garis, kerucut, cincin, cakram, pilar, atau badai."),
    "guide.soulland.section.4": (
        "Spirit rings come from soul beasts according to their age: 100 years, 1,000 years, "
        + "10,000 years, 100,000 years and the gold tier of 1,000,000 years.\n"
        "The gold tier falls from the Ender Dragon and sometimes from the Warden.\n\n"
        "RISK: every ring has a safe level. Absorbing above that level can fail: the ring shatters "
        + "and you take backlash. Hold Shift to confirm the risky absorption.\n"
        "MUTATION: a successful absorption has a 1% chance to mutate the ring, raising its effective "
        + "age one grade and giving a permanent stat bonus.\n\n"
        "Press X to burst your rings: the whole energy is released at once, radius and damage "
        + "follow the number of rings.",
        "Spirit ring datang dari soul beast sesuai usianya: 100 tahun, 1.000 tahun, 10.000 tahun, "
        + "100.000 tahun, dan tier emas 1.000.000 tahun.\n"
        "Tier emas jatuh dari Ender Dragon dan kadang dari Warden.\n\n"
        "RISIKO: tiap ring punya batas aman level. Menyerap di atas level itu bisa gagal: ring "
        + "hancur dan kamu kena backlash. Tahan Shift untuk mengonfirmasi penyerapan berisiko.\n"
        "MUTASI: penyerapan berhasil punya peluang 1% membuat ring bermutasi, menaikkan umur efektif "
        + "satu grade dan memberi bonus stat permanen.\n\n"
        "Tekan X untuk ledakan spirit ring: seluruh energi ring dilepas sekaligus, radius dan damage "
        + "mengikuti jumlah ring."),
    "guide.soulland.section.5": (
        "Cultivation runs from level 1 to 120 and fourteen ranks, from Spirit Scholar to God.\n"
        "Every rank boundary is a gate.\n\n"
        "A gate cannot be crossed with XP alone. Run the breakthrough ceremony: hold C, meditate "
        + "3 seconds, and spend 1 Soul Essence.\n"
        "Your foundation decides the outcome. It builds while you meditate: at 20 you may attempt "
        + "the ceremony, at 100 you always succeed. A failed ceremony costs 25 foundation points.\n\n"
        "Plain meditation (hold C) restores spirit power and adds XP. Moving breaks the meditation.",
        "Kultivasi berjalan dari level 1 sampai 120 dan empat belas peringkat, dari Murid Spirit "
        + "sampai Dewa.\n"
        "Setiap batas peringkat adalah gerbang.\n\n"
        "Gerbang tidak bisa dilewati hanya dengan XP. Jalankan upacara terobosan: tahan C, medic "
        + "3 detik, dan pakai 1 Esensi Jiwa.\n"
        "Fondasimu menentukan hasilnya. Fondasi bertambah selama meditasi: di 20 kamu boleh mencoba, "
        + "di 100 selalu berhasil. Upacara yang gagal mengurangi 25 poin fondasi.\n\n"
        "Meditasi biasa (tahan C) mengisi Spirit Power dan menambah XP. Bergerak akan memutus "
        + "meditasi."),
    "guide.soulland.section.6": (
        "A sect gives a passive bonus that always applies and never hurts. Pick one through the "
        + "SECT tab on the Soul Card, or click a sect medallion directly.\n\n"
        "Tang Sect: +20% cultivation XP.\n"
        "Shrek Sect: +12% damage.\n"
        "Star Luo Sect: +10% movement speed and +10% max health.\n"
        "Seven Treasure Sect: +20% max health and +25% healing.",
        "Sekte memberi bonus pasif yang selalu berlaku dan tidak pernah merugikan. Pilih lewat tab "
        + "SECT pada kartu soul, atau klik lencana sekte langsung.\n\n"
        "Sekte Tang: +20% XP kultivasi.\n"
        "Sekte Shrek: +12% damage.\n"
        "Sekte Bintang Luo: +10% kecepatan gerak dan +10% HP maksimum.\n"
        "Sekte Tujuh Harta: +20% HP maksimum dan +25% penyembuhan."),
    "guide.soulland.section.7": (
        "Soul chain: every skill that lands adds one link. The chain lasts 4 seconds, "
        + "each link adds 8% damage to the next skill, up to 5 links (+40%).\n\n"
        "Element counter: a soul beast name carries its element. +25% damage when your element wins, "
        + "-15% when it loses.\n\n"
        "Blood burn (B): trade 25 HP for 30 Spirit Power, 8 seconds of +50% damage. "
        + "Not allowed below 35 HP.\n\n"
        "Bosses: Elder Guardian 120 HP, Wither 600 HP, Warden 800 HP, "
        + "Ender Dragon 1000 HP as the final boss.",
        "Rantai soul: setiap skill yang berhasil menambah satu mata rantai. Rantai bertahan 4 detik, "
        + "tiap mata menambah 8% damage skill berikutnya, maksimal 5 mata (+40%).\n\n"
        "Lawan elemen: nama soul beast menyebut elemennya. +25% damage kalau elemen soul-mu menang, "
        + "-15% kalau kalah.\n\n"
        "Bakar darah (B): tukar 25 HP jadi 30 Spirit Power, 8 detik damage +50%. "
        + "Tidak boleh dipakai di bawah 35 HP.\n\n"
        "Boss: Elder Guardian 120 HP, Wither 600 HP, Warden 800 HP, "
        + "Ender Dragon 1000 HP sebagai boss akhir."),
    "guide.soulland.tip.1": (
        "Every number on this screen is read from your own state, so it always matches the world.",
        "Semua angka di layar ini dibaca dari state pemain, jadi isinya selalu sama dengan yang "
        + "terjadi di dunia."),
    "guide.soulland.tip.2": (
        "Your martial soul never changes by itself. Only Creative mode can replace it.",
        "Martial soul-mu tidak pernah berubah sendiri. Hanya mode Creative yang bisa menggantinya."),
    "guide.soulland.tip.3": (
        "Skill loadouts are free. Rearrange them as often as you like.",
        "Loadout skill bebas disusun ulang sesering kamu mau."),
    "guide.soulland.tip.4": (
        "Absorbing above the safe level is a gamble. The tooltip always shows your current risk.",
        "Menyerap di atas batas aman level itu percoddsan. Tooltip selalu menampilkan risikonya."),
    "guide.soulland.tip.5": (
        "The breakthrough ceremony needs 3 seconds of still meditation and 1 Soul Essence. "
        + "Moving restarts the timer.",
        "Upacara terobosan butuh 3 detik meditasi penuh dan 1 Esensi Jiwa. Bergerak sedikit saja "
        + "akan mengulang timer."),
    "guide.soulland.tip.6": (
        "You can switch sects at any time, so try the one that fits how you play.",
        "Sekte bisa diganti kapan saja, jadi coba yang cocok dengan cara mainmu."),
    "guide.soulland.tip.7": (
        "The soul chain is the cheapest way to raise damage. Line your skills up in order.",
        "Rantai soul adalah cara paling murah menaikkan damage. Susun skill beruntun sesuai urutan."),
}


def main() -> None:
    en_path = LANG / "en_us.json"
    id_path = LANG / "id_id.json"
    en = json.loads(en_path.read_text(encoding="utf-8"))
    id_ = json.loads(id_path.read_text(encoding="utf-8"))

    added = 0
    for key, (en_value, id_value) in NEW.items():
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


if __name__ == "__main__":
    main()
