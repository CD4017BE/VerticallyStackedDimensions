package cd4017be.dimstack.api;

import java.util.ArrayList;

import cd4017be.dimstack.api.util.BlockPredicate;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.Constants.NBT;

/**
 * Stores persistent information for a dimension about where and how certain blocks (like Bedrock) are replaced with another block.
 * @author CD4017BE
 */
public class BlockReplacements implements IDimensionSettings {

	public final ArrayList<Replacement> replacements = new ArrayList<>();

	@Override
	public NBTTagCompound serializeNBT() {
		if (replacements.isEmpty()) return null;
		NBTTagList list = new NBTTagList();
		for (Replacement r : replacements) {
			NBTTagCompound tag = new NBTTagCompound();
			tag.setShort("minY", (short)r.minY);
			tag.setShort("maxY", (short)r.maxY);
			tag.setTag("target", r.target.writeNBT());
			tag.setString("block", BlockPredicate.serialize(r.repl));
			list.appendTag(tag);
		}
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setTag("entries", list);
		return nbt;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		NBTTagList list = nbt.getTagList("entries", NBT.TAG_COMPOUND);
		for (NBTBase tag : list) {
			NBTTagCompound ctag = (NBTTagCompound)tag;
			try {
				Replacement r = new Replacement(
					BlockPredicate.loadNBT(ctag.getTagList("target", NBT.TAG_STRING)),
					BlockPredicate.parse(ctag.getString("block")),
					ctag.getShort("minY"),
					ctag.getShort("maxY")
				);
				replacements.add(r);
			} catch (Exception e) {}
		}
	}

	public static class Replacement {

		/** Block to search for */
		public final BlockPredicate target;
		/** BlockState to replace with */
		public final IBlockState repl;
		/** where to search */
		public final int minY, maxY;

		public Replacement(BlockPredicate target, IBlockState repl, int minY, int maxY) {
			this.target = target;
			this.repl = repl;
			this.minY = Math.max(0, minY);
			this.maxY = Math.min(256, maxY);
		}

		public void doReplace(World world, int cx, int cz) {
			Chunk chunk = world.getChunkFromChunkCoords(cx, cz);
			MutableBlockPos pos = new MutableBlockPos();
			BlockPredicate target = this.target;
			IBlockState repl = this.repl;
			int x0 = cx << 4, x1 = x0 + 16,
				y0 = this.minY, y1 = this.maxY;
			for (int z = cz << 4, z1 = z + 16; z < z1; z++)
				for (int x = x0; x < x1; x++)
					for (int y = y0; y < y1; y++)
						if (target.test(chunk.getBlockState(pos.setPos(x, y, z))))
							chunk.setBlockState(pos, repl);
		}

	}

}