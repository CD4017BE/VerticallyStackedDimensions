package cd4017be.dimstack.worldgen;

import java.util.Random;

import cd4017be.api.recipes.RecipeAPI;
import cd4017be.api.recipes.RecipeAPI.IRecipeHandler;
import cd4017be.api.recipes.RecipeScriptContext.ConfigConstants;
import cd4017be.dimstack.Main;
import cd4017be.dimstack.api.DisableVanillaOres;
import cd4017be.dimstack.api.OreGeneration;
import cd4017be.dimstack.api.gen.IOreGenerator;
import cd4017be.dimstack.api.util.BlockPredicate;
import cd4017be.dimstack.core.PortalConfiguration;
import cd4017be.lib.script.Parameters;
import cd4017be.lib.script.obj.Error;
import cd4017be.lib.script.obj.IOperand;
import cd4017be.lib.script.obj.Number;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.OreGenEvent;
import net.minecraftforge.event.terraingen.OreGenEvent.GenerateMinable.EventType;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * 
 * @author cd4017be
 */
public class OreGenHandler implements IWorldGenerator, IRecipeHandler {

	private static final String OREGEN = "oregen";
	static boolean registered;

	public static void register() {
		if (registered) return;
		registered = true;
		MinecraftForge.ORE_GEN_BUS.register(Main.proxy.worldgenOres);
	}

	public OreGenHandler() {
		GameRegistry.registerWorldGenerator(this, 0);
		RecipeAPI.Handlers.put(OREGEN, this);
		OreGeneration.REGISTRY.put("even", OreGenEven::new);
		OreGeneration.REGISTRY.put("center", OreGenCentered::new);
		OreGeneration.REGISTRY.put("gauss", OreGenGaussian::new);
	}

	@SubscribeEvent
	public void onGenerate(OreGenEvent.GenerateMinable event) {
		DisableVanillaOres cfg = PortalConfiguration.get(event.getWorld()).getSettings(DisableVanillaOres.class, false);
		if (cfg == null) return;
		if (cfg.disabled(event.getType()))
			event.setResult(Result.DENY);
	}

	public void initConfig(ConfigConstants cfg) {
		DisableVanillaOres d = PortalConfiguration.get(0).getSettings(DisableVanillaOres.class, true);
		for (EventType t : EventType.values()) {
			String id = "disable_" + t.name().toLowerCase();
			d.setDisabled(t, cfg.get(id, Boolean.class, Boolean.FALSE));
			cfg.get(id, OreDisableInfo.class, new OreDisableInfo(t));
		}
		if (d.disabled(EventType.QUARTZ))
			PortalConfiguration.get(-1).getSettings(DisableVanillaOres.class, true).setDisabled(EventType.QUARTZ, true);
	}

	@Override
	public void generate(Random random, int cx, int cy, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
		OreGeneration cfg = PortalConfiguration.get(world).getSettings(OreGeneration.class, false);
		if (cfg == null) return;
		Chunk chunk = chunkProvider.provideChunk(cx, cy);
		for (IOreGenerator og : cfg.entries)
			og.generate(chunk, random);
	}

	@Override
	public void addRecipe(Parameters param) {
		OreGeneration cfg = PortalConfiguration.get(param.getIndex(1)).getSettings(OreGeneration.class, true);
		BlockPredicate target = BlockPredicate.parse(param.get(2));
		float veins = (float)param.getNumber(3);
		int count = param.getIndex(4);
		IBlockState ore = BlockPredicate.parse(param.get(5, String.class));
		double[] heights = param.getVectorOrAll(7);
		
		String type = param.getString(6);
		if (type.startsWith("even"))
			cfg.entries.add(new OreGenEven(ore, count, veins, target, (int)heights[0], (int)heights[1]));
		else if (type.startsWith("center"))
			cfg.entries.add(new OreGenCentered(ore, count, veins, target, (int)heights[0], (int)heights[1], (int)heights[2]));
		else if (type.startsWith("gauss"))
			cfg.entries.add(new OreGenGaussian(ore, count, veins, target, (float)heights[0], (float)heights[1]));
		else throw new IllegalArgumentException("invalid ore distribution mode: " + type);
	}

	private static class OreDisableInfo implements IOperand {

		private final EventType type;

		OreDisableInfo(EventType type) {
			super();
			this.type = type;
		}

		@Override
		public boolean asBool() throws Error {return true;}
		@Override
		public Object value() {return this;}

		@Override
		public IOperand get(IOperand idx) {
			DisableVanillaOres cfg = PortalConfiguration.get(idx.asIndex()).getSettings(DisableVanillaOres.class, false);
			return cfg != null && cfg.disabled(type) ? Number.TRUE : Number.FALSE;
		}

		@Override
		public void put(IOperand idx, IOperand val) {
			try {
				boolean disable = val.asBool();
				DisableVanillaOres cfg = PortalConfiguration.get(idx.asIndex()).getSettings(DisableVanillaOres.class, disable);
				if (cfg != null) cfg.setDisabled(type, disable);
			} catch (Error e) {e.printStackTrace();}
		}

	}

}
