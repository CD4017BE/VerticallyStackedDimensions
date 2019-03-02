package cd4017be.dimstack.api.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import cd4017be.dimstack.api.IDimensionSettings;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

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
	protected void deserializeNBT(NBTTagList list, Map<String, Function<NBTTagCompound, T>> registry) {
		entries.clear();
		for (NBTBase tag : list) {
			NBTTagCompound ctag = (NBTTagCompound)tag;
			Function<NBTTagCompound, T> f = registry.get(ctag.getString("id"));
			if (f != null) entries.add(f.apply(ctag));
		}
	}

	@Override
	public NBTBase serializeNBT() {
		NBTTagList list = new NBTTagList();
		for (T e : entries) {
			NBTTagCompound tag = e.writeNBT();
			if (tag == null) continue;
			tag.setString("id", e.getRegistryName());
			list.appendTag(tag);
		}
		return list;
	}

}
