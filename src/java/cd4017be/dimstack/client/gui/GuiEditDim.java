package cd4017be.dimstack.client.gui;

import java.io.IOException;
import java.util.ArrayList;
import cd4017be.dimstack.ClientProxy;
import cd4017be.dimstack.Main;
import cd4017be.dimstack.api.DisabledBlockGen;
import cd4017be.dimstack.api.util.ICfgButtonHandler;
import cd4017be.dimstack.core.PortalConfiguration;

import static cd4017be.lib.util.TooltipUtil.translate;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiListButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlider;
import net.minecraft.client.gui.GuiPageButtonList.GuiResponder;
import net.minecraft.init.Blocks;

/**
 * @author CD4017BE
 *
 */
public class GuiEditDim extends GuiMenuBase implements GuiResponder {

	private final PortalConfiguration dim;
	private final ArrayList<ICfgButtonHandler> entries;

	public GuiEditDim(GuiScreen parent, PortalConfiguration dim) {
		super(parent);
		this.dim = dim;
		this.entries = new ArrayList<ICfgButtonHandler>();
		for (ICfgButtonHandler b : ((ClientProxy)Main.proxy).cfgButtons)
			if (b.showButton(dim))
				entries.add(b);
	}

	@Override
	public void initGui() {
		super.initGui();
		title = translate("gui.dimstack.edit") + " [\u00a7e" + dim + "\u00a7r]";
		
		int n = entries.size();
		int x = width / 2 - 158, y = (height - (n + 2) / 2 * 28 - 20) / 2;
		addButton(new GuiSlider(this, 1, x, y, translate("gui.dimstack.ceil"), 1, 255, dim.ceilY, (id, name, val)-> String.format("%s: %.0f", name, val))).setWidth(264);
		addButton(new GuiButton(2, x + 264, y, translate(dim.flipped ? "gui.dimstack.flip1" : "gui.dimstack.flip0"))).setWidth(52);
		y += 28;
		DisabledBlockGen cfg = dim.getSettings(DisabledBlockGen.class, false);
		addButton(new GuiListButton(this, 3, x, y, "gui.dimstack.bedrock", cfg != null && cfg.disabledBlock == Blocks.BEDROCK.getDefaultState()));
		for (int i = 1; i <= n; i++)
			addButton(new GuiButton(3 + i, x + (i&1) * 166, y + (i>>1) * 28, 150, 20, entries.get(i - 1).getButtonName(dim)));
	}

	@Override
	protected void actionPerformed(GuiButton b) throws IOException {
		int id = b.id - 4;
		if (id >= 0 && id < entries.size()) {
			GuiScreen gui = entries.get(id).getGui(this, dim);
			if (gui == null) gui = this;
			mc.displayGuiScreen(gui);
		} else if (id == -2) {
			dim.flipped = !dim.flipped;
			b.displayString = translate(dim.flipped ? "gui.dimstack.flip1" : "gui.dimstack.flip0");
		} else super.actionPerformed(b);
	}

	@Override
	public void setEntryValue(int id, boolean value) {
		switch(id) {
		case 3: {
			DisabledBlockGen cfg = dim.getSettings(DisabledBlockGen.class, true);
			cfg.disabledBlock = value ? Blocks.BEDROCK.getDefaultState() : null;
		}	break;
		}
	}

	@Override
	public void setEntryValue(int id, float value) {
		dim.ceilY = Math.round(value);
	}

	@Override
	public void setEntryValue(int id, String value) {
	}

}
