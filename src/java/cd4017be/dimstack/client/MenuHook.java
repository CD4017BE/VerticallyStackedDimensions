package cd4017be.dimstack.client;

import java.util.List;

import cd4017be.dimstack.Main;
import cd4017be.dimstack.client.gui.GuiDimStack;
import cd4017be.lib.util.TooltipUtil;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiCreateWorld;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author CD4017BE
 *
 */
@SideOnly(Side.CLIENT)
public class MenuHook {

	private GuiButton dimstackCfgButton;

	@SubscribeEvent
	public void initScreen(GuiScreenEvent.InitGuiEvent.Post event) {
		GuiScreen gui = event.getGui();
		if (gui instanceof GuiCreateWorld) {
			List<GuiButton> list = event.getButtonList();
			GuiButton cfg = new GuiButton(list.size(), gui.width / 2 - 100, gui.height - 52, TooltipUtil.translate("gui.dimstack.cfg"));
			for (GuiButton b : list)
				if (b.id == 3) {
					cfg.width = b.width;
					cfg.y = b.y;
					cfg.x = gui.width / 2 + 5;
					b.x = gui.width / 2 - 5 - b.width;
					break;
				}
			list.add(dimstackCfgButton = cfg);
		}
	}

	@SubscribeEvent
	public void clickButton(GuiScreenEvent.ActionPerformedEvent.Pre event) {
		if (event.getButton() == dimstackCfgButton) {
			Main.dimstack.reset();
			GuiScreen gui = event.getGui();
			gui.mc.displayGuiScreen(new GuiDimStack(gui));
		}
	}
}
