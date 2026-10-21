package com.soulland.net;

import com.soulland.SoulLandMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModNetwork {
    /**
     * Protokol jaringan mod. Naik ke "4" pada v1.3 karena
     * {@link SyncCultivationPacket} menambah batas spirit power, dan ada
     * paket baru {@link SyncBonesPacket}, {@link EquipBonePacket},
     * {@link UnequipBonePacket}, serta {@link OpenBoneScreenPacket}.
     */
    private static final String PROTOCOL = "4";

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
        CHANNEL.messageBuilder(OpenAwakeningGuiPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(OpenAwakeningGuiPacket::encode)
                .decoder(OpenAwakeningGuiPacket::decode)
                .consumerMainThread(OpenAwakeningGuiPacket::handle)
                .add();
        CHANNEL.messageBuilder(ChooseSoulPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ChooseSoulPacket::encode)
                .decoder(ChooseSoulPacket::decode)
                .consumerMainThread(ChooseSoulPacket::handle)
                .add();
        CHANNEL.messageBuilder(ChooseSoulResultPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ChooseSoulResultPacket::encode)
                .decoder(ChooseSoulResultPacket::decode)
                .consumerMainThread(ChooseSoulResultPacket::handle)
                .add();
        CHANNEL.messageBuilder(ManifestationPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ManifestationPacket::encode)
                .decoder(ManifestationPacket::decode)
                .consumerMainThread(ManifestationPacket::handle)
                .add();
        CHANNEL.messageBuilder(SyncRingAgesPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SyncRingAgesPacket::encode)
                .decoder(SyncRingAgesPacket::decode)
                .consumerMainThread(SyncRingAgesPacket::handle)
                .add();
        CHANNEL.messageBuilder(CloseAwakeningGuiPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(CloseAwakeningGuiPacket::encode)
                .decoder(CloseAwakeningGuiPacket::decode)
                .consumerMainThread(CloseAwakeningGuiPacket::handle)
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
        CHANNEL.messageBuilder(BloodBurnPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(BloodBurnPacket::encode)
                .decoder(BloodBurnPacket::decode)
                .consumerMainThread(BloodBurnPacket::handle)
                .add();
        CHANNEL.messageBuilder(AssignSkillPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(AssignSkillPacket::encode)
                .decoder(AssignSkillPacket::decode)
                .consumerMainThread(AssignSkillPacket::handle)
                .add();
        CHANNEL.messageBuilder(OpenGuiPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(OpenGuiPacket::encode)
                .decoder(OpenGuiPacket::decode)
                .consumerMainThread(OpenGuiPacket::handle)
                .add();
        CHANNEL.messageBuilder(SelectLanguagePacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(SelectLanguagePacket::encode)
                .decoder(SelectLanguagePacket::decode)
                .consumerMainThread(SelectLanguagePacket::handle)
                .add();
        CHANNEL.messageBuilder(OpenLanguageScreenPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(OpenLanguageScreenPacket::encode)
                .decoder(OpenLanguageScreenPacket::decode)
                .consumerMainThread(OpenLanguageScreenPacket::handle)
                .add();
        CHANNEL.messageBuilder(OpenAlchemyPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(OpenAlchemyPacket::encode)
                .decoder(OpenAlchemyPacket::decode)
                .consumerMainThread(OpenAlchemyPacket::handle)
                .add();
        CHANNEL.messageBuilder(CraftAlchemyPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(CraftAlchemyPacket::encode)
                .decoder(CraftAlchemyPacket::decode)
                .consumerMainThread(CraftAlchemyPacket::handle)
                .add();
        CHANNEL.messageBuilder(AlchemyResultPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(AlchemyResultPacket::encode)
                .decoder(AlchemyResultPacket::decode)
                .consumerMainThread(AlchemyResultPacket::handle)
                .add();
        CHANNEL.messageBuilder(SyncBonesPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SyncBonesPacket::encode)
                .decoder(SyncBonesPacket::decode)
                .consumerMainThread(SyncBonesPacket::handle)
                .add();
        CHANNEL.messageBuilder(EquipBonePacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(EquipBonePacket::encode)
                .decoder(EquipBonePacket::decode)
                .consumerMainThread(EquipBonePacket::handle)
                .add();
        CHANNEL.messageBuilder(UnequipBonePacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(UnequipBonePacket::encode)
                .decoder(UnequipBonePacket::decode)
                .consumerMainThread(UnequipBonePacket::handle)
                .add();
        CHANNEL.messageBuilder(OpenBoneScreenPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(OpenBoneScreenPacket::encode)
                .decoder(OpenBoneScreenPacket::decode)
                .consumerMainThread(OpenBoneScreenPacket::handle)
                .add();
        SoulLandMod.LOGGER.info("Network channel Soul Land terdaftar (protokol {}).", PROTOCOL);
    }
}
