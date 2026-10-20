package com.soulland.soul;

import com.soulland.CultivationRanks;
import com.soulland.book.GuideBook;
import com.soulland.cultivation.CultivationCapability;
import com.soulland.cultivation.CultivationManager;
import com.soulland.lang.SoulLang;
import com.soulland.net.ModNetwork;
import com.soulland.net.OpenAwakeningGuiPacket;
import com.soulland.net.ChooseSoulResultPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server otoritatif atas Upacara Kebangkitan:.player memilih satu martial soul,
 * server memvalidasi lalu menyimpan takdir tersebut.
 */
public final class SoulSelection {
    private SoulSelection() {}

    /** Pemain yang sedang membuka GUI dalam mode pilih ulang (hanya mode Creative). */
    private static final Set<UUID> REROLL_SESSIONS = ConcurrentHashMap.newKeySet();

    /** Membuka layar upacara. reroll = true berarti pemain memilih ulang soul yang sudah ada. */
    public static void openCeremony(ServerPlayer player, boolean reroll) {
        if (reroll) {
            if (!player.isCreative()) {
                player.sendSystemMessage(Component.literal("§cPilih ulang martial soul hanya bisa dilakukan dalam mode Creative."));
                return;
            }
            REROLL_SESSIONS.add(player.getUUID());
        } else {
            REROLL_SESSIONS.remove(player.getUUID());
        }
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new OpenAwakeningGuiPacket(reroll));
    }

    /** Menutup sesi pilih ulang (pemain menutup GUI tanpa memilih). */
    public static void closeSession(ServerPlayer player) {
        REROLL_SESSIONS.remove(player.getUUID());
    }

    /** Menangani permintaan memilih soul dari client. Semua validasi terjadi di server. */
    public static void handleChoice(ServerPlayer player, String soulId) {
        MartialSoul soul = (soulId == null || soulId.isEmpty() || soulId.length() > 64) ? null : MartialSouls.get(soulId);
        if (soul == null) {
            reject(player, "gui.soulland.awakening.error.invalid_soul");
            return;
        }

        CultivationCapability.get(player).ifPresent(cap -> {
            boolean firstAwakening = !cap.isAwakened();
            boolean rerollAllowed = REROLL_SESSIONS.contains(player.getUUID()) && player.isCreative();
            if (!firstAwakening && !rerollAllowed) {
                reject(player, "gui.soulland.awakening.error.already_awakened");
                return;
            }

            String language = CultivationManager.messageLanguage(player);
            cap.setMartialSoulId(soul.id());
            cap.setAwakened(true);
            if (firstAwakening) {
                cap.setLevel(1);
                cap.setEnergy(CultivationRanks.MAX_ENERGY);
                player.addItem(GuideBook.create(language));
            }
            REROLL_SESSIONS.remove(player.getUUID());

            CultivationManager.applyStats(player);
            CultivationManager.sync(player);
            RingHaloSync.broadcast(player);

            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                    new ChooseSoulResultPacket(true, ""));

            if (firstAwakening) {
                player.sendSystemMessage(Component.literal("§6══════════════════════════════════"));
                player.sendSystemMessage(SoulLang.text(language, "message.soulland.awakening.title",
                        "§eUPACARA KEBANGKITAN MARTIAL SOUL"));
                player.sendSystemMessage(SoulLang.text(language, "message.soulland.awakening.soul",
                        "§fMartial Soul-mu: §b%s §7(%s)",
                        SoulText.soulName(soul, language).getString(),
                        SoulText.soulRarity(soul, language).getString()));
                player.sendSystemMessage(SoulLang.text(language, "message.soulland.awakening.owner",
                        "§7Pemilik asli: %s | Tipe: %s",
                        SoulText.soulOwner(soul, language).getString(),
                        SoulText.soulType(soul, language).getString()));
                player.sendSystemMessage(Component.literal("§7" + SoulText.soulDescription(soul, language).getString()));
                player.sendSystemMessage(SoulLang.text(language, "message.soulland.awakening.guide",
                        "§fBaca §a[Buku Panduan Dunia Soul Land]§f di inventory-mu untuk mempelajari segalanya."));
                player.sendSystemMessage(SoulLang.text(language, "message.soulland.awakening.hunt",
                        "§fBerburu soul beast, serap Spirit Ring, lalu kalahkan §5Ender Dragon§f!"));
                player.sendSystemMessage(Component.literal("§6══════════════════════════════════"));
            } else {
                player.sendSystemMessage(SoulLang.text(language, "message.soulland.awakening.changed",
                        "§bMartial soul-mu berubah menjadi: %s (%s)",
                        SoulText.soulName(soul, language).getString(),
                        SoulText.soulRarity(soul, language).getString()));
            }
        });
    }

    private static void reject(ServerPlayer player, String messageKey) {
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new ChooseSoulResultPacket(false, messageKey));
        player.sendSystemMessage(SoulLang.text(CultivationManager.messageLanguage(player), messageKey, messageKey));
    }
}
