package com.soulland.client.particle;

import com.soulland.SoulLandMod;
import com.soulland.particle.ModParticles;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Pendaftaran provider partikel kustom. Setiap jenis punya texture sendiri. */
@Mod.EventBusSubscriber(modid = SoulLandMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientParticleSetup {

    @SubscribeEvent
    public static void registerProviders(RegisterParticleProvidersEvent event) {
        register(event, ModParticles.SOUL_FLAME.get(), SoulParticle.Kind.FLAME);
        register(event, ModParticles.SOUL_SPARK.get(), SoulParticle.Kind.SPARK);
        register(event, ModParticles.SOUL_SHADOW.get(), SoulParticle.Kind.SHADOW);
        register(event, ModParticles.SOUL_RIPPLE.get(), SoulParticle.Kind.RIPPLE);
        register(event, ModParticles.SOUL_STAR.get(), SoulParticle.Kind.STAR);
        register(event, ModParticles.SOUL_BLOOD.get(), SoulParticle.Kind.BLOOD);
        register(event, ModParticles.SOUL_FROST.get(), SoulParticle.Kind.FROST);
        register(event, ModParticles.SOUL_THUNDER.get(), SoulParticle.Kind.THUNDER);
        register(event, ModParticles.SOUL_MIST.get(), SoulParticle.Kind.MIST);
        register(event, ModParticles.SOUL_EMBERS.get(), SoulParticle.Kind.EMBERS);
        register(event, ModParticles.SOUL_SHARDS.get(), SoulParticle.Kind.SHARDS);
    }

    private static void register(RegisterParticleProvidersEvent event,
                                 SimpleParticleType type,
                                 SoulParticle.Kind kind) {
        event.registerSprite(type, (ParticleProvider.Sprite<SimpleParticleType>)
                (particleType, level, x, y, z, xSpeed, ySpeed, zSpeed) ->
                        new SoulParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, kind));
    }
}
