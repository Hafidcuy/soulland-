package com.soulland.bone;

import com.soulland.SoulLandMod;
import com.soulland.cultivation.CultivationCapability;
import com.soulland.cultivation.CultivationManager;
import com.soulland.cultivation.ICultivation;
import com.soulland.item.ModItems;
import com.soulland.lang.SoulLang;
import com.soulland.net.ModNetwork;
import com.soulland.net.SyncBonesPacket;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.PacketDistributor;

import java.util.EnumMap;
import java.util.Map;

/**
 * Tulang spirit yang terpasang di tubuh pemain.
 *
 * <p>Server adalah satu-satunya pihak yang boleh mengubah susunan tulang.
 * Capability menyimpan id registry item, bukan {@link ItemStack}, supaya data
 * lama tetap terbaca kalau nama registry berubah.
 */
public final class SpiritBoneManager {
    private SpiritBoneManager() {}

    /** Jumlah slot tubuh, sama dengan jumlah nilai {@link SpiritBoneSlot}. */
    public static final int SLOT_COUNT = SpiritBoneSlot.values().length;

    /** Lokasi tempat stack tulang ditemukan di inventaris pemain. */
    private static final int LOC_ITEMS = 0;
    private static final int LOC_OFFHAND = 1;
    private static final int LOC_ARMOR = 2;

    /** Item tulang dari id registry-nya, atau null kalau bukan tulang atau tidak dikenal. */
    public static SpiritBoneItem boneById(String itemId) {
        if (itemId == null || itemId.isEmpty()) return null;
        Item item = net.minecraft.core.registries.BuiltInRegistries.ITEM
                .get(new ResourceLocation(SoulLandMod.MODID, itemId));
        return item instanceof SpiritBoneItem bone ? bone : null;
    }

    /** Susunan tulang terpasang: slot ke tier. Slot kosong tidak ikut. */
    public static Map<SpiritBoneSlot, SpiritBoneTier> equipped(Player player) {
        Map<SpiritBoneSlot, SpiritBoneTier> equipped = new EnumMap<>(SpiritBoneSlot.class);
        if (player == null) return equipped;
        CultivationCapability.get(player).ifPresent(cap -> {
            for (String itemId : cap.getBones()) {
                SpiritBoneItem bone = boneById(itemId);
                if (bone == null) continue;
                equipped.put(bone.slot(), bone.tier());
            }
        });
        return equipped;
    }

    /** Total bonus stat dari tulang yang terpasang. */
    public static BoneStats stats(Player player) {
        return BoneStatsCalculator.aggregate(equipped(player));
    }

    /** Bonus set yang aktif dari tulang yang terpasang. */
    public static BoneSetBonus setBonus(Player player) {
        return BoneStatsCalculator.setBonus(equipped(player));
    }

    /** Id item tulang yang terpasang pada sebuah slot. */
    public static String equippedId(Player player, SpiritBoneSlot slot) {
        if (player == null || slot == null) return "";
        return CultivationCapability.get(player)
                .map(cap -> {
                    String[] bones = cap.getBones();
                    int index = slot.index();
                    return index >= 0 && index < bones.length ? bones[index] : "";
                })
                .orElse("");
    }

    /**
     * Memasang tulang ke slotnya, dengan stack asal dicari di tas.
     *
     * @return pesan status untuk pemain, sudah diterjemahkan
     */
    public static Status equip(ServerPlayer player, SpiritBoneSlot slot, String itemId) {
        return equip(player, slot, itemId, null);
    }

    /**
     * Memasang tulang ke slotnya.
     *
     * @param preferred stack yang sedang dipegang pemain, atau null kalau
     *                  tulang dikirim lewat layar dan harus dicari sendiri
     * @return pesan status untuk pemain, sudah diterjemahkan
     */
    public static Status equip(ServerPlayer player, SpiritBoneSlot slot, String itemId, ItemStack preferred) {
        String language = CultivationManager.messageLanguage(player);
        SpiritBoneItem bone = boneById(itemId);
        if (bone == null) {
            return Status.error(SoulLang.translate(language, "message.soulland.bone.unknown",
                    "Tulang itu tidak dikenali."));
        }
        if (bone.slot() != slot) {
            return Status.error(SoulLang.translate(language, "message.soulland.bone.wrong_slot",
                    "%s tidak bisa dipasang di slot %s.",
                    SoulLang.translate(language, "bone.soulland.slot." + bone.slot().id(), bone.slot().id()),
                    SoulLang.translate(language, "bone.soulland.slot." + slot.id(), slot.id())));
        }

        ICultivation cap = CultivationCapability.get(player).orElse(null);
        if (cap == null) {
            return Status.error(SoulLang.translate(language, "message.soulland.bone.unknown",
                    "Tulang itu tidak dikenali."));
        }
        if (cap.getRings() < bone.tier().requiredRings()) {
            return Status.error(SoulLang.translate(language, "message.soulland.bone.need_rings",
                    "%s butuh %s spirit ring, kamu punya %s.",
                    SoulLang.translate(language, "bone.soulland.tier." + bone.tier().id(), bone.tier().id()),
                    bone.tier().requiredRings(), cap.getRings()));
        }

        Found found = locate(player, bone, preferred);
        if (found == null) {
            return Status.error(SoulLang.translate(language, "message.soulland.bone.not_carried",
                    "Kamu tidak membawa tulang itu."));
        }
        ItemStack source = found.stack();

        String[] bones = cap.getBones();
        String previous = bones[slot.index()];
        if (previous.equals(itemId)) {
            return Status.success(SoulLang.translate(language, "message.soulland.bone.already",
                    "Tulang itu sudah terpasang di slot itu."));
        }

        SpiritBoneItem old = previous.isEmpty() ? null : boneById(previous);
        boolean freesMainSlot = source.getCount() == 1 && found.location() == LOC_ITEMS;
        if (old != null && !freesMainSlot && !canStore(player, new ItemStack(old))) {
            return Status.error(SoulLang.translate(language, "message.soulland.bone.inventory_full",
                    "Tas penuh, %s tidak bisa dilepas.",
                    SoulLang.translate(language, "bone.soulland.tier." + old.tier().id(), old.tier().id())));
        }

        source.shrink(1);
        if (old != null) {
            ItemStack returned = new ItemStack(old);
            if (!player.getInventory().add(returned)) {
                dropAt(player, returned);
            }
        }

        bones[slot.index()] = itemId;
        cap.setBones(bones);
        CultivationManager.applyStats(player);
        CultivationManager.sync(player);
        sync(player);

        return Status.success(SoulLang.translate(language, "message.soulland.bone.equipped",
                "Tulang %s dipasang di slot %s.",
                SoulLang.translate(language, "bone.soulland.tier." + bone.tier().id(), bone.tier().id()),
                SoulLang.translate(language, "bone.soulland.slot." + slot.id(), slot.id())));
    }

    /**
     * Melepas tulang dari slotnya dan mengembalikannya ke tas.
     *
     * @return pesan status untuk pemain, sudah diterjemahkan
     */
    public static Status unequip(ServerPlayer player, SpiritBoneSlot slot) {
        String language = CultivationManager.messageLanguage(player);
        ICultivation cap = CultivationCapability.get(player).orElse(null);
        if (cap == null) return Status.none();

        String[] bones = cap.getBones();
        String itemId = bones[slot.index()];
        if (itemId == null || itemId.isEmpty()) {
            return Status.error(SoulLang.translate(language, "message.soulland.bone.empty_slot",
                    "Slot itu memang kosong."));
        }

        SpiritBoneItem bone = boneById(itemId);
        if (bone != null && !canStore(player, new ItemStack(bone))) {
            return Status.error(SoulLang.translate(language, "message.soulland.bone.inventory_full",
                    "Tas penuh, tulang itu tidak bisa dilepas."));
        }

        bones[slot.index()] = "";
        cap.setBones(bones);

        if (bone != null) {
            ItemStack returned = new ItemStack(bone);
            if (!player.getInventory().add(returned)) {
                dropAt(player, returned);
            }
        }

        CultivationManager.applyStats(player);
        CultivationManager.sync(player);
        sync(player);
        return Status.success(SoulLang.translate(language, "message.soulland.bone.removed",
                "Tulang dilepas dari slot %s.",
                SoulLang.translate(language, "bone.soulland.slot." + slot.id(), slot.id())));
    }

    /** Mengirim susunan tulang terpasang ke client pemain. */
    public static void sync(ServerPlayer player) {
        sync(player, Status.none());
    }

    /** Mengirim susunan tulang terpasang beserta pesan status ke client. */
    public static void sync(ServerPlayer player, Status status) {
        String[] bones = new String[SLOT_COUNT];
        CultivationCapability.get(player).ifPresent(cap -> {
            String[] stored = cap.getBones();
            for (int index = 0; index < bones.length && index < stored.length; index++) {
                bones[index] = stored[index] == null ? "" : stored[index];
            }
        });
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new SyncBonesPacket(bones, status.text(), status.ok()));
    }

    /** Membangun stack tulang untuk tier dan slot tertentu. */
    public static ItemStack stack(SpiritBoneTier tier, SpiritBoneSlot slot) {
        var registry = ModItems.bone(tier, slot);
        return registry == null ? ItemStack.EMPTY : new ItemStack(registry.get());
    }

    // Inventaris

    /**
     * Mencari stack tulang yang akan dipakai.
     *
     * @param preferred stack yang sedang dipegang, boleh null
     * @return lokasi dan stack-nya, atau null kalau tidak ada
     */
    private static Found locate(Player player, SpiritBoneItem bone, ItemStack preferred) {
        if (preferred != null && !preferred.isEmpty() && preferred.is(bone)) {
            return new Found(locationOf(player, preferred), preferred);
        }
        return findInInventory(player, bone);
    }

    /**
     * Mencari stack tulang di inventaris pemain.
     *
     * @return lokasi dan stack-nya, atau null kalau tidak ada
     */
    private static Found findInInventory(Player player, SpiritBoneItem bone) {
        Inventory inventory = player.getInventory();
        for (int index = 0; index < inventory.items.size(); index++) {
            ItemStack stack = inventory.items.get(index);
            if (!stack.isEmpty() && stack.is(bone)) {
                return new Found(LOC_ITEMS, stack);
            }
        }
        for (int index = 0; index < inventory.offhand.size(); index++) {
            ItemStack stack = inventory.offhand.get(index);
            if (!stack.isEmpty() && stack.is(bone)) {
                return new Found(LOC_OFFHAND, stack);
            }
        }
        for (int index = 0; index < inventory.armor.size(); index++) {
            ItemStack stack = inventory.armor.get(index);
            if (!stack.isEmpty() && stack.is(bone)) {
                return new Found(LOC_ARMOR, stack);
            }
        }
        return null;
    }

    /** Letak sebuah stack di tas pemain, atau -1 kalau tidak ada di tas. */
    private static int locationOf(Player player, ItemStack stack) {
        Inventory inventory = player.getInventory();
        for (int index = 0; index < inventory.items.size(); index++) {
            if (inventory.items.get(index) == stack) return LOC_ITEMS;
        }
        for (int index = 0; index < inventory.offhand.size(); index++) {
            if (inventory.offhand.get(index) == stack) return LOC_OFFHAND;
        }
        for (int index = 0; index < inventory.armor.size(); index++) {
            if (inventory.armor.get(index) == stack) return LOC_ARMOR;
        }
        return -1;
    }

    /** Stack tulang yang ditemukan beserta tempatnya. */
    private record Found(int location, ItemStack stack) {
    }

    /**
     * Hasil sebuah aksi tulang: pesan siap tampil dan apakah berhasil.
     *
     * @param text pesan status, sudah diterjemahkan
     * @param ok true kalau aksi berhasil
     */
    public record Status(String text, boolean ok) {
        public static Status none() {
            return new Status("", true);
        }

        public static Status success(String text) {
            return new Status(text, true);
        }

        public static Status error(String text) {
            return new Status(text, false);
        }
    }

    /** True kalau stack itu muat di tas utama pemain. */
    private static boolean canStore(Player player, ItemStack stack) {
        if (stack == null || stack.isEmpty()) return true;
        NonNullList<ItemStack> items = player.getInventory().items;
        for (ItemStack existing : items) {
            if (existing.isEmpty()) return true;
        }
        for (ItemStack existing : items) {
            if (existing.isEmpty()) continue;
            if (ItemStack.isSameItemSameTags(existing, stack)
                    && existing.getCount() + stack.getCount() <= existing.getMaxStackSize()) {
                return true;
            }
        }
        return false;
    }

    private static void dropAt(ServerPlayer player, ItemStack stack) {
        if (stack == null || stack.isEmpty()) return;
        ItemEntity entity = new ItemEntity(player.level(), player.getX(), player.getY() + 0.5, player.getZ(), stack);
        entity.setDefaultPickUpDelay();
        player.level().addFreshEntity(entity);
    }
}
