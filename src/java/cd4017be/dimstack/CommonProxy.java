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
		
	}

	public void registerRenderers() {
	}

}
