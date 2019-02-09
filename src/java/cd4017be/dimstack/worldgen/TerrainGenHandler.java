package cd4017be.dimstack.worldgen;

import cd4017be.api.recipes.RecipeAPI;
import cd4017be.api.recipes.RecipeAPI.IRecipeHandler;
import cd4017be.dimstack.api.TerrainGeneration;
import cd4017be.dimstack.api.gen.ITerrainGenerator;
import cd4017be.dimstack.api.util.BlockPredicate;
import cd4017be.dimstack.core.PortalConfiguration;
import cd4017be.lib.script.Parameters;
import net.minecraft.item.ItemStack;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;
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

	public TerrainGenHandler() {
		MinecraftForge.TERRAIN_GEN_BUS.register(this);
		MinecraftForge.EVENT_BUS.register(this);
		RecipeAPI.Handlers.put(SimpleLayerGen.ID, this);
	}

	@SubscribeEvent
	public void init(InitNoiseGensEvent<Context> event) {
		TerrainGeneration cfg = PortalConfiguration.get(event.getWorld()).getSettings(TerrainGeneration.class, false);
		if (cfg != null)
			cfg.setupNoiseGens(event.getNewValues(), event.getRandom());
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void generate(ReplaceBiomeBlocks event) {
		TerrainGeneration cfg = PortalConfiguration.get(event.getWorld()).getSettings(TerrainGeneration.class, false);
		if (cfg == null) return;
		IChunkGenerator gen = event.getGen();
		ChunkPrimer cp = event.getPrimer();
		int cx = event.getX(), cz = event.getZ();
		for (ITerrainGenerator tg : cfg.entries)
			tg.generate(gen, cp, cx, cz, cfg);
	}

	@Override
	public void addRecipe(Parameters param) {
		ITerrainGenerator gen;
		String key = param.getString(0);
		if (key.equals(SimpleLayerGen.ID)) {
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

}
