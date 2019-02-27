package cd4017be.dimstack.client;

import java.util.function.BiFunction;

import cd4017be.dimstack.api.IDimension;
import cd4017be.dimstack.api.util.ICfgButtonHandler;
import cd4017be.lib.util.TooltipUtil;
import net.minecraft.client.gui.GuiScreen;


/**
 * @author CD4017BE
 *
 */
public class CfgButtonHandler implements ICfgButtonHandler {

	private final String name;
	private final BiFunction<GuiScreen, IDimension, GuiScreen> constr;

	public CfgButtonHandler(String name, BiFunction<GuiScreen, IDimension, GuiScreen> constr) {
		this.name = name;
		this.constr = constr;
	}

	@Override
	public boolean showButton(IDimension dim) {
		return true;
	}

	@Override
	public String getButtonName(IDimension dim) {
		return TooltipUtil.translate(name);
	}

	@Override
	public GuiScreen getGui(GuiScreen parent, IDimension dim) {
		return constr.apply(parent, dim);
	}

}
