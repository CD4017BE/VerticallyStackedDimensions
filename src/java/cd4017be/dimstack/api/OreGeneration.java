package cd4017be.dimstack.api;

import java.util.HashMap;
import java.util.function.Function;

import cd4017be.dimstack.api.gen.IOreGenerator;
import cd4017be.dimstack.api.util.CfgList;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;


/**
 * Holds information about ore generation for a dimension.<br>
 * Use {@link #REGISTRY} to register custom IOreGenerators.
 * @author CD4017BE
 */
public class OreGeneration extends CfgList<IOreGenerator> {

	/** OreGenerator registry */
	public static final HashMap<String, Function<NBTTagCompound, IOreGenerator>> REGISTRY = new HashMap<>();

	@Override
	public void deserializeNBT(NBTBase nbt) {
		deserializeNBT((NBTTagList)nbt, REGISTRY);
	}

}
