package cd4017be.dimstack.client;

import cd4017be.dimstack.api.IDimension;
import cd4017be.dimstack.client.gui.GuiWorldProvider;
import cd4017be.dimstack.core.Dimensionstack;

/** @author CD4017BE */
public class WorldProviderHandler extends CfgButtonHandler {

	public WorldProviderHandler(String name) {
		super(name, GuiWorldProvider::new);
	}

	@Override
	public boolean showButton(IDimension dim) {
		return Dimensionstack.isDimCreated(dim.id());
	}

}
