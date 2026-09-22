package net.rasanovum.rosetta.util;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
//? if >=1.19.3
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.core.BlockPos;

import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.levelgen.RandomState;
import java.util.List;
import java.util.Optional;

/** Biome sampling helpers. */
public final class BiomeCompat {
    private BiomeCompat() {}

    public static Climate.Sampler climateSampler(RandomState state) {
        //? if >=26.3 {
        /*return state.createClimateSampler(net.minecraft.world.level.levelgen.densityfunction.SamplerContext.EMPTY_UNCACHED);
        *///?} else {
        return state.sampler();
        //?}
    }

    @FunctionalInterface
    public interface Sampler {
        Holder<Biome> getNoiseBiome(int x, int y, int z);
    }

    /** Coordinates are in quart-biome units. Reuse the resolver for repeated samples. */
    public static Sampler resolver(BiomeSource source,
            Climate.Sampler sampler) {
        //? if >=26.3 {
        /*return source.createResolver(sampler)::getNoiseBiome;
        *///?} else {
        return (x, y, z) -> source.getNoiseBiome(x, y, z, sampler);
        //?}
    }

    public static Registry<Biome> registry(MinecraftServer server) {
        //? if >=26.1 {
        /*return server.registryAccess().lookupOrThrow(Registries.BIOME);
        *///?} else {
        //? if <1.19.3 {
        /*return server.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY);
        *///?} else {
        return server.registryAccess().registryOrThrow(Registries.BIOME);
        //?}
        //?}
    }

    public static Registry<Biome> registry(ServerLevel level) {
        //? if >=26.1 {
        /*return level.registryAccess().lookupOrThrow(Registries.BIOME);
        *///?} else {
        //? if <1.19.3 {
        /*return level.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY);
        *///?} else {
        return level.registryAccess().registryOrThrow(Registries.BIOME);
        //?}
        //?}
    }

    public static Optional<Holder.Reference<Biome>> holder(Registry<Biome> registry, ResourceLocation location) {
        //? if >=26.1 {
        /*return registry.get(location);
        *///?} else {
        //? if <1.19.3 {
        /*return registry.getHolder(ResourceKey.create(Registry.BIOME_REGISTRY, location))
                .filter(holder -> holder instanceof Holder.Reference<Biome>)
                .map(holder -> (Holder.Reference<Biome>) holder);
        *///?} else {
        return registry.getHolder(ResourceKey.create(Registries.BIOME, location));
        //?}
        //?}
    }

    public static List<Holder<Biome>> holders(Registry<Biome> registry) {
        //? if >=26.1 {
        /*return registry.entrySet().stream().map(entry -> registry.wrapAsHolder(entry.getValue())).toList();
        *///?} else {
        return registry.holders().map(holder -> (Holder<Biome>) holder).toList();
        //?}
    }

    public static ProtoChunk createProtoChunk(ServerLevel level) {
        //? if >=26.1 {
        /*return new ProtoChunk(new ChunkPos(0, 0), UpgradeData.EMPTY, level, level.palettedContainerFactory(), null);
        *///?} else {
        return new ProtoChunk(new ChunkPos(0, 0), UpgradeData.EMPTY, level, registry(level), null);
        //?}
    }

    public static void setBlockState(ProtoChunk chunk, BlockPos pos, BlockState state) {
        //? if >=26.1 {
        /*chunk.setBlockState(pos, state, 0);
        *///?} else {
        chunk.setBlockState(pos, state, false);
        //?}
    }

    public static boolean coldEnoughToSnow(Biome biome, BlockPos pos, int seaLevel) {
        //? if >=26.1 {
        /*return biome.coldEnoughToSnow(pos, seaLevel);
        *///?} else {
        return biome.coldEnoughToSnow(pos);
        //?}
    }
}
