package com.soulland.bone;

import com.soulland.config.BoneConfig;
import com.soulland.cultivation.CultivationCapability;
import com.soulland.cultivation.ICultivation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;

import java.util.Collection;
import java.util.Random;

/**
 * Drop tulang spirit dari soul beast.
 *
 * <p>Soul beast kustom baru datang di v1.6, jadi sementara ini sumbernya
 * boss vanilla yang sudah diperkuat mod ini. Boss memberi peluang besar,
 * mob biasa memberi peluang kecil yang ikut naik seiring jumlah spirit ring pemain.
 * Tulang Eksternal hanya keluar dari boss, dan peluangnya paling kecil.
 */public final class BoneDrops {
    private BoneDrops() {}

    /** Sumber drop yang bisa menjatuhkan tulang. */
    public enum Source {
        DRAGON,
        WITHER,
        WARDEN,
        GUARDIAN,
        HOSTILE
    }

    /** Sumber drop untuk sebuah makhluk, atau null kalau bukan sumber tulang. */
    public static Source sourceOf(LivingEntity entity) {
        if (entity instanceof EnderDragon) return Source.DRAGON;
        if (entity instanceof WitherBoss) return Source.WITHER;
        if (entity instanceof Warden) return Source.WARDEN;
        if (entity instanceof ElderGuardian) return Source.GUARDIAN;
        if (entity instanceof Monster) return Source.HOSTILE;
        return null;
    }

    /**
     * Melempar undian drop untuk satu kematian.
     *
     * @param drops koleksi drop yang akan ditambah hasil undian
     */
    public static void roll(LivingEntity entity, ServerPlayer player, Collection<ItemEntity> drops) {
        Source source = sourceOf(entity);
        if (source == null) return;

        Random random = asJavaRandom(entity.getRandom());
        int rings = CultivationCapability.get(player).map(ICultivation::getRings).orElse(0);

        if (source == Source.HOSTILE) {
            double chance = BoneDropLogic.hostileChance(
                    BoneConfig.hostileBaseChance(),
                    BoneConfig.hostileRingScale(),
                    BoneConfig.hostileMaxChance(),
                    rings);
            if (!BoneDropLogic.roll(chance, random)) return;
            SpiritBoneTier tier = BoneDropLogic.pickTier(BoneConfig.hostileWeights(), random);
            spawn(entity, tier, BoneDropLogic.pickSlot(random), drops);
            return;
        }

        if (BoneDropLogic.roll(BoneDropLogic.bossChance(bossChance(source)), random)) {
            SpiritBoneTier tier = BoneDropLogic.pickTier(BoneConfig.bossWeights(), random);
            spawn(entity, tier, BoneDropLogic.pickSlot(random), drops);
        }

        if (BoneDropLogic.roll(BoneDropLogic.bossChance(externalChance(source)), random)) {
            SpiritBoneTier tier = BoneDropLogic.pickTier(BoneConfig.bossWeights(), random);
            spawn(entity, tier, SpiritBoneSlot.EXTERNAL, drops);
        }
    }

    /**
     * Membungkus undian Minecraft jadi {@link Random} biasa.
     *
     * <p>Logika peluang tetap murni Java supaya bisa diuji tanpa runtime game.
     */
    private static Random asJavaRandom(RandomSource source) {
        return new Random() {
            @Override
            public int nextInt(int bound) {
                return source.nextInt(bound);
            }

            @Override
            public double nextDouble() {
                return source.nextDouble();
            }
        };
    }

    /** Peluang boss menjatuhkan tulang biasa, dari config. */
    public static double bossChance(Source source) {
        return switch (source) {
            case DRAGON -> BoneConfig.DRAGON_CHANCE.get();
            case WITHER -> BoneConfig.WITHER_CHANCE.get();
            case WARDEN -> BoneConfig.WARDEN_CHANCE.get();
            case GUARDIAN -> BoneConfig.GUARDIAN_CHANCE.get();
            case HOSTILE -> 0.0;
        };
    }

    /** Peluang boss menjatuhkan Tulang Eksternal, dari config. */
    public static double externalChance(Source source) {
        return switch (source) {
            case DRAGON -> BoneConfig.EXTERNAL_DRAGON_CHANCE.get();
            case WITHER -> BoneConfig.EXTERNAL_WITHER_CHANCE.get();
            case WARDEN -> BoneConfig.EXTERNAL_WARDEN_CHANCE.get();
            case GUARDIAN -> BoneConfig.EXTERNAL_GUARDIAN_CHANCE.get();
            case HOSTILE -> 0.0;
        };
    }

    private static void spawn(LivingEntity entity, SpiritBoneTier tier, SpiritBoneSlot slot,
                              Collection<ItemEntity> drops) {
        ItemStack stack = SpiritBoneManager.stack(tier, slot);
        if (stack.isEmpty()) return;
        ItemEntity drop = new ItemEntity(entity.level(), entity.getX(), entity.getY() + 0.5, entity.getZ(), stack);
        drop.setDefaultPickUpDelay();
        drops.add(drop);
    }
}
