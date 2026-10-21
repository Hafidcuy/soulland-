package com.soulland.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.soulland.SoulLandMod;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

/**
 * Peta tombol skill martial soul. Hanya sisi client yang boleh menyentuh kelas ini
 * (Dist.CLIENT) supaya server khusus tidak pernah memuatnya.
 *
 * <p>Tombol True Body (T) dan Domain (D) sengaja tidak memakai huruf yang
 * sudah dipakai skill (R, G, X, C) maupun tombol kartu soul (J).
 */
@Mod.EventBusSubscriber(modid = SoulLandMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class KeyBindings {

    public static final String CATEGORY = "key.categories.soulland";

    public static KeyMapping SKILL_1;
    public static KeyMapping SKILL_2;
    public static KeyMapping SKILL_3;
    public static KeyMapping SKILL_4;
    public static KeyMapping TRUE_BODY;
    public static KeyMapping DOMAIN;

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        SKILL_1 = new KeyMapping("key.soulland.skill1", GLFW.GLFW_KEY_R, "key.categories.soulland");
        SKILL_2 = new KeyMapping("key.soulland.skill2", GLFW.GLFW_KEY_G, "key.categories.soulland");
        SKILL_3 = new KeyMapping("key.soulland.skill3", GLFW.GLFW_KEY_X, "key.categories.soulland");
        SKILL_4 = new KeyMapping("key.soulland.skill4", GLFW.GLFW_KEY_C, "key.categories.soulland");
        TRUE_BODY = new KeyMapping("key.soulland.truebody", GLFW.GLFW_KEY_T, "key.categories.soulland");
        DOMAIN = new KeyMapping("key.soulland.domain", GLFW.GLFW_KEY_D, "key.categories.soulland");
        event.register(SKILL_1);
        event.register(SKILL_2);
        event.register(SKILL_3);
        event.register(SKILL_4);
        event.register(TRUE_BODY);
        event.register(DOMAIN);
    }
}
