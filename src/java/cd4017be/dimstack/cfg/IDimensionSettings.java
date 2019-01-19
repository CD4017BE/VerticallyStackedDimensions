package cd4017be.dimstack.cfg;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;


/**
 * @author CD4017BE
 *
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
