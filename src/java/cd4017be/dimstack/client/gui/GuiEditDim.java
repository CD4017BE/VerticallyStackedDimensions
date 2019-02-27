package cd4017be.dimstack.client.gui;

import java.io.IOException;
import java.util.ArrayList;
import cd4017be.dimstack.ClientProxy;
import cd4017be.dimstack.Main;
import cd4017be.dimstack.api.TerrainGeneration;
import cd4017be.dimstack.api.util.ICfgButtonHandler;
import cd4017be.dimstack.core.PortalConfiguration;

import static cd4017be.lib.util.TooltipUtil.translate;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiListButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
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
		title = translate("gui.dimstack.edit") + " [§e" + dim + "§r]";
		
		int n = entries.size();
		int x = width / 2, y = (height - (n + 1) / 2 * 28 - 20) / 2;
		if (dim.up() != null) {
			GuiTextField tf = new GuiTextField(1, fontRenderer, x - 38, y, 30, 20);
			tf.setMaxStringLength(3);
			tf.setText("" + dim.ceilY);
			tf.setGuiResponder(this);
			textFields.add(tf);
			GuiLabel l = new GuiLabel(fontRenderer, 0, x - 158, y, 120, 20, 0xc0c0c0).setCentered();
			l.addLine(translate("gui.dimstack.ceil"));
			labelList.add(l);
		}
		TerrainGeneration cfg = dim.getSettings(TerrainGeneration.class, false);
		addButton(new GuiListButton(this, 2, x + 8, y, "gui.dimstack.bedrock", cfg != null && cfg.disabledBlock == Blocks.BEDROCK.getDefaultState()));
		y += 28; x -= 158;
		for (int i = 0; i < n; i++)
			addButton(new GuiButton(3 + i, x + (i&1) * 166, y + (i>>1) * 28, 150, 20, entries.get(i).getButtonName(dim)));
	}

	@Override
	protected void actionPerformed(GuiButton b) throws IOException {
		int id = b.id - 3;
		if (id >= 0 && id < entries.size()) {
			GuiScreen gui = entries.get(id).getGui(this, dim);
			if (gui == null) gui = this;
			mc.displayGuiScreen(gui);
		} else super.actionPerformed(b);
	}

	@Override
	public void setEntryValue(int id, boolean value) {
		switch(id) {
		case 2: {
			TerrainGeneration cfg = dim.getSettings(TerrainGeneration.class, true);
			cfg.disabledBlock = value ? Blocks.BEDROCK.getDefaultState() : null;
		}	break;
		}
	}

	@Override
	public void setEntryValue(int id, float value) {
	}

	@Override
	public void setEntryValue(int id, String value) {
		switch(id) {
		case 1:
			try {
				int h = Integer.parseInt(value);
				if (h < 1) h = 1;
				if (h > 255) h = 255;
				dim.ceilY = h;
				textFields.get(0).setText(Integer.toString(h));
			} catch (NumberFormatException e) {}
			break;
		}
	}

}
