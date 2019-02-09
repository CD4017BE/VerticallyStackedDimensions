package cd4017be.dimstack.api;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;


/**
 * API for dimension settings that store custom data alongside a dimension.<br>
 * Implementors should provide an empty constructor for instantiation.
 * @see IDimension#getSettings(Class, boolean)
 * @author CD4017BE
 */
public interface IDimensionSettings extends INBTSerializable<NBTTagCompound> {

	public static <T extends IDimensionSettings> T newInstance(Class<T> type) {
		try {
			return type.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
			return null;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			return null;
		}
	}

}
