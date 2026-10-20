package com.soulland.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.soulland.CultivationRanks;
import com.soulland.cultivation.CultivationCapability;
import com.soulland.cultivation.CultivationManager;
import com.soulland.soul.MartialSoul;
import com.soulland.soul.MartialSouls;
import com.soulland.soul.SoulSelection;
import com.soulland.soul.SoulText;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class SoulCommands {
    private SoulCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("soulinfo")
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    return showInfo(ctx.getSource(), player);
                }));

        dispatcher.register(Commands.literal("martialsoul")
                .then(Commands.literal("info")
                        .executes(ctx -> showSoul(ctx.getSource(), ctx.getSource().getPlayerOrException())))
                .then(Commands.literal("reroll")
                        .executes(ctx -> reroll(ctx.getSource(), ctx.getSource().getPlayerOrException()))));

        dispatcher.register(Commands.literal("cultivation")
                .requires(src -> src.hasPermission(2))
                .then(Commands.literal("set")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("level", IntegerArgumentType.integer(1, CultivationRanks.MAX_LEVEL))
                                        .executes(ctx -> {
                                            ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                            int level = IntegerArgumentType.getInteger(ctx, "level");
                                            CultivationCapability.get(target).ifPresent(cap -> cap.setLevel(level));
                                            CultivationManager.applyStats(target);
                                            CultivationManager.sync(target);
                                            ctx.getSource().sendSuccess(() ->
                                                    Component.literal("Level " + target.getName().getString() + " diatur ke " + level + "."), true);
                                            return 1;
                                        })))));
    }

    private static int showInfo(CommandSourceStack source, ServerPlayer player) {
        CultivationCapability.get(player).ifPresent(cap -> {
            MartialSoul soul = MartialSouls.get(cap.getMartialSoulId());
            source.sendSuccess(() -> Component.literal(
                    "§6=== Status Kultivasi ===\n" +
                    "§fMartial Soul: §b" + (soul == null ? "-" : soul.name()) + " §7(" + (soul == null ? "-" : soul.rarity()) + ")\n" +
                    "§fLevel Spirit Power: §a" + cap.getLevel() + " §7- " + CultivationRanks.rankName(cap.getLevel()) + "\n" +
                    "§fXP: " + cap.getXp() + " / " + CultivationRanks.xpForNextLevel(cap.getLevel()) + "\n" +
                    "§fSpirit Ring: §d" + cap.getRings() + " / 9\n" +
                    "§fSpirit Power (energi): §b" + (int) cap.getEnergy() + " / 100"), false);
        });
        return 1;
    }

    private static int showSoul(CommandSourceStack source, ServerPlayer player) {
        CultivationCapability.get(player).ifPresent(cap -> {
            MartialSoul soul = MartialSouls.get(cap.getMartialSoulId());
            if (soul == null) {
                source.sendFailure(Component.literal("Kamu belum membangkitkan martial soul."));
                return;
            }
            StringBuilder sb = new StringBuilder();
            sb.append("§6=== ").append(SoulText.soulName(soul).getString()).append(" ===\n");
            sb.append("§7Pemilik asli: ").append(soul.owner())
                    .append(" | Tipe: ").append(soul.type()).append(" | ").append(soul.rarity()).append("\n");
            sb.append("§f").append(SoulText.soulDescription(soul).getString()).append("\n");
            soul.skills().forEach(sk -> sb.append("§e- ").append(SoulText.skillName(sk).getString())
                    .append(" §7(").append(sk.ringsRequired()).append(" ring, energi ").append((int) sk.energyCost()).append("): ")
                    .append(SoulText.skillDescription(sk).getString()).append("\n"));
            source.sendSuccess(() -> Component.literal(sb.toString()), false);
        });
        return 1;
    }

    /** Pilih ulang martial soul (mode pick ulang), HANYA boleh dalam mode Creative. */
    private static int reroll(CommandSourceStack source, ServerPlayer player) {
        if (!player.isCreative()) {
            source.sendFailure(Component.literal("Martial soul adalah takdir sejak lahir! Pilih ulang hanya bisa dilakukan dalam mode Creative."));
            return 0;
        }
        SoulSelection.openCeremony(player, true);
        source.sendSuccess(() -> Component.literal("Upacara Kebangkitan dibuka. Pilih martial soul baru dari daftar."), true);
        return 1;
    }
}
