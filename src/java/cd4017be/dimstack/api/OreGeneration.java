package cd4017be.dimstack.api;

import java.util.HashMap;
import java.util.function.Function;

import cd4017be.dimstack.api.util.CfgList;
import net.minecraft.nbt.NBTTagCompound;


/**
 * Holds information about ore generation for a dimension.<br>
 * Use {@link #REGISTRY} to register custom OreGenerators.
 * @author CD4017BE
 */
public class OreGeneration extends CfgList<OreGenerator> {

	/** OreGenerator registry */
	public static final HashMap<String, Function<NBTTagCompound, OreGenerator>> REGISTRY = new HashMap<>();

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		deserializeNBT(nbt, REGISTRY);
	}

}
