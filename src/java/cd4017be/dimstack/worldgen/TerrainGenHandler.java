package cd4017be.dimstack.worldgen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import cd4017be.api.recipes.RecipeAPI;
import cd4017be.api.recipes.RecipeAPI.IRecipeHandler;
import cd4017be.api.recipes.RecipeScriptContext.ConfigConstants;
import cd4017be.dimstack.api.API;
import cd4017be.dimstack.api.DisabledBlockGen;
import cd4017be.dimstack.api.IDimension;
import cd4017be.dimstack.api.SharedNoiseFields;
import cd4017be.dimstack.api.TerrainGeneration;
import cd4017be.dimstack.api.TransitionInfo;
import cd4017be.dimstack.api.gen.ITerrainGenerator;
import cd4017be.dimstack.api.util.BlockPredicate;
import cd4017be.dimstack.api.util.NoiseField;
import cd4017be.dimstack.core.Dimensionstack;
import cd4017be.dimstack.core.PortalConfiguration;
import cd4017be.dimstack.util.DebugInfo;
import cd4017be.lib.script.Parameters;
import cd4017be.lib.script.obj.Array;
import cd4017be.lib.script.obj.Error;
import cd4017be.lib.script.obj.IOperand;
import cd4017be.lib.script.obj.Nil;
import cd4017be.lib.script.obj.Text;
import cd4017be.lib.script.obj.Number;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;
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
		RecipeAPI.Handlers.put(NoiseLayerGen.ID, this);
		RecipeAPI.Handlers.put(NetherTop.ID, this);
		
		TerrainGeneration.REGISTRY.put(SimpleLayerGen.ID, SimpleLayerGen::new);
		TerrainGeneration.REGISTRY.put(NoiseLayerGen.ID, NoiseLayerGen::new);
		TerrainGeneration.REGISTRY.put(NetherTop.ID, NetherTop::new);
	}

	@SubscribeEvent
	public void init(InitNoiseGensEvent<Context> event) {
		World world = event.getWorld();
		PortalConfiguration pc = PortalConfiguration.get(world);
		
		SharedNoiseFields snf = Dimensionstack.INSTANCE.getSettings(SharedNoiseFields.class, false);
		if (snf != null)
			snf.init(world.getSeed());
		
		TransitionInfo ti = pc.getSettings(TransitionInfo.class, false);
		if (ti != null && ti.init())
			initTransitions(ti, pc);
		
		TerrainGeneration cfg = pc.getSettings(TerrainGeneration.class, false);
		if (cfg != null)
			cfg.setupNoiseGens(pc, event.getNewValues(), event.getRandom());
		
		pc.getSettings(DebugInfo.class, true).setInitialized();
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void generate(ReplaceBiomeBlocks event) {
		PortalConfiguration pc = PortalConfiguration.get(event.getWorld());
		pc.getSettings(DebugInfo.class, true).fixInitialization(pc, event.getWorld(), event.getGen());
		
		TerrainGeneration tg = pc.getSettings(TerrainGeneration.class, false);
		if (tg != null)
			tg.generate(event.getGenerator(), event.getPrimer(), event.getX(), event.getZ());
		
		DisabledBlockGen dbg = pc.getSettings(DisabledBlockGen.class, false);
		if (dbg != null)
			BlockPredicate.disableBlock(event.getPrimer(), dbg.disabledBlock);
		
		pc.getSettings(DebugInfo.class, true).setGenerated();
	}

	private void initTransitions(TransitionInfo cfg, IDimension d) {
		do {
			int n = cfg.sizeTop;
			if (n < 0) break;
			
			IDimension d1 = d.up();
			if (d1 == null) break;
			
			TransitionInfo cfg1 = d1.getSettings(TransitionInfo.class, false);
			if (cfg1 == null) break;
			
			IBlockState block = cfg1.blockBot;
			if (block == null || block == cfg.blockTop) break;
			
			int c = d.ceilHeight();
			d.getSettings(TerrainGeneration.class, true).entries
				.add(0, new TransitionGen(block, c - n, c + 1, n + cfg1.sizeBot, true));
		} while(false);
		do {
			int n = cfg.sizeBot;
			if (n < 0) break;
			
			IDimension d1 = d.down();
			if (d1 == null) break;
			
			TransitionInfo cfg1 = d1.getSettings(TransitionInfo.class, false);
			if (cfg1 == null) break;
			
			IBlockState block = cfg1.blockTop;
			if (block == null || block == cfg.blockBot) break;
			
			d.getSettings(TerrainGeneration.class, true).entries
				.add(0, new TransitionGen(block, 0, n + 1, n + cfg1.sizeTop, false));
		} while(false);
	}

	public void initConfig(ConfigConstants cfg) {
		double[] v = cfg.get("custom_noise_octaves", double[].class, new double[0]);
		SharedNoiseFields snf = Dimensionstack.INSTANCE.getSettings(SharedNoiseFields.class, true);
		if (v.length > 0) {
			int l = v.length;
			byte[] oct = new byte[l];
			for (int i = 0; i < l; i++)
				oct[i] = (byte)v[i];
			snf.octaves = oct;
		} else snf.octaves = new byte[] {4};
		snf.noiseFields = new NoiseField[] {new NoiseField(4, 4, 1.0, 1.0)};
		snf.source = new byte[] {0};
		cfg.get("rem_block_gen", BlockRemInfo.class, new BlockRemInfo());
		cfg.get("dim_transitions", TransInfo.class, new TransInfo());
	}

	@Override
	public void addRecipe(Parameters param) {
		ITerrainGenerator gen;
		String key = param.getString(0);
		if (key.equals(NOISE_FIELD)) {
			String name = param.getString(1);
			NoiseFieldInfo nfi = NoiseFieldInfo.REGISTRY.get(name);
			SharedNoiseFields snf = API.INSTANCE.getSettings(SharedNoiseFields.class, true);
			int id, src = param.getIndex(2);
			if (nfi == null) {
				id = snf.source.length;
				nfi = new NoiseFieldInfo(name, id).setSource(src);
				snf.source = Arrays.copyOf(snf.source, id + 1);
				snf.noiseFields = Arrays.copyOf(snf.noiseFields, id + 1);
			} else id = nfi.setSource(src).id;
			snf.source[id] = (byte)src;
			NoiseField nf = new NoiseField(param.getIndex(3), param.getIndex(4), param.getNumber(5), param.getNumber(6));
			snf.noiseFields[id] = nf;
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
				if (eb < 0) eb = 0;
				if (et < 0) et = 0;
			} else throw new IllegalArgumentException("expected 2 or 4 height values @ " + 3);
			gen = new SimpleLayerGen(BlockPredicate.parse(param.get(2, String.class)), y0, y1, eb, et);
		} else if (key.equals(NoiseLayerGen.ID)) {
			Object[] layers = param.getArray(2);
			ArrayList<IBlockState> blocks = new ArrayList<>();
			FloatArrayList levels = new FloatArrayList();
			boolean lastB = false;
			for (Object o : layers) {
				if (o instanceof String) {
					if (lastB) throw new IllegalArgumentException("Blocks must have discriminator values in between!");
					lastB = true;
					blocks.add(BlockPredicate.parse((String)o));
				} else if (o instanceof Double) {
					if (!lastB) blocks.add(null);
					lastB = false;
					levels.add(((Double)o).floatValue());
				}
			}
			if (!lastB) blocks.add(null);
			NoiseFieldInfo cfg = NoiseFieldInfo.REGISTRY.getOrDefault(param.getString(4), NoiseFieldInfo.DEFAULT);
			double gradient = param.getNumber(3) / (double)((1 << cfg.octaves) - 1);
			double[] vec = param.getVectorOrAll(5);
			int idx = cfg.id;
			gen = new NoiseLayerGen(blocks.toArray(new IBlockState[blocks.size()]), levels.toFloatArray(), (float)gradient, (int)vec[0], (int)vec[1], idx);
		} else if (key.equals(NetherTop.ID)) {
			Object[] mats = param.getArray(2);
			int n = mats.length;
			IBlockState[] blocks = new IBlockState[n];
			for (int i = 0; i < n; i++)
				blocks[i] = BlockPredicate.parse((String)mats[i]);
			double[] vec = param.getVector(3);
			boolean hasLake = vec[1] > 0, hasSand = vec[2] > 0;
			gen = new NetherTop((int)vec[0], (int)vec[3], param.getIndex(4),
					hasLake ? (int)vec[1] : 0, hasSand ? (int)vec[2] : 0,
					blocks[0], hasLake || hasSand ? blocks[1] : null,
					hasSand ? blocks[2] : null, hasSand ? blocks[3] : null,
					hasSand ? blocks[4] : null, hasSand ? blocks[5] : null);
		} else return;
		TerrainGeneration cfg = PortalConfiguration.get(param.getIndex(1)).getSettings(TerrainGeneration.class, true);
		cfg.entries.add(gen);
	}

	static class NoiseFieldInfo {
		static HashMap<String, NoiseFieldInfo> REGISTRY = new HashMap<>();
		static final NoiseFieldInfo DEFAULT = new NoiseFieldInfo("main", 0).setSource(0);

		final int id;
		int octaves;

		NoiseFieldInfo(String name, int id) {
			this.id = id;
			if (REGISTRY.put(name, this) != null)
				throw new IllegalStateException("noise field '" + name + "' already registered");
		}

		NoiseFieldInfo setSource(int src) {
			switch(src) {
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
				if (snf != null && src >= 0 && src < snf.octaves.length) {
					this.octaves = snf.octaves[src];
					break;
				}
				throw new IllegalArgumentException("invalid noise gen source " + src);
			}
			return this;
		}

	}

	private static class BlockRemInfo implements IOperand {

		@Override
		public boolean asBool() throws Error {return true;}
		@Override
		public Object value() {return this;}

		@Override
		public IOperand get(IOperand idx) {
			DisabledBlockGen cfg = PortalConfiguration.get(idx.asIndex()).getSettings(DisabledBlockGen.class, false);
			return cfg == null || cfg.disabledBlock == null ? Nil.NIL : new Text(BlockPredicate.serialize(cfg.disabledBlock));
		}

		@Override
		public void put(IOperand idx, IOperand val) {
			boolean set = val instanceof Text;
			DisabledBlockGen cfg = PortalConfiguration.get(idx.asIndex()).getSettings(DisabledBlockGen.class, set);
			if (set) 
				cfg.disabledBlock = BlockPredicate.parse(((Text)val).value);
			else if (cfg != null)
				cfg.disabledBlock = null;
		}

	}

	private static class TransInfo implements IOperand {

		@Override
		public boolean asBool() throws Error {return true;}
		@Override
		public Object value() {return this;}

		@Override
		public IOperand get(IOperand idx) {
			TransitionInfo cfg = PortalConfiguration.get(idx.asIndex()).getSettings(TransitionInfo.class, false);
			return cfg == null ? Nil.NIL : new Array(
					cfg.blockBot == null ? Nil.NIL : new Text(BlockPredicate.serialize(cfg.blockBot)), new Number(cfg.sizeBot),
					cfg.blockTop == null ? Nil.NIL : new Text(BlockPredicate.serialize(cfg.blockTop)), new Number(cfg.sizeTop)
				);
		}

		@Override
		public void put(IOperand idx, IOperand val) {
			if (!(val instanceof Array)) return;
			IOperand[] arr = ((Array)val).array;
			if (arr.length != 4) return;
			TransitionInfo cfg = PortalConfiguration.get(idx.asIndex()).getSettings(TransitionInfo.class, true);
			cfg.blockBot = arr[0] instanceof Text ? BlockPredicate.parse(((Text)arr[0]).value) : null;
			cfg.blockTop = arr[2] instanceof Text ? BlockPredicate.parse(((Text)arr[2]).value) : null;
			cfg.sizeBot = arr[1].asIndex();
			cfg.sizeTop = arr[3].asIndex();
		}

	}

}
