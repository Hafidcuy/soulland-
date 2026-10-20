package com.soulland.alchemy;

import java.util.List;

/**
 * Daftar resep yang bisa diracik di stasiun alchemy.
 *
 * <p>Semua bahan memakai item yang sudah ada di mod, jadi station ini tidak
 * menambah sumber daya baru. Resep memakai tiga herbal dan esensi jiwa yang
 * sudah dibuang dari soul beast, lalu mengubahnya menjadi pil.
 *
 * <p>Daftar ini adalah sumber kebenaran tunggal. Layar di sisi client hanya
 * membacanya untuk menampilkan daftar, sedangkan server yang memvalidasi bahan
 * dan memotong stok.
 */
public final class AlchemyRecipes {
    private AlchemyRecipes() {}

    /** Resep Pil Kultivasi: dua herbal hangat plus esensi jiwa. */
    public static final String CULTIVATION = "cultivation";

    /** Resep Pil Kilat: lumut dan embun plus esensi jiwa. */
    public static final String SWIFT = "swift";

    /** Resep Pil Fondasi: dua herbal panas plus dua esensi jiwa. */
    public static final String FOUNDATION = "foundation";

    private static final List<AlchemyRecipe> RECIPES = List.of(
            new AlchemyRecipe(CULTIVATION, "cultivation_pill", 2, List.of(
                    new AlchemyRecipe.Ingredient("spirit_dew_leaf", 1),
                    new AlchemyRecipe.Ingredient("spirit_ember_leaf", 1),
                    new AlchemyRecipe.Ingredient("soul_essence", 1))),
            new AlchemyRecipe(SWIFT, "swift_pill", 2, List.of(
                    new AlchemyRecipe.Ingredient("spirit_moss_sprout", 1),
                    new AlchemyRecipe.Ingredient("spirit_dew_leaf", 1),
                    new AlchemyRecipe.Ingredient("soul_essence", 1))),
            new AlchemyRecipe(FOUNDATION, "foundation_pill", 1, List.of(
                    new AlchemyRecipe.Ingredient("spirit_ember_leaf", 1),
                    new AlchemyRecipe.Ingredient("spirit_moss_sprout", 1),
                    new AlchemyRecipe.Ingredient("soul_essence", 2))));

    /** Semua resep, urut sesuai urutan ditampilkan di layar. */
    public static List<AlchemyRecipe> all() {
        return RECIPES;
    }

    /** Resep dengan id tertentu, atau null bila id tidak dikenal. */
    public static AlchemyRecipe byId(String id) {
        if (id == null) return null;
        for (AlchemyRecipe recipe : RECIPES) {
            if (recipe.id().equals(id)) return recipe;
        }
        return null;
    }
}
