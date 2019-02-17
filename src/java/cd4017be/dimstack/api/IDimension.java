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
	 * @return the height of this dimension in the stack (relative to an arbitrary "ground level" dimension)
	 */
	int height();

	/**
	 * @return the height at which ceiling portals are generated in this dimension (if it has up() != null)
	 */
	int ceilHeight();

	/**
	 * @return the destination when traversing up (null for regular world border)
	 */
	IDimension up();

	/**
	 * @return the destination when traversing down (null for regular world border)
	 */
	IDimension down();

	/**
	 * @param n number of dimension to move up (negative to move down)
	 * @return the destination when traversing n dimensions up (null if end of stack is reached)
	 */
	default IDimension move(int n) {
		IDimension d = this;
		if (n > 0) while(--n >= 0 && d != null) d = d.up();
		else while(++n <= 0 && d != null) d = d.down();
		return d;
	}

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
