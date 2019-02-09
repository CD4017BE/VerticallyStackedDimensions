package cd4017be.dimstack.api;

import net.minecraft.world.World;

/**
 * The central dimension stack API.
 * @author CD4017BE
 */
public abstract class API {

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

	/**@deprecated internal function */
	public abstract void registerOreDisable();

}
