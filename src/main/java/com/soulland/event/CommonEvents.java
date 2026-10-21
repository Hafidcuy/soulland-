package com.soulland.event;

import com.soulland.CultivationRanks;
import com.soulland.SoulLandMod;
import com.soulland.cultivation.CultivationCapability;
import com.soulland.cultivation.Foundation;
import com.soulland.cultivation.CultivationManager;
import com.soulland.cultivation.ICultivation;
import com.soulland.book.GuideBook;
import com.soulland.bone.BoneDrops;
import com.soulland.bone.BoneSetBonus;
import com.soulland.bone.BoneStats;
import com.soulland.bone.SpiritBoneManager;
import com.soulland.entity.SoulCompanion;
import com.soulland.item.HuntContractItem;
import com.soulland.item.ModItems;
import com.soulland.item.ToolCharge;
import com.soulland.lang.SoulLang;
import com.soulland.skill.AttackEffects;
import com.soulland.skill.SkillFx;
import com.soulland.soul.Element;
import com.soulland.soul.MartialSoul;
import com.soulland.soul.MartialSouls;
import com.soulland.soul.RingHaloSync;
import com.soulland.soul.SoulBeast;
import com.soulland.soul.SoulSelection;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class CommonEvents {

    /**
     * Login pertama: kalau bahasa belum dipilih, layar Pilih Bahasa dibuka lebih
     * dulu dan upacara Kebangkitan menyusul setelah pemain memilih. Login
     * berikutnya tidak menampilkan layar bahasa lagi.
     */
    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        CultivationCapability.get(player).ifPresent(cap -> {
            if (cap.getLanguage().isEmpty()) {
                com.soulland.net.ModNetwork.CHANNEL.send(
                        net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player),
                        new com.soulland.net.OpenLanguageScreenPacket(true));
            } else if (!cap.isAwakened()) {
                SoulSelection.openCeremony(player, false);
            }
            CultivationManager.applyStats(player);
            CultivationManager.sync(player);
            SpiritBoneManager.sync(player);
        });
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            CultivationManager.applyStats(player);
            CultivationManager.sync(player);
            SpiritBoneManager.sync(player);
            RingHaloSync.broadcast(player);
        }
    }

    @SubscribeEvent
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            SoulCompanion.removeAll(player);
            CultivationManager.sync(player);
            SpiritBoneManager.sync(player);
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

    /** Regenerasi spirit energy, meditasi, dan sinkronisasi ringan. */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;

        BoneSetBonus boneSet = SpiritBoneManager.setBonus(event.player);
        CultivationCapability.get(event.player).ifPresent(cap -> {
            if (cap.getEnergy() < cap.getEnergyCap()) {
                float regen = 0.05f * (1.0f + boneSet.spiritRegenPercent() / 100.0f);
                cap.setEnergy(cap.getEnergy() + regen);
                if (event.player.tickCount % 20 == 0 && event.player instanceof ServerPlayer sp) {
                    CultivationManager.sync(sp);
                }
            }

            if (event.player.tickCount % 20 == 0) {
                applyBoneRegen(event.player);
            }

            if (cap.isMeditating()) {
                handleMeditation(event.player, cap);
            }

            long gameTime = event.player.level().getGameTime();
            if (cap.getChainCount() > 0 && gameTime > cap.getChainUntil()) {
                cap.setChainCount(0);
                if (event.player instanceof ServerPlayer sp) {
                    CultivationManager.sync(sp);
                }
            }
            if (cap.getBloodBurnTicks() > 0) {
                cap.setBloodBurnTicks(cap.getBloodBurnTicks() - 1);
            }
        });
        if (event.player instanceof ServerPlayer sp) {
            if (sp.tickCount % ToolCharge.BATTERY_REGEN_INTERVAL == 0) {
                ToolCharge.regenBattery(sp, sp.getMainHandItem());
            }
        }
    }

    /** Bonus damage keluaran berdasarkan level kultivasi, plus efek serangan khas tiap soul. */
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getSource().getEntity() instanceof ServerPlayer player) {
            CultivationCapability.get(player).ifPresent(cap ->
                    event.setAmount(event.getAmount() * (1f + (cap.getLevel() - 1) * 0.01f + cap.getRings() * 0.05f)));
        }
        if (event.getEntity() instanceof ServerPlayer victim) {
            BoneSetBonus set = SpiritBoneManager.setBonus(victim);
            if (set.active() && set.defensePercent() > 0f) {
                event.setAmount(event.getAmount() * (1f - set.defensePercent() / 100.0f));
            }
        }
        AttackEffects.onLivingHurt(event);
    }

    /** Regenerasi HP dari tulang spirit, dihitung sekali per detik. */
    private static void applyBoneRegen(Player player) {
        if (!player.isAlive() || player.isCreative() || player.isSpectator()) return;
        BoneStats bones = SpiritBoneManager.stats(player);
        BoneSetBonus set = SpiritBoneManager.setBonus(player);
        float perSecond = bones.regen() + set.regen();
        if (perSecond <= 0f) return;
        if (player.getHealth() >= player.getMaxHealth()) return;
        player.heal(perSecond);
    }

    /** Setiap pukulan biasa menyisakan kilatan warna martial soul di target. */
    @SubscribeEvent
    public static void onAttack(AttackEntityEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        Entity target = event.getTarget();
        CultivationCapability.get(player).ifPresent(cap -> {
            MartialSoul soul = MartialSouls.effective(cap.getMartialSoulId(), cap.getEvolutionStage());
            if (soul == null) return;
            player.serverLevel().sendParticles(new DustParticleOptions(SkillFx.rgb(soul.color()), 1.2f),
                    target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                    14, 0.28, 0.28, 0.28, 0.02);
            player.serverLevel().sendParticles(ParticleTypes.END_ROD,
                    target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                    4, 0.18, 0.18, 0.18, 0.01);
        });
    }

    /** Membunuh soul beast (monster) memberi XP kultivasi. */
    @SubscribeEvent
    public static void onKill(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide) return;
        if (event.getSource().getEntity() instanceof ServerPlayer player && event.getEntity() instanceof Monster) {
            int xp = (int) (event.getEntity().getMaxHealth() * 1.5) + 15;
            CultivationManager.addXp(player, xp);
            advanceHuntContract(player, event.getEntity().level().getGameTime());
            Hints.show(player, Hints.ELEMENT,
                    SoulLang.translate(CultivationManager.messageLanguage(player),
                            "hint.soulland.element",
                            "Nama soul beast menyebut usianya dan elemennya. Elemen soul-mu yang menang "
                                    + "memberi +25% damage, kalah memberi -15%."));
        }
    }

    /** Kontrak perburuan: setiap soul beast yang diburu menambah satu, target tercapai memberi hadiah. */
    private static void advanceHuntContract(ServerPlayer player, long gameTime) {
        CultivationCapability.get(player).ifPresent(cap -> {
            if (cap.getContractUntil() <= gameTime) return;
            int kills = cap.getContractKills() + 1;
            cap.setContractKills(kills);
            if (kills < HuntContractItem.TARGET) {
                if (kills % 5 == 0) {
                    player.sendSystemMessage(SoulLang.text(CultivationManager.messageLanguage(player),
                            "message.soulland.contract.progress", "Kontrak perburuan: %s/%s soul beast.",
                            kills, HuntContractItem.TARGET));
                }
                return;
            }
            cap.setContractKills(0);
            cap.setContractUntil(0L);
            player.getInventory().add(new ItemStack(ModItems.SPIRIT_RING_100.get()));
            player.getInventory().add(new ItemStack(ModItems.SOUL_ESSENCE.get(), 4));
            player.sendSystemMessage(SoulLang.text(CultivationManager.messageLanguage(player),
                            "message.soulland.contract.done",
                            "Kontrak perburuan selesai. Hadiah: spirit ring 100 tahun dan 4 esensi jiwa.")
                    .withStyle(ChatFormatting.GOLD));
            player.serverLevel().sendParticles(net.minecraft.core.particles.ParticleTypes.TOTEM_OF_UNDYING,
                    player.getX(), player.getY() + 1.0, player.getZ(), 40, 0.6, 1.0, 0.6, 0.05);
            CultivationManager.sync(player);
        });
    }

    /** Soul beast menjatuhkan spirit ring dan esensi jiwa; boss menjatuhkan ring langka. */
    @SubscribeEvent
    public static void onDrops(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) return;
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;

        ItemStack ring = null;
        if (entity instanceof EnderDragon) {
            // Kaisar Soul Beast: hadiah puncak, grade Emas satu juta tahun.
            ring = new ItemStack(ModItems.SPIRIT_RING_1000000.get());
            broadcast(entity, "message.soulland.boss.dragon", ChatFormatting.DARK_PURPLE,
                    "§5§lEnder Dragon (Kaisar Soul Beast terakhir) telah jatuh! Spirit Ring Emas 1.000.000 tahun muncul!");
        } else if (entity instanceof WitherBoss || entity instanceof Warden || entity instanceof ElderGuardian) {
            ring = new ItemStack(ModItems.SPIRIT_RING_100000.get());
            broadcast(entity, "message.soulland.boss.tier", ChatFormatting.LIGHT_PURPLE,
                    "Boss soul beast tumbang. Spirit Ring 100.000 tahun terjatuh.");
        } else if (entity instanceof Monster monster) {
            float hp = monster.getMaxHealth();
            double chance = hp >= 60 ? 0.35 : hp >= 30 ? 0.25 : 0.15;
            if (monster.getRandom().nextDouble() < chance) {
                if (hp >= 60) ring = new ItemStack(ModItems.SPIRIT_RING_10000.get());
                else if (hp >= 30) ring = new ItemStack(ModItems.SPIRIT_RING_1000.get());
                else ring = new ItemStack(ModItems.SPIRIT_RING_100.get());
            }
            if (monster.getRandom().nextDouble() < 0.22) {
                event.getDrops().add(new ItemEntity(entity.level(), entity.getX(), entity.getY() + 0.5, entity.getZ(),
                        new ItemStack(ModItems.SOUL_ESSENCE.get())));
            }
        }

        if (ring != null) {
            ItemEntity drop = new ItemEntity(entity.level(), entity.getX(), entity.getY() + 0.5, entity.getZ(), ring);
            drop.setDefaultPickUpDelay();
            event.getDrops().add(drop);
        }

        BoneDrops.roll(entity, player, event.getDrops());
    }

    /** Boss diperkuat jauh melampaui vanilla: mereka adalah soul beast tingkat tinggi. */
    @SubscribeEvent
    public static void onBossJoin(EntityJoinLevelEvent event) {
        Entity e = event.getEntity();
        if (event.getLevel().isClientSide || !(e instanceof LivingEntity living)) return;
        if (living.getPersistentData().getBoolean("soulland_buffed")) return;

        double hpMul = 1.0;
        double dmgAdd = 0;
        if (e instanceof EnderDragon) { hpMul = 5.0; dmgAdd = 8; }
        else if (e instanceof WitherBoss) { hpMul = 2.0; dmgAdd = 4; }
        else if (e instanceof Warden) { hpMul = 1.6; dmgAdd = 5; }
        else if (e instanceof ElderGuardian) { hpMul = 1.5; dmgAdd = 2; }
        else {
            nameSoulBeast(living);
            return;
        }

        living.getPersistentData().putBoolean("soulland_buffed", true);
        var hp = living.getAttribute(Attributes.MAX_HEALTH);
        if (hp != null) {
            hp.setBaseValue(hp.getBaseValue() * hpMul);
            living.setHealth(living.getMaxHealth());
        }
        var atk = living.getAttribute(Attributes.ATTACK_DAMAGE);
        if (atk != null) atk.setBaseValue(atk.getBaseValue() + dmgAdd);
    }

    /** Monster biasa diberi nama soul beast sesuai usia dan elemen tempurnya. */
    private static void nameSoulBeast(LivingEntity living) {
        if (!(living instanceof Monster monster)) return;
        if (monster.getCustomName() != null) return;

        float hp = monster.getMaxHealth();
        int years = hp >= 80 ? 10000 : hp >= 40 ? 1000 : hp >= 20 ? 100 : 10;
        Element element = elementFor(monster);
        element = elementForBiome(element, monster);
        String name = monster.getType().getDescription().getString() + " " + years + " Tahun (" + element.label() + ")";

        net.minecraft.ChatFormatting color = switch (years) {
            case 10000 -> net.minecraft.ChatFormatting.LIGHT_PURPLE;
            case 1000 -> net.minecraft.ChatFormatting.AQUA;
            case 100 -> net.minecraft.ChatFormatting.GREEN;
            default -> net.minecraft.ChatFormatting.GRAY;
        };
        monster.setCustomName(Component.literal(name).withStyle(color));
        monster.setCustomNameVisible(true);
        SoulBeast.assign(monster, element, years);
    }

    /**
     * Bioma mengubah karakter soul beast: wilayah Moonlight dipenuhi spirit hutan, jadi
     * elemennya berpindah ke Tumbuhan dan Cahaya. Ini yang membuat berburu terasa berbeda
     * di tiap wilayah.
     */
    private static Element elementForBiome(Element element, LivingEntity monster) {
        var biome = monster.level().getBiome(monster.blockPosition());
        String id = biome.unwrapKey().map(key -> key.location().toString()).orElse("");
        if (id.contains("moonlight")) {
            return switch (element) {
                case FIRE, EARTH, NONE -> Element.NATURE;
                case ICE, WATER -> Element.LIGHT;
                default -> element;
            };
        }
        return element;
    }

    /** Elemen tempur soul beast berdasarkan jenis mobnya, supaya target bisa dibaca pemain. */
    private static Element elementFor(LivingEntity monster) {
        if (monster instanceof net.minecraft.world.entity.monster.Blaze
                || monster instanceof net.minecraft.world.entity.monster.MagmaCube
                || monster instanceof net.minecraft.world.entity.monster.piglin.Piglin
                || monster instanceof net.minecraft.world.entity.monster.piglin.PiglinBrute
                || monster instanceof net.minecraft.world.entity.monster.Ghast) {
            return Element.FIRE;
        }
        if (monster instanceof net.minecraft.world.entity.animal.SnowGolem
                || monster instanceof net.minecraft.world.entity.monster.Stray
                || monster instanceof net.minecraft.world.entity.animal.PolarBear) {
            return Element.ICE;
        }
        if (monster instanceof net.minecraft.world.entity.monster.Creeper) {
            return Element.LIGHTNING;
        }
        if (monster instanceof net.minecraft.world.entity.monster.Phantom
                || monster instanceof net.minecraft.world.entity.monster.Witch
                || monster instanceof net.minecraft.world.entity.boss.wither.WitherBoss
                || monster instanceof net.minecraft.world.entity.monster.EnderMan) {
            return Element.DARK;
        }
        if (monster instanceof net.minecraft.world.entity.monster.Slime
                || monster instanceof net.minecraft.world.entity.monster.Drowned
                || monster instanceof net.minecraft.world.entity.monster.Guardian) {
            return Element.WATER;
        }
        if (monster instanceof net.minecraft.world.entity.monster.Silverfish
                || monster instanceof net.minecraft.world.entity.monster.Endermite
                || monster instanceof net.minecraft.world.entity.monster.CaveSpider
                || monster instanceof net.minecraft.world.entity.monster.Spider) {
            return Element.POISON;
        }
        if (monster instanceof net.minecraft.world.entity.animal.Wolf
                || monster instanceof net.minecraft.world.entity.animal.Fox
                || monster instanceof net.minecraft.world.entity.ambient.Bat) {
            return Element.WIND;
        }
        if (monster instanceof net.minecraft.world.entity.animal.Cow
                || monster instanceof net.minecraft.world.entity.animal.Sheep
                || monster instanceof net.minecraft.world.entity.animal.Pig
                || monster instanceof net.minecraft.world.entity.animal.Chicken
                || monster instanceof net.minecraft.world.entity.animal.Bee) {
            return Element.NATURE;
        }
        return Element.EARTH;
    }

    /**
     * Kabar dunia soal soul beast besar. Setiap pemain menerima versinya
     * sendiri, mengikuti bahasa yang ia pilih.
     */
    private static void broadcast(LivingEntity entity, String key, ChatFormatting color, String fallback) {
        if (!(entity.level() instanceof ServerLevel serverLevel) || serverLevel.getServer() == null) return;
        serverLevel.getServer().getPlayerList().getPlayers().forEach(player ->
                player.sendSystemMessage(SoulLang.text(CultivationManager.messageLanguage(player), key, fallback)
                        .withStyle(color)));
    }

    private static void handleMeditation(net.minecraft.world.entity.player.Player player,
                                         ICultivation cap) {
        String language = CultivationManager.messageLanguage(player);
        if (player.getDeltaMovement().horizontalDistanceSqr() > 0.0004) {
            cap.setMeditating(false);
            cap.setMeditationTicks(0);
            player.sendSystemMessage(SoulLang.text(language, "message.soulland.meditation_broken",
                    "Meditasi terputus karena kamu bergerak."));
            return;
        }

        int ticks = cap.getMeditationTicks() + 1;
        cap.setMeditationTicks(ticks);

        // Fondasi kultivasi bertambah pelan selama meditasi.
        if (ticks % Foundation.TICKS_PER_POINT == 0) {
            int before = cap.getFoundation();
            cap.setFoundation(Foundation.gain(before, 1));
            if (cap.getFoundation() > before && cap.getFoundation() % 10 == 0) {
                player.sendSystemMessage(SoulLang.text(language, "message.soulland.foundation_gain",
                        "§bFondasi kultivasi menguat: %s/100.", cap.getFoundation()));
            }
        }

        if (CultivationManager.canBreakthrough(cap)) {
            if (ticks % 20 == 0) {
                player.sendSystemMessage(SoulLang.text(language, "message.soulland.breakthrough_progress",
                        "Upacara terobosan: %s/3", ticks / 20));
            }
            if (ticks >= 60) {
                if (consumeEssence(player, 1)) {
                    cap.setMeditationTicks(0);
                    CultivationManager.breakthrough(player);
                } else {
                    player.sendSystemMessage(SoulLang.text(language, "message.soulland.breakthrough_essence",
                            "Butuh 1 Esensi Jiwa untuk upacara terobosan."));
                }
            }
            return;
        }

        if (CultivationRanks.isGateLevel(cap.getLevel()) && !Foundation.canAttempt(cap.getFoundation())) {
            if (ticks % 40 == 0) {
                player.sendSystemMessage(SoulLang.text(language, "message.soulland.foundation_low",
                        "§eFondasi belum cukup untuk upacara terobosan (butuh %s, sekarang %s).",
                        Foundation.MIN_TO_ATTEMPT, cap.getFoundation()));
            }
            return;
        }

        if (ticks % 40 == 0) {
            CultivationManager.addXp(player, 4);
            cap.setEnergy(Math.min(100f, cap.getEnergy() + 12f));
            if (player instanceof ServerPlayer sp) {
                sp.serverLevel().sendParticles(ParticleTypes.SCULK_SOUL,
                        player.getX(), player.getY() + 0.4, player.getZ(), 14, 0.45, 0.9, 0.45, 0.01);
            }
        }
    }

    private static boolean consumeEssence(net.minecraft.world.entity.player.Player player, int amount) {
        int left = amount;
        for (int i = 0; i < player.getInventory().items.size() && left > 0; i++) {
            var stack = player.getInventory().items.get(i);
            if (!stack.is(ModItems.SOUL_ESSENCE.get())) continue;
            int take = Math.min(left, stack.getCount());
            stack.shrink(take);
            left -= take;
        }
        return left == 0;
    }
}
