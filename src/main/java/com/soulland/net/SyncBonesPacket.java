package com.soulland.net;

import com.soulland.bone.SpiritBoneSlot;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Server -> client: susunan tulang spirit yang terpasang.
 *
 * <p>Paket ini juga membawa pesan status siap tampil, sudah diterjemahkan di
 * server memakai bahasa yang dipilih pemain. Client menampilkannya di baris
 * status layar Tulang Spirit.
 */
public record SyncBonesPacket(String[] bones, String statusText, boolean statusOk) {

    /** Jumlah slot tubuh, harus sama dengan jumlah slot di {@code SpiritBoneSlot}. */
    public static final int SLOT_COUNT = SpiritBoneSlot.COUNT;

    public static void encode(SyncBonesPacket msg, FriendlyByteBuf buf) {
        String[] bones = msg.bones() == null ? new String[0] : msg.bones();
        buf.writeVarInt(bones.length);
        for (String bone : bones) {
            buf.writeUtf(bone == null ? "" : bone, 96);
        }
        buf.writeUtf(msg.statusText() == null ? "" : msg.statusText(), 256);
        buf.writeBoolean(msg.statusOk());
    }

    public static SyncBonesPacket decode(FriendlyByteBuf buf) {
        int length = buf.readVarInt();
        String[] bones = new String[Math.max(0, length)];
        for (int index = 0; index < bones.length; index++) {
            bones[index] = buf.readUtf(96);
        }
        String status = buf.readUtf(256);
        boolean ok = buf.readBoolean();
        return new SyncBonesPacket(bones, status, ok);
    }

    public static void handle(SyncBonesPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientBridge.onBonesSync(msg));
        ctx.get().setPacketHandled(true);
    }
}
