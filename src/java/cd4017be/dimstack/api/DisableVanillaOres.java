package cd4017be.dimstack.api;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.terraingen.OreGenEvent.GenerateMinable.EventType;

/**
 * Stores persistent information for a dimension about disabled vanilla ore generation.
 * @author CD4017BE
 */
public class DisableVanillaOres implements IDimensionSettings {

	private short disabled;

	/** re-enable all ores in this dimension */
	public void reset() {
		disabled = 0;
	}

	/**
	 * @param ore ore generator type
	 * @return whether given ore generator is disabled for this dimension
	 */
	public boolean disabled(EventType ore) {
		return (disabled & 1 << ore.ordinal()) != 0;
	}

	/**
	 * disable given ore generator for this dimension
	 * @param ore ore generator type
	 */
	public void disable(EventType ore) {
		disabled |= 1 << ore.ordinal();
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
	public void deserializeNBT(NBTTagCompound nbt) {
		reset();
		for (EventType t : EventType.values())
			if (nbt.getBoolean(t.name().toLowerCase()))
				disable(t);
		if (disabled != 0) API.INSTANCE.registerOreDisable();
	}

}