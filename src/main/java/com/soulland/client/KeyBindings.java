package com.soulland.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.soulland.SoulLandMod;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = SoulLandMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class KeyBindings {
    public static KeyMapping SKILL_1;
    public static KeyMapping SKILL_2;

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        SKILL_1 = new KeyMapping("key.soulland.skill1", GLFW.GLFW_KEY_R, "key.categories.soulland");
        SKILL_2 = new KeyMapping("key.soulland.skill2", GLFW.GLFW_KEY_G, "key.categories.soulland");
        event.register(SKILL_1);
        event.register(SKILL_2);
    }
}
