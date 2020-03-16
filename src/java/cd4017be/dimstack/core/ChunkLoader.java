package cd4017be.dimstack.core;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import cd4017be.api.recipes.RecipeScriptContext.ConfigConstants;
import cd4017be.dimstack.Main;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.OrderedLoadingCallback;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class ChunkLoader implements OrderedLoadingCallback {

	private static ChunkLoader instance;

	/** time in ms a chunk stays loaded after requested through a portal */
	public static long EXPIRE_TIME = 30000;
	/** time in ms of continuous chunk-loading after which more strict criteria must be fulfilled to stay loaded. */
	public static long OVER_TIME = 300000;
	/** interval in ms to check for expired chunks */
	public static int CHECK_INTERVAL = 60;

	private int timer;

	public static void initConfig(ConfigConstants cfg) {
		EXPIRE_TIME = (long)(cfg.getNumber("chunk_load_time", (double)EXPIRE_TIME / 1000D) * 1000D);
		OVER_TIME = (long)(cfg.getNumber("cont_load_time", (double)OVER_TIME / 1000D) * 1000D);
		CHECK_INTERVAL = (int) (EXPIRE_TIME / 200L);
		if (EXPIRE_TIME > 0 && instance == null)
			instance = new ChunkLoader();
	}

	public static boolean active() {
		return instance != null;
	}

	private ChunkLoader() {
		ForgeChunkManager.setForcedChunkLoadingCallback(Main.instance, this);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public List<Ticket> ticketsLoaded(List<Ticket> tickets, World world, int maxTicketCount) {
		//chunks should not stay loaded after restart
		return Collections.emptyList();
	}

	@Override
	public void ticketsLoaded(List<Ticket> tickets, World world) {
	}

	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event) {
		World world = event.getWorld();
		if (world instanceof WorldServer) {
			PortalConfiguration pc = PortalConfiguration.get(world);
			if (pc.up() != null || pc.down() != null)
				world.addEventListener(pc);
		}
	}

	@SubscribeEvent
	public void onChunkUnload(ChunkEvent.Unload event) {
		//immediately unforce chunks that are kept loaded through a portal
		PortalConfiguration pc = PortalConfiguration.dimensions.get(event.getWorld().provider.getDimension()), pc1;
		if (pc == null || pc.loadingTicket == null) return;
		LoadingInfo ti;
		ChunkPos pos = event.getChunk().getPos();
		if (
			(pc1 = pc.up()) != null && (ti = pc1.loadedChunks.get(pos)) != null
			&& ti.onUnload(false).checkExpired(System.currentTimeMillis() - EXPIRE_TIME)
		) ti.remove();
		if (
			(pc1 = pc.down()) != null && (ti = pc1.loadedChunks.get(pos)) != null
			&& ti.onUnload(true).checkExpired(System.currentTimeMillis() - EXPIRE_TIME)
		) ti.remove();
	}

	@SubscribeEvent
	public void onTick(TickEvent.ServerTickEvent event) {
		if (event.phase != TickEvent.Phase.START || --timer > 0) return;
		timer = CHECK_INTERVAL;
		long t = System.currentTimeMillis() - EXPIRE_TIME;
		for (PortalConfiguration pc : PortalConfiguration.dimensions.values()) {
			if (pc.loadingTicket == null) continue;
			PortalConfiguration up = pc.up(), down = pc.down();
			for (Iterator<LoadingInfo> it = pc.loadedChunks.values().iterator(); it.hasNext();) {
				LoadingInfo li = it.next();
				if (t > li.startTime) {
					if (li.lastReqT > t && up != null && !isChunkExternallyForced(up.dimId, li.chunk))
						li.onUnload(true);
					if (li.lastReqB > t && down != null && !isChunkExternallyForced(down.dimId, li.chunk))
						li.onUnload(false);
				}
				if (li.checkExpired(t)) {
					it.remove(); //remove via iterator first to avoid concurrent modification
					li.remove();
				}
			}
		}
	}

	/**
	 * @param dim dimension id
	 * @param chunk chunk position
	 * @return whether the given chunk is currently kept loaded by players, spawn area or other mods' chunk loaders (in other words: not by this mod or access hot loading).
	 */
	public static boolean isChunkExternallyForced(int dim, ChunkPos chunk) {
		WorldServer world = DimensionManager.getWorld(dim);
		if (world == null || !world.isBlockLoaded(chunk.getBlock(8, 64, 8))) return false;
		for (Ticket t : ForgeChunkManager.getPersistentChunksFor(world).get(chunk))
			if (!Main.ID.equals(t.getModId())) return true;
		return !world.provider.canDropChunk(chunk.x, chunk.z) ||
				world.getPlayerChunkMap().contains(chunk.x, chunk.z);
	}

}
