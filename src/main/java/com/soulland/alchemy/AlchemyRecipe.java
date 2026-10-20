package com.soulland.alchemy;

import java.util.List;

/**
 * Satu resep racikan di stasiun alchemy.
 *
 * <p>Id item memakai string, bukan {@code Item}, supaya kelas ini murni tanpa
 * kelas Minecraft. Aturannya bisa diuji tanpa runtime game, persis seperti
 * {@code Foundation} dan {@code RingRisk}.
 */
public record AlchemyRecipe(String id, String resultItemId, int resultCount, List<Ingredient> ingredients) {

    /** Satu bahan yang dibutuhkan, beserta jumlah butirnya. */
    public record Ingredient(String itemId, int count) {}

    public AlchemyRecipe {
        ingredients = List.copyOf(ingredients);
    }

    /** Total jumlah butir bahan yang dipakai satu resep. */
    public int totalIngredientCount() {
        int total = 0;
        for (Ingredient ingredient : ingredients) {
            total += ingredient.count();
        }
        return total;
    }

    /** Id bahan yang dipakai lebih dari satu kali dalam satu resep. */
    public boolean hasDuplicateIngredient() {
        for (int i = 0; i < ingredients.size(); i++) {
            for (int j = i + 1; j < ingredients.size(); j++) {
                if (ingredients.get(i).itemId().equals(ingredients.get(j).itemId())) return true;
            }
        }
        return false;
    }
}
