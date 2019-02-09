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
public class OreGenCentered extends OreGenBase {

	final int baseY;
	final float min, max;

	public OreGenCentered(NBTTagCompound tag) {
		super(tag);
		this.baseY = tag.getShort("mainY");
		this.min = baseY - tag.getShort("minY");
		this.max = tag.getShort("maxY") - baseY;
	}

	public OreGenCentered(IBlockState state, int numB, float veins, BlockPredicate target, int minH, int mainH, int maxH) {
		super(state, numB, veins, target);
		this.min = mainH - minH;
		this.max = maxH - mainH;
		this.baseY = mainH;
	}

	public void generate(Chunk chunk, Random rand) {
		int x = chunk.x << 4, z = chunk.z << 4;
		boolean side = max < min;
		int r;
		float f;
		for (int n = veins(rand); n > 0; n--) {
			r = rand.nextInt();//split into: x[0...15], z[0...15], f[-4095...4095] more dense towards 0
			f = (float)((r & 0xfff) + (r >> 12 & 0xfff) - 4095) / 4095F;
			if (side) {
				f *= min;
				if (f > max) f = max - (f - max) / (min - max) * (min + max);
			} else {
				f *= max;
				if (f > min) f = min - (f - min) / (max - min) * (max + min);
				f = -f;
			}
			genOreVein(chunk.getWorld(), x + (r >> 24 & 0xf), baseY + MathHelper.floor(f), z + (r >> 28 & 0xf), rand);
		}
	}

	@Override
	public NBTTagCompound writeNBT() {
		NBTTagCompound nbt = super.writeNBT();
		nbt.setShort("mainY", (short)baseY);
		nbt.setShort("minY", (short)(baseY - min));
		nbt.setShort("maxY", (short)(baseY + max));
		return nbt;
	}

	@Override
	public String getRegistryName() {
		return "center";
	}

}