package cd4017be.dimstack.api;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

/** @author CD4017BE */
public class CustomWorldProps implements IDimensionSettings {

	public boolean skylight;
	public boolean noWater;
	public boolean netherlike;
	public boolean visibleSky;
	public String biomeGen = "";
	public int chunkGen = 0;

	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setBoolean("skyl", skylight);
		nbt.setBoolean("skyv", visibleSky);
		nbt.setBoolean("evap", noWater);
		nbt.setBoolean("nether", netherlike);
		nbt.setString("biome", biomeGen);
		nbt.setByte("gen", (byte)chunkGen);
		return nbt;
	}

	@Override
	public void deserializeNBT(NBTBase tag) {
		NBTTagCompound nbt = (NBTTagCompound)tag;
		skylight = nbt.getBoolean("skyl");
		visibleSky = nbt.getBoolean("skyv");
		noWater = nbt.getBoolean("evap");
		netherlike = nbt.getBoolean("nether");
		biomeGen = nbt.getString("biome");
		chunkGen = nbt.getByte("gen") & 0xff;
	}

}
