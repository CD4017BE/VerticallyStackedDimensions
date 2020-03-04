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
import cd4017be.dimstack.api.API;
import cd4017be.dimstack.api.IDimension;
import cd4017be.dimstack.api.IDimensionSettings;
import cd4017be.dimstack.api.util.ICfgButtonHandler;
import cd4017be.dimstack.api.util.SettingProvider;
import cd4017be.dimstack.worldgen.OreGenHandler;
import cd4017be.lib.Lib;
import cd4017be.lib.script.Parameters;
import cd4017be.lib.script.obj.Error;
import cd4017be.lib.script.obj.IOperand;
import cd4017be.lib.script.obj.Number;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static cd4017be.dimstack.core.PortalConfiguration.*;

/**
 * @author CD4017BE
 *
 */
public class Dimensionstack extends API implements IRecipeHandler {

	private static String DIMENSION_STACK = "dimstack";
	public static final int FILE_VERSION = 4;
	private NBTTagCompound defaultCfg;
	private File cfgFile;
	private static boolean cfgModified;

	public Dimensionstack() {
		API.INSTANCE = this;
		RecipeAPI.Handlers.put(DIMENSION_STACK, this);
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
				save(data);
				data.setIntArray("topOpen", open.toIntArray());
				CompressedStreamTools.write(data, cfgFile);
				Main.LOG.info("updated dimension stack configuration file");
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

	private static void loadSettings(SettingProvider sp, NBTTagCompound cfg) {
		if (sp instanceof PortalConfiguration) {
			PortalConfiguration pc = (PortalConfiguration)sp;
			int y = cfg.getByte("ceil") & 0xff;
			pc.ceilY = y > 0 ? y : 255;
			pc.flipped = cfg.getBoolean("flip");
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

	private static NBTTagCompound saveSettings(SettingProvider sp) {
		NBTTagCompound cfg = new NBTTagCompound();
		if (sp instanceof PortalConfiguration) {
			PortalConfiguration pc = (PortalConfiguration)sp;
			cfg.setByte("ceil", (byte)pc.ceilY);
			cfg.setBoolean("flip", pc.flipped);
		}
		for (IDimensionSettings s : sp.getAllSettings()) {
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
	public static void save(NBTTagCompound nbt) {
		NBTTagCompound cfg = saveSettings(INSTANCE);
		if (!cfg.hasNoTags())
			nbt.setTag("global", cfg);
		for (PortalConfiguration pc : dimensions.values())
			if (!(cfg = saveSettings(pc)).hasNoTags())
				nbt.setTag(Integer.toString(pc.dimId), cfg);
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
			save(defaultCfg);
			reload = false;
		}
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
			Main.LOG.error("Given preset dimension stack configuration file {} doesn't exist!", file);
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
		double[] vec = param.getVectorOrAll(1);
		int[] stack = new int[vec.length];
		for (int i = 0; i < vec.length; i++)
			stack[i] = (int)vec[i];
		PortalConfiguration.link(stack);
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

}
