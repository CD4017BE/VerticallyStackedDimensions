package cd4017be.dimstack;

import cd4017be.api.recipes.RecipeScriptContext;
import cd4017be.api.recipes.RecipeScriptContext.ConfigConstants;
import cd4017be.dimstack.core.ChunkLoader;
import cd4017be.dimstack.core.PortalConfiguration;
import cd4017be.dimstack.worldgen.BlockReplacer;
import cd4017be.dimstack.worldgen.NetherTop;
import cd4017be.dimstack.worldgen.OreGen;
import cd4017be.dimstack.worldgen.PortalGen;
import cd4017be.lib.TickRegistry;

/**
 * 
 * @author cd4017be
 */
public class CommonProxy {

	PortalGen worldgenPortal;
	BlockReplacer worldgenBedrock;
	NetherTop worldgenNether;
	OreGen worldgenOres;

	public void init() {
		TickRegistry.register();
		
		worldgenPortal = new PortalGen();
		worldgenBedrock = new BlockReplacer();
		worldgenOres = new OreGen();
		setConfig();
	}

	private void setConfig() {
		ConfigConstants cfg = new ConfigConstants(RecipeScriptContext.instance.modules.get(Main.ConfigName));
		ChunkLoader.init(cfg);
		Object[] arr = cfg.get("linked_dimensions", Object[].class, new Object[0]);
		for (Object o : arr)
			if (o instanceof double[]) {
				double[] vec = (double[])o;
				int[] dims = new int[vec.length];
				for (int i = 0; i < vec.length; i++)
					dims[i] = (int)vec[i];
				PortalConfiguration.link(dims);
			}
		int n = (int)cfg.getNumber("gen_topNether", Double.NEGATIVE_INFINITY);
		if (n >= 0) worldgenNether = new NetherTop(n);
		worldgenOres.initConfig(cfg);
	}

	public void registerRenderers() {
	}

}
