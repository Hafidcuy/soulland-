package com.soulland.item;

import com.soulland.client.ClientScreenOpener;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

/** Lencana sekte. Klik kanan membuka kartu soul langsung di tab sekte. */
public class SectTokenItem extends Item {

    public SectTokenItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientScreenOpener::openSectTab);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
