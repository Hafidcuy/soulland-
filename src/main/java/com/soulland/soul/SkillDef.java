package com.soulland.soul;

/** Definisi satu skill martial soul. */
public record SkillDef(
        String id,
        String name,
        String description,
        float energyCost,
        int cooldownTicks,
        int ringsRequired
) {}
