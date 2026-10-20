package com.soulland.cultivation;

import com.soulland.book.GuideBook;
import com.soulland.net.ModNetwork;
import com.soulland.net.OpenGuiPacket;
import com.soulland.soul.MartialSoul;
import com.soulland.soul.MartialSouls;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Upacara kebangkitan: undian martial soul.
 *
 * <p>Undian dipanggil dari beberapa pintu masuk (event join, event login, dan saat skill
 * ditekan) supaya pemain selalu punya soul meski urutan event berbeda antar versi Forge.
 * Set {@link #ROLLED} dilepas saat pemain keluar, jadi setiap kali masuk dunia soul diundi
 * ulang.
 */
public final class Awakening {
    private Awakening() {}

    private static final Set<UUID> ROLLED = ConcurrentHashMap.newKeySet();

    public static boolean roll(ServerPlayer player) {
        if (!ROLLED.add(player.getUUID())) return false;

        CultivationCapability.get(player).ifPresent(cap -> {
            boolean firstTime = !cap.isAwakened();
            MartialSoul previous = MartialSouls.get(cap.getMartialSoulId());
            MartialSoul soul = MartialSouls.roll(player.getRandom());
            boolean changed = previous == null || !previous.id().equals(soul.id());

            cap.setMartialSoulId(soul.id());
            cap.setAwakened(true);
            cap.setEvolutionStage(0);
            cap.setChainCount(0);
            if (firstTime) {
                cap.setLevel(1);
                cap.setEnergy(100f);
                cap.setRings(0);
                cap.setRingSkills(0);
            }
            if (changed) {
                cap.setLoadout(new String[]{"", "", "", "", ""});
            }

            if (firstTime) {
                player.addItem(GuideBook.create());
                player.sendSystemMessage(Component.literal("UPACARA KEBANGKITAN MARTIAL SOUL"));
            } else {
                com.soulland.SoulLandMod.LOGGER.info("Awakening: {} mendapat martial soul {}", player.getName().getString(), soul.name());
            player.sendSystemMessage(Component.literal("Undian martial soul: " + soul.name()
                        + (previous == null ? "" : " (sebelumnya " + previous.name() + ")")));
            }
            player.sendSystemMessage(Component.literal("Martial Soul-mu: " + soul.name() + " (" + soul.rarity() + ")"));
            player.sendSystemMessage(Component.literal("Pemilik asli: " + soul.owner() + " | Tipe: " + soul.type()
                    + " | Elemen: " + soul.element()));
            player.sendSystemMessage(Component.literal(soul.description()));
            player.sendSystemMessage(Component.literal("Tekan K untuk memunculkan martial soul, J untuk kartu soul, "
                    + "R G F V Z untuk skill, B untuk bakar darah."));
            player.sendSystemMessage(Component.literal("Absorpsi berani: tahan Shift saat klik kanan spirit ring."));
            player.serverLevel().sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                    player.getX(), player.getY() + 1.0, player.getZ(), 40, 0.6, 1.0, 0.6, 0.05);

            CultivationManager.applyStats(player);
            CultivationManager.sync(player);

            if (!cap.isWelcomeSeen()) {
                cap.setWelcomeSeen(true);
                ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                        new OpenGuiPacket(OpenGuiPacket.WELCOME));
            }
        });
        return true;
    }

    /** Dilepas saat pemain keluar supaya undian berikutnya terjadi lagi di sesi berikutnya. */
    public static void forget(ServerPlayer player) {
        ROLLED.remove(player.getUUID());
    }
}
