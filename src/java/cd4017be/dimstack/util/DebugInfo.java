package cd4017be.dimstack.util;

import cd4017be.dimstack.api.IDimensionSettings;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.nbt.NBTTagByte;

/**
 * 
 * @author cd4017be
 */
public class DebugInfo implements IDimensionSettings {

	public boolean chunksGenerated = false;

	@Override
	public NBTBase serializeNBT() {
		return new NBTTagByte((byte) (chunksGenerated ? 1 : 0));
	}

	@Override
	public void deserializeNBT(NBTBase nbt) {
		byte state = ((NBTPrimitive)nbt).getByte();
		chunksGenerated = (state & 1) != 0;
	}

}
