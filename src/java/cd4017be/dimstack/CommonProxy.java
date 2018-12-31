package cd4017be.dimstack;

import cd4017be.api.recipes.RecipeScriptContext;
import cd4017be.api.recipes.RecipeScriptContext.ConfigConstants;
import cd4017be.lib.TickRegistry;

/**
 * 
 * @author cd4017be
 */
public class CommonProxy {

	public void init() {
		TickRegistry.register();
		
		setConfig();
	}

	private void setConfig() {
		ConfigConstants cfg = new ConfigConstants(RecipeScriptContext.instance.modules.get(Main.ConfigName));
		Object[] arr = cfg.get("linked_dimensions", Object[].class, new Object[0]);
		for (Object o : arr)
			if (o instanceof double[]) {
				double[] vec = (double[])o;
				int[] dims = new int[vec.length];
				for (int i = 0; i < vec.length; i++)
					dims[i] = (int)vec[i];
				PortalConfiguration.link(dims);
			}
	}

	public void registerRenderers() {
	}

}
