package com.soulland.item;

import com.soulland.cultivation.CultivationCapability;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Pil qi: mengisi ulang Spirit Power yang habis dipakai bertarung. */
public class QiPillItem extends Item {

    private static final float RESTORE = 50f;

    public QiPillItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }
        CultivationCapability.get(player).ifPresent(cap -> {
            float before = cap.getEnergy();
            cap.setEnergy(Math.min(100f, before + RESTORE));
            cap.setEnergy(cap.getEnergy());
            player.sendSystemMessage(Component.literal("Pil qi dissolves. Spirit Power +" + (int) (cap.getEnergy() - before))
                    .withStyle(ChatFormatting.AQUA));
        });
        if (!player.isCreative()) {
            stack.shrink(1);
        }
        return InteractionResultHolder.success(stack);
    }
}
