package cd4017be.dimstack.core;

import java.util.List;
import java.util.Random;
import net.minecraft.block.BlockFalling;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.NoiseGeneratorOctaves;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.event.terraingen.InitNoiseGensEvent.Context;
import net.minecraftforge.event.terraingen.TerrainGen;

/** @author CD4017BE */
public class ChunkGeneratorVoid implements IChunkGenerator {

	private final Random rand;
	private final World world;

	public ChunkGeneratorVoid(World world, long seed) {
		this.world = world;
		this.rand = new Random(seed);
		NoiseGeneratorOctaves minLimNoise = new NoiseGeneratorOctaves(this.rand, 16);
		NoiseGeneratorOctaves maxLimNoise = new NoiseGeneratorOctaves(this.rand, 16);
		NoiseGeneratorOctaves mainNoise = new NoiseGeneratorOctaves(this.rand, 8);
		NoiseGeneratorOctaves scaleNoise = new NoiseGeneratorOctaves(this.rand, 10);
		NoiseGeneratorOctaves depthNoise = new NoiseGeneratorOctaves(this.rand, 16);
		TerrainGen.getModdedNoiseGenerators(
			world, this.rand, new Context(minLimNoise, maxLimNoise, mainNoise, scaleNoise, depthNoise)
		);
	}

	@Override
	public Chunk generateChunk(int x, int z) {
		this.rand.setSeed(x * 341873128712L + z * 132897987541L);
		ChunkPrimer primer = new ChunkPrimer();
		ForgeEventFactory.onReplaceBiomeBlocks(this, x, z, primer, this.world);
		Chunk chunk = new Chunk(this.world, primer, x, z);
		Biome[] biomes = this.world.getBiomeProvider().getBiomes((Biome[])null, x * 16, z * 16, 16, 16);
		byte[] array = chunk.getBiomeArray();
		for(int i = 0; i < array.length; ++i)
			array[i] = (byte)Biome.getIdForBiome(biomes[i]);
		if(world.provider.shouldClientCheckLighting())
			chunk.resetRelightChecks();
		if(world.provider.hasSkyLight())
			chunk.generateSkylightMap();
		return chunk;
	}

	@Override
	public void populate(int x, int z) {
		BlockFalling.fallInstantly = true;
		ForgeEventFactory.onChunkPopulate(true, this, this.world, this.rand, x, z, false);
		BlockPos pos = new BlockPos(x << 4, 0, z << 4);
		Biome biome = this.world.getBiome(pos.add(16, 0, 16));
		ChunkPos chunkpos = new ChunkPos(x, z);
		ForgeEventFactory.onChunkPopulate(false, this, this.world, this.rand, x, z, false);

		MinecraftForge.EVENT_BUS.post(new DecorateBiomeEvent.Pre(this.world, this.rand, chunkpos));
		biome.decorate(this.world, this.rand, pos);
		// MinecraftForge.EVENT_BUS.post(new DecorateBiomeEvent.Post(this.world,
		// this.rand, pos));
		BlockFalling.fallInstantly = false;
	}

	@Override
	public boolean generateStructures(Chunk chunkIn, int x, int z) {
		return false;
	}

	@Override
	public List<SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
		Biome biome = this.world.getBiome(pos);
		return biome.getSpawnableList(creatureType);
	}

	@Override
	public BlockPos getNearestStructurePos(
		World worldIn, String structureName, BlockPos position, boolean findUnexplored
	) {
		return null;
	}

	@Override
	public void recreateStructures(Chunk chunkIn, int x, int z) {}

	@Override
	public boolean isInsideStructure(World worldIn, String structureName, BlockPos pos) {
		return false;
	}

}
