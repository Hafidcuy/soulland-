package com.soulland.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Meminta client membuka salah satu tampilan mod: menyembah atau panduan. */
public record OpenGuiPacket(String screen) {

    public static final String WELCOME = "welcome";
    public static final String GUIDE = "guide";

    public static void encode(OpenGuiPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.screen(), 32);
    }

    public static OpenGuiPacket decode(FriendlyByteBuf buf) {
        return new OpenGuiPacket(buf.readUtf(32));
    }

    public static void handle(OpenGuiPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> WELCOME.equals(msg.screen())
                        ? com.soulland.client.ClientScreenOpener::openWelcome
                        : com.soulland.client.ClientScreenOpener::openGuide));
        ctx.get().setPacketHandled(true);
    }
}
