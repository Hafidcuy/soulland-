package com.soulland.item;

import com.soulland.CultivationRanks;
import com.soulland.cultivation.CultivationCapability;
import com.soulland.cultivation.CultivationManager;
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

/** Spirit Ring: klik kanan untuk menyerapnya (absorb) bila level mencukupi. */
public class SpiritRingItem extends Item {

    public enum Tier {
        Y100(100, 10),
        Y1000(1_000, 20),
        Y10000(10_000, 40),
        Y100000(100_000, 70);

        public final int years;
        public final int minLevel;

        Tier(int years, int minLevel) {
            this.years = years;
            this.minLevel = minLevel;
        }
    }

    private final Tier tier;

    public SpiritRingItem(Tier tier, Properties properties) {
        super(properties);
        this.tier = tier;
    }

    public Tier tier() {
        return tier;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) return InteractionResultHolder.success(stack);

        var capOpt = CultivationCapability.get(player);
        if (capOpt.isEmpty()) return InteractionResultHolder.fail(stack);
        var cap = capOpt.orElseThrow();

        if (!cap.isAwakened()) {
            player.sendSystemMessage(Component.literal("§cKamu belum membangkitkan martial soul. Masuk kembali ke dunia untuk awakening."));
            return InteractionResultHolder.fail(stack);
        }
        if (cap.getRings() >= CultivationRanks.MAX_RINGS) {
            player.sendSystemMessage(Component.literal("§cSpirit ring-mu sudah penuh (9 ring). Kamu sudah di puncak manusia."));
            return InteractionResultHolder.fail(stack);
        }
        if (cap.getLevel() < tier.minLevel) {
            player.sendSystemMessage(Component.literal("§cLevel-mu (" + cap.getLevel() + ") belum cukup untuk menyerap ring " + tier.years + " tahun. Butuh level " + tier.minLevel + "."));
            return InteractionResultHolder.fail(stack);
        }
        // Ring kuat menuntut kamu benar-benar berada di gerbang kultivasi (level kelipatan 10) atau di atasnya.
        int ringsAllowed = Math.max(1, cap.getLevel() / 10);
        if (cap.getRings() >= ringsAllowed && cap.getLevel() < CultivationRanks.MAX_LEVEL) {
            player.sendSystemMessage(Component.literal("§cTubuhmu belum siap untuk ring berikutnya. Naikkan level kultivasimu dulu."));
            return InteractionResultHolder.fail(stack);
        }

        cap.setRings(cap.getRings() + 1);
        if (!player.isCreative()) stack.shrink(1);
        CultivationManager.applyStats(player);
        CultivationManager.sync(player);
        player.sendSystemMessage(Component.literal("§a✦ Spirit Ring " + tier.years + " tahun diserap! Sekarang kamu punya " + cap.getRings() + " spirit ring. ✦")
                .withStyle(ChatFormatting.GOLD));
        if (cap.getRings() == 1) {
            player.sendSystemMessage(Component.literal("§eSkill 1 martial soul-mu terbuka! Tekan [R] untuk menggunakannya."));
        }
        if (cap.getRings() == 3) {
            player.sendSystemMessage(Component.literal("§eSkill 2 martial soul-mu terbuka! Tekan [G] untuk menggunakannya."));
        }
        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Spirit Ring berusia " + tier.years + " tahun").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.literal("Syarat serap: level " + tier.minLevel + "+").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Klik kanan untuk menyerap.").withStyle(ChatFormatting.GRAY));
    }
}
