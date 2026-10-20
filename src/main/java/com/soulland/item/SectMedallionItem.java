package com.soulland.item;

import com.soulland.net.ChooseSectPacket;
import com.soulland.net.ModNetwork;
import com.soulland.sect.Sect;
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

/** Lencana sekte. Sekali klik kanan, pemain langsung bergabung dengan sekte itu. */
public class SectMedallionItem extends Item {

    private final String sectId;

    public SectMedallionItem(Properties properties, String sectId) {
        super(properties);
        this.sectId = sectId;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        Sect sect = Sect.get(sectId);
        if (sect == null) {
            return InteractionResultHolder.fail(stack);
        }
        if (level.isClientSide) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }
        ModNetwork.CHANNEL.sendToServer(new ChooseSectPacket(sect.id()));
        player.sendSystemMessage(Component.literal("Lencana " + sect.name() + " bersinar. Memakai lencana ini.")
                .withStyle(ChatFormatting.GOLD));
        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        Sect sect = Sect.get(sectId);
        if (sect == null) return;
        tooltip.add(Component.literal(sect.name()).withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal(sect.description()).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Klik kanan untuk bergabung dengan sekte ini.").withStyle(ChatFormatting.DARK_GRAY));
    }
}
