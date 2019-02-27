package cd4017be.dimstack.api;

import cd4017be.dimstack.api.util.ICfgButtonHandler;
import cd4017be.dimstack.api.util.SettingProvider;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The central dimension stack API.
 * @author CD4017BE
 */
public abstract class API extends SettingProvider {

	/**
	 * the API implementation which gets set during PRE_INIT phase
	 */
	public static API INSTANCE;

	/**
	 * @param id current dimension id
	 * @return the dimension stack element for the given dimension
	 */
	public abstract IDimension getDim(int id);

	/**
	 * @param world current world
	 * @return the dimension stack element for the given world's dimension
	 */
	public IDimension getDim(World world) {
		return getDim(world.provider.getDimension());
	}

	/**
	 * add a custom dimension configuration screen
	 * @param handler the button handler to register
	 */
	@SideOnly(Side.CLIENT)
	public abstract void registerConfigGui(ICfgButtonHandler handler);

	/**@deprecated internal function */
	public abstract void registerOreDisable();

}
