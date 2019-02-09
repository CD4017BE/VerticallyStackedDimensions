package cd4017be.dimstack.api;

import net.minecraft.world.WorldServer;

/**
 * @author CD4017BE
 *
 */
public interface IDimension {

	/**
	 * @return the dimension id
	 */
	int id();

	/**
	 * @return the destination when traversing up (null for regular world border)
	 */
	IDimension up();

	/**
	 * @return the destination when traversing down (null for regular world border)
	 */
	IDimension down();

	/**
	 * @return the highest dimension in current stack or null if looped
	 */
	IDimension top();

	/**
	 * @return the lowest dimension in current stack or null if looped
	 */
	IDimension bottom();

	/**
	 * @return the server world for this dimension
	 */
	WorldServer getWorld();

	/**
	 * @param type setting type
	 * @param create whether to create a new settings instance if absent
	 * @return the settings of given type
	 */
	<T extends IDimensionSettings> T getSettings(Class<T> type, boolean create);

	/**
	 * attempts to add the given dimension into an dimension stack by placing it between this dimension and this.{@link #up()}.<dl>
	 * Note: this method should not be used anymore after the server has started loading a world
	 * @param dim the dimension to insert on top
	 */
	void insertTop(IDimension dim);

	/**
	 * attempts to add the given dimension into an dimension stack by placing it between this dimension and this.{@link #down()}.<dl>
	 * Note: this method should not be used anymore after the server has started loading a world
	 * @param dim the dimension to insert at the bottom
	 */
	void insertBottom(IDimension dim);

}
