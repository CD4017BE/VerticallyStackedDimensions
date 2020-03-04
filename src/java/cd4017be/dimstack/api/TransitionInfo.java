package cd4017be.dimstack.api;

import cd4017be.dimstack.api.util.BlockPredicate;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants.NBT;


/**
 * @author CD4017BE
 *
 */
public class TransitionInfo implements IDimensionSettings {

	public IBlockState blockTop, blockBot;
	public int sizeTop, sizeBot;
	private boolean initialized = false;

	@Override
	public NBTBase serializeNBT() {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setShort("sB", (short)sizeBot);
		nbt.setShort("sT", (short)sizeTop);
		if (blockBot != null) nbt.setString("bB", BlockPredicate.serialize(blockBot));
		if (blockTop != null) nbt.setString("bT", BlockPredicate.serialize(blockTop));
		return nbt;
	}

	@Override
	public void deserializeNBT(NBTBase nbt) {
		NBTTagCompound ctag = (NBTTagCompound)nbt;
		sizeBot = ctag.getShort("sB");
		sizeTop = ctag.getShort("sT");
		blockBot = ctag.hasKey("bB", NBT.TAG_STRING) ? BlockPredicate.parse(ctag.getString("bB")) : null;
		blockTop = ctag.hasKey("bT", NBT.TAG_STRING) ? BlockPredicate.parse(ctag.getString("bT")) : null;
	}

	public boolean init() {
		if (initialized) return false;
		initialized = true;
		return true;
	}

}
