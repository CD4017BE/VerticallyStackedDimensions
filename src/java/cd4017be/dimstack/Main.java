package cd4017be.dimstack;

import cd4017be.api.recipes.RecipeScriptContext;
import cd4017be.lib.script.ScriptFiles.Version;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/**
 * 
 * @author cd4017be
 */
@Mod(modid = Main.ID, useMetadata = true)
public class Main {

	public static final String ID = "dimstack";
	static final String ConfigName = "verticalDimensionStack";

	@Instance
	public static Main instance;

	@SidedProxy(serverSide = "cd4017be." + ID + ".CommonProxy", clientSide = "cd4017be." + ID + ".ClientProxy")
	public static CommonProxy proxy;

	public Main() {
		RecipeScriptContext.scriptRegistry.add(new Version(ConfigName, "/assets/" + ID + "/config/recipes.rcp"));
	}

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		Objects.init();
		RecipeScriptContext.instance.run(ConfigName + ".PRE_INIT");
		proxy.init();
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		proxy.registerRenderers();
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
	}

}
