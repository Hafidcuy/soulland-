package com.soulland.block;

import com.soulland.SoulLandMod;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registrasi blok mod.
 *
 * <p>Stasiun alchemy memakai blok sederhana tanpa block entity. Resep dan
 * stok bahan tidak disimpan di dunia, jadi memecah blok tidak pernah
 * menghilangkan bahan yang sudah diracik.
 */
public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, SoulLandMod.MODID);

    /** Stasiun peracikan. */
    public static final RegistryObject<Block> ALCHEMY_STATION = BLOCKS.register("alchemy_station",
            () -> new AlchemyStationBlock(BlockBehaviour.Properties.copy(Blocks.STONE).strength(3.0f)));
}
