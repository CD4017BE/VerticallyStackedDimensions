package cd4017be.dimstack.api.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import cd4017be.dimstack.api.IDimensionSettings;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.common.util.Constants.NBT;

/**
 * Template for a dimension setting consisting of a list of elements {@link #entries} with varying implementation.
 * @see #deserializeNBT(NBTTagCompound, Map)
 * @author CD4017BE
 */
public abstract class CfgList<T extends ICfgListEntry> implements IDimensionSettings {

	public final List<T> entries = new ArrayList<>();

	/**
	 * loads the element list from nbt based on the given registry
	 * @param nbt serialized data
	 * @param registry a registry that maps element type IDs to instance generator functions.
	 */
	protected void deserializeNBT(NBTTagCompound nbt, Map<String, Function<NBTTagCompound, T>> registry) {
		NBTTagList list = nbt.getTagList("ids", NBT.TAG_STRING);
		int l = list.tagCount();
		String[] ids = new String[l];
		for (int i = 0; i < l; i++)
			ids[i] = list.getStringTagAt(i);
		entries.clear();
		for (NBTBase tag : nbt.getTagList("entries", NBT.TAG_COMPOUND)) {
			NBTTagCompound ctag = (NBTTagCompound)tag;
			int id = ctag.getByte("id") & 0xff;
			if (id < ids.length) continue;
			Function<NBTTagCompound, T> f = registry.get(ids[id]);
			if (f != null) entries.add(f.apply(ctag));
		}
	}

	@Override
	public NBTTagCompound serializeNBT() {
		ArrayList<String> ids = new ArrayList<>();
		NBTTagList list = new NBTTagList();
		for (T e : entries) {
			String loc = e.getRegistryName();
			int i = ids.indexOf(loc);
			if (i < 0) {
				i = ids.size();
				ids.add(loc);
			}
			NBTTagCompound tag = e.writeNBT();
			tag.setByte("id", (byte)i);
			list.appendTag(tag);
		}
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setTag("entries", list);
		list = new NBTTagList();
		for (String loc : ids)
			list.appendTag(new NBTTagString(loc));
		nbt.setTag("ids", list);
		return nbt;
	}

}
