package cd4017be.dimstack.client.gui;

import cd4017be.dimstack.api.DisableVanillaOres;
import cd4017be.dimstack.api.IDimension;

import static cd4017be.lib.util.TooltipUtil.translate;
import net.minecraft.client.gui.GuiListButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiPageButtonList.GuiResponder;
import net.minecraftforge.event.terraingen.OreGenEvent.GenerateMinable.EventType;

/**
 * @author CD4017BE
 *
 */
public class GuiDefOres extends GuiMenuBase implements GuiResponder {

	private final DisableVanillaOres cfg;

	/**
	 * @param parent
	 */
	public GuiDefOres(GuiScreen parent, IDimension dim) {
		super(parent);
		this.cfg = dim.getSettings(DisableVanillaOres.class, true);
	}

	@Override
	public void initGui() {
		super.initGui();
		title = translate("gui.dimstack.defOre");
		EventType[] ores = EventType.values();
		int n = ores.length, x = (width - 158 * 3 + 8) / 2, y = (height - (n + 1) / 3 * 24 + 8) / 2;
		for (int i = 0; i < n; i++)
			addButton(new GuiListButton(this, i, x + (i % 3) * 158, y + (i / 3) * 24, ores[i].toString(), !cfg.disabled(ores[i])));
		addButton(new GuiListButton(this, -1, width - 158, height - 28, translate("gui.dimstack.selall"), false));
	}

	@Override
	public void setEntryValue(int id, boolean value) {
		if (id == -1) {
			for (EventType t : EventType.values()) {
				cfg.setDisabled(t, value);
				((GuiListButton)buttonList.get(t.ordinal() + 1)).setValue(!value);
			}
		} else cfg.setDisabled(EventType.values()[id], !value);
	}

	@Override
	public void setEntryValue(int id, float value) {
	}

	@Override
	public void setEntryValue(int id, String value) {
	}

}
