package com.soulland.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

/**
 * Kartu martial soul.
 *
 * <p>Sekali klik kanan, kartu soul terbuka sebagai overlay. Kartu ini juga berisi
 * daftar skill, status spirit ring, dan tempat memilih sekte.
 */
public class SoulCardItem extends Item {

    public SoulCardItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                    net.minecraft.client.Minecraft.getInstance().setScreen(
                            new com.soulland.screen.SoulCardScreen()));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
