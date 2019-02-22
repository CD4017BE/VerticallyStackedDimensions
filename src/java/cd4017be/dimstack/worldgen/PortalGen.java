package cd4017be.dimstack.worldgen;

import java.util.Random;

import cd4017be.dimstack.Objects;
import cd4017be.dimstack.block.Portal;
import cd4017be.dimstack.core.PortalConfiguration;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * 
 * @author cd4017be
 */
public class PortalGen implements IWorldGenerator {

	public PortalGen() {
		//this generator should run rather late to ensure the portal states won't need to change much later on.
		GameRegistry.registerWorldGenerator(this, 10);
	}

	@Override
	public void generate(Random random, int cx, int cz, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
		PortalConfiguration pc = PortalConfiguration.get(world), pc1;
		Chunk chunk = world.getChunkFromChunkCoords(cx, cz);
		if ((pc1 = pc.down()) != null)
			placePortals(chunk, 0, false, pc1);
		if ((pc1 = pc.up()) != null) {
			int yc = pc.ceilY;
			if (chunk.getTopFilledSegment() >= yc - 15)
				placePortals(chunk, yc, false, pc1);
			else pc.setTopOpen();
		}
	}

	public static void fixCeil(World world, BlockPos pos, int y, PortalConfiguration other) {
		int x0 = pos.getX() - 8, x1 = x0 + 16,
			z0 = pos.getZ() - 8, z1 = z0 + 16;
		IChunkProvider cp = world.getChunkProvider();
		Chunk chunk;
		chunk = cp.getLoadedChunk(x0 >> 4, z0 >> 4);
		if (chunk != null && chunk.getBlockState(x0, y, z0).getMaterial() != Objects.M_PORTAL)
			placePortals(chunk, y, true, other);
		chunk = cp.getLoadedChunk(x1 >> 4, z0 >> 4);
		if (chunk != null && chunk.getBlockState(x1, y, z0).getMaterial() != Objects.M_PORTAL)
			placePortals(chunk, y, true, other);
		chunk = cp.getLoadedChunk(x0 >> 4, z1 >> 4);
		if (chunk != null && chunk.getBlockState(x0, y, z1).getMaterial() != Objects.M_PORTAL)
			placePortals(chunk, y, true, other);
		chunk = cp.getLoadedChunk(x1 >> 4, z1 >> 4);
		if (chunk != null && chunk.getBlockState(x1, y, z1).getMaterial() != Objects.M_PORTAL)
			placePortals(chunk, y, true, other);
	}

	public static void placePortals(Chunk chunk, int y, boolean update, PortalConfiguration neighb) {
		IBlockState state = Objects.PORTAL.getDefaultState(), olds;
		boolean s0 = state.getValue(Portal.solidOther1),
				s_ = state.getValue(Portal.solidOther2),
				s1 = state.getValue(Portal.solidThis1),
				s2 = state.getValue(Portal.solidThis2);
		int y1 = y == 0 ? 1 : y-1, y2 = y1 + y1 - y;
		int x0 = chunk.x << 4, x1 = x0 + 16;
		int z0 = chunk.z << 4, z1 = z0 + 16;
		MutableBlockPos pos = new MutableBlockPos();
		World world = chunk.getWorld();
		do {
			World worldO = DimensionManager.getWorld(neighb.dimId);
			if (worldO == null) break;
			Chunk chunk_ = worldO.getChunkProvider().getLoadedChunk(chunk.x, chunk.z);
			if (chunk_ == null) break;
			int Y = y == 0 ? neighb.ceilY : 0;
			if (chunk_.getTopFilledSegment() < Y - 15) break;
			IBlockState state_ = state
					.withProperty(Portal.solidOther1, s1)
					.withProperty(Portal.solidOther2, s2)
					.withProperty(Portal.solidThis1, s0)
					.withProperty(Portal.solidThis2, s_);
			int y0 = Y == 0 ? 1 : Y - 1, y_ = y0 + y0 - Y;
			for (int z = z0; z < z1; z++)
				for (int x = x0; x < x1; x++) {
					if ((olds = chunk.getBlockState(x, y, z)).getMaterial() == Objects.M_PORTAL) continue;
					if (s2 ^ Portal.isSolid(chunk.getBlockState(x, y2, z))) {
						state = state.withProperty(Portal.solidThis2, s2 = !s2);
						state_ = state_.withProperty(Portal.solidOther2, s2);
					}
					if (s1 ^ Portal.isSolid(chunk.getBlockState(x, y1, z))) {
						state = state.withProperty(Portal.solidThis1, s1 = !s1);
						state_ = state_.withProperty(Portal.solidOther1, s1);
					}
					if (s_ ^ Portal.isSolid(chunk_.getBlockState(x, y_, z))) {
						state = state.withProperty(Portal.solidOther2, s_ = !s_);
						state_ = state_.withProperty(Portal.solidThis2, s_);
					}
					if (s0 ^ Portal.isSolid(chunk_.getBlockState(x, y0, z))) {
						state = state.withProperty(Portal.solidOther1, s0 = !s0);
						state_ = state_.withProperty(Portal.solidThis1, s0);
					}
					chunk.setBlockState(pos.setPos(x, y, z), state);
					if (update) world.notifyBlockUpdate(pos, olds, state, 2);
					if ((olds = chunk_.getBlockState(x, Y, z)) != state_) {
						chunk.setBlockState(pos.setPos(x, Y, z), state_);
						world.notifyBlockUpdate(pos, olds, state_, 2);
					}
				}
			return;
		} while(false);
		for (int z = z0; z < z1; z++)
			for (int x = x0; x < x1; x++) {
				if ((olds = chunk.getBlockState(x, y, z)).getMaterial() == Objects.M_PORTAL) continue;
				if (s2 ^ Portal.isSolid(chunk.getBlockState(x, y2, z)))
					state = state.withProperty(Portal.solidThis2, s2 = !s2);
				if (s1 ^ Portal.isSolid(chunk.getBlockState(x, y1, z)))
					state = state.withProperty(Portal.solidThis1, s1 = !s1);
				if (s0 ^ Portal.isSolid(chunk.getBlockState(x, y, z)))
					state = state.withProperty(Portal.solidOther1, s0 = !s0).withProperty(Portal.solidOther2, s0);
				chunk.setBlockState(pos.setPos(x, y, z), state);
				if (update) world.notifyBlockUpdate(pos, olds, state, 2);
			}
	}

}
