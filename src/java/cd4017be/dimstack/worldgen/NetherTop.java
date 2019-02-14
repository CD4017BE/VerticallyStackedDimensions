package cd4017be.dimstack.worldgen;

import java.util.Random;

import cd4017be.dimstack.api.TerrainGeneration;
import cd4017be.dimstack.api.gen.ITerrainGenerator;
import cd4017be.dimstack.api.util.BlockPredicate;
import cd4017be.dimstack.api.util.NoiseField;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;

public class NetherTop implements ITerrainGenerator {

	public static final String ID = "nether";

	private double[] pr, ssr, gvr, p3r, ar, br;
	private double[] buffer, vWaves;
	private final IBlockState main, liquid, sand1B, sand1, sand2B, sand2;
	private final int minY, maxY, lakeLvl, sandLvl, border;
	private int fieldSize, fieldOfs;


	public NetherTop(int minY, int maxY, int border, int lakeLvl, int sandLvl, IBlockState main, IBlockState liquid, IBlockState sand1b, IBlockState sand1, IBlockState sand2b, IBlockState sand2) {
		this.main = main;
		this.liquid = liquid;
		this.sand1B = sand1b;
		this.sand1 = sand1;
		this.sand2B = sand2b;
		this.sand2 = sand2;
		this.minY = minY;
		this.maxY = maxY;
		this.lakeLvl = lakeLvl;
		this.sandLvl = sandLvl;
		this.border = border;
		initVertTerrain();
	}

	public NetherTop(NBTTagCompound nbt) {
		this.main = BlockPredicate.parse(nbt.getString("main"));
		this.liquid = BlockPredicate.parse(nbt.getString("liquid"));
		this.sand1B = BlockPredicate.parse(nbt.getString("sand1B"));
		this.sand1 = BlockPredicate.parse(nbt.getString("sand1"));
		this.sand2B = BlockPredicate.parse(nbt.getString("sand2B"));
		this.sand2 = BlockPredicate.parse(nbt.getString("sand2"));
		this.lakeLvl = nbt.getByte("lakeY") & 0xff;
		this.sandLvl = nbt.getByte("sandY") & 0xff;
		this.minY = nbt.getByte("minY") & 0xff;
		this.maxY = (nbt.getByte("maxY") & 0xff) + 1;
		this.border = nbt.getByte("border");
		initVertTerrain();
	}

	private void initVertTerrain() {
		int y0 = minY / 8, y1 = (maxY - 1) / 8 + 1;
		int ny = y1 - y0 + 1;
		this.fieldSize = ny;
		this.fieldOfs = y0;
		double[] vWaves = new double[ny];
		double b = (double)this.border / 8.0;
		for (int y = 0; y < ny; ++y) {
			vWaves[y] = Math.cos((double)(y + y0) * Math.PI * 6.0D / 16.0) * 2.0D; //6 full circles over 256 blocks
			double yEdge = (double)y;
			if (y > ny / 2) yEdge = (double)(ny - 1 - y);
			if (yEdge < b) {
				yEdge = 1.0 - yEdge / b;
				vWaves[y] -= yEdge * yEdge * yEdge * 640.0D; //dampening on edge
			}
		}
		this.vWaves = vWaves;
	}

	@Override
	public String getRegistryName() {
		return ID;
	}

	@Override
	public NBTTagCompound writeNBT() {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setString("main", BlockPredicate.serialize(main));
		nbt.setString("liquid", BlockPredicate.serialize(liquid));
		nbt.setString("sand1B", BlockPredicate.serialize(sand1B));
		nbt.setString("sand1", BlockPredicate.serialize(sand1));
		nbt.setString("sand2B", BlockPredicate.serialize(sand2B));
		nbt.setString("sand2", BlockPredicate.serialize(sand2));
		nbt.setByte("lakeY", (byte)lakeLvl);
		nbt.setByte("sandY", (byte)sandLvl);
		nbt.setByte("minY", (byte)minY);
		nbt.setByte("maxY", (byte)(maxY - 1));
		nbt.setByte("border", (byte)border);
		return nbt;
	}

	@Override
	public void generate(IChunkGenerator gen, ChunkPrimer cp, int cx, int cz, TerrainGeneration cfg) {
		this.buffer = getHeights(this.buffer, cx * 4, fieldOfs, cz * 4, 5, fieldSize, 5, cfg);
		final IBlockState main = this.main, liquid = this.liquid;
		final int msl = this.lakeLvl;
		NoiseField.interpolate3D(buffer, 4, 8, fieldOfs * 8, minY, maxY, (x, y, z, f)->
			cp.setBlockState(x, y, z, f > 0.0 ? main : y < msl ? liquid : null));
		
		buildSurfaces(cx, cz, cp, cfg);
	}

	private double[] getHeights(double[] data, int ox, int oy, int oz, int nx, int ny, int nz, TerrainGeneration cfg) {
		if (data == null) data = new double[nx * ny * nz];
		final double dh = 684.412D;
		final double dv = 2053.236D;
		final double[] pr = this.pr = cfg.perlin.generateNoiseOctaves(this.pr, ox, oy, oz, nx, ny, nz, dh / 80.0, dv / 60.0, dh / 80.0);
		final double[] ar = this.ar = cfg.l_perlin1.generateNoiseOctaves(this.ar, ox, oy, oz, nx, ny, nz, dh, dv, dh);
		final double[] br = this.br = cfg.l_perlin2.generateNoiseOctaves(this.br, ox, oy, oz, nx, ny, nz, dh, dv, dh);
		final double[] vWaves = this.vWaves;
		int i = 0;
		for (int x = 0; x < nx; ++x)
			for (int z = 0; z < nz; ++z)
				for (int y = 0; y < ny; ++y) {
					double a = ar[i] / 512.0D;
					double b = br[i] / 512.0D;
					double weight = (pr[i] / 10.0D + 1.0D) / 2.0D;
					double val = weight < 0.0D ? a
							: weight > 1.0D ? b
							: a + (b - a) * weight;
					data[i++] = val - vWaves[y];
				}
		return data;
	}

	public void buildSurfaces(int chunkX, int chunkZ, ChunkPrimer primer, TerrainGeneration cfg) {
		final int msl = this.sandLvl;
		final double scale = 0.03125D;
		final double[] ssr = this.ssr = cfg.perlin2.generateNoiseOctaves(this.ssr, chunkX * 16, chunkZ * 16, 128, 16, 16, 1, scale, scale, 1.0D);
		final double[] gvr = this.gvr = cfg.perlin2.generateNoiseOctaves(this.gvr, chunkX * 16, 237, chunkZ * 16, 16, 1, 16, scale, 1.0D, scale);
		final double[] p3r = this.p3r = cfg.perlin3.generateNoiseOctaves(this.p3r, chunkX * 16, chunkZ * 16, 0, 16, 16, 1, 0.0625D, 0.0625D, 0.0625D);
		final Random rand = cfg.rand;
		final IBlockState air = Blocks.AIR.getDefaultState(),
				main = this.main, liquid = this.liquid,
				sand1 = this.sand1, sand1B = this.sand1B,
				sand2 = this.sand2, sand2B = this.sand2B;
		int y1 = maxY - 1, y0 = minY;
		for (int z = 0; z < 16; ++z) {
			for (int x = 0; x < 16; ++x) {
				boolean genSs = ssr[z + x * 16] + rand.nextDouble() * 0.2D > 0.0D;
				boolean genGv = gvr[z + x * 16] + rand.nextDouble() * 0.2D > 0.0D;
				int thick = (int)(p3r[z + x * 16] / 3.0D + 3.0D + rand.nextDouble() * 0.25D);
				int str = -1;
				IBlockState stateT = main;
				IBlockState stateB = main;
				for (int y = y1; y >= y0; --y) {
					IBlockState state = primer.getBlockState(x, y, z);
					if (state.getBlock() != null && state.getMaterial() != Material.AIR) {
						if (state == main) {
							if (str == -1) {
								if (thick <= 0) {
									stateT = air;
									stateB = main;
								} else if (y >= msl - 4 && y <= msl + 1) {//around msl
									stateT = main;
									stateB = main;
									if (genGv) {
										stateT = sand2;
										stateB = sand2B;
									}
									if (genSs) {
										stateT = sand1;
										stateB = sand1B;
									}
								}
								if (y < msl && (stateT == null || stateT.getMaterial() == Material.AIR))
									stateT = liquid;
								str = thick;
								if (y >= msl - 1)
									primer.setBlockState(x, y, z, stateT);
								else
									primer.setBlockState(x, y, z, stateB);
							} else if (str > 0) {
								--str;
								primer.setBlockState(x, y, z, stateB);
							}
						}
					} else str = -1;
				}
			}
		}
	}

}
