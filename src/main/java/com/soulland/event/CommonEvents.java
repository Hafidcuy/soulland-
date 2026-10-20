package com.soulland.event;

import com.soulland.SoulLandMod;
import com.soulland.cultivation.CultivationCapability;
import com.soulland.cultivation.CultivationManager;
import com.soulland.item.ModItems;
import com.soulland.skill.AttackEffects;
import com.soulland.soul.RingHaloSync;
import com.soulland.soul.SoulSelection;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SoulLandMod.MODID)
public class CommonEvents {

    /** Login pertama: buka Upacara Kebangkitan. Martial soul dipilih pemain sendiri, bukan diundi. */
    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        CultivationCapability.get(player).ifPresent(cap -> {
            if (!cap.isAwakened()) {
                SoulSelection.openCeremony(player, false);
            }
            CultivationManager.applyStats(player);
            CultivationManager.sync(player);
        });
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            CultivationManager.applyStats(player);
            CultivationManager.sync(player);
            RingHaloSync.broadcast(player);
        }
    }

    @SubscribeEvent
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            CultivationManager.sync(player);
            RingHaloSync.broadcast(player);
        }
    }

    /** Data kultivasi ikut saat pemain mati/respawn (clone). */
    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        event.getOriginal().reviveCaps();
        event.getOriginal().getCapability(CultivationCapability.CAP).ifPresent(oldCap ->
                event.getEntity().getCapability(CultivationCapability.CAP).ifPresent(newCap ->
                        newCap.deserializeNBT(oldCap.serializeNBT())));
        event.getOriginal().invalidateCaps();
    }

    /** Regenerasi spirit energy + jaga sinkronisasi ringan. */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;
        CultivationCapability.get(event.player).ifPresent(cap -> {
            if (cap.getEnergy() < 100f) {
                cap.setEnergy(cap.getEnergy() + 0.15f); // ~3 energi/detik
                if (event.player.tickCount % 20 == 0 && event.player instanceof ServerPlayer sp) {
                    CultivationManager.sync(sp);
                }
            }
        });
    }

    /** Bonus damage keluaran berdasarkan level kultivasi, plus efek serangan khas tiap soul. */
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getSource().getEntity() instanceof ServerPlayer player) {
            CultivationCapability.get(player).ifPresent(cap ->
                    event.setAmount(event.getAmount() * (1f + (cap.getLevel() - 1) * 0.01f + cap.getRings() * 0.05f)));
        }
        AttackEffects.onLivingHurt(event);
    }

    /** Membunuh soul beast (monster) memberi XP kultivasi. */
    @SubscribeEvent
    public static void onKill(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide) return;
        if (event.getSource().getEntity() instanceof ServerPlayer player && event.getEntity() instanceof Monster) {
            int xp = Math.max(5, (int) (event.getEntity().getMaxHealth() / 2));
            CultivationManager.addXp(player, xp);
        }
    }

    /** Soul beast menjatuhkan spirit ring; boss menjatuhkan ring langka. Ender Dragon = boss akhir. */
    @SubscribeEvent
    public static void onDrops(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) return;
        if (!(event.getSource().getEntity() instanceof ServerPlayer)) return;

        ItemStack ring = null;
        if (entity instanceof EnderDragon) {
            ring = new ItemStack(ModItems.SPIRIT_RING_100000.get());
            broadcast(entity, "§5§lEnder Dragon (Kaisar Soul Beast terakhir) telah jatuh! Spirit Ring 100.000 tahun muncul!");
        } else if (entity instanceof WitherBoss || entity instanceof Warden || entity instanceof ElderGuardian) {
            ring = new ItemStack(ModItems.SPIRIT_RING_10000.get());
            broadcast(entity, "§dBoss soul beast tumbang! Spirit Ring 10.000 tahun terjatuh.");
        } else if (entity instanceof Monster monster) {
            float hp = monster.getMaxHealth();
            double chance = hp >= 60 ? 0.35 : hp >= 30 ? 0.25 : 0.15;
            if (monster.getRandom().nextDouble() < chance) {
                if (hp >= 60) ring = new ItemStack(ModItems.SPIRIT_RING_10000.get());
                else if (hp >= 30) ring = new ItemStack(ModItems.SPIRIT_RING_1000.get());
                else ring = new ItemStack(ModItems.SPIRIT_RING_100.get());
            }
        }
        if (ring != null) {
            ItemEntity drop = new ItemEntity(entity.level(), entity.getX(), entity.getY() + 0.5, entity.getZ(), ring);
            drop.setDefaultPickUpDelay();
            event.getDrops().add(drop);
        }
    }

    /** Boss diperkuat jauh melampaui vanilla: mereka adalah soul beast tingkat tinggi. */
    @SubscribeEvent
    public static void onBossJoin(EntityJoinLevelEvent event) {
        Entity e = event.getEntity();
        if (event.getLevel().isClientSide || !(e instanceof LivingEntity living)) return;
        if (living.getPersistentData().getBoolean("soulland_buffed")) return;

        double hpMul = 1.0;
        double dmgAdd = 0;
        if (e instanceof EnderDragon) { hpMul = 5.0; dmgAdd = 8; }        // 200 -> 1000 HP: boss akhir sejati
        else if (e instanceof WitherBoss) { hpMul = 2.0; dmgAdd = 4; }     // 300 -> 600 HP
        else if (e instanceof Warden) { hpMul = 1.6; dmgAdd = 5; }         // 500 -> 800 HP
        else if (e instanceof ElderGuardian) { hpMul = 1.5; dmgAdd = 2; }  // 80 -> 120 HP
        else return;

        living.getPersistentData().putBoolean("soulland_buffed", true);
        var hp = living.getAttribute(Attributes.MAX_HEALTH);
        if (hp != null) {
            hp.setBaseValue(hp.getBaseValue() * hpMul);
            living.setHealth(living.getMaxHealth());
        }
        var atk = living.getAttribute(Attributes.ATTACK_DAMAGE);
        if (atk != null) atk.setBaseValue(atk.getBaseValue() + dmgAdd);
    }

    private static void broadcast(LivingEntity entity, String message) {
        if (entity.level() instanceof ServerLevel serverLevel && serverLevel.getServer() != null) {
            serverLevel.getServer().getPlayerList().broadcastSystemMessage(Component.literal(message), false);
        }
    }
}
