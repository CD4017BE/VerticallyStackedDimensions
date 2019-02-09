package cd4017be.dimstack.worldgen;

import java.util.Random;

import cd4017be.dimstack.api.TerrainGeneration;
import cd4017be.dimstack.api.gen.ITerrainGenerator;
import cd4017be.dimstack.api.util.BlockPredicate;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;


/**
 * Generates Bedrock like block layers.
 * @author CD4017BE
 */
public class SimpleLayerGen implements ITerrainGenerator {
	public static final String ID = "basicLayer";

	final IBlockState block;
	final int minY, maxY, extB, extT;

	@Override
	public String getRegistryName() {
		return ID;
	}

	public SimpleLayerGen(IBlockState block, int minY, int maxY, int extB, int extT) {
		this.block = block;
		this.minY = minY;
		this.maxY = maxY - 1;
		this.extB = extB;
		this.extT = extT;
	}

	public SimpleLayerGen(NBTTagCompound nbt) {
		this.block = BlockPredicate.parse(nbt.getString("block"));
		this.minY = nbt.getByte("minY") & 0xff;
		this.maxY = nbt.getByte("maxY") & 0xff;
		this.extB = nbt.getByte("extB") & 0xff;
		this.extT = nbt.getByte("extT") & 0xff;
	}

	@Override
	public NBTTagCompound writeNBT() {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setString("block", BlockPredicate.serialize(block));
		nbt.setByte("minY", (byte)minY);
		nbt.setByte("maxY", (byte)maxY);
		nbt.setByte("extB", (byte)extB);
		nbt.setByte("extT", (byte)extT);
		return nbt;
	}

	@Override
	public void generate(IChunkGenerator gen, ChunkPrimer cp, int cx, int cz, TerrainGeneration cfg) {
		Random rand = cfg.rand;
		int eb = extB + 1, et = extT + 1,
			y0 = minY, y1 = maxY;
		for (int x = 0; x < 16; x++)
			for (int z = 0; z < 16; z++) {
				for (int y = y0; y <= y1; y++)
					cp.setBlockState(x, y, z, block);
				for (int d = 1; d < eb; d++)
					if (d <= rand.nextInt(eb))
						cp.setBlockState(x, y0 - d, z, block);
				for (int d = 1; d < et; d++)
					if (d <= rand.nextInt(et))
						cp.setBlockState(x, y1 + d, z, block);
			}
	}

}
