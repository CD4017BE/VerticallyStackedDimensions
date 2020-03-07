package cd4017be.dimstack.client.gui;

import static cd4017be.lib.util.TooltipUtil.translate;
import static cd4017be.dimstack.api.CustomWorldProps.*;
import java.io.IOException;
import cd4017be.dimstack.api.CustomWorldProps;
import cd4017be.dimstack.api.IDimension;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiListButton;
import net.minecraft.client.gui.GuiPageButtonList.GuiResponder;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlider;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.world.biome.Biome;

/** @author CD4017BE */
public class GuiWorldProvider extends GuiMenuBase implements GuiResponder {

	private final IDimension dim;
	private final CustomWorldProps cfg;
	private AutoCompletion<Biome> complete;
	private GuiBlockSel biomes;

	/** @param parent */
	public GuiWorldProvider(GuiScreen parent, IDimension dim) {
		super(parent);
		this.dim = dim;
		this.cfg = dim.getSettings(CustomWorldProps.class, true);
	}

	@Override
	public void initGui() {
		super.initGui();
		complete = AutoCompletions.biomes();
		title = translate("gui.dimstack.world_provider") + " [\u00a7e" + dim + "\u00a7r]";
		int n = 4, x = width / 2 - 158, y = (height - n * 28 - 20) / 2;
		GuiTextField tf;
		GuiLabel l;
		addButton(new GuiSlider(this, 9, x, y, translate("gui.dimstack.horizon"), 0, 256, cfg.horizonHeight, (id, name, val)-> String.format(name.startsWith("Format error: ") ? name.substring(14) : name, val))).setWidth(150);
		addButton(new GuiSlider(this, 10, x + 166, y, translate("gui.dimstack.clouds"), 0, 256, cfg.cloudHeight, (id, name, val)-> String.format(name.startsWith("Format error: ") ? name.substring(14) : name, val))).setWidth(150);
		y += 28;
		addButton(new GuiListButton(this, 5, x, y, "gui.dimstack.fog", (cfg.flags & F_FOG) != 0));
		labelList.add(l = new GuiLabel(fontRenderer, 0, x + 166, y, 90, 20, 0xff808080));
		l.addLine(translate("gui.dimstack.fogc"));
		l.addLine("hex: YYRRGGBB");
		textFields.add(tf = new GuiTextField(6, fontRenderer, x + 256, y, 60, 20));
		tf.setText(String.format("%08X", cfg.fogColor));
		tf.setGuiResponder(this);
		y += 28;
		addButton(new GuiListButton(this, 1, x, y, "gui.dimstack.skylight", (cfg.flags & F_SKYLIGHT) != 0));
		addButton(new GuiListButton(this, 2, x + 166, y, "gui.dimstack.skybox", (cfg.flags & F_SKYBOX) != 0));
		y += 28;
		addButton(new GuiListButton(this, 3, x, y, "gui.dimstack.nether", (cfg.flags & F_NETHER) != 0));
		addButton(new GuiListButton(this, 4, x + 166, y, "gui.dimstack.nowater", (cfg.flags & F_WATEREVAP) != 0));
		y += 28;
		labelList.add(l = new GuiLabel(fontRenderer, 1, x, y, 150, 10, 0xff808080));
		l.addLine(translate("gui.dimstack.biome"));
		labelList.add(l = new GuiLabel(fontRenderer, 2, x + 166, y, 150, 10, 0xff808080));
		l.addLine(translate("gui.dimstack.chunkgen"));
		y += 10;
		textFields.add(biomes = new GuiBlockSel(7, fontRenderer, complete, x, y, 150, 20));
		biomes.setText(cfg.biomeGen);
		biomes.setGuiResponder(this);
		addButton(new GuiButton(8, x + 166, y, translate("gui.dimstack.chunkgen" + Integer.toHexString(cfg.chunkGen)))).setWidth(150);
	}

	@Override
	public void drawScreen(int mx, int my, float t) {
		super.drawScreen(mx, my, t);
		biomes.drawOverlay();
	}

	@Override
	public void onGuiClosed() {
		complete.dispose();
	}

	@Override
	public void setEntryValue(int id, boolean value) {
		int mask = 0;
		switch(id) {
		case 1:
			mask = F_SKYLIGHT;
			break;
		case 2:
			mask = F_SKYBOX;
			break;
		case 3:
			mask = F_NETHER;
			break;
		case 4:
			mask = F_WATEREVAP;
			break;
		case 5:
			mask = F_FOG;
			break;
		}
		if (value) cfg.flags |= mask;
		else cfg.flags &= ~mask;
	}

	@Override
	public void setEntryValue(int id, float value) {
		switch(id) {
		case 9:
			cfg.horizonHeight = value;
			break;
		case 10:
			cfg.cloudHeight = value;
			break;
		}
	}

	@Override
	public void setEntryValue(int id, String value) {
		switch(id) {
		case 6:
			try {
				cfg.fogColor = Integer.parseInt(value, 16);
			} catch (NumberFormatException e) {}
			break;
		case 7:
			cfg.biomeGen = value;
			break;
		}
	}

	@Override
	protected void actionPerformed(GuiButton b) throws IOException {
		if (b.id == 8) {
			if (++cfg.chunkGen >= 4) cfg.chunkGen = 0;
			b.displayString = translate("gui.dimstack.chunkgen" + Integer.toHexString(cfg.chunkGen));
		} else super.actionPerformed(b);
	}

}
