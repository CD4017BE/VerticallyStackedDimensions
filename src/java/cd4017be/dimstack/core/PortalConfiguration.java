package cd4017be.dimstack.core;

import java.util.HashMap;

import cd4017be.dimstack.Main;
import cd4017be.dimstack.api.IDimension;
import cd4017be.dimstack.api.IDimensionSettings;
import cd4017be.dimstack.block.Portal;
import cd4017be.dimstack.worldgen.PortalGen;
import cd4017be.lib.util.DimPos;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorldEventListener;
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
public class PortalConfiguration implements IDimension, IWorldEventListener {

	/** the dimension id this refers to */
	public final int dimId;
	/** the destination when traversing up (null for regular world border) */
	private PortalConfiguration neighbourUp;
	/** the destination when traversing down (null for regular world border) */
	private PortalConfiguration neighbourDown;
	/** additional dimension settings mapped by type */
	private HashMap<Class<?extends IDimensionSettings>, IDimensionSettings> settings = new HashMap<>();
	/** this dimension has chunks with missing portal top layer */
	boolean topOpen = false;
	/** the dimension height level */
	private int height = Integer.MIN_VALUE;

	/** chunk loading ticket for this dimension */
	Ticket loadingTicket;
	/** list of temporary loaded chunks for this dimension */
	HashMap<ChunkPos, LoadingInfo> loadedChunks = new HashMap<>();

	private PortalConfiguration(int dimId) {
		this.dimId = dimId;
	}

	@Override
	public int id() {return dimId;}

	@Override
	public PortalConfiguration up() {return neighbourUp;}

	@Override
	public PortalConfiguration down() {return neighbourDown;}

	@Override
	public PortalConfiguration bottom() {
		PortalConfiguration pc, pc1;
		for (pc = this; (pc1 = pc.neighbourDown) != null; pc = pc1)
			if (pc1 == this)
				return null;
		return pc;
	}

	@Override
	public PortalConfiguration top() {
		PortalConfiguration pc, pc1;
		for (pc = this; (pc1 = pc.neighbourUp) != null; pc = pc1)
			if (pc1 == this)
				return null;
		return pc;
	}

	@Override
	public int height() {
		if (height == Integer.MIN_VALUE)
			computeHeights();
		return height;
	}

	private void computeHeights() {
		PortalConfiguration pc = bottom(), end;
		if (pc == null) {
			pc = neighbourUp;
			end = this;
		} else end = null;
		int base = dimId, abs = Math.abs(dimId);
		for (; pc != end; pc = pc.neighbourUp) {
			int d = pc.dimId, a = Math.abs(d);
			if (a < abs || a == abs && d < base) {
				abs = a;
				base = d;
			}
		}
		pc = get(base);
		for (int h = 0; pc != null; pc = pc.neighbourUp, h++)
			if (h > 0 && pc.dimId == base) return;
			else pc.height = h;
		pc = get(base).neighbourDown;
		for (int h = -1; pc != null; pc = pc.neighbourDown, h--)
			pc.height = h;
	}

	private void unlink() {
		PortalConfiguration u = neighbourUp, d = neighbourDown;
		if (u != null) u.neighbourDown = d;
		if (d != null) d.neighbourUp = u;
	}

	@Override
	public void insertTop(IDimension dim) {
		if (dim == neighbourUp) return;
		PortalConfiguration pc = (PortalConfiguration)dim, u = neighbourUp;
		pc.unlink();
		neighbourUp = pc;
		if (u != null) {
			pc.neighbourUp = u;
			u.neighbourDown = pc;
		}
	}

	@Override
	public void insertBottom(IDimension dim) {
		if (dim == neighbourDown) return;
		PortalConfiguration pc = (PortalConfiguration)dim, d = neighbourDown;
		pc.unlink();
		neighbourDown = pc;
		if (d != null) {
			pc.neighbourDown = d;
			d.neighbourUp = pc;
		}
	}

	@Override
	public WorldServer getWorld() {
		return FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(dimId);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends IDimensionSettings> T getSettings(Class<T> type, boolean create) {
		return (T)(create ?
			settings.computeIfAbsent(type, IDimensionSettings::newInstance)
			: settings.get(type));
	}

	void loadSettings(NBTTagList nbt) {
		for (NBTBase tag : nbt) {
			NBTTagCompound ctag = (NBTTagCompound)tag;
			String cname = ctag.getString("class");
			try {
				Class<?> c = Class.forName(cname);
				if (IDimensionSettings.class.isAssignableFrom(c)) {
					@SuppressWarnings("unchecked")
					IDimensionSettings setting = getSettings((Class<? extends IDimensionSettings>)c, true);
					if (setting != null) setting.deserializeNBT(ctag);
				} else Main.LOG.warn("Can't load entry for dimension {}: Invalid class {} doesn't implement IDimensionSettings", this, cname);
			} catch (ClassNotFoundException e) {
				Main.LOG.warn("Can't load entry for dimension {}: Class {} not found!", this, cname);
			}
		}
	}

	NBTTagList saveSettings() {
		NBTTagList cfg = new NBTTagList();
		for (IDimensionSettings s : settings.values()) {
			NBTTagCompound tag = s.serializeNBT();
			if (tag != null && !tag.hasNoTags()) {
				tag.setString("class", s.getClass().getName());
				cfg.appendTag(tag);
			}
		}
		return cfg;
	}

	/**
	 * reduce the amount of loaded chunks down to the given value by unforcing the least relevant ones.
	 * @param n maximum number of chunks to leave forced
	 */
	void unforceLeastRelevantChunks(int n) {
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

	@Override
	public String toString() {
		return Integer.toString(dimId);
	}

	/**
	 * states that this dimension has chunks with missing portal top layer
	 */
	public void setTopOpen() {
		if (topOpen || neighbourUp == null) return;
		topOpen = true;
		cfgModified = true;
	}

	@Override
	public void notifyBlockUpdate(World world, BlockPos pos, IBlockState oldState, IBlockState newState, int flags) {
		if ((flags & 1) == 0) return;
		int y = pos.getY();
		if (y >= 252 && topOpen && oldState.getMaterial() == Material.AIR && newState.getMaterial() != Material.AIR)
			PortalGen.fixCeil(world, pos);
		if (y == 2) {
			BlockPos posP = pos.down(2);
			IBlockState stateP = world.getBlockState(posP);
			if (stateP.getBlock() instanceof Portal)
				stateP.neighborChanged(world, posP, newState.getBlock(), pos);
		} else if (y == 253) {
			BlockPos posP = pos.up(2);
			IBlockState stateP = world.getBlockState(posP);
			if (stateP.getBlock() instanceof Portal)
				stateP.neighborChanged(world, posP, newState.getBlock(), pos);
		}
	}

	@Override
	public void notifyLightSet(BlockPos pos) {}
	@Override
	public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {}
	@Override
	public void playSoundToAllNearExcept(EntityPlayer player, SoundEvent soundIn, SoundCategory category, double x, double y, double z, float volume, float pitch) {}
	@Override
	public void playRecord(SoundEvent soundIn, BlockPos pos) {}
	@Override
	public void spawnParticle(int particleID, boolean ignoreRange, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, int... parameters) {}
	@Override
	public void spawnParticle(int id, boolean ignoreRange, boolean p_190570_3_, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, int... parameters) {}
	@Override
	public void onEntityAdded(Entity entityIn) {}
	@Override
	public void onEntityRemoved(Entity entityIn) {}
	@Override
	public void broadcastSound(int soundID, BlockPos pos, int data) {}
	@Override
	public void playEvent(EntityPlayer player, int type, BlockPos blockPosIn, int data) {}
	@Override
	public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {}

	//static features:
	static final Int2ObjectOpenHashMap<PortalConfiguration> dimensions = new Int2ObjectOpenHashMap<>();
	static boolean cfgModified;

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
			if (A.neighbourUp != null)
				A.neighbourUp.neighbourDown = null;
			if (B.neighbourDown != null)
				B.neighbourDown.neighbourUp = null;
			A.neighbourUp = B;
			B.neighbourDown = A;
		}
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

}
