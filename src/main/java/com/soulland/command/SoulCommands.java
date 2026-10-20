package com.soulland.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.soulland.CultivationRanks;
import com.soulland.cultivation.CultivationCapability;
import com.soulland.cultivation.CultivationManager;
import com.soulland.cultivation.Foundation;
import com.soulland.entity.SoulCompanion;
import com.soulland.lang.SoulLang;
import com.soulland.net.ModNetwork;
import com.soulland.net.OpenGuiPacket;
import com.soulland.net.OpenLanguageScreenPacket;
import com.soulland.sect.Sect;
import com.soulland.soul.MartialSoul;
import com.soulland.soul.MartialSouls;
import com.soulland.soul.SoulSelection;
import com.soulland.soul.SoulText;
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
                    ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                            new OpenGuiPacket(OpenGuiPacket.GUIDE));
                    return 1;
                }));

        dispatcher.register(Commands.literal("soul")
                .then(Commands.literal("language")
                        .executes(ctx -> openLanguage(ctx.getSource(), ctx.getSource().getPlayerOrException())))
                .then(Commands.literal("info")
                        .executes(ctx -> showInfo(ctx.getSource(), ctx.getSource().getPlayerOrException()))));

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
        String language = CultivationManager.messageLanguage(player);
        CultivationCapability.get(player).ifPresent(cap -> {
            MartialSoul soul = MartialSouls.effective(cap.getMartialSoulId(), cap.getEvolutionStage());
            Sect sect = Sect.get(cap.getSect());
            StringBuilder sb = new StringBuilder();
            sb.append(SoulLang.text(language, "message.soulland.info.header", "Status Kultivasi")).append('\n');
            sb.append(SoulLang.text(language, "message.soulland.info.soul", "Martial Soul: %s (%s)",
                    soul == null ? "-" : SoulText.soulName(soul, language).getString(),
                    soul == null ? "-" : SoulText.soulRarity(soul, language).getString())).append('\n');
            sb.append(SoulLang.text(language, "message.soulland.info.level", "Level Spirit Power: %s - %s",
                    cap.getLevel(), CultivationRanks.rankName(cap.getLevel()))).append('\n');
            sb.append(SoulLang.text(language, "message.soulland.info.xp", "XP: %s / %s",
                    cap.getXp(), CultivationRanks.xpForNextLevel(cap.getLevel()))).append('\n');
            sb.append(SoulLang.text(language, "message.soulland.info.foundation", "Fondasi: %s / %s (peluang terobosan %s%%)",
                    cap.getFoundation(), Foundation.MAX,
                    Foundation.successChancePercent(cap.getFoundation()))).append('\n');
            sb.append(SoulLang.text(language, "message.soulland.info.rings", "Spirit Ring: %s / %s",
                    cap.getRings(), CultivationRanks.MAX_RINGS)).append('\n');
            sb.append(SoulLang.text(language, "message.soulland.info.mutations", "Ring bermutasi: %s", cap.getMutationCount())).append('\n');
            sb.append(SoulLang.text(language, "message.soulland.info.energy", "Spirit Power: %s / %s",
                    (int) cap.getEnergy(), (int) CultivationRanks.MAX_ENERGY)).append('\n');
            sb.append(SoulLang.text(language, "message.soulland.info.sect", "Sekte: %s",
                    sect == null ? SoulLang.translate(language, "message.soulland.sect.none", "belum memilih")
                            : sect.name()));
            source.sendSuccess(() -> Component.literal(sb.toString()), false);
        });
        return 1;
    }

    private static int showSoul(CommandSourceStack source, ServerPlayer player) {
        String language = CultivationManager.messageLanguage(player);
        CultivationCapability.get(player).ifPresent(cap -> {
            MartialSoul soul = MartialSouls.effective(cap.getMartialSoulId(), cap.getEvolutionStage());
            if (soul == null) {
                source.sendFailure(SoulLang.text(language, "message.soulland.soul.none",
                        "Kamu belum membangkitkan martial soul."));
                return;
            }
            StringBuilder sb = new StringBuilder();
            sb.append("§6=== ").append(SoulText.soulName(soul, language).getString()).append(" ===\n");
            sb.append(SoulLang.text(language, "message.soulland.soul.owner", "§7Pemilik asli: %s | Tipe: %s | %s",
                    SoulText.soulOwner(soul, language).getString(),
                    SoulText.soulType(soul, language).getString(),
                    SoulText.soulRarity(soul, language).getString())).append('\n');
            sb.append("§f").append(SoulText.soulDescription(soul, language).getString()).append('\n');
            soul.skills().forEach(sk -> sb.append("§e- ").append(SoulText.skillName(sk, language).getString())
                    .append(" §7(").append(sk.ringsRequired()).append(" ring, energi ").append((int) sk.energyCost()).append("): ")
                    .append(SoulText.skillDescription(sk, language).getString()).append('\n'));
            source.sendSuccess(() -> Component.literal(sb.toString()), false);
        });
        return 1;
    }

    private static int showSect(CommandSourceStack source, ServerPlayer player) {
        String language = CultivationManager.messageLanguage(player);
        CultivationCapability.get(player).ifPresent(cap -> {
            Sect current = Sect.get(cap.getSect());
            if (current == null) {
                source.sendSuccess(() -> SoulLang.text(language, "message.soulland.sect.none_choice",
                        "Kamu belum memilih sekte. Pilih dengan /sect choose <nama> atau tekan J lalu tab SECT."), false);
                return;
            }
            source.sendSuccess(() -> Component.literal(current.name() + "\n" + current.description()), false);
        });
        return 1;
    }

    private static int chooseSect(CommandSourceStack source, ServerPlayer player, String sectId) {
        String language = CultivationManager.messageLanguage(player);
        Sect sect = Sect.get(sectId.toLowerCase());
        if (sect == null) {
            source.sendFailure(SoulLang.text(language, "message.soulland.sect.unknown", "Sekte tidak dikenal. Pilihan: %s",
                    Sect.all().stream().map(Sect::id).collect(Collectors.joining(", "))));
            return 0;
        }
        CultivationCapability.get(player).ifPresent(cap -> {
            cap.setSect(sect.id());
            CultivationManager.applyStats(player);
            CultivationManager.sync(player);
            source.sendSuccess(() -> SoulLang.text(language, "message.soulland.sect.chosen", "Kamu bergabung dengan %s.",
                    sect.name()), true);
        });
        return 1;
    }

    /** Membuka layar Pilih Bahasa kapan pun; pilihan langsung berlaku dan tersimpan. */
    private static int openLanguage(CommandSourceStack source, ServerPlayer player) {
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new OpenLanguageScreenPacket(false));
        source.sendSuccess(() -> SoulLang.text(CultivationManager.messageLanguage(player),
                "message.soulland.language.opened", "Layar pilih bahasa dibuka."), true);
        return 1;
    }

    /** Pilih ulang martial soul (mode pick ulang), HANYA boleh dalam mode Creative. */
    private static int reroll(CommandSourceStack source, ServerPlayer player) {
        String language = CultivationManager.messageLanguage(player);
        if (!player.isCreative()) {
            source.sendFailure(SoulLang.text(language, "message.soulland.soul.reroll_blocked",
                    "Martial soul adalah takdir sejak lahir! Pilih ulang hanya bisa dilakukan dalam mode Creative."));
            return 0;
        }
        SoulSelection.openCeremony(player, true);
        source.sendSuccess(() -> SoulLang.text(language, "message.soulland.soul.reroll_opened",
                "Upacara Kebangkitan dibuka. Pilih martial soul baru dari daftar."), true);
        return 1;
    }
}
