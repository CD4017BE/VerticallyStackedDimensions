package cd4017be.dimstack.client.gui;

import java.io.IOException;

import cd4017be.dimstack.api.TerrainGeneration;
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

	public GuiEditDim(GuiScreen parent, PortalConfiguration dim) {
		super(parent);
		this.dim = dim;
	}

	@Override
	public void initGui() {
		super.initGui();
		title = translate("gui.dimstack.edit") + " [§e" + dim + "§r]";
		int x = width / 2, y = height / 2 - 38;
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
		y += 28;
		addButton(new GuiButton(3, x - 158, y, 150, 20, translate("gui.dimstack.defOre")));
		addButton(new GuiButton(4, x + 8, y, 150, 20, translate("gui.dimstack.custOre")));
		y += 28;
		addButton(new GuiButton(5, x - 158, y, 150, 20, translate("gui.dimstack.repl")));
		addButton(new GuiButton(6, x + 8, y, 150, 20, translate("gui.dimstack.terrain")));
	}

	@Override
	protected void actionPerformed(GuiButton b) throws IOException {
		switch(b.id) {
		default: super.actionPerformed(b);
		}
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
