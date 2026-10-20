package com.soulland.block;

import com.soulland.net.ModNetwork;
import com.soulland.net.OpenAlchemyPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.PacketDistributor;

/**
 * Stasiun alchemy.
 *
 * <p>Klik kanan membuka layar peracikan. Layar hanya menampilkan daftar resep
 * dan jumlah bahan yang dibawa pemain, sedangkan server yang memeriksa bahan
 * dan memotong stok, jadi tidak ada jalan untuk meracik tanpa bahan.
 */
public class AlchemyStationBlock extends Block {

    public AlchemyStationBlock(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (player instanceof ServerPlayer serverPlayer) {
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer),
                    new OpenAlchemyPacket(pos.immutable()));
        }
        return InteractionResult.CONSUME;
    }
}
