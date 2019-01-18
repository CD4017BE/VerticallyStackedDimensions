package cd4017be.dimstack;

import java.util.HashMap;

import cd4017be.lib.util.DimPos;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.ForgeChunkManager.Type;
import net.minecraftforge.fml.common.FMLCommonHandler;

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
	/** chunk loading ticket for this dimension */
	public Ticket loadingTicket;
	/** list of temporary loaded chunks for this dimension */
	public HashMap<ChunkPos, LoadingInfo> loadedChunks = new HashMap<>();
	
	public int LBedrockMin, LBedrockMax;
	public IBlockState LBedrockRepl;
	public int UBedrockMin, UBedrockMax;
	public IBlockState UBedrockRepl;

	private PortalConfiguration(int dimId) {
		this.dimId = dimId;
	}

	public WorldServer getWorld() {
		return FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(dimId);
	}

	/**
	 * @param pos current portal location
	 * @return the destination position and world the given portal leads to (or null if there is none or it can't be loaded)
	 */
	public static DimPos getAdjacentPos(DimPos pos) {
		PortalConfiguration pc = get(pos.dimId);
		int y = pos.getY();
		pc = y == 255 ? pc.neighbourUp : y == 0 ? pc.neighbourDown : null;
		if (pc == null) return null;
		WorldServer world = pos.getWorldServer().getMinecraftServer().getWorld(pc.dimId);
		if (world == null) return null;
		if (ChunkLoader.active()) {
			ChunkPos chunk = new ChunkPos(pos);
			LoadingInfo ti = pc.loadedChunks.get(chunk);
			if (ti != null) ti.onRequest(pos);
			else if (pc.loadingTicket != null || (pc.loadingTicket = ForgeChunkManager.requestTicket(Main.instance, pc.getWorld(), Type.NORMAL)) != null)
				pc.loadedChunks.put(chunk, new LoadingInfo(pc, pos));
		}
		return new DimPos(pos.getX(), 255 - pos.getY(), pos.getZ(), world);
	}

	/**
	 * @param pos current portal location
	 * @return location of the paired portal in the destination world
	 */
	public static BlockPos getAdjacentPos(BlockPos pos) {
		return new BlockPos(pos.getX(), 255 - pos.getY(), pos.getZ());
	}

	static final Int2ObjectOpenHashMap<PortalConfiguration> dimensions = new Int2ObjectOpenHashMap<>();

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

	/**
	 * unforce all chunks
	 */
	public static void cleanup() {
		for (PortalConfiguration pc : dimensions.values()) {
			pc.loadedChunks.clear();
			if (pc.loadingTicket != null) {
				ForgeChunkManager.releaseTicket(pc.loadingTicket);
				pc.loadingTicket = null;
			}
		}
	}

	/**
	 * reduce the amount of loaded chunks down to the given value by unforcing the least relevant ones.
	 * @param n maximum number of chunks to leave forced
	 */
	private void unforceLeastRelevantChunks(int n) {
		if (n < 0) n = 0;
		while(loadedChunks.size() > n) {
			long t = System.currentTimeMillis();
			float p = 1.0F;
			ChunkPos last = null;
			for (LoadingInfo li : loadedChunks.values()) {
				float f = li.stillNeeded(t);
				if (f < p) {
					p = f;
					last = li.chunk;
				}
			}
			loadedChunks.remove(last);
			ForgeChunkManager.unforceChunk(loadingTicket, last);
		}
	}

	/**
	 * Used to keep track from where and when a chunk was requested
	 * @author cd4017be
	 */
	public static class LoadingInfo {

		public final PortalConfiguration pc;
		public final long startTime;
		public final ChunkPos chunk;
		public long lastReqT, lastReqB;

		public LoadingInfo(PortalConfiguration pc, BlockPos from) {
			this.pc = pc;
			long t = System.currentTimeMillis();
			this.startTime = t;
			this.chunk = new ChunkPos(from);
			if (from.getY() < 128) {
				lastReqT = t;
				lastReqB = Long.MIN_VALUE;
			} else {
				lastReqT = Long.MIN_VALUE;
				lastReqB = t;
			}
			Ticket ticket = pc.loadingTicket;
			int n = ticket.getChunkListDepth();
			if (pc.loadedChunks.size() >= n)
				pc.unforceLeastRelevantChunks(n - 1);
			ForgeChunkManager.forceChunk(ticket, chunk);
			Main.LOG.info("forced chunk {} in {}", chunk, pc.dimId);
		}

		public void onRequest(BlockPos from) {
			long t = System.currentTimeMillis();
			if (from.getY() < 128) lastReqT = t;
			else lastReqB = t;
		}

		public LoadingInfo onUnload(boolean top) {
			if (top) lastReqT = Long.MIN_VALUE;
			else lastReqB = Long.MIN_VALUE;
			Main.LOG.info("{} access of chunk {} in {} became unloaded", top ? "TOP" : "BOTTOM", chunk, pc.dimId);
			return this;
		}

		public boolean checkExpired(long t) {
			if (lastReqB < t && lastReqT < t) {
				ForgeChunkManager.unforceChunk(pc.loadingTicket, chunk);
				Main.LOG.info("unforced chunk {} in {} after {}", chunk, pc.dimId, String.format("%.2fs", (float)(t - startTime + ChunkLoader.EXPIRE_TIME) / 1000F));
				return true;
			} else return false;
		}

		/**
		 * @param t current time
		 * @return estimated probability (0.0 ... 1.0) that this chunk will be accessed again before it expires.
		 */
		public float stillNeeded(long t) {
			long s = startTime, rt;
			float ref = (float)(t - s + ChunkLoader.EXPIRE_TIME), f = +0F;
			if ((rt = lastReqB) >= s)
				f += ref / (float)(t - rt) - 1F;
			if ((rt = lastReqT) >= s)
				f += ref / (float)(t - rt) - 1F;
			return Math.max(1F - 1F / f, 0);
		}

	}

}
