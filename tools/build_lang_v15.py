#!/usr/bin/env python3
"""Tambahkan kunci bahasa v1.5 (True Body dan Domain) ke en_us.json dan id_id.json.

Skrip ini idempoten: kunci yang sudah ada tidak ditimpa, jadi aman dijalankan
ulang. Isi kedua file dijaga tetap sinkron (nama dan jumlah kunci sama).

Nama True Body dan Domain dibaca langsung dari katalog Java, jadi nama bahasa
Inggris dan Indonesia hanya ditulis satu kali di kode. Yang tetap ditulis di
sini hanya deskripsi, karena deskripsi bukan data yang dipakai server.

Teks memakai satu tanda persen tunggal (``%s``), karena formatter milik mod
mengganti setiap ``%s`` dengan argumen berurutan dan tidak memproses ``%%``.
"""
import json
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
LANG = ROOT / "src/main/resources/assets/soulland/lang"
TRUE_BODY_FILE = ROOT / "src/main/java/com/soulland/combat/TrueBodyCatalog.java"
DOMAIN_FILE = ROOT / "src/main/java/com/soulland/combat/DomainCatalog.java"

# ---------------------------------------------------------------- deskripsi

# id soul -> (deskripsi True Body EN, ID, deskripsi Domain EN, ID)
DESCRIPTIONS = {
    "hoe": (
        "The plain tool turns into the authority of every furrow it opens.",
        "Alat biasa berubah menjadi penguasa setiap alur yang ia buka.",
        "Nothing grows faster in a field that has already been ploughed.",
        "Tidak ada yang tumbuh lebih cepat di ladang yang sudah dibajak.",
    ),
    "blue_silver_grass": (
        "Ten thousand years of grass learn to stand like a ruler.",
        "Sepuluh ribu tahun rumput belajar berdiri seperti seorang penguasa.",
        "Blue silver floods the ground and holds every footstep inside it.",
        "Blue silver membanjiri tanah dan menahan setiap langkah di dalamnya.",
    ),
    "hell_civet": (
        "Nine lives folded into one shadow that strikes without a sound.",
        "Sembilan nyawa dilipat jadi satu bayangan yang menyerang tanpa suara.",
        "Inside the shadow nothing can see you, and nothing can hide.",
        "Di dalam bayangan tak ada yang bisa melihatmu, dan tak ada yang bisa bersembunyi.",
    ),
    "sausage": (
        "A feast judge rules the table and decides who leaves hungry.",
        "Hakim jamuan mengatur meja dan memutuskan siapa yang pergi lapar.",
        "A table set for everyone, and a hunger that follows everyone out.",
        "Meja yang disediakan untuk semua orang, dan lapar yang mengejar semua orang keluar.",
    ),
    "seven_treasure_pagoda": (
        "Seven floors of blessing lift every ally who stands close enough.",
        "Tujuh lantai berkah mengangkat setiap sekutu yang berdiri cukup dekat.",
        "A golden roof over the field: allies burn brighter, enemies see less.",
        "Atap emas di atas arena: sekutu menyala lebih terang, musuh melihat lebih sedikit.",
    ),
    "evil_fire_phoenix": (
        "The fire that never dies climbs higher with every life it burns.",
        "Api yang tak pernah mati mendaki lebih tinggi setiap nyawa yang dibakar.",
        "Cinders on the ground: the field itself starts to burn.",
        "Bara di tanah: arena itu sendiri mulai terbakar.",
    ),
    "white_tiger": (
        "A tiger of the snow plains, where every paw print is a claim.",
        "Harimau Padang Salju, di mana setiap jejak cakarnya adalah klaim.",
        "Snow covers the field and every step in it slows down.",
        "Salju menutup arena dan setiap langkah di dalamnya melambat.",
    ),
    "soft_bone_rabbit": (
        "The bones go soft and the body becomes too fast to follow.",
        "Tulangnya melunak dan tubuhnya jadi terlalu cepat untuk dikejar.",
        "A field of mirages: you hit what you see and miss what is real.",
        "Arena bayangan: kau memukul yang terlihat dan meleset yang nyata.",
    ),
    "jade_serpent": (
        "The venom that once slowed a wound now slows an army.",
        "Racun yang memperlambat luka sekarang memperlambat pasukan.",
        "The marsh water carries poison into every wound it touches.",
        "Air rawa membawa racun ke setiap luka yang disentuhnya.",
    ),
    "clear_sky_hammer": (
        "The sky clears, and the hammer that falls leaves nothing standing.",
        "Langit jernih, dan palu yang jatuh tidak menyisakan yang tegak.",
        "A field of open sky: allies strike harder, enemies lose their footing.",
        "Arena langit terbuka: sekutu memukul lebih keras, musuh kehilangan pijakan.",
    ),
    "golden_dragon": (
        "The wyrm sheds its old scales and wears the sun instead.",
        "Naga itu melepaskan sisik lamanya dan memakai matahari sebagai kulit.",
        "Molten ground that burns the resolve of allies and the footing of enemies.",
        "Tanah leleh yang membakar tekad sekutu dan pijakan musuh.",
    ),
    "abyssal_demon_dragon": (
        "Starless light clings to the dragon like a second set of scales.",
        "Cahaya tanpa bintang menempel pada naga seperti lapisan sisik kedua.",
        "A field without stars: allies vanish, enemies lose their way.",
        "Arena tanpa bintang: sekutu menghilang, musuh kehilangan arah.",
    ),
    "abyssal_ice_demon_dragon": (
        "The cold of the demon stops being cold and starts being silence.",
        "Dingin setan berhenti jadi dingin dan mulai jadi sunyi.",
        "Frost holds the ground so steady that nothing in it can move.",
        "Es menahan tanah sedemikian kuat sehingga tak ada yang bisa bergerak di dalamnya.",
    ),
    "ultimate_fire_dragon": (
        "A thousand years of volcano taught this dragon to wait, then to erupt.",
        "Seribu tahun gunung api mengajari naga ini menunggu, lalu meledak.",
        "The ground opens like a caldera and burns everything around the rim.",
        "Tanah terbuka seperti kaldera dan membakar semua yang ada di tepinya.",
    ),
    "ultimate_ice_dragon": (
        "The last glacier's dragon wakes slowly and never forgives being woken.",
        "Naga gletser terakhir bangun perlahan dan tak pernah memaafkan yang membangunkannya.",
        "A winter that does not end while the domain stands.",
        "Musim dingin yang tak berakhir selama domain masih berdiri.",
    ),
    "thunder_elephant": (
        "The herd runs with you, and the sky answers every step.",
        "Kawanan itu berlari bersamamu, dan langit menjawab setiap langkah.",
        "A storm without a shelter: allies move fast, enemies stand still.",
        "Badai tanpa tempat berteduh: sekutu bergerak cepat, musuh diam di tempat.",
    ),
    "golden_ant": (
        "What looked like a worker ant turns out to be the hill itself.",
        "Yang tampak seperti semut pekerja ternyata adalah bukit itu sendiri.",
        "Tunnels open under the field and the ground swallows the enemy's step.",
        "Terowongan terbuka di bawah arena dan tanah menelan langkah musuh.",
    ),
    "titan_giant_ape": (
        "The ape stops hiding its size and starts using it.",
        "Kera berhenti menyembunyikan ukurannya dan mulai memakainya.",
        "The plain itself answers the ape's fist and keeps shaking.",
        "Dataran itu sendiri menjawab tinju kera dan terus bergetar.",
    ),
    "sea_devil_manatee": (
        "The slow shape under the water finally shows how deep it goes.",
        "Bentuk lambat di bawah air akhirnya menunjukkan seberapa dalam ia turun.",
        "The water under the field pulls every enemy toward the centre.",
        "Air di bawah arena menarik setiap musuh ke arah pusat.",
    ),
    "bright_wing_moth": (
        "A thousand wings scatter the light until nothing can look at you.",
        "Seribu sayap menebar cahaya sampai tak ada yang bisa melihatmu.",
        "The air fills with light that blinds, and no one can find you in it.",
        "Udara dipenuhi cahaya yang membutakan, dan tak ada yang bisa menemukanmu di dalamnya.",
    ),
    "phoenix": (
        "The bird that burned comes back older, and quieter, and brighter.",
        "Burung yang terbakar kembali lebih tua, lebih tenang, dan lebih terang.",
        "The field keeps the ashes warm, so the next fire starts faster.",
        "Arena menjaga abu tetap hangat, jadi api berikutnya menyala lebih cepat.",
    ),
    "blue_silver_spear": (
        "The spear that grew from grass now points where the emperor points.",
        "Tombak yang tumbuh dari rumput sekarang menunjuk ke arah kaisar menunjuk.",
        "A tide of grass rises through the field and drags the fight down to the ground.",
        "Arus rumput naik lewat arena dan menarik pertarungan turun ke tanah.",
    ),
    "seven_kill_sword": (
        "The sword stops counting the dead and starts counting the gods.",
        "Pedang berhenti menghitung yang mati dan mulai menghitung para dewa.",
        "Seven marks on the field: whatever is marked here can be finished.",
        "Tujuh tanda di arena: apa pun yang ditandai di sini bisa diselesaikan.",
    ),
    "giant_hammer": (
        "The hammer that outgrew its smith now answers to the mountain.",
        "Palu yang tumbuh melebihi tuannya kini menjawab gunung.",
        "An anvil field: allies stand firm, enemies are pinned to the ground.",
        "Arena landasan: sekutu berdiri kokoh, musuh dipaku ke tanah.",
    ),
    "blood_lash": (
        "The cord stops opening wounds and starts deciding who closes them.",
        "Tali itu berhenti membuka luka dan mulai memutuskan siapa yang menutupnya.",
        "A ledger opens in the field: allies get sharper, enemies get marked.",
        "Catatan terbuka di arena: sekutu jadi lebih tajam, musuh ditandai.",
    ),
    "frost_needle": (
        "The needle that hides inside a body now hides inside the fight.",
        "Jarum yang bersembunyi di dalam tubuh kini bersembunyi di dalam pertarungan.",
        "The cold walks through the field and numbers every step it touches.",
        "Dingin menyusuri arena dan memberi nomor pada setiap langkah yang disentuhnya.",
    ),
    "thunder_spear": (
        "The spear is only visible when the lightning inside it is.",
        "Tombak ini hanya terlihat saat petir di dalamnya menyala.",
        "Forked lightning runs under the field and chooses who it reaches.",
        "Petir bercabang menjalar di bawah arena dan memilih siapa yang disentuhnya.",
    ),
    "spirit_sword": (
        "The plain blade stops asking permission and answers on its own.",
        "Bilah biasa berhenti meminta izin dan menjawab dengan caranya sendiri.",
        "A still point in the middle of the fight, where nothing else can move.",
        "Titik diam di tengah pertarungan, tempat tak ada yang bisa bergerak.",
    ),
    "iron_anchor": (
        "The anchor that never came up finally finds the bottom it was made for.",
        "Jangkar yang tak pernah naik akhirnya menemukan dasar yang memang untuknya.",
        "The field holds fast: allies do not move, enemies cannot leave.",
        "Arena menahan erat: sekutu tidak bergeser, musuh tidak bisa pergi.",
    ),
    "bronze_mirror": (
        "The mirror turns its face outward and shows the sect its worst moment.",
        "Cermin memutar wajahnya ke luar dan menunjukkan sekte momen terburuknya.",
        "Everything thrown at the field is thrown back at whoever threw it.",
        "Segala yang dilempar ke arena dilempar kembali kepada orang yang melemparnya.",
    ),
    "blue_silver_emperor": (
        "The grass grows old enough to rule, and rules without raising its voice.",
        "Rumput tumbuh cukup tua untuk memerintah, dan memerintah tanpa menaikkan suara.",
        "A crown of grass closes over the field and decides what grows and what does not.",
        "Mahkota menutupi arena dan memutuskan apa yang tumbuh dan apa yang tidak.",
    ),
    "iron_body": (
        "The body that learned to be metal stops pretending it was ever flesh.",
        "Tubuh yang belajar menjadi logam berhenti berpura-pura masih daging.",
        "A bell tolls in the field and everyone nearby braces for it.",
        "Lonceng berbunyi di arena dan semua orang di dekatnya bersiap.",
    ),
    "bone_shell": (
        "The shell closes over the body like a decision that cannot be taken back.",
        "Cangkang menutupi tubuh seperti keputusan yang tidak bisa ditarik kembali.",
        "The field grows a cage and every enemy in it learns the bars.",
        "Arena tumbuh kurungan dan setiap musuh di dalamnya menghafal jerunya.",
    ),
    "stone_skin": (
        "Ten years of rain teach the skin to answer blows with weight.",
        "Sepuluh tahun hujan mengajarkan kulit menjawab pukulan dengan berat.",
        "The ground opens under the field and keeps whatever falls in.",
        "Tanah terbuka di bawah arena dan menahan apa pun yang jatuh ke dalamnya.",
    ),
    "flesh_regrowth": (
        "The wound closes faster than it should, and the body forgets the reason.",
        "Luka menutup lebih cepat dari seharusnya, dan tubuh lupa penyebabnya.",
        "The field grows a red harvest and takes a cut of everyone's health.",
        "Arena memanen merah dan mengambil sebagian HP semua orang di dalamnya.",
    ),
    "flame_body": (
        "The fire inside answers every heat sent at it with more heat.",
        "Api di dalam menjawab setiap panas yang datang dengan panas yang lebih besar.",
        "A column of fire stands in the field and nobody crosses it unharmed.",
        "Pilar api berdiri di arena dan tak ada yang melewatinya tanpa luka.",
    ),
    "spirit_veins": (
        "Power stops running through the chest and starts running through the whole body.",
        "Tenaga berhenti mengalir lewat dada dan mulai mengalir ke seluruh tubuh.",
        "Every vein in the field lights up at once and the ground follows.",
        "Setiap urat di arena menyala sekaligus dan tanah mengikutinya.",
    ),
    "soul_mirror": (
        "The mirror breaks into a thousand pieces and every piece still shows the truth.",
        "Cermin pecah jadi seribu pecahan dan setiap pecahan tetap menunjukkan kebenaran.",
        "The field becomes a mirror: what enters is shown back as it truly is.",
        "Arena jadi cermin: apa pun yang masuk ditampilkan kembali apa adanya.",
    ),
    "spirit_lamp": (
        "The lamp that burns without oil finally lights the thought it was made for.",
        "Lentera yang menyala tanpa minyak akhirnya menerangi pikiran yang ia buat.",
        "A circle of lamplight keeps the field visible and the dark at bay.",
        "Lingkaran cahaya lentera menjaga arena tetap terlihat dan kegelapan menyingkir.",
    ),
    "soul_forge": (
        "The forge that shapes souls finally gets to shape the one that holds it.",
        "Tempa yang membentuk soul akhirnya bisa membentuk soul yang memegangnya.",
        "The field is heated like metal: allies are hardened, enemies are cracked.",
        "Arena dipanaskan seperti logam: sekutu dikeraskan, musuh retak.",
    ),
    "star_array": (
        "Seven points of light fold the field into the shape the array wants.",
        "Tujuh titik cahaya melipat arena menjadi bentuk yang diinginkan susunan.",
        "The stars above the field rearrange, and the field obeys.",
        "Bintang di atas arena menyusun ulang, dan arena ikut menurut.",
    ),
    "demon_eye": (
        "The eye opens wide enough to take in the whole field at once.",
        "Mata terbuka cukup lebar untuk melihat seluruh arena sekaligus.",
        "The field is watched, so nothing in it can move unseen.",
        "Arena diawasi, jadi tak ada yang bergerak di dalamnya tanpa terlihat.",
    ),
    "ancient_seal": (
        "The seal answers the hand that made it, and the field obeys the seal.",
        "Segel itu menjawab tangan yang membuatnya, dan arena itu menaati segel.",
        "The seal presses its mark into the field and nothing there moves freely.",
        "Segel menekan tandanya ke arena dan tak ada yang bergerak bebas di dalamnya.",
    ),
}

# ---------------------------------------------------------------- pelabelsan lain

HUD_LABELS = {
    "gui.soulland.hud.truebody": ("True Body", "True Body"),
    "gui.soulland.hud.truebody.locked": ("needs %s rings", "butuh %s ring"),
    "gui.soulland.hud.truebody.active": ("active %s s", "aktif %s dtk"),
    "gui.soulland.hud.truebody.cooldown": ("cooldown %s s", "cooldown %s dtk"),
    "gui.soulland.hud.truebody.ready": ("ready", "siap"),
    "gui.soulland.hud.domain": ("Domain", "Domain"),
    "gui.soulland.hud.domain.active": ("active %s SP/s", "aktif %s SP/dtk"),
    "gui.soulland.hud.domain.off": ("off", "mati"),
    "gui.soulland.combat.truebody": ("TRUE BODY", "TRUE BODY"),
    "gui.soulland.combat.domain": ("DOMAIN", "DOMAIN"),
    "gui.soulland.combat.truebody_stats": (
        "Damage +%s%%   Aura %s blocks   Cost %s SP",
        "Damage +%s%%   Aura %s blok   Biaya %s SP",
    ),
    "gui.soulland.combat.domain_stats": (
        "Radius %s blocks   Drains %s SP/s   Damage %s/s",
        "Radius %s blok   Kuras %s SP/dtk   Damage %s/dtk",
    ),
    "gui.soulland.combat.truebody_rules": (
        "Needs 7 spirit rings. Active 20 seconds, cooldown 5 minutes.",
        "Butuh 7 spirit ring. Aktif 20 detik, jeda 5 menit.",
    ),
    "gui.soulland.combat.domain_rules": (
        "The domain stands until spirit power runs out.",
        "Domain berdiri sampai spirit power habis.",
    ),
    "gui.soulland.combat.unknown": ("No data.", "Tidak ada data."),
    "key.soulland.truebody": ("True Body", "True Body"),
    "key.soulland.domain": ("Domain", "Domain"),
}

MESSAGE_LABELS = {
    "message.soulland.truebody.not_awakened": (
        "Finish the awakening ceremony before opening a True Body.",
        "Selesaikan upacara kebangkitan dulu sebelum membuka True Body.",
    ),
    "message.soulland.truebody.unknown": (
        "This martial soul has no True Body.",
        "Martial soul ini tidak punya True Body.",
    ),
    "message.soulland.truebody.need_rings": (
        "A True Body needs %s spirit rings. You have %s.",
        "True Body butuh %s spirit ring. Punyamu cuma %s.",
    ),
    "message.soulland.truebody.cooldown": (
        "True Body is still on cooldown for %s seconds.",
        "True Body masih cooldown %s detik.",
    ),
    "message.soulland.truebody.need_energy": (
        "Not enough spirit power for a True Body. You need %s.",
        "Spirit power tidak cukup untuk True Body. Butuh %s.",
    ),
    "message.soulland.truebody.activated": (
        "True Body active: %s. Damage +%s%% for %s seconds.",
        "True Body aktif: %s. Damage +%s%% selama %s detik.",
    ),
    "message.soulland.truebody.expired": (
        "True Body ended. The cooldown starts now.",
        "True Body berakhir. Jeda dimulai.",
    ),
    "message.soulland.domain.not_awakened": (
        "Finish the awakening ceremony before standing in a domain.",
        "Selesaikan upacara kebangkitan dulu sebelum berdiri di domain.",
    ),
    "message.soulland.domain.unknown": (
        "This martial soul has no domain.",
        "Martial soul ini tidak punya domain.",
    ),
    "message.soulland.domain.need_energy": (
        "Not enough spirit power to hold a domain. You need %s.",
        "Spirit power tidak cukup untuk berdiri di domain. Butuh %s.",
    ),
    "message.soulland.domain.opened": (
        "Domain stands: %s. Spirit power drains %s per second.",
        "Domain berdiri: %s. Spirit power terkuras %s per detik.",
    ),
    "message.soulland.domain.closed": (
        "Domain closed.",
        "Domain ditutup.",
    ),
    "message.soulland.domain.drained": (
        "Spirit power ran out, the domain closes itself.",
        "Spirit power habis, domain menutup diri.",
    ),
}

# ---------------------------------------------------------------- baca katalog

TRUE_BODY_PATTERN = re.compile(
    r'register\(new TrueBodyDef\(\s*"(?P<soul_id>[^"]+)",\s*"(?P<name_en>[^"]*)",\s*"(?P<name_id>[^"]*)"',
    re.S,
)
DOMAIN_PATTERN = re.compile(
    r'register\(new DomainDef\(\s*"(?P<soul_id>[^"]+)",\s*"(?P<name_en>[^"]*)",\s*"(?P<name_id>[^"]*)"',
    re.S,
)


def read_catalog(path: pathlib.Path, pattern: re.Pattern) -> dict:
    source = path.read_text(encoding="utf-8")
    return {
        match.group("soul_id"): (match.group("name_en"), match.group("name_id"))
        for match in pattern.finditer(source)
    }


def build_entries(true_bodies: dict, domains: dict) -> dict:
    entries = {}

    for soul_id, (name_en, name_id) in true_bodies.items():
        entries[f"soul.soulland.truebody.{soul_id}.name"] = (name_en, name_id)
        body_en, body_id, _, _ = DESCRIPTIONS[soul_id]
        entries[f"soul.soulland.truebody.{soul_id}.description"] = (body_en, body_id)

    for soul_id, (name_en, name_id) in domains.items():
        entries[f"soul.soulland.domain.{soul_id}.name"] = (name_en, name_id)
        _, _, domain_en, domain_id = DESCRIPTIONS[soul_id]
        entries[f"soul.soulland.domain.{soul_id}.description"] = (domain_en, domain_id)

    entries.update(HUD_LABELS)
    entries.update(MESSAGE_LABELS)
    return entries


def main() -> None:
    true_bodies = read_catalog(TRUE_BODY_FILE, TRUE_BODY_PATTERN)
    domains = read_catalog(DOMAIN_FILE, DOMAIN_PATTERN)

    missing = [soul_id for soul_id in true_bodies if soul_id not in DESCRIPTIONS]
    if missing:
        print(f"ERROR: deskripsi hilang untuk soul: {missing}")
        return 1
    extra = [soul_id for soul_id in DESCRIPTIONS if soul_id not in true_bodies]
    if extra:
        print(f"ERROR: deskripsi tanpa entri katalog: {extra}")
        return 1

    entries = build_entries(true_bodies, domains)

    en_path = LANG / "en_us.json"
    id_path = LANG / "id_id.json"
    en = json.loads(en_path.read_text(encoding="utf-8"))
    id_ = json.loads(id_path.read_text(encoding="utf-8"))

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
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
