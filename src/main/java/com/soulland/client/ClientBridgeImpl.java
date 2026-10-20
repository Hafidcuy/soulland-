package com.soulland.client;

import com.soulland.net.ChooseSoulResultPacket;
import com.soulland.net.ClientBridge;
import com.soulland.net.ManifestationPacket;
import com.soulland.net.SyncCultivationPacket;
import com.soulland.net.SyncRingAgesPacket;

/**
 * Implementasi sisi client untuk {@link ClientBridge}.
 * Dipasang saat client setup, jadi kelas client-only ini tidak pernah
 * disentuh server khusus.
 */
public final class ClientBridgeImpl implements ClientBridge.Handler {

    public ClientBridgeImpl() {
        ClientBridge.setSoulIdSupplier(() -> ClientCultivationData.soulId);
        ClientBridge.setLanguageSupplier(() -> ClientCultivationData.effectiveLanguage());
        ClientBridge.setLevelSupplier(() -> ClientCultivationData.level);
    }

    @Override
    public void openAwakening(boolean reroll) {
        ClientGuiHooks.openAwakening(reroll);
    }

    @Override
    public void openLanguageScreen(boolean mandatory) {
        ClientGuiHooks.openLanguageScreen(mandatory);
    }


    @Override
    public void onChooseSoulResult(ChooseSoulResultPacket packet) {
        ClientGuiHooks.onChooseSoulResult(packet);
    }

    @Override
    public void onCultivationSync(SyncCultivationPacket packet) {
        ClientCultivationData.set(packet);
    }

    @Override
    public void onRingAgesSync(SyncRingAgesPacket packet) {
        ClientHaloData.set(packet.entityId(), packet.ringAges());
    }

    @Override
    public void onManifestation(ManifestationPacket packet) {
        ClientManifestations.start(packet.entityId(), packet.soulId());
    }
}
