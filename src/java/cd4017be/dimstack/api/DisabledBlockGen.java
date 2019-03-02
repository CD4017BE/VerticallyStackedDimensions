package cd4017be.dimstack.api;

import cd4017be.dimstack.api.util.BlockPredicate;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagString;


/**
 * 
 * @author CD4017BE
 */
public class DisabledBlockGen implements IDimensionSettings {

	/** A block that should be prevented from generation during regular world-gen code */
	public IBlockState disabledBlock = null;

	@Override
	public NBTBase serializeNBT() {
		return disabledBlock == null ? null : new NBTTagString(BlockPredicate.serialize(disabledBlock));
	}

	@Override
	public void deserializeNBT(NBTBase nbt) {
		String s = ((NBTTagString)nbt).getString();
		this.disabledBlock = s.isEmpty() ? null : BlockPredicate.parse(s);
	}

}
