package cd4017be.dimstack;

import cd4017be.api.recipes.RecipeScriptContext;
import cd4017be.api.recipes.RecipeScriptContext.ConfigConstants;
import cd4017be.dimstack.core.ChunkLoader;
import cd4017be.dimstack.worldgen.BlockReplacer;
import cd4017be.dimstack.worldgen.NetherTop;
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
	public NetherTop worldgenNether;
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
		ChunkLoader.init(cfg);
		Main.dimstack.init(cfg);
		int n = (int)cfg.getNumber("gen_topNether", Double.NEGATIVE_INFINITY);
		if (n >= 0) worldgenNether = new NetherTop(n);
		worldgenOres.initConfig(cfg);
		worldgenTerrain.initConfig(cfg);
	}

	public void registerRenderers() {
	}

}
