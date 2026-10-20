package com.soulland.alchemy;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Memastikan tabel resep alchemy konsisten dan tidak memakai bahan yang tidak ada. */
class AlchemyRecipesTest {

    /** Id item yang boleh dipakai sebagai bahan racikan. */
    private static final List<String> KNOWN_ITEMS = List.of(
            "spirit_dew_leaf", "spirit_moss_sprout", "spirit_ember_leaf", "soul_essence",
            "cultivation_pill", "swift_pill", "foundation_pill");

    @Test
    void adaTigaResepYangTerdaftar() {
        assertEquals(3, AlchemyRecipes.all().size());
        assertNotNull(AlchemyRecipes.byId(AlchemyRecipes.CULTIVATION));
        assertNotNull(AlchemyRecipes.byId(AlchemyRecipes.SWIFT));
        assertNotNull(AlchemyRecipes.byId(AlchemyRecipes.FOUNDATION));
    }

    @Test
    void idResepTidakDikenalDiberiNilaiKosong() {
        assertNull(AlchemyRecipes.byId("tidak_ada"));
        assertNull(AlchemyRecipes.byId(null));
        assertNull(AlchemyRecipes.byId(""));
    }

    @Test
    void setiapResepPunyaBahanDanHasil() {
        for (AlchemyRecipe recipe : AlchemyRecipes.all()) {
            assertFalse(recipe.ingredients().isEmpty(), recipe.id());
            assertTrue(recipe.resultCount() > 0, recipe.id());
            assertTrue(KNOWN_ITEMS.contains(recipe.resultItemId()), recipe.id());
            for (AlchemyRecipe.Ingredient ingredient : recipe.ingredients()) {
                assertTrue(KNOWN_ITEMS.contains(ingredient.itemId()), recipe.id());
                assertTrue(ingredient.count() > 0, recipe.id());
            }
        }
    }

    @Test
    void bahanTidakSamaDiDalamSatuResep() {
        for (AlchemyRecipe recipe : AlchemyRecipes.all()) {
            assertFalse(recipe.hasDuplicateIngredient(), recipe.id());
        }
    }

    @Test
    void pilFondasiButuhDuaEsensiJiwaDanHasilSatu() {
        AlchemyRecipe recipe = AlchemyRecipes.byId(AlchemyRecipes.FOUNDATION);
        assertNotNull(recipe);
        assertEquals("foundation_pill", recipe.resultItemId());
        assertEquals(1, recipe.resultCount());
        int essence = 0;
        for (AlchemyRecipe.Ingredient ingredient : recipe.ingredients()) {
            if (ingredient.itemId().equals("soul_essence")) essence = ingredient.count();
        }
        assertEquals(2, essence);
        assertEquals(4, recipe.totalIngredientCount());
    }

    @Test
    void duaResepAwalMembuatDuaPil() {
        assertEquals(2, AlchemyRecipes.byId(AlchemyRecipes.CULTIVATION).resultCount());
        assertEquals(2, AlchemyRecipes.byId(AlchemyRecipes.SWIFT).resultCount());
    }
}
