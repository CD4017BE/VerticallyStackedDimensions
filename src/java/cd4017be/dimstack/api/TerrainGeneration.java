package cd4017be.dimstack.api;

import java.util.HashMap;
import java.util.Random;
import java.util.function.Function;

import cd4017be.dimstack.api.gen.ITerrainGenerator;
import cd4017be.dimstack.api.util.CfgList;
import net.minecraft.nbt.NBTTagCompound;
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

	public void setupNoiseGens(Context c, Random rand) {
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
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		deserializeNBT(nbt, REGISTRY);
	}

}
