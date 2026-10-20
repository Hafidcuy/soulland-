package com.soulland.entity;

import com.soulland.SoulLandMod;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Entitas kustom mod ini. */
public final class ModEntities {
    private ModEntities() {}

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, SoulLandMod.MODID);

    public static final RegistryObject<EntityType<SoulVfxEntity>> SOUL_VFX = ENTITIES.register("soul_vfx",
            () -> EntityType.Builder.<SoulVfxEntity>of(SoulVfxEntity::new, MobCategory.MISC)
                    .sized(1.2f, 1.2f)
                    .clientTrackingRange(10)
                    .updateInterval(1)
                    .build("soul_vfx"));

    public static final RegistryObject<EntityType<MartialSoulEntity>> MARTIAL_SOUL = ENTITIES.register("martial_soul",
            () -> EntityType.Builder.<MartialSoulEntity>of(MartialSoulEntity::new, MobCategory.MISC)
                    .sized(1.0f, 1.0f)
                    .clientTrackingRange(12)
                    .updateInterval(1)
                    .build("martial_soul"));
}
