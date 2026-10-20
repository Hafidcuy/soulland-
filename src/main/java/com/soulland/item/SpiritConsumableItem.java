package com.soulland.item;

import com.soulland.cultivation.CultivationCapability;
import com.soulland.cultivation.CultivationManager;
import com.soulland.cultivation.Foundation;
import com.soulland.lang.SoulLang;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Consumable spirit: tiga herbal dan tiga pil.
 *
 * <p>Efek ditentukan server. Pill kultivasi memakai jalur XP yang sudah ada
 * ({@link CultivationManager#addXp}), bukan sistem XP baru. Pil Fondasi
 * menambah fondasi kultivasi sehingga upacara terobosan lebih mudah.
 */
public class SpiritConsumableItem extends LocalizedItem {
    /** Macam efek consumable. */
    public enum Effect {
        HEAL,
        REGENERATION,
        SPIRIT_POWER,
        CULTIVATION_XP,
        SWIFT,
        FOUNDATION
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
        String language = CultivationManager.messageLanguage(player);
        switch (effect) {
            case HEAL -> player.heal(amount);
            case REGENERATION -> addEffect(player, MobEffects.REGENERATION, 160, 1);
            case SPIRIT_POWER -> {
                CultivationCapability.get(player).ifPresent(cap ->
                        cap.setEnergy(cap.getEnergy() + amount));
                player.heal(2f);
            }
            case CULTIVATION_XP -> CultivationManager.addXp(player, amount);
            case SWIFT -> {
                addEffect(player, MobEffects.MOVEMENT_SPEED, 600, 1);
                addEffect(player, MobEffects.DAMAGE_BOOST, 600, 0);
            }
            case FOUNDATION -> {
                CultivationCapability.get(player).ifPresent(cap -> {
                    int before = cap.getFoundation();
                    cap.setFoundation(Foundation.gain(before, amount));
                    player.sendSystemMessage(SoulLang.text(language, "message.soulland.foundation_gain",
                            "§bFondasi kultivasi menguat: %s/100.", cap.getFoundation()));
                    if (cap.getFoundation() > before) {
                        CultivationManager.sync(player);
                    }
                });
            }
        }
        player.level().playSound(null, player.blockPosition(), SoundEvents.GENERIC_DRINK,
                SoundSource.PLAYERS, 0.8f, 1.2f);
        player.sendSystemMessage(SoulLang.text(language, "message.soulland.consumed",
                "Kamu menelan %s.", LocalizedItem.name(this, language).getString()));
    }

    private void addEffect(Player player, net.minecraft.world.effect.MobEffect mobEffect, int ticks, int amplifier) {
        player.addEffect(new MobEffectInstance(mobEffect, ticks, amplifier, false, true));
    }
}
