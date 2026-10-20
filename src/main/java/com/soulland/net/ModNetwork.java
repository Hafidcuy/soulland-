package com.soulland.net;

import com.soulland.SoulLandMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModNetwork {
    private static final String PROTOCOL = "2";

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
        CHANNEL.messageBuilder(VisualFxPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(VisualFxPacket::encode)
                .decoder(VisualFxPacket::decode)
                .consumerMainThread(VisualFxPacket::handle)
                .add();
        CHANNEL.messageBuilder(ToggleSoulPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ToggleSoulPacket::encode)
                .decoder(ToggleSoulPacket::decode)
                .consumerMainThread(ToggleSoulPacket::handle)
                .add();
        CHANNEL.messageBuilder(MeditatePacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(MeditatePacket::encode)
                .decoder(MeditatePacket::decode)
                .consumerMainThread(MeditatePacket::handle)
                .add();
        CHANNEL.messageBuilder(RingBurstPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(RingBurstPacket::encode)
                .decoder(RingBurstPacket::decode)
                .consumerMainThread(RingBurstPacket::handle)
                .add();
        CHANNEL.messageBuilder(ChooseSectPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ChooseSectPacket::encode)
                .decoder(ChooseSectPacket::decode)
                .consumerMainThread(ChooseSectPacket::handle)
                .add();
        SoulLandMod.LOGGER.info("Network channel Soul Land terdaftar.");
    }
}
