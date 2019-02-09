package cd4017be.dimstack.api.util;

import net.minecraft.nbt.NBTTagCompound;

/**
 * Element of a {@link CfgList} dimension setting
 * @author CD4017BE
 */
public interface ICfgListEntry {

	/**
	 * @return the registry id of this element for (de-)serialization.
	 */
	String getRegistryName();

	/**
	 * @return this element serialized to NBT
	 */
	NBTTagCompound writeNBT();

}