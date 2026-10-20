package com.soulland.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Kristal soul: dipecah menjadi 8 esensi jiwa. */
public class SoulCrystalItem extends Item {

    public static final int ESSENCE_YIELD = 8;

    public SoulCrystalItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }
        if (!player.isCreative()) {
            stack.shrink(1);
        }
        player.getInventory().add(new ItemStack(ModItems.SOUL_ESSENCE.get(), ESSENCE_YIELD));
        player.sendSystemMessage(Component.literal("Kristal soul pecah menjadi " + ESSENCE_YIELD + " esensi jiwa.")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        return InteractionResultHolder.success(stack);
    }
}
