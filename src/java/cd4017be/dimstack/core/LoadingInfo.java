package cd4017be.dimstack.core;

import cd4017be.dimstack.Main;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;

/**
 * Used to keep track from where and when a chunk was requested
 * @author cd4017be
 */
public class LoadingInfo {

	public final PortalConfiguration pc;
	public final long startTime;
	public final ChunkPos chunk;
	public long lastReqT, lastReqB;

	public LoadingInfo(PortalConfiguration pc, BlockPos from) {
		this.pc = pc;
		long t = System.currentTimeMillis();
		this.startTime = t;
		this.chunk = new ChunkPos(from);
		if (from.getY() == 0) {
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
		if (from.getY() == 0) lastReqT = t;
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