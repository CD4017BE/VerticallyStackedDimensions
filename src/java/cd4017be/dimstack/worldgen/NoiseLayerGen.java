package cd4017be.dimstack.worldgen;

import java.util.Arrays;

import cd4017be.dimstack.api.TerrainGeneration;
import cd4017be.dimstack.api.gen.ITerrainGenerator;
import cd4017be.dimstack.api.util.BlockPredicate;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraftforge.common.util.Constants.NBT;


/**
 * @author CD4017BE
 *
 */
public class NoiseLayerGen implements ITerrainGenerator {
	public static final String ID = "noiseLayer";

	private final int minY, maxY, source;
	private final IBlockState[] blocks;
	private final float[] levels;
	private final float gradient;

	public NoiseLayerGen(NBTTagCompound nbt) {
		this.minY = nbt.getByte("minY") & 0xff;
		this.maxY = (nbt.getByte("maxY") & 0xff) + 1;
		this.source = nbt.getByte("Ngen") & 0xff;
		this.gradient = nbt.getFloat("grad");
		NBTTagList list = nbt.getTagList("blocks", NBT.TAG_COMPOUND);
		int n = list.tagCount();
		blocks = new IBlockState[n];
		levels = new float[n - 1];
		for (int i = 0; i < n; i++) {
			NBTTagCompound tag = list.getCompoundTagAt(i);
			if (i < n-1) levels[i] = tag.getFloat("<=");
			String s = tag.getString("b");
			blocks[i] = s.isEmpty() ? null : BlockPredicate.parse(s);
		}
	}

	public NoiseLayerGen(IBlockState[] blocks, float[] levels, float gradient, int minY, int maxY, int source) {
		if (blocks.length != levels.length + 1)
			throw new IllegalArgumentException("There must be exactly one discriminator value inbetween each Block entry!");
		float f = Float.NEGATIVE_INFINITY;
		for (float l : levels)
			if (l > f) f = l;
			else throw new IllegalArgumentException("Discriminatior values must be sorted in ascending order!");
		this.minY = minY;
		this.maxY = maxY;
		this.source = source;
		this.blocks = blocks;
		this.levels = levels;
		this.gradient = gradient;
	}

	@Override
	public String getRegistryName() {
		return ID;
	}

	@Override
	public NBTTagCompound writeNBT() {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setByte("minY", (byte)minY);
		nbt.setByte("maxY", (byte)(maxY - 1));
		nbt.setByte("Ngen", (byte)source);
		nbt.setFloat("grad", gradient);
		NBTTagList list = new NBTTagList();
		for (int i = 0, n = blocks.length; i < n; i++) {
			NBTTagCompound tag = new NBTTagCompound();
			if (i < n-1)
				tag.setFloat("<=", levels[i]);
			if (blocks[i] != null)
				tag.setString("b", BlockPredicate.serialize(blocks[i]));
			list.appendTag(tag);
		}
		nbt.setTag("blocks", list);
		return nbt;
	}

	@Override
	public void initNoise(TerrainGeneration cfg) {
		cfg.noiseFields[source].provideRange(minY, maxY);
	}

	@Override
	public void generate(World world, ChunkPrimer cp, int cx, int cz, TerrainGeneration cfg) {
		final IBlockState[] blocks = this.blocks;
		final float[] levels = this.levels;
		final float a = gradient, b = 1F / (float)(maxY - minY);
		
		cfg.noiseFields[source].generate(minY, maxY, (x, y, z, f)-> {
			int i = Arrays.binarySearch(levels, (float)f * a + (float)y * b);
			IBlockState state = blocks[i < 0 ? -1 - i : i];
			if (state != null) cp.setBlockState(x, y, z, state);
		});
	}

}
