package com.soulland.item;

import com.soulland.CultivationRanks;
import com.soulland.cultivation.CultivationCapability;
import com.soulland.cultivation.CultivationManager;
import com.soulland.compat.CuriosBridge;
import com.soulland.cultivation.ICultivation;
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
 * Spirit Ring: klik kanan untuk menyerapnya (absorb) bila level mencukupi.
 *
 * <p>Tahan Shift saat klik kanan untuk absorpsi berani: spirit power dan sebagian HP
 * dikorbankan demi chances ring naik satu tingkat. Kalau gagal, ring hilang dan vitalitas
 *ILD pay. Ini meniru lore penyerap ring yang umurnya terlalu tinggi.
 */
public class SpiritRingItem extends Item {

    public static final float RISKY_CHANCE = 0.55f;
    public static final float RISKY_HP_COST = 0.20f;
    public static final float RISKY_ENERGY_COST = 40f;
    public static final float FAILURE_HP_COST = 0.25f;

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

        public Tier next() {
            return switch (this) {
                case Y100 -> Y1000;
                case Y1000 -> Y10000;
                case Y10000 -> Y100000;
                case Y100000 -> Y100000;
            };
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
        if (level.isClientSide) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }

        var capOpt = CultivationCapability.get(player);
        ICultivation cap = capOpt.orElse(null);
        if (cap == null) return InteractionResultHolder.fail(stack);

        if (!cap.isAwakened()) {
            player.sendSystemMessage(Component.literal("Kamu belum membangkitkan martial soul. Masuk kembali ke dunia untuk awakening."));
            return InteractionResultHolder.fail(stack);
        }
        if (cap.getRings() >= CultivationRanks.MAX_RINGS) {
            player.sendSystemMessage(Component.literal("Spirit ring-mu sudah penuh (9 ring). Kamu sudah di puncak manusia."));
            return InteractionResultHolder.fail(stack);
        }
        if (cap.getLevel() < tier.minLevel) {
            player.sendSystemMessage(Component.literal("Level-mu (" + cap.getLevel() + ") belum cukup untuk menyerap ring "
                    + tier.years + " tahun. Butuh level " + tier.minLevel + "."));
            return InteractionResultHolder.fail(stack);
        }

        int ringsAllowed = Math.max(1, cap.getLevel() / 10);
        if (cap.getRings() >= ringsAllowed && cap.getLevel() < CultivationRanks.MAX_LEVEL) {
            player.sendSystemMessage(Component.literal("Tubuhmu belum siap untuk ring berikutnya. Naikkan level kultivasimu dulu."));
            return InteractionResultHolder.fail(stack);
        }

        if (player.isShiftKeyDown()) {
            return absorbRisky(level, player, stack, cap);
        }
        return absorbNormal(level, player, stack, cap);
    }

    private InteractionResultHolder<ItemStack> absorbNormal(Level level, Player player, ItemStack stack, ICultivation cap) {
        if (CuriosBridge.available() && CuriosBridge.storeRing(player, stack.copy())) {
            if (!player.isCreative()) {
                stack.shrink(1);
            }
        }
        cap.setRings(cap.getRings() + 1);
        cap.setRingSkills(Math.max(cap.getRingSkills(), cap.getRings()));
        player.heal(player.getMaxHealth() * 0.10f);
        if (!player.isCreative()) {
            stack.shrink(1);
        }
        CultivationManager.applyStats(player);
        CultivationManager.sync(player);

        player.sendSystemMessage(Component.literal("Spirit Ring " + tier.years + " tahun diserap. Sekarang kamu punya "
                + cap.getRings() + " spirit ring.").withStyle(ChatFormatting.GOLD));
        announceSkillUnlock(player, cap.getRings());
        return InteractionResultHolder.success(stack);
    }

    /**
     * Absorpsi berani: pemain membayar HP dan spirit power demi chance mendapat hasil lebih baik.
     */
    private InteractionResultHolder<ItemStack> absorbRisky(Level level, Player player, ItemStack stack, ICultivation cap) {
        if (tier == Tier.Y100000) {
            player.sendSystemMessage(Component.literal("Ring 100.000 tahun sudah puncak. Tidak ada tingkat yang lebih tinggi."));
            return InteractionResultHolder.fail(stack);
        }
        float hpCost = player.getMaxHealth() * RISKY_HP_COST;
        if (player.getHealth() - hpCost <= 1f) {
            player.sendSystemMessage(Component.literal("Darahmu terlalu tipis untuk absorpsi berani."));
            return InteractionResultHolder.fail(stack);
        }
        if (cap.getEnergy() < RISKY_ENERGY_COST) {
            player.sendSystemMessage(Component.literal("Spirit Power tidak cukup untuk absorpsi berani (butuh "
                    + (int) RISKY_ENERGY_COST + ")."));
            return InteractionResultHolder.fail(stack);
        }

        player.setHealth(player.getHealth() - hpCost);
        cap.setEnergy(cap.getEnergy() - RISKY_ENERGY_COST);
        if (!player.isCreative()) {
            stack.shrink(1);
        }

        boolean success = player.getRandom().nextFloat() < RISKY_CHANCE;
        if (success) {
            cap.setRings(cap.getRings() + 1);
            cap.setRingSkills(cap.getRingSkills() + 1);
            player.sendSystemMessage(Component.literal("Absorpsi berani berhasil! Ring naik ke "
                    + tier.next().years + " tahun dan skill ring tambahan terbuka.")
                    .withStyle(ChatFormatting.GOLD));
        } else {
            float loss = player.getMaxHealth() * FAILURE_HP_COST;
            player.setHealth(Math.max(1f, player.getHealth() - loss));
            player.sendSystemMessage(Component.literal("Absorpsi gagal. Ring hancur dan vitalitas turun.")
                    .withStyle(ChatFormatting.RED));
        }

        CultivationManager.applyStats(player);
        CultivationManager.sync(player);
        announceSkillUnlock(player, cap.getRings());
        return InteractionResultHolder.success(stack);
    }

    private void announceSkillUnlock(Player player, int rings) {
        if (rings == 1) {
            player.sendSystemMessage(Component.literal("Skill 1 terbuka. Tekan [R] untuk menggunakannya."));
        } else if (rings == 2) {
            player.sendSystemMessage(Component.literal("Skill 2 terbuka. Tekan [G] untuk menggunakannya."));
        } else if (rings == 3) {
            player.sendSystemMessage(Component.literal("Skill 3 terbuka. Tekan [F] untuk menggunakannya."));
        } else if (rings == 5) {
            player.sendSystemMessage(Component.literal("Skill 4 terbuka. Tekan [V] untuk menggunakannya."));
        } else if (rings == 7) {
            player.sendSystemMessage(Component.literal("Skill 5 terbuka. Tekan [Z] untuk menggunakannya."));
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Spirit Ring berusia " + tier.years + " tahun").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.literal("Syarat serap: level " + tier.minLevel + "+").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Bonus: +2 HP maksimum dan +0.5 attack damage").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Klik kanan untuk menyerap.").withStyle(ChatFormatting.GRAY));
        if (tier != Tier.Y100000) {
            tooltip.add(Component.literal("Tahan Shift: absorpsi berani, chance " + (int) (RISKY_CHANCE * 100)
                    + "% naik satu tingkat. Risiko: 20% HP dan 40 Spirit Power.")
                    .withStyle(ChatFormatting.DARK_RED));
        }
    }
}
