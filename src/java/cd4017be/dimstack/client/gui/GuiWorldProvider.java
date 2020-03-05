package cd4017be.dimstack.client.gui;

import static cd4017be.lib.util.TooltipUtil.translate;
import java.io.IOException;
import cd4017be.dimstack.api.CustomWorldProps;
import cd4017be.dimstack.api.IDimension;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiListButton;
import net.minecraft.client.gui.GuiPageButtonList.GuiResponder;
import net.minecraft.client.gui.GuiScreen;
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
		int n = 4, x = width / 2 - 158, y = (height - n / 2 * 28 - 20) / 2;
		addButton(new GuiListButton(this, 1, x, y, "gui.dimstack.skylight", cfg.skylight));
		addButton(new GuiListButton(this, 2, x + 166, y, "gui.dimstack.skybox", cfg.visibleSky));
		y += 28;
		addButton(new GuiListButton(this, 3, x, y, "gui.dimstack.nether", cfg.netherlike));
		addButton(new GuiListButton(this, 4, x + 166, y, "gui.dimstack.nowater", cfg.noWater));
		y += 28;
		GuiLabel l;
		labelList.add(l = new GuiLabel(fontRenderer, 0, x, y, 150, 10, 0xff808080));
		l.addLine(translate("gui.dimstack.biome"));
		labelList.add(l = new GuiLabel(fontRenderer, 1, x + 166, y, 150, 10, 0xff808080));
		l.addLine(translate("gui.dimstack.chunkgen"));
		y += 10;
		textFields.add(biomes = new GuiBlockSel(5, fontRenderer, complete, x, y, 150, 20));
		biomes.setText(cfg.biomeGen);
		biomes.setGuiResponder(this);
		addButton(new GuiButton(6, x + 166, y, translate("gui.dimstack.chunkgen" + Integer.toHexString(cfg.chunkGen)))).setWidth(150);
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
		switch(id) {
		case 1:
			cfg.skylight = value;
			break;
		case 2:
			cfg.visibleSky = value;
			break;
		case 3:
			cfg.netherlike = value;
			break;
		case 4:
			cfg.noWater = value;
			break;
		}
	}

	@Override
	public void setEntryValue(int id, float value) {
	}

	@Override
	public void setEntryValue(int id, String value) {
		switch(id) {
		case 5:
			cfg.biomeGen = value;
			break;
		}
	}

	@Override
	protected void actionPerformed(GuiButton b) throws IOException {
		if (b.id == 6) {
			if (++cfg.chunkGen >= 4) cfg.chunkGen = 0;
			b.displayString = translate("gui.dimstack.chunkgen" + Integer.toHexString(cfg.chunkGen));
		} else super.actionPerformed(b);
	}

}
