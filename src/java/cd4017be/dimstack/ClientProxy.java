package cd4017be.dimstack;

import cd4017be.lib.BlockItemRegistry;
import cd4017be.lib.render.SpecialModelLoader;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import static cd4017be.dimstack.Objects.*;

import java.util.ArrayList;

import cd4017be.dimstack.api.util.ICfgButtonHandler;
import cd4017be.dimstack.client.CfgButtonHandler;
import cd4017be.dimstack.client.MenuHook;
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
	}

	@Override
	public void registerRenderers() {
		super.registerRenderers();
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
