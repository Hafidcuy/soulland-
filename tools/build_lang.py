#!/usr/bin/env python3
"""Bangun file bahasa mod (en_us.json dan id_id.json) dari tabel terjemahan.

Kunci yang dibuat:
- item.soulland.<registry_name>          nama item di creative tab
- key.soulland.skill<N>                  nama tombol skill
- gui.soulland.awakening.*               teks layar Upacara Kebangkitan
- gui.soulland.category.*                label tab kategori
- soul.soulland.rarity.*                 nama rarity
- soul.soulland.type.*                   nama tipe soul
- soul.soulland.owner.*                  pemilik asli soul
- soul.soulland.<id>.name/.description   nama dan deskripsi soul
- skill.soulland.<id>.name/.description  nama dan deskripsi skill

Jalankan: python3 tools/build_lang.py
"""
import json
import pathlib
import re

ROOT = pathlib.Path(__file__).resolve().parent.parent
JAVA = ROOT / "src/main/java/com/soulland/soul/MartialSouls.java"
LANG_DIR = ROOT / "src/main/resources/assets/soulland/lang"

# Nama item manifestasi memakai nama soul yang sama supaya hubungannya jelas.
ITEM_SUFFIX = {
    "hoe": "Old Hoe",
    "blue_silver_grass": "Blue Silver Grass",
    "hell_civet": "Hell Civet",
    "sausage": "Magic Sausage",
    "seven_treasure_pagoda": "Seven Treasure Glazed Tile Pagoda",
    "evil_fire_phoenix": "Evil Fire Phoenix",
    "white_tiger": "White Tiger",
    "soft_bone_rabbit": "Soft Bone Rabbit",
    "jade_serpent": "Jade Phosphor Serpent",
    "clear_sky_hammer": "Clear Sky Hammer",
}

SOUL_EN = {
    "hoe": (
        "Old Hoe",
        "The martial soul of ordinary folk. In steady hands even a hoe can reach the sky.",
        "Common Farmer",
    ),
    "blue_silver_grass": (
        "Blue Silver Grass",
        "Blue silver grass looks weak, yet it can bind and poison anything that crosses it.",
        "Tang San",
    ),
    "hell_civet": (
        "Hell Civet",
        "A hellish cat, quick and deadly, striking out of the shadows.",
        "Zhu Zhuqing",
    ),
    "sausage": (
        "Magic Sausage",
        "A sausage made by a master of the food system: the finest healer and cleanser on the battlefield.",
        "Oscar",
    ),
    "seven_treasure_pagoda": (
        "Seven Treasure Glazed Tile Pagoda",
        "A seven storey pagoda: the mightiest support martial soul, empowering the whole team.",
        "Ning Rongrong",
    ),
    "evil_fire_phoenix": (
        "Evil Fire Phoenix",
        "An evil fire phoenix: it burns everything, and its flame never dies.",
        "Ma Hongjun",
    ),
    "white_tiger": (
        "White Tiger",
        "The white tiger with the silver mane: brutal physical power and defence.",
        "Dai Mubai",
    ),
    "soft_bone_rabbit": (
        "Soft Bone Rabbit",
        "The soft bone rabbit: a charming presence and movement as fast as lightning.",
        "Xiao Wu",
    ),
    "jade_serpent": (
        "Jade Phosphor Serpent",
        "The venomous jade serpent: mists and bites that slowly paralyse enemies.",
        "Dugu Yan",
    ),
    "clear_sky_hammer": (
        "Clear Sky Hammer",
        "The Clear Sky Hammer: the legendary martial soul of the Clear Sky clan, a breaker of everything.",
        "Tang San / Tang Hao",
    ),
}

SKILL_EN = {
    "hoe_heal": ("Bountiful Harvest", "Restores 6 HP and grants saturation."),
    "hoe_slam": ("Earth Shatter", "A small area slam that damages nearby enemies."),
    "hoe_harvest": ("Field Plough", "The hoe soaks the soil: an area slam with radius 5 and restores 3 HP."),
    "hoe_season": ("Harvest Season", "Harvest time: an area slam with radius 8, heals allies for 8 HP, and grants Absorption for 30 seconds."),
    "bsg_entangle": ("Blue Silver Entangle", "Blue roots bind nearby enemies: heavy slow plus weakness."),
    "bsg_parasite": ("Parasitic Seed", "Plants a parasitic seed in the closest enemy: poison for 8 seconds while you recover health."),
    "bsg_nest": ("Blue Silver Nest", "Roots bind the three closest enemies and hold them in place."),
    "bsg_rain": ("Blue Silver Rain", "Blue silver rain sweeps the area: poison and weakening at the same time."),
    "civet_dash": ("Nether Thrust", "Dashes forward and wounds every enemy it passes through."),
    "civet_slash": ("Nether Slash", "A fast area slash around you plus movement speed."),
    "civet_shadow": ("Shadow Dash", "Your body fades into shadow: dash 6 blocks and Speed II for 5 seconds."),
    "civet_storm": ("Nether Storm", "A shadow storm rushes forward: a dashing strike that wounds enemies and grants Speed III for 8 seconds."),
    "sausage_heal": ("Recovery Sausage", "Restores 8 HP and grants full saturation."),
    "sausage_fly": ("Flying Mushroom", "Floats through the air with movement speed for 20 seconds."),
    "sausage_soup": ("Hot Sausage Soup", "Hot sausage soup: heals nearby allies for 8 HP and grants Regeneration for 10 seconds."),
    "sausage_banquet": ("Sausage Banquet", "A sausage feast: restore 15 HP and Resistance II for 20 seconds."),
    "pagoda_power": ("Blessing of Power", "Nearby allies gain Strength II and Absorption for 30 seconds."),
    "pagoda_heal": ("Blessing of Life", "Heals nearby allies for 10 HP and grants Regeneration."),
    "pagoda_aegis": ("Blessing of Guard", "A protective blessing: allies gain Resistance II and Speed I for 30 seconds."),
    "pagoda_blood": ("Blood of the Pagoda", "Sacred pagoda blood flows: heals 12 HP and grants allies Damage Boost III."),
    "phoenix_line": ("Phoenix Fireline", "A cone of fire erupts toward where you look."),
    "phoenix_meteor": ("Phoenix Meteor", "A rain of fireballs crashes down in front of you."),
    "phoenix_wings": ("Wings of Fire", "Fire wings unfold: a ring of fire that wounds and burns nearby enemies."),
    "phoenix_ruin": ("Phoenix Ruin", "A giant fireball detonates and devastates a wide area."),
    "tiger_wave": ("White Tiger Light Wave", "A wave of tiger light: straight line damage plus knockback."),
    "tiger_vajra": ("Vajra Transformation", "Transformation: Strength II and Resistance II for 30 seconds."),
    "tiger_claw": ("Tiger Claw", "The claw locks onto one target: a heavy burst of damage."),
    "tiger_roar": ("Tiger Roar", "A roar that stuns: area damage and a hard knockback to every enemy."),
    "rabbit_charm": ("Charm", "Charms nearby monsters: they stop attacking you."),
    "rabbit_blink": ("Teleport", "Blinks 8 blocks toward where you look."),
    "rabbit_hop": ("Rabbit Leap", "A light leap: jump high and Speed II for 6 seconds."),
    "rabbit_sprint": ("Breaking Sprint", "Sprints through the arena: teleport 12 blocks and confuse every enemy you pass."),
    "serpent_mist": ("Poison Mist", "A poison mist area: poison plus slow for 10 seconds."),
    "serpent_bite": ("Serpent Bite", "A venomous bite on the closest enemy: heavy damage plus strong poison."),
    "serpent_siphon": ("Poison Siphon", "Siphons the health of the closest enemy into yourself."),
    "serpent_death": ("Mist of Death", "A death mist with radius 10: poison and wither that wears down the vitality."),
    "hammer_slam": ("Hammer Slam", "A giant hammer slam: massive area damage plus upward knockback."),
    "hammer_cloak": ("Chaotic Cloak", "A spinning hammer technique: Strength III and Speed II for 20 seconds."),
    "hammer_thunder": ("Thunder Hammer", "The hammer calls lightning: one target struck by extraordinary damage."),
    "hammer_clear_sky": ("Clear Sky Hammer", "The clear sky collapses: massive area damage with upward knockback."),
}

TYPE_EN = {
    "Alat": "Tool",
    "Kontrol": "Control",
    "Agility": "Agility",
    "Support": "Support",
    "Serangan Api": "Fire Attack",
    "Power": "Power",
    "Racun": "Poison",
}

CATEGORY_SHORT_EN = {
    "Alat": "Tool",
    "Kontrol": "Control",
    "Agility": "Agility",
    "Support": "Support",
    "Serangan Api": "Fire",
    "Power": "Power",
    "Racun": "Poison",
}

CATEGORY_SHORT_ID = {
    "Alat": "Alat",
    "Kontrol": "Kontrol",
    "Agility": "Agility",
    "Support": "Support",
    "Serangan Api": "Api",
    "Power": "Power",
    "Racun": "Racun",
}

GUI_EN = {
    "title": "Martial Soul Awakening Ceremony",
    "title.reroll": "Choose a New Martial Soul",
    "subtitle": "Choose the martial soul that will bind to your fate",
    "subtitle.reroll": "Creative mode: replace the martial soul you already awakened",
    "category_empty": "No soul in this category",
    "empty": "No soul selected yet",
    "current_soul": "Current soul: %s",
    "owner": "Original owner: %s",
    "confirming": "Confirming...",
    "confirm": "AWAKEN",
    "pending": "Waiting for the server to answer...",
    "hint": "Tab switches area, arrows select, Enter confirms",
    "error.generic": "That martial soul could not be chosen",
    "error.invalid_soul": "That martial soul does not exist",
    "error.already_awakened": "Your martial soul is already awakened",
}

GUI_ID = {
    "title": "Upacara Kebangkitan Martial Soul",
    "title.reroll": "Pilih Martial Soul Baru",
    "subtitle": "Pilih martial soul yang akan menjadi takdirmu",
    "subtitle.reroll": "Mode Creative: ganti martial soul yang sudah dibangkitkan",
    "category_empty": "Belum ada soul di kategori ini",
    "empty": "Belum ada soul yang dipilih",
    "current_soul": "Soul sekarang: %s",
    "owner": "Pemilik asli: %s",
    "confirming": "Memproses...",
    "confirm": "BANGKITKAN",
    "pending": "Menunggu jawaban server...",
    "hint": "Tab pindah area, panah memilih, Enter mengonfirmasi",
    "error.generic": "Martial soul itu tidak bisa dipilih",
    "error.invalid_soul": "Martial soul itu tidak ada",
    "error.already_awakened": "Martial soul-mu sudah dibangkitkan",
}

RARITY = {
    "common": "Common",
    "uncommon": "Uncommon",
    "rare": "Rare",
    "epic": "Epic",
    "legendary": "Legendary",
}


def parse_java():
    """Baca registry soul dan skill langsung dari file Java."""
    src = JAVA.read_text(encoding="utf-8")
    souls = []
    pattern = re.compile(
        r'register\(new MartialSoul\(\s*"([^"]+)",\s*"([^"]*)",\s*"([^"]*)",\s*"([^"]*)",\s*"([^"]*)",\s*'
        r'"([^"]*)",\s*List\.of\((.*?)\n\s*\)\)\);',
        re.S,
    )
    for match in pattern.finditer(src):
        sid, name, owner, stype, rarity, desc, skills = match.groups()
        entries = []
        for skill in re.finditer(
            r'new SkillDef\("([^"]+)",\s*"([^"]*)",\s*"([^"]*)"', skills
        ):
            entries.append(skill.groups())
        souls.append(
            {
                "id": sid,
                "name": name,
                "owner": owner,
                "type": stype,
                "rarity": rarity,
                "description": desc,
                "skills": [{"id": s[0], "name": s[1], "description": s[2]} for s in entries],
            }
        )
    return souls


def slug(text):
    return re.sub(r"[^a-z0-9]+", "_", text.lower()).strip("_")


def build(souls):
    en = {
        "itemGroup.soulland": "Soul Land: Douluo Dalu",
        "key.categories.soulland": "Soul Land",
        "item.soulland.spirit_ring_100": "Spirit Ring (100 Years)",
        "item.soulland.spirit_ring_1000": "Spirit Ring (1,000 Years)",
        "item.soulland.spirit_ring_10000": "Spirit Ring (10,000 Years)",
        "item.soulland.spirit_ring_100000": "Spirit Ring (100,000 Years)",
        "key.soulland.skill1": "Martial Soul Skill 1",
        "key.soulland.skill2": "Martial Soul Skill 2",
        "key.soulland.skill3": "Martial Soul Skill 3",
        "key.soulland.skill4": "Martial Soul Skill 4",
    }
    id_ = dict(en)
    id_["item.soulland.spirit_ring_100"] = "Spirit Ring (100 Tahun)"
    id_["item.soulland.spirit_ring_1000"] = "Spirit Ring (1.000 Tahun)"
    id_["item.soulland.spirit_ring_10000"] = "Spirit Ring (10.000 Tahun)"
    id_["item.soulland.spirit_ring_100000"] = "Spirit Ring (100.000 Tahun)"
    id_["key.soulland.skill1"] = "Skill 1 Martial Soul"
    id_["key.soulland.skill2"] = "Skill 2 Martial Soul"
    id_["key.soulland.skill3"] = "Skill 3 Martial Soul"
    id_["key.soulland.skill4"] = "Skill 4 Martial Soul"

    for key, value in GUI_EN.items():
        en[f"gui.soulland.awakening.{key}"] = value
    for key, value in GUI_ID.items():
        id_[f"gui.soulland.awakening.{key}"] = value

    for rarity, label in RARITY.items():
        en[f"soul.soulland.rarity.{rarity}"] = label
        id_[f"soul.soulland.rarity.{rarity}"] = label

    for soul in souls:
        sid = soul["id"]
        en_name, en_desc, en_owner = SOUL_EN[sid]
        en[f"soul.soulland.{sid}.name"] = en_name
        en[f"soul.soulland.{sid}.description"] = en_desc
        id_[f"soul.soulland.{sid}.name"] = soul["name"]
        id_[f"soul.soulland.{sid}.description"] = soul["description"]
        en[f"soul.soulland.owner.{sid}"] = en_owner
        id_[f"soul.soulland.owner.{sid}"] = soul["owner"]
        en[f"soul.soulland.type.{slug(soul['type'])}"] = TYPE_EN[soul["type"]]
        id_[f"soul.soulland.type.{slug(soul['type'])}"] = soul["type"]
        en[f"item.soulland.manifestation_{sid}"] = f"Manifestation: {ITEM_SUFFIX[sid]}"
        id_[f"item.soulland.manifestation_{sid}"] = f"Manifestasi: {soul['name']}"
        for skill in soul["skills"]:
            sk_name, sk_desc = SKILL_EN[skill["id"]]
            en[f"skill.soulland.{skill['id']}.name"] = sk_name
            en[f"skill.soulland.{skill['id']}.description"] = sk_desc
            id_[f"skill.soulland.{skill['id']}.name"] = skill["name"]
            id_[f"skill.soulland.{skill['id']}.description"] = skill["description"]

    for stype, short in CATEGORY_SHORT_EN.items():
        en[f"gui.soulland.category.{slug(stype)}"] = short
    for stype, short in CATEGORY_SHORT_ID.items():
        id_[f"gui.soulland.category.{slug(stype)}"] = short

    return en, id_


def write(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(
        json.dumps(dict(sorted(data.items())), ensure_ascii=False, indent=2) + "\n",
        encoding="utf-8",
    )
    print(f"{path.relative_to(ROOT)}: {len(data)} kunci")


def main():
    souls = parse_java()
    if len(souls) != 10:
        raise SystemExit(f"harus ada 10 soul, ditemukan {len(souls)}")
    en, id_ = build(souls)
    write(LANG_DIR / "en_us.json", en)
    write(LANG_DIR / "id_id.json", id_)


if __name__ == "__main__":
    main()
