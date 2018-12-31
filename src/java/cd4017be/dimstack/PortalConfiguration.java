package cd4017be.dimstack;

import java.util.HashMap;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

/**
 * 
 * @author cd4017be
 */
public class PortalConfiguration {

	/** the dimension id this refers to */
	public final int dimId;
	/** the destination when traversing up (null for regular world border) */
	public PortalConfiguration neighbourUp;
	/** the destination when traversing down (null for regular world border) */
	public PortalConfiguration neighbourDown;

	private PortalConfiguration(int dimId) {
		this.dimId = dimId;
	}

	/**
	 * @param world current (server) world
	 * @param pos current portal location
	 * @return the destination world the given portal leads to (or null if there is none or it can't be loaded)
	 */
	public static WorldServer getAdjacentWorld(WorldServer world, BlockPos pos) {
		PortalConfiguration pc = get(world.provider.getDimension());
		int y = pos.getY();
		pc = y == 255 ? pc.neighbourUp : y == 0 ? pc.neighbourDown : null;
		return pc == null ? null : world.getMinecraftServer().getWorld(pc.dimId);
	}

	/**
	 * @param pos current portal location
	 * @return location of the paired portal in the destination world
	 */
	public static BlockPos getAdjacentPos(BlockPos pos) {
		return new BlockPos(pos.getX(), 255 - pos.getY(), pos.getZ());
	}

	private static final HashMap<Integer, PortalConfiguration> dimensions = new HashMap<Integer, PortalConfiguration>();

	/**
	 * @param world current world
	 * @return the portal configuration for the given world's dimension
	 */
	public static PortalConfiguration get(World world) {
		return get(world.provider.getDimension());
	}

	/**
	 * @param dimId current dimension id
	 * @return the portal configuration for the given dimension
	 */
	public static PortalConfiguration get(int dimId) {
		PortalConfiguration pc = dimensions.get(dimId);
		if (pc == null) dimensions.put(dimId, pc = new PortalConfiguration(dimId));
		return pc;
	}

	/**
	 * registeres the given dimensions to be vertically connected
	 * @param dims ids of the dimensions to link in order bottom to top
	 */
	public static void link(int... dims) {
		if (dims.length < 2) return;
		PortalConfiguration A = get(dims[0]), B;
		for (int i = 1; i < dims.length; i++, A=B) {
			B = get(dims[i]);
			A.neighbourUp = B;
			B.neighbourDown = A;
		}
	}

}
