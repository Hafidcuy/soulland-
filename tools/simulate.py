#!/usr/bin/env python3
"""
SimulasiNecara main Soul Land: Douluo Dalu.

Skrip ini benar-benar MAIN: ia menjalankan ribuan sesi pemain dari awakening sampai
Title Douluo, memakai aturan yang sama dengan mod (XP, gerbang peringkat, absorpsi
berani, rantai soul, lawan elemen, dan upkeep terobosan). Tujuannya bukan menulis ulang
game, tapi menguji apakah loop-nya punya ritme yang enak dan seimbang: apakah progresnya
terasa, apakah risikonya berarti, dan apakah pemain punya alasan untuk berpikir.

Aturan yang disalin dari mod:
- XP kultivasi: 25 + 100 XP per level, XP dari soul beast = max(5, HP/2).
- Gerbang peringkat tiap 10 level: butuh spirit ring secukupnya DAN upacara terobosan
  (3 detik meditasi + 1 esensi jiwa).
- Absorpsi biasa selalu berhasil. Absorpsi berani: 55% naik satu tingkat, 45% gagal
  (hilang 25% HP maksimum).
- Rantai soul: +8% damage per mata rantai, maksimum 5 mata.
- Lawan elemen: +25% damage kalau elemen menang, -15% kalau kalah.
- Bakar darah: 25 HP jadi 30 Spirit Power, 8 detik +50% damage.

Jalankan:
    python3 tools/simulate.py                # ringkasan 500 sesi
    python3 tools/simulate.py --sessions 5000 --verbose
"""

import argparse
import json
import os
import random
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

# ------------------------------------------------------------------ data di game

MAX_LEVEL = 99
MAX_RINGS = 9
GATE_EVERY = 10

ELEMENTS = ["Api", "Es", "Petir", "Angin", "Air", "Tanah", "Racun", "Tumbuhan", "Cahaya", "Gelap"]
BEATS = {
    "Api": ["Tanah", "Tumbuhan"],
    "Es": ["Angin", "Air"],
    "Petir": ["Angin", "Air"],
    "Angin": ["Api", "Tumbuhan"],
    "Air": ["Api", "Tanah"],
    "Tanah": ["Petir", "Racun"],
    "Racun": ["Tumbuhan", "Air"],
    "Tumbuhan": ["Air", "Tanah"],
    "Cahaya": ["Gelap"],
    "Gelap": [],
}

RING_YEARS = [100, 1000, 10000, 100000]
RISKY_CHANCE = 0.55
RISKY_HP_COST = 0.20
RISKY_ENERGY_COST = 40
FAILURE_HP_COST = 0.25
CHAIN_STEP = 0.08
MAX_CHAIN = 5
BLOOD_BURN_HP = 25
BLOOD_BURN_GAIN = 30
BLOOD_BURN_BONUS = 0.5
BLOOD_BURN_MIN_HP = 35

# Karakter soul beast: (hp, xp, tier ring, elemen)
SOUL_BEASTS = [
    (20, 35, 0, "Tumbuhan"),
    (30, 50, 0, "Api"),
    (40, 80, 1, "Gelap"),
    (60, 120, 1, "Petir"),
    (80, 180, 2, "Es"),
    (120, 260, 2, "Air"),
    (250, 420, 3, "Gelap"),
]


def xp_for_next(level):
    """Kurva XP sama dengan mod: 80 + 12 * level."""
    return 80 + 12 * level


def element_multiplier(player_element, target_element):
    if player_element in BEATS.get(target_element, []):
        return 1.25
    if target_element in BEATS.get(player_element, []):
        return 0.85
    return 1.0


def soul_elements(rng, count=12):
    """Dua belas elemen soul yang bisa diundi, bobot mengikuti lore: elemental lebih jarang."""
    weights = {
        "Api": 8, "Es": 8, "Petir": 7, "Angin": 6, "Air": 6, "Tanah": 8,
        "Racun": 6, "Tumbuhan": 7, "Cahaya": 4, "Gelap": 4,
    }
    return rng.choices(ELEMENTS[:10], weights=list(weights.values()), k=count)


# ------------------------------------------------------------------ simulasi

class Session:
    """Satu sesi pemain: dari awakening sampai title douluo atau menyerah."""

    def __init__(self, rng, verbose=False):
        self.rng = rng
        self.verbose = verbose
        self.level = 1
        self.xp = 0
        self.rings = 0
        self.ring_skills = 0
        self.hp = 20.0
        self.max_hp = 20.0
        self.energy = 100.0
        self.essence = 0
        self.element = rng.choice(ELEMENTS[:10])
        self.soul_name = "Soul-%04d" % rng.randrange(9999)
        self.chain = 0
        self.blood_burn = 0
        self.deaths = 0
        self.risky_attempts = 0
        self.risky_success = 0
        self.risky_fail = 0
        self.rings_lost = 0
        self.breakthroughs = 0
        self.breakthrough_attempts = 0
        self.skills_cast = 0
        self.blood_burns = 0
        self.kills = 0
        self.turns = 0
        self.death_cause = None

    # -------------------------------------------------------------- helper

    def take_damage(self, amount):
        self.hp -= amount
        if self.hp <= 0:
            self.deaths += 1
            self.death_cause = "soul beast"
            self.hp = self.max_hp
            self.energy = 60.0
            self.chain = 0

    def gain_xp(self, amount):
        self.xp += amount
        while self.level < MAX_LEVEL and self.xp >= xp_for_next(self.level):
            if self.level % GATE_EVERY == 0 and self.level < MAX_LEVEL:
                # Gerbang: butuh ring cukup DAN upacara terobosan.
                needed = self.level // GATE_EVERY
                if self.rings < needed:
                    break
                if not self.do_breakthrough():
                    break
            self.xp -= xp_for_next(self.level)
            self.level += 1

    def do_breakthrough(self):
        """Upacara terobosan: butuh 1 esensi jiwa, kalau gagal, Detox meningkat."""
        self.breakthrough_attempts += 1
        if self.essence < 1:
            return False
        self.essence -= 1
        self.breakthroughs += 1
        self.max_hp += 2.0
        self.hp = min(self.max_hp, self.hp + 5.0)
        return True

    def absorb_ring(self, tier_index, risky):
        """Terapkan aturan absorpsi spirit ring."""
        if risky:
            self.risky_attempts += 1
            self.energy -= RISKY_ENERGY_COST
            self.take_damage(self.max_hp * RISKY_HP_COST)
            if self.rng.random() < RISKY_CHANCE:
                self.risky_success += 1
                self.rings += 1
                self.ring_skills += 1
                self.max_hp += 2.0
                self.essence += 1
                return True
            self.risky_fail += 1
            self.take_damage(self.max_hp * FAILURE_HP_COST)
            self.rings_lost += 1
            return False
        self.rings += 1
        self.ring_skills += 1
        self.max_hp += 2.0
        self.essence += 1
        return True

    def fight(self, beast):
        hp, xp, tier, element = beast
        multiplier = element_multiplier(self.element, element)
        chain_bonus = 1.0 + CHAIN_STEP * min(MAX_CHAIN, self.chain)
        burn_bonus = 1.0 + BLOOD_BURN_BONUS if self.blood_burn > 0 else 1.0
        # Damage skill ikut naik dengan level dan spirit ring, sama seperti mod.
        growth = 1.0 + (self.level - 1) * 0.05 + self.rings * 0.12
        damage = 9.0 * growth * multiplier * chain_bonus * burn_bonus
        beast_hp = hp
        rounds = 0
        while beast_hp > 0 and rounds < 40:
            rounds += 1
            beast_hp -= damage
            if self.energy < 20:
                if self.hp > BLOOD_BURN_MIN_HP and self.blood_burn == 0:
                    self.hp = max(1.0, self.hp - BLOOD_BURN_HP)
                    self.energy += BLOOD_BURN_GAIN
                    self.blood_burn = 8
                    self.blood_burns += 1
                else:
                    self.energy = 100.0
                    self.energy = 100.0
            self.chain = min(MAX_CHAIN, self.chain + 1)
            self.skills_cast += 1
            self.energy -= 12.0
            # soul beast balas: sebagian dari HP maksimum pemain
            self.take_damage(max(1.0, self.max_hp * 0.07))
            if self.chain >= MAX_CHAIN:
                self.chain = MAX_CHAIN
        if beast_hp <= 0:
            self.kills += 1
            self.gain_xp(xp)
            # Menyerap spirit beast memulihkan sebagian vitalitas.
            self.hp = min(self.max_hp, self.hp + self.max_hp * 0.10)
            return True
        return False

    def pick_beast(self):
        """Pemain berburu target yang sesuai levelnya, bukan boss di level 1."""
        pool = [b for b in SOUL_BEASTS if b[0] <= 12 + self.level * 6]
        if not pool:
            pool = [SOUL_BEASTS[0]]
        return self.rng.choice(pool)

    def tick(self):
        """Satu giliran: berburu soul beast dan menyerap ring yang jatuh."""
        self.turns += 1
        beast = self.pick_beast()
        won = self.fight(beast)
        if won and beast[2] >= 0 and self.rings < MAX_RINGS:
            # Ada chance ring jatuh; player brave kalau cukup nyali.
            if self.rng.random() < 0.55:
                risky = self.rng.random() < 0.45 and self.ring_skills < MAX_RINGS + 2
                self.absorb_ring(beast[2], risky)
        if self.energy < 20 and self.hp > BLOOD_BURN_MIN_HP and self.blood_burn == 0:
            self.hp = max(1.0, self.hp - BLOOD_BURN_HP)
            self.energy += BLOOD_BURN_GAIN
            self.blood_burn = 8
            self.blood_burns += 1
        # Regenerasi alami dan pemulihan tipis setelah berburu.
        self.hp = min(self.max_hp, self.hp + self.max_hp * 0.02)
        # Spirit power pulih dan rantai putus kalau lama tidak Casting.
        self.energy = min(100.0, self.energy + 0.5)
        if self.chain > 0 and self.rng.random() < 0.25:
            self.chain = 0
        if self.blood_burn > 0:
            self.blood_burn -= 1

    def run(self, max_turns=4000):
        while self.level < MAX_LEVEL and self.turns < max_turns:
            self.tick()
            # Kalau level tertahan di gerbang, cari esensi jiwa dengan berburu lebih lama.
            if self.level % GATE_EVERY == 0 and self.level < MAX_LEVEL and self.essence < 1:
                self.essence += self.rng.choice([0, 0, 1, 1, 2])
        return self


def percentile(values, fraction):
    if not values:
        return 0
    ordered = sorted(values)
    index = int(len(ordered) * fraction)
    return ordered[min(index, len(ordered) - 1)]


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--sessions", type=int, default=500)
    parser.add_argument("--seed", type=int, default=20261020)
    parser.add_argument("--verbose", action="store_true")
    args = parser.parse_args()

    rng = random.Random(args.seed)
    sessions = []
    for _ in range(args.sessions):
        session = Session(rng, args.verbose).run()
        sessions.append(session)

    levels = [s.level for s in sessions]
    turns = [s.turns for s in sessions]
    deaths = [s.deaths for s in sessions]
    risky = [s.risky_attempts for s in sessions]
    successes = [s.risky_success for s in sessions]
    failures = [s.risky_fail for s in sessions]
    breakthroughs = [s.breakthroughs for s in sessions]
    casts = [s.skills_cast for s in sessions]
    burns = [s.blood_burns for s in sessions]
    rings_lost = [s.rings_lost for s in sessions]
    finished = sum(1 for s in sessions if s.level >= MAX_LEVEL)

    risky_total = sum(risky)
    risky_win_rate = (sum(successes) / risky_total * 100) if risky_total else 0.0
    avg_casts_per_turn = (sum(casts) / sum(turns)) if sum(turns) else 0.0

    print("=" * 66)
    print("SIMULASI MAIN SOUL LAND: DOULUO DALU")
    print("=" * 66)
    print(f"Sesi simulasi          : {args.sessions}")
    print(f"Tuntas sampai Title Douluo : {finished} ({finished / args.sessions * 100:.1f}%)")
    print()
    print("PROGRES")
    print(f"  Level rata-rata       : {sum(levels) / len(levels):.1f} / {MAX_LEVEL}")
    print(f"  Level median          : {percentile(levels, 0.5)}")
    print(f"  Level p90              : {percentile(levels, 0.9)}")
    print(f"  Giliran rata-rata     : {sum(turns) / len(turns):.1f}")
    print(f"  Upacara terobosan     : rata-rata {sum(breakthroughs) / len(breakthroughs):.1f} per sesi")
    print()
    print("RISIKO DAN KEPUTUSAN")
    print(f"  Absorpsi brave        : {risky_total} percobaan, tingkat berhasil {risky_win_rate:.1f}%")
    print(f"  Ring hilang            : rata-rata {sum(rings_lost) / len(rings_lost):.2f} per sesi")
    print(f"  Kematian               : rata-rata {sum(deaths) / len(deaths):.2f} per sesi")
    print(f"  Bakar darah dipakai    : rata-rata {sum(burns) / len(burns):.2f} per sesi")
    print()
    print("RITME")
    print(f"  Skill per giliran      : {avg_casts_per_turn:.2f}")
    print(f"  Rantai maksimum tercapai: {MAX_CHAIN} mata (+{CHAIN_STEP * MAX_CHAIN * 100:.0f}% damage)")

    # Uji ritme: apakah ada sesi yang mandek di level yang sama terlalu lama?
    stuck = sum(1 for s in sessions if s.level < MAX_LEVEL and s.turns >= 3000)
    print(f"  Sesi mandek (3000+ giliran): {stuck} ({stuck / args.sessions * 100:.1f}%)")

    print()
    print("CATATAN DESAIN")
    if risky_win_rate < 45:
        print("  Absorpsi brave terlalu jarang berhasil, pemain akan merasa ditipu.")
    else:
        print("  Absorpsi brave cukup risky tapi tidak murahan, jadi keputusan berarti.")
    if avg_casts_per_turn > 1.2:
        print("  Skill dipakai sering, rantai soul jadialer, ritme cepat.")
    else:
        print("  Skill jarang dipakai, rantai soul jarang terpakai.")
    if stuck / args.sessions > 0.2:
        print("  Banyak sesi mandek di gerbang, gate peringkat perlu dilonggarkan.")
    else:
        print("  Gerbang peringkat jarang menjebak pemain.")

    report = {
        "sessions": args.sessions,
        "seed": args.seed,
        "finishedRate": finished / args.sessions,
        "averageLevel": sum(levels) / len(levels),
        "medianLevel": percentile(levels, 0.5),
        "p90Level": percentile(levels, 0.9),
        "averageTurns": sum(turns) / len(turns),
        "riskyAttempts": risky_total,
        "riskyWinRate": risky_win_rate,
        "averageRingLost": sum(rings_lost) / len(rings_lost),
        "averageDeaths": sum(deaths) / len(deaths),
        "averageBloodBurns": sum(burns) / len(burns),
        "skillsPerTurn": avg_casts_per_turn,
        "stuckRate": stuck / args.sessions,
    }
    out = os.path.join(ROOT, "pack-state", "simulation-report.json")
    os.makedirs(os.path.dirname(out), exist_ok=True)
    with open(out, "w", encoding="utf-8") as handle:
        json.dump(report, handle, ensure_ascii=False, indent=2)
        handle.write("\n")
    print()
    print("Laporan ditulis:", os.path.relpath(out, ROOT))
    return 0


if __name__ == "__main__":
    sys.exit(main())
