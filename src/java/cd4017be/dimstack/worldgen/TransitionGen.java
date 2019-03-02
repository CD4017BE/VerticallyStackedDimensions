package cd4017be.dimstack.worldgen;

import cd4017be.dimstack.api.TerrainGeneration;
import cd4017be.dimstack.api.gen.ITerrainGenerator;
import cd4017be.dimstack.api.util.BlockPlacer;
import cd4017be.dimstack.api.util.NoiseField;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;


/**
 * @author CD4017BE
 *
 */
public class TransitionGen implements ITerrainGenerator, BlockPlacer {

	private static final double NOISE_SCALE = 15.0;
	private final int minY, maxY;
	private final double centerY, scale;
	private final boolean top;
	private final IBlockState state;
	private ChunkPrimer cp;

	public TransitionGen(IBlockState block, int minY, int maxY, int ext, boolean top) {
		this.state = block;
		this.minY = minY;
		this.maxY = maxY;
		this.top = top;
		this.centerY = (double)(minY + maxY + (top ? ext : -ext)) / 2.0 - 0.5;
		double d = (double)(maxY - minY + ext) / 2.0;
		this.scale = NOISE_SCALE / d / d / d;
	}

	@Override
	public String getRegistryName() {
		return "";
	}

	@Override
	public NBTTagCompound writeNBT() {
		return null;
	}

	@Override
	public void initNoise(TerrainGeneration cfg) {
		NoiseField nf = cfg.noiseFields[0];
		nf.provideRange(minY, maxY);
	}

	@Override
	public void generate(IChunkGenerator gen, ChunkPrimer cp, int cx, int cz, TerrainGeneration cfg) {
		this.cp = cp;
		cfg.noiseFields[0].generate(minY, maxY, this);
		this.cp = null;
	}

	@Override
	public void place(int x, int y, int z, double f) {
		double dy = (double)y - centerY;
		dy *= dy * dy * scale;
		if (top ? dy > f : dy < f)
			cp.setBlockState(x, y, z, state);
	}

}
