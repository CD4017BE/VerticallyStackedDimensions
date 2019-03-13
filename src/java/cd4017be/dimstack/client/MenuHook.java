package cd4017be.dimstack.client;

import java.util.ArrayList;
import java.util.List;

import cd4017be.dimstack.Main;
import cd4017be.dimstack.client.gui.GuiDimStack;
import cd4017be.lib.util.TooltipUtil;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiCreateWorld;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author CD4017BE
 *
 */
@SideOnly(Side.CLIENT)
public class MenuHook {

	private boolean openingGuiCreateWorld = false;
	private Class<?extends GuiScreen> guiCreateWorld;

	private GuiButton dimstackCfgButton;

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void openScreenPre(GuiOpenEvent event) {
		openingGuiCreateWorld = event.getGui() instanceof GuiCreateWorld;
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void openScreenPost(GuiOpenEvent event) {
		//if a mod overrides GuiCreateWorld with its own screen, make sure we inject our button there as well.
		if (openingGuiCreateWorld) guiCreateWorld = event.getGui().getClass();
	}

	@SubscribeEvent
	public void initScreen(GuiScreenEvent.InitGuiEvent.Post event) {
		GuiScreen gui = event.getGui();
		if (gui.getClass().equals(guiCreateWorld)) {
			List<GuiButton> list = event.getButtonList();
			//find a nice spot to place our button on the screen...
			int x, y, w, h;
			do {
				//attempt 1: next to bottom most centered button
				x = (gui.width - 150) / 2;
				y = 0;
				w = 150;
				h = 20;
				GuiButton b = null;
				for (GuiButton b1 : list)
					if (b1.x == x && b1.width == w && b1.height == h && b1.y > y) {
						y = b1.y;
						b = b1;
					}
				if (b != null && collision((x = (gui.width + 10) / 2) - 160, y, 310, h, list, b) == null) {
					b.x = x - 160;
					y = b.y;
					continue;
				}
				//attempt 2: bottom right screen corner
				if (collision(x = gui.width - w - 8, y = gui.height - h - 8, w, h, list, null) == null) continue;
				//attempt 3: bottom left screen corner
				if (collision(x = 8, y, w, h, list, null) == null) continue;
				//attempt 4: reorder and if needed also resize all buttons at the bottom of the screen so that our button fits in there too.
				ArrayList<GuiButton> botLine = new ArrayList<>();
				int l = w + 16;
				for (GuiButton b1 : list)
					if (b1.y + b1.height >= y) {
						botLine.add(b1);
						l += b1.width + 8;
					}
				botLine.sort((p, q)-> p.x - q.x);
				float scale, ofs;
				if (l > gui.width) {
					scale = (float)gui.width / (float)l;
					ofs = 0F;
				} else {
					scale = 1F;
					ofs = (float)(gui.width - l) / 2F;
				}
				for (GuiButton b1 : botLine) {
					b1.y = y;
					b1.x = (int)(ofs += 8F * scale);
					float d = (float)b1.width * scale;
					ofs += d;
					b1.width = (int)d;
				}
				x = (int)(ofs + 8F * scale);
				w = (int)((float)w * scale);
			} while (false);
			list.add(dimstackCfgButton = new GuiButton(list.size(), x, y, w, h, TooltipUtil.translate("gui.dimstack.cfg")));
		}
	}

	private static GuiButton collision(int x, int y, int w, int h, List<GuiButton> list, GuiButton except) {
		for (GuiButton b : list)
			if (b != except && x + w >= b.x && x <= b.x + b.width && y + h >= b.y && y <= b.y + b.height)
				return b;
		return null;
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
