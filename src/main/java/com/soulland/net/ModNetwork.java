package com.soulland.net;

import com.soulland.SoulLandMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModNetwork {
    private static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(SoulLandMod.MODID, "main"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
    );

    public static void register() {
        int id = 0;
        CHANNEL.messageBuilder(SyncCultivationPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SyncCultivationPacket::encode)
                .decoder(SyncCultivationPacket::decode)
                .consumerMainThread(SyncCultivationPacket::handle)
                .add();
        CHANNEL.messageBuilder(ActivateSkillPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ActivateSkillPacket::encode)
                .decoder(ActivateSkillPacket::decode)
                .consumerMainThread(ActivateSkillPacket::handle)
                .add();
        SoulLandMod.LOGGER.info("Network channel Soul Land terdaftar.");
    }
}
