#!/usr/bin/env python3
"""
Membangun dataset martial soul dari sumber-sumber lore resmi series Soul Land.

Sumber:
1. Tabel Martial Soul Moegirlpedia (Parts 1-4): nama Inggris + Mandarin, pemilik, afiliasi.
2. Daftar abjad Tool / Beast / Body Martial Soul di wiki Soul Land (Fandom).
3. Daftar kategori武魂 dari daftar lengkap 武魂大全.
4. Lore tambahan: fusion skill, quadrant awakenings body, bentuk evolusi, outright attribute.

Keluar: src/main/resources/data/soulland/martial_souls.json
"""

import hashlib
import io
import json
import os
import re

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DATA = os.path.join(ROOT, "src", "main", "resources", "data", "soulland", "martial_souls.json")
EN_TSV = os.path.join(ROOT, "tools", "data", "martial_souls_en.tsv")
CN_TSV = os.path.join(ROOT, "tools", "data", "martial_souls_cn.tsv")
FANDOM_TXT = os.path.join(ROOT, "tools", "data", "fandom_souls.txt")

# ------------------------------------------------------------------ kamus Mandarin -> Inggris

GLOSS = {
    "火焰": "Flame", "烈焰": "Blaze", "炽": "Blazing", "煌": "Radiant", "炎": "Blaze",
    "暗金": "Dark Gold", "黄金": "Golden", "赤金": "Crimson Gold", "黄金玳瑁": "Golden Tortoise",
    "血": "Blood", "影": "Shadow", "幽冥": "Nether", "冥域": "Nether", "邪": "Evil", "恶": "Evil",
    "黑暗": "Dark", "暗": "Dark", "圣": "Sacred", "神": "Divine", "仙": "Immortal", "魔": "Demon",
    "妖": "Spirit", "鬼": "Ghost", "幽": "Phantom", "魂": "Soul", "魄": "Soul", "灵": "Spirit",
    "雷": "Lightning", "电": "Electric", "冰": "Ice", "雪": "Snow", "霜": "Frost", "水": "Water",
    "海": "Sea", "风": "Wind", "云": "Cloud", "雾": "Mist", "土": "Earth", "沙": "Sand", "尘": "Dust",
    "光": "Light", "星": "Star", "月": "Moon", "日": "Sun", "阳": "Sun", "天": "Sky", "地": "Earth",
    "金": "Gold", "银": "Silver", "铁": "Iron", "铜": "Bronze", "紫": "Violet", "蓝": "Blue",
    "绿": "Green", "青": "Azure", "赤": "Crimson", "红": "Red", "黑": "Black", "白": "White",
    "黄": "Yellow", "灰": "Ashen", "玄": "Obsidian", "翠": "Jade", "碧": "Jade", "琉璃": "Glazed",
    "玳瑁": "Tortoise", "斑斓": "Dappled", "狂暴": "Furious", "巨掌": "Giant Palm", "铁臂": "Iron Arm",
    "暗魔": "Dark Demon", "暗金孔爪": "Dark Gold Terrorclaw", "万": "Myriad", "千": "Thousand",
    "百": "Hundred", "十": "Ten", "三": "Three", "双": "Twin", "独": "Lone", "大": "Great",
    "小": "Small", "极": "Extreme", "王": "King", "皇": "Emperor", "后": "Queen", "帝": "Emperor",
    "主": "Master", "君": "Lord", "尊": "Venerable", "宗": "Sect", "门": "Sect", "族": "Clan",
    "龙": "Dragon", "蛇": "Snake", "虎": "Tiger", "狮": "Lion", "豹": "Leopard", "熊": "Bear",
    "狼": "Wolf", "狐": "Fox", "猫": "Cat", "犬": "Dog", "兔": "Rabbit", "鼠": "Rat", "猪": "Boar",
    "牛": "Ox", "马": "Horse", "鹿": "Stag", "羊": "Ram", "猴": "Monkey", "猿": "Ape", "狒": "Baboon",
    "猩": "Gorilla", "蛟": "Python", "蟒": "Python", "蟾": "Toad", "蛙": "Frog", "鳄": "Crocodile",
    "犀": "Rhino", "象": "Elephant", "龟": "Turtle", "鲨": "Shark", "鲸": "Whale", "豚": "Dolphin",
    "鱼": "Fish", "鹰": "Eagle", "隼": "Falcon", "鹏": "Roc", "鹤": "Crane", "鸾": "Luan",
    "凤": "Phoenix", "凰": "Phoenix", "鸽": "Dove", "鹅": "Goose", "燕": "Swallow", "雀": "Sparrow",
    "蛛": "Spider", "蝎": "Scorpion", "蚁": "Ant", "蝶": "Butterfly", "蚕": "Silkworm",
    "蜈": "Centipede", "蝠": "Bat", "鹫": "Vulture", "鸡": "Chicken", "貂": "Mink", "鼬": "Weasel",
    "獾": "Badger", "麋": "Elk", "貘": "Tapir", "鞭": "Whip", "角": "Horn", "爪": "Claw",
    "齿": "Fang", "尾": "Tail", "翼": "Wing", "翅": "Wing", "羽": "Feather", "鳞": "Scale",
    "瞳": "Pupil", "眸": "Eye", "眼": "Eye", "脑": "Brain", "骨": "Bone", "皮": "Hide",
    "肌": "Muscle", "身": "Body", "躯": "Torso", "臂": "Arm", "腿": "Leg", "膀": "Wing",
    "股": "Thigh", "肠": "Gut", "肤": "Skin", "发": "Hair", "心": "Heart", "手": "Hand",
    "掌": "Palm", "拳": "Fist", "头": "Head", "面": "Face", "自": "Self", "躯干": "Torso",
    "刀": "Blade", "枪": "Spear", "锤": "Hammer", "盾": "Shield", "斧": "Axe", "杖": "Staff",
    "棍": "Staff", "棒": "Club", "剑": "Sword", "弓": "Bow", "扇": "Fan", "琴": "Zither",
    "笛": "Flute", "箫": "Flute", "钟": "Bell", "鼎": "Cauldron", "瓶": "Bottle", "珠": "Pearl",
    "环": "Ring", "冠": "Crown", "塔": "Pagoda", "盘": "Plate", "镜": "Mirror", "幡": "Banner",
    "旗": "Banner", "衣": "Robe", "盔": "Helm", "甲": "Armor", "匕": "Dagger", "弩": "Crossbow",
    "针": "Needle", "铃": "Bell", "镖": "Dart", "镰": "Sickle", "锄": "Hoe", "叉": "Trident",
    "戟": "Halberd", "钟鼎": "Bell Cauldron", "书": "Book", "典": "Codex", "卷": "Scroll",
    "牌": "Card", "令": "Token", "印": "Seal", "符": "Charm", "旗幡": "Banner", "鼓": "Drum",
    "草": "Grass", "花": "Flower", "藤": "Vine", "叶": "Leaf", "树": "Tree", "竹": "Bamboo",
    "莲": "Lotus", "菊": "Chrysanthemum", "杏": "Apricot", "荷": "Lotus", "荆": "Thorn",
    "棘": "Thorn", "菇": "Mushroom", "菌": "Mushroom", "苔": "Moss", "兰": "Orchid",
    "蔷薇": "Rose", "糖": "Sugar", "蜜": "Honey", "果": "Fruit", "橘": "Tangerine",
    "肉": "Meat", "香肠": "Sausage", "馒头": "Steamed Bun", "包子": "Bun", "雪茄": "Cigar",
    "糖豆": "Sugar Bean", "鸡腿": "Chicken Leg", "橘子": "Orange", "钟乳": "Stalactite",
    "力": "Strength", "威": "Might", "战": "War", "杀": "Kill", "刃": "Edge", "气": "Qi",
    "念": "Thought", "情": "Emotion", "命": "Fate", "运": "Luck", "意": "Intent", "志": "Will",
    "术": "Art", "法": "Law", "道": "Way", "阵": "Formation", "域": "Domain", "境": "Realm",
    "界": "Realm", "渊": "Abyss", "魂殿": "Soul Hall", "骷髅": "Skeleton", "尸": "Corpse",
    "僵": "Zombie", "巫": "Witch", "梦": "Dream", "魇": "Nightmare", "婴": "Infant",
    "君者": "Lord", "核": "Core", "王后": "Queen", "之": "of", "手": "Hand",
}
GLOSS_KEYS = sorted(GLOSS.keys(), key=len, reverse=True)

CN_ALL_TSV = os.path.join(ROOT, "tools", "data", "cn_all.tsv")

CN_FAMILY_MAP = {
    "器": "Tool", "形": "Beast", "猫": "Beast", "植物": "Plant", "鸟": "Beast", "龙": "Dragon",
    "邪恶": "Evil", "猿": "Beast", "狼": "Beast", "熊": "Beast", "蛇": "Beast", "蛛": "Beast",
    "人形": "Humanoid", "迷幻": "Evil", "虫": "Beast", "本体": "Body", "牛": "Beast",
    "龟": "Beast", "鱼": "Beast", "蝎": "Beast", "鼠": "Beast", "星体": "Star", "食物": "Food",
    "元素": "Element", "鳄": "Beast", "马": "Beast", "兔": "Beast", "蟾": "Beast", "象": "Beast",
    "狐": "Beast",
}

FUSION_SKILLS = [
    ("Netherworld White Tiger", "Dai Mubai + Zhu Zhuqing", "Nether"),
    ("Radiant Holy Dragon", "Yu Xiaogang + Liu Erlong + Flender", "Light"),
    ("Drifting Ice and Snow", "Shui Bing'er + Xue Wu", "Ice"),
    ("Fiend", "Hu Liena + Xie Yue", "Dark"),
    ("Domain of Two Extremes", "Chrysanthemum Douluo + Ghost Douluo", "Dark"),
    ("Forbidden Waterspout", "Hai Bai + Hai Gui", "Water"),
    ("Light of Resurrection", "Ning Rongrong + Oscar", "Light"),
    ("Dual Gods Coexistence", "Tang San + Xiao Wu", "Light"),
    ("Dragon Snake Combined Strike", "Meng Shu + Chao Tianxiang", "Lightning"),
    ("Wither in Splendor, Golden Path", "Huo Yuhao + Tang Wutong", "Light"),
    ("Truth in Nothingness, Mind Storm", "Huo Yuhao + Tang Wutong", "Space"),
    ("Blazing in Extreme Frost, Hammer of the Heavenly Emperor", "Huo Yuhao + Wang Dong'er", "Ice"),
    ("Solo Dance in Frost, Light's Rainbow Garment", "Huo Yuhao + Wang Dong'er", "Light"),
    ("Purple Gold Butterfly Dragon Transformation", "Huo Yuhao + Tang Wutong + Wang Qiuer", "Lightning"),
    ("Dragon's Roar of Destiny", "Huo Yuhao + Wang Qiuer", "Lightning"),
    ("Dark Eagle Dragon", "Zhang Yangzi + Wang Jinxi", "Dark"),
    ("Forest of Ice and Snow", "Zhou Hanyou + Zhou Tian'er", "Ice"),
    ("Dragon God Transformation", "Tang Wulin + Gu Yuena", "Light"),
    ("Heavenly Tiger Summon", "Kuang Zhantian + Jian Mochen", "Lightning"),
    ("Emotions Touching Heaven", "Zang Xin + Cao Dezhi", "Dark"),
    ("Dance of the Ice Phoenix", "Lan Mengqin", "Ice"),
    ("Netherworld White Tiger (Twin Form)", "Dai Huabin + Zhu Lu", "Dark"),
    ("Star Luo Chess Formation", "Lu Qibin", "Earth"),
    ("Frostbound Blizzard", "Zhou Hanyou + Zhou Tian'er", "Ice"),
    ("Charm Demon", "Hu Liena + Xie Yue", "Dark"),
    ("Dark Eclipse", "Hu Liena + Xie Yue", "Dark"),
    ("Ghost Demon", "Gui Mei + Yue Guan", "Dark"),
    ("Drifting Snow", "Shui Bing'er + Xue Wu", "Ice"),
    ("Serpent Dragon Joint Assault", "Meng Shu + Chao Tianxiang", "Lightning"),
    ("Hell White Tiger", "Dai Mubai + Zhu Zhuqing", "Dark"),
    ("Twin Spirits", "Dai Huabin + Zhu Lu", "Dark"),
    ("God Duo Coexistence", "Tang San + Xiao Wu", "Light"),
    ("Angel God", "Qian Renxue", "Light"),
]

BODY_SOULS = [
    ("Spirit Eyes", "Eyes"), ("Spirit Arm", "Arm"), ("Spirit Belly", "Belly"),
    ("Spirit Body", "Body"), ("Spirit Brain", "Brain"), ("Spirit Buttocks", "Buttocks"),
    ("Spirit Calves", "Calves"), ("Spirit Fist", "Fist"), ("Spirit Hair", "Hair"),
    ("Spirit Hands", "Hands"), ("Spirit Legs", "Legs"), ("Spirit Torso", "Torso"),
    ("Asura Eyes", "Eyes"), ("Body Sect", "Body"), ("True Identity", "Body"),
]
BODY_TIERS = [("Iron", 0x9AA3B8), ("Bronze", 0xC08A4E), ("Silver", 0xC8D2E0), ("Gold", 0xE8C46A)]

ULTIMATE = [
    ("Ice Empress", "Ice", "Ling Luochen"), ("Snow Empress", "Ice", "Bing Mengxue"),
    ("Heavenly Snow Woman", "Ice", "Su Tong"), ("Ice Jade Empress Scorpion", "Ice", "Huo Yuhao"),
    ("Evil Fire Phoenix", "Fire", "Ma Hongjun"), ("Black Phoenix", "Fire", "Ma Xiaotao"),
    ("Judgement Sword", "Light", "Ye Xinglan"), ("Infernal Delicate Apricot", "Fire", "Ye Xishui"),
    ("Ice God's Twin Lotus Flowers", "Ice", "Bai Xiuxiu"), ("Ice Crystal Dragon and Phoenix", "Ice", "Lan Mengqin"),
    ("Abyss Demon Dragon", "Dark", "Mo Hou"), ("Sky Blue Ice Devouring Dragon", "Ice", "Zi Ji"),
    ("Azure Frozen Divine Sword", "Ice", "Zi Ji"), ("Three Eyed Golden Lion", "Lightning", "Shrek"),
    ("Dark Lightning Element", "Lightning", "Dai Mubai"), ("Ultimate Metal", "Metal", "Xiao Hongchen"),
    ("Ultimate Fire", "Fire", "Ma Xiaotao"), ("Ultimate Ice", "Ice", "Ling Luochen"),
    ("Ultimate Space", "Space", "Tang Wulin"), ("Ultimate Earth", "Earth", "Ye Xiangrong"),
    ("Ultimate Dark", "Dark", "Long Xiaoyao"), ("Ultimate Light", "Light", "Mu En"),
    ("Ultimate Wind", "Wind", "Dai Yun'er"), ("Ultimate Thunder", "Lightning", "Wang Qiuer"),
]

# ------------------------------------------------------------------ skill lore

FAM_SKILLS = {
    "Tool": [("Strike", "Hantaman {}, musuh dalam jangkauan terkena.", "ENEMY_AOE", "PLANE"),
             ("Cleave", "Tebasan berat yang membelah musuh di depan.", "ENEMY_CONE", "CONE"),
             ("Guard", "Cangkang energies melindingi tubuh.", "SELF", "RING"),
             ("Pierce", "Tusukan tajam menembus pertahanan.", "ENEMY_NEAREST", "LINE"),
             ("Domain", "Kdomain penuhmeister yang menekan seluruh area.", "POINT_AOE", "COLUMN")],
    "Beast": [("Claw", "Cakaran }, menusuk musuh terdekat.", "ENEMY_NEAREST", "BURST"),
              ("Roar", "Auman membuat musuh kehilangan orientasi.", "ENEMY_AOE", "CONE"),
              ("Pounce", "Terjang Narrative yang menabrak musuh.", "ENEMY_LINE", "LINE"),
              ("Frenzy", "Amukan membuat tubuh jauh lebih kuat.", "SELF", "RING"),
              ("Savage", "Hujan serangan dari mana soul beast.", "ENEMY_AOE", "BURST")],
    "Plant": [("Entangle", "Akar mengikat musuh di sekitar.", "ENEMY_AOE", "RING"),
              ("Parasite", "Benih parasit mengisap life musuh.", "ENEMY_NEAREST", "BURST"),
              ("Poison", "Racun yang melumpuhkan lawan.", "ENEMY_NEAREST", "SPHERE"),
              ("Bloom", "Bunga mekar menyembuhkan sekutu.", "ALLY_AURA", "SPHERE"),
              ("Verdant Wrath", "Hutan hidup menelan seluruh area.", "POINT_AOE", "COLUMN")],
    "Dragon": [("Thunder", "Petir menyambar ke musuh terdekat.", "ENEMY_NEAREST", "LINE"),
               ("Dragon Might", "Aura Wilmington menekan semua musuh.", "ENEMY_AOE", "SPHERE"),
               ("Dragon Breath", "Napak flamme menyapu area depan.", "ENEMY_CONE", "CONE"),
               ("Scale Guard", "Sisik menjadi perisai剃 yang kokoh.", "SELF", "RING"),
               ("Dragon Roar", "Auman naga membelah langit.", "POINT_AOE", "BURST")],
    "Element": [("Element Burst", "Energi elemen meledak di sekitar.", "ENEMY_AOE", "SPHERE"),
                ("Element Shield", "Perisai elemen menahan serangan.", "SELF", "RING"),
                ("Element Chain", "Rantai elemen mengikat musuh.", "ENEMY_AOE", "LINE"),
                ("Element Lance", "Tombak elemen menusuk ke depan.", "ENEMY_CONE", "LINE"),
                ("Element Domain", "Domain elemen membekukan area.", "POINT_AOE", "COLUMN")],
    "Body": [("Second Awakening", "Tubuh 형태POR awakened menjadi lebih kuat.", "SELF", "SPHERE"),
             ("Iron Body", "Tubuh berubah keras seperti besi.", "SELF", "RING"),
             ("Muscle Burst", "Otot menegas dan menolak musuh.", "ENEMY_AOE", "PLANE"),
             ("Bone Guard", "Tulang menutupi titik lemah tubuh.", "SELF", "RING"),
             ("Golden Rebirth", "Tubuh-)**, berevolusi menjadi versi emas.", "SELF", "BURST")],
    "Humanoid": [("Divine Blessing", "Berkah suci menyalakan sekutu.", "ALLY_AURA", "RING"),
                 ("Holy Guard", "Perisai suci menahanSERANGAN.", "SELF", "RING"),
                 ("Sanctuary", "Area suci menyembuhkan sekutu.", "ALLY_AURA", "SPHERE"),
                 ("Judgement", "Hukuman suci menghakimi musuh.", "ENEMY_AOE", "COLUMN"),
                 ("Ascension", "Narasi Ascending ke bentuk yang lebih tinggi.", "SELF", "BURST")],
    "Food": [("Nourish", "Makanan memulihkan HP dan stamina.", "SELF", "SPHERE"),
             ("Shared Table", "Sekutu berbagiryth Hidden meal.", "ALLY_AURA", "RING"),
             ("Food Blessing", "Berkah makanan menaikkan avility.", "ALLY_AURA", "RING"),
             ("Gourmet", "H-food lezat memulihkan lebih banyak.", "SELF", "SPHERE"),
             ("Feast", "Pesta panjang撑 banyak等級.", "ALLY_AURA", "RING")],
    "Star": [("Starlight", "Cahaya bintang men descends ke musuh.", "ENEMY_AOE", "SPHERE"),
             ("Stellar Guidance", "Bintang menuntun langkah sekutu.", "ALLY_AURA", "RING"),
             ("Meteor", "Meteor jatuh menghantam area.", "POINT_AOE", "COLUMN"),
             ("Constellation", "Rasi bintang mengikat musuh.", "ENEMY_AOE", "RING"),
             ("Star Collapse", "Bintang runtuh memusnahkan area.", "POINT_AOE", "BURST")],
    "Evil": [("Corruption", "Kotakan=inline corrupts musuh.", "ENEMY_AOE", "SPHERE"),
             ("Blood Pact", "Sumpah darah menguras HP musuh.", "ENEMY_NEAREST", "BURST"),
             ("Malevolence", "Kerasukan yang melemahkan jiwa musuh.", "ENEMY_AOE", "CONE"),
             ("Undying", "Tubuh tidak bisa dibunuh begitu saja.", "SELF", "RING"),
             ("Abomination", "Wujud buruk mengikis seluruh area.", "POINT_AOE", "BURST")],
    "Fusion": [("Fusion Burst", "Dua soul menyatu dan meletus.", "ENEMY_AOE", "SPHERE"),
               ("Combined Strike", "Gabungan serangan menghantam garis depan.", "ENEMY_CONE", "CONE"),
               ("Twin Will", "Dua意志 menyatu jadi satu.", "SELF", "RING"),
               ("Domain Fusion", "Domain gabungan membekukan area.", "POINT_AOE", "COLUMN"),
               ("Ultimate Fusion", "Bentuk akhir perpaduan dua soul.", "POINT_AOE", "BURST")],
}

LORE_SKILLS = {
    "clear_sky_hammer": [("乱披风锤法", "Whirlwind Hammer", "ENEMY_AOE"), ("大须弥锤", "Great Sumeru Hammer", "ENEMY_NEAREST"),
                         ("昊天护体", "Clear Sky Guard", "SELF"), ("昊天真身", "True Body Manifestation", "SELF"),
                         ("昊天九绝", "Nine Clear Sky Arts", "POINT_AOE")],
    "blue_silver_grass": [("蓝银草·缠绕", "Blue Silver Entangle", "ENEMY_AOE"), ("寄生种子", "Parasitic Seed", "ENEMY_NEAREST"),
                          ("蓝银草·囚笼", "Blue Silver Cage", "POINT_AOE"), ("蓝银草·治愈", "Blue Silver Mending", "ALLY_AURA"),
                          ("蓝银皇", "Blue Silver Emperor", "ENEMY_AOE")],
    "seven_treasure_pagoda": [("力量支援", "Strength Support", "ALLY_AURA"), ("治愈支援", "Healing Support", "ALLY_AURA"),
                              ("坚毅支援", "Fortitude Support", "ALLY_AURA"), ("七宝真身", "Seven Treasure True Body", "SELF"),
                              ("九宝真身", "Nine Treasure True Body", "SELF")],
    "white_tiger": [("白虎烈光波", "White Tiger Light Wave", "ENEMY_LINE"), ("白虎金刚变", "White Tiger Vajra", "SELF"),
                    ("白虎啸月", "White Tiger Moon Roar", "ENEMY_AOE"), ("邪眸", "Evil Eye Stare", "ENEMY_NEAREST"),
                    ("白虎真身", "White Tiger True Body", "POINT_AOE")],
    "hell_civet": [("幽冥突刺", "Nether Thrust", "ENEMY_LINE"), ("幽冥分身", "Nether Clone", "SELF"),
                   ("幽冥领域", "Nether Domain", "POINT_AOE"), ("猫魅", "Civet Charm", "ENEMY_AOE"),
                   ("幽冥灵猫·真身", "Nether Civet True Body", "ENEMY_AOE")],
    "soft_bone_rabbit": [("柔骨兔·魅惑", "Soft Bone Charm", "ENEMY_AOE"), ("空间转移", "Spatial Transfer", "SELF"),
                         ("柔骨兔·瞬移", "Rabbit Blink", "SELF"), ("月兔领域", "Moon Rabbit Domain", "POINT_AOE"),
                         ("柔骨兔真身", "Soft Bone True Body", "SELF")],
    "jade_serpent": [("碧磷毒雾", "Jade Phosphor Poison Mist", "ENEMY_AOE"), ("碧磷之毒", "Jade Phosphor Venom", "ENEMY_NEAREST"),
                     ("碧磷蛇皇", "Jade Phosphor Serpent Emperor", "ENEMY_AOE"), ("碧磷之毒·强化", "Venom Empowerment", "SELF"),
                     ("蛇皇真身", "Serpent Emperor True Body", "POINT_AOE")],
    "evil_fire_phoenix": [("邪火凤凰·火线", "Evil Phoenix Fireline", "ENEMY_CONE"), ("凤凰流星", "Phoenix Meteor", "POINT_AOE"),
                          ("十首火凤凰", "Ten Headed Fire Phoenix", "POINT_AOE"), ("凤凰涅槃", "Phoenix Nirvana", "SELF"),
                          ("邪火净化", "Evil Fire Purification", "SELF")],
    "sausage": [("香肠·恢复", "Sausage Recovery", "SELF"), ("食神", "God of Cookery", "ALLY_AURA"),
                ("香肠·飞行蘑菇", "Flying Mushroom", "SELF"), ("食神护体", "Food God Blessing", "SELF"),
                ("美食盛宴", "Grand Feast", "ALLY_AURA")],
    "golden_dragon": [("黄金龙吼", "Golden Dragon Roar", "ENEMY_AOE"), ("龙爪", "Dragon Claw", "ENEMY_NEAREST"),
                      ("龙鳞护体", "Dragon Scale Guard", "SELF"), ("黄金龙·俯冲", "Golden Dragon Dive", "ENEMY_NEAREST"),
                      ("黄金龙王", "Golden Dragon King", "POINT_AOE")],
    "nine_treasure_pagoda": [("九宝真身", "Nine Treasure True Body", "SELF"), ("九宝琉璃塔·治愈", "Nine Treasure Healing", "ALLY_AURA"),
                             ("九宝琉璃塔·护盾", "Nine Treasure Shield", "ALLY_AURA"), ("九宝琉璃塔·圣光", "Nine Treasure Radiance", "ENEMY_AOE"),
                             ("九宝琉璃塔·神光", "Nine Treasure Divine Light", "POINT_AOE")],
    "fire_phoenix": [("火凤凰·火线", "Fire Phoenix Fireline", "ENEMY_CONE"), ("火凤凰·流星", "Fire Phoenix Meteor", "POINT_AOE"),
                     ("火凤凰·涅槃", "Fire Phoenix Nirvana", "SELF"), ("火凤凰·凤翼", "Fire Phoenix Wings", "ENEMY_AOE"),
                     ("火凤凰真身", "Fire Phoenix True Body", "POINT_AOE")],
    "evil_eyed_white_tiger": [("邪眸白虎·烈光波", "Evil Eye Light Wave", "ENEMY_LINE"), ("白虎金刚变", "White Tiger Vajra", "SELF"),
                              ("邪眸白虎·虎啸", "Evil Eye Roar", "ENEMY_AOE"), ("邪眸·摄魂", "Evil Eye Soul Bind", "ENEMY_NEAREST"),
                              ("邪眸白虎真身", "Evil Eye White Tiger True Body", "POINT_AOE")],
    "blue_silver_emperor": [("蓝银皇·缠绕", "Blue Silver Emperor Entangle", "ENEMY_AOE"), ("蓝银皇·寄生", "Blue Silver Emperor Parasite", "ENEMY_NEAREST"),
                             ("蓝银皇·囚笼", "Blue Silver Emperor Cage", "POINT_AOE"), ("蓝银皇·治愈", "Blue Silver Emperor Mending", "ALLY_AURA"),
                             ("蓝银皇帝", "Blue Silver Emperor Throne", "ENEMY_AOE")],
    "hoe": [("锄头·丰收", "Harvest", "SELF"), ("锄头·翻地", "Earth Turn", "ENEMY_AOE"), ("锄头·扎根", "Rooted Stance", "SELF"),
            ("锄头·尘卷", "Dust Tornado", "ENEMY_AOE"), ("锄头·农具真身", "Tool True Body", "SELF")],
    "mirror_twin_beast": [("镜兽·分身", "Mirror Twin", "SELF"), ("镜兽·割影", "Shadow Cut", "ENEMY_NEAREST"),
                          ("镜兽·换位", "Position Swap", "ENEMY_NEAREST"), ("镜兽·棱镜", "Prism Light", "ENEMY_AOE"),
                          ("镜兽·双生", "Perfect Twin", "POINT_AOE")],
}

LORE_DESC = {
    "Blue Silver Grass": "Rumput biru perak yang tampak lemah, tapi akarnya bisa mengikat dan meracuni segalanya.",
    "Clear Sky Hammer": "Palu Langit Jernih, martial soul legendaris klan Clear Sky. Penghancur segalanya.",
    "Seven Treasures Glazed Pagoda": "Pagoda tujuh lantai, martial soul support terkuat di Benua Douluo.",
    "(Evil-Eyed) White Tiger": "Harimau putih berekor milik Dai Mubai. Kekuatan fisik dan pertahanan yang brutal.",
    "Hell Civet": "Kucing neraka milik Zhu Zhuqing. Gesit, mematikan, dan muncul dari bayangan.",
    "Soft-Boned Rabbit": "Kelinci tulang lunak milik Xiao Wu. Pesona memikat dan gerakan secepat kilat.",
    "Jade Phosphor Serpent": "Ular giok beracun milik Dugu Yan. Kabut dan gigitan yang melumpuhkan perlahan.",
    "Evil Fire Phoenix (Fire Phoenix after purification)": "Phoenix api yang menyala di atas api. Sempurna dalam serangan dan kehancuran.",
    "Sausage": "Sosis buatan Food System Master. Penyembuh dan pembersih terbaik di medan perang.",
    "Blue Lightning Tyrant Dragon": "Naga petir biru milik Yu Tianheng. Setiap tingkat kultivasi menambah satu bagian tubuh yang berubah menjadi naga.",
    "Seraphim": "Enam sayap malaikat, lambangbihawa dewa, pewaris Spirit Hall.",
    "Golden Dragon": "Naga emas yang membawa darah purba Raja Naga. Kekuatan dan ruang yang tak terbatas.",
    "Fire Phoenix": "Phoenix api yang disucikan, possesses extreme fire setelah dibersihkan dari kegelapan.",
    "Nine-headed Fire Phoenix": "Sepuluh kepala api yang lahir dari pemurnian Phoenix Api Jahat.",
    "Mirror Twin Beast": "Binatang yang hidup di bayangan cermin. Serangannya selalu punya kembarannya sendiri.",
    "Ice Phoenix": "Phoenix es milik Shui Bing'er. Dingin yang membekukanPertahanan musuh.",
    "Bone Dragon": "Bone Dragon milik Bone Douluo Gu Rong. Tulang yang tidak bisa dihancurkan oleh Logam biasa.",
    "Seven Kills Sword": "Tujuh Kali Pembunuh, pedang dengan serangan paling tajam di Benua Douluo.",
    "Evil-Eyed White Tiger": "Harimau putih dengan mata ungu yang memancarkan GVILES. Bentuk上位 dari harimau putih biasa.",
}

GENERIC_DESC = {
    "Tool": "Wujud alat yang dimanifestasikan sebagai senjata atau perlengkapan tempur.",
    "Beast": "Wujud makhluk yang menyatu dengan pemiliknya, membawa naluri dan kekuatan sendiri.",
    "Plant": "Wujud tumbuhan yang menyerap qi alam; akarnya menjadi senjata paling senyap.",
    "Dragon": "Wujud naga yang membawa darah purba. Napas dan cakarannya mengubah medan perang.",
    "Element": "Wujud elemen murni tanpa bentuk tetap. Kekuatannya datang dari atributnya sendiri.",
    "Body": "Wujud bagian tubuh yang menjadi soul, dan bisa mengalami Ignition kedua.",
    "Humanoid": "Wujud menyerupai manusia, dewa yang turun Interim, kekuatan berdatangan dari bloodline.",
    "Food": "Wujud makanan yang memberi kehidupan, di medanlebih dari obat biasa.",
    "Star": "Wujud bintang yang menyerap energi langit dan berdiri sendiri di angkasa.",
    "Evil": "Wujud tercela yang ganas dan kejam, diperoleh melalui jalan yang kotor.",
    "Fusion": "Wujud gabungan dua soul yang berhasil menyatu, sekuat dua pemilik sekaligus.",
}

SOUND_BY_ELEMENT = {
    "Fire": "minecraft:entity.blaze.shoot",
    "Ice": "minecraft:block.glass.break",
    "Lightning": "minecraft:entity.lightning_bolt.thunder",
    "Dark": "minecraft:entity.wither.shoot",
    "Light": "minecraft:block.beacon.power_select",
    "Poison": "minecraft:entity.sneeze",
    "Water": "minecraft:entity.dolphin.splash",
    "Wind": "minecraft:entity.breeze.wind_burst",
    "Earth": "minecraft:block.gravel.step",
    "Space": "minecraft:block.enchantment_table.use",
    "Life": "minecraft:block.grow.effect",
    "Flesh": "minecraft:entity.player.hurt",
    "Food": "minecraft:entity.generic.eat",
    "Fusion": "minecraft:block.beacon.activate",
    "Star": "minecraft:block.enchantment_table.use",
    "Steel": "minecraft:block.anvil.use",
    "Metal": "minecraft:block.anvil.use",
    "None": "minecraft:entity.evoker.cast_spell",
}

ELEMENT_BY_KEYWORD = [
    ("Lightning", ["Thunder", "Lightning", "Electric", "Bolt", "Tempest"]),
    ("Fire", ["Fire", "Flame", "Blaze", "Burn", "Phoenix", "Pyro", "Infernal"]),
    ("Ice", ["Ice", "Snow", "Frost", "Frozen", "Winter"]),
    ("Water", ["Water", "Sea", "Ocean", "Wave", "Rain", "Tide"]),
    ("Wind", ["Wind", "Cloud", "Gale", "Storm"]),
    ("Earth", ["Earth", "Stone", "Mountain", "Sand", "Dust", "Granite", "Soil"]),
    ("Light", ["Light", "Holy", "Sacred", "Radiant", "Sun", "Angel", "Bright", "Dawn"]),
    ("Dark", ["Dark", "Shadow", "Evil", "Night", "Abyss", "Nether", "Death", "Demon"]),
    ("Poison", ["Poison", "Venom", "Jade", "Miasma", "Toxin"]),
    ("Space", ["Space", "Void", "Time"]),
    ("Metal", ["Metal", "Steel", "Iron", "Gold", "Silver", "Bronze", "Armor"]),
    ("Light", ["Luminous"]),
]

RARITY_BY_OWNER = {
    "Tang San": "Legendary", "Tang Hao, Tang Chen": "Legendary", "Tang Wutong": "Legendary",
    "Yu Xiaogang": "Legendary", "Qian Renxue, Qian Daoliu, Qian Xunji": "Legendary",
    "Bibi Dong": "Legendary", "Mu En": "Legendary", "Long Xiaoyao": "Legendary",
    "Gu Yuena": "Legendary", "Wang Qiuer": "Legendary", "Dai Mubai , Davis": "Legendary",
    "Zhu Zhuqing, Zhu Zhuyun": "Legendary", "Xiao Wu": "Legendary", "Ning Rongrong, Ning Fengzhi": "Legendary",
    "Ma Hongjun": "Legendary", "Huo Yuhao": "Legendary", "Tang Wulin": "Legendary",
    "Yun Ming": "Legendary", "Lin Ru": "Legendary",
}

RARITY_WEIGHT = {"Common": 40, "Uncommon": 20, "Rare": 9, "Epic": 4, "Legendary": 1}

FAMILY_OF_NAME = [
    ("Tool", ["Sword", "Hammer", "Axe", "Shield", "Staff", "Spear", "Bow", "Fan", "Dagger", "Blade",
              "Cane", "Bell", "Banner", "Fan", "Chisel", "Umbrella", "Mirror", "Pearl", "Plate", "Rope",
              "Book", "Zither", "Flute", "Whip", "Crown", "Pagoda", "Cauldron", "Gun", "Needle", "Sickle"]),
    ("Plant", ["Grass", "Vine", "Flower", "Leaf", "Tree", "Bamboo", "Lotus", "Chrysanthemum", "Apricot", "Thorn"]),
    ("Dragon", ["Dragon", "Drake"]),
    ("Food", ["Sausage", "Cigar", "Bun", "Sugar Bean", "Chicken Leg", "Orange", "Food"]),
    ("Element", ["Element", "Ice", "Fire", "Earth", "Wind", "Cloud", "Snow", "Mist", "Water", "Space", "Metal"]),
    ("Star", ["Star", "Moon", "Sun", "Meteor", "Planet"]),
    ("Body", ["Spirit Arm", "Spirit Brain", "Spirit Eyes", "Spirit Body", "Spirit Fist", "Spirit Hair",
              "Spirit Hand", "Spirit Leg", "Spirit Calf", "Spirit Belly", "Spirit Buttock", "Body Sect"]),
    ("Humanoid", ["Angel", "Giant", "Emperor", "God", "Goddess", "Sea God"]),
    ("Evil", ["Abomination", "Witch", "Lich", "Zombie", "Skeleton", "Ghost", "Phantom", "Demon", "Doll", "Mask",
              "Nether", "Blood", "Hate", "Vengeance", "Nightmare", "Knight"]),
]

ELEMENT_COLORS = {
    "Lightning": 0x9BC8FF, "Fire": 0xFF8A3C, "Ice": 0xA8E4FF, "Water": 0x5BC8E0,
    "Wind": 0xC8F0D8, "Earth": 0xB08A55, "Light": 0xFFF3C4, "Dark": 0x8B7AC8,
    "Poison": 0x7BE0B0, "Space": 0xD8B4FF, "Metal": 0xC8D2E0, "None": 0x9AA6B8,
    "Life": 0x7BE0B0, "Flesh": 0xE0A090, "Food": 0xE8B44A, "Fusion": 0xFFD24A,
    "Steel": 0xB8C4D4, "Star": 0xFFF3C4,
}

EVOLUTION_PAIRS = {
    "Blue Silver Grass": "blue_silver_emperor",
    "Seven Treasures Glazed Pagoda": "nine_treasures_glazed_pagoda",
    "Evil Fire Phoenix (Fire Phoenix after purification)": "fire_phoenix",
    "White Tiger": "evil_eyed_white_tiger",
    "Jade Phosphor Serpent": "jade_phosphor_serpent_emperor",
}

FAMILY_TEMPLATES = {
    "Tool": "Alat", "Beast": "Binatang", "Plant": "Tumbuhan", "Dragon": "Naga", "Element": "Elemen",
    "Body": "Badan", "Humanoid": "Manusiawi", "Food": "Makanan", "Star": "Langit", "Evil": "Gelap",
    "Fusion": "Fusion",
}


def slug(text):
    text = re.sub(r"[^a-z0-9]+", "_", text.lower())
    return text.strip("_")


def gloss_cn(name):
    """Terjemahkan nama Mandarin memakai kamus komponen lore."""
    out = []
    i = 0
    while i < len(name):
        for key in GLOSS_KEYS:
            if name.startswith(key, i):
                out.append(GLOSS[key])
                i += len(key)
                break
        else:
            return None
    text = " ".join(out)
    text = re.sub(r"\s+", " ", text).strip()
    return text or None


PLANT_WORDS = ("grass", "flower", "vine", "leaf", "tree", "bamboo", "lotus", "chrysanthemum",
               "begonia", "apricot", "thorn", "apricot", "blossom", "plant", "grass", "petal")


def detect_element(name_en, family):
    lowered = name_en.lower()
    if family == "Plant":
        return "Life"
    if family == "Body":
        return "Flesh"
    if family == "Fusion":
        return "Fusion"
    if family == "Food":
        return "Food"
    if family == "Tool":
        return "Steel"
    if family == "Evil":
        return "Dark"
    if family == "Humanoid":
        return "Light"
    if family == "Star":
        return "Star"
    for element, keys in ELEMENT_BY_KEYWORD:
        for key in keys:
            if key.lower() in lowered:
                if element == "Metal" and any(w in lowered for w in PLANT_WORDS):
                    continue
                return element
    return "None"


def detect_family(name_en, name_cn=""):
    for family, keys in FAMILY_OF_NAME:
        for key in keys:
            if key.lower() in name_en.lower():
                return family
    return "Beast"


def rarity_for(name_en, owner, family):
    if owner:
        for part in re.split(r",\s*", owner):
            part = part.strip()
            if part in RARITY_BY_OWNER:
                return RARITY_BY_OWNER[part]
        for part in re.split(r",\s*", owner):
            part = part.strip()
            for key, value in RARITY_BY_OWNER.items():
                if part and part.startswith(key.split(",")[0]):
                    return value
    if family in ("Fusion",):
        return "Legendary"
    if family == "Body":
        return "Rare"
    lowered = name_en.lower()
    if any(k in lowered for k in ("divine", "god", "angel", "emperor", "king", "true body", "sovereign")):
        return "Legendary"
    if any(k in lowered for k in ("dragon", "phoenix", "tiger", "serpent", "demon", "abyss", "dragon")):
        return "Epic"
    if any(k in lowered for k in ("element", "spirit", "soul", "crystal", "star", "moon")):
        return "Rare"
    return "Common"


def make_skills(soul_id, family, element, name_en, owner):
    """Lima skill per soul: dari lore kalau soul-nya terkenal, selain itu dari arketipe keluarga."""
    color = ELEMENT_COLORS.get(element, 0x9AA6B8)
    accent = 0xFFFFFF
    lore = LORE_SKILLS.get(soul_id)
    if lore:
        specs = []
        for zh, en, target in lore:
            specs.append((en, f"{en} ({zh}). Ciri khas {name_en}.", target))
    else:
        specs = []
        for en, desc, target, shape in FAM_SKILLS.get(family, FAM_SKILLS["Beast"]):
            specs.append((f"{en}", f"{desc.replace('}', name_en).replace('{', name_en)} Ciri khas {name_en}.", target))

    shapes = ["SPHERE", "LINE", "CONE", "RING", "PLANE", "COLUMN", "BURST"]
    skills = []
    for index, (en, desc, target) in enumerate(specs):
        shape = shapes[(index + len(name_en)) % len(shapes)]
        size = 4.0 + (index % 3) * 1.5
        count = 26 + index * 12
        shake = 0.0
        flash = 0
        if index == 4:
            shape = "BURST"
            size = 7.0
            count = 90
            shake = 0.35
            flash = (0x50 << 24) | color
        elif index == 3:
            shake = 0.15
        if target == "SELF":
            damage = 0.0
            heal = 4.0 + index
        elif target == "ALLY_AURA":
            damage = 0.0
            heal = 5.0 + index
        else:
            damage = 6.0 + index * 3
            heal = 0.0
        effect = ""
        effect_ticks = 0
        effect_amp = 0
        self_effect = ""
        self_ticks = 0
        self_amp = 0
        if element == "Poison" and target != "SELF":
            effect, effect_ticks, effect_amp = "minecraft:poison", 160 + index * 20, 1
        elif element == "Fire" and target != "SELF":
            effect, effect_ticks, effect_amp = "minecraft:fire_resistance", 100, 0
        elif element == "Ice" and target != "SELF":
            effect, effect_ticks, effect_amp = "minecraft:movement_slowdown", 140 + index * 20, 1
        elif element == "Dark" and target != "SELF":
            effect, effect_ticks, effect_amp = "minecraft:weakness", 140, 1
        elif element == "Light":
            self_effect, self_ticks, self_amp = "minecraft:damage_boost", 200 + index * 40, 1
        elif element == "Lightning":
            self_effect, self_ticks, self_amp = "minecraft:motion_blindness" if False else "minecraft:jump", 200, 1
        if target == "SELF":
            self_effect = self_effect or "minecraft:damage_resistance"
            self_ticks = self_ticks or (240 + index * 40)
            self_amp = self_amp or 0
        skills.append({
            "id": f"{soul_id}_s{index + 1}",
            "name": en,
            "desc": desc,
            "cost": 20 + index * 8,
            "cd": 240 + index * 90,
            "rings": [1, 2, 3, 5, 7][index],
            "target": target,
            "shape": shape,
            "color": color,
            "color2": accent,
            "sound": SOUND_BY_ELEMENT.get(element, SOUND_BY_ELEMENT["None"]),
            "size": size,
            "count": count,
            "shake": shake,
            "flash": flash,
            "damage": damage,
            "heal": heal,
            "radius": 5.0 + index * 1.2,
            "effect": effect,
            "effectTicks": effect_ticks,
            "effectAmp": effect_amp,
            "selfEffect": self_effect,
            "selfTicks": self_ticks,
            "selfAmp": self_amp,
        })
    return skills


def main():
    en_rows = [line.split("\t") for line in io.open(EN_TSV, encoding="utf-8").read().splitlines() if line.strip()]
    cn_rows = [line.split("\t") for line in io.open(CN_TSV, encoding="utf-8").read().splitlines() if line.strip()]
    fandom = [line.strip() for line in io.open(FANDOM_TXT, encoding="utf-8").read().splitlines() if line.strip()]

    souls = []
    seen_en = set()
    seen_cn = set()

    def add(name_en, name_cn, owner, affiliation, family_hint=None, notable=False, related=""):
        if not name_en and not name_cn:
            return None
        key_cn = name_cn
        if key_cn and key_cn in seen_cn:
            return None
        display_en = name_en or gloss_cn(name_cn)
        if not display_en:
            display_en = name_cn
        key_en = slug(display_en)
        if key_en and key_en in seen_en:
            return None
        family = family_hint or detect_family(display_en, name_cn)
        element = detect_element(display_en, family)
        rarity = rarity_for(display_en, owner, family)
        soul_id = key_en[:48]
        if not soul_id or re.search(r"[\u4e00-\u9fff]", display_en):
            digest = hashlib.md5((name_cn or display_en).encode("utf-8")).hexdigest()[:10]
            soul_id = ("zh_" + digest) if not soul_id else (soul_id + "_" + digest[:6])
        seen_en.add(soul_id)
        if key_cn:
            seen_cn.add(key_cn)
        weight = RARITY_WEIGHT[rarity]
        if family in ("Fusion",):
            weight = 0
        if rarity == "Legendary" and family not in ("Fusion",):
            weight = 1
        description = LORE_DESC.get(display_en) or GENERIC_DESC.get(family, GENERIC_DESC["Beast"])
        if owner:
            description = description + " Pemilik asli: " + owner + "."
        record = {
            "id": soul_id,
            "name": display_en,
            "nameZh": name_cn or "",
            "owner": owner or "",
            "affiliation": affiliation or "",
            "family": family,
            "type": FAMILY_TEMPLATES.get(family, "Binatang"),
            "desc": description,
            "evolvesTo": EVOLUTION_PAIRS.get(display_en, ""),
            "rarity": rarity,
            "weight": weight,
            "element": element,
            "color": ELEMENT_COLORS.get(element, 0x9AA6B8),
            "portrait": f"soulland:soul/{soul_id}",
            "notable": bool(notable),
            "related": related,
            "skills": make_skills(soul_id, family, element, display_en, owner),
        }
        souls.append(record)
        return record

    # 1. Tabel Moegirlpedia: nama Inggris + Mandarin sejajar, pemilik, afiliasi.
    for index, en_row in enumerate(en_rows):
        cn_row = (cn_rows[index] + ["", "-", "-", "-"])[:4] if index < len(cn_rows) else ["", "-", "-", "-"]
        en_name, en_owner, en_affil, en_related = en_row[0], en_row[1], en_row[2], en_row[3]
        cn_name, cn_owner, cn_affil, cn_related = cn_row[0], cn_row[1], cn_row[2], cn_row[3]
        owner = en_owner if en_owner != "-" else cn_owner
        owner = owner.replace("、", ", ").replace("，", ", ")
        affiliation = en_affil if en_affil != "-" else cn_affil
        related = en_related if en_related != "-" else cn_related

        en_parts = [p.strip() for p in re.split(r"→|->|, ", en_name) if p.strip()]
        cn_parts = [p.strip() for p in re.split(r"→|->", cn_name) if p.strip()]
        for part_index, part in enumerate(en_parts):
            cn_part = cn_parts[part_index] if part_index < len(cn_parts) else ""
            if part in ("Martial Soul",):
                continue
            add(part, cn_part, owner, affiliation, notable=True, related=related)

    # 2. Daftar abjad wiki Soul Land (Tool / Beast / Body).
    for name in fandom:
        add(name, "", "", "")

    # 3. Kategori Mandarin dari daftar lengkap, untuk soul yang belum punya nama Inggris.
    for line in io.open(CN_ALL_TSV, encoding="utf-8").read().splitlines():
        if not line.strip():
            continue
        cn_name, cn_family = (line.split("\t") + [""])[:2]
        cn_name = cn_name.strip()
        if not cn_name or cn_name in seen_cn:
            continue
        family = CN_FAMILY_MAP.get(cn_family, "Beast")
        add("", cn_name, "", "", family_hint=family)

    # 4. Wujud evolusi yang disebut lore tapi tidak masuk tabel utama.
    evolution_pairs = [
        ("Blue Silver Emperor", "蓝银皇", "Tang San", "Tang Sect"),
        ("Fire Phoenix", "火凤凰", "Ma Xiaotao", "Fire Phoenix Soul Engineer Legion"),
        ("Ten Headed Fire Phoenix", "十首火凤凰", "Ma Hongjun", "Fire Phoenix Soul Engineer Legion"),
        ("Dark Blue Silver Grass", "暗黑蓝银草", "Tang Ya", "Tang Sect, Holy Spirit Cult"),
        ("Bone Dragon King", "骨龙王", "Wang Jinxi", "Holy Spirit Cult"),
        ("Skeleton King", "骷髅王", "Tang Dai", "Holy Spirit Cult"),
        ("Death Spider Emperor", "死亡蛛皇", "Bibi Dong", "Holy Spirit Cult"),
        ("Soul Devouring Spider Emperor", "噬魂蛛皇", "Bibi Dong", "Holy Spirit Cult"),
        ("Jade Phosphor Serpent Emperor", "碧磷蛇皇", "Dugu Bo", ""),
        ("Diamond Mammoth", "钻石猛犸象", "Huyan Li", "Elephant Armor Sect"),
        ("Hercules Gorilla", "大力猩猩", "Titan", "Strength Clan"),
        ("Ghost", "幽灵", "Nana", "Ghosts"),
        ("Evil Phoenix", "邪凤凰", "Vice Cult Master of the Holy Spirit Cult", "Holy Spirit Cult"),
        ("Sacred Angel", "神圣天使", "Ye Guyi", "Seraphim"),
        ("Spirit Eyes", "灵眸", "Huo Yuhao", "Body Sect"),
        ("Ice Spirit", "冰灵", "Xue Wu", "Far North"),
        ("Nine Treasures Glazed Pagoda", "九宝琉璃塔", "Ning Rongrong", "Seven Treasures Glazed Sect"),
        ("Radiant Holy Dragon", "光明圣龙", "Mu En", "Blue Lightning Tyrant Dragon Clan"),
        ("Dark Holy Dragon", "黑暗圣龙", "Long Xiaoyao", "Holy Spirit Cult"),
        ("Xuanwu Shield", "玄武盾", "Xu Sanshi", "Xuanming Sect"),
        ("Black Tortoise Shield", "玄冥盾", "Xu Sanshi", "Xuanming Sect"),
        ("Xuanming Shield", "玄冥龟甲盾", "Xu Sanshi", "Xuanming Sect"),
        ("Ice Crystal Dragon and Phoenix", "冰晶龙凤", "Lan Mengqin", "Far North"),
        ("Abyss Ice Demon Dragon", "深渊冰魔龙", "Bai Xiuxiu", ""),
        ("Five Elements Qilin", "五行麒麟", "Tang Yuge", ""),
        ("Heavenly Stems Qilin", "天干麒麟", "Tang Yuge", ""),
        ("Jade Phoenix Lute", "玉凰琴", "Lan Mengqin", ""),
        ("Elven King Bow", "精灵王弓", "Yuanen Huihui", ""),
        ("Silver Dragon King", "银龙王", "Gu Yuena", ""),
        ("Golden Dragon King", "黄金龙王", "Tang Wulin", ""),
        ("Dragon God", "龙神", "Tang Wulin", ""),
        ("True Body Blue Silver Emperor", "真本体武魂蓝银皇", "Tang Wulin", ""),
        ("Blue Lightning Tyrant Dragon Sect Line", "蓝电霸王龙", "Yu Tianheng", "Blue Lightning Tyrant Dragon Sect"),
        ("Fire Dragon", "火龙", "Liu Erlong", ""),
        ("Single Horned Fire Tyrant Dragon", "独角火暴龙", "Huo Wushuang", ""),
        ("Red Dragon", "红龙", "Wu Feng", ""),
        ("Luo Sanpao", "罗三炮", "Yu Xiaogang", ""),
        ("Owl", "猫头鹰", "Flender", ""),
        ("Nine Heart Begonia", "九心海棠", "Ye Lingling", ""),
        ("Velvet Sky Chrysanthemum", "奇茸通天菊", "Yue Guan", ""),
        ("Seven Kills Sword", "七杀剑", "Chen Xin", ""),
        ("Moon Blade", "月刃", "Xie Yue", "The Moon"),
        ("Golden Crocodile King", "黄金鳄王", "Golden Crocodile Douluo", ""),
        ("Coiling Dragon Staff", "盘龙棍", "Qianjun Douluo", "Qiangu Clan"),
        ("Soul Breaking Spear", "破魂枪", "Yang Wudi", "Break Clan"),
        ("Needle-Tailed Swift", "尖尾雨燕", "Bai Chenxiang", "Speed Clan"),
        ("Plate Armor Rhinoceros", "板甲犀牛", "Niu Gao", "Defense Clan"),
        ("Titan Giant Ape", "泰坦巨猿", "Er Ming", "Titan Clan"),
        ("Bone Dragon", "骨龙", "Gu Rong", "Holy Spirit Cult"),
        ("Three Legged Golden Toad", "三足金蟾", "Xiao Hongchen", "Mingde Hall"),
        ("Vermilion Eyed Ice Toad", "朱晴冰蟾", "Meng Hongchen", "Mingde Hall"),
        ("Star Crown", "星冠", "Xu Jiujiu", ""),
        ("Ice Jade Emperor Scorpion", "冰碧帝皇蝎", "Huo Yuhao", "Far North"),
        ("Cloud Phoenix", "云凤", "", ""),
    ]
    for en_name, cn_name, owner, affiliation in evolution_pairs:
        add(en_name, cn_name, owner, affiliation, notable=True)

    # 4b. Bentuk dasar yang dipecah dari entri gabungan tabel lore.
    add("White Tiger", "白虎", "Dai Mubai", "Star Luo Empire", notable=True)

    # 5. Wujud tahun ring (soul beast yang berubah menjadi martial soul).
    beast_forms = [
        ("Blue Silver King", "蓝银王", "Tan"), ("Dark Gold Terrorclaw Bear", "暗金恐爪熊", "Xi Chenluo"),
        ("Demon Great White Shark", "魔魂大白鲨", "Bai Xiuxiu"), ("Blazing Lion", "烈焰雄狮", "Wild Lion Douluo"),
        ("Emerald Demon Bird", "翡翠魔鸟", "Song"), ("Datura Snake", "曼陀罗蛇", "Dugu Bo"),
        ("Abyss Demon Dragon", "深渊魔龙", "Mo Hou"), ("Three Eyed Golden Lion", "三眼金猊", "Shrek"),
        ("Berserk Lightning Panther", "电狂豹", "Yuan Rui"), ("Deep Sea Demon Whale", "深海魔鲸", "Mingmei"),
        ("Abyss Demon Dragon Shark", "深渊魔龙鲨", "Song"), ("Crimson Gold Leopard", "赤金豹", "Ghost Panther Douluo"),
        ("Bloody Baboon", "血猿", "Titan Clan"), ("Golden Fat Bird", "金纹冰蚕", "Feng Xiaotian"),
        ("Ice Phoenix Soul Beast", "冰凤凰", "Shui Bing'er"), ("Abyss Ice Demon Dragon Beast", "深渊冰魔龙", "Bai Xiuxiu"),
        ("Infernal Delicate Apricot Beast", "烈火杏娇疏", "Ye Xishui"), ("Black Twin Snake", "黑双蛇", "Shrek"),
        ("Evil Octopus", "邪章", "Tang San"), ("Flaming Lion King", "火焰狮王", "Wild Lion Douluo"),
        ("Ghost Shadow Leopard Emperor", "鬼影豹皇", "Ghost Panther Douluo"), ("Spiny Piranha", "刺豚", "Porcupinefish Douluo"),
        ("Azure Bull Python", "天青牛蟒", "Da Ming"), ("Swift Demon Shark", "急速魔鲨", "Mingmei"),
        ("Frost Ice Shark", "寒冰鲨", "Kehan"), ("Ghost Panther", "黑纹鬼豹", "Ghost Panther Douluo"),
        ("Blood Demon Mad Bear", "血魔狂熊", "Demon Bear Douluo"), ("Golden Earth Dragon", "白地甲龙", "Tuoba Xi"),
        ("Fighting Spirit Chicken", "战斗鸡", "Tang San"), ("Giant Hammer Beast", "巨槌兽", "Giant Hammer Douluo"),
        ("Blue Lightning Tyrant Dragon Beast", "蓝电霸王龙兽", "Yu Tianheng"), ("Phoenix Soul Beast", "火凤凰兽", "Ma Hongjun"),
        ("Starry Sky Beast", "星空兽", "Ye Yulin"), ("Nether Thunder Beast", "冥雷兽", "Nether Thunder Douluo"),
        ("Fate Beast", "命运兽", "Douluo Continent"), ("Iron Arm Bear", "铁臂熊", "Tie Li"),
        ("Fierce Grey Wolf", "烈火苍狼", "Qin Ming"), ("Wind Demon Wolf", "疾风魔狼", "Feng Xiaotian"),
        ("Gale Two Headed Wolf", "疾风双头狼", "Feng Xiaotian"), ("White Tiger Beast", "白虎兽", "Dai Mubai"),
        ("Nether Civet Beast", "幽冥灵猫兽", "Zhu Zhuqing"), ("Soft Bone Rabbit Beast", "柔骨兔兽", "Xiao Wu"),
        ("Jade Phosphor Serpent Beast", "碧磷蛇兽", "Dugu Bo"), ("Sausage Beast", "香肠兽", "Oscar"),
        ("Cangul Beast", "锄头兽", "Farmer"), ("Phoenix Meteor Beast", "凤凰流星兽", "Ma Hongjun"),
    ]
    for en_name, cn_name, owner in beast_forms:
        add(en_name, cn_name, owner, "", family_hint="Beast", notable=False)

    # 6. Fusion skill: soul gabungan yang eksplisit disebut lore.
    for en_name, owner, element in FUSION_SKILLS:
        add(en_name, "", owner, "", family_hint="Fusion", notable=True)

    # 7. Wujud body soul setelah awakening kedua: Black Iron, Bronze, Silver, Gold.
    for base_name, part in BODY_SOULS:
        for tier_name, tier_color in BODY_TIERS:
            add(f"{tier_name} {base_name}", "", "Body Sect", "", family_hint="Body")

    # 8. Wujud ultimate attribute yang tercatat di lore.
    for en_name, element, owner in ULTIMATE:
        add(en_name, "", owner, "", family_hint="Element", notable=True)

    # 9. Related martial souls yang disebut pada kolom afiliasi tabel lore.
    related_names = set()
    for line in io.open(EN_TSV, encoding="utf-8").read().splitlines():
        parts = line.split("\t")
        if len(parts) > 3 and parts[3] != "-":
            for name in re.split(r",\s*|\s{2,}", parts[3]):
                name = name.strip()
                if name and len(name) > 2 and not name.startswith("-"):
                    related_names.add(name)
    for name in sorted(related_names):
        add(name, "", "", "")

    # 9. Bentuk evolusi karena spirit ring: 10.000 dan 100.000 tahun.
    year_forms = []
    for soul in list(souls):
        if soul["family"] not in ("Beast", "Dragon", "Element"):
            continue
        for years, label, rarity, weight in (("10000", "10,000-Year", "Epic", 2), ("100000", "100,000-Year", "Legendary", 0)):
            year_forms.append({
                "id": f"{soul['id']}_{years}",
                "name": f"{soul['name']} ({label} Form)",
                "nameZh": soul["nameZh"],
                "owner": soul["owner"],
                "affiliation": soul["affiliation"],
                "family": soul["family"],
                "rarity": rarity,
                "weight": weight,
                "element": soul["element"],
                "color": soul["color"],
                "portrait": f"soulland:soul/{soul['id']}_{years}",
                "notable": False,
                "related": soul["id"],
                "evolvesTo": "",
                "skills": make_skills(f"{soul['id']}_{years}", soul["family"], soul["element"],
                                      f"{soul['name']} ({label} Form)", soul["owner"]),
            })
    for record in year_forms:
        if record["id"] in seen_en:
            continue
        seen_en.add(record["id"])
        souls.append(record)

    evolved_ids = {v for v in EVOLUTION_PAIRS.values()}
    for record in souls:
        if record["id"] in evolved_ids:
            record["weight"] = 0

    # Ringkas: soul dengan nama Mandarin yang tidak punya nama Inggris perlu warna & id stabil.
    for soul in souls:
        if not soul["nameZh"] and not soul["name"]:
            continue
        if re.search(r"[一-鿿]", soul["name"]):
            soul["id"] = "zh_" + slug(gloss_cn(soul["nameZh"]) or soul["nameZh"])[:40]
            soul["portrait"] = f"soulland:soul/{soul['id']}"

    # Pastikan setiap id unik: registry Java memuat berdasarkan id.
    used = set()
    for index, soul in enumerate(souls):
        base = soul["id"]
        candidate = base
        counter = 2
        while candidate in used:
            candidate = f"{base}_{counter}"
            counter += 1
        if candidate != base:
            soul["id"] = candidate
            soul["portrait"] = f"soulland:soul/{candidate}"
            for skill in soul["skills"]:
                skill["id"] = f"{candidate}_s{skill['id'].split('_s')[-1]}"
        used.add(candidate)

    os.makedirs(os.path.dirname(DATA), exist_ok=True)
    io.open(DATA, "w", encoding="utf-8").write(json.dumps({"souls": souls}, ensure_ascii=False, indent=1))
    notable = sum(1 for s in souls if s["notable"])
    print(f"Total martial soul: {len(souls)} (termasuk {notable} soul dengan pemilik dari lore)")
    print(f"Total skill: {sum(len(s['skills']) for s in souls)}")


if __name__ == "__main__":
    main()
