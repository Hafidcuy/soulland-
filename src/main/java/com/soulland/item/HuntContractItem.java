package com.soulland.item;

import com.soulland.cultivation.CultivationCapability;
import com.soulland.cultivation.CultivationManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Kontrak perburuan soul beast.
 *
 * <p>Kontrak memberi target jumlah soul beast yang harus diburu dalam waktu tertentu.
 * Setelah target tercapai, pemain menerima spirit ring dan esensi jiwa.
 */
public class HuntContractItem extends Item {

    public static final int TARGET = 10;
    public static final int DURATION_SECONDS = 600;

    public HuntContractItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }
        CultivationCapability.get(player).ifPresent(cap -> {
            long now = level.getGameTime();
            if (cap.getContractUntil() > now) {
                player.sendSystemMessage(Component.literal("Kontrak masih berjalan: " + cap.getContractKills()
                        + "/" + TARGET + " soul beast."));
                return;
            }
            cap.setContractKills(0);
            cap.setContractUntil(now + (long) DURATION_SECONDS * 20L);
            CultivationManager.sync(player);
            player.sendSystemMessage(Component.literal("Kontrak perburuan dimulai: buru " + TARGET
                    + " soul beast dalam " + (DURATION_SECONDS / 60) + " menit.")
                    .withStyle(ChatFormatting.GOLD));
        });
        if (!player.isCreative()) {
            stack.shrink(1);
        }
        return InteractionResultHolder.success(stack);
    }
}
