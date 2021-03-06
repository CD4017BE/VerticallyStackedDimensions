package cd4017be.dimstack;

import java.io.File;

import org.apache.logging.log4j.Logger;

import cd4017be.api.recipes.RecipeScriptContext;
import cd4017be.dimstack.asm.Test;
import cd4017be.dimstack.command.Regen;
import cd4017be.dimstack.core.Dimensionstack;
import cd4017be.lib.script.ScriptFiles.Version;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
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

	public static final Dimensionstack dimstack = new Dimensionstack();

	public static Logger LOG;

	public Main() {
		RecipeScriptContext.scriptRegistry.add(new Version(ConfigName, "/assets/" + ID + "/config/recipes.rcp"));
	}

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		LOG = event.getModLog();
		Test.run();
		MinecraftForge.EVENT_BUS.register(proxy);
		proxy.init();
		RecipeScriptContext.instance.run(ConfigName + ".PRE_INIT");
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		proxy.registerRenderers();
		Objects.init();
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
	}

	@Mod.EventHandler
	public void serverStart(FMLServerAboutToStartEvent event) {
		dimstack.loadWorldSettings(
			new File(FMLCommonHandler.instance().getSavesDirectory(), event.getServer().getFolderName())
		);
	}

	@Mod.EventHandler
	public void registerCommands(FMLServerStartingEvent event) {
		event.registerServerCommand(new Regen());
	}

	@Mod.EventHandler
	public void serverStop(FMLServerStoppingEvent event) {
		dimstack.cleanup();
	}

}
