package cd4017be.dimstack.worldgen;

import java.util.Random;

import cd4017be.dimstack.api.util.BlockPredicate;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.chunk.Chunk;

/**
 * 
 * @author CD4017BE
 */
public class OreGenGaussian extends OreGenBase {

	final float center, var;

	public OreGenGaussian(NBTTagCompound tag) {
		super(tag);
		this.center = tag.getFloat("mainY");
		this.var = tag.getFloat("devY");
	}

	public OreGenGaussian(IBlockState ore, int size, float veins, BlockPredicate target, float center, float var) {
		super(ore, size, veins, target);
		this.center = center;
		this.var = var;
	}

	@Override
	public void generate(Chunk chunk, Random rand) {
		int x = chunk.x << 4, z = chunk.z << 4;
		for (int n = veins(rand); n > 0; n--)
			genOreVein(chunk.getWorld(), x + rand.nextInt(16), MathHelper.floor(center + var * (float)rand.nextGaussian()), z + rand.nextInt(16), rand);
	}

	@Override
	public NBTTagCompound writeNBT() {
		NBTTagCompound nbt = super.writeNBT();
		nbt.setFloat("mainY", center);
		nbt.setFloat("devY", var);
		return nbt;
	}

	@Override
	public String getRegistryName() {
		return "gauss";
	}

}