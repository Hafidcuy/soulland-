package com.soulland.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Server -> client: buka layar Boutique Tulang Spirit. */
public record OpenBoneScreenPacket() {

    public static void encode(OpenBoneScreenPacket msg, FriendlyByteBuf buf) {
    }

    public static OpenBoneScreenPacket decode(FriendlyByteBuf buf) {
        return new OpenBoneScreenPacket();
    }

    public static void handle(OpenBoneScreenPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> com.soulland.client.ClientScreenOpener::openBoneScreen));
        ctx.get().setPacketHandled(true);
    }
}
