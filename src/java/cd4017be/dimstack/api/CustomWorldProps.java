package cd4017be.dimstack.api;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

/** @author CD4017BE */
public class CustomWorldProps implements IDimensionSettings {

	public static final int F_SKYLIGHT = 1, F_WATEREVAP = 2, F_NETHER = 4, F_SKYBOX = 8, F_FOG = 16;

	public int flags;
	public String biomeGen = "";
	public int chunkGen = 0;
	//client:
	public float horizonHeight = 64;
	public float cloudHeight = 120;
	public int fogColor = 0x08c0d9ff;

	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setByte("flags", (byte)flags);
		nbt.setString("biome", biomeGen);
		nbt.setByte("gen", (byte)chunkGen);
		nbt.setInteger("fog", fogColor);
		nbt.setFloat("horizon", horizonHeight);
		nbt.setFloat("clouds", cloudHeight);
		return nbt;
	}

	@Override
	public void deserializeNBT(NBTBase tag) {
		NBTTagCompound nbt = (NBTTagCompound)tag;
		flags = nbt.getByte("flags");
		biomeGen = nbt.getString("biome");
		chunkGen = nbt.getByte("gen") & 0xff;
		fogColor = nbt.getInteger("fog");
		horizonHeight = nbt.getFloat("horizon");
		cloudHeight = nbt.getFloat("clouds");
	}

	@Override
	public boolean isClientRelevant() {
		return true;
	}

}
