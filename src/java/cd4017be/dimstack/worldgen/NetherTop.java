package cd4017be.dimstack.worldgen;

import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.ChunkGeneratorHell;
import net.minecraft.world.gen.NoiseGeneratorOctaves;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.ChunkGeneratorEvent.ReplaceBiomeBlocks;
import net.minecraftforge.event.terraingen.InitNoiseGensEvent;
import net.minecraftforge.event.terraingen.InitNoiseGensEvent.ContextHell;
import net.minecraftforge.event.terraingen.TerrainGen;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class NetherTop {

	private static final IBlockState
			NETHERRACK = Blocks.NETHERRACK.getDefaultState(),
			STONE = Blocks.STONE.getDefaultState(),
			WATER = Blocks.WATER.getDefaultState(),
			DIRT = Blocks.DIRT.getDefaultState(),
			GRAVEL = Blocks.GRAVEL.getDefaultState(),
			AIR = Blocks.AIR.getDefaultState(),
			SAND = Blocks.SAND.getDefaultState(),
			SANDSTONE = Blocks.SANDSTONE.getDefaultState(),
			OBSIDIAN = Blocks.OBSIDIAN.getDefaultState();
	private WorldGenerator genDirt, genGravel, genGranite, genDiorite, genAndesite;
	private NoiseGeneratorOctaves depth, scale, perlin1, perlin2, perlin3, Lperlin1, Lperlin2;
	double[] dr, sr, pr, ssr, gvr, p3r, ar, br;
	private double[] buffer;
	Random rand;

	public NetherTop() {
		//mods commonly use 0 for their ore-gen, so this runs just before.
		//GameRegistry.registerWorldGenerator(this, -2);
		MinecraftForge.EVENT_BUS.register(this);
		MinecraftForge.TERRAIN_GEN_BUS.register(this);
	}

	@SubscribeEvent
	public void init(InitNoiseGensEvent<ContextHell> event) {
		if (event.getWorld().provider.isNether()) {
			ContextHell c = event.getNewValues();
			this.depth = c.getDepth();
			this.scale = c.getScale();
			this.perlin1 = c.getPerlin(); //+-255
			this.perlin2 = c.getPerlin2();
			this.perlin3 = c.getPerlin3();
			this.Lperlin1 = c.getLPerlin1(); //+-65535
			this.Lperlin2 = c.getLPerlin2(); //+-65535
			this.rand = event.getRandom();
		}
	}

	@SubscribeEvent
	public void generate(ReplaceBiomeBlocks event) {
		if (!(event.getGen() instanceof ChunkGeneratorHell)) return;
		int cx = event.getX(), cz = event.getZ();
		ChunkPrimer primer = event.getPrimer();
		prepareHeights(cx, cz, primer);
		buildSurfaces(cx, cz, primer);
	}

	public void prepareHeights(int chunkX, int chunkZ, ChunkPrimer primer) {
		int msl = 160;
		final int sx = 4, sy = 8, sz = 4;
		final int nx = 5, ny = 17, nz = 5;
		this.buffer = this.getHeights(this.buffer, chunkX * 4, 16, chunkZ * 4, nx, ny, nz);
		for (int X = 0; X < 4; ++X) {
			for (int Z = 0; Z < 4; ++Z) {
				for (int Y = 0; Y < 16; ++Y) {
					double f00 = this.buffer[((X + 0) * nx + Z + 0) * ny + Y + 0];
					double f01 = this.buffer[((X + 0) * nx + Z + 1) * ny + Y + 0];
					double f10 = this.buffer[((X + 1) * nx + Z + 0) * ny + Y + 0];
					double f11 = this.buffer[((X + 1) * nx + Z + 1) * ny + Y + 0];
					double d00 = (this.buffer[((X + 0) * nx + Z + 0) * ny + Y + 1] - f00) / (double)sy;
					double d01 = (this.buffer[((X + 0) * nx + Z + 1) * ny + Y + 1] - f01) / (double)sy;
					double d10 = (this.buffer[((X + 1) * nx + Z + 0) * ny + Y + 1] - f10) / (double)sy;
					double d11 = (this.buffer[((X + 1) * nx + Z + 1) * ny + Y + 1] - f11) / (double)sy;
					for (int y = 0; y < sy; ++y) {
						double f0 = f00;
						double f1 = f01;
						double d0 = (f10 - f00) / (double)sx;
						double d1 = (f11 - f01) / (double)sx;
						for (int x = 0; x < sx; ++x) {
							double f = f0;
							double d = (f1 - f0) / (double)sz;
							for (int z = 0; z < sz; ++z) {
								int bx = x + X * 4;
								int by = y + Y * 8 + 128;
								int bz = z + Z * 4;
								IBlockState state = null;
								if (by < msl) state = WATER;
								if (f > 0.0D) state = STONE;
								primer.setBlockState(bx, by, bz, state);
								f += d;
							}
							f0 += d0;
							f1 += d1;
						}
						f00 += d00;
						f01 += d01;
						f10 += d10;
						f11 += d11;
					}
				}
			}
		}
	}

	private double[] getHeights(double[] data, int ox, int oy, int oz, int nx, int ny, int nz) {
		if (data == null) data = new double[nx * ny * nz];
		double dh = 684.412D;
		double dv = 2053.236D;
		//this.dr = this.depth.generateNoiseOctaves(this.dr, ox, oy, oz, nx, 1, nz, 100.0D, 0.0D, 100.0D);
		this.pr = this.perlin1.generateNoiseOctaves(this.pr, ox, oy, oz, nx, ny, nz, dh / 80.0, dv / 60.0, dh / 80.0);
		this.ar = this.Lperlin1.generateNoiseOctaves(this.ar, ox, oy, oz, nx, ny, nz, dh, dv, dh);
		this.br = this.Lperlin2.generateNoiseOctaves(this.br, ox, oy, oz, nx, ny, nz, dh, dv, dh);
		double[] vWaves = new double[ny];
		for (int y = 0; y < ny; ++y) {
			vWaves[y] = Math.cos((double)y * Math.PI * 6.0D / (double)ny) * 2.0D; //3 vollkreise (2, 0, -2, 0, 2, 0, -2, 0, 2, 0, -2, 0, 2)
			double yEdge = (double)y;
			if (y > ny / 2) yEdge = (double)(ny - 1 - y);
			if (yEdge < 4.0D) {
				yEdge = 4.0D - yEdge;
				vWaves[y] -= yEdge * yEdge * yEdge * 10.0D; //dämpfung an rändern (-10(4-y)³)
			}
		}
		int i = 0;
		for (int x = 0; x < nx; ++x)
			for (int z = 0; z < nz; ++z)
				for (int y = 0; y < ny; ++y) {
					double a = this.ar[i] / 512.0D;
					double b = this.br[i] / 512.0D;
					double weight = (this.pr[i] / 10.0D + 1.0D) / 2.0D;
					double val = weight < 0.0D ? a
							: weight > 1.0D ? b
							: a + (b - a) * weight;
					val -= vWaves[y];
					if (y > ny - 4) {
						double edge = (double)(y - (ny - 4)) / 3.0; //edge 3 ... 0 -> 0.0 ... 1.0
						val = val * (1.0D - edge) + -10.0D * edge; //interpolate edge 3 ... 0 -> val ... -10
					}
					data[i++] = val;
				}
		return data;
	}

	public void buildSurfaces(int chunkX, int chunkZ, ChunkPrimer primer) {
		final int msl = 192;
		final double scale = 0.03125D;
		this.ssr = this.perlin2.generateNoiseOctaves(this.ssr, chunkX * 16, chunkZ * 16, 128, 16, 16, 1, scale, scale, 1.0D);
		this.gvr = this.perlin2.generateNoiseOctaves(this.gvr, chunkX * 16, 237, chunkZ * 16, 16, 1, 16, scale, 1.0D, scale);
		this.p3r = this.perlin3.generateNoiseOctaves(this.p3r, chunkX * 16, chunkZ * 16, 0, 16, 16, 1, 0.0625D, 0.0625D, 0.0625D);
		for (int z = 0; z < 16; ++z) {
			for (int x = 0; x < 16; ++x) {
				boolean genSs = ssr[z + x * 16] + this.rand.nextDouble() * 0.2D > 0.0D;
				boolean genGv = gvr[z + x * 16] + this.rand.nextDouble() * 0.2D > 0.0D;
				int thick = (int)(p3r[z + x * 16] / 3.0D + 3.0D + this.rand.nextDouble() * 0.25D);
				int str = -1;
				IBlockState stateT = STONE;
				IBlockState stateB = STONE;
				for (int y = 255; y >= 128; --y) {
					if (y > 127 + this.rand.nextInt(5)) {
						IBlockState state = primer.getBlockState(x, y, z);
						if (state.getBlock() != null && state.getMaterial() != Material.AIR) {
							if (state.getBlock() == Blocks.STONE) {
								if (str == -1) {
									if (thick <= 0) {
										stateT = AIR;
										stateB = STONE;
									} else if (y >= msl - 4 && y <= msl + 1) {//around msl
										stateT = STONE;
										stateB = STONE;
										if (genGv) {
											stateT = GRAVEL;
											stateB = STONE;
										}
										if (genSs) {
											stateT = SAND;
											stateB = SANDSTONE;
										}
									}
									if (y < msl && (stateT == null || stateT.getMaterial() == Material.AIR))
										stateT = WATER;
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
					} else primer.setBlockState(x, y, z, OBSIDIAN);
				}
			}
		}
	}

}
