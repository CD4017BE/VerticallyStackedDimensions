package cd4017be.dimstack.api.gen;

import cd4017be.dimstack.api.TerrainGeneration;
import cd4017be.dimstack.api.util.ICfgListEntry;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;

/**
 * ITerrainGenerators run during initial chunk generation just after the main IChunkGenerator.
 * @author CD4017BE
 */
public interface ITerrainGenerator extends ICfgListEntry {

	/**
	 * called during initial chunk generation to modify the terrain
	 * @param world the world
	 * @param cp the chunk primer that will generate the chunk
	 * @param cx chunk X-coordinate
	 * @param cz chunk Z-coordinate
	 * @param cfg the terrain generation data (contains random generator and noise fields)
	 */
	void generate(World world, ChunkPrimer cp, int cx, int cz, TerrainGeneration cfg);

	/**
	 * called during noise generator initialization event
	 * @param cfg Terrain generation settings
	 */
	default void initNoise(TerrainGeneration cfg) {}

}