package cd4017be.dimstack.api;

import java.util.HashMap;
import java.util.Random;
import java.util.function.Function;

import cd4017be.dimstack.api.gen.ITerrainGenerator;
import cd4017be.dimstack.api.util.CfgList;
import cd4017be.dimstack.api.util.NoiseField;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.NoiseGenerator;
import net.minecraft.world.gen.NoiseGeneratorOctaves;
import net.minecraft.world.gen.NoiseGeneratorPerlin;
import net.minecraft.world.gen.NoiseGeneratorSimplex;
import net.minecraftforge.event.terraingen.InitNoiseGensEvent.Context;
import net.minecraftforge.event.terraingen.InitNoiseGensEvent.ContextEnd;
import net.minecraftforge.event.terraingen.InitNoiseGensEvent.ContextHell;
import net.minecraftforge.event.terraingen.InitNoiseGensEvent.ContextOverworld;


/**
 * Holds information about terrain generation features for a dimension.<br>
 * Use {@link #REGISTRY} to register custom ITerrainGenerators.
 * @author CD4017BE
 */
public class TerrainGeneration extends CfgList<ITerrainGenerator> {

	/** Terrain generator registry */
	public static final HashMap<String, Function<NBTTagCompound, ITerrainGenerator>> REGISTRY = new HashMap<>();

	/** 3D, 16 octaves : +/- 65535 */
	public NoiseGeneratorOctaves depth, l_perlin1, l_perlin2;
	/** 3D, 10 octaves : +/- 1023 */
	public NoiseGeneratorOctaves scale;
	/** 3D, 8 octaves : +/- 255 */
	public NoiseGeneratorOctaves perlin;
	/** 3D, 8 octaves : +/- 255 (Overworld only) */
	public NoiseGeneratorOctaves forest;
	/** 3D, 4 octaves : +/- 15 (Nether only) */
	public NoiseGeneratorOctaves perlin2, perlin3;
	/** 2D, 4 octaves : +/- 15 (Overworld only) */
	public NoiseGeneratorPerlin height;
	/** 2D, 1 octave : +/- 1 (End Only) */
	public NoiseGeneratorSimplex islands;
	/** terrain random */
	public Random rand;
	/** custom noise fields */
	public NoiseField[] noiseFields;
	/** vertical offset of this dimension relative to stack bottom (used for interdimensional terrain features) */
	public int offsetY;
	/** dimension id */
	public int dimId;

	@Override
	public void deserializeNBT(NBTBase nbt) {
		deserializeNBT((NBTTagList)nbt, REGISTRY);
	}

	public void setupNoiseGens(IDimension dim, Context c, Random rand) {
		this.depth = c.getDepth();
		this.scale = c.getScale();
		this.perlin = c.getPerlin();
		this.l_perlin1 = c.getLPerlin1();
		this.l_perlin2 = c.getLPerlin2();
		if (c instanceof ContextOverworld) {
			ContextOverworld co = (ContextOverworld)c;
			this.forest = co.getForest();
			this.height = co.getHeight();
		} else if (c instanceof ContextHell) {
			ContextHell ch = (ContextHell)c;
			this.perlin2 = ch.getPerlin2();
			this.perlin3 = ch.getPerlin3();
		} else if (c instanceof ContextEnd) {
			ContextEnd ce = (ContextEnd)c;
			this.islands = ce.getIsland();
		}
		this.rand = rand;
		this.dimId = dim.id();
		int y = 0, h = dim.height();
		for (; h > 0; h--) {
			dim = dim.down();
			y += dim.ceilHeight() - 1;
		}
		for (; h < 0; h++) {
			y -= dim.ceilHeight() - 1;
			dim = dim.up();
		}
		this.offsetY = y;
		
		initNoiseFields();
	}

	private void initNoiseFields() {
		SharedNoiseFields snf = API.INSTANCE.getSettings(SharedNoiseFields.class, false);
		int l = snf.noiseFields.length;
		this.noiseFields = new NoiseField[l];
		for (int i = 0; i < l; i++) {
			NoiseField nf = snf.noiseFields[i].clone();
			NoiseGenerator gen;
			int id = snf.source[i];
			switch(id) {
			case -1: gen = depth; break;
			case -2: gen = scale; break;
			case -3: gen = perlin; break;
			case -4: gen = l_perlin1; break;
			case -5: gen = l_perlin2; break;
			case -6: gen = forest; break;
			case -7: gen = height; break;
			case -8: gen = perlin2; break;
			case -9: gen = perlin3; break;
			default: gen = nf.gen;
			}
			nf.setGenerator(gen).setOffsetY(offsetY);
			noiseFields[i] = nf;
		}
		for (ITerrainGenerator g : entries)
			g.initNoise(this);
	}

	public void generate(World world, IChunkGenerator gen, ChunkPrimer cp, int cx, int cz) {
		for (NoiseField f : noiseFields)
			f.prepareFor(cx, cz);
		for (ITerrainGenerator tg : entries)
			tg.generate(world, cp, cx, cz, this);
	}

}
