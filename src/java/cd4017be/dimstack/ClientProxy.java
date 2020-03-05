package cd4017be.dimstack;

import cd4017be.lib.BlockItemRegistry;
import cd4017be.lib.render.SpecialModelLoader;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import static cd4017be.dimstack.Objects.*;

import java.util.ArrayList;
import cd4017be.api.recipes.RecipeScriptContext.ConfigConstants;
import cd4017be.dimstack.api.util.ICfgButtonHandler;
import cd4017be.dimstack.client.CfgButtonHandler;
import cd4017be.dimstack.client.MenuHook;
import cd4017be.dimstack.client.WorldProviderHandler;
import cd4017be.dimstack.client.gui.GuiBlockLayers;
import cd4017be.dimstack.client.gui.GuiCustomOres;
import cd4017be.dimstack.client.gui.GuiDefOres;
import cd4017be.dimstack.client.gui.GuiTransition;

/**
 * 
 * @author cd4017be
 */
public class ClientProxy extends CommonProxy {

	public final ArrayList<ICfgButtonHandler> cfgButtons = new ArrayList<>();
	public MenuHook menuHook = new MenuHook();

	@Override
	public void init() {
		super.init();
		cfgButtons.add(new CfgButtonHandler("gui.dimstack.trans", GuiTransition::new));
		cfgButtons.add(new CfgButtonHandler("gui.dimstack.defOre", GuiDefOres::new));
		cfgButtons.add(new CfgButtonHandler("gui.dimstack.newOre", GuiCustomOres::new));
		cfgButtons.add(new CfgButtonHandler("gui.dimstack.layer", GuiBlockLayers::new));
		cfgButtons.add(new WorldProviderHandler("gui.dimstack.world_provider"));
	}

	@Override
	protected void setConfig(ConfigConstants cfg) {
		super.setConfig(cfg);
		if (cfg.getNumber("dimstack_editor", 1.0) > 0.0)
			MinecraftForge.EVENT_BUS.register(menuHook);
	}

	@SubscribeEvent
	public void registerModels(ModelRegistryEvent ev) {
		SpecialModelLoader.setMod(Main.ID);
		
		DIM_PIPE.setBlockLayer(BlockRenderLayer.CUTOUT);
		
		BlockItemRegistry.registerRender(PORTAL);
		BlockItemRegistry.registerRender(DIM_PIPE);
		BlockItemRegistry.registerRenderBS(BEDROCK, 0, 15);
	}

}
