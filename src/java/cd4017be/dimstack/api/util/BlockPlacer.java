package cd4017be.dimstack.api.util;

/**
 * Places blocks based on a noise field to generate terrain.
 * @author CD4017BE
 */
@FunctionalInterface
public interface BlockPlacer {

	/**
	 * place a block at the given location
	 * @param x X-coord
	 * @param y Y-coord
	 * @param z Z-coord
	 * @param f noise field value
	 */
	void place(int x, int y, int z, double f);

}