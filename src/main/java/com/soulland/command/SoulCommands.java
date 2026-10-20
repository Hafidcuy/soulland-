package com.soulland.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.soulland.CultivationRanks;
import com.soulland.cultivation.CultivationCapability;
import com.soulland.cultivation.CultivationManager;
import com.soulland.entity.SoulCompanion;
import com.soulland.sect.Sect;
import com.soulland.soul.MartialSoul;
import com.soulland.soul.MartialSouls;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

import java.util.stream.Collectors;

public final class SoulCommands {
    private SoulCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("soulinfo")
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    return showInfo(ctx.getSource(), player);
                }));

        dispatcher.register(Commands.literal("panduan")
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    com.soulland.net.ModNetwork.CHANNEL.send(
                            net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player),
                            new com.soulland.net.OpenGuiPacket(com.soulland.net.OpenGuiPacket.GUIDE));
                    return 1;
                }));

        dispatcher.register(Commands.literal("martialsoul")
                .then(Commands.literal("info")
                        .executes(ctx -> showSoul(ctx.getSource(), ctx.getSource().getPlayerOrException())))
                .then(Commands.literal("reroll")
                        .executes(ctx -> reroll(ctx.getSource(), ctx.getSource().getPlayerOrException())))
                .then(Commands.literal("summon")
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            SoulCompanion.toggle(player);
                            return 1;
                        })));

        dispatcher.register(Commands.literal("sect")
                .then(Commands.literal("info")
                        .executes(ctx -> showSect(ctx.getSource(), ctx.getSource().getPlayerOrException())))
                .then(Commands.literal("choose")
                        .then(Commands.argument("sect", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    String current = builder.getRemaining().toLowerCase();
                                    for (Sect sect : Sect.all()) {
                                        if (sect.id().startsWith(current)) {
                                            builder.suggest(sect.id());
                                        }
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(ctx -> chooseSect(ctx.getSource(), ctx.getSource().getPlayerOrException(),
                                        StringArgumentType.getString(ctx, "sect"))))));

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
            MartialSoul soul = MartialSouls.effective(cap.getMartialSoulId(), cap.getEvolutionStage());
            Sect sect = Sect.get(cap.getSect());
            source.sendSuccess(() -> Component.literal(
                    "Status Kultivasi\n" +
                            "Martial Soul: " + (soul == null ? "-" : soul.name()) + " (" + (soul == null ? "-" : soul.rarity()) + ")\n" +
                            "Level Spirit Power: " + cap.getLevel() + " - " + CultivationRanks.rankName(cap.getLevel()) + "\n" +
                            "XP: " + cap.getXp() + " / " + CultivationRanks.xpForNextLevel(cap.getLevel()) + "\n" +
                            "Spirit Ring: " + cap.getRings() + " / " + CultivationRanks.MAX_RINGS + "\n" +
                            "Spirit Power: " + (int) cap.getEnergy() + " / " + (int) CultivationRanks.MAX_ENERGY + "\n" +
                            "Sekte: " + (sect == null ? "belum memilih" : sect.name())), false);
        });
        return 1;
    }

    private static int showSoul(CommandSourceStack source, ServerPlayer player) {
        CultivationCapability.get(player).ifPresent(cap -> {
            MartialSoul base = MartialSouls.get(cap.getMartialSoulId());
            MartialSoul soul = MartialSouls.effective(cap.getMartialSoulId(), cap.getEvolutionStage());
            if (soul == null) {
                source.sendFailure(Component.literal("Kamu belum membangkitkan martial soul."));
                return;
            }
            StringBuilder sb = new StringBuilder();
            sb.append(soul.name()).append("\n");
            sb.append("Pemilik asli: ").append(soul.owner()).append(" | Tipe: ").append(soul.type())
                    .append(" | ").append(soul.rarity()).append(" | Elemen: ").append(soul.element()).append("\n");
            sb.append(soul.description()).append("\n");
            if (cap.getEvolutionStage() > 0 && base != null) {
                sb.append("Bentuk dasar: ").append(base.name()).append("\n");
            }
            soul.skills().forEach(sk -> sb.append("- ").append(sk.name())
                    .append(" (butuh ").append(sk.ringsRequired()).append(" ring, energi ").append((int) sk.energyCost())
                    .append(", ").append(sk.cooldownTicks() / 20).append(" dtk): ").append(sk.description()).append("\n"));
            source.sendSuccess(() -> Component.literal(sb.toString()), false);
        });
        return 1;
    }

    private static int showSect(CommandSourceStack source, ServerPlayer player) {
        CultivationCapability.get(player).ifPresent(cap -> {
            Sect current = Sect.get(cap.getSect());
            if (current == null) {
                source.sendSuccess(() -> Component.literal(
                        "Kamu belum memilih sekte. Pilih dengan /sect choose <nama> atau tekan J lalu tab SECT."), false);
                return;
            }
            source.sendSuccess(() -> Component.literal(current.name() + "\n" + current.description()), false);
        });
        return 1;
    }

    private static int chooseSect(CommandSourceStack source, ServerPlayer player, String sectId) {
        Sect sect = Sect.get(sectId.toLowerCase());
        if (sect == null) {
            source.sendFailure(Component.literal("Sekte tidak dikenal. Pilihan: "
                    + Sect.all().stream().map(Sect::id).collect(Collectors.joining(", "))));
            return 0;
        }
        CultivationCapability.get(player).ifPresent(cap -> {
            cap.setSect(sect.id());
            CultivationManager.applyStats(player);
            CultivationManager.sync(player);
            source.sendSuccess(() -> Component.literal("Kamu bergabung dengan " + sect.name() + "."), true);
        });
        return 1;
    }

    /** Undi ulang martial soul - HANYA boleh dalam mode Creative, sesuai desain dunia. */
    private static int reroll(CommandSourceStack source, ServerPlayer player) {
        if (!player.isCreative()) {
            source.sendFailure(Component.literal("Martial soul adalah takdir sejak lahir. Undi ulang hanya bisa dilakukan dalam mode Creative."));
            return 0;
        }
        CultivationCapability.get(player).ifPresent(cap -> {
            MartialSoul soul = MartialSouls.roll(player.getRandom());
            cap.setMartialSoulId(soul.id());
            cap.setAwakened(true);
            cap.setEvolutionStage(0);
            CultivationManager.sync(player);
            source.sendSuccess(() -> Component.literal("Martial soul-mu berubah menjadi: " + soul.name() + " (" + soul.rarity() + ")"), true);
        });
        return 1;
    }
}
