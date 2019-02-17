package cd4017be.dimstack;

import cd4017be.api.recipes.RecipeScriptContext;
import cd4017be.api.recipes.RecipeScriptContext.ConfigConstants;
import cd4017be.dimstack.core.ChunkLoader;
import cd4017be.dimstack.core.Dimensionstack;
import cd4017be.dimstack.worldgen.BlockReplacer;
import cd4017be.dimstack.worldgen.OreGenHandler;
import cd4017be.dimstack.worldgen.PortalGen;
import cd4017be.dimstack.worldgen.TerrainGenHandler;
import cd4017be.lib.TickRegistry;

/**
 * 
 * @author cd4017be
 */
public class CommonProxy {

	public PortalGen worldgenPortal;
	public BlockReplacer worldgenBedrock;
	public TerrainGenHandler worldgenTerrain;
	public OreGenHandler worldgenOres;

	public void init() {
		TickRegistry.register();
		
		worldgenTerrain = new TerrainGenHandler();
		worldgenPortal = new PortalGen();
		worldgenBedrock = new BlockReplacer();
		worldgenOres = new OreGenHandler();
		setConfig();
	}

	private void setConfig() {
		ConfigConstants cfg = new ConfigConstants(RecipeScriptContext.instance.modules.get(Main.ConfigName));
		Dimensionstack.initConfig(cfg);
		ChunkLoader.initConfig(cfg);
		worldgenTerrain.initConfig(cfg);
		worldgenOres.initConfig(cfg);
	}

	public void registerRenderers() {
	}

}
