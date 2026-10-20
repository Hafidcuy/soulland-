package com.soulland.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Forge cincin: menaikkan satu tingkat spirit ring tertua di inventory.
 *
 * <p>Ini jalur alternatif untuk absorpsi brave: risikonya kecil, tapi butuh satu forge item
 * dan dua esensi jiwa.
 */
public class RingForgeItem extends Item {

    public static final int ESSENCE_COST = 2;

    public RingForgeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }

        if (countEssence(player) < ESSENCE_COST) {
            player.sendSystemMessage(Component.literal("Forge cincin butuh " + ESSENCE_COST + " esensi jiwa."));
            return InteractionResultHolder.fail(stack);
        }

        SpiritRingItem.Tier target = null;
        int targetIndex = -1;
        List<ItemStack> items = player.getInventory().items;
        for (int i = 0; i < items.size(); i++) {
            ItemStack candidate = items.get(i);
            if (!(candidate.getItem() instanceof SpiritRingItem ring)) continue;
            if (ring.tier() == SpiritRingItem.Tier.Y1000000) continue;
            if (target == null || ring.tier().years < target.years) {
                target = ring.tier();
                targetIndex = i;
            }
        }
        if (target == null) {
            player.sendSystemMessage(Component.literal("Tidak ada spirit ring yang bisa menaikkan tingkat."));
            return InteractionResultHolder.fail(stack);
        }

        consumeEssence(player, ESSENCE_COST);
        if (!player.isCreative()) {
            stack.shrink(1);
        }
        if (targetIndex >= 0) {
            items.set(targetIndex, new ItemStack(nextRingItem(target)));
        }
        player.sendSystemMessage(Component.literal("Forge cincin berhasil: spirit ring naik ke tingkat berikutnya.")
                .withStyle(ChatFormatting.GOLD));
        return InteractionResultHolder.success(stack);
    }

    private static net.minecraft.world.item.Item nextRingItem(SpiritRingItem.Tier tier) {
        return switch (tier) {
            case Y100 -> ModItems.SPIRIT_RING_1000.get();
            case Y1000 -> ModItems.SPIRIT_RING_10000.get();
            case Y10000 -> ModItems.SPIRIT_RING_100000.get();
            case Y100000 -> ModItems.SPIRIT_RING_1000000.get();
            case Y1000000 -> ModItems.SPIRIT_RING_1000000.get();
        };
    }

    private static int countEssence(Player player) {
        int total = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(ModItems.SOUL_ESSENCE.get())) total += stack.getCount();
        }
        return total;
    }

    private static void consumeEssence(Player player, int amount) {
        int left = amount;
        List<ItemStack> items = player.getInventory().items;
        for (int i = 0; i < items.size() && left > 0; i++) {
            ItemStack stack = items.get(i);
            if (!stack.is(ModItems.SOUL_ESSENCE.get())) continue;
            int take = Math.min(left, stack.getCount());
            stack.shrink(take);
            left -= take;
        }
    }
}
