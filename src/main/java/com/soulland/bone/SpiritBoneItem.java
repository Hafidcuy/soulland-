package com.soulland.bone;

import com.soulland.item.LocalizedItem;
import com.soulland.lang.SoulLang;
import com.soulland.net.ClientBridge;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Item tulang spirit.
 *
 * <p>Sekali klik kanan, tulang langsung dipasang ke slotnya. Server yang
 * memeriksa: tier yang melampaui kapasitas tubuh pemain ditolak, dan tulang
 * lama di slot itu kembali ke tas.
 */
public class SpiritBoneItem extends LocalizedItem {

    private final SpiritBoneTier tier;
    private final SpiritBoneSlot slot;

    public SpiritBoneItem(SpiritBoneTier tier, SpiritBoneSlot slot, Properties properties) {
        super(properties);
        this.tier = tier;
        this.slot = slot;
    }

    /** Mutu tulang ini. */
    public SpiritBoneTier tier() {
        return tier;
    }

    /** Slot tubuh tempat tulang ini dipasang. */
    public SpiritBoneSlot slot() {
        return slot;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.pass(stack);
        }

        SpiritBoneManager.Status status = SpiritBoneManager.equip(serverPlayer, slot, itemId(), stack);
        if (!status.text().isEmpty()) {
            serverPlayer.sendSystemMessage(Component.literal(status.text()));
        }
        return InteractionResultHolder.success(stack);
    }

    /** Id registry item ini, dipakai server saat memasang dan client saat menyinkronkan. */
    public String itemId() {
        return net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(this).getPath();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        String language = SoulLang.activeLanguage();
        BoneStats stats = BoneStats.of(tier, slot);

        tooltip.add(SoulLang.text(language, "tooltip.soulland.bone.tier",
                "Tulang %s (%s tahun)", tierName(language), tier.years())
                .withStyle(ChatFormatting.AQUA));
        tooltip.add(SoulLang.text(language, "tooltip.soulland.bone.slot",
                "Slot: %s", slotName(language)).withStyle(ChatFormatting.GRAY));
        tooltip.add(SoulLang.text(language, "tooltip.soulland.bone.attack",
                "+%s serangan", format(stats.attack())).withStyle(ChatFormatting.GOLD));
        tooltip.add(SoulLang.text(language, "tooltip.soulland.bone.defense",
                "+%s defense", format(stats.defense())).withStyle(ChatFormatting.BLUE));
        tooltip.add(SoulLang.text(language, "tooltip.soulland.bone.energy",
                "+%s spirit power maksimum", (int) stats.energy()).withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(SoulLang.text(language, "tooltip.soulland.bone.regen",
                "+%s HP per detik", format(stats.regen())).withStyle(ChatFormatting.GREEN));

        int rings = ClientBridge.rings();
        if (tier.requiredRings() > 0) {
            tooltip.add(SoulLang.text(language, "tooltip.soulland.bone.need_rings",
                            "Butuh %s spirit ring (kamu punya %s)", tier.requiredRings(), rings)
                    .withStyle(rings >= tier.requiredRings() ? ChatFormatting.GRAY : ChatFormatting.DARK_RED));
        }
        tooltip.add(SoulLang.text(language, "tooltip.soulland.bone.use",
                "Klik kanan untuk memasang ke slotnya.").withStyle(ChatFormatting.GRAY));
    }

    private String tierName(String language) {
        return SoulLang.translate(language, "bone.soulland.tier." + tier.id(), tier.id());
    }

    private String slotName(String language) {
        return SoulLang.translate(language, "bone.soulland.slot." + slot.id(), slot.id());
    }

    /** Angka bonus dibulatkan supaya tooltip tidak penuh pecahan. */
    private static String format(float value) {
        if (value == (long) value) return String.valueOf((long) value);
        return String.format(java.util.Locale.ROOT, "%.2f", value);
    }
}
