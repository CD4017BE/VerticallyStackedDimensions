package cd4017be.dimstack.core;

import java.io.File;
import java.io.IOException;

import cd4017be.api.recipes.RecipeAPI;
import cd4017be.api.recipes.RecipeAPI.IRecipeHandler;
import cd4017be.dimstack.Main;
import cd4017be.dimstack.api.API;
import cd4017be.dimstack.api.IDimension;
import cd4017be.dimstack.api.IDimensionSettings;
import cd4017be.dimstack.api.util.SettingProvider;
import cd4017be.dimstack.worldgen.OreGenHandler;
import cd4017be.lib.script.Parameters;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.util.Constants.NBT;
import static cd4017be.dimstack.core.PortalConfiguration.*;

/**
 * @author CD4017BE
 *
 */
public class Dimensionstack extends API implements IRecipeHandler {

	private static String DIMENSION_STACK = "dimstack";
	private static final int FILE_VERSION = 3;
	private NBTTagCompound defaultCfg;
	private File cfgFile;

	public Dimensionstack() {
		API.INSTANCE = this;
		RecipeAPI.Handlers.put(DIMENSION_STACK, this);
	}

	@Override
	public IDimension getDim(int id) {
		return get(id);
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
				data.setIntArray("topOpen", open.toIntArray());
				CompressedStreamTools.write(data, cfgFile);
				Main.LOG.info("updated dimension stack configuration file");
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

	private static void loadSettings(SettingProvider sp, NBTTagCompound cfg) {
		for (String key : cfg.getKeySet()) {
			try {
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
	}

	private static NBTTagCompound saveSettings(SettingProvider sp) {
		NBTTagCompound cfg = new NBTTagCompound();
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
	public void load(NBTTagCompound nbt) {
		dimensions.clear();
		NBTTagList stacks = nbt.getTagList("stacks", NBT.TAG_INT_ARRAY);
		for (NBTBase tag : stacks)
			link(((NBTTagIntArray)tag).getIntArray());
		for (int dim : nbt.getIntArray("topOpen")) {
			PortalConfiguration pc = get(dim);
			if (pc.up() != null) pc.topOpen = true;
		}
		getAllSettings().clear();
		for (String key : nbt.getKeySet())
			try {
				SettingProvider sp = key.equals("global") ? this : get(Integer.parseInt(key));
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
	public void save(NBTTagCompound nbt) {
		IntOpenHashSet bottoms = new IntOpenHashSet(), loops = new IntOpenHashSet();
		NBTTagCompound cfg = saveSettings(this);
		if (!cfg.hasNoTags())
			nbt.setTag("global", cfg);
		for (PortalConfiguration pc : dimensions.values()) {
			PortalConfiguration bot = pc.bottom();
			if (bot == null) loops.add(pc.dimId);
			else if (bot != pc) bottoms.add(bot.dimId);
			if (!(cfg = saveSettings(pc)).hasNoTags())
				nbt.setTag(pc.toString(), cfg);
		}
		NBTTagList stacks = new NBTTagList();
		IntArrayList stack = new IntArrayList();
		for (int d : bottoms) {
			stack.add(d);
			for (PortalConfiguration pc = get(d), pc1; (pc1 = pc.up()) != null; pc = pc1)
				stack.add(pc1.dimId);
			stacks.appendTag(new NBTTagIntArray(stack.toIntArray()));
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
			stacks.appendTag(new NBTTagIntArray(stack.toIntArray()));
			stack.clear();
		}
		nbt.setTag("stacks", stacks);
		nbt.setByte("version", (byte)FILE_VERSION);
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

}
