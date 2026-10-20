package com.soulland.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.soulland.SoulLandMod;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = SoulLandMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class KeyBindings {

    public static final String CATEGORY = "key.categories.soulland";

    public static KeyMapping SKILL_1;
    public static KeyMapping SKILL_2;
    public static KeyMapping SKILL_3;
    public static KeyMapping SKILL_4;
    public static KeyMapping SKILL_5;
    public static KeyMapping RING_BURST;
    public static KeyMapping SOUL_CARD;
    public static KeyMapping TOGGLE_SOUL;
    public static KeyMapping MEDITATE;
    public static KeyMapping BLOOD_BURN;
    public static KeyMapping OPEN_GUIDE;

    private static final List<KeyMapping> ALL = new ArrayList<>();

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        SKILL_1 = register(event, "key.soulland.skill1", GLFW.GLFW_KEY_R);
        SKILL_2 = register(event, "key.soulland.skill2", GLFW.GLFW_KEY_G);
        SKILL_3 = register(event, "key.soulland.skill3", GLFW.GLFW_KEY_F);
        SKILL_4 = register(event, "key.soulland.skill4", GLFW.GLFW_KEY_V);
        SKILL_5 = register(event, "key.soulland.skill5", GLFW.GLFW_KEY_Z);
        RING_BURST = register(event, "key.soulland.ring_burst", GLFW.GLFW_KEY_X);
        SOUL_CARD = register(event, "key.soulland.soul_card", GLFW.GLFW_KEY_J);
        TOGGLE_SOUL = register(event, "key.soulland.toggle_soul", GLFW.GLFW_KEY_K);
        MEDITATE = register(event, "key.soulland.meditate", GLFW.GLFW_KEY_C);
        BLOOD_BURN = register(event, "key.soulland.blood_burn", GLFW.GLFW_KEY_B);
        OPEN_GUIDE = register(event, "key.soulland.open_guide", GLFW.GLFW_KEY_H);
    }

    private static KeyMapping register(RegisterKeyMappingsEvent event, String name, int key) {
        KeyMapping mapping = new KeyMapping(name, InputConstants.Type.KEYSYM, key, CATEGORY);
        event.register(mapping);
        ALL.add(mapping);
        return mapping;
    }
}
