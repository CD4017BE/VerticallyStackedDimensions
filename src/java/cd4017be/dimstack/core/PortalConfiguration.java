package cd4017be.dimstack.core;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import cd4017be.dimstack.Main;
import cd4017be.dimstack.block.Portal;
import cd4017be.dimstack.cfg.IDimensionSettings;
import cd4017be.dimstack.worldgen.PortalGen;
import cd4017be.lib.util.DimPos;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
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
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.FMLCommonHandler;

/**
 * 
 * @author cd4017be
 */
public class PortalConfiguration implements IWorldEventListener {

	/** the dimension id this refers to */
	public final int dimId;
	/** the destination when traversing up (null for regular world border) */
	private PortalConfiguration neighbourUp;
	/** the destination when traversing down (null for regular world border) */
	private PortalConfiguration neighbourDown;
	/** additional dimension settings mapped by type */
	private HashMap<Class<?extends IDimensionSettings>, IDimensionSettings> settings = new HashMap<>();
	/** this dimension has chunks with missing portal top layer */
	private boolean topOpen = false;

	/** chunk loading ticket for this dimension */
	Ticket loadingTicket;
	/** list of temporary loaded chunks for this dimension */
	HashMap<ChunkPos, LoadingInfo> loadedChunks = new HashMap<>();

	private PortalConfiguration(int dimId) {
		this.dimId = dimId;
	}

	/**@return the destination when traversing up (null for regular world border) */
	public PortalConfiguration up() {return neighbourUp;}

	/**@return the destination when traversing down (null for regular world border) */
	public PortalConfiguration down() {return neighbourDown;}

	/**@return the lowest dimension in current stack or null if looped */
	public PortalConfiguration findBottomEnd() {
		PortalConfiguration pc, pc1;
		for (pc = this; (pc1 = pc.neighbourDown) != null; pc = pc1)
			if (pc1 == this)
				return null;
		return pc;
	}

	/**@return the highest dimension in current stack or null if looped */
	public PortalConfiguration findTopEnd() {
		PortalConfiguration pc, pc1;
		for (pc = this; (pc1 = pc.neighbourUp) != null; pc = pc1)
			if (pc1 == this)
				return null;
		return pc;
	}

	/**@return the server world for this dimension */
	public WorldServer getWorld() {
		return FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(dimId);
	}

	/**
	 * @param type setting type
	 * @param create whether to create a new settings instance if absent
	 * @return the settings of given type.
	 */
	@SuppressWarnings("unchecked")
	public <T extends IDimensionSettings> T getSettings(Class<T> type, boolean create) {
		return (T)(create ?
			settings.computeIfAbsent(type, IDimensionSettings::newInstance)
			: settings.get(type));
	}

	private void loadSettings(NBTTagList nbt) {
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

	private NBTTagList saveSettings() {
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
	static File cfgFile;
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
	 * unforce all chunks
	 */
	public static void cleanup() {
		IntArrayList open = new IntArrayList();
		for (PortalConfiguration pc : dimensions.values()) {
			pc.loadedChunks.clear();
			if (pc.loadingTicket != null) {
				ForgeChunkManager.releaseTicket(pc.loadingTicket);
				pc.loadingTicket = null;
			}
			if (pc.topOpen) open.add(pc.dimId);
		}
		if (cfgModified && cfgFile != null)
			try {
				NBTTagCompound data = CompressedStreamTools.read(cfgFile);
				data.setIntArray("topOpen", open.toIntArray());
				CompressedStreamTools.write(data, cfgFile);
				Main.LOG.info("updated dimension stack configuration file");
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

	/**
	 * reload all dimension stack info and settings
	 * @param nbt data to load from
	 */
	public static void load(NBTTagCompound nbt) {
		dimensions.clear();
		NBTTagList stacks = nbt.getTagList("stacks", NBT.TAG_INT_ARRAY);
		for (NBTBase tag : stacks)
			link(((NBTTagIntArray)tag).getIntArray());
		for (int dim : nbt.getIntArray("topOpen")) {
			PortalConfiguration pc = get(dim);
			if (pc.neighbourUp != null) pc.topOpen = true;
		}
		for (String key : nbt.getKeySet())
			try {
				int dim = Integer.parseInt(key);
				NBTTagList cfg = nbt.getTagList(key, NBT.TAG_COMPOUND);
				if (!cfg.hasNoTags())
					get(dim).loadSettings(cfg);
			} catch(NumberFormatException e) {}
		cfgModified = false;
	}

	/**
	 * save all dimension stack info and settings
	 * @param nbt data to save in
	 */
	public static void save(NBTTagCompound nbt) {
		IntOpenHashSet bottoms = new IntOpenHashSet(), loops = new IntOpenHashSet();
		for (PortalConfiguration pc : dimensions.values()) {
			PortalConfiguration bot = pc.findBottomEnd();
			if (bot == null) loops.add(pc.dimId);
			else if (bot != pc) bottoms.add(bot.dimId);
			NBTTagList cfg = pc.saveSettings();
			if (!cfg.hasNoTags())
				nbt.setTag(pc.toString(), cfg);
		}
		NBTTagList stacks = new NBTTagList();
		IntArrayList stack = new IntArrayList();
		for (int d : bottoms) {
			stack.add(d);
			for (PortalConfiguration pc = get(d), pc1; (pc1 = pc.neighbourUp) != null; pc = pc1)
				stack.add(pc1.dimId);
			stacks.appendTag(new NBTTagIntArray(stack.toIntArray()));
			stack.clear();
		}
		while(!loops.isEmpty()) {
			int d = loops.iterator().nextInt();
			loops.rem(d);
			stack.add(d);
			for (PortalConfiguration pc = get(d), pc1 = pc.neighbourUp; pc1 != pc; pc1 = pc1.neighbourUp) {
				int d1 = pc1.dimId;
				loops.rem(d1);
				stack.add(d1);
			}
			stack.add(d);
			stacks.appendTag(new NBTTagIntArray(stack.toIntArray()));
			stack.clear();
		}
		nbt.setTag("stacks", stacks);
	}

	private static NBTTagCompound defaultCfg;

	/**
	 * load or initialize dimension settings for a specific world
	 * @param dir world save directory
	 */
	public static void loadWorldSettings(File dir) {
		boolean reload = true;
		if (defaultCfg == null) {
			defaultCfg = new NBTTagCompound();
			save(defaultCfg);
			reload = false;
		}
		File file = new File(dir, "dimensionstack.dat");
		try {
			if (file.exists()) {
				load(CompressedStreamTools.read(file));
				Main.LOG.info("Dimension stack configuration file for world {} sucessfully loaded.", dir.getName());
				reload = false;
			} else {
				CompressedStreamTools.write(defaultCfg, file);
				Main.LOG.info("new dimension stack configuration file for world {} sucessfully created.", dir.getName());
			}
			cfgFile = file;
		} catch (IOException e) {
			cfgFile = null;
			e.printStackTrace();
		}
		if (reload) load(defaultCfg);
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
