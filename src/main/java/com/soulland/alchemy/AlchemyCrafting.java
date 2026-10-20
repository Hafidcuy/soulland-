package com.soulland.alchemy;

import com.soulland.SoulLandMod;
import com.soulland.block.AlchemyStationBlock;
import com.soulland.cultivation.CultivationCapability;
import com.soulland.cultivation.ICultivation;
import com.soulland.item.LocalizedItem;
import com.soulland.lang.SoulLang;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Racikan di stasiun alchemy.
 *
 * <p>Server yang memutuskan. Bahan dihitung dari inventaris pemain, stok
 * dipotong hanya setelah resep dinyatakan benar, dan hasilnya langsung masuk
 * inventaris. Client tidak pernah menentukan apa yang boleh diracik.
 */
public final class AlchemyCrafting {
    private AlchemyCrafting() {}

    /** Item mod dengan id tertentu, atau null bila id tidak dikenal. */
    public static Item item(String itemId) {
        if (itemId == null || itemId.isEmpty()) return null;
        return BuiltInRegistries.ITEM.get(new ResourceLocation(SoulLandMod.MODID, itemId));
    }

    /** Berapa butir item tertentu yang dibawa pemain. */
    public static int count(ServerPlayer player, String itemId) {
        Item item = item(itemId);
        if (item == null) return 0;
        int total = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(item)) total += stack.getCount();
        }
        for (ItemStack stack : player.getInventory().offhand) {
            if (stack.is(item)) total += stack.getCount();
        }
        for (ItemStack stack : player.getInventory().armor) {
            if (stack.is(item)) total += stack.getCount();
        }
        return total;
    }

    /** True kalau semua bahan resep sudah lengkap di inventaris pemain. */
    public static boolean hasAll(ServerPlayer player, AlchemyRecipe recipe) {
        if (player.getAbilities().instabuild) return true;
        for (AlchemyRecipe.Ingredient ingredient : recipe.ingredients()) {
            if (count(player, ingredient.itemId()) < ingredient.count()) return false;
        }
        return true;
    }

    /** Bahasa pesan untuk pemain: pilihannya, atau default mod. */
    public static String languageOf(ServerPlayer player) {
        return CultivationCapability.get(player)
                .map(ICultivation::getLanguage)
                .map(SoulLang::normalize)
                .filter(code -> !code.isEmpty())
                .orElse(SoulLang.DEFAULT);
    }

    /**
     * Rakit satu resep.
     *
     * @param stationPos posisi stasiun yang membuka layar. Racikan hanya
     *                   boleh terjadi di dekat posisi itu.
     * @param maxDistance jarak maksimum pemain dari stasiun
     * @return true kalau racikan berhasil, false kalau bahan kurang, resep
     *         tidak dikenal, atau pemain terlalu jauh. Pesan untuk pemain
     *         sudah dikirim di dalam.
     */
    public static boolean craft(ServerPlayer player, AlchemyRecipe recipe, BlockPos stationPos, int maxDistance) {
        String language = languageOf(player);

        if (!nearStation(player, stationPos, maxDistance)) {
            player.sendSystemMessage(SoulLang.text(language, "message.soulland.alchemy.too_far",
                    "Kamu terlalu jauh dari stasiun alchemy."));
            return false;
        }

        if (recipe == null) {
            player.sendSystemMessage(SoulLang.text(language, "message.soulland.alchemy.unknown",
                    "Resep itu tidak dikenal."));
            return false;
        }

        if (!hasAll(player, recipe)) {
            player.sendSystemMessage(SoulLang.text(language, "message.soulland.alchemy.missing",
                    "Bahan kurang: %s.", missingList(player, recipe, language)));
            return false;
        }

        Item result = item(recipe.resultItemId());
        if (result == null) {
            player.sendSystemMessage(SoulLang.text(language, "message.soulland.alchemy.unknown",
                    "Resep itu tidak dikenal."));
            return false;
        }

        boolean creative = player.getAbilities().instabuild;
        if (!creative) {
            for (AlchemyRecipe.Ingredient ingredient : recipe.ingredients()) {
                remove(player, ingredient.itemId(), ingredient.count());
            }
        }

        give(player, new ItemStack(result, recipe.resultCount()));

        player.sendSystemMessage(SoulLang.text(language, "message.soulland.alchemy.crafted",
                "§aRacikan selesai: %sx %s.", recipe.resultCount(),
                LocalizedItem.name(result, language).getString()));
        player.level().playSound(null, player.blockPosition(), SoundEvents.BREWING_STAND_BREW,
                SoundSource.BLOCKS, 0.8f, 1.1f);
        return true;
    }

    /** True kalau pemain berdiri dekat stasiun alchemy yang membuka layar. */
    private static boolean nearStation(ServerPlayer player, BlockPos stationPos, int maxDistance) {
        if (stationPos == null) return false;
        double limit = (double) maxDistance * maxDistance;
        if (player.distanceToSqr(Vec3.atCenterOf(stationPos)) > limit) return false;
        BlockState state = player.serverLevel().getBlockState(stationPos);
        return state.getBlock() instanceof AlchemyStationBlock;
    }

    /** Daftar bahan yang kurang, ditulis sebagai "Nama (punya/butuh)". */
    private static String missingList(ServerPlayer player, AlchemyRecipe recipe, String language) {
        StringBuilder builder = new StringBuilder();
        for (AlchemyRecipe.Ingredient ingredient : recipe.ingredients()) {
            int have = count(player, ingredient.itemId());
            if (have >= ingredient.count()) continue;
            if (builder.length() > 0) builder.append(", ");
            Item item = item(ingredient.itemId());
            String name = item == null ? ingredient.itemId() : LocalizedItem.name(item, language).getString();
            builder.append(name).append(" (").append(have).append('/').append(ingredient.count()).append(')');
        }
        return builder.length() == 0 ? "-" : builder.toString();
    }

    /** Kurangi jumlah item di inventaris pemain. */
    private static void remove(ServerPlayer player, String itemId, int amount) {
        Item item = item(itemId);
        if (item == null) return;
        int left = amount;
        for (ItemStack stack : player.getInventory().items) {
            if (left <= 0) break;
            if (!stack.is(item)) continue;
            int take = Math.min(left, stack.getCount());
            stack.shrink(take);
            left -= take;
        }
        for (ItemStack stack : player.getInventory().offhand) {
            if (left <= 0) break;
            if (!stack.is(item)) continue;
            int take = Math.min(left, stack.getCount());
            stack.shrink(take);
            left -= take;
        }
        for (ItemStack stack : player.getInventory().armor) {
            if (left <= 0) break;
            if (!stack.is(item)) continue;
            int take = Math.min(left, stack.getCount());
            stack.shrink(take);
            left -= take;
        }
    }

    /** Beri hasil racikan, dan jatuhkan ke tanah kalau inventaris penuh. */
    private static void give(ServerPlayer player, ItemStack stack) {
        if (player.getInventory().add(stack)) return;
        player.drop(stack, false, false);
    }
}
