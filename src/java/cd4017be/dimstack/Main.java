package cd4017be.dimstack;

import org.apache.logging.log4j.Logger;

import cd4017be.api.recipes.RecipeScriptContext;
import cd4017be.dimstack.core.PortalConfiguration;
import cd4017be.lib.script.ScriptFiles.Version;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;

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

	public static Logger LOG;

	public Main() {
		RecipeScriptContext.scriptRegistry.add(new Version(ConfigName, "/assets/" + ID + "/config/recipes.rcp"));
	}

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		LOG = event.getModLog();
		MinecraftForge.EVENT_BUS.register(proxy);
		Objects.init();
		proxy.init();
		RecipeScriptContext.instance.run(ConfigName + ".PRE_INIT");
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		proxy.registerRenderers();
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
	}

	@Mod.EventHandler
	public void serverStop(FMLServerStoppingEvent event) {
		PortalConfiguration.cleanup();
	}

}
