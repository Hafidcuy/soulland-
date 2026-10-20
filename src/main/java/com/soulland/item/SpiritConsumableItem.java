package com.soulland.item;

import com.soulland.cultivation.CultivationManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Consumable spirit: tiga herbal dan dua pil.
 *
 * <p>Efek ditentukan server. Pill kultivasi memakai jalur XP yang sudah ada
 * ({@link CultivationManager#addXp}), bukan sistem XP baru.
 */
public class SpiritConsumableItem extends Item {
    /** Macam efek consumable. */
    public enum Effect {
        HEAL,
        REGENERATION,
        SPIRIT_POWER,
        CULTIVATION_XP,
        SWIFT
    }

    private final Effect effect;
    private final int amount;

    public SpiritConsumableItem(Effect effect, int amount, Properties properties) {
        super(properties);
        this.effect = effect;
        this.amount = amount;
    }

    public Effect effect() {
        return effect;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.pass(stack);
        }

        apply(serverPlayer);
        if (!player.isCreative()) {
            stack.shrink(1);
        }
        return InteractionResultHolder.consume(stack);
    }

    private void apply(ServerPlayer player) {
        switch (effect) {
            case HEAL -> player.heal(amount);
            case REGENERATION -> addEffect(player, MobEffects.REGENERATION, 160, 1);
            case SPIRIT_POWER -> {
                com.soulland.cultivation.CultivationCapability.get(player).ifPresent(cap ->
                        cap.setEnergy(cap.getEnergy() + amount));
                player.heal(2f);
            }
            case CULTIVATION_XP -> CultivationManager.addXp(player, amount);
            case SWIFT -> {
                addEffect(player, MobEffects.MOVEMENT_SPEED, 600, 1);
                addEffect(player, MobEffects.DAMAGE_BOOST, 600, 0);
            }
        }
        player.level().playSound(null, player.blockPosition(), SoundEvents.GENERIC_DRINK,
                SoundSource.PLAYERS, 0.8f, 1.2f);
        player.sendSystemMessage(Component.translatable("message.soulland.consumed",
                Component.translatable(getDescriptionId())));
    }

    private void addEffect(Player player, net.minecraft.world.effect.MobEffect mobEffect, int ticks, int amplifier) {
        player.addEffect(new MobEffectInstance(mobEffect, ticks, amplifier, false, true));
    }
}
