#!/usr/bin/env python3
"""Bangun data resep mod Soul Land (folder data/soulland/recipes).

Resep yang dibuat:
- 3 smelting + 3 blasting: rantai tempa Besi Spirit -> Baja Spirit -> Perak Dalam.
- 1 crafting shapeless: Serpihan Kristal Soul.
- 5 crafting shapeless: tiga herbal spirit dan dua pil kultivasi.
- 10 crafting shapeless: tool spirit mutu Besi Spirit (bahan awal upgrade).
- 20 smithing transform: naik mutu tool per soul (Besi -> Baja -> Perak Dalam).

Jalankan: python3 tools/build_recipes.py
"""
import json
import pathlib

ROOT = pathlib.Path(__file__).resolve().parent.parent
RECIPE_DIR = ROOT / "src/main/resources/data/soulland/recipes"

MODID = "soulland"

TIERS = ["spirit_iron", "spirit_steel", "deep_silver"]
NEXT_TIER = {"spirit_iron": "spirit_steel", "spirit_steel": "deep_silver"}

# soul -> (tipe tool, bahan tambahan untuk crafting mutu awal)
TOOLS = {
    "hoe": "sword",
    "blue_silver_grass": "spear",
    "hell_civet": "fang",
    "sausage": "fan",
    "seven_treasure_pagoda": "seal",
    "evil_fire_phoenix": "bow",
    "white_tiger": "claw",
    "soft_bone_rabbit": "staff",
    "jade_serpent": "dagger",
    "clear_sky_hammer": "hammer",
}

INGOT = {
    "spirit_iron": "spirit_iron_ingot",
    "spirit_steel": "spirit_steel_ingot",
    "deep_silver": "deep_silver_ingot",
}


def item(name: str, namespace: str = "minecraft"):
    return {"item": f"{namespace}:{name}"}


def write(name: str, payload: dict) -> None:
    RECIPE_DIR.mkdir(parents=True, exist_ok=True)
    path = RECIPE_DIR / f"{name}.json"
    path.write_text(json.dumps(payload, indent=2) + "\n", encoding="utf-8")
    print(f"  wrote {path.relative_to(ROOT)}")


def cooking(recipe_type: str, name: str, ingredient: str, result: str, xp: float, time: int) -> None:
    write(name, {
        "type": f"minecraft:{recipe_type}",
        "ingredient": item(ingredient),
        "result": f"{MODID}:{result}",
        "experience": xp,
        "cookingtime": time,
    })


def shapeless(name: str, ingredients: list, result: str, count: int = 1) -> None:
    write(name, {
        "type": "minecraft:crafting_shapeless",
        "ingredients": ingredients,
        "result": {"item": f"{MODID}:{result}", "count": count},
    })


def smithing(name: str, base: str, addition: str, result: str) -> None:
    write(name, {
        "type": "minecraft:smithing_transform",
        "template": item("soul_crystal_shard", MODID),
        "base": item(base, MODID),
        "addition": item(addition, MODID),
        "result": item(result, MODID),
    })


def main() -> None:
    print("Resep tempa:")
    cooking("smelting", "spirit_iron_ingot", "iron_ingot", "spirit_iron_ingot", 0.35, 200)
    cooking("blasting", "spirit_iron_ingot_from_raw", "raw_iron", "spirit_iron_ingot", 0.35, 100)
    cooking("smelting", "spirit_steel_ingot", "spirit_iron_ingot", "spirit_steel_ingot", 0.5, 240)
    cooking("blasting", "spirit_steel_ingot_from_iron", "spirit_iron_ingot", "spirit_steel_ingot", 0.5, 120)
    cooking("smelting", "deep_silver_ingot", "spirit_steel_ingot", "deep_silver_ingot", 0.7, 280)
    cooking("blasting", "deep_silver_ingot_from_steel", "spirit_steel_ingot", "deep_silver_ingot", 0.7, 140)

    print("Bahan dan consumable:")
    shapeless("soul_crystal_shard", [item("amethyst_shard"), item("diamond")], "soul_crystal_shard")
    shapeless("spirit_dew_leaf", [item("sweet_berries"), item("sugar")], "spirit_dew_leaf", 2)
    shapeless("spirit_moss_sprout", [item("wheat_seeds"), item("moss_block")], "spirit_moss_sprout", 2)
    shapeless("spirit_ember_leaf", [item("nether_wart"), item("blaze_powder")], "spirit_ember_leaf", 2)
    shapeless("cultivation_pill", [item("wheat"), item("honey_bottle"), item("amethyst_shard")], "cultivation_pill")
    shapeless("swift_pill", [item("sugar"), item("rabbit_foot"), item("blaze_powder")], "swift_pill")

    print("Tool mutu awal:")
    for soul, tool_type in TOOLS.items():
        shapeless(
            f"spirit_iron_{soul}_{tool_type}",
            [item(INGOT["spirit_iron"], MODID), item(INGOT["spirit_iron"], MODID), item("amethyst_shard")],
            f"spirit_iron_{soul}_{tool_type}",
        )

    print("Upgrade smithing:")
    for tier in ("spirit_iron", "spirit_steel"):
        nxt = NEXT_TIER[tier]
        for soul, tool_type in TOOLS.items():
            smithing(
                f"{tier}_to_{nxt}_{soul}_{tool_type}",
                f"{tier}_{soul}_{tool_type}",
                INGOT[nxt],
                f"{nxt}_{soul}_{tool_type}",
            )


if __name__ == "__main__":
    main()
