package com.soulland.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.soulland.CultivationRanks;
import com.soulland.bone.SpiritBoneManager;
import com.soulland.bone.SpiritBoneSlot;
import com.soulland.bone.SpiritBoneTier;
import com.soulland.cultivation.CultivationCapability;
import com.soulland.cultivation.CultivationManager;
import com.soulland.cultivation.Foundation;
import com.soulland.entity.SoulCompanion;
import com.soulland.lang.SoulLang;
import com.soulland.net.ModNetwork;
import com.soulland.net.OpenBoneScreenPacket;
import com.soulland.net.OpenGuiPacket;
import com.soulland.net.OpenLanguageScreenPacket;
import com.soulland.sect.Sect;
import com.soulland.soul.MartialSoul;
import com.soulland.soul.MartialSouls;
import com.soulland.soul.SoulSelection;
import com.soulland.soul.SoulText;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.PacketDistributor;

import java.util.concurrent.CompletableFuture;
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
                        .executes(ctx -> showInfo(ctx.getSource(), ctx.getSource().getPlayerOrException())))
                .then(boneCommand()));

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

    /**
     * Subtree command {@code /soul bone}: buka layar Boutique Tulang Spirit,
     * plus {@code give} untuk QA supaya semua kombinasi bisa diuji tanpa
     * harus berburu boss.
     */
    private static LiteralArgumentBuilder<CommandSourceStack> boneCommand() {
        return Commands.literal("bone")
                .executes(ctx -> openBoneScreen(ctx.getSource(), ctx.getSource().getPlayerOrException()))
                .then(Commands.literal("give")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("tier", StringArgumentType.word())
                                        .suggests((ctx, builder) -> suggestTiers(builder))
                                        .then(Commands.argument("slot", StringArgumentType.word())
                                                .suggests((ctx, builder) -> suggestSlots(builder))
                                                .executes(ctx -> giveBone(ctx))))));
    }

    /** Membuka layar Boutique Tulang Spirit untuk pemain. */
    private static int openBoneScreen(CommandSourceStack source, ServerPlayer player) {
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new OpenBoneScreenPacket());
        source.sendSuccess(() -> SoulLang.text(CultivationManager.messageLanguage(player),
                "message.soulland.bone.screen_opened", "Layar Tulang Spirit dibuka."), true);
        return 1;
    }

    /** Saran nama mutu tulang untuk argumen command. */
    private static CompletableFuture<Suggestions> suggestTiers(SuggestionsBuilder builder) {
        String current = builder.getRemaining().toLowerCase(java.util.Locale.ROOT);
        for (SpiritBoneTier tier : SpiritBoneTier.values()) {
            if (tier.id().startsWith(current)) builder.suggest(tier.id());
        }
        return builder.buildFuture();
    }

    /** Saran nama slot tubuh untuk argumen command. */
    private static CompletableFuture<Suggestions> suggestSlots(SuggestionsBuilder builder) {
        String current = builder.getRemaining().toLowerCase(java.util.Locale.ROOT);
        for (SpiritBoneSlot slot : SpiritBoneSlot.values()) {
            if (slot.id().startsWith(current)) builder.suggest(slot.id());
        }
        return builder.buildFuture();
    }

    /** Memberi satu item tulang spirit ke pemain, dipakai untuk pengujian. */
    private static int giveBone(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
        String tierId = StringArgumentType.getString(ctx, "tier").toLowerCase(java.util.Locale.ROOT);
        String slotId = StringArgumentType.getString(ctx, "slot").toLowerCase(java.util.Locale.ROOT);
        SpiritBoneTier tier = SpiritBoneTier.byId(tierId);
        if (tier == null) {
            ctx.getSource().sendFailure(Component.literal("Mutu tulang tidak dikenal: " + tierId));
            return 0;
        }
        SpiritBoneSlot slot = SpiritBoneSlot.byId(slotId);
        if (slot == null) {
            ctx.getSource().sendFailure(Component.literal("Slot tubuh tidak dikenal: " + slotId));
            return 0;
        }
        ItemStack stack = SpiritBoneManager.stack(tier, slot);
        if (stack.isEmpty()) {
            ctx.getSource().sendFailure(Component.literal("Item tulang tidak terdaftar."));
            return 0;
        }
        target.getInventory().add(stack);
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Tulang " + tierId + " " + slotId + " diberikan ke " + target.getName().getString()), true);
        return 1;
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
