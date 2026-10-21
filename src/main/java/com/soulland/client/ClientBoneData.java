package com.soulland.client;

import com.soulland.net.SyncBonesPacket;

/**
 * Salinan susunan tulang spirit di sisi client.
 *
 * <p>Isinya cuma cermin dari server: client tidak pernah decides sendiri
 * tulang mana yang terpasang.
 */
public final class ClientBoneData {
    private ClientBoneData() {}

    /** Id item tulang yang terpasang per slot tubuh. String kosong berarti kosong. */
    public static String[] bones = new String[SyncBonesPacket.SLOT_COUNT];

    /** Pesan status terakhir dari server, sudah diterjemahkan. */
    public static String statusText = "";

    /** True kalau aksi terakhir dari server berhasil. */
    public static boolean statusOk = true;

    public static void set(SyncBonesPacket packet) {
        String[] incoming = packet.bones() == null ? new String[0] : packet.bones();
        String[] stored = new String[SyncBonesPacket.SLOT_COUNT];
        for (int index = 0; index < stored.length; index++) {
            stored[index] = index < incoming.length && incoming[index] != null ? incoming[index] : "";
        }
        bones = stored;
        statusText = packet.statusText() == null ? "" : packet.statusText();
        statusOk = packet.statusOk();
    }

    /** Id item tulang yang terpasang pada sebuah slot. */
    public static String boneAt(int index) {
        if (index < 0 || index >= bones.length) return "";
        String value = bones[index];
        return value == null ? "" : value;
    }
}
