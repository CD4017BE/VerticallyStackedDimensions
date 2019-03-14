package cd4017be.dimstack.util;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;

/**
 * 
 * @author cd4017be
 */
public class PostGenChunkBuffer extends ChunkPrimer {

	private static final PostGenChunkBuffer instance = new PostGenChunkBuffer();

	public static ChunkPrimer wrap(Chunk chunk) {
		instance.chunk = chunk;
		return instance;
	}

	private Chunk chunk;
	private MutableBlockPos pos = new MutableBlockPos();

	@Override
	public IBlockState getBlockState(int x, int y, int z) {
		return chunk.getBlockState(x, y, z);
	}

	@Override
	public void setBlockState(int x, int y, int z, IBlockState state) {
		Chunk chunk = this.chunk;
		chunk.setBlockState(pos.setPos(x + (chunk.x << 4), y, z + (chunk.z << 4)), state);
	}

}
