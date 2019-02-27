package cd4017be.dimstack.api;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.terraingen.OreGenEvent.GenerateMinable.EventType;

/**
 * Stores persistent information for a dimension about disabled vanilla ore generation.
 * @author CD4017BE
 */
public class DisableVanillaOres implements IDimensionSettings {

	private long disabled;

	/**
	 * @param ore ore generator type
	 * @return whether given ore generator is disabled for this dimension
	 */
	public boolean disabled(EventType ore) {
		return (disabled & 1L << ore.ordinal()) != 0;
	}

	/**
	 * set the disabled state of given ore generator for this dimension
	 * @param ore ore generator type
	 * @param d whether to disable
	 */
	public void setDisabled(EventType ore, boolean d) {
		if (d) disabled |= 1L << ore.ordinal();
		else disabled &= ~(1L << ore.ordinal());
	}

	@SuppressWarnings("deprecation")
	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound nbt = new NBTTagCompound();
		for (EventType t : EventType.values())
			if (disabled(t))
				nbt.setBoolean(t.name().toLowerCase(), true);
		if (disabled != 0) API.INSTANCE.registerOreDisable();
		return nbt;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void deserializeNBT(NBTBase nbt) {
		for (EventType t : EventType.values())
			setDisabled(t, ((NBTTagCompound)nbt).getBoolean(t.name().toLowerCase()));
		if (disabled != 0) API.INSTANCE.registerOreDisable();
	}

}