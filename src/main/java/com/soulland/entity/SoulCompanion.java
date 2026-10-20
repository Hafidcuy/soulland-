package com.soulland.entity;

import com.soulland.cultivation.CultivationCapability;
import com.soulland.soul.MartialSoul;
import com.soulland.soul.MartialSouls;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

/**
 * Pengelola companion martial soul: memunculkan, menyembunyikan, dan memberi aba-aba
 * echo saat pemilik memakai skill.
 */
public final class SoulCompanion {
    private SoulCompanion() {}

    public static MartialSoulEntity find(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        List<MartialSoulEntity> found = level.getEntitiesOfClass(MartialSoulEntity.class,
                player.getBoundingBox().inflate(48.0),
                e -> e.getOwnerId().equals(player.getUUID()));
        return found.isEmpty() ? null : found.get(0);
    }

    /** Munculkan companion kalau belum ada, hilangkan kalau sudah ada. */
    public static void toggle(ServerPlayer player) {
        MartialSoulEntity existing = find(player);
        if (existing != null) {
            existing.discard();
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("Martial soul disembunyikan."));
            return;
        }
        spawn(player);
    }

    public static boolean spawn(ServerPlayer player) {
        MartialSoul soul = soulOf(player);
        if (soul == null) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "Belum ada martial soul untuk dimunculkan."));
            return false;
        }
        MartialSoulEntity entity = new MartialSoulEntity(ModEntities.MARTIAL_SOUL.get(), player.serverLevel());
        entity.setOwner(player.getUUID());
        entity.setSoulId(soul.id());
        entity.setTint(soul.color());
        entity.setPos(player.getX(), player.getY() + 1.5, player.getZ());
        player.serverLevel().addFreshEntity(entity);
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "Martial soul \"" + soul.name() + "\" muncul di belakangmu."));
        return true;
    }

    /** Companion maju sekejap saat skill dilepas. */
    public static void echo(ServerPlayer player) {
        MartialSoulEntity entity = find(player);
        if (entity != null) {
            entity.echo();
        }
    }

    private static MartialSoul soulOf(ServerPlayer player) {
        return CultivationCapability.get(player)
                .map(cap -> MartialSouls.effective(cap.getMartialSoulId(), cap.getEvolutionStage()))
                .orElse(null);
    }

    /** Hapus companion yang tertinggal ketika pemain keluar dunia. */
    public static void removeAll(ServerPlayer player) {
        MartialSoulEntity entity = find(player);
        if (entity != null) {
            entity.discard();
        }
    }
}
