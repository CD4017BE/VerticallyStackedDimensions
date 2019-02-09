package cd4017be.dimstack.api.oregen;

import java.util.Random;

import cd4017be.dimstack.api.OreGenerator;
import cd4017be.dimstack.api.util.BlockPredicate;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.chunk.Chunk;

/**
 * 
 * @author CD4017BE
 */
public class OreGenEven extends OreGenerator {

	final int baseY, height;

	public OreGenEven(NBTTagCompound tag) {
		super(tag);
		this.baseY = tag.getShort("minY");
		this.height = tag.getShort("maxY") - baseY;
	}

	public OreGenEven(IBlockState state, int numB, float veins, BlockPredicate target, int minH, int maxH) {
		super(state, numB, veins, target);
		this.baseY = minH < maxH ? minH : maxH;
		this.height = minH < maxH ? maxH - minH : minH - maxH;
	}

	public void generate(Chunk chunk, Random rand) {
		int x = chunk.x << 4, z = chunk.z << 4;
		for (int n = veins(rand); n > 0; n--)
			genOreVein(chunk.getWorld(), x + rand.nextInt(16), baseY + rand.nextInt(height), z + rand.nextInt(16), rand);
	}

	@Override
	public NBTTagCompound writeNBT() {
		NBTTagCompound nbt = super.writeNBT();
		nbt.setShort("minY", (short)baseY);
		nbt.setShort("maxY", (short)(baseY + height));
		return nbt;
	}

	@Override
	public String getRegistryName() {
		return "even";
	}

}