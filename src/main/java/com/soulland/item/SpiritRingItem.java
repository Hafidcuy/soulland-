package com.soulland.item;

import com.soulland.CultivationRanks;
import com.soulland.cultivation.CultivationCapability;
import com.soulland.cultivation.CultivationManager;
import com.soulland.cultivation.ICultivation;
import com.soulland.cultivation.RingMutation;
import com.soulland.cultivation.RingRisk;
import com.soulland.lang.SoulLang;
import com.soulland.net.ClientBridge;
import com.soulland.soul.RingHaloSync;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Spirit Ring: klik kanan untuk menyerapnya.
 *
 * <p>Menyerap ring jauh di atas kapasitas level pemain itu berbahaya. Server
 * menghitung risikonya dari selisih umur ring terhadap batas aman level:
 * di dalam batas aman penyerapan selalu berhasil, di luar itu ring bisa
 * hancur dan pemain kena backlash. Ring yang berbahaya hanya bisa diserap
 * dengan konfirmasi (tahan Shift), supaya pemain sempat membaca peringatan
 * risikonya lebih dulu.
 *
 * <p>Selain itu, setiap penyerapan berhasil punya peluang kecil ring
 * bermutasi: umur efektifnya naik satu grade visual dan pemain mendapat bonus
 * stat permanen.
 */
public class SpiritRingItem extends LocalizedItem {

    /** Lamanya efek kelelahan setelah backlash. */
    private static final int BACKLASH_WEAKNESS_TICKS = 100;
    /** Lamanya efek lumpur setelah backlash berat. */
    private static final int BACKLASH_SLOWNESS_TICKS = 160;

    public enum Tier {
        Y100(100, 10),
        Y1000(1_000, 20),
        Y10000(10_000, 40),
        Y100000(100_000, 70),
        /** Grade Emas: hadiah puncak dari Kaisar Soul Beast, batas aman level 90. */
        Y1000000(1_000_000, 90);

        public final int years;
        /** Level minimum untuk menyerap ring ini tanpa risiko. */
        public final int safeLevel;

        Tier(int years, int safeLevel) {
            this.years = years;
            this.safeLevel = safeLevel;
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

        String language = CultivationManager.messageLanguage(player);

        if (!cap.isAwakened()) {
            player.sendSystemMessage(SoulLang.text(language, "message.soulland.ring.not_awakened",
                    "Kamu belum membangkitkan martial soul. Masuk kembali ke dunia untuk awakening."));
            return InteractionResultHolder.fail(stack);
        }
        if (cap.getRings() >= CultivationRanks.MAX_RINGS) {
            player.sendSystemMessage(SoulLang.text(language, "message.soulland.ring.full",
                    "Spirit ring-mu sudah penuh (%s ring). Kamu sudah di puncak manusia.",
                    CultivationRanks.MAX_RINGS));
            return InteractionResultHolder.fail(stack);
        }

        RingRisk.Severity severity = RingRisk.severity(cap.getLevel(), tier.safeLevel);
        if (severity != RingRisk.Severity.SAFE && !player.isShiftKeyDown()) {
            // Peringatan risiko: penyerapan berbahaya butuh konfirmasi.
            player.sendSystemMessage(SoulLang.text(language, "message.soulland.ring.risk_warning",
                    "§cSpirit Ring %s tahun terlalu berat untuk level %s. Risiko %s: gagal %s%%.",
                    tier.years, cap.getLevel(), riskLabel(language, severity),
                    RingRisk.failureChancePercent(cap.getLevel(), tier.safeLevel)));
            player.sendSystemMessage(SoulLang.text(language, "message.soulland.ring.risk_confirm",
                    "§eTahan Shift dan klik kanan lagi untuk mengonfirmasi penyerapan berisiko."));
            return InteractionResultHolder.fail(stack);
        }

        int failureChance = RingRisk.failureChancePercent(cap.getLevel(), tier.safeLevel);
        boolean failed = failureChance > 0 && player.getRandom().nextInt(100) < failureChance;

        if (failed) {
            absorbFailed(player, stack, cap, language, severity);
            return InteractionResultHolder.success(stack);
        }

        boolean mutated = player.getRandom().nextInt(100) < RingMutation.CHANCE_PERCENT;
        int effectiveAge = mutated ? RingMutation.mutatedAge(tier.years) : tier.years;
        cap.addRing(effectiveAge);
        if (mutated) {
            cap.setMutationCount(cap.getMutationCount() + 1);
        }
        if (!player.isCreative()) stack.shrink(1);
        CultivationManager.applyStats(player);
        CultivationManager.sync(player);
        if (player instanceof ServerPlayer serverPlayer) {
            RingHaloSync.broadcast(serverPlayer);
        }
        player.sendSystemMessage(SoulLang.text(language, "message.soulland.ring.absorbed",
                "§a✦ Spirit Ring %s tahun diserap! Sekarang kamu punya %s spirit ring.",
                tier.years, cap.getRings()).withStyle(ChatFormatting.GOLD));
        if (mutated) {
            player.sendSystemMessage(SoulLang.text(language, "message.soulland.ring.mutated",
                    "§d✧ Ring ini bermutasi! Umur efektifnya %s tahun dan bonus stat permanen melonjak.",
                    effectiveAge).withStyle(ChatFormatting.LIGHT_PURPLE));
        }
        announceSkillUnlock(player, cap.getRings(), language);
        return InteractionResultHolder.success(stack);
    }

    /** Ring hancur: pemain kehilangan ring dan kena backlash sesuai risikonya. */
    private void absorbFailed(Player player, ItemStack stack, ICultivation cap, String language,
                              RingRisk.Severity severity) {
        if (!player.isCreative()) stack.shrink(1);

        int backlash = RingRisk.backlashPercent(cap.getLevel(), tier.safeLevel);
        if (backlash > 0) {
            float damage = player.getMaxHealth() * backlash / 100f;
            player.setHealth(Math.max(1f, player.getHealth() - damage));
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, BACKLASH_WEAKNESS_TICKS, 0, false, true));
            if (severity == RingRisk.Severity.SEVERE) {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, BACKLASH_SLOWNESS_TICKS, 0, false, true));
                int drain = RingRisk.spiritDrain(cap.getLevel(), tier.safeLevel);
                if (drain > 0) {
                    cap.setEnergy(Math.max(0f, cap.getEnergy() - drain));
                }
            }
        }

        player.sendSystemMessage(SoulLang.text(language, "message.soulland.ring.failed",
                "§cPenyerapannya gagal! Spirit Ring %s tahun hancur.", tier.years)
                .withStyle(ChatFormatting.RED));
        if (backlash > 0) {
            player.sendSystemMessage(SoulLang.text(language, "message.soulland.ring.backlash",
                    "§cBacklash: vitalitas turun %s%% dan tubuhmu kaku.", backlash)
                    .withStyle(ChatFormatting.RED));
        }
        CultivationManager.applyStats(player);
        CultivationManager.sync(player);
    }

    private void announceSkillUnlock(Player player, int rings, String language) {
        if (rings == 1) {
            player.sendSystemMessage(SoulLang.text(language, "message.soulland.ring.skill_1",
                    "§eSkill 1 terbuka. Tekan [R] untuk menggunakannya."));
        } else if (rings == 3) {
            player.sendSystemMessage(SoulLang.text(language, "message.soulland.ring.skill_2",
                    "§eSkill 2 terbuka. Tekan [G] untuk menggunakannya."));
        } else if (rings == CultivationRanks.RINGS_FOR_THIRD_SKILL) {
            player.sendSystemMessage(SoulLang.text(language, "message.soulland.ring.skill_3",
                    "§eSkill 3 terbuka. Tekan [F] untuk menggunakannya."));
        } else if (rings == CultivationRanks.RINGS_FOR_FOURTH_SKILL) {
            player.sendSystemMessage(SoulLang.text(language, "message.soulland.ring.skill_4",
                    "§eSkill 4 terbuka. Tekan [V] untuk menggunakannya."));
        }
    }

    private String riskLabel(String language, RingRisk.Severity severity) {
        return switch (severity) {
            case SAFE -> SoulLang.translate(language, "message.soulland.ring.risk_safe", "aman");
            case MILD -> SoulLang.translate(language, "message.soulland.ring.risk_mild", "ringan");
            case MODERATE -> SoulLang.translate(language, "message.soulland.ring.risk_moderate", "sedang");
            case SEVERE -> SoulLang.translate(language, "message.soulland.ring.risk_severe", "berat");
        };
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        String language = SoulLang.activeLanguage();
        int playerLevel = ClientBridge.level();

        tooltip.add(SoulLang.text(language, "tooltip.soulland.ring.age",
                "Spirit Ring berusia %s tahun", tier.years).withStyle(ChatFormatting.AQUA));
        if (playerLevel > 0) {
            tooltip.add(SoulLang.text(language, "tooltip.soulland.ring.safe_level",
                    "Batas aman: level %s (kamu level %s)", tier.safeLevel, playerLevel)
                    .withStyle(ChatFormatting.GRAY));
            RingRisk.Severity severity = RingRisk.severity(playerLevel, tier.safeLevel);
            tooltip.add(riskTooltip(language, severity, playerLevel).withStyle(
                    severity == RingRisk.Severity.SAFE ? ChatFormatting.GRAY : ChatFormatting.DARK_RED));
        } else {
            tooltip.add(SoulLang.text(language, "tooltip.soulland.ring.safe_level_unknown",
                    "Batas aman: level %s", tier.safeLevel).withStyle(ChatFormatting.GRAY));
        }
        tooltip.add(SoulLang.text(language, "tooltip.soulland.ring.bonus",
                "Bonus: +2 HP maksimum dan +0.5 attack damage").withStyle(ChatFormatting.GRAY));
        tooltip.add(SoulLang.text(language, "tooltip.soulland.ring.use",
                "Klik kanan untuk menyerap.").withStyle(ChatFormatting.GRAY));
        tooltip.add(SoulLang.text(language, "tooltip.soulland.ring.confirm",
                "Tahan Shift saat klik kanan untuk mengonfirmasi penyerapan berisiko.")
                .withStyle(ChatFormatting.DARK_RED));
        tooltip.add(SoulLang.text(language, "tooltip.soulland.ring.mutation",
                "Peluang mutasi: %s%% (umur efektif naik satu grade, bonus stat permanen).",
                RingMutation.CHANCE_PERCENT).withStyle(ChatFormatting.LIGHT_PURPLE));
    }

    private MutableComponent riskTooltip(String language, RingRisk.Severity severity, int playerLevel) {
        int failure = RingRisk.failureChancePercent(playerLevel, tier.safeLevel);
        int backlash = RingRisk.backlashPercent(playerLevel, tier.safeLevel);
        return switch (severity) {
            case SAFE -> SoulLang.text(language, "tooltip.soulland.ring.risk_safe",
                    "Risiko: aman, penyerapan selalu berhasil.");
            case MILD -> SoulLang.text(language, "tooltip.soulland.ring.risk_mild",
                    "Risiko ringan: gagal %s%%, ring hancur.", failure);
            case MODERATE -> SoulLang.text(language, "tooltip.soulland.ring.risk_moderate",
                    "Risiko sedang: gagal %s%%, backlash %s%% HP maksimum.", failure, backlash);
            case SEVERE -> SoulLang.text(language, "tooltip.soulland.ring.risk_severe",
                    "Risiko berat: gagal %s%%, backlash %s%% HP maksimum.", failure, backlash);
        };
    }
}
