package cd4017be.dimstack.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import cd4017be.api.recipes.RecipeAPI;
import cd4017be.api.recipes.RecipeScriptContext;
import cd4017be.api.recipes.RecipeAPI.IRecipeHandler;
import cd4017be.api.recipes.RecipeScriptContext.ConfigConstants;
import cd4017be.dimstack.ClientProxy;
import cd4017be.dimstack.Main;
import cd4017be.dimstack.Objects;
import cd4017be.dimstack.api.API;
import cd4017be.dimstack.api.CustomWorldProps;
import cd4017be.dimstack.api.IDimension;
import cd4017be.dimstack.api.IDimensionSettings;
import cd4017be.dimstack.api.util.ICfgButtonHandler;
import cd4017be.dimstack.api.util.SettingProvider;
import cd4017be.dimstack.worldgen.OreGenHandler;
import cd4017be.lib.Lib;
import cd4017be.lib.script.Parameters;
import cd4017be.lib.script.obj.Error;
import cd4017be.lib.script.obj.IOperand;
import cd4017be.lib.script.obj.Nil;
import cd4017be.lib.script.obj.Number;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DimensionType;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientCustomPacketEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.CustomNetworkEvent;
import net.minecraftforge.fml.common.network.NetworkHandshakeEstablished;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import static cd4017be.dimstack.core.PortalConfiguration.*;

/**
 * @author CD4017BE
 *
 */
public class Dimensionstack extends API implements IRecipeHandler {

	private static String DIMENSION_STACK = "dimstack", DIMENSION = "dimension", CHANNEL = "dimstack";
	public static final int FILE_VERSION = 4;
	private NBTTagCompound defaultCfg;
	private File cfgFile;
	private static boolean cfgModified;
	private final FMLEventChannel networkChannel;

	public Dimensionstack() {
		API.INSTANCE = this;
		RecipeAPI.Handlers.put(DIMENSION_STACK, this);
		RecipeAPI.Handlers.put(DIMENSION, this);
		networkChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel(CHANNEL);
		networkChannel.register(this);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public IDimension getDim(int id) {
		return get(id);
	}

	public static void markDirty() {
		cfgModified = true;
	}

	/**
	 * unforce all chunks
	 */
	public void cleanup() {
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
				save(data, false);
				data.setIntArray("topOpen", open.toIntArray());
				CompressedStreamTools.write(data, cfgFile);
				Main.LOG.info("updated dimension stack configuration file");
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

	public static boolean canCreateDim(int id) {
		return !DimensionManager.isDimensionRegistered(id) || DimensionManager.getProviderType(id) == Objects.CUSTOM_DIM_TYPE;
	}

	public static boolean isDimCreated(int id) {
		return DimensionManager.isDimensionRegistered(id) && DimensionManager.getProviderType(id) == Objects.CUSTOM_DIM_TYPE;
	}

	public static void setDimCreation(int id, boolean create) {
		if (!DimensionManager.isDimensionRegistered(id)) {
			if (create)
				DimensionManager.registerDimension(id, Objects.CUSTOM_DIM_TYPE);
			return;
		}
		DimensionType registered = DimensionManager.getProviderType(id);
		if (registered == Objects.CUSTOM_DIM_TYPE) {
			if (!create)
				DimensionManager.unregisterDimension(id);
		} else if (create)
			Main.LOG.fatal("Can't register custom WorldProvider for dimension {} because this id is already occupied by {}", id, registered.getName());
	}

	private static void loadSettings(SettingProvider sp, NBTTagCompound cfg) {
		if (sp instanceof PortalConfiguration) {
			PortalConfiguration pc = (PortalConfiguration)sp;
			int y = cfg.getByte("ceil") & 0xff;
			pc.ceilY = y > 0 ? y : 255;
			pc.flipped = cfg.getBoolean("flip");
			setDimCreation(pc.dimId, cfg.getBoolean("create"));
		}
		for (String key : cfg.getKeySet())
			if (key.indexOf('.') >= 0) try {
				Class<?> c = Class.forName(key);
				if (IDimensionSettings.class.isAssignableFrom(c)) {
					@SuppressWarnings("unchecked")
					IDimensionSettings setting = sp.getSettings((Class<? extends IDimensionSettings>)c, true);
					if (setting != null) setting.deserializeNBT(cfg.getTag(key));
				} else Main.LOG.warn("Can't load entry for dimension {}: Invalid class {} doesn't implement IDimensionSettings", sp, key);
			} catch (ClassNotFoundException e) {
				Main.LOG.warn("Can't load entry for dimension {}: Class {} not found!", sp, key);
			}
		
	}

	private static NBTTagCompound saveSettings(SettingProvider sp, boolean client) {
		NBTTagCompound cfg = new NBTTagCompound();
		if (sp instanceof PortalConfiguration) {
			PortalConfiguration pc = (PortalConfiguration)sp;
			cfg.setByte("ceil", (byte)pc.ceilY);
			cfg.setBoolean("flip", pc.flipped);
			cfg.setBoolean("create", isDimCreated(pc.dimId));
		}
		for (IDimensionSettings s : sp.getAllSettings()) {
			if (client && !s.isClientRelevant()) continue;
			NBTBase tag = s.serializeNBT();
			if (tag != null && !tag.hasNoTags())
				cfg.setTag(s.getClass().getName(), tag);
		}
		return cfg;
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
			if (pc.up() != null) pc.topOpen = true;
		}
		INSTANCE.getAllSettings().clear();
		for (String key : nbt.getKeySet())
			try {
				SettingProvider sp = key.equals("global") ? INSTANCE : get(Integer.parseInt(key));
				NBTTagCompound cfg = nbt.getCompoundTag(key);
				if (!cfg.hasNoTags())
					loadSettings(sp, cfg);
			} catch(NumberFormatException e) {}
		cfgModified = false;
	}

	/**
	 * save all dimension stack info and settings
	 * @param nbt data to save in
	 */
	public static void save(NBTTagCompound nbt, boolean client) {
		NBTTagCompound cfg = saveSettings(INSTANCE, client);
		if (!cfg.hasNoTags())
			nbt.setTag("global", cfg);
		IntArrayList open = new IntArrayList();
		for (PortalConfiguration pc : dimensions.values()) {
			if (!(cfg = saveSettings(pc, client)).hasNoTags())
				nbt.setTag(Integer.toString(pc.dimId), cfg);
			if (pc.topOpen) open.add(pc.dimId);
		}
		nbt.setIntArray("topOpen", open.toIntArray());
		NBTTagList stacks = new NBTTagList();
		for (int[] stack : getStacks())
			stacks.appendTag(new NBTTagIntArray(stack));
		nbt.setTag("stacks", stacks);
		nbt.setByte("version", (byte)FILE_VERSION);
	}

	/**
	 * @return all currently registered dimension stacks as dimension id arrays
	 */
	public static ArrayList<int[]> getStacks() {
		IntOpenHashSet bottoms = new IntOpenHashSet(), loops = new IntOpenHashSet();
		for (PortalConfiguration pc : dimensions.values()) {
			PortalConfiguration bot = pc.bottom();
			if (bot == null) loops.add(pc.dimId);
			else if (bot != pc) bottoms.add(bot.dimId);
		}
		ArrayList<int[]> stacks = new ArrayList<>();
		IntArrayList stack = new IntArrayList();
		for (int d : bottoms) {
			stack.add(d);
			for (PortalConfiguration pc = get(d), pc1; (pc1 = pc.up()) != null; pc = pc1)
				stack.add(pc1.dimId);
			stacks.add(stack.toIntArray());
			stack.clear();
		}
		while(!loops.isEmpty()) {
			int d = loops.iterator().nextInt();
			loops.rem(d);
			stack.add(d);
			for (PortalConfiguration pc = get(d), pc1 = pc.up(); pc1 != pc; pc1 = pc1.up()) {
				int d1 = pc1.dimId;
				loops.rem(d1);
				stack.add(d1);
			}
			stack.add(d);
			stacks.add(stack.toIntArray());
			stack.clear();
		}
		return stacks;
	}

	/**
	 * load or initialize dimension settings for a specific world
	 * @param dir world save directory
	 */
	public void loadWorldSettings(File dir) {
		boolean reload = true;
		if (defaultCfg == null) {
			defaultCfg = new NBTTagCompound();
			save(defaultCfg, false);
			reload = false;
		}
		dir.mkdirs();
		File file = new File(dir, "dimensionstack.dat");
		try { 
			do {
				if (file.exists()) {
					NBTTagCompound nbt = CompressedStreamTools.read(file);
					int v = nbt.getByte("version") & 0xff;
					if (v >= FILE_VERSION) {
						load(nbt);
						Main.LOG.info("Dimension stack configuration file for world {} sucessfully loaded.", dir.getName());
						reload = false;
						break;//skip
					}
					Main.LOG.warn("Dimension stack configuration file has outdated format!");
					file.renameTo(new File(dir, "dimensionstack.dat.old"));
				}
				CompressedStreamTools.write(defaultCfg, file);
				Main.LOG.info("new dimension stack configuration file for world {} sucessfully created.", dir.getName());
			} while(false);//end skip
			cfgFile = file;
		} catch (IOException e) {
			cfgFile = null;
			e.printStackTrace();
		}
		if (reload) load(defaultCfg);
	}

	/**
	 * initialize the default configuration from a preset file
	 * @param file preset
	 * @return success
	 */
	public boolean loadPreset(File file) {
		if (!file.exists()) {
			Main.LOG.error("Given preset dimension stack configuration file {} doesn't exist: loading config normally", file);
			return false;
		}
		try {
			NBTTagCompound nbt = CompressedStreamTools.read(file);
			int v = nbt.getByte("version") & 0xff;
			if (v < FILE_VERSION) {
				Main.LOG.warn("Preset dimension stack configuration file has outdated format!");
				return false;
			}
			load(nbt);
			Main.LOG.info("Preset dimension stack configuration file sucessfully loaded.");
			return true;
		} catch(IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * resets the current dimension configuration to default
	 */
	public void reset() {
		if (defaultCfg == null) return;
		load(defaultCfg);
		defaultCfg = null;
	}

	@Override
	public void registerOreDisable() {
		OreGenHandler.register();
	}

	@Override
	public void addRecipe(Parameters param) {
		String rcp = param.getString(0);
		if (rcp.equals(DIMENSION_STACK)) {
			double[] vec = param.getVectorOrAll(1);
			int[] stack = new int[vec.length];
			for (int i = 0; i < vec.length; i++)
				stack[i] = (int)vec[i];
			PortalConfiguration.link(stack);
		} else if (rcp.equals(DIMENSION)) {
			int id = param.getIndex(1);
			setDimCreation(id, true);
			if (param.has(3)) {
				CustomWorldProps props = PortalConfiguration.get(id).getSettings(CustomWorldProps.class, true);
				switch(param.getString(2)) {
				case "worldtype": props.chunkGen = 1; break;
				case "overworld": props.chunkGen = 2; break;
				case "nether": props.chunkGen = 3; break;
				}
				props.biomeGen = param.param[3] == Nil.NIL ? "" : param.getString(3);
				if (param.has(10)) {
					props.horizonHeight = (float)param.getNumber(4);
					props.cloudHeight = (float)param.getNumber(5);
					for (int i = 0; i < 5; i++) {
						if (param.getBool(i + 6))
							props.flags |= 1 << i;
					}
					double[] vec = param.getVectorOrAll(11);
					if (vec.length == 4)
						props.fogColor = MathHelper.clamp((int)vec[0], 0, 255) << 24
							| MathHelper.clamp((int)(vec[1] * 255D), 0, 255) << 16
							| MathHelper.clamp((int)(vec[2] * 255D), 0, 255) << 8
							| MathHelper.clamp((int)(vec[3] * 255D), 0, 255);
				}
			}
		}
	}

	public static void initConfig(ConfigConstants cfg) {
		defaultCeilY = (int)cfg.getNumber("dim_ceiling", defaultCeilY);
		cfg.get("dim_ceiling", CeilingInfo.class, new CeilingInfo());
	}

	private static class CeilingInfo implements IOperand {

		@Override
		public boolean asBool() throws Error {return true;}
		@Override
		public Object value() {return this;}

		@Override
		public IOperand get(IOperand idx) {
			return new Number(PortalConfiguration.get(idx.asIndex()).ceilY);
		}

		@Override
		public void put(IOperand idx, IOperand val) {
			int y = val.asIndex();
			boolean flip = y < 0;
			y = Math.abs(y);
			if (y <= 0 || y >= 256) {
				Lib.LOG.error(RecipeScriptContext.ERROR, "script attempted to set portal ceiling height to an invalid value {}", y);
				return;
			}
			PortalConfiguration pc = PortalConfiguration.get(idx.asIndex());
			pc.ceilY = y;
			pc.flipped = flip;
		}

	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerConfigGui(ICfgButtonHandler handler) {
		((ClientProxy)Main.proxy).cfgButtons.add(handler);
	}

	//synchronize to configuration to client

	@SubscribeEvent
	public void handShakeComplete(CustomNetworkEvent event) {
		if (!(event.getWrappedEvent() instanceof NetworkHandshakeEstablished)) return;
		NetworkHandshakeEstablished nhse = (NetworkHandshakeEstablished)event.getWrappedEvent();
		if (nhse.side != Side.SERVER) return;
		String player = ((NetHandlerPlayServer)nhse.netHandler).player.getName();
		if (nhse.dispatcher.manager.isLocalChannel()) {
			Main.LOG.info("Skipping dimension stack synchronization for integrated server owner {}", player);
			return;
		}
		Main.LOG.info("Sending dimension stack configuration packet to {}", player);
		NBTTagCompound nbt = new NBTTagCompound();
		save(nbt, true);
		nhse.dispatcher.sendProxy(new FMLProxyPacket(new PacketBuffer(Unpooled.buffer()).writeCompoundTag(nbt), CHANNEL));
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onPacketFromServer(ClientCustomPacketEvent event) {
		if (event.getManager().isLocalChannel()) {
			Main.LOG.warn("Ignoring dimension stack configuration packet for singleplayer ... which shouldn't have been send actually!");
			return;
		}
		try {
			NBTTagCompound nbt = ((PacketBuffer)event.getPacket().payload()).readCompoundTag();
			load(nbt);
			Main.LOG.info("Dimension stack configuration from server sucessfully synchronized!");
		} catch(IOException e) {
			Main.LOG.error("Failed to decode dimension stack configuration packet from server:", e);
		}
	}

}
