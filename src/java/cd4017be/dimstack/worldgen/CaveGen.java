package cd4017be.dimstack.worldgen;

import java.util.Random;
import cd4017be.dimstack.api.TerrainGeneration;
import cd4017be.dimstack.api.gen.ITerrainGenerator;
import cd4017be.dimstack.api.util.BlockPredicate;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.MapGenBase;
import net.minecraftforge.common.util.Constants.NBT;

/** @author CD4017BE */
public class CaveGen extends MapGenBase implements ITerrainGenerator {
	public static final String ID = "caves";

	public int minY = 0, maxY = 256, fillY = 10;
	public int genChance = 37, count = 15, bigChance = 26;
	public float vertShape = 1.0F, thickness = 1.0F, bigScale = 0.0F, roomSize = 6.0F;
	public IBlockState setBlock = Blocks.AIR.getDefaultState(), setFluid = setBlock;
	public BlockPredicate replace = new BlockPredicate();

	@Override
	public String getRegistryName() {
		return ID;
	}

	public CaveGen readNBT(NBTTagCompound nbt) {
		range = nbt.getByte("size");
		minY = nbt.getByte("minY") & 0xff;
		maxY = (nbt.getByte("maxY") & 0xff) + 1;
		fillY = nbt.getByte("fillY") & 0xff;
		genChance = (nbt.getByte("pGen") & 0xff) + 1;
		bigChance = (nbt.getByte("pSca") & 0xff) + 1;
		count = nbt.getByte("n") & 0xff;
		vertShape = nbt.getFloat("shp");
		thickness = nbt.getFloat("rad");
		roomSize = nbt.getFloat("room");
		bigScale = nbt.getFloat("sca");
		setBlock = BlockPredicate.parse(nbt.getString("block"));
		setFluid = BlockPredicate.parse(nbt.getString("fluid"));
		replace = BlockPredicate.loadNBT(nbt.getTagList("repl", NBT.TAG_STRING));
		return this;
	}

	@Override
	public NBTTagCompound writeNBT() {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setByte("size", (byte)range);
		nbt.setByte("minY", (byte)minY);
		nbt.setByte("maxY", (byte)(maxY - 1));
		nbt.setByte("fillY", (byte)fillY);
		nbt.setByte("pGen", (byte)(genChance - 1));
		nbt.setByte("pSca", (byte)(bigChance - 1));
		nbt.setByte("n", (byte)count);
		nbt.setFloat("shp", vertShape);
		nbt.setFloat("rad", thickness);
		nbt.setFloat("room", roomSize);
		nbt.setFloat("sca", bigScale);
		nbt.setString("block", BlockPredicate.serialize(setBlock));
		nbt.setString("fluid", BlockPredicate.serialize(setFluid));
		nbt.setTag("repl", replace.writeNBT());
		return nbt;
	}

	@Override
	public void generate(World world, ChunkPrimer cp, int cx, int cz, TerrainGeneration cfg) {
		generate(world, cx, cz, cp);
	}

	@Override
	protected void recursiveGenerate(World world, int cX, int cZ, int oX, int oZ, ChunkPrimer cp) {
		int n = rand.nextInt(rand.nextInt(rand.nextInt(count) + 1) + 1);
		if(rand.nextInt(256) >= genChance) n = 0;

		for(int j = 0; j < n; ++j) {
			double x = cX * 16 + rand.nextInt(16);
			double y = rand.nextInt(maxY - minY) + minY;
			double z = cZ * 16 + rand.nextInt(16);

			int k = 1;
			if(rand.nextInt(4) == 0) {
				addTunnel(
					rand.nextLong(), oX, oZ, cp, x, y, z,
					1.0F + rand.nextFloat() * roomSize, 0.0F, 0.0F, -1, -1, 0.5D
				);
				k += rand.nextInt(4);
			}

			for(int l = 0; l < k; ++l) {
				float a = rand.nextFloat() * ((float)Math.PI * 2F);
				float f1 = (rand.nextFloat() - 0.5F) * 2.0F / 8.0F;
				float f2 = (rand.nextFloat() * 2.0F + rand.nextFloat()) * thickness;
				if(rand.nextInt(256) >= bigChance)
					f2 *= rand.nextFloat() * rand.nextFloat() * bigScale + 1.0F;

				addTunnel(rand.nextLong(), oX, oZ, cp, x, y, z, f2, a, f1, 0, 0, 1.0D);
			}
		}
	}

	protected void addTunnel(
		long seed, int oX, int oZ, ChunkPrimer cp, double x, double y, double z,
		float radius, float yaw, float pitch, int segment, int segments, double vertShape
	) {
		double ox = oX * 16 + 8;
		double oz = oZ * 16 + 8;
		float f = 0.0F;
		float f1 = 0.0F;
		Random random = new Random(seed);

		if(segments <= 0) {
			int i = range * 16 - 16;
			if (i <= 0) i = 8;
			segments = i - random.nextInt(i / 4);
		}

		boolean room = false;
		if(segment == -1) {
			segment = segments / 2;
			room = true;
		}

		int splitAt = random.nextInt(segments / 2) + segments / 4;

		segloop: for(boolean flag = random.nextInt(6) == 0; segment < segments; ++segment) {
			double sxz = 1.5D + MathHelper.sin(segment * (float)Math.PI / segments) * radius;
			double sy = sxz * vertShape;
			float f2 = MathHelper.cos(pitch);
			float f3 = MathHelper.sin(pitch);
			x += MathHelper.cos(yaw) * f2;
			y += f3;
			z += MathHelper.sin(yaw) * f2;

			if(flag) pitch *= 0.92F;
			else pitch *= 0.7F;

			pitch += f1 * 0.1F;
			yaw += f * 0.1F;
			f1 *= 0.9F;
			f *= 0.75F;
			f1 += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 2.0F;
			f += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4.0F;

			if(!room && segment == splitAt && radius > 1.0F) {
				addTunnel(
					random.nextLong(), oX, oZ, cp, x, y, z,
					random.nextFloat() * 0.5F + 0.5F, yaw - (float)Math.PI / 2F, pitch / 3.0F,
					segment, segments, 1.0D
				);
				addTunnel(
					random.nextLong(), oX, oZ, cp, x, y, z,
					random.nextFloat() * 0.5F + 0.5F, yaw + (float)Math.PI / 2F, pitch / 3.0F,
					segment, segments, 1.0D
				);
				return;
			}

			if(room || random.nextInt(4) != 0) {
				double dx = x - ox;
				double dz = z - oz;
				double remL = segments - segment;
				double d = radius + 2.0F + 16.0F;
				if(dx * dx + dz * dz > remL * remL + d * d) return;

				d = 9.0D + sxz;
				if(dx >= -d && dz >= -d && dx <= d && dz <= d) {
					int x0 = MathHelper.floor(x - sxz) - oX * 16 - 1;
					int x1 = MathHelper.floor(x + sxz) - oX * 16 + 1;
					int y0 = MathHelper.floor(y - sy) - 1;
					int y1 = MathHelper.floor(y + sy) + 1;
					int z0 = MathHelper.floor(z - sxz) - oZ * 16 - 1;
					int z1 = MathHelper.floor(z + sxz) - oZ * 16 + 1;

					if(x0 < 0) x0 = 0;
					if(x1 > 16) x1 = 16;
					if(y0 < minY + 1) y0 = minY + 1;
					if(y1 > maxY - 8) y1 = maxY - 8;
					if(z0 < 0) z0 = 0;
					if(z1 > 16) z1 = 16;

					for(int bx = x0; bx < x1; ++bx)
						for(int bz = z0; bz < z1; ++bz)
							for(int by = y1 + 1; by >= y0 - 1; --by)
								if(by >= minY && by < maxY) {
									if(isLiquid(cp, bx, by, bz, oX, oZ))
										continue segloop;
									if(by != y0 - 1 && bx != x0 && bx != x1 - 1 && bz != z0 && bz != z1 - 1)
										by = y0;
								}

					dx += 8;
					dz += 8;
					for(int bx = x0; bx < x1; ++bx) {
						double rx = (bx + 0.5 - dx) / sxz;
						for(int bz = z0; bz < z1; ++bz) {
							double rz = (bz + 0.5 - dz) / sxz;
							boolean top = false;
							if(rx * rx + rz * rz < 1.0D) for(int by = y1; by > y0; --by) {
								double ry = (by - 0.5 - y) / sy;
								if(ry > -0.7D && rx * rx + ry * ry + rz * rz < 1.0D)
									top = digBlock(cp, bx, by, bz, oX, oZ, top);
							}
						}
					}

					if(room) break;
				}
			}
		}
	}

	protected boolean isLiquid(ChunkPrimer data, int x, int y, int z, int chunkX, int chunkZ) {
		Block block = data.getBlockState(x, y, z).getBlock();
		if(y <= fillY && block == setFluid.getBlock()) return false;
		return block == Blocks.FLOWING_WATER || block == Blocks.WATER
			|| block == Blocks.FLOWING_LAVA || block == Blocks.LAVA;
	}

	private boolean isExceptionBiome(Biome biome) {
		if(biome == Biomes.BEACH) return true;
		if(biome == Biomes.DESERT) return true;
		if(biome == Biomes.HELL) return true;
		return false;
	}

	protected boolean digBlock(ChunkPrimer data, int x, int y, int z, int chunkX, int chunkZ, boolean foundTop) {
		IBlockState state = data.getBlockState(x, y, z);
		Block block = state.getBlock();
		Biome biome = world.getBiome(new BlockPos(x + chunkX * 16, 0, z + chunkZ * 16));
		Block top = biome.topBlock.getBlock();
		Block filler = biome.fillerBlock.getBlock();

		if(!foundTop)
			foundTop = isExceptionBiome(biome) ? block == Blocks.GRASS : block == top;

		if(replace.test(state) || block == top || block == filler)
			if(y <= fillY)
				data.setBlockState(x, y, z, setFluid);
			else {
				data.setBlockState(x, y, z, setBlock);
				if(foundTop && data.getBlockState(x, y - 1, z).getBlock() == filler)
					data.setBlockState(x, y - 1, z, top.getDefaultState());
			}
		return foundTop;
	}

}
