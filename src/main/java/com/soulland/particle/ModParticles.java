package com.soulland.particle;

import com.soulland.SoulLandMod;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Partikel kustom mod ini.
 *
 * <p>Setiap partikel punya texture sendiri di {@code assets/soulland/textures/particle/},
 * jadi bentuk dan warnanya berbeda dari partikel vanilla. Delapan jenis ini dipakai untuk
 * efek skill, ritual, dan enh ambience.
 */
public final class ModParticles {
    private ModParticles() {}

    @SuppressWarnings("unchecked")
    private static RegistryObject<SimpleParticleType> register(String name) {
        return (RegistryObject<SimpleParticleType>) (RegistryObject<?>)
                PARTICLES.register(name, () -> new SimpleParticleType(false));
    }

    public static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, SoulLandMod.MODID);

    /** Nyala soul naik ke atas, untuk skill api dan ledakan. */
    public static final RegistryObject<SimpleParticleType> SOUL_FLAME = register("soul_flame");

    /** Percikan soul yang berjatuhan, untuk skill petir dan tebasan. */
    public static final RegistryObject<SimpleParticleType> SOUL_SPARK = register("soul_spark");

    /** Kabut bayangan yang mengambang, untuk skill gelap dan hantu. */
    public static final RegistryObject<SimpleParticleType> SOUL_SHADOW = register("soul_shadow");

    /** Riak cincin soul yang melebar, untuk gelombang serangan. */
    public static final RegistryObject<SimpleParticleType> SOUL_RIPPLE = register("soul_ripple");

    /** Bintang soul yang berkedip, untuk skill cahaya dan evolusi. */
    public static final RegistryObject<SimpleParticleType> SOUL_STAR = register("soul_star");

    /** Tetesan darah terbakar, untuk absorpsi gagal dan bakar darah. */
    public static final RegistryObject<SimpleParticleType> SOUL_BLOOD = register("soul_blood");

    /** Serpihan es yang melayang pelan, untuk skill es dan hibernasi. */
    public static final RegistryObject<SimpleParticleType> SOUL_FROST = register("soul_frost");

    /** Petir soul yang menyambar, untuk skill petir. */
    public static final RegistryObject<SimpleParticleType> SOUL_THUNDER = register("soul_thunder");

    /** Debu qi yang naik pelan, untuk meditasi dan upacara terobosan. */
    public static final RegistryObject<SimpleParticleType> SOUL_MIST = register("soul_mist");

    public static final RegistryObject<SimpleParticleType> SOUL_EMBERS = register("soul_embers");

    public static final RegistryObject<SimpleParticleType> SOUL_SHARDS = register("soul_shards");
}
