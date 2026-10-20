package com.soulland.compat;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;

/**
 * Jembatan ke Curios.
 *
 * <p>Curios hanya jadi dependensi opsional, jadi kelasnya dipanggil lewat refleksi. Kalau
 * Curios terpasang, sembilan spirit ring disimpan di slot {@code spirit_ring} sesuai
 * {@code config/curios-common.toml} milik pack. Kalau tidak, ring tetap jalan seperti biasa
 * di inventory.
 */
public final class CuriosBridge {
    private CuriosBridge() {}

    public static final String SLOT_TYPE = "spirit_ring";
    private static final int SLOT_COUNT = 9;

    private static final boolean ENABLED = ModList.get().isLoaded("curios");
    private static Method getCapability;
    private static Method getCurios;
    private static Method getStacks;

    static {
        if (ENABLED) {
        try {
            Class<?> capabilityClass = Class.forName("top.theillusivec4.curios.api.CuriosCapability");
            getCapability = capabilityClass.getMethod("get", Player.class);
            Class<?> curiosInterface = Class.forName("top.theillusivec4.curios.api.ICuriosCapability");
            getCurios = curiosInterface.getMethod("getCurios");
            Class<?> handlerClass = Class.forName("top.theillusivec4.curios.api.ICurioStackHandler");
            getStacks = handlerClass.getMethod("getStacks");
        } catch (Exception e) {
            getCapability = null;
        }
        }
    }

    public static boolean available() {
        return ENABLED && getCapability != null;
    }

    /** Taruh ring ke slot Curios pertama yang kosong. False kalau tidak ada slot kosong. */
    @SuppressWarnings("unchecked")
    public static boolean storeRing(Player player, ItemStack stack) {
        if (!available()) return false;
        try {
            Object curios = getCapability.invoke(null, player);
            if (!(curios instanceof Optional<?> optional) || optional.isEmpty()) return false;
            Object capability = optional.get();
            Object map = getCurios.invoke(capability);
            if (!(map instanceof Map<?, ?> curiosMap)) return false;
            Object handler = curiosMap.get(SLOT_TYPE);
            if (handler == null) return false;
            Object stacks = getStacks.invoke(handler);
            if (!(stacks instanceof net.minecraftforge.items.IItemHandlerModifiable itemHandler)) return false;
            for (int slot = 0; slot < SLOT_COUNT; slot++) {
                if (itemHandler.getStackInSlot(slot).isEmpty()) {
                    itemHandler.setStackInSlot(slot, stack);
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public static ItemStack ringInSlot(Player player, int slot) {
        if (!available()) return ItemStack.EMPTY;
        try {
            Object curios = getCapability.invoke(null, player);
            if (!(curios instanceof Optional<?> optional) || optional.isEmpty()) return ItemStack.EMPTY;
            Object capability = optional.get();
            Object map = getCurios.invoke(capability);
            if (!(map instanceof Map<?, ?> curiosMap)) return ItemStack.EMPTY;
            Object handler = curiosMap.get(SLOT_TYPE);
            if (handler == null) return ItemStack.EMPTY;
            Object stacks = getStacks.invoke(handler);
            if (!(stacks instanceof net.minecraftforge.items.IItemHandlerModifiable itemHandler)) return ItemStack.EMPTY;
            if (slot < 0 || slot >= SLOT_COUNT) return ItemStack.EMPTY;
            return itemHandler.getStackInSlot(slot);
        } catch (Exception e) {
            return ItemStack.EMPTY;
        }
    }

    public static int slotCount() {
        return SLOT_COUNT;
    }
}
