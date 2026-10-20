package com.soulland.item;

import com.soulland.net.ClientBridge;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Tool soul. Satu tool per soul, tiga mutu material.
 *
 * <p>Perilaku:
 * <ul>
 *   <li>Tahan klik kanan untuk mengisi charge, lepas untuk menyerang
 *       bermuatan (lihat {@link ToolCharge}).</li>
 *   <li>Tool milik soul sendiri memberi bonus skill (lihat
 *       {@code com.soulland.skill.ToolSynergy}).</li>
 *   <li>Tier Deep Silver menyimpan spirit power di dalam item.</li>
 * </ul>
 *
 * <p>Kecepatan tambang dan ketahanan mengikuti mutu material, jadi Mathf
 * {@code DiggerItem} sudah cukup: damage dan kecepatan serang dikirim lewat
 * constructor, sedangkan kecepatan tambang diambil dari tier.
 */
public class SpiritToolItem extends DiggerItem {
    private final SpiritToolTier tier;
    private final SpiritToolType type;

    public SpiritToolItem(SpiritToolTier tier, SpiritToolType type) {
        super(type.baseDamage(), type.attackSpeed() - 4.0f, tier, BlockTags.MINEABLE_WITH_PICKAXE,
                new Item.Properties());
        this.tier = tier;
        this.type = type;
    }

    public SpiritToolTier tier() {
        return tier;
    }

    public SpiritToolType type() {
        return type;
    }

    public String soulId() {
        return type.soulId();
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return type == SpiritToolType.BOW ? UseAnim.BOW : UseAnim.BLOCK;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 7200;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int timeLeft) {
        if (level.isClientSide) return;
        if (!(entity instanceof ServerPlayer player)) return;
        ToolCharge.tickCharge(player, stack);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (level.isClientSide) return;
        if (!(entity instanceof ServerPlayer player)) return;
        ToolCharge.releaseStrike(player, stack);
    }

    @Override
    public Component getName(ItemStack stack) {
        return LocalizedItem.name(this);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.soulland.soul", soulDisplayName())
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.soulland.tier", tierName())
                .withStyle(ChatFormatting.DARK_AQUA));
        tooltip.add(Component.translatable("tooltip.soulland.damage", (int) (1 + getAttackDamage()))
                .withStyle(ChatFormatting.RED));
        tooltip.add(Component.translatable("tooltip.soulland.mining", (int) tier.getSpeed())
                .withStyle(ChatFormatting.GRAY));

        if (tier == SpiritToolTier.DEEP_SILVER) {
            tooltip.add(Component.translatable("tooltip.soulland.battery",
                            ToolCharge.getBattery(stack), ToolCharge.BATTERY_MAX)
                    .withStyle(ChatFormatting.AQUA));
        } else {
            tooltip.add(Component.translatable("tooltip.soulland.charge_hint")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }

        boolean synergy = synergyActive();
        tooltip.add(Component.translatable(synergy
                        ? "tooltip.soulland.synergy_on"
                        : "tooltip.soulland.synergy_off")
                .withStyle(synergy ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY));
    }

    /** True bila martial soul pemain sama dengan soul pemilik tool ini. */
    private boolean synergyActive() {
        String soulId = ClientBridge.soulId();
        return !soulId.isEmpty() && soulId.equals(soulId());
    }

    private Component tierName() {
        return Component.translatable("tier.soulland." + tier.id());
    }

    private Component soulDisplayName() {
        return Component.translatable("soul.soulland." + soulId() + ".name");
    }
}
