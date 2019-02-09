package cd4017be.dimstack.api;

import java.util.ArrayList;
import java.util.Random;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.Constants.NBT;


/**
 * @author CD4017BE
 *
 */
public class OreGeneration implements IDimensionSettings {

	public static final String[] TYPES = {"e", "c", "g"};
	public static int getType(String name) {
		for (int i = 0; i < TYPES.length; i++)
			if (name.startsWith(TYPES[i]))
				return i;
		return -1;
	}

	public final ArrayList<OreGenerator> generators = new ArrayList<>();


	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagList list = new NBTTagList();
		for (OreGenerator gen : generators)
			list.appendTag(gen.writeNBT());
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setTag("gen", list);
		return nbt;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		for (NBTBase tag : nbt.getTagList("gen", NBT.TAG_COMPOUND)) {
			NBTTagCompound ctag = (NBTTagCompound)tag;
			switch(ctag.getByte("type")) {
			case OreGenEven.ID: generators.add(new OreGenEven(ctag)); break;
			case OreGenCentered.ID: generators.add(new OreGenCentered(ctag)); break;
			case OreGenGaussian.ID: generators.add(new OreGenGaussian(ctag)); break;
			}
		}
	}

	public static abstract class OreGenerator {

		final float veins;
		final int size;
		final IBlockState ore;
		final BlockPredicate target;

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

		protected int veins(Random rand) {
			return MathHelper.floor(veins + rand.nextFloat());
		}

		public abstract void generate(Chunk chunk, Random rand);

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

	public static class OreGenEven extends OreGenerator {
		public static final byte ID = 0;

		final int baseY, height;

		public OreGenEven(NBTTagCompound tag) {
			super(tag);
			this.baseY = tag.getShort("minY");
			this.height = tag.getShort("maxY") - baseY;
		}

		public OreGenEven(IBlockState state, int numB, float veins, BlockPredicate target, int minH, int maxH) {
			super(state, numB, veins, target);
			this.baseY = minH < maxH ? minH : maxH;
			this.height = minH < maxH ? maxH - minH : minH - maxH;
		}

		public void generate(Chunk chunk, Random rand) {
			int x = chunk.x << 4, z = chunk.z << 4;
			for (int n = veins(rand); n > 0; n--)
				genOreVein(chunk.getWorld(), x + rand.nextInt(16), baseY + rand.nextInt(height), z + rand.nextInt(16), rand);
		}

		@Override
		public NBTTagCompound writeNBT() {
			NBTTagCompound nbt = super.writeNBT();
			nbt.setByte("id", ID);
			nbt.setShort("minY", (short)baseY);
			nbt.setShort("maxY", (short)(baseY + height));
			return nbt;
		}

	}

	public static class OreGenCentered extends OreGenerator {
		public static final byte ID = 1;

		final int baseY;
		final float min, max;

		public OreGenCentered(NBTTagCompound tag) {
			super(tag);
			this.baseY = tag.getShort("mainY");
			this.min = baseY - tag.getShort("minY");
			this.max = tag.getShort("maxY") - baseY;
		}

		public OreGenCentered(IBlockState state, int numB, float veins, BlockPredicate target, int minH, int mainH, int maxH) {
			super(state, numB, veins, target);
			this.min = mainH - minH;
			this.max = maxH - mainH;
			this.baseY = mainH;
		}

		public void generate(Chunk chunk, Random rand) {
			int x = chunk.x << 4, z = chunk.z << 4;
			boolean side = max < min;
			int r;
			float f;
			for (int n = veins(rand); n > 0; n--) {
				r = rand.nextInt();//split into: x[0...15], z[0...15], f[-4095...4095] more dense towards 0
				f = (float)((r & 0xfff) + (r >> 12 & 0xfff) - 4095) / 4095F;
				if (side) {
					f *= min;
					if (f > max) f = max - (f - max) / (min - max) * (min + max);
				} else {
					f *= max;
					if (f > min) f = min - (f - min) / (max - min) * (max + min);
					f = -f;
				}
				genOreVein(chunk.getWorld(), x + (r >> 24 & 0xf), baseY + MathHelper.floor(f), z + (r >> 28 & 0xf), rand);
			}
		}

		@Override
		public NBTTagCompound writeNBT() {
			NBTTagCompound nbt = super.writeNBT();
			nbt.setByte("id", ID);
			nbt.setShort("mainY", (short)baseY);
			nbt.setShort("minY", (short)(baseY - min));
			nbt.setShort("maxY", (short)(baseY + max));
			return nbt;
		}

	}

	public static class OreGenGaussian extends OreGenerator {

		public static final byte ID = 2;

		final float center, var;

		public OreGenGaussian(NBTTagCompound tag) {
			super(tag);
			this.center = tag.getFloat("mainY");
			this.var = tag.getFloat("devY");
		}

		public OreGenGaussian(IBlockState ore, int size, float veins, BlockPredicate target, float center, float var) {
			super(ore, size, veins, target);
			this.center = center;
			this.var = var;
		}

		@Override
		public void generate(Chunk chunk, Random rand) {
			int x = chunk.x << 4, z = chunk.z << 4;
			for (int n = veins(rand); n > 0; n--)
				genOreVein(chunk.getWorld(), x + rand.nextInt(16), MathHelper.floor(center + var * (float)rand.nextGaussian()), z + rand.nextInt(16), rand);
		}

		@Override
		public NBTTagCompound writeNBT() {
			NBTTagCompound nbt = super.writeNBT();
			nbt.setFloat("mainY", center);
			nbt.setFloat("devY", var);
			return nbt;
		}

	}

}
