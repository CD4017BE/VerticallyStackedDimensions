package cd4017be.dimstack.worldgen;

import java.util.Random;

import cd4017be.dimstack.Objects;
import cd4017be.dimstack.block.Portal;
import cd4017be.dimstack.core.PortalConfiguration;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
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
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
		PortalConfiguration pc = PortalConfiguration.get(world);
		if (pc.down() != null)
			placePortals(world, chunkX, chunkZ, 0);
		if (pc.up() != null)
			placePortals(world, chunkX, chunkZ, 255);
	}

	private void placePortals(World world, int cx, int cz, int y) {
		Chunk chunk = world.getChunkFromChunkCoords(cx, cz);
		if (y >= (chunk.getTopFilledSegment() + 1) << 4)
			return;//TODO mark dimension
		IBlockState state = Objects.PORTAL.getDefaultState();
		boolean s0 = state.getValue(Portal.solidOther1),
				s1 = state.getValue(Portal.solidThis1),
				s2 = state.getValue(Portal.solidThis2);
		MutableBlockPos pos = new MutableBlockPos();
		int y1 = y < 128 ? y+1 : y-1, y2 = y1 + y1 - y;
		int x0 = cx << 4, x1 = x0 + 16;
		for (int z = cz << 4, z1 = z + 16; z < z1; z++)
			for (int x = x0; x < x1; x++)
				if (chunk.getBlockState(x, y, z).getMaterial() != Objects.M_PORTAL) {
					if (s2 ^ Portal.isSolid(world, pos.setPos(x, y2, z)))
						state = state.withProperty(Portal.solidThis2, s2 = !s2);
					if (s1 ^ Portal.isSolid(world, pos.setPos(x, y1, z)))
						state = state.withProperty(Portal.solidThis1, s1 = !s1);
					if (s0 ^ Portal.isSolid(world, pos.setPos(x, y, z)))
						state = state.withProperty(Portal.solidOther1, s0 = !s0).withProperty(Portal.solidOther2, s0);
					chunk.setBlockState(pos, state);
				}
	}

}
