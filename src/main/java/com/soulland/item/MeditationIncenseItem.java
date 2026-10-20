package com.soulland.item;

import com.soulland.cultivation.CultivationCapability;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Dupa meditasi.
 *
 * <p>Memberi ketahanan dan regenerasi selama satu menit, sekaligus mengisi ulang
 * sebagian spirit power yang habis dipakai bertarung.
 */
public class MeditationIncenseItem extends Item {

    public MeditationIncenseItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }

        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 1200, 1, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 1200, 0, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.HERO_OF_THE_VILLAGE, 1200, 0, false, true));

        CultivationCapability.get(player).ifPresent(cap ->
                cap.setEnergy(Math.min(100f, cap.getEnergy() + 40f)));

        if (!player.isCreative()) {
            stack.shrink(1);
        }
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.SMOKE,
                    player.getX(), player.getY() + 1.0, player.getZ(), 20, 0.3, 0.6, 0.3, 0.02);
        }
        player.sendSystemMessage(Component.literal("Dupa menyala. Qi-mu beralir perlahan.")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Regenerasi II dan Resistance I selama 60 detik.")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Mengisi 40 Spirit Power.").withStyle(ChatFormatting.GRAY));
    }
}
