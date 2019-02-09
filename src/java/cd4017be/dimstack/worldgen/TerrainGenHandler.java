package cd4017be.dimstack.worldgen;

import cd4017be.api.recipes.RecipeAPI.IRecipeHandler;
import cd4017be.dimstack.api.TerrainGeneration;
import cd4017be.dimstack.api.gen.ITerrainGenerator;
import cd4017be.dimstack.core.PortalConfiguration;
import cd4017be.lib.script.Parameters;
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
	}

	@SubscribeEvent
	public void init(InitNoiseGensEvent<Context> event) {
		TerrainGeneration cfg = PortalConfiguration.get(event.getWorld()).getSettings(TerrainGeneration.class, false);
		if (cfg != null)
			cfg.setupNoiseGens(event.getNewValues(), event.getRandom());
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
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
		// TODO Auto-generated method stub
		
	}

}
