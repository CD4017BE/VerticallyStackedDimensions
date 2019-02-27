package cd4017be.dimstack.api.util;

import cd4017be.dimstack.api.IDimension;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Handles a custom dimension configuration button that leads to a sub screen.
 * @author CD4017BE
 */
@SideOnly(Side.CLIENT)
public interface ICfgButtonHandler {

	/**
	 * @param dim dimension to configure
	 * @return whether to show the button
	 */
	boolean showButton(IDimension dim);

	/**
	 * @param dim dimension to configure
	 * @return the text to display on the button
	 */
	String getButtonName(IDimension dim);

	/**
	 * @param parent the current screen to return to on close
	 * @param dim dimension to configure
	 * @return the screen to show when button is clicked
	 */
	GuiScreen getGui(GuiScreen parent, IDimension dim);

}
