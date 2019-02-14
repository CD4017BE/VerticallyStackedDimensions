package cd4017be.dimstack.worldgen;

import java.util.Arrays;
import java.util.HashMap;

import cd4017be.api.recipes.RecipeAPI;
import cd4017be.api.recipes.RecipeAPI.IRecipeHandler;
import cd4017be.api.recipes.RecipeScriptContext.ConfigConstants;
import cd4017be.dimstack.api.API;
import cd4017be.dimstack.api.SharedNoiseFields;
import cd4017be.dimstack.api.TerrainGeneration;
import cd4017be.dimstack.api.gen.ITerrainGenerator;
import cd4017be.dimstack.api.util.BlockPredicate;
import cd4017be.dimstack.api.util.NoiseField;
import cd4017be.dimstack.core.Dimensionstack;
import cd4017be.dimstack.core.PortalConfiguration;
import cd4017be.lib.script.Parameters;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.gen.NoiseGenerator;
import net.minecraftforge.event.terraingen.InitNoiseGensEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.ChunkGeneratorEvent.ReplaceBiomeBlocks;
import net.minecraftforge.event.terraingen.InitNoiseGensEvent.Context;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author CD4017BE
 *
 */
public class TerrainGenHandler implements IRecipeHandler {

	private static final String NOISE_FIELD = "noiseField";


	public TerrainGenHandler() {
		MinecraftForge.TERRAIN_GEN_BUS.register(this);
		MinecraftForge.EVENT_BUS.register(this);
		RecipeAPI.Handlers.put(NOISE_FIELD, this);
		RecipeAPI.Handlers.put(SimpleLayerGen.ID, this);
	}

	@SubscribeEvent
	public void init(InitNoiseGensEvent<Context> event) {
		World world = event.getWorld();
		SharedNoiseFields snf = Dimensionstack.INSTANCE.getSettings(SharedNoiseFields.class, false);
		if (snf != null) snf.init(world.getSeed());
		
		PortalConfiguration pc = PortalConfiguration.get(world);
		TerrainGeneration cfg = pc.getSettings(TerrainGeneration.class, false);
		if (cfg != null) cfg.setupNoiseGens(pc, event.getNewValues(), event.getRandom());
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void generate(ReplaceBiomeBlocks event) {
		TerrainGeneration cfg = PortalConfiguration.get(event.getWorld()).getSettings(TerrainGeneration.class, false);
		if (cfg != null)
			cfg.generate(event.getGen(), event.getPrimer(), event.getX(), event.getZ());
	}

	public void initConfig(ConfigConstants cfg) {
		double[] v = cfg.get("custom_noise_octaves", double[].class, new double[0]);
		if (v.length > 0) {
			SharedNoiseFields snf = Dimensionstack.INSTANCE.getSettings(SharedNoiseFields.class, true);
			int l = v.length;
			byte[] oct = new byte[l];
			for (int i = 0; i < l; i++)
				oct[i] = (byte)v[i];
			snf.octaves = oct;
			snf.noiseFields = new NoiseGenerator[l];
		}
	}

	@Override
	public void addRecipe(Parameters param) {
		ITerrainGenerator gen;
		String key = param.getString(0);
		if (key.equals(NOISE_FIELD)) {
			new NoiseFieldCfg(param);
			return;
		} else if (key.equals(SimpleLayerGen.ID)) {
			double[] vec = param.getVectorOrAll(3);
			int l = vec.length;
			int y0, y1, eb = 0, et = 0;
			if (l == 2) {
				y0 = (int)vec[0];
				y1 = (int)vec[1];
			} else if (l == 4) {
				y0 = (int)vec[1];
				y1 = (int)vec[2];
				eb = y0 - (int)vec[0];
				et = (int)vec[3] - y1;
			} else throw new IllegalArgumentException("expected 2 or 4 height values @ " + 3);
			gen = new SimpleLayerGen(BlockPredicate.parse(param.get(2, ItemStack.class)), y0, y1, eb, et);
		} else return;
		TerrainGeneration cfg = PortalConfiguration.get(param.getIndex(1)).getSettings(TerrainGeneration.class, true);
		cfg.entries.add(gen);
	}

	static class NoiseFieldCfg {
		static HashMap<String, NoiseFieldCfg> REGISTRY = new HashMap<>();

		final int hGrid, vGrid, source, octaves;
		final double hScale, vScale;
		final Int2IntOpenHashMap regDims;

		NoiseFieldCfg(Parameters param) {
			this.source = param.getIndex(2);
			this.hGrid = param.getIndex(3);
			this.vGrid = param.getIndex(4);
			this.hScale = param.getNumber(5);
			this.vScale = param.getNumber(6);
			this.regDims = new Int2IntOpenHashMap();
			regDims.defaultReturnValue(-1);
			REGISTRY.put(param.getString(1), this);
			switch(source) {
			case -1: case -4: case -5:
				this.octaves = 16; break;
			case -2:
				this.octaves = 10; break;
			case -3: case -6:
				this.octaves = 8; break;
			case -7: case -8: case -9:
				this.octaves = 4; break;
			default:
				SharedNoiseFields snf = API.INSTANCE.getSettings(SharedNoiseFields.class, false);
				if (snf != null && source >= 0 && source < snf.octaves.length) {
					this.octaves = snf.octaves[source];
					break;
				}
				throw new IllegalArgumentException("invalid noise gen source " + source);
			}
		}

		int getIndex(int dim) {
			int i = regDims.get(dim);
			if (i >= 0) return i;
			TerrainGeneration gen = PortalConfiguration.get(dim).getSettings(TerrainGeneration.class, true);
			int n = gen.noiseFields.length;
			int[] srcs = Arrays.copyOf(gen.sources, n + 1);
			srcs[n] = source;
			gen.sources = srcs;
			NoiseField[] nf = Arrays.copyOf(gen.noiseFields, n + 1);
			nf[n] = new NoiseField(hGrid, vGrid, hScale, vScale);
			gen.noiseFields = nf;
			regDims.put(dim, n);
			return n;
		}

	}

}
