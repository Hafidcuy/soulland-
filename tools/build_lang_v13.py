#!/usr/bin/env python3
"""Tambahkan kunci bahasa v1.3 (Spirit Bone) ke en_us.json dan id_id.json.

Skrip ini idempoten: kunci yang sudah ada tidak ditimpa, jadi aman dijalankan
ulang. Isi kedua file dijaga tetap sinkron (jumlah dan nama kunci sama).

Teks memakai satu tanda persen tunggal (``%s``), karena formatter milik mod
mengganti setiap ``%s`` dengan argumen berurutan dan tidak memproses ``%%``.
"""
import json
import pathlib

ROOT = pathlib.Path(__file__).resolve().parent.parent
LANG = ROOT / "src/main/resources/assets/soulland/lang"

# Id tier umur, urut dari yang termuda. Sepadan dengan grade spirit ring.
TIERS = [
    ("y100", "Hundred Year", "Seratus Tahun", "100"),
    ("y1000", "Thousand Year", "Seribu Tahun", "1,000"),
    ("y10000", "Ten Thousand Year", "Sepuluh Ribu Tahun", "10,000"),
    ("y100000", "Hundred Thousand Year", "Seratus Ribu Tahun", "100,000"),
    ("y1000000", "One Million Year", "Satu Juta Tahun", "1,000,000"),
]

# Tujuh slot tubuh, urut sama seperti enum SpiritBoneSlot di kode.
SLOTS = [
    ("skull", "Skull", "Tengkorak"),
    ("torso", "Rib Cage", "Rongga Dada"),
    ("left_arm", "Left Arm", "Lengan Kiri"),
    ("right_arm", "Right Arm", "Lengan Kanan"),
    ("left_leg", "Left Leg", "Kaki Kiri"),
    ("right_leg", "Right Leg", "Kaki Kanan"),
    ("external", "External Bone", "Tulang Eksternal"),
]

# Nama item disusun dari tier lalu slot, jadi 35 nama unik.
ITEM_NAMES = {}
for tier_id, tier_en, tier_id_text, _ in TIERS:
    for slot_id, slot_en, slot_id_text in SLOTS:
        item_id = f"spirit_bone_{tier_id}_{slot_id}"
        ITEM_NAMES[f"item.soulland.{item_id}"] = (
            f"{tier_en} {slot_en}",
            f"{slot_id_text} {tier_id_text}",
        )

NEW = {}

# ---------------------------------------------------------------- nama tier dan slot
for tier_id, tier_en, tier_id_text, _ in TIERS:
    NEW[f"bone.soulland.tier.{tier_id}"] = (tier_en, tier_id_text)
for slot_id, slot_en, slot_id_text in SLOTS:
    NEW[f"bone.soulland.slot.{slot_id}"] = (slot_en, slot_id_text)

NEW.update(ITEM_NAMES)

# ---------------------------------------------------------------- tooltip item
NEW.update({
    "tooltip.soulland.bone.tier": ("Bone %s (%s years)", "Tulang %s (%s tahun)"),
    "tooltip.soulland.bone.slot": ("Slot: %s", "Slot: %s"),
    "tooltip.soulland.bone.attack": ("+%s attack", "+%s serangan"),
    "tooltip.soulland.bone.defense": ("+%s defense", "+%s defense"),
    "tooltip.soulland.bone.energy": ("+%s max spirit power", "+%s spirit power maksimum"),
    "tooltip.soulland.bone.regen": ("+%s HP per second", "+%s HP per detik"),
    "tooltip.soulland.bone.need_rings": (
        "Needs %s spirit rings (you have %s)",
        "Butuh %s spirit ring (kamu punya %s)"),
    "tooltip.soulland.bone.use": (
        "Right click to fit it into its slot.",
        "Klik kanan untuk memasang ke slotnya."),
})

# ---------------------------------------------------------------- pesan server
NEW.update({
    "message.soulland.bone.equipped": (
        "%s fitted into the %s slot.",
        "Tulang %s dipasang di slot %s."),
    "message.soulland.bone.removed": (
        "Bone removed from the %s slot.",
        "Tulang dilepas dari slot %s."),
    "message.soulland.bone.already": (
        "That bone is already fitted in its slot.",
        "Tulang itu sudah terpasang di slotnya."),
    "message.soulland.bone.empty_slot": (
        "That slot is empty.",
        "Slot itu memang kosong."),
    "message.soulland.bone.wrong_slot": (
        "%s cannot be fitted in the %s slot.",
        "%s tidak bisa dipasang di slot %s."),
    "message.soulland.bone.need_rings": (
        "%s needs %s spirit rings, you have %s.",
        "%s butuh %s spirit ring, kamu punya %s."),
    "message.soulland.bone.not_carried": (
        "You are not carrying that bone.",
        "Kamu tidak membawa tulang itu."),
    "message.soulland.bone.inventory_full": (
        "Your bag is full, %s cannot be taken off.",
        "Tas penuh, %s tidak bisa dilepas."),
    "message.soulland.bone.unknown": (
        "That bone is not recognized.",
        "Tulang itu tidak dikenali."),
    "message.soulland.bone.screen_opened": (
        "The Spirit Bone screen is open.",
        "Layar Tulang Spirit dibuka."),
})

# ---------------------------------------------------------------- layar boutique
NEW.update({
    "gui.soulland.bone.title": ("Spirit Bone Boutique", "Boutique Tulang Spirit"),
    "gui.soulland.bone.subtitle": (
        "Click a bone to fit it, click a slot to take it off",
        "Klik tulang untuk memasang, klik slot untuk melepas"),
    "gui.soulland.bone.list_header": ("BONES YOU CARRY", "TULANG YANG DIBAWA"),
    "gui.soulland.bone.list_empty": ("No bones yet", "Belum ada tulang"),
    "gui.soulland.bone.list_empty_hint": (
        "Bones drop from soul beasts and bosses.",
        "Tulang datang dari soul beast dan boss."),
    "gui.soulland.bone.set_header": ("SET BONUS", "BONUS SET"),
    "gui.soulland.bone.set_none": ("No set active yet", "Belum ada set aktif"),
    "gui.soulland.bone.set_none_hint": (
        "Fit 4 bones of the same age to wake a set bonus.",
        "Pasang 4 tulang satu mutu untuk memunculkan bonus set."),
    "gui.soulland.bone.set_attack": ("+%s% attack", "+%s% serangan"),
    "gui.soulland.bone.set_defense": ("+%s% defense", "+%s% defense"),
    "gui.soulland.bone.set_energy": ("+%s spirit power", "+%s spirit power"),
    "gui.soulland.bone.set_regen": ("+%s HP/s", "+%s HP/dtk"),
    "gui.soulland.bone.set_spirit_regen": (
        "+%s% spirit power regen",
        "+%s% regenerasi spirit power"),
    "gui.soulland.bone.set_peak": ("Peak: %s", "Puncak: %s"),
    "gui.soulland.bone.status.pending": ("Asking the server...", "Meminta server..."),
    "gui.soulland.bone.hint": (
        "Up and down arrows pick a bone, Enter fits it, Escape closes",
        "Panah atas bawah memilih, Enter memasang, Escape menutup"),
})

# ---------------------------------------------------------------- creative tab
NEW["itemGroup.soulland.spirit_bone"] = ("Spirit Bones", "Tulang Spirit")


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
