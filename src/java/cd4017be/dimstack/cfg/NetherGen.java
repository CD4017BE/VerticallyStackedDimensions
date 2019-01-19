package cd4017be.dimstack.cfg;

import net.minecraft.nbt.NBTTagCompound;


/**
 * @author CD4017BE
 *
 */
public class NetherGen implements IDimensionSettings {

	public enum Type {SOLID_ROCK, MIRROR_NETHER}

	public Type genMode;
	public boolean stoneVariants;

	@Override
	public NBTTagCompound serializeNBT() {
		if (genMode == null) return null;
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setBoolean("stoneVar", stoneVariants);
		nbt.setByte("mode", (byte)genMode.ordinal());
		return nbt;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		stoneVariants = nbt.getBoolean("stoneVar");
		Type[] v = Type.values();
		genMode = v[Math.min(nbt.getByte("mode") & 0xff, v.length - 1)];
	}

}
