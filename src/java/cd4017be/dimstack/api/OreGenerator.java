package cd4017be.dimstack.api;

import java.util.Random;

import cd4017be.dimstack.api.util.BlockPredicate;
import cd4017be.dimstack.api.util.ICfgListEntry;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.Constants.NBT;

/**
 * Super type of {@link OreGeneration} entries. Generates a specific ore type in a dimension.
 * @author CD4017BE
 */
public abstract class OreGenerator implements ICfgListEntry {

	private final float veins;
	/** number of blocks per vein */
	protected final int size;
	/** block state of the ore to generate */
	protected final IBlockState ore;
	/** filter for which blocks to generate in */
	protected final BlockPredicate target;

	protected OreGenerator(NBTTagCompound tag) {
		this.veins = tag.getFloat("vpc");
		this.size = tag.getShort("bpv");
		this.target = BlockPredicate.loadNBT(tag.getTagList("target", NBT.TAG_STRING));
		this.ore = BlockPredicate.parse(tag.getString("ore"));
	}

	protected OreGenerator(IBlockState ore, int size, float veins, BlockPredicate target) {
		this.size = size;
		this.ore = ore;
		this.target = target;
		this.veins = veins;
	}

	public NBTTagCompound writeNBT() {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setFloat("vpc", veins);
		nbt.setShort("bpv", (short)size);
		nbt.setTag("target", target.writeNBT());
		nbt.setString("ore", BlockPredicate.serialize(ore));
		return nbt;
	}

	/** number of veins to generate in current chunk */
	protected int veins(Random rand) {
		return MathHelper.floor(veins + rand.nextFloat());
	}

	/**
	 * called during chunk population to generates the ores in the world
	 * @param chunk world and chunk position to generate for
	 * @param rand world gen random
	 */
	public abstract void generate(Chunk chunk, Random rand);

	/**
	 * generates a basic ore vein of this generator's ore block, vein size, and target block filter
	 * @param world
	 * @param x center X
	 * @param y center Y
	 * @param z center Z
	 * @param rand
	 */
	protected void genOreVein(World world, int x, int y, int z, Random rand) {
		float f = rand.nextFloat() * (float)Math.PI, sin = MathHelper.sin(f), cos = MathHelper.cos(f);
		double x0 = (double)((float)(x + 8) + sin * (float)size / 8.0F);
		double x1 = (double)((float)(x + 8) - sin * (float)size / 8.0F);
		double z0 = (double)((float)(z + 8) + cos * (float)size / 8.0F);
		double z1 = (double)((float)(z + 8) - cos * (float)size / 8.0F);
		double y0 = (double)(y + rand.nextInt(3) - 2);
		double y1 = (double)(y + rand.nextInt(3) - 2);
		for (int i = 0; i < size; ++i) {
			float f1 = (float)i / (float)size;
			double x2 = x0 + (x1 - x0) * (double)f1;
			double y2 = y0 + (y1 - y0) * (double)f1;
			double z2 = z0 + (z1 - z0) * (double)f1;
			double d9 = rand.nextDouble() * (double)size / 16.0D;
			double d10 = (double)(MathHelper.sin((float)Math.PI * f1) + 1.0F) * d9 + 1.0D;
			double d11 = (double)(MathHelper.sin((float)Math.PI * f1) + 1.0F) * d9 + 1.0D;
			int j = MathHelper.floor(x2 - d10 / 2.0D);
			int k = MathHelper.floor(y2 - d11 / 2.0D);
			int l = MathHelper.floor(z2 - d10 / 2.0D);
			int i1 = MathHelper.floor(x2 + d10 / 2.0D);
			int j1 = MathHelper.floor(y2 + d11 / 2.0D);
			int k1 = MathHelper.floor(z2 + d10 / 2.0D);
			MutableBlockPos pos = new MutableBlockPos();
			for (int bx = j; bx <= i1; ++bx) {
				double d12 = ((double)bx + 0.5D - x2) / (d10 / 2.0D);
				if (d12 * d12 < 1.0D) {
					for (int by = k; by <= j1; ++by) {
						double d13 = ((double)by + 0.5D - y2) / (d11 / 2.0D);
						if (d12 * d12 + d13 * d13 < 1.0D) {
							for (int bz = l; bz <= k1; ++bz) {
								double d14 = ((double)bz + 0.5D - z2) / (d10 / 2.0D);
								if (d12 * d12 + d13 * d13 + d14 * d14 < 1.0D) {
									pos.setPos(bx, by, bz);
									IBlockState state = world.getBlockState(pos);
									if (state.getBlock().isReplaceableOreGen(state, world, pos, target::test))
										world.setBlockState(pos, ore, 2);
								}
							}
						}
					}
				}
			}
		}
	}

}