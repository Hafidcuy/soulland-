package com.soulland.net;

import com.soulland.alchemy.AlchemyCrafting;
import com.soulland.alchemy.AlchemyRecipe;
import com.soulland.alchemy.AlchemyRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

/**
 * Client -> server: pemain minta meracik satu resep.
 *
 * <p>Server yang menentukan boleh atau tidak. Permintaan hanya diterima
 * kalau pemain masih berdiri di dekat stasiun alchemy yang membuka layar,
 * bahan lengkap, dan resepnya dikenal. Jawaban selalu dikirim kembali, jadi
 * layar tidak pernah menggantung.
 */
public record CraftAlchemyPacket(String recipeId, BlockPos stationPos) {

    /** Batas panjang id resep yang diterima dari client. */
    private static final int MAX_ID_LENGTH = 32;

    /** Jarak maksimum pemain dari stasiun untuk boleh meracik. */
    private static final int MAX_DISTANCE = 8;

    public static void encode(CraftAlchemyPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.recipeId() == null ? "" : msg.recipeId(), MAX_ID_LENGTH);
        buf.writeBlockPos(msg.stationPos());
    }

    public static CraftAlchemyPacket decode(FriendlyByteBuf buf) {
        return new CraftAlchemyPacket(buf.readUtf(MAX_ID_LENGTH), buf.readBlockPos());
    }

    public static void handle(CraftAlchemyPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            AlchemyRecipe recipe = AlchemyRecipes.byId(msg.recipeId());
            boolean crafted = AlchemyCrafting.craft(player, recipe, msg.stationPos(), MAX_DISTANCE);
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                    new AlchemyResultPacket(crafted, recipe == null ? "" : recipe.id()));
        });
        ctx.get().setPacketHandled(true);
    }
}
