package com.soulland.item;

import com.soulland.cultivation.CultivationCapability;
import com.soulland.cultivation.CultivationManager;
import com.soulland.soul.MartialSoul;
import com.soulland.soul.MartialSouls;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Inti evolusi: bahan yang mengubah martial soul dasar menjadi bentuk evolusinya.
 *
 * <p>Syarat: 6 spirit ring sudah diserap, 8 esensi jiwa ada di inventory, dan soul
 * tersebut memang punya bentuk evolusi.
 */
public class EvolutionCoreItem extends Item {

    public static final int ESSENCE_COST = 8;

    public EvolutionCoreItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }

        var capOpt = CultivationCapability.get(player);
        if (capOpt.isEmpty()) {
            return InteractionResultHolder.fail(stack);
        }
        var cap = capOpt.get();

        MartialSoul soul = MartialSouls.get(cap.getMartialSoulId());
        if (soul == null) {
            player.sendSystemMessage(Component.literal("Kamu belum punya martial soul."));
            return InteractionResultHolder.fail(stack);
        }
        if (cap.getEvolutionStage() > 0) {
            player.sendSystemMessage(Component.literal("Martial soul-mu sudah berevolusi."));
            return InteractionResultHolder.fail(stack);
        }
        if (!soul.hasEvolution()) {
            player.sendSystemMessage(Component.literal("Martial soul \"" + soul.name() + "\" tidak punya bentuk evolusi."));
            return InteractionResultHolder.fail(stack);
        }
        if (cap.getRings() < 6) {
            player.sendSystemMessage(Component.literal("Evolusi butuh 6 spirit ring. Kamu punya " + cap.getRings() + "."));
            return InteractionResultHolder.fail(stack);
        }

        int essence = countEssence(player);
        if (essence < ESSENCE_COST) {
            player.sendSystemMessage(Component.literal("Evolusi butuh " + ESSENCE_COST + " esensi jiwa. Kamu punya " + essence + "."));
            return InteractionResultHolder.fail(stack);
        }

        consumeEssence(player, ESSENCE_COST);
        cap.setEvolutionStage(1);
        if (!player.isCreative()) {
            stack.shrink(1);
        }
        CultivationManager.applyStats(player);
        CultivationManager.sync(player);

        MartialSoul evolved = MartialSouls.get(soul.evolutionId());
        player.sendSystemMessage(Component.literal("Martial soul-mu berevolusi menjadi " + evolved.name() + "!")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        player.level().sendParticles(net.minecraft.core.particles.ParticleTypes.TOTEM_OF_UNDYING,
                player.getX(), player.getY() + 1.0, player.getZ(), 60, 1.0, 1.4, 1.0, 0.1);
        return InteractionResultHolder.success(stack);
    }

    private static int countEssence(Player player) {
        int total = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(ModItems.SOUL_ESSENCE.get())) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private static void consumeEssence(Player player, int amount) {
        int left = amount;
        for (int i = 0; i < player.getInventory().items.size() && left > 0; i++) {
            ItemStack stack = player.getInventory().items.get(i);
            if (!stack.is(ModItems.SOUL_ESSENCE.get())) continue;
            int take = Math.min(left, stack.getCount());
            stack.shrink(take);
            left -= take;
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Membuka bentuk evolusi martial soul.").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("Syarat: 6 spirit ring dan " + ESSENCE_COST + " esensi jiwa.")
                .withStyle(ChatFormatting.GRAY));
    }
}
